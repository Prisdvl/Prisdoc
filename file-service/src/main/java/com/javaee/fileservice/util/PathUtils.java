package com.javaee.fileservice.util;

import java.io.File;

/**
 * 路径拼接与校验工具类，提供路径拼接、标准化、安全校验与相对路径计算。
 */
public class PathUtils {

    /**
     * 用系统分隔符拼接多个路径片段。
     *
     * @param paths 待拼接的路径片段，空片段会被跳过
     * @return 拼接后的路径；入参为空时返回空字符串
     */
    public static String joinPath(String... paths) {
        if (paths == null || paths.length == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < paths.length; i++) {
            String path = paths[i];
            if (path == null || path.isEmpty()) {
                continue;
            }

            if (i > 0 && !path.startsWith(File.separator)) {
                sb.append(File.separator);
            }

            if (i == paths.length - 1 && path.endsWith(File.separator)) {
                path = path.substring(0, path.length() - 1);
            }

            sb.append(path);
        }

        return sb.toString();
    }

    /**
     * 统一路径分隔符并压缩多余的斜杠。
     *
     * @param path 原始路径
     * @return 统一使用正斜杠、去掉重复斜杠与末尾斜杠的路径；入参为 null 时返回空字符串
     */
    public static String normalizePath(String path) {
        if (path == null) {
            return "";
        }

        // 替换反斜杠为正斜杠
        path = path.replace('\\', '/');

        // 移除多余的斜杠
        while (path.contains("//")) {
            path = path.replace("//", "/");
        }

        // 移除末尾的斜杠
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        return path;
    }

    /**
     * 检查路径是否安全（防止目录遍历攻击）。
     *
     * @param path 待校验的相对路径
     * @return 不含 ..、不是绝对路径且不含 Windows 驱动器号时返回 true
     */
    public static boolean isSafePath(String path) {
        if (path == null) {
            return false;
        }

        // 检查是否包含 ".." 路径
        if (path.contains("..")) {
            return false;
        }

        // 检查是否以根路径开头（绝对路径）
        if (path.startsWith("/") || path.startsWith(File.separator)) {
            return false;
        }

        // 检查是否包含 Windows 驱动器号
        if (path.matches("^[a-zA-Z]:.*")) {
            return false;
        }

        return true;
    }

    /**
     * 计算完整路径相对基准目录的部分。
     *
     * @param basePath 基准目录
     * @param fullPath 完整路径
     * @return 相对基准目录的路径；fullPath 不在 basePath 下时原样返回 fullPath
     */
    public static String getRelativePath(String basePath, String fullPath) {
        if (basePath == null || fullPath == null) {
            return fullPath;
        }

        basePath = normalizePath(basePath);
        fullPath = normalizePath(fullPath);

        if (fullPath.startsWith(basePath)) {
            String relativePath = fullPath.substring(basePath.length());
            if (relativePath.startsWith("/")) {
                relativePath = relativePath.substring(1);
            }
            return relativePath;
        }

        return fullPath;
    }

}
