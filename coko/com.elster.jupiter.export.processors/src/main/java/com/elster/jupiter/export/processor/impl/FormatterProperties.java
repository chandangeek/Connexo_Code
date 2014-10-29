package com.elster.jupiter.export.processor.impl;

import com.elster.jupiter.nls.TranslationKey;

/**
 * Copyrights EnergyICT
 * Date: 29/10/2014
 * Time: 13:11
 */
public enum FormatterProperties implements TranslationKey {
    FILENAME_PREFIX("fileFormat.filenamePrefix", "File name prefix"),
    FILE_EXTENSION("fileFormat.fileExtension", "File extension"),
    UPDATE_IN_SEPARATE_FILE("fileFormat.updatedData.separateFile", "Separate file"),
    UPDATE_FILE_PREFIX("fileFormat.updatedData.filenamePrefix", "File name prefix"),
    UPDATE_FILE_EXTENSION("fileFormat.updatedData.fileExtension", "File extension"),
    SEPARATOR("formatterProperties.separator", "Separator");

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
