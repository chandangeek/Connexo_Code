/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.processor.impl;

import com.elster.jupiter.nls.TranslationKey;

import java.util.Arrays;
import java.util.List;

public enum FormatterProperties implements TranslationKey {
    FORMATTER("fileFormat", "File format"),
    UPDATEDDATA("updatedData", "Updated data"),
    UPDATE_IN_SEPARATE_FILE(FORMATTER.getKey() + '.' + UPDATEDDATA.getKey() + ".separateFile", "Separate file"),
    UPDATE_FILE_PREFIX(FORMATTER.getKey() + '.' + UPDATEDDATA.getKey() + ".updateFilenamePrefix", "File name prefix"),
    UPDATE_FILE_EXTENSION(FORMATTER.getKey() + '.' + UPDATEDDATA.getKey() + ".updateFileExtension", "File extension"),
    FORMATTER_PROPERTIES("formatterProperties", "Formatter properties"),
    SEPARATOR(FORMATTER_PROPERTIES.getKey() + ".separator", "Separator"),
    SEPARATOR_COMMA(FORMATTER_PROPERTIES.getKey() + ".separator.comma", "Comma (,)"),
    SEPARATOR_SEMICOLON(FORMATTER_PROPERTIES.getKey() + ".separator.semicolon", "Semicolon (;)"),
    TAG(FORMATTER_PROPERTIES.getKey() + '.' + "tag", "Identifier"),
    UPDATE_TAG(FORMATTER_PROPERTIES.getKey() + '.' + "update.tag", "Update identifier"),
    ;

    private String name;
    private String defaultTranslation;

    FormatterProperties(String name, String defaultTranslation) {
        this.name = name;
        this.defaultTranslation = defaultTranslation;
    }

    public static FormatterProperties defaultSeparator() {
        return SEPARATOR_COMMA;
    }

    public static List<FormatterProperties> separatorValues() {
        return Arrays.asList(SEPARATOR_COMMA, SEPARATOR_SEMICOLON);
    }

    public static FormatterProperties separatorValueFrom(String key) {
        return separatorValues()
                .stream()
                .filter(p -> p.getKey().equals(key))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Unknown or unsupport formatter separator:" + key));
    }

    @Override
    public String getKey() {
        return name;
    }

    @Override
    public String getDefaultFormat() {
        return defaultTranslation;
    }
}