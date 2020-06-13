package com.chakfong.blog.service;

import com.chakfong.blog.configuration.properties.ImageServerProperties;
import com.chakfong.blog.dao.UserFriendApplicationRepository;
import com.chakfong.blog.dao.UserFriendRepository;
import com.chakfong.blog.dao.UserRepository;
import com.chakfong.blog.dto.request.RegisterDto;
import com.chakfong.blog.dto.response.IsFriendDto;
import com.chakfong.blog.entity.*;
import com.chakfong.blog.entity.status_enum.UserFriendApplicationStatus;
import com.chakfong.blog.entity.status_enum.UserStatus;
import com.chakfong.blog.exception.ErrorCode;
import com.chakfong.blog.exception.ServiceException;
import com.chakfong.blog.security.SecurityUtils;
import com.chakfong.blog.utils.RedisKeys;
import com.chakfong.blog.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.internal.constraintvalidators.hv.EmailValidator;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.chakfong.blog.utils.RedisKeys.REGISTER_CAPTCHA_KEY;
import static com.chakfong.blog.utils.ServiceExceptionUtils.throwIfNotEquals;
import static com.chakfong.blog.utils.ServiceExceptionUtils.throwIfNull;

@Service
@Transactional
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    private final UserFriendRepository userFriendRepository;

    private final UserFriendApplicationRepository userFriendApplicationRepository;

    private final RedissonClient redissonClient;

    private final CaptchaService captchaService;

    private final PasswordEncoder passwordEncoder;

    private final EmailService emailService;

    private final SftpService sftpService;

    public UserService(UserRepository userRepository, UserFriendRepository userFriendRepository, UserFriendApplicationRepository userFriendApplicationRepository, RedissonClient redissonClient, CaptchaService captchaService, PasswordEncoder passwordEncoder, EmailService emailService, SftpService sftpService, ImageServerProperties imageServerProperties) {
        this.userRepository = userRepository;
        this.userFriendRepository = userFriendRepository;
        this.userFriendApplicationRepository = userFriendApplicationRepository;
        this.redissonClient = redissonClient;
        this.captchaService = captchaService;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.sftpService = sftpService;
    }

    public User getUserById(Long userId) {
        //TODO 存DTO
        String key = RedisUtils.keySplice(RedisKeys.USER, userId);
        RBucket<User> userRBucket = redissonClient.getBucket(key);
        User updateDetail = userRBucket.get();
        if (updateDetail == null) {
            updateDetail = userRepository.findUserByUserId(userId);
            throwIfNull(updateDetail,ErrorCode.NO_USER, "找不到该用户");
            userRBucket.set(updateDetail);
        }
        return updateDetail;

    }

    public boolean hasLogin() {
        Optional<String> username = SecurityUtils.getCurrentUsername();
        return username.map(s -> !s.equals("anonymousUser")).orElse(false);
    }

    @Transactional(readOnly = true)
    public User getUserWithAuthorities() {
        return SecurityUtils.getCurrentUsername()
                .flatMap(userRepository::findOneWithAuthoritiesByUsername)
                .orElseThrow(() -> new ServiceException("找不到该用户", ErrorCode.UNAUTHORIZED));
    }

    @Transactional()
    public User register(RegisterDto registerDto) {
        RBucket<String> codeBucket = redissonClient.getBucket(RedisUtils.keySplice(REGISTER_CAPTCHA_KEY, registerDto.getEmail()));
        String realcode = codeBucket.get();

        log.info("获取redis key为 {}，值为{} ,用户输入值为 {}",
                RedisUtils.keySplice(REGISTER_CAPTCHA_KEY, registerDto.getEmail()), realcode, registerDto.getCode());
        throwIfNull(realcode,ErrorCode.CAPTCHA_EXPIRED, "验证码失效");
        throwIfNotEquals(realcode, registerDto.getCode(),ErrorCode.CAPTCHA_UNCORRECTED, "验证码错误");

        if (userRepository.findUserByUsername(registerDto.getUsername()) != null)
            throw new ServiceException("该账号已被使用", ErrorCode.BAD_PARAM);

        User user = new User();
        user.setUsername(registerDto.getUsername());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        user.setEmail(registerDto.getEmail());
        user.setStatus(UserStatus.ACTIVE.getValue());

        Set<Authority> authoritySet = new HashSet<>();
        authoritySet.add(Authority.ROLE_USER);
        user.setAuthorities(authoritySet);

        userRepository.saveAndFlush(user);
        return user;
    }

    public void sudoRegister(Integer createCount) {
        SplittableRandom random = new SplittableRandom();
        List<User> users = new ArrayList<>();
        User user = new User();
        Set<Authority> authoritySet = new HashSet<>();
        authoritySet.add(Authority.ROLE_USER);
        user.setAuthorities(authoritySet);
        user.setStatus(UserStatus.ACTIVE.getValue());
        user.setAvatar("http://47.107.149.16:8700/upload/159184996777818918197067739809.jpg");
        user.setPassword("$10$CYym84ZDaDOjP3vJ3jKZ6eW.wynPmo/xXZ9H1a5IdrlZeYRrTQXkm");
        for (int i = 0; i < createCount; i++) {
            user.setEmail((10000 + random.nextInt(50000)) + "@qq.com");
            user.setUsername((1000000 + random.nextInt(1000000) + ""));
            users.add(user.clone());
            if (users.size() > 100) {
                userRepository.saveAll(users);
                users.clear();
            }
        }
    }

    @Transactional
    public void sendEmailCaptcha(String email) {
        if (!new EmailValidator().isValid(email, null))
            throw new ServiceException("请求邮箱号不合法", ErrorCode.BAD_PARAM);
        if (userRepository.findUserByEmail(email) != null)
            throw new ServiceException("邮箱已被占用", ErrorCode.BAD_PARAM);

        String captchaCode = captchaService.createCode();
        RBucket<String> codeBucket = redissonClient.getBucket(RedisUtils.keySplice(REGISTER_CAPTCHA_KEY, email));
        log.info("获取redis key {} ", RedisUtils.keySplice(REGISTER_CAPTCHA_KEY, email));
        codeBucket.set(captchaCode, 5, TimeUnit.MINUTES);

        emailService.sendCaptcha(email, captchaCode);


    }

    private void uploadFileToRemoteServer(String fileName, MultipartFile file) {
        try {
            sftpService.uploadFile(fileName, file.getInputStream());
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new ServiceException(e.getMessage(), ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public String updateUserAvatar(MultipartFile avatarFile) {
        String fileType = avatarFile.getOriginalFilename();
        if (fileType != null) {
            fileType = fileType.substring(fileType.lastIndexOf('.'));
        }
        String fileName = sftpService.getAvatarPath() + System.currentTimeMillis() + fileType;

        uploadFileToRemoteServer(fileName, avatarFile);
        String newAvatarPath = sftpService.buildURI(fileName);
        // 当前用户
        User currentUser = getUserWithAuthorities();
        currentUser.setAvatar(newAvatarPath);
        RedisUtils.setCache(redissonClient, currentUser, currentUser.getUserId());
        userRepository.save(currentUser);
        return newAvatarPath;
    }

    public List<User> searchUserByType(String searchType, String key) {
        List<User> users;
        if (searchType.equalsIgnoreCase("username")) {
            users = userRepository.findUsersByUsernameContaining(key);
        } else {
            users = userRepository.findUsersByEmailContaining(key);
        }
        return users;
    }

    public boolean addUserFriend(Long userId) {
        Long myUserId = getUserWithAuthorities().getUserId();
        //把之前的好友申请记录删掉
        userFriendApplicationRepository.deleteUserFriendApplicationByFromUserIdIsAndToUserIdIs(myUserId, userId);

        //插入新的好友申请记录
        UserFriendApplication userFriendApplication = new UserFriendApplication();
        userFriendApplication.setFromUserId(getUserWithAuthorities().getUserId());
        userFriendApplication.setStatus(UserFriendApplicationStatus.PROCESSING.getValue());
        userFriendApplication.setToUserId(userId);
        userFriendApplicationRepository.saveAndFlush(userFriendApplication);
        return true;
    }

    public List<User> getAllFriend() {
        Long userId = getUserWithAuthorities().getUserId();
        //全连接找出所有的好友关系，
        List<Long> userIds = userFriendRepository.findAllFriendByUserId(userId)
                .stream()
                .map(userFriend -> {
                    log.info(userFriend.toString());
                    if (userFriend.getToUserId().equals(userId)) {
                        return userFriend.getFromUserId();
                    } else {
                        return userFriend.getToUserId();
                    }
                })
                .collect(Collectors.toList());
        return userRepository.findUsersByUserIdIn(userIds);
    }

    public boolean deleteFriend(Long userId) {
        Long myUserId = getUserWithAuthorities().getUserId();
        Long fromUserId = Math.min(userId, myUserId);
        Long toUserId = Math.max(userId, myUserId);
        userFriendApplicationRepository.deleteUserFriendApplicationByFromUserIdIsAndToUserIdIs(fromUserId, toUserId);
        userFriendApplicationRepository.deleteUserFriendApplicationByFromUserIdIsAndToUserIdIs(toUserId, fromUserId);
        return userFriendRepository.deleteUserFriendByFromUserIdIsAndToUserIdIs(fromUserId, toUserId) > 0;
    }

    public boolean addWhitelist(Long userId) {
        User user = getUserWithAuthorities();
        List<User> whiteList = user.getWhitelist();
        whiteList.add(getUserById(userId));
        userRepository.save(user);
        return true;
    }

    public boolean addBlacklist(Long userId) {
        User user = getUserWithAuthorities();
        List<User> blackList = user.getBlacklist();
        blackList.add(getUserById(userId));
        userRepository.save(user);
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean permitFriendApplication(Long userId) {

        Long myUserId = getUserWithAuthorities().getUserId();
        UserFriendApplication userFriendApplication = userFriendApplicationRepository.findUserFriendApplicationByFromUserIdIsAndToUserIdIs(userId, myUserId);

        userFriendApplication.setStatus(UserFriendApplicationStatus.PERMITTED.getValue());
        userFriendApplicationRepository.saveAndFlush(userFriendApplication);

        UserFriend userFriend = new UserFriend();
        userFriend.setFromUserId(Math.min(userId, myUserId));
        userFriend.setToUserId(Math.max(userId, myUserId));
        userFriendRepository.save(userFriend);

        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean rejectFriendApplication(Long userId) {
        Long myUserId = getUserWithAuthorities().getUserId();
        UserFriendApplication userFriendApplication = userFriendApplicationRepository.findUserFriendApplicationByFromUserIdIsAndToUserIdIs(userId, myUserId);

        userFriendApplication.setStatus(UserFriendApplicationStatus.REJECTED.getValue());
        userFriendApplicationRepository.saveAndFlush(userFriendApplication);
        return true;
    }

    public List<User> getAllFriendApplication() {
        Long myUserId = getUserWithAuthorities().getUserId();
        return userFriendApplicationRepository.findUserFriendApplicationsByToUserIdIs(myUserId)
                .stream()
                .map(userFriendApplication -> getUserById(userFriendApplication.getFromUserId()))
                .collect(Collectors.toList());
    }

    public List<IsFriendDto> isFriend(String userIds) {
        Long myUserId = getUserWithAuthorities().getUserId();
        return Arrays.stream(userIds.split(","))
                .map(userIdString -> {
                    long userId = Long.parseLong(userIdString);
                    boolean isFriend = userFriendRepository.countDistinctByFromUserIdIsAndToUserIdIs(
                            Math.min(userId, myUserId),
                            Math.max(userId, myUserId)) > 0;
                    return new IsFriendDto(userId, isFriend);
                })
                .collect(Collectors.toList());
    }

    public boolean isFriend(Long fromUserId, Long toUserId) {
        return userFriendRepository.countDistinctByFromUserIdIsAndToUserIdIs(
                Math.min(fromUserId, toUserId),
                Math.max(fromUserId, toUserId)) > 0;
    }

    public List<Long> getUsersInWhitelist(Long userId) {
        return userRepository.findFromUserIdByToUserIdInWhitelist(userId);
    }

    public boolean isInUsersWhitelist(Long fromUserId, Long toUserId) {
        return userRepository.findWhitelistRecord(fromUserId, toUserId) > 0;
    }

    public boolean isInUsersBlacklist(Long fromUserId, Long toUserId) {
        return userRepository.findBlacklistRecord(fromUserId, toUserId) > 0;
    }


}
