/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.masterdata.rest.impl;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Where;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class ReadingTypeFilterUtil {

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
    static List<ReadingType> getFilteredList(String searchText, List<ReadingType> readingTypes) {
        if (searchText == null || searchText.isEmpty())
            return readingTypes;

        String regex = searchText.replace(" ", "*");
        regex = ".*" + computeRegex(regex) + ".*";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        return readingTypes.stream()
                .filter(rt -> pattern.matcher(rt.getFullAliasName()).matches())
                .collect(Collectors.toList());
    }

    /**
     * If search text is not present, we just filter the invalid reading types for register type creation.
     * @param dbSearchText like query parameter value
     * @return Condition to query the database
     */
    static Condition getFilterCondition(String dbSearchText) {
        if (dbSearchText == null || dbSearchText.isEmpty()){
            return mridMatchOfRegisters();
        }
        String regex = "*" + dbSearchText.replace(" ", "*") + "*";
        return Where.where("fullAliasName").likeIgnoreCase(regex).and(mridMatchOfRegisters());
    }

    /**
     *
     * @param mRID regular expression to match mRID values
     * @return Condition to query the database
     */
    static Condition getMRIDFilterCondition(String mRID){
        return Where.where("mRID").matches(mRID, "").and(mridMatchOfRegisters());
    }

    /**
     * @return Condition that removes the invalid reading types for register type creation
     */
    private static Condition mridMatchOfRegisters() {
        return mrIdMatchOfNormalRegisters()
                .or(mrIdMatchOfBillingRegisters())
                .or(mrIdMatchOfPeriodRelatedRegisters());
    }

    private static Condition mrIdMatchOfPeriodRelatedRegisters() {
        return Where.where("mRID").matches("^[11-13]\\.\\[1-24]\\.0", "");
    }

    private static Condition mrIdMatchOfBillingRegisters() {
        return Where.where("mRID").matches("^8\\.\\d+\\.0", "");
    }

    private static Condition mrIdMatchOfNormalRegisters() {
        return Where.where("mRID").matches("^0\\.\\d+\\.0", "");
    }

}
