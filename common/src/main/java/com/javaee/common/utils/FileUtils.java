package com.javaee.common.utils;

import com.javaee.common.constant.CommonConstant;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

/**
 * 文件工具，提供文件类型判断、大小格式化、路径处理与读写能力。
 *
 * <p>类型判断基于小写扩展名；路径拼接与规范化统一走当前系统的文件分隔符。</p>
 *
 * @author qxk
 */
public class FileUtils {

    private static final List<String> IMAGE_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "webp");
    private static final List<String> DOCUMENT_EXTENSIONS = Arrays.asList("doc", "docx", "pdf", "txt", "xls", "xlsx", "ppt", "pptx");
    private static final List<String> VIDEO_EXTENSIONS = Arrays.asList("mp4", "avi", "mov", "wmv", "flv", "mkv");
    private static final List<String> AUDIO_EXTENSIONS = Arrays.asList("mp3", "wav", "flac", "aac", "ogg");
    private static final List<String> ARCHIVE_EXTENSIONS = Arrays.asList("zip", "rar", "7z", "tar", "gz");

    private static final DecimalFormat SIZE_FORMAT = new DecimalFormat("#.##");

    /**
     * 获取文件扩展名。
     *
     * @param filename 文件名或完整路径
     * @return 小写扩展名；无扩展名时返回空串
     */
    public static String getExtension(String filename) {
        if (StringUtils.isEmpty(filename)) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf(CommonConstant.PERIOD);
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1).toLowerCase();
    }

    /**
     * 判断是否为图片文件。
     *
     * @param filename 文件名或完整路径
     * @return true-扩展名属于图片类型
     */
    public static boolean isImage(String filename) {
        String extension = getExtension(filename);
        return IMAGE_EXTENSIONS.contains(extension);
    }

    /**
     * 判断是否为文档文件。
     *
     * @param filename 文件名或完整路径
     * @return true-扩展名属于文档类型
     */
    public static boolean isDocument(String filename) {
        String extension = getExtension(filename);
        return DOCUMENT_EXTENSIONS.contains(extension);
    }

    /**
     * 判断是否为视频文件。
     *
     * @param filename 文件名或完整路径
     * @return true-扩展名属于视频类型
     */
    public static boolean isVideo(String filename) {
        String extension = getExtension(filename);
        return VIDEO_EXTENSIONS.contains(extension);
    }

    /**
     * 判断是否为音频文件。
     *
     * @param filename 文件名或完整路径
     * @return true-扩展名属于音频类型
     */
    public static boolean isAudio(String filename) {
        String extension = getExtension(filename);
        return AUDIO_EXTENSIONS.contains(extension);
    }

    /**
     * 判断是否为压缩文件。
     *
     * @param filename 文件名或完整路径
     * @return true-扩展名属于压缩包类型
     */
    public static boolean isArchive(String filename) {
        String extension = getExtension(filename);
        return ARCHIVE_EXTENSIONS.contains(extension);
    }

    /**
     * 把字节数格式化为带单位的可读文本。
     *
     * @param size 文件大小，单位字节
     * @return 可读大小，如 1.5 MB；非正数返回 0 B
     */
    public static String formatSize(long size) {
        if (size <= 0) {
            return "0 B";
        }
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return SIZE_FORMAT.format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    /**
     * 获取文件大小。
     *
     * @param filepath 文件路径
     * @return 文件字节数；文件不存在时返回 0
     */
    public static long getFileSize(String filepath) {
        File file = new File(filepath);
        if (!file.exists()) {
            return 0;
        }
        return file.length();
    }

    /**
     * 判断文件或目录是否存在。
     *
     * @param filepath 文件路径
     * @return true-路径存在；入参为空时返回 false
     */
    public static boolean exists(String filepath) {
        if (StringUtils.isEmpty(filepath)) {
            return false;
        }
        return new File(filepath).exists();
    }

    /**
     * 判断路径是否指向普通文件。
     *
     * @param filepath 文件路径
     * @return true-路径存在且为文件
     */
    public static boolean isFile(String filepath) {
        if (StringUtils.isEmpty(filepath)) {
            return false;
        }
        File file = new File(filepath);
        return file.exists() && file.isFile();
    }

    /**
     * 判断路径是否指向目录。
     *
     * @param filepath 目录路径
     * @return true-路径存在且为目录
     */
    public static boolean isDirectory(String filepath) {
        if (StringUtils.isEmpty(filepath)) {
            return false;
        }
        File file = new File(filepath);
        return file.exists() && file.isDirectory();
    }

    /**
     * 创建目录，已存在时直接视为成功。
     *
     * @param dirpath 目录路径
     * @return true-目录已存在或创建成功
     */
    public static boolean createDirectory(String dirpath) {
        if (StringUtils.isEmpty(dirpath)) {
            return false;
        }
        File dir = new File(dirpath);
        if (dir.exists()) {
            return true;
        }
        return dir.mkdirs();
    }

    /**
     * 删除文件，文件不存在时直接视为成功。
     *
     * @param filepath 文件路径
     * @return true-已删除或本就不存在
     */
    public static boolean deleteFile(String filepath) {
        if (StringUtils.isEmpty(filepath)) {
            return false;
        }
        File file = new File(filepath);
        if (!file.exists()) {
            return true;
        }
        return file.delete();
    }

    /**
     * 获取文件名（不含扩展名）。
     *
     * @param filename 文件名
     * @return 去掉扩展名后的文件名
     */
    public static String getFileNameWithoutExtension(String filename) {
        if (StringUtils.isEmpty(filename)) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf(CommonConstant.PERIOD);
        if (lastDotIndex == -1) {
            return filename;
        }
        return filename.substring(0, lastDotIndex);
    }

    /**
     * 从路径中获取文件名（含扩展名）。
     *
     * @param filepath 文件路径
     * @return 文件名
     */
    public static String getFileName(String filepath) {
        if (StringUtils.isEmpty(filepath)) {
            return "";
        }
        int lastSeparatorIndex = Math.max(filepath.lastIndexOf(CommonConstant.FILE_SEPARATOR), filepath.lastIndexOf("/"));
        if (lastSeparatorIndex == -1) {
            return filepath;
        }
        return filepath.substring(lastSeparatorIndex + 1);
    }

    /**
     * 获取文件所在父目录。
     *
     * @param filepath 文件路径
     * @return 父目录路径；无父目录时返回 null
     */
    public static String getParentPath(String filepath) {
        if (StringUtils.isEmpty(filepath)) {
            return "";
        }
        File File = new File(filepath);
        return File.getParent();
    }

    /**
     * 拼接路径片段，自动补齐系统文件分隔符。
     *
     * @param paths 路径片段，按顺序拼接
     * @return 拼接后的路径；无片段时返回空串
     */
    public static String joinPath(String... paths) {
        if (paths == null || paths.length == 0) {
            return "";
        }
        StringBuilder result = new StringBuilder(paths[0]);
        for (int i = 1; i < paths.length; i++) {
            String path = paths[i];
            if (StringUtils.isEmpty(path)) {
                continue;
            }
            if (!result.toString().endsWith(CommonConstant.FILE_SEPARATOR) && !path.startsWith(CommonConstant.FILE_SEPARATOR)) {
                result.append(CommonConstant.FILE_SEPARATOR);
            }
            result.append(path);
        }
        return result.toString();
    }

    /**
     * 规范化路径，消除多余的 . 与 .. 片段。
     *
     * @param path 原始路径
     * @return 规范化后的路径；解析失败时原样返回
     */
    public static String normalizePath(String path) {
        if (StringUtils.isEmpty(path)) {
            return "";
        }
        try {
            Path normalizedPath = Paths.get(path).normalize();
            return normalizedPath.toString();
        } catch (Exception e) {
            return path;
        }
    }

    /**
     * 根据扩展名推断文件的 MIME 类型。
     *
     * @param filename 文件名
     * @return MIME 类型；未知类型返回 application/octet-stream
     */
    public static String getMimeType(String filename) {
        if (StringUtils.isEmpty(filename)) {
            return "application/octet-stream";
        }
        String extension = getExtension(filename);
        switch (extension) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "pdf":
                return "application/pdf";
            case "txt":
                return "text/plain";
            case "html":
                return "text/html";
            case "json":
                return "application/json";
            case "xml":
                return "application/xml";
            case "mp4":
                return "video/mp4";
            case "mp3":
                return "audio/mpeg";
            case "zip":
                return "application/zip";
            default:
                return "application/octet-stream";
        }
    }

    /**
     * 读取文件全部内容。
     *
     * @param filepath 文件路径
     * @return 文件文本内容；文件不存在时返回空串
     * @throws IOException 读取文件失败时抛出
     */
    public static String readFile(String filepath) throws IOException {
        if (!exists(filepath)) {
            return "";
        }
        Path path = Paths.get(filepath);
        return Files.readString(path);
    }

    /**
     * 将内容写入文件，文件不存在时自动创建。
     *
     * @param filepath 文件路径
     * @param content 待写入的文本内容
     * @throws IOException 写入文件失败时抛出
     */
    public static void writeFile(String filepath, String content) throws IOException {
        Path path = Paths.get(filepath);
        Files.writeString(path, content);
    }
}
