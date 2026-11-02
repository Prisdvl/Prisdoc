package com.javaee.fileservice.util;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * 文件处理工具类，提供唯一文件名生成、类型识别与字节流转换等静态方法。
 */
public class FileUtils {

    /**
     * 用随机 UUID 与原始扩展名生成唯一文件名。
     *
     * @param originalFileName 原始文件名，用于提取扩展名
     * @return 唯一文件名，原始文件无扩展名时只返回 UUID
     */
    public static String generateUniqueFileName(String originalFileName) {
        String extension = getFileExtension(originalFileName);
        String fileName = UUID.randomUUID().toString();
        if (extension != null) {
            fileName += "." + extension;
        }
        return fileName;
    }

    /**
     * 取文件名最后一个点之后的扩展名。
     *
     * @param fileName 文件名
     * @return 小写扩展名；文件名为空或不含点时返回 null
     */
    public static String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf('.') == -1) {
            return null;
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    }

    /**
     * 按扩展名把文件归入图片、文档、视频等大类。
     *
     * @param fileName 文件名
     * @return 文件大类编码：image、document、video、audio、archive 或 other
     */
    public static String getFileType(String fileName) {
        String extension = getFileExtension(fileName);
        if (extension == null) {
            return "other";
        }

        // 图片文件
        String[] imageExtensions = {"jpg", "jpeg", "png", "gif", "bmp", "webp"};
        for (String ext : imageExtensions) {
            if (ext.equals(extension)) {
                return "image";
            }
        }

        // 文档文件
        String[] docExtensions = {"doc", "docx", "pdf", "txt", "xls", "xlsx", "ppt", "pptx"};
        for (String ext : docExtensions) {
            if (ext.equals(extension)) {
                return "document";
            }
        }

        // 视频文件
        String[] videoExtensions = {"mp4", "avi", "mov", "wmv", "flv", "mkv"};
        for (String ext : videoExtensions) {
            if (ext.equals(extension)) {
                return "video";
            }
        }

        // 音频文件
        String[] audioExtensions = {"mp3", "wav", "ogg", "flac", "aac"};
        for (String ext : audioExtensions) {
            if (ext.equals(extension)) {
                return "audio";
            }
        }

        // 压缩文件
        String[] archiveExtensions = {"zip", "rar", "7z", "tar", "gz"};
        for (String ext : archiveExtensions) {
            if (ext.equals(extension)) {
                return "archive";
            }
        }

        return "other";
    }

    /**
     * 把上传文件保存到本地，父目录不存在时自动创建。
     *
     * @param file 待保存的上传文件
     * @param filePath 目标文件的完整路径
     * @return 保存后的目标文件
     * @throws IOException 写入目标文件失败时抛出
     */
    public static File saveMultipartFile(MultipartFile file, String filePath) throws IOException {
        File destFile = new File(filePath);
        File parentDir = destFile.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }
        file.transferTo(destFile);
        return destFile;
    }

    /**
     * 删除本地文件，文件不存在时视为删除失败。
     *
     * @param filePath 待删除文件的路径
     * @return 文件存在且删除成功返回 true，否则返回 false
     */
    public static boolean deleteFile(String filePath) {
        File file = new File(filePath);
        return file.exists() && file.delete();
    }

    /**
     * 检查文件大小是否超过上限。
     *
     * @param file 待检查的上传文件
     * @param maxSize 大小上限，单位为 MB
     * @return 未超过上限返回 true，否则返回 false
     */
    public static boolean checkFileSize(MultipartFile file, long maxSize) {
        return file.getSize() <= maxSize * 1024 * 1024;
    }

    /**
     * 把字节数组包装为字节数组输入流。
     *
     * @param bytes 字节数组
     * @return 对应的字节数组输入流
     */
    public static java.io.InputStream toInputStream(byte[] bytes) {
        return new java.io.ByteArrayInputStream(bytes);
    }

    /**
     * 读取输入流中的全部字节，调用方负责关闭输入流。
     *
     * @param inputStream 待读取的输入流
     * @return 输入流中的全部字节
     * @throws java.io.IOException 读取输入流失败时抛出
     */
    public static byte[] toByteArray(java.io.InputStream inputStream) throws java.io.IOException {
        java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, length);
        }
        return outputStream.toByteArray();
    }

    /**
     * 按文件名推断 MIME 类型。
     *
     * @param fileName 文件名
     * @return 对应的 MIME 类型；无法识别时返回 application/octet-stream
     */
    public static String getContentType(String fileName) {
        String extension = getFileExtension(fileName);
        if (extension == null) {
            return "application/octet-stream";
        }

        // 常见文件类型的 MIME 类型映射
        java.util.Map<String, String> mimeTypes = new java.util.HashMap<>();
        mimeTypes.put("jpg", "image/jpeg");
        mimeTypes.put("jpeg", "image/jpeg");
        mimeTypes.put("png", "image/png");
        mimeTypes.put("gif", "image/gif");
        mimeTypes.put("webp", "image/webp");
        mimeTypes.put("pdf", "application/pdf");
        mimeTypes.put("doc", "application/msword");
        mimeTypes.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        mimeTypes.put("xls", "application/vnd.ms-excel");
        mimeTypes.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        mimeTypes.put("ppt", "application/vnd.ms-powerpoint");
        mimeTypes.put("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
        mimeTypes.put("txt", "text/plain");
        mimeTypes.put("csv", "text/csv");
        mimeTypes.put("html", "text/html");
        mimeTypes.put("css", "text/css");
        mimeTypes.put("js", "application/javascript");
        mimeTypes.put("json", "application/json");
        mimeTypes.put("xml", "application/xml");
        mimeTypes.put("zip", "application/zip");
        mimeTypes.put("rar", "application/x-rar-compressed");
        mimeTypes.put("7z", "application/x-7z-compressed");
        mimeTypes.put("mp3", "audio/mpeg");
        mimeTypes.put("wav", "audio/wav");
        mimeTypes.put("mp4", "video/mp4");
        mimeTypes.put("avi", "video/x-msvideo");
        mimeTypes.put("mov", "video/quicktime");

        return mimeTypes.getOrDefault(extension.toLowerCase(), "application/octet-stream");
    }

}
