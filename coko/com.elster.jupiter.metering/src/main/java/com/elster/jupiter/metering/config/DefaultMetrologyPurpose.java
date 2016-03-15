package com.elster.jupiter.metering.config;

import com.elster.jupiter.nls.TranslationKey;

public enum DefaultMetrologyPurpose {
    BILLING(Translation.BILLING_NAME, Translation.BILLING_DESCRIPTION),
    INFORMATION(Translation.INFORMATION_NAME, Translation.INFORMATION_DESCRIPTION),
    VOLTAGE_MONITORING(Translation.VOLTAGE_MONITORING_NAME, Translation.VOLTAGE_MONITORING_DESCRIPTION),;

    private TranslationKey nameTranslationKey;
    private TranslationKey descriptionTranslationKey;

    DefaultMetrologyPurpose(TranslationKey nameTranslationKey, TranslationKey descriptionTranslationKey) {
        this.nameTranslationKey = nameTranslationKey;
        this.descriptionTranslationKey = descriptionTranslationKey;
    }

    public TranslationKey getNameTranslationKey() {
        return nameTranslationKey;
    }

    public TranslationKey getDescriptionTranslationKey() {
        return descriptionTranslationKey;
    }

    public enum Translation implements TranslationKey {
        BILLING_NAME("metrology.purpose.billing.name", "Billing"),
        INFORMATION_NAME("metrology.purpose.information.name", "Information"),
        VOLTAGE_MONITORING_NAME("metrology.purpose.voltage.monitoring.name", "Voltage monitoring"),
        BILLING_DESCRIPTION("metrology.purpose.billing.description", "The calculation of data based on data from meters for further export to the external billing system"),
        INFORMATION_DESCRIPTION("metrology.purpose.information.description", ""),
        VOLTAGE_MONITORING_DESCRIPTION("metrology.purpose.voltage.monitoring.description", ""),;

        private String key;
        private String defaultFormat;

        Translation(String key, String defaultFormat) {
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
    }
}
