package com.elster.jupiter.bpm.rest;


import com.elster.jupiter.nls.TranslationKey;

public enum TranslationKeys implements TranslationKey {

    TASK_ASSIGNEE_ME ("TaskAssigneeMe", "Me"),
    TASK_ASSIGNEE_UNASSIGNED ("TaskAssigneeUnassigned", "Unassigned"),
    ;

    private final String key;
    private final String defaultFormat;

    TranslationKeys(String key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultFormat;
    }

    public static TranslationKeys from(String key) {
        if (key != null) {
            for (TranslationKeys translationKey : TranslationKeys.values()) {
                if (translationKey.getKey().equals(key)) {
                    return translationKey;
                }
            }
        }
        return null;
    }

}