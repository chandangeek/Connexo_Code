/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util;

/**
 * This class contains some of the methods that can be utilized to perform verification of paths/URI based on requirement.
 *
 * @author Diksha
 * @since 19-May-2020
 */

import java.nio.file.InvalidPathException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PathVerification {

    /**
     * This method splits the input path into tokens by '/' and checks if any token contains path backtracking inputs.
     *
     * @param inputPath
     * @throws InvalidPathException if the input path contains characters that perform path traversals.
     */
    public static void validatePathForFolders(String inputPath) {
        if (!inputPath.isEmpty()) {
            String[] tokens = inputPath.replace("\\", "/").split("/");
            //tokens are folder & file names split by / in the path
            for (String token : tokens) {
                if (token.equals("..") || token.equals(".")) {
                    throw new InvalidPathException(inputPath, "Invalid characters in path");
                }
            }
        }
    }

    /**
     * This method checks for any invalid characters that are present in input.
     *
     * @param input
     */
    public static boolean validateInputPattern(CharSequence input,String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        return !matcher.find();
    }

    /**
     * This method checks every character of input and performs whitelisting
     *
     * @param inputPath
     * @return false if path contains any character which is not whitelisted
     */
    public static boolean pathCharVerification(String inputPath) {
        StringBuilder verifiedPath = new StringBuilder();
        if (!inputPath.isEmpty()) {
            for (int i = 0; i < inputPath.length(); ++i) {
                verifiedPath.append(charCheck(inputPath.charAt(i)));
            }
            return !verifiedPath.toString().contains("%");
        }
        return false;
    }

    private static char charCheck(char inputChar) {
        for (int i = 48; i < 58; ++i) {
            if (inputChar == i) {
                return (char) i;
            }
        }
        for (int i = 65; i < 91; ++i) {
            if (inputChar == i) {
                return (char) i;
            }
        }
        for (int i = 97; i < 123; ++i) {
            if (inputChar == i) {
                return (char) i;
            }
        }
        switch (inputChar) {
            case '/':
                return '/';
            case '.':
                return '.';
            case '-':
                return '-';
            case '_':
                return '_';
            case ' ':
                return ' ';
            case ':':
                return ':';
        }
        return '%';
    }
}