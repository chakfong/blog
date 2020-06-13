package com.chakfong.blog.service;


import com.chakfong.blog.dao.DynamicCommentRepository;
import com.chakfong.blog.dao.DynamicLikeRepository;
import com.chakfong.blog.dao.DynamicPictureRepository;
import com.chakfong.blog.dao.DynamicRepository;
import com.chakfong.blog.dto.request.PostCommentDto;
import com.chakfong.blog.dto.request.PostDynamicDto;
import com.chakfong.blog.dto.response.DynamicCommentDto;
import com.chakfong.blog.dto.response.DynamicDto;
import com.chakfong.blog.entity.*;
import com.chakfong.blog.entity.status_enum.DynamicVisibleStatus;
import com.chakfong.blog.entity.status_enum.IBaseStatusEnum;
import com.chakfong.blog.exception.ErrorCode;
import com.chakfong.blog.exception.ServiceException;
import com.chakfong.blog.utils.RedisKeys;
import com.chakfong.blog.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

import static com.chakfong.blog.utils.ServiceExceptionUtils.*;

@Service
@Slf4j
public class DynamicService {

    private final DynamicRepository dynamicRepository;

    private final DynamicCommentRepository dynamicCommentRepository;

    private final DynamicPictureRepository dynamicPictureRepository;

    private final DynamicLikeRepository dynamicLikeRepository;

    private final UserService userService;

    private final SftpService sftpService;

    private final RedissonClient redissonClient;

    public DynamicService(DynamicRepository dynamicRepository, DynamicCommentRepository dynamicCommentRepository,
                          DynamicPictureRepository dynamicPictureRepository,
                          DynamicLikeRepository dynamicLikeRepository, UserService userService,
                          SftpService sftpService, RedissonClient redissonClient) {
        this.dynamicRepository = dynamicRepository;
        this.dynamicCommentRepository = dynamicCommentRepository;
        this.dynamicPictureRepository = dynamicPictureRepository;
        this.dynamicLikeRepository = dynamicLikeRepository;
        this.userService = userService;
        this.sftpService = sftpService;
        this.redissonClient = redissonClient;
    }

    public List<DynamicDto> getPublicDynamic() {
        List<DynamicDto> publicDynamics = dynamicRepository.findDynamicsByVisibleIsOrderByCreateTime(DynamicVisibleStatus.PUBLIC.getValue()).stream()
                .map(Dynamic::toDynamicDto)
                .collect(Collectors.toList());

        if (!userService.hasLogin())
            return publicDynamics;
        // 若登录状态，再添加被列为白名单或好友的动态列表。
        Long userId = userService.getUserWithAuthorities().getUserId();
        List<Long> friendsIds = userService.getAllFriend().stream().map(User::getUserId).collect(Collectors.toList());
        List<Long> whitelistIds = userService.getUsersInWhitelist(userId);
        friendsIds.addAll(whitelistIds);
        publicDynamics.addAll(dynamicRepository.findDistinctDynamicsByUser_UserIdIn(friendsIds)
                .stream()
                .map(Dynamic::toDynamicDto)
                .collect(Collectors.toList()));
        return publicDynamics;
    }


    public List<DynamicDto> getDynamicWithAuthorities() {
        Long userId = userService.getUserWithAuthorities().getUserId();
        return dynamicRepository.findDynamicsByUser_UserIdIs(userId).stream()
                .map(Dynamic::toDynamicDto)
                .collect(Collectors.toList());
    }


