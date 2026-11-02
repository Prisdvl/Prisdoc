package com.javaee.aiservice.service;

import com.javaee.aiservice.dto.FileDownloadDTO;
import com.javaee.aiservice.security.BucketPermissionService;
import com.javaee.aiservice.security.RequestUserContext;
import com.javaee.aiservice.vo.FileDownloadVO;
import com.javaee.common.utils.UserBucketUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;

/**
 * 文件下载服务，提供文件流下载与预签名访问 URL 获取。
 *
 * <p>操作前校验当前用户对目标存储桶的访问权限；未指定存储桶时回落到用户默认桶。</p>
 */
@Service
public class FileDownloadService {

    private static final Logger log = LoggerFactory.getLogger(FileDownloadService.class);

    @Autowired
    private MinIOService minIOService;

    @Autowired
    private BucketPermissionService bucketPermissionService;

    @Autowired
    private RequestUserContext requestUserContext;

    @Value("${minio.url.expiry:3600}")
    private int defaultExpiry;

    /**
     * 从 MinIO 下载指定对象并返回文件输入流。
     *
     * <p>调用前校验存储桶权限；对象名为空时抛出 {@link IllegalArgumentException}。</p>
     *
     * @param dto 下载参数，包含存储桶名与对象名
     * @return 文件输入流，由调用方负责关闭
     */
    public InputStream downloadFile(FileDownloadDTO dto) {
        log.info("开始下载文件: bucket={}, object={}", dto.getBucketName(), dto.getObjectName());

        try {
            // 确定存储桶名称
            String bucketName = resolveBucketName(dto.getBucketName());
            bucketPermissionService.assertCanAccess(bucketName);
            
            // 确定对象名称
            String objectName = dto.getObjectName();
            if (objectName == null || objectName.trim().isEmpty()) {
                throw new IllegalArgumentException("对象名称不能为空");
            }

            // 调用 MinIOService 下载文件
            log.info("调用MinIO下载文件: bucket={}, object={}", bucketName, objectName);
            InputStream inputStream = minIOService.downloadFile(bucketName, objectName);

            log.info("文件下载成功: bucket={}, object={}", bucketName, objectName);
            return inputStream;

        } catch (Exception e) {
            log.error("文件下载失败", e);
            throw new RuntimeException("文件下载失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取 MinIO 对象的预签名访问 URL。
     *
     * <p>有效期取配置项 {@code minio.url.expiry}，调用前同样校验存储桶权限。</p>
     *
     * @param dto 下载参数，包含存储桶名与对象名
     * @return 文件下载结果，含预签名 URL、存储桶、对象名与有效期
     */
    public FileDownloadVO getFileUrl(FileDownloadDTO dto) {
        log.info("开始获取文件URL: bucket={}, object={}", dto.getBucketName(), dto.getObjectName());

        try {
            // 确定存储桶名称
            String bucketName = resolveBucketName(dto.getBucketName());
            bucketPermissionService.assertCanAccess(bucketName);
            
            // 确定对象名称
            String objectName = dto.getObjectName();
            if (objectName == null || objectName.trim().isEmpty()) {
                throw new IllegalArgumentException("对象名称不能为空");
            }

            // 确定过期时间
            int expirySeconds = defaultExpiry;

            // 调用 MinIOService 获取文件 URL
            log.info("调用MinIO获取文件URL: bucket={}, object={}, expiry={}s", 
                bucketName, objectName, expirySeconds);
            String fileUrl = minIOService.getFileUrl(bucketName, objectName, expirySeconds);

            // 构建返回结果
            FileDownloadVO vo = new FileDownloadVO();
            vo.setFileUrl(fileUrl);
            vo.setBucketName(bucketName);
            vo.setObjectName(objectName);
            vo.setExpirySeconds(expirySeconds);

            log.info("获取文件URL成功: url={}", fileUrl);
            return vo;

        } catch (Exception e) {
            log.error("获取文件URL失败", e);
            throw new RuntimeException("获取文件URL失败: " + e.getMessage(), e);
        }
    }

    private String resolveBucketName(String requestedBucketName) {
        if (requestedBucketName != null && !requestedBucketName.isBlank()) {
            return requestedBucketName;
        }
        return UserBucketUtils.bucketNameForUser(requestUserContext.getRequiredUserId());
    }
}
