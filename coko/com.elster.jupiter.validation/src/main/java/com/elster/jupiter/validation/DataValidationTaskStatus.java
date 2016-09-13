package com.elster.jupiter.validation;


import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;

public enum DataValidationTaskStatus {
    BUSY("Ongoing"),
    SUCCESS("Successful"),
    WARNING("Warning"),
    FAILED("Failed"),
    NOT_PERFORMED("Created");

    private String name;

    DataValidationTaskStatus(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }

    public String getDisplayName(Thesaurus thesaurus) {
        return thesaurus.getString("validation.task.status." + this.name.toLowerCase(), this.name);
    }

    public enum DataValidationTaskStatusTranslationKeys implements TranslationKey {
        BUSY("validation.task.status.ongoing", "Ongoing"),
        SUCCESS("validation.task.status.successful", "Successful"),
        WARNING("validation.task.status.warning", "Warning"),
        FAILED("validation.task.status.failed", "Failed"),
        NOT_PERFORMED("validation.task.status.created", "Created");
        private final String key;
        private final String defaultFormat;

        DataValidationTaskStatusTranslationKeys(String key, String defaultFormat) {
            this.key = key;
            this.defaultFormat = defaultFormat;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public String getDefaultFormat() {
            return defaultFormat;
        }


    }

}