    public List<DynamicDto> getUserDynamic(Long userId) {
        List<DynamicDto> allDynamics = dynamicRepository.findDynamicsByUser_UserIdIs(userId).stream()
                .map(Dynamic::toDynamicDto)
                .collect(Collectors.toList());
        boolean hasLogin = userService.hasLogin();
        if (!hasLogin) {
            return allDynamics.stream()
                    .filter(dynamicDto -> dynamicDto.getVisible().equals(DynamicVisibleStatus.PUBLIC.getValue()))
                    .collect(Collectors.toList());
        }

        Long myUserId = userService.getUserWithAuthorities().getUserId();
        boolean isInWhitelist = userService.isInUsersWhitelist(userId, myUserId);
        boolean isInBlacklist = userService.isInUsersBlacklist(userId, myUserId);
        boolean isFriend = userService.isFriend(userId, myUserId);
        return allDynamics.stream().filter(dynamicDto -> predicateDynamicAuth(dynamicDto, isFriend, isInWhitelist, isInBlacklist, userId.equals(myUserId)))
                .collect(Collectors.toList());
    }


    public Dynamic getDetailDynamic(Long dynamicId) {
        Dynamic dynamic = dynamicRepository.findDynamicByDynamicIdIs(dynamicId);
        throwIfNull(dynamic, ErrorCode.NO_DYNAMIC, "找不到动态");
        if (!requestForPredicateDynamicAuth(dynamic.toDynamicDto())) {
            throw new ServiceException("没有权限", ErrorCode.UNAUTHORIZED);
        }
        return dynamic;
    }

    @Transactional(rollbackFor = Exception.class)
    public void postDynamic(PostDynamicDto postDynamicDto) {

        Dynamic dynamic = new Dynamic();
        dynamic.setContent(postDynamicDto.getContent());
        dynamic.setTitle(postDynamicDto.getTitle());
        User user = userService.getUserWithAuthorities();
        dynamic.setUser(user);
        dynamic.setVisible(postDynamicDto.getVisible());
        dynamic.setLikeCount(0);
        String key = RedisUtils.keySplice(RedisKeys.DYNAMIC_IMAGE, user.getUserId());
        RList<String> uploadPaths = redissonClient.getList(key);
        List<DynamicPicture> dynamicPictures = uploadPaths.readAll().stream()
                .map(path -> {
                    DynamicPicture dynamicPicture = new DynamicPicture(path);
                    dynamicPicture.setDynamic(dynamic);
                    return dynamicPicture;
                })
                .collect(Collectors.toList());
        dynamic.setPictureList(dynamicPictures);
        uploadPaths.clear();
        dynamicRepository.save(dynamic);

    }

    @Transactional(rollbackFor = Exception.class)
    public List<String> uploadDynamicPicture(MultipartFile file) {
        User user = userService.getUserWithAuthorities();
        //获取上传照片数量，超过6张则抛异常
        String key = RedisUtils.keySplice(RedisKeys.DYNAMIC_IMAGE, user.getUserId());
        RList<String> pathsList = redissonClient.getList(key);
        List<String> uploadPaths = pathsList.readAll();
        if (uploadPaths.size() > 6) {
            throw new ServiceException("上传超过6张照片", ErrorCode.SUCCESS);
        }

        String username = user.getUsername();
        String filenameExtension = StringUtils.getFilenameExtension(file.getOriginalFilename());


        if (filenameExtension != null && !filenameExtension.equals("jpg") && !filenameExtension.equals("png"))
            throw new ServiceException("请上传jpg或png格式照片", ErrorCode.SUCCESS);


        String fileName = StringUtils.arrayToDelimitedString(
                new Object[]{System.currentTimeMillis(),
                        username, '.', filenameExtension},
                "");
        String targetPath = sftpService.getDynamicPath() + fileName; // "/upload/dynamic/ + 0000-username.jpg
        String imagePath = sftpService.buildURI(targetPath); // x.x.x.x:xxxx + /upload/dynamic/0000-username.jpg
        try {
            sftpService.uploadFile(targetPath, file.getInputStream());
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new ServiceException(e.getMessage(), ErrorCode.INTERNAL_SERVER_ERROR);
        }
        pathsList.add(imagePath);
        uploadPaths.add(imagePath);
        return uploadPaths;
    }


