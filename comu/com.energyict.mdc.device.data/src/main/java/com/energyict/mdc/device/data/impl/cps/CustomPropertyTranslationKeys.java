/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.data.impl.cps;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;

public enum CustomPropertyTranslationKeys implements TranslationKey {

    DOMAIN_NAME_DEVICE("domain.name.device", "Device"),
    CPS_SIM_CARD("device.cps.simcard", "SIM card"),

    ICCID("property.iccid", "ICCID"),
    ICCID_DESCRIPTION("property.iccid.description", "ICCID"),

    PROVIDER("property.provider", "Provider"),
    PROVIDER_DESCRIPTION("property.provider.description", "Provider"),

    ACTIVE_IMSI("property.active.imsi", "Active IMSI"),
    ACTIVE_IMSI_DESCRIPTION("property.active.imsi.description", "Active IMSI"),

    INACTIVE_IMSI_FIRST("property.inactive.imsi.first", "Inactive IMSI #1"),
    INACTIVE_IMSI_FIRST_DESCRIPTION("property.inactive.imsi.first.description", "Inactive IMSI #1"),

    INACTIVE_IMSI_SECOND("property.inactive.imsi.second", "Inactive IMSI #2"),
    INACTIVE_IMSI_SECOND_DESCRIPTION("property.inactive.imsi.second.description", "Inactive IMSI #2"),

    INACTIVE_IMSI_THIRD("property.inactive.imsi.third", "Inactive IMSI #3"),
    INACTIVE_IMSI_THIRD_DESCRIPTION("property.inactive.imsi.third.description", "Inactive IMSI #3"),

    BATCH_ID("property.batch.id", "Batch ID"),
    BATCH_ID_DESCRIPTION("property.batch.id.description", "Batch ID"),

    CARD_FORMAT("property.cardformat", "Card format"),
    CARD_FORMAT_DESCRIPTION("property.cardformat.description", "Card format"),
    CARD_FORMAT_FULL_SIZE("property.cardformat.fullsize", "Full-size (1FF)"),
    CARD_FORMAT_MINI("property.cardformat.mini", "Mini (2FF)"),
    CARD_FORMAT_MICRO("property.cardformat.micro", "Micro (3FF)"),
    CARD_FORMAT_NANO("property.cardformat.nano", "Nano (4FF)"),
    CARD_FORMAT_EMBEDDED("property.cardformat.embedded", "Embedded (e-SIM)"),
    CARD_FORMAT_SW("property.cardformat.sw", "SW SIM (software SIM)"),

    STATUS("property.status", "Status"),
    STATUS_DESCRIPTION("property.status.description", "Status"),
    STATUS_ACTIVE("property.status.active", "Active"),
    STATUS_DEMOLISHED("property.status.demolished", "Demolished"),
    STATUS_INACTIVE("property.status.inactive", "Inactive"),
    STATUS_PRE_ACTIVE("property.status.pre.active", "Pre-active"),
    STATUS_TEST("property.status.test", "Test"),

    ;

    private final String key;
    private final String defaultFormat;

    CustomPropertyTranslationKeys(String key, String defaultFormat) {
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

    public static String translationFor(CustomPropertyTranslationKeys key, Thesaurus thesaurus) {
        return thesaurus.getFormat(key).format();
    }
}