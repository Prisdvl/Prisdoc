package com.javaee.documentservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 文件服务客户端，通过 OpenFeign 调用 file-service 的文件接口。
 *
 * <p>地址默认取 {@code file.service.url}，未配置时回落到服务名 {@code file}。</p>
 */
@FeignClient(name = "file", url = "${file.service.url:http://file:8082}")
public interface FileServiceClient {

    /**
     * 按文件 ID 下载文件内容。
     *
     * @param fileId 文件 ID
     * @return 文件内容字节数组的响应
     */
    @GetMapping("/api/files/download/{fileId}")
    ResponseEntity<byte[]> downloadFile(@PathVariable("fileId") String fileId);

    /**
     * 按文件 ID 获取文件名。
     *
     * @param fileId 文件 ID
     * @return 文件名的响应
     */
    @GetMapping("/api/files/info/{fileId}/name")
    ResponseEntity<String> getFileName(@PathVariable("fileId") String fileId);

    /**
     * 按文件 ID 获取文件类型。
     *
     * @param fileId 文件 ID
     * @return 文件 content-type 的响应
     */
    @GetMapping("/api/files/info/{fileId}/type")
    ResponseEntity<String> getFileType(@PathVariable("fileId") String fileId);
}