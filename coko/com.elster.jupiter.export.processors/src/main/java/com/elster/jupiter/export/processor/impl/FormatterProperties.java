package com.elster.jupiter.export.processor.impl;

import com.elster.jupiter.nls.TranslationKey;

/**
 * Copyrights EnergyICT
 * Date: 29/10/2014
 * Time: 13:11
 */
public enum FormatterProperties implements TranslationKey {
    FORMATTER("fileFormat", "File format"),
    FILE_PATH(FORMATTER.getKey() + ".path", "Relative export folder path"),
    FILENAME_PREFIX(FORMATTER.getKey() + ".filenamePrefix", "File name prefix"),
    FILE_EXTENSION(FORMATTER.getKey() + ".fileExtension", "File extension"),
    UPDATEDDATA("updatedData", "Updated data"),
    UPDATE_IN_SEPARATE_FILE(FORMATTER.getKey() + "." + UPDATEDDATA.getKey() + ".separateFile", "Separate file"),
    UPDATE_FILE_PREFIX(FORMATTER.getKey() + "." + UPDATEDDATA.getKey() + ".updateFilenamePrefix", "File name prefix"),
    UPDATE_FILE_EXTENSION(FORMATTER.getKey() + "." + UPDATEDDATA.getKey() + ".updateFileExtension", "File extension"),
    FORMATTER_PROPERTIES("formatterProperties", "Formatter properties"),
    SEPARATOR(FORMATTER_PROPERTIES.getKey() + ".separator", "Separator");


    private String name;
    private String defaultTranslation;

    private FormatterProperties(String name, String defaultTranslation) {
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
