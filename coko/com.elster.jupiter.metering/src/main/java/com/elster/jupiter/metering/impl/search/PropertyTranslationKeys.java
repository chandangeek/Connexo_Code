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

    USAGEPOINT_MRID("usagepoint.mRID", "mRID"),
    USAGEPOINT_SERVICECATEGORY("usagepoint.serviceKind", "Service Category"),
    USAGEPOINT_NAME("usagepoint.name", "Name"),
    USAGEPOINT_ALIASNAME("usagepoint.aliasName", "Alias Name"),
    USAGEPOINT_SERVICELOCATION("usagepoint.serviceLocation", "Service Location"),
    USAGEPOINT_CREATETIME("usagepoint.createTime", "Create Time"),
    USAGEPOINT_MODTIME("usagepoint.modifyTime", "Modify Time"),
    USAGEPOINT_ISSDP("usagepoint.isSdp", "SDP"),
    USAGEPOINT_ISVIRTUAL("usagepoint.isVirtual", "Virtual"),
    USAGEPOINT_OUTAGEREGION("usagepoint.outageRegion", "Outage Region"),
    USAGEPOINT_READCYCLE("usagepoint.readCycle", "Read Cycle"),
    USAGEPOINT_READROUTE("usagepoint.readRoute", "Read Route"),
    USAGEPOINT_USERNAME("usagepoint.userName", "User Name"),
    USAGEPOINT_VERSION("usagepoint.versionCount", "Version"),
    USAGEPOINT_SERVICEPRIORITY("usagepoint.servicePriority", "Service Priority"),

    USAGEPOINT_CONNECTIONSTATE("usagepoint.connectionState", "Connection State"),
    USAGEPOINT_STARTTIME("usagepoint.startTime", "Start Time"),
    USAGEPOINT_ENDTIME("usagepoint.endTime", "End Time"),

    USAGEPOINT_GROUNDED("usagepoint.grounded", "Grounded"),
    USAGEPOINT_PHASECODE("usagepoint.phaseCode", "Phase Code"),
    USAGEPOINT_NOMINALVOLTAGE("usagepoint.nominalVoltage", "Nominal Voltage (in volts)"),
    USAGEPOINT_RATEDCURRENT("usagepoint.ratedCurrent", "Rated Current (in amperes)"),
    USAGEPOINT_RATEDPOWER("usagepoint.ratedPower", "Rated Power (in watts)"),
    USAGEPOINT_ESTIMATEDLOAD("usagepoint.estimatedLoad", "Estimated Load (in watts)"),

    USAGEPOINT_GROUP_ELECTRICITY("usagepoint.group.electricity", "Electricity"),
    USAGEPOINT_GROUP_GAS("usagepoint.group.gas", "Gas"),
    USAGEPOINT_GROUP_WATER("usagepoint.group.water", "Water");

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