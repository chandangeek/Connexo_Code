/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.config;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.nls.SimpleNlsKey;
import com.elster.jupiter.nls.TranslationKey;

public enum DefaultMetrologyPurpose {
    BILLING(Translation.BILLING_NAME, Translation.BILLING_DESCRIPTION),
    INFORMATION(Translation.INFORMATION_NAME, Translation.INFORMATION_DESCRIPTION),
    VOLTAGE_MONITORING(Translation.VOLTAGE_MONITORING_NAME, Translation.VOLTAGE_MONITORING_DESCRIPTION),;

    private TranslationKey name;
    private TranslationKey description;

    DefaultMetrologyPurpose(TranslationKey name, TranslationKey description) {
        this.name = name;
        this.description = description;
    }

    public NlsKey getName() {
        return SimpleNlsKey.key(MeteringService.COMPONENTNAME, Layer.DOMAIN, this.name.getKey())
                .defaultMessage(this.name.getDefaultFormat());
    }

    public NlsKey getDescription() {
        return SimpleNlsKey.key(MeteringService.COMPONENTNAME, Layer.DOMAIN, this.description.getKey())
                .defaultMessage(this.description.getDefaultFormat());
    }

    public enum Translation implements TranslationKey {
        BILLING_NAME("metrology.purpose.billing.name", "Billing"),
        INFORMATION_NAME("metrology.purpose.information.name", "Information"),
        VOLTAGE_MONITORING_NAME("metrology.purpose.voltage.monitoring.name", "Voltage monitoring"),
        BILLING_DESCRIPTION("metrology.purpose.billing.description", "The calculation of data based on data from meters for further export to the external billing system"),
        INFORMATION_DESCRIPTION("metrology.purpose.information.description", "Information metrology purpose"),
        VOLTAGE_MONITORING_DESCRIPTION("metrology.purpose.voltage.monitoring.description", "Voltage monitoring metrology purpose"),

        METROLOGY_CONTRACT_STATUS_COMPLETE("metrology.contract.status.complete", "Complete"),
        METROLOGY_CONTRACT_STATUS_INCOMPLETE("metrology.contract.status.incomplete", "Incomplete"),
        METROLOGY_CONTRACT_STATUS_UNKNOWN("metrology.contract.status.unknown", "Unknown"),;

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
