package com.ywl01.bing.utils;

import android.text.TextUtils;

/**
 * Created by ywl01 on 2017/3/13.
 */

public class StringUtils {
    public static boolean isEmpty(String str) {
        if (TextUtils.isEmpty(str))
            return true;
        else if ("null".equals(str.toLowerCase()))
            return true;
        else
            return false;
    }

    public static String checkStr(String str) {
        if (TextUtils.isEmpty(str)) {
            return "";
        } else if (str.toLowerCase().equals("null")) {
            return "";
        } else
            return str;
    }

    /**
     * 判断给定字符串是否空白串 空白串是指由空格、制表符、回车符、换行符组成的字符串 若输入字符串为null或空字符串，返回true
     */
    public static boolean isBland(CharSequence input) {
        if (input == null || "".equals(input) || "null".equals(input))
            return true;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c != ' ' && c != '\t' && c != '\r' && c != '\n') {
                return false;
            }
        }
        return true;
    }
}
