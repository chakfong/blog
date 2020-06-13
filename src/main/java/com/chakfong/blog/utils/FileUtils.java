package com.chakfong.blog.utils;

import com.chakfong.blog.entity.User;
import com.chakfong.blog.exception.ErrorCode;
import com.chakfong.blog.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


@Slf4j
public class FileUtils {

    private static final String WINDOWS_PROFILES_PATH = "C:/data/blog/profiles/";

    private static final String LINUX_PROFILES_PATH = "/data/blog/profiles/";

    @Deprecated
    public static String uploadFileToLocalServer(String path, MultipartFile file) {
        // 根据Windows和Linux配置不同的头像保存路径
        String OSName = System.getProperty("os.name");
        String avatarPath = OSName.toLowerCase().startsWith("win") ? WINDOWS_PROFILES_PATH
                : LINUX_PROFILES_PATH;
        String newAvatarPath = avatarPath + System.currentTimeMillis() + file.getOriginalFilename();

        // 磁盘保存
        BufferedOutputStream out = null;
        try {
            File folder = new File(avatarPath);
            if (!folder.exists())
                folder.mkdirs();
            out = new BufferedOutputStream(new FileOutputStream(newAvatarPath));
            // 写入新文件
            out.write(file.getBytes());
            out.flush();
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new ServiceException("文件保存失败", ErrorCode.INTERNAL_SERVER_ERROR);
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }
        return newAvatarPath;
    }

}
