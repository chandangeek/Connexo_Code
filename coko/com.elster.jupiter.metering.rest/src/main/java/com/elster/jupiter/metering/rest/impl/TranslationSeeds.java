package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;

public enum TranslationSeeds implements TranslationKey {
    MRID("mRID", "MRID"),
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
    PHASE_CODE("phaseCode", "Phase code"),
    RATED_CURRENT("ratedCurrent", "Rated current"),
    RATED_POWER("ratedPower", "Rated power"),
    READ_CYCLE("readCycle", "Read cycle"),
    READ_ROUTE("readRoute", "Read route"),
    REMARK("serviceDeliveryRemark", "Service remark"),
    PRIORITY("servicePriority", "Service priority"),
    ISSUES("openIssues", "Open issues"), 
    
    SERIALNUMBER("serialNumber", "Serial number"),
    UTCNUMBER("utcNumber", "UTC number"),
    EMAIL1("email1", "Email 1"),
    EMAIL2("email2", "Email 2"),
    AMRSYSTEMNAME("amrSystemName", "AMR system name"),
    INSTALLEDDATE("installedDate", "Installed date"),
    REMOVEDDATE("removedDate", "Removed date"),
    RETIREDDATE("retiredDate", "Retired date"),

    SERVICE_CATEGORY_DISPLAY("displayServiceCategory", "Service category"),
    CONNECTION_STATE_DISPLAY("displayConnectionState", "Connection state"),
    BILLING_READY_DISPLAY("displayAmiBillingReady", "AMI billing ready"),
    LOCATION("locationID", "Location"),

    UNMEASURED(UsagePointTypeInfo.UsagePointType.UNMEASURED.name(), UsagePointTypeInfo.UsagePointType.UNMEASURED.displayName),
    SMART_DUMB(UsagePointTypeInfo.UsagePointType.SMART_DUMB.name(), UsagePointTypeInfo.UsagePointType.SMART_DUMB.displayName),
    INFRASTRUCTURE(UsagePointTypeInfo.UsagePointType.INFRASTRUCTURE.name(), UsagePointTypeInfo.UsagePointType.INFRASTRUCTURE.displayName),
    N_A(UsagePointTypeInfo.UsagePointType.N_A.name(), UsagePointTypeInfo.UsagePointType.N_A.displayName)
;

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

    public String getDisplayName(Thesaurus thesaurus) {
        return thesaurus.getString(this.key, this.defaultFormat);
    }
}