/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.prepayment.export.redknee.impl;

import com.elster.jupiter.nls.TranslationKey;

public enum FormatterProperties implements TranslationKey {
    FORMATTER("fileFormat", "File format"),
    UPDATEDDATA("updatedData", "Updated data"),
    UPDATE_IN_SEPARATE_FILE(FORMATTER.getKey() + '.' + UPDATEDDATA.getKey() + ".separateFile", "Separate file"),
    UPDATE_FILE_PREFIX(FORMATTER.getKey() + '.' + UPDATEDDATA.getKey() + ".updateFilenamePrefix", "File name prefix"),
    UPDATE_FILE_EXTENSION(FORMATTER.getKey() + '.' + UPDATEDDATA.getKey() + ".updateFileExtension", "File extension"),
    FORMATTER_PROPERTIES("formatterProperties", "Formatter properties"),
    SEPARATOR(FORMATTER_PROPERTIES.getKey() + ".separator", "Separator"),
    TAG(FORMATTER_PROPERTIES.getKey() + '.' + "tag", "Identifier"),
    UPDATE_TAG(FORMATTER_PROPERTIES.getKey() + '.' + "update.tag", "Update identifier");


    private String name;
    private String defaultTranslation;

    FormatterProperties(String name, String defaultTranslation) {
        this.name = name;
        this.defaultTranslation = defaultTranslation;
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
