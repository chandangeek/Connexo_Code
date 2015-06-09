package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.nls.TranslationKey;

/**
 * Created by bvn on 6/8/15.
 */
public enum TranslationSeeds implements TranslationKey {
    MRID("mRID", "mRID"),
    SERVICE_CATEGORY("serviceCategory", "Service category"),
    ALIAS_NAME("aliasName", "Alias"),
    DESCRIPTION("description", "Description"),
    NAME("name", "Name"),
    BILLING_READY("amiBillingReady", "AMI billing ready"),
    CHECK_BILLING("checkBilling", "Check billing"),
    CONNECTION_STATE("connectionState", "Connection state"),
    ESTIMATED_LOAD("estimatedLoad", "Estimated load"),
    GROUNDED("grounded", "Grounded"),
    DSP("isSdp", "Service delivery point"),
    VIRTUAL("isVirtual", "Virtual"),
    MIN_USAGE_EXPECTED("minimalUsageExpected", "Min. usage expected"),
    SERVICE_VOLTAGE("nominalServiceVoltage", "Nom. service voltage"),
    OUTAGE_REGION("outageRegion", "Outage region"),
    PHASE_CODE("phaseCode", "phaseCode"),
    RATED_CURRENT("ratedCurrent", "Rated current"),
    RATED_POWER("ratedPower", "Rated power"),
    READ_CYCLE("readCycle", "Read cycle"),
    READ_ROUTE("readRoute", "Read route"),
    REMARK("serviceDeliveryRemark", "Service remark"),
    PRIORITY("servicePriority", "Service priority");

    private final String key;
    private final String defaultFormat;

    TranslationSeeds(String key, String defaultFormat) {
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
