package com.chakfong.blog.service;

import com.chakfong.blog.configuration.properties.ImageServerProperties;
import com.chakfong.blog.configuration.properties.SftpProperties;
import com.chakfong.blog.exception.ErrorCode;
import com.chakfong.blog.exception.ServiceException;
import com.chakfong.blog.utils.StringUtils;
import com.jcraft.jsch.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Arrays;

@Service
@Slf4j
public class SftpService {

    private final SftpProperties config;

    private final ImageServerProperties imageServerProperties;


    private static final String SESSION_CONFIG_STRICT_HOST_KEY_CHECKING = "StrictHostKeyChecking";

    public SftpService(SftpProperties config, ImageServerProperties imageServerProperties) {
        this.config = config;
        this.imageServerProperties = imageServerProperties;
    }

    public boolean deleteFile(String targetPath) {
        ChannelSftp sftp = null;
        try {
            sftp = this.createSftp();
//            sftp.cd(config.getRoot());
            sftp.rm('.' + targetPath);
            return true;
        } catch (Exception e) {
            log.error("Delete file failure. TargetPath: {}", targetPath, e);
            throw new ServiceException("Delete File failure", ErrorCode.INTERNAL_SERVER_ERROR);
        } finally {
            this.disconnect(sftp);
        }
    }

    public File downloadFile(String targetPath) throws JSchException, IOException {
        ChannelSftp sftp = this.createSftp();
        OutputStream outputStream = null;
        try {
            sftp.cd(config.getRoot());
            log.info("Change path to {}", config.getRoot());

            File file = new File(targetPath.substring(targetPath.lastIndexOf('/') + 1));

            outputStream = new FileOutputStream(file);
            sftp.get(targetPath, outputStream);
            log.info("Download file success. TargetPath: {}", targetPath);
            return file;
        } catch (Exception e) {
            log.error("Download file failure. TargetPath: {}", targetPath, e);
            throw new ServiceException("Download File failure", ErrorCode.INTERNAL_SERVER_ERROR);
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
            this.disconnect(sftp);
        }
    }

    private ChannelSftp createSftp() throws JSchException {
        JSch jsch = new JSch();
        log.info("Try to connect sftp[" + config.getUsername() + "@" + config.getHost() + "], use password[" + config.getPassword() + "]");

        Session session = createSession(jsch, config.getHost(), config.getUsername(), config.getPort());
        session.setPassword(config.getPassword());
        session.connect(config.getSessionConnectTimeout());

        log.info("Session connected to {}.", config.getHost());

        Channel channel = session.openChannel(config.getProtocol());
        channel.connect(config.getChannelConnectedTimeout());

        log.info("Channel created to {}.", config.getHost());

        return (ChannelSftp) channel;
    }

    private Session createSession(JSch jsch, String host, String username, Integer port) {
        Session session = null;

        try {
            if (port <= 0) {
                session = jsch.getSession(username, host);
            } else {
                session = jsch.getSession(username, host, port);
            }
        } catch (JSchException e) {
            log.error(e.getMessage());
        }

        if (session == null) {
            throw new ServiceException(host + " session is null", ErrorCode.INTERNAL_SERVER_ERROR);
        }

        session.setConfig(SESSION_CONFIG_STRICT_HOST_KEY_CHECKING, config.getSessionStrictHostKeyChecking());
        return session;
    }

    private void disconnect(ChannelSftp sftp) {
        try {
            if (sftp != null) {
                if (sftp.isConnected()) {
                    sftp.disconnect();
                } else if (sftp.isClosed()) {
                    log.info("sftp is closed already");
                }
                if (null != sftp.getSession()) {
                    sftp.getSession().disconnect();
                }
            }
        } catch (JSchException e) {
            log.warn(e.getMessage());
        }
    }

    public boolean uploadFile(String targetPath, InputStream inputStream) throws JSchException {
        // targetPath = "upload/xxx/xxx.jpg"
        ChannelSftp sftp = this.createSftp();
        try {
//            sftp.cd(config.getRoot());
//            log.info("Change path to {}", config.getRoot());

            int index = targetPath.lastIndexOf('/');
            String fileDir = targetPath.substring(0, index);
            String fileName = targetPath.substring(index + 1);
            boolean dirs = this.createDirs(fileDir, sftp);
            if (!dirs) {
                log.error("Remote path error. path:{}", targetPath);
                throw new ServiceException("Upload File failure", ErrorCode.INTERNAL_SERVER_ERROR);
            }
            sftp.put(inputStream, fileName);
            return true;
        } catch (Exception e) {
            log.error("Upload file failure. TargetPath: {}", targetPath, e);
            throw new ServiceException("Upload File failure", ErrorCode.INTERNAL_SERVER_ERROR);
        } finally {
            this.disconnect(sftp);
        }
    }

    private boolean createDirs(String dirPath, ChannelSftp sftp) {
        if (dirPath != null && !dirPath.isEmpty()
                && sftp != null) {
            String[] dirs = Arrays.stream(dirPath.split("/"))
                    .filter(StringUtils::isNotNullOrBlank)
                    .toArray(String[]::new);

            for (String dir : dirs) {
                try {
                    sftp.cd(dir);
                    log.info("Change directory {}", dir);
                } catch (Exception e) {
                    try {
                        sftp.mkdir(dir);
                        log.info("Create directory {}", dir);
                    } catch (SftpException e1) {
                        log.error("Create directory failure, directory:{}", dir, e1);
                    }
                    try {
                        sftp.cd(dir);
                        log.info("Change directory {}", dir);
                    } catch (SftpException e1) {
                        log.error("Change directory failure, directory:{}", dir, e1);
                    }
                }
            }
            return true;
        }
        return false;
    }

    private String getSocket() {
        return imageServerProperties.getHost() +
                ':' +
                imageServerProperties.getPort();
    }


    public String buildURI(String filePath) {
        return getSocket() + filePath;
    }

    public String getImagePath(String URI) {
        return URI.substring(getSocket().length());
    }

    public String getAvatarPath() {
        return imageServerProperties.getAvatarPath();
    }

    public String getDynamicPath() {
        return imageServerProperties.getDynamicPath();
    }
}
