package com.elster.jupiter.metering.impl.search;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;

/**
 * Provides the translation keys for the search properties
 * that are supported by the metering bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-06-02 (15:05)
 */
public enum PropertyTranslationKeys implements TranslationKey {

    USAGEPOINT_MRID("usagepoint.mRID", "MRID"),
    USAGEPOINT_SERVICECATEGORY("usagepoint.serviceKind", "Service category"),
    USAGEPOINT_NAME("usagepoint.name", "Name"),
    USAGEPOINT_ALIASNAME("usagepoint.aliasName", "Alias name"),
    USAGEPOINT_SERVICELOCATION("usagepoint.serviceLocation", "Service location"),
    USAGEPOINT_CREATETIME("usagepoint.createTime", "Create time"),
    USAGEPOINT_MODTIME("usagepoint.modifyTime", "Modify time"),
    USAGEPOINT_ISSDP("usagepoint.isSdp", "SDP"),
    USAGEPOINT_ISVIRTUAL("usagepoint.isVirtual", "Virtual"),
    USAGEPOINT_OUTAGEREGION("usagepoint.outageRegion", "Outage region"),
    USAGEPOINT_READCYCLE("usagepoint.readCycle", "Read cycle"),
    USAGEPOINT_READROUTE("usagepoint.readRoute", "Read route"),
    USAGEPOINT_USERNAME("usagepoint.userName", "User name"),
    USAGEPOINT_VERSION("usagepoint.versionCount", "Version"),
    USAGEPOINT_SERVICEPRIORITY("usagepoint.servicePriority", "Service priority"),

    USAGEPOINT_CONNECTIONSTATE("usagepoint.connectionState", "Connection state"),
    USAGEPOINT_STARTTIME("usagepoint.startTime", "Start time"),
    USAGEPOINT_ENDTIME("usagepoint.endTime", "End time"),

    USAGEPOINT_GROUNDED("usagepoint.grounded", "Grounded"),
    USAGEPOINT_PHASECODE("usagepoint.phaseCode", "Phase code"),
    USAGEPOINT_NOMINALVOLTAGE("usagepoint.nominalVoltage", "Nominal voltage (in volts)"),
    USAGEPOINT_RATEDCURRENT("usagepoint.ratedCurrent", "Rated current (in amperes)"),
    USAGEPOINT_RATEDPOWER("usagepoint.ratedPower", "Rated power (in watts)"),
    USAGEPOINT_ESTIMATEDLOAD("usagepoint.estimatedLoad", "Estimated load (in watts)"),

    USAGEPOINT_GROUP_ELECTRICITY("usagepoint.group.electricity", "Electricity"),
    USAGEPOINT_GROUP_GAS("usagepoint.group.gas", "Gas"),
    USAGEPOINT_GROUP_WATER("usagepoint.group.water", "Water"),

    USAGEPOINT_DOMAIN("usagepoint.domain", "Usage point"),

    ;

    private String key;
    private String defaultFormat;

    PropertyTranslationKeys(String key, String defaultFormat) {
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

    public String getDisplayName(Thesaurus thesaurus) {
        return thesaurus.getString(this.key, this.defaultFormat);
    }
}
