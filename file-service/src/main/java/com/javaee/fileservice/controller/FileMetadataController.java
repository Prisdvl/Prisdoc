package com.javaee.fileservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.javaee.fileservice.service.FileMetadataService;
import com.javaee.fileservice.entity.FileMetadata;
import java.util.List;
import java.util.Map;

/**
 * 文件元数据查询接口控制器，提供元数据详情、列表、搜索与目录结构查询。
 */
@RestController
@RequestMapping("/api/files")
@Tag(name = "文件元数据", description = "文件元数据查询接口")
public class FileMetadataController {

    @Autowired
    private FileMetadataService fileMetadataService;

    /**
     * 根据文件 ID 查询文件元数据。
     *
     * @param fileId 文件 ID
     * @return 文件元数据；不存在时返回 404，查询异常时返回 500 与错误文本
     */
    @GetMapping("/metadata/{fileId}")
    @Operation(summary = "获取文件元数据", description = "根据文件ID获取文件详细信息")
    public ResponseEntity<?> getMetadata(@Parameter(description = "文件ID") @PathVariable String fileId) {
        try {
            FileMetadata metadata = fileMetadataService.getMetadata(fileId);
            if (metadata == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(metadata);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("获取元数据失败: " + e.getMessage());
        }
    }

    /**
     * 分页查询文件元数据，支持按字段排序。
     *
     * @param page 页码，从 1 开始
     * @param size 每页条数
     * @param sortBy 排序字段，驼峰命名会被转换为数据库列名
     * @param direction 排序方向，asc 为升序，其余取值按降序
     * @return 当前页的文件元数据列表；查询异常时返回 500 与错误文本
     */
    @GetMapping("/list")
    @Operation(summary = "获取文件列表", description = "获取文件列表，支持分页和排序")
    public ResponseEntity<?> getFileList(@Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
                           @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
                           @Parameter(description = "排序字段") @RequestParam(defaultValue = "createTime") String sortBy,
                           @Parameter(description = "排序方向") @RequestParam(defaultValue = "desc") String direction) {
        try {
            List<FileMetadata> fileList = fileMetadataService.getFileList(page, size, sortBy, direction);
            return ResponseEntity.ok(fileList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("获取文件列表失败: " + e.getMessage());
        }
    }

    /**
     * 按关键词分页搜索文件，匹配文件名与原始文件名。
     *
     * @param keyword 搜索关键词
     * @param page 页码，从 1 开始
     * @param size 每页条数
     * @return 匹配的文件元数据列表；查询异常时返回 500 与错误文本
     */
    @GetMapping("/search")
    @Operation(summary = "搜索文件", description = "根据关键词搜索文件")
    public ResponseEntity<?> searchFiles(@Parameter(description = "搜索关键词") @RequestParam("keyword") String keyword,
                          @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
                          @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size) {
        try {
            List<FileMetadata> searchResults = fileMetadataService.searchFiles(keyword, page, size);
            return ResponseEntity.ok(searchResults);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("搜索文件失败: " + e.getMessage());
        }
    }

    /**
     * 查询指定路径下的目录结构。
     *
     * @param path 目录路径，默认为根路径
     * @return 目录结构，包含子目录、文件与当前路径；查询异常时返回 500 与错误文本
     */
    @GetMapping("/directory")
    @Operation(summary = "获取目录结构", description = "获取文件系统目录结构")
    public ResponseEntity<?> getDirectoryStructure(@Parameter(description = "目录路径") @RequestParam(defaultValue = "/") String path) {
        try {
            Object directoryStructure = fileMetadataService.getDirectoryStructure(path);
            return ResponseEntity.ok(directoryStructure);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("获取目录结构失败: " + e.getMessage());
        }
    }

    /**
     * 根据文件 ID 查询文件名。
     *
     * @param fileId 文件 ID
     * @return 文件名；元数据不存在时返回 404，查询异常时返回 500 与错误文本
     */
    @GetMapping("/metadata/{fileId}/name")
    @Operation(summary = "获取文件名", description = "根据文件ID获取文件名")
    public ResponseEntity<?> getFileName(@Parameter(description = "文件ID") @PathVariable String fileId) {
        try {
            FileMetadata metadata = fileMetadataService.getMetadata(fileId);
            if (metadata == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(metadata.getFileName());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("获取文件名失败: " + e.getMessage());
        }
    }

    /**
     * 根据文件 ID 查询文件类型。
     *
     * @param fileId 文件 ID
     * @return 文件的内容类型；元数据不存在时返回 404，查询异常时返回 500 与错误文本
     */
    @GetMapping("/metadata/{fileId}/type")
    @Operation(summary = "获取文件类型", description = "根据文件ID获取文件类型")
    public ResponseEntity<?> getFileType(@Parameter(description = "文件ID") @PathVariable String fileId) {
        try {
            FileMetadata metadata = fileMetadataService.getMetadata(fileId);
            if (metadata == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(metadata.getFileType());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("获取文件类型失败: " + e.getMessage());
        }
    }

}
