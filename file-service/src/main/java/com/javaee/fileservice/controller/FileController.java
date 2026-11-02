package com.javaee.fileservice.controller;

import com.javaee.common.model.Result;
import com.javaee.fileservice.config.RabbitMQConfig;
import com.javaee.fileservice.entity.FileMetadata;
import com.javaee.fileservice.service.FileMetadataService;
import com.javaee.fileservice.service.FileService;
import com.javaee.fileservice.util.RabbitMQUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 文件核心接口控制器，提供上传、下载、预览、删除、重命名、移动与复制接口。
 *
 * <p>上传、下载与删除成功后向 RabbitMQ 投递事件，投递失败只记录告警，不影响接口返回。</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/files")
@Tag(name = "文件管理", description = "文件上传、下载、删除、分片等核心接口")
public class FileController {

    @Autowired
    private FileService fileService;

    @Autowired
    private FileMetadataService fileMetadataService;

    @Autowired
    private RabbitMQUtil rabbitMQUtil;

    /**
     * 上传单个文件，保存成功后投递上传事件。
     *
     * @param file 待上传的文件，提供原始文件名与内容
     * @param userId 发起上传的用户 ID，来自 X-User-Id 请求头，可为空
     * @return 统一响应结果，成功时携带文件 ID 与提示消息
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "单文件上传", description = "上传单个文件到服务器")
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file,
                                              @RequestHeader(value = "X-User-Id", required = false) String userId) {
        log.info("=== 收到文件上传请求 ===");
        log.info("文件名: {}, 文件大小: {}, 文件类型: {}", 
            file.getOriginalFilename(), file.getSize(), file.getContentType());
        try {
            String fileId = fileService.upload(file);
            log.info("文件上传成功，文件ID: {}", fileId);
            
            Map<String, Object> message = new HashMap<>();
            message.put("fileId", fileId);
            message.put("fileName", file.getOriginalFilename());
            message.put("fileSize", file.getSize());
            message.put("userId", userId);
            message.put("timestamp", LocalDateTime.now().toString());
            
            try {
                log.info("发送文件上传消息到 RabbitMQ");
                rabbitMQUtil.send(RabbitMQConfig.FILE_EXCHANGE, RabbitMQConfig.FILE_UPLOAD_ROUTING_KEY, message);
            } catch (Exception mqException) {
                log.warn("文件已保存，但上传事件发送 RabbitMQ 失败: fileId={}, error={}",
                        fileId, mqException.getMessage(), mqException);
            }
            
            return Result.success(Map.of("fileId", fileId, "message", "文件上传成功"));
        } catch (Exception e) {
            log.error("文件上传失败: {}", e.getMessage(), e);
            return Result.fail("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 逐个上传多个文件，任一文件失败则整体失败。
     *
     * @param files 待上传的文件数组
     * @return 统一响应结果，成功时携带文件 ID 数组与上传数量
     */
    @PostMapping(value = "/upload-multiple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "多文件上传", description = "上传多个文件到服务器")
    public Result<Map<String, Object>> uploadMultiple(@RequestParam("files") MultipartFile[] files) {
        System.out.println("=== 收到多文件上传请求 ===");
        System.out.println("文件数量: " + files.length);
        try {
            System.out.println("调用fileService.uploadMultiple方法");
            String[] fileIds = fileService.uploadMultiple(files);
            System.out.println("多文件上传成功，文件ID数量: " + fileIds.length);
            return Result.success(Map.of("fileIds", fileIds, "message", "文件上传成功", "count", fileIds.length));
        } catch (Exception e) {
            System.out.println("多文件上传失败: " + e.getMessage());
            e.printStackTrace();
            return Result.fail("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 保存大文件的一个分片，供后续合并。
     *
     * @param chunk 当前分片文件
     * @param fileId 分片所属文件的唯一标识，由客户端生成并在合并时复用
     * @param chunkIndex 分片序号，从 0 开始
     * @param totalChunks 分片总数
     * @return 成功时返回 200 与分片序号，失败时返回 500 与错误信息
     */
    @PostMapping(value = "/upload-chunk", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "分片上传", description = "大文件分片上传")
    public ResponseEntity<Map<String, String>> uploadChunk(@RequestParam("chunk") MultipartFile chunk,
                           @RequestParam("fileId") String fileId,
                           @RequestParam("chunkIndex") int chunkIndex,
                           @RequestParam("totalChunks") int totalChunks) {
        try {
            fileService.uploadChunk(chunk, fileId, chunkIndex, totalChunks);
            return ResponseEntity.ok(Map.of("message", "分片上传成功", "chunkIndex", String.valueOf(chunkIndex)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "分片上传失败: " + e.getMessage()));
        }
    }

    /**
     * 合并已上传的分片，生成完整文件。
     *
     * @param fileId 分片所属文件的唯一标识
     * @param fileName 合并后的原始文件名，用于推断扩展名与内容类型
     * @return 成功时返回 200 与合并后的文件 ID，失败时返回 500 与错误信息
     */
    @PostMapping("/merge-chunk")
    @Operation(summary = "分片合并", description = "合并文件分片")
    public ResponseEntity<Map<String, String>> mergeChunk(@Parameter(description = "文件唯一标识") @RequestParam("fileId") String fileId,
                          @Parameter(description = "文件名") @RequestParam("fileName") String fileName) {
        try {
            String mergedFileId = fileService.mergeChunk(fileId, fileName);
            return ResponseEntity.ok(Map.of("fileId", mergedFileId, "message", "文件合并成功"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "文件合并失败: " + e.getMessage()));
        }
    }

    /**
     * 以附件形式下载文件，并投递下载事件。
     *
     * @param fileId 文件 ID
     * @param userId 发起下载的用户 ID，来自 X-User-Id 请求头，可为空
     * @return 文件字节流；失败时返回 500 与错误文本
     */
    @GetMapping("/download/{fileId}")
    @Operation(summary = "文件下载", description = "下载指定文件")
    public ResponseEntity<byte[]> download(@Parameter(description = "文件ID") @PathVariable String fileId,
                                           @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            byte[] fileBytes = fileService.download(fileId);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "file_" + fileId);
            
            Map<String, Object> message = new HashMap<>();
            message.put("fileId", fileId);
            message.put("userId", userId);
            message.put("timestamp", LocalDateTime.now().toString());
            
            try {
                log.info("发送文件下载消息到 RabbitMQ");
                rabbitMQUtil.send(RabbitMQConfig.FILE_EXCHANGE, RabbitMQConfig.FILE_DOWNLOAD_ROUTING_KEY, message);
            } catch (Exception mqException) {
                log.warn("文件下载成功，但下载事件发送 RabbitMQ 失败: fileId={}, error={}",
                        fileId, mqException.getMessage(), mqException);
            }
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(fileBytes);
        } catch (Exception e) {
            log.error("文件下载失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("文件下载失败: " + e.getMessage()).getBytes());
        }
    }

    /**
     * 以内联方式返回文件字节流，供浏览器预览。
     *
     * @param fileId 文件 ID
     * @return 文件字节流；失败时返回 500 与错误文本
     */
    @GetMapping("/preview/{fileId}")
    @Operation(summary = "文件预览", description = "预览指定文件")
    public ResponseEntity<byte[]> preview(@Parameter(description = "文件ID") @PathVariable String fileId) {
        try {
            byte[] fileBytes = fileService.download(fileId);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(fileBytes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("文件预览失败: " + e.getMessage()).getBytes());
        }
    }

    /**
     * 获取文件原始名称，供文档服务解析时使用。
     *
     * @param fileId 文件 ID
     * @return 原始文件名；元数据缺失或查询异常时降级返回文件 ID
     */
    @GetMapping("/info/{fileId}/name")
    @Operation(summary = "获取文件名", description = "根据文件ID获取原始文件名")
    public ResponseEntity<String> getFileName(@Parameter(description = "文件ID") @PathVariable String fileId) {
        try {
            String fileName = Optional.ofNullable(fileMetadataService.getMetadata(fileId))
                    .map(metadata -> metadata.getOriginalFileName() != null
                            ? metadata.getOriginalFileName()
                            : metadata.getFileName())
                    .orElse(fileId);
            return ResponseEntity.ok(fileName);
        } catch (Exception e) {
            log.warn("获取文件名失败，返回 fileId 作为降级值: fileId={}, error={}", fileId, e.getMessage(), e);
            return ResponseEntity.ok(fileId);
        }
    }

    /**
     * 获取文件类型，供文档服务解析时使用。
     *
     * @param fileId 文件 ID
     * @return 文件的内容类型；元数据缺失或查询异常时返回 application/octet-stream
     */
    @GetMapping("/info/{fileId}/type")
    @Operation(summary = "获取文件类型", description = "根据文件ID获取文件Content-Type")
    public ResponseEntity<String> getFileType(@Parameter(description = "文件ID") @PathVariable String fileId) {
        try {
            String fileType = Optional.ofNullable(fileMetadataService.getMetadata(fileId))
                    .map(FileMetadata::getFileType)
                    .orElse(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            return ResponseEntity.ok(fileType);
        } catch (Exception e) {
            log.warn("获取文件类型失败，返回默认类型: fileId={}, error={}", fileId, e.getMessage(), e);
            return ResponseEntity.ok(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        }
    }

    /**
     * 删除文件，并投递删除事件。
     *
     * @param fileId 文件 ID
     * @param userId 发起删除的用户 ID，来自 X-User-Id 请求头，可为空
     * @return 成功时返回 200 与提示消息，失败时返回 500 与错误信息
     */
    @DeleteMapping("/{fileId}")
    @Operation(summary = "文件删除", description = "删除指定文件")
    public ResponseEntity<Map<String, String>> delete(@Parameter(description = "文件ID") @PathVariable String fileId,
                                                      @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            fileService.delete(fileId);
            
            Map<String, Object> message = new HashMap<>();
            message.put("fileId", fileId);
            message.put("userId", userId);
            message.put("timestamp", LocalDateTime.now().toString());
            
            try {
                log.info("发送文件删除消息到 RabbitMQ");
                rabbitMQUtil.send(RabbitMQConfig.FILE_EXCHANGE, RabbitMQConfig.FILE_DELETE_ROUTING_KEY, message);
            } catch (Exception mqException) {
                log.warn("文件删除成功，但删除事件发送 RabbitMQ 失败: fileId={}, error={}",
                        fileId, mqException.getMessage(), mqException);
            }
            
            return ResponseEntity.ok(Map.of("message", "文件删除成功"));
        } catch (Exception e) {
            log.error("文件删除失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "文件删除失败: " + e.getMessage()));
        }
    }

    /**
     * 重命名指定文件。
     *
     * @param fileId 文件 ID
     * @param newName 新的文件名，含扩展名
     * @return 成功时返回 200 与提示消息，失败时返回 500 与错误信息
     */
    @PutMapping("/{fileId}/rename")
    @Operation(summary = "文件重命名", description = "重命名指定文件")
    public ResponseEntity<Map<String, String>> rename(@Parameter(description = "文件ID") @PathVariable String fileId,
                      @Parameter(description = "新文件名") @RequestParam("newName") String newName) {
        try {
            fileService.rename(fileId, newName);
            return ResponseEntity.ok(Map.of("message", "文件重命名成功"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "文件重命名失败: " + e.getMessage()));
        }
    }

    /**
     * 将文件移动到指定目录。
     *
     * @param fileId 文件 ID
     * @param targetPath 目标目录
     * @return 成功时返回 200 与提示消息，失败时返回 500 与错误信息
     */
    @PutMapping("/{fileId}/move")
    @Operation(summary = "文件移动", description = "移动文件到指定目录")
    public ResponseEntity<Map<String, String>> move(@Parameter(description = "文件ID") @PathVariable String fileId,
                    @Parameter(description = "目标目录") @RequestParam("targetPath") String targetPath) {
        try {
            fileService.move(fileId, targetPath);
            return ResponseEntity.ok(Map.of("message", "文件移动成功"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "文件移动失败: " + e.getMessage()));
        }
    }

    /**
     * 复制文件到指定目录，并生成新的文件 ID。
     *
     * @param fileId 源文件 ID
     * @param targetPath 目标目录
     * @return 成功时返回 200 与新文件 ID，失败时返回 500 与错误信息
     */
    @PutMapping("/{fileId}/copy")
    @Operation(summary = "文件复制", description = "复制文件到指定目录")
    public ResponseEntity<Map<String, String>> copy(@Parameter(description = "文件ID") @PathVariable String fileId,
                    @Parameter(description = "目标目录") @RequestParam("targetPath") String targetPath) {
        try {
            String newFileId = fileService.copy(fileId, targetPath);
            return ResponseEntity.ok(Map.of("fileId", newFileId, "message", "文件复制成功"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "文件复制失败: " + e.getMessage()));
        }
    }

}
