package com.sonder.yunpicture.infrastructure.utils;

import java.awt.*;

/**
 * 颜色转换工具类
 */
public class ColorTransformUtils {


    /**
     * 获取标准颜色（将数据万象的 5 位色值转为6位）
     * @param color
     * @return
     */
    public static String getStandardColor(String color) {
        if (color == null || color.trim().isEmpty()) {
            throw new IllegalArgumentException("颜色值不能为空");
        }

        // 处理前缀和空白
        String trimmed = color.trim();
        if (!trimmed.startsWith("0x")) {
            throw new IllegalArgumentException("颜色值必须以0x开头: " + color);
        }

        // 提取颜色部分并验证
        String colorPart = trimmed.substring(2);
        int length = colorPart.length();

        // 验证长度是否在允许范围内
        if (length < 3 || length > 6) {
            throw new IllegalArgumentException("颜色值长度必须是3-6位: " + color);
        }

        // 验证是否为有效的十六进制字符
        if (!isValidHex(colorPart)) {
            throw new IllegalArgumentException("包含无效的十六进制字符: " + color);
        }

        // 根据不同长度进行补全
        switch (length) {
            case 3:
                // 3位格式: R G B → RR GG BB
                return "0x" + expandEachChar(colorPart).toUpperCase();

            case 4:
                // 4位格式: 取前3位按3位规则扩展（忽略第4位透明度）
                return "0x" + expandEachChar(colorPart.substring(0, 3)).toUpperCase();

            case 5:
                // 5位格式: 前4位两两分组，最后1位复制补全
                // 如F02A0 → F0 2A 00
                return "0x" + colorPart.substring(0, 4) + colorPart.charAt(4) + colorPart.charAt(4);

            case 6:
                // 6位格式直接返回
                return trimmed.toUpperCase();

            default:
                throw new IllegalArgumentException("无效的颜色值格式: " + color);
        }
    }

    /**
     * 将每个字符扩展为两个相同字符
     * 如"F00" → "FF0000"
     */
    private static String expandEachChar(String str) {
        StringBuilder sb = new StringBuilder();
        for (char c : str.toCharArray()) {
            sb.append(c).append(c);
        }
        return sb.toString();
    }

    /**
     * 验证是否为有效的十六进制字符
     */
    private static boolean isValidHex(String hex) {
        for (int i = 0; i < hex.length(); i++) {
            char c = hex.charAt(i);
            if (!((c >= '0' && c <= '9') ||
                    (c >= 'A' && c <= 'F') ||
                    (c >= 'a' && c <= 'f'))) {
                return false;
            }
        }
        return true;
    }

    // 测试方法
    public static void main(String[] args) {
        System.out.println(getStandardColor("0xF00"));    // 输出 0xFF0000
        System.out.println(getStandardColor("0xf0f"));    // 输出 0xF0F0F0
        System.out.println(getStandardColor("0xe000"));
        System.out.println(getStandardColor("0xF008"));   // 输出 0xFF0000（取前3位扩展）
        System.out.println(getStandardColor("0xF02A0"));  // 输出 0xF02A00（补全最后一位）
        System.out.println(getStandardColor("0xFF0000")); // 输出 0xFF0000
        System.out.println(getStandardColor("0xabcdEF")); // 输出 0xABCDEF
    }
}
