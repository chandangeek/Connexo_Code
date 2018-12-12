/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.masterdata.rest.impl;

import com.elster.jupiter.metering.ReadingType;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class ReadingTypeUtil {

    private final static int NUMBER_OF_READING_TYPE_ARGUMENTS = 18;
    private final static String DELIMITER = "\\\\.";
    private final static String NUMBER_REGEX = "[0-9]+";
    private final static String DOT= ".";
    public final static String DEFAULT_VALUE = "0";


    /**
     * Check every MRID field for multiple matches. If there is a single match,
     * we return the unchanged value, otherwise we return a default value.
     * Example:
     * IN : 0\.0\.0.\.0\.1\.1\.(12|37)\.0\.0\.0\.0\.0\.[0-9]+\.[0-9]+\.0\.-?[0-9]+\.[0-9]+\.[0-9]+
     * OUT: 0.0.0.0.1.1.0.0.0.0.0.0.0.0.0.0.0.0
     * @param regex MRID regex that matches one or multiple reading types
     * @return String
     */
    public static String extractUniqueFromRegex(String regex) {
        String values[] = regex.split(DELIMITER);
        if (values.length != NUMBER_OF_READING_TYPE_ARGUMENTS) {
            return "";
        }
        Pattern numberPattern = Pattern.compile(NUMBER_REGEX);
        StringBuilder mrid = new StringBuilder();
        int i = 1;
        String code;
        for(String value : values){
            Matcher m = numberPattern.matcher(value);
            code = m.matches() ? value : DEFAULT_VALUE;
            mrid.append(code);

            if (i++ != values.length)
                mrid.append(DOT);
        }
        return mrid.toString();
    }

    /**
     * The only supported wildcards are "*" and "?"
     * We're trying to match the Where.toOracleSql behavior so:
     *  "*" - zero, one or multiple chars
     *  "?" - single character.
     * Every other character is quoted using the /Q quoted chars /E construction
     * @param regex Regex value entered by user
     * @return regex value that can be processed by Java Pattern class
     */
    public static String computeRegex(String regex){
        // This will add the start/end quotes and handle the special case where
        // the "//E" substring is present in the regex string
        regex = Pattern.quote(regex);

        // Java Pattern matches "?" to one or not at all. So we will use the {1}
        // construction to match exactly one character, as the sql underscore does
        regex = regex.replace("?","\\E.{1}\\Q");
        regex = regex.replace("*","\\E.*\\Q");

        // Previously we altered a possible quoted wildcard. Revert changes.
        regex = regex.replace("\\\\E.*\\Q", "*");
        regex = regex.replace("\\\\E.{1}\\Q", "?");
        return regex;
    }

    /**
     * @param searchText like query parameter value
     * @param readingTypes list of reading types that we've filtered from the database using a MRID regex
     * @return Input reading type list if search text is missing, otherwise a new list filtered by the search text.
     */
    public static List<ReadingType> getFilteredList(String searchText, List<ReadingType> readingTypes) {
        if (searchText == null || searchText.isEmpty())
            return readingTypes;

        String regex = searchText.replace(" ", "*");
        regex = ".*" + computeRegex(regex) + ".*";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        return readingTypes.stream()
                .filter(rt -> pattern.matcher(rt.getFullAliasName()).matches())
                .collect(Collectors.toList());
    }
}