    @Transactional(rollbackFor = Exception.class)
    public void deleteDynamic(Long dynamicId) {

        Dynamic dynamic = dynamicRepository.findDynamicByDynamicIdIs(dynamicId);
        throwIfNull(dynamic, ErrorCode.NO_DYNAMIC, "找不到该动态");
        User user = userService.getUserWithAuthorities();
        // 删除个人的动态
        if (user.getUserId().equals(dynamic.getUser().getUserId())) {
            deleteDynamicCascade(dynamicId);
            return;
        }
        // 如果是管理员，则可以强制删除任何人的动态
        boolean isAdmin = user.getAuthorities().contains(Authority.ROLE_ADMIN);
        throwIfFalse(isAdmin, ErrorCode.UNAUTHORIZED, "没有权限删除动态");
        deleteDynamicCascade(dynamicId);
    }


    private void deleteDynamicCascade(Long dynamicId) {
        dynamicCommentRepository.deleteAllByDynamic_DynamicId(dynamicId);
        dynamicLikeRepository.deleteAllByDynamicIdIs(dynamicId);
        List<DynamicPicture> dynamicPictures = dynamicPictureRepository.findDynamicPicture(dynamicId);
        dynamicPictures.forEach(dynamicPicture -> {
            String filename = sftpService.getImagePath(dynamicPicture.getPicture());
            // TODO 异步删除
            sftpService.deleteFile(filename);
        });
        dynamicPictureRepository.deleteAllByDynamic_DynamicIdIs(dynamicId);
        dynamicRepository.deleteById(dynamicId);
    }

    public List<DynamicDto> searchDynamic(String searchType, String key) {
        List<Dynamic> dynamics = Collections.emptyList();
        switch (searchType) {
            case "content":
                dynamics = dynamicRepository.findDynamicsByContentContaining(key);
                break;
            case "title":
                dynamics = dynamicRepository.findDynamicsByTitleContaining(key);
                break;
            case "all":
                dynamics = dynamicRepository.findDynamicsByContentContainingOrTitleContaining(key, key);
                break;
            default:
                break;
        }
        return dynamics.stream()
                .filter(dynamic -> dynamic.getVisible().equals(DynamicVisibleStatus.PUBLIC.getValue()))
                .map(Dynamic::toDynamicDto)
                .collect(Collectors.toList());

    }

    public void validateCommentLayer(PostCommentDto postCommentDto) {
        Integer layer = postCommentDto.getLayer();
        if (layer == 0) {
            return;
        }
        if (layer < 0 || layer > 2) {
            throw new ServiceException("layer 无效", ErrorCode.BAD_PARAM);
        }
        Long lastCommentId = postCommentDto.getLastLayerCommentId();

        DynamicComment lastDynamicComment = dynamicCommentRepository.findByDynamicCommentId(lastCommentId);
        throwIfNull(lastDynamicComment, ErrorCode.NO_DYNAMIC, "上层评论不存在");
        throwIfNotEquals(lastDynamicComment.getLayer(), layer - 1, ErrorCode.BAD_PARAM, "上层评论ID与层数不符");
    }

    @Transactional(rollbackFor = Exception.class)
    public void commentDynamic(PostCommentDto postCommentDto) {


        User user = userService.getUserWithAuthorities();
        DynamicComment dynamicComment = new DynamicComment();
        Dynamic dynamic = new Dynamic();
        dynamic.setDynamicId(postCommentDto.getDynamicId());
        dynamicComment.setDynamic(dynamic);
        dynamicComment.setContent(postCommentDto.getContent());
        dynamicComment.setLayer(postCommentDto.getLayer());
        dynamicComment.setUser(user);
        if (postCommentDto.getLayer() != 0) {
            DynamicComment lastDynamicComment = new DynamicComment();
            lastDynamicComment.setDynamicCommentId(postCommentDto.getLastLayerCommentId());
            dynamicComment.setLastDynamicComment(lastDynamicComment);
        }

        dynamicCommentRepository.save(dynamicComment);
    }

    public List<DynamicCommentDto> getDynamicComment(Long dynamicId) {

        List<DynamicComment> dynamicComments = findDynamicComment(dynamicId);
        Map<Long, DynamicCommentDto> dynamicCommentDtoMap = new HashMap<>();
        List<DynamicCommentDto> dynamicCommentDtos = new ArrayList<>();
        dynamicComments.forEach(dynamicComment -> {
            DynamicCommentDto dynamicCommentDto = DynamicCommentDto.parseDynamicComment(dynamicComment);
            dynamicCommentDtoMap.put(dynamicComment.getDynamicCommentId(), dynamicCommentDto);
            if (dynamicComment.getLayer() == 0) {
                dynamicCommentDtos.add(dynamicCommentDto);
            } else {
                Long lastDynamicCommentId = dynamicComment.getLastDynamicComment().getDynamicCommentId();
                DynamicCommentDto lastDynamicCommmentDto = dynamicCommentDtoMap.get(lastDynamicCommentId);

                List<DynamicCommentDto> nextDynamicCommentDtos = Optional.ofNullable(lastDynamicCommmentDto.getNextLayerDynamicComments())
                        .orElseGet(() -> {
                            lastDynamicCommmentDto.setNextLayerDynamicComments(new ArrayList<>());
                            return lastDynamicCommmentDto.getNextLayerDynamicComments();
                        });
                nextDynamicCommentDtos.add(dynamicCommentDto);

            }
        });
        return dynamicCommentDtos;
    }

    @Transactional
    public void likeDynamic(Long dynamicId) {
        Long userId = userService.getUserWithAuthorities().getUserId();
        Integer liked = dynamicLikeRepository.countByDynamicIdIsAndUserIdIs(dynamicId, userId);
        throwIfNotEquals(liked,0,ErrorCode.DYNAMIC_LIKED,"请勿重复点赞");
        DynamicLike dynamicLike = new DynamicLike();
        dynamicLike.setDynamicId(dynamicId);
        dynamicLike.setUserId(userService.getUserWithAuthorities().getUserId());
        dynamicLikeRepository.save(dynamicLike);
        dynamicRepository.updateLikeCount(dynamicId);
    }

    private List<DynamicComment> findDynamicComment(Long dynamicId) {
        return dynamicCommentRepository.findDynamicCommentsByDynamic_DynamicIdIsOrderByCreateTimeAsc(dynamicId);
    }

    private boolean requestForPredicateDynamicAuth(DynamicDto dynamicDto) {
        boolean hasLogin = userService.hasLogin();
        boolean isInWhitelist;
        boolean isFriend;
        boolean isInBlacklist;
        boolean isMe;
        Long userId = dynamicDto.getUser().getUserId();
        if (!hasLogin)
            return dynamicDto.getVisible().equals(DynamicVisibleStatus.PUBLIC.getValue());
        Long myUserId = userService.getUserWithAuthorities().getUserId();
        isMe = userId.equals(myUserId);
        //减少不必要的数据库查询
        isInWhitelist = isMe || userService.isInUsersWhitelist(userId, myUserId);
        isInBlacklist = !isMe && userService.isInUsersBlacklist(userId, myUserId);
        isFriend = isMe || userService.isFriend(userId, myUserId);
        return predicateDynamicAuth(dynamicDto, isFriend, isInWhitelist, isInBlacklist, isMe);
    }

    private boolean predicateDynamicAuth(DynamicDto dynamicDto,
                                         boolean isFriend,
                                         boolean isInWhitelist,
                                         boolean isInBlacklist,
                                         boolean isMe) {
        DynamicVisibleStatus visible = IBaseStatusEnum.fromValue(DynamicVisibleStatus.class, dynamicDto.getVisible());
        boolean result = false;
        switch (visible) {
            case PUBLIC:
                result = true;
                break;
            case PRIVATE:
                result = isMe;
                break;
            case FRIEND:
                result = isMe || (!isInBlacklist && isFriend);
                break;
            case WHITE:
                result = isMe || (!isInBlacklist && isInWhitelist);
                break;
            default:
                break;
        }
        return result;
    }
}
