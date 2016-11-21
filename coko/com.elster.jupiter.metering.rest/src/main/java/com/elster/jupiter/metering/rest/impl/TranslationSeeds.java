package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.metering.UsagePointTypeInfo;
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
    INSTALLATION_TIME("installationTime", "Installation time"),
    REMOVEDDATE("removedDate", "Removed date"),
    RETIREDDATE("retiredDate", "Retired date"),

    CONNECTION_STATE_DISPLAY("displayConnectionState", "ConnectionState"),
    SERVICECATEGORY_DISPLAY("displayServiceCategory", "Service category"),
    METROLOGY_CONFIGURATION_DISPLAY("displayMetrologyConfiguration", "Metrology configuration"),
    BILLING_READY_DISPLAY("displayAmiBillingReady", "AMI billing ready"),
    LOCATION("location", "Location"),

    PHYSICAL_SDP(UsagePointTypeInfo.UsagePointType.PHYSICAL_SDP.name(), UsagePointTypeInfo.UsagePointType.PHYSICAL_SDP.displayName),
    PHYSICAL_NON_SDP(UsagePointTypeInfo.UsagePointType.PHYSICAL_NON_SDP.name(), UsagePointTypeInfo.UsagePointType.PHYSICAL_NON_SDP.displayName),
    VIRTUAL_NON_SDP(UsagePointTypeInfo.UsagePointType.VIRTUAL_NON_SDP.name(), UsagePointTypeInfo.UsagePointType.VIRTUAL_NON_SDP.displayName),
    VIRTUAL_SDP(UsagePointTypeInfo.UsagePointType.VIRTUAL_SDP.name(), UsagePointTypeInfo.UsagePointType.VIRTUAL_SDP.displayName)
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