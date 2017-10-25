/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.protocol.api.ConnectionType;

public enum DefaultTranslationKey implements TranslationKey {
    PRE_TRANSITION_CHECKS_FAILED("PreTransitionChecksFailed" , "Pretransition checks failed"),
    CIM_DATE_RECEIVE("DeviceCimReceivedDate" , "Shipment date"),
    CIM_DATE_INSTALLED("DeviceCimInstalledDate" , "Installation date"),
    CIM_DATE_REMOVE("DeviceCimRemovedDate" , "Deactivation date"),
    CIM_DATE_RETRIED("DeviceCimRetriedDate" , "Decommissioning date"),
    LAST_CHECKED_PROPERTY_NAME(DeviceLifeCycleService.MicroActionPropertyName.LAST_CHECKED.key() , "Start validation date"),
    SET_MULTIPLIER_PROPERTY_NAME(DeviceLifeCycleService.MicroActionPropertyName.MULTIPLIER.key() , "Multiplier"),
    CONNECTION_TASK_STATUS_INCOMPLETE(Keys.CONNECTION_TASK_STATUS_INCOMPLETE, "Incomplete"),
    CONNECTION_TASK_STATUS_ACTIVE(Keys.CONNECTION_TASK_STATUS_ACTIVE, "Active"),
    CONNECTION_TASK_STATUS_INACTIVE(Keys.CONNECTION_TASK_STATUS_INACTIVE, "Inactive"),
    DEFAULT("Default", "Default"),
    NONE("None", "None"),
    CONNECTION_FUNCTION("Connection.function", "''{0}'' function"),
    DEFAULT_NOT_DEFINED("DefaultNotDefined", "Default (not defined yet)"),
    INDIVIDUAL("Individual", "Individual"),
    FAILURE("Failure", "Failed"),
    INBOUND(ConnectionType.ConnectionTypeDirection.INBOUND.name(), "Inbound"),
    OUTBOUND(ConnectionType.ConnectionTypeDirection.OUTBOUND.name(), "Outbound"),
    COMPLETE("Complete", "Complete"),
    INCOMPLETE("Incomplete", "Incomplete"),
    ON_REQUEST("onRequest", "On request"),
    SHARED_SCHEDULE("masterSchedule", "Shared schedule"),
    INDIVIDUAL_SCHEDULE("individualSchedule", "Individual schedule"),
    NOT_DEFINED_YET("NotDefinedYet", "(not defined yet)"),
    AS_SOON_AS_POSSIBLE("asSoonAsPossible", "As soon as possible"),
    MINIMIZE_CONNECTIONS("minimizeConnections", "Minimize connections"),
    MDC_LABEL_CATEGORY_FAVORITES("mdc.label.category.favorites", "Favorites"),
    NO_RESTRICTIONS("NoRestrictions", "No restrictions"),
    ;

    private String key;
    private String defaultFormat;

    DefaultTranslationKey(String key, String defaultFormat) {
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

    public String translateWith(Thesaurus thesaurus){
        return thesaurus.getFormat(this).format();
    }

    public static class Keys {
        public static final String CONNECTION_TASK_STATUS_INCOMPLETE = "connectionTaskStatusIncomplete";
        public static final String CONNECTION_TASK_STATUS_ACTIVE = "connectionTaskStatusActive";
        public static final String CONNECTION_TASK_STATUS_INACTIVE = "connectionTaskStatusInActive";
    }
}