package com.javaee.fileservice.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5 计算工具类，支持对文件与输入流计算摘要。
 */
public class Md5Utils {

    private static final char[] HEX_CHARS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    /**
     * 计算文件的 MD5 值。
     *
     * @param file 待计算的文件
     * @return 32 位小写十六进制 MD5 字符串
     * @throws IOException 读取文件失败时抛出
     */
    public static String calculateFileMd5(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            return calculateInputStreamMd5(fis);
        }
    }

    /**
     * 计算输入流的 MD5 值，输入流由调用方负责关闭。
     *
     * @param inputStream 待读取的输入流
     * @return 32 位小写十六进制 MD5 字符串
     * @throws IOException 读取输入流失败或 MD5 算法不可用时抛出
     */
    public static String calculateInputStreamMd5(InputStream inputStream) throws IOException {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[8192];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                md.update(buffer, 0, read);
            }
            byte[] digest = md.digest();
            return bytesToHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IOException("MD5 algorithm not found", e);
        }
    }

    /**
     * 将字节数组转换为十六进制字符串。
     */
    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            hexChars[i * 2] = HEX_CHARS[v >>> 4];
            hexChars[i * 2 + 1] = HEX_CHARS[v & 0x0F];
        }
        return new String(hexChars);
    }

}
