package com.elster.jupiter.export.processor.impl;

import com.elster.jupiter.nls.TranslationKey;

public enum FormatterProperties implements TranslationKey {
    FORMATTER("fileFormat", "File format"),
    UPDATEDDATA("updatedData", "Updated data"),
    UPDATE_IN_SEPARATE_FILE(FORMATTER.getKey() + '.' + UPDATEDDATA.getKey() + ".separateFile", "Separate file"),
    UPDATE_FILE_PREFIX(FORMATTER.getKey() + '.' + UPDATEDDATA.getKey() + ".updateFilenamePrefix", "File name prefix"),
    UPDATE_FILE_EXTENSION(FORMATTER.getKey() + '.' + UPDATEDDATA.getKey() + ".updateFileExtension", "File extension"),
    FORMATTER_PROPERTIES("formatterProperties", "Formatter properties"),
    SEPARATOR(FORMATTER_PROPERTIES.getKey() + ".separator", "Separator"),
    TAG(FORMATTER_PROPERTIES.getKey() + '.' + "tag", "Tag"),
    UPDATE_TAG(FORMATTER_PROPERTIES.getKey() + '.' + "update.tag", "Tag");


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
