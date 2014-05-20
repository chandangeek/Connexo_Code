package com.energyict.mdc.tasks.rest.util;

public class RestHelper {
    public static String capitalize(String s) {
        StringBuilder sb = new StringBuilder(s);
        sb.replace(0, 1, s.substring(0, 1).toUpperCase());
        return sb.toString();
    }
}