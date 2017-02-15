/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
    USAGEPOINT_LOCATION("usagepoint.location", "Location"),
    USAGEPOINT_METROLOGYCONFIGURATION("usagepoint.metrologyConfiguration", "Metrology configuration"),
    USAGEPOINT_INSTALLATION_TIME("usagepoint.installationTime", "Installation time"),
    USAGEPOINT_TYPE("usagepoint.type", "Type"),
    USAGEPOINT_METER("usagepoint.meter", "Meter"),

    USAGEPOINT_CONNECTIONSTATE("usagepoint.connectionState", "Connection state"),
    USAGEPOINT_STARTTIME("usagepoint.startTime", "Start time"),
    USAGEPOINT_ENDTIME("usagepoint.endTime", "End time"),

    USAGEPOINT_GROUNDED("usagepoint.grounded", "Grounded"),
    USAGEPOINT_PHASECODE("usagepoint.phaseCode", "Phase code"),
    USAGEPOINT_NOMINALVOLTAGE("usagepoint.nominalVoltage", "Nominal voltage"),
    USAGEPOINT_RATEDCURRENT("usagepoint.ratedCurrent", "Rated current"),
    USAGEPOINT_RATEDPOWER("usagepoint.ratedPower", "Rated power"),
    USAGEPOINT_ESTIMATEDLOAD("usagepoint.estimatedLoad", "Estimated load"),

    USAGEPOINT_GROUP_ELECTRICITY("usagepoint.group.electricity", "Electricity"),
    USAGEPOINT_GROUP_GAS("usagepoint.group.gas", "Gas"),
    USAGEPOINT_GROUP_WATER("usagepoint.group.water", "Water"),
    USAGEPOINT_GROUP_HEAT("usagepoint.group.heat", "Heat"),

    USAGEPOINT_DOMAIN("usagepoint.domain", "Usage point"),

    USAGEPOINT_ID("usagepoint.id", "Id"),
    USAGEPOINT_ID_DESCRIPTION("usagepoint.id.description", "Usage point ID"),
    USAGE_POINT_REQUIREMENT_SEARCH_DOMAIN("usage.point.requirement.search.domain", "Usage point"),
    USAGEPOINT_LIMITER("usagepoint.limiter", "Limiter"),
    USAGEPOINT_LOAD_LIMITER_TYPE("usagepoint.load.limiter.type", "Load limiter type"),
    USAGEPOINT_LOADLIMIT("usagepoint.loadLimit", "Load limit"),
    USAGEPOINT_COLLAR("usagepoint.collar", "Collar"),
    USAGEPOINT_INTERRUPTABLE("usagepoint.interruptible", "Interruptible"),
    USAGEPOINT_PHYSICAL_CAPACITY("usagepoint.physicalCapacity", "Physical capacity"),
    USAGEPOINT_BYPASS("usagepoint.bypass", "Bypass"),
    USAGEPOINT_VALVE("usagepoint.valve", "Valve"),
    USAGEPOINT_CAPPED("usagepoint.capped", "Capped"),
    USAGEPOINT_CLAMPED("usagepoint.clamped", "Clamped"),
    USAGEPOINT_PRESSURE("usagepoint.pressure", "Pressure"),
    USAGEPOINT_BYPASS_STATUS("usagepoint.bypassStatus", "Bypass status"),
    USAGEPOINT_PURPOSE("usagepoint.purpose", "Purpose"),
    USAGEPOINT_STATE("usagepoint.state", "State")
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
        return thesaurus.getFormat(this).format();
    }

}