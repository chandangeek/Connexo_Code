/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.config.security.Privileges;
import com.energyict.mdc.upl.messages.ProtocolSupportedCalendarOptions;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {
    FIELD_IS_REQUIRED(1, "RequiredField", "This field is required"),
    PROTOCOL_INVALID_NAME(2, "deviceType.no.such.protocol", "A protocol with name ''{0}'' does not exist"),
    NO_LOGBOOK_TYPE_ID_FOR_ADDING(3, "NoLogBookTypeIdForAdding", "User should specify ids of LogBook Type for adding"),
    NO_LOGBOOK_TYPE_FOUND(4, "NoLogBookTypeFound", "No LogBook type with id {0}"),
    NO_LOGBOOK_SPEC_FOUND(5, "NoLogBookSpecFound", "No LogBook configuration with id {0}"),
    NO_LOAD_PROFILE_TYPE_ID_FOR_ADDING(6, "NoLoadProfileTypeIdForAdding", "User should specify ids of Load Profile Type for adding"),
    NO_LOAD_PROFILE_TYPE_FOUND(7, "NoLoadProfileTypeFound", "No Load Profile type with id {0}"),
    NO_CHANNEL_SPEC_FOUND(9, "NoChannelSpecFound", "No channel specification with id {0}"),
    INVALID_REFERENCE_TO_REGISTER_TYPE(10, "NoSuchRegisterType", "Register type could not be found"),
    DUPLICATE_OBISCODE(11, "DuplicateObisCode", "A register mapping with obis code ''{0}'', unit ''{1}'' and time of use ''{2}'' already exists"),
    NO_SUCH_DEVICE(14, "noSuchDevice", "No device with id {0}"),
    DEVICE_DOES_NOT_MATCH_CONFIG(15, "deviceDoesNotMatchConfig", "Device does not match device configuration"),
    NO_SUCH_CONNECTION_TASK(16, "NoSuchConnectionTask", "No such connection task"),
    NO_DEVICECONFIG_ID_FOR_ADDING(17, "NoDeviceConfigurationIdForAdding", "User should specify ids of Device Configuration for adding"),
    CONNECTION_TYPE_UNKNOWN(18, "NoSuchConnectionType", "No connection type pluggable class could be found for ''{0}''"),
    NO_VALIDATIONRULESET_ID_FOR_ADDING(20, "NoValidationRuleSetIdForAdding", "User should specify ids of Validation Ruleset for adding"),
    EXECUTE_COM_TASK_LEVEL1(21, Privileges.Constants.EXECUTE_COM_TASK_1, "Execute level 1"),
    EXECUTE_COM_TASK_LEVEL2(22, Privileges.Constants.EXECUTE_COM_TASK_2, "Execute level 2"),
    EXECUTE_COM_TASK_LEVEL3(23, Privileges.Constants.EXECUTE_COM_TASK_3, "Execute level 3"),
    EXECUTE_COM_TASK_LEVEL4(24, Privileges.Constants.EXECUTE_COM_TASK_4, "Execute level 4"),
    UNKNOWN_PRIVILEGE_ID(33, "NoSuchExecutionLevels", "No such execution levels: {0}"),
    NO_SUCH_DEVICE_MESSAGE_SPEC(34, "NoSuchDeviceMessageSpec", "No such device message spec: {0}"),
    FILE_IO(35, Keys.FILE_IO, "Failure while doing IO on file"),
    NO_KEY_TYPE_FOUND(36, "noKeyTypeFound", "No key type with id {0}"),
    NO_KEY_TYPE_FOUND_NAME(37, "noKeyTypeFoundName", "No key type with name {0}"),
    NO_TRUST_STORE_FOUND(38, "noTrustStoreFound", "No trust store with id {0}"),
    NO_SUCH_DEVICE_LIFE_CYCLE(206, "NoSuchDeviceLifeCycle", "There is no device life cycle with id = {0}"),
    UNABLE_TO_CHANGE_DEVICE_LIFE_CYCLE(207, "UnableToChangeDeviceLifeCycle", "Unable to change device life cycle to \"{0}\""),
    CONCURRENT_FAIL_ACTIVATE_TITLE(210, "ConcurrentFailActivateTitle", "Failed to activate ''{0}''"),
    CONCURRENT_FAIL_DEACTIVATE_TITLE(211, "ConcurrentFailDeActivateTitle", "Failed to deactivate ''{0}''"),
    CONCURRENT_FAIL_ACTIVATE_BODY(212, "ConcurrentFailActivateBody", "{0} has changed since the page was last updated."),
    CONCURRENT_FAIL_DEACTIVATE_BODY(213, "ConcurrentFailDeActivateBody", "{0} has changed since the page was last updated."),
    CALENDAR_OPTION_SEND(214, Keys.CALENDAR_OPTION_SEND, "Send activity calendar"),
    CALENDAR_OPTION_SEND_WITH_DATE(215, Keys.CALENDAR_OPTION_SEND_WITH_DATE, "Send activity calendar with activation date"),
    CALENDAR_OPTION_SEND_WITH_DATE_TYPE(216, Keys.CALENDAR_OPTION_SEND_WITH_DATE_TYPE, "Send activity calendar with activation date and type"),
    CALENDAR_OPTION_SEND_WITH_DATE_CONTRACT(217, Keys.CALENDAR_OPTION_SEND_WITH_DATE_CONTRACT, "Send activity calendar with activation date and contract"),
    CALENDAR_OPTION_SEND_WITH_DATETIME(218, Keys.CALENDAR_OPTION_SEND_WITH_DATETIME, "Send activity calendar with activation date and time"),
    CALENDAR_OPTION_SEND_SPECIAL_DAYS(219, Keys.CALENDAR_OPTION_SEND_SPECIAL_DAYS, "Send special days calendar"),
    CALENDAR_OPTION_SEND_SPECIAL_DAYS_TYPE(220, Keys.CALENDAR_OPTION_SEND_SPECIAL_DAYS_TYPE, "Send special days calendar with type"),
    CALENDAR_OPTION_SEND_SPECIAL_DAYS_CONTRACT_DATE(221, Keys.CALENDAR_OPTION_SEND_SPECIAL_DAYS_CONTRACT_DATE, "Send special days calendar with contract and activation date"),
    CALENDAR_OPTION_CLEAR_DISABLE_TARIFF(222, Keys.CALENDAR_OPTION_CLEAR_DISABLE_TARIFF, "Clear and disable passive tariff"),
    CALENDAR_OPTION_ACTIVATE_PASSIVE(223, Keys.CALENDAR_OPTION_ACTIVATE_PASSIVE, "Activate passive calendar"),
    DATALOGGER_SLAVES_AT_LEAST_ONE_DATASOURCE(224, Keys.DATALOGGER_SLAVES_AT_LEAST_ONE_DATASOURCE, "You must define at least one channel configuration or register configuration before you can " +
            "activate a datalogger slave device configuration"),
    DATALOGGER_ENABLEMENTS_AT_LEAST_ONE_DATASOURCE(225, Keys.DATALOGGER_ENABLEMENTS_AT_LEAST_ONE_DATASOURCE, "You must define at least one channel configuration or register configuration before " +
            "you can activate a datalogger device configuration"),
    MULTI_ELEMENT_SLAVE_AT_LEAST_ONE_DATASOURCE(226, Keys.MULTI_ELEMENT_SLAVE_AT_LEAST_ONE_DATASOURCE, "You must define at least one channel configuration or register configuration before you can " +
            "activate a multi-element slave configuration"),
    MULTI_ELEMENT_ENABLEMENTS_AT_LEAST_ONE_DATASOURCE(227, Keys.MULTI_ELEMENT_ENABLEMENTS_AT_LEAST_ONE_DATASOURCE, "You must define at least one channel configuration or register configuration before " +
            "you can activate a multi-element device configuration"),
    NO_SUCH_KEY_ACCESSOR_TYPE(228, "NoSuchKeyAccessorType", "No such security accessor"),
    INVALID_VALUE(229, "InvalidValue", "Invalid value"),
    INVALID_TIME_DURATION(230, "TimeDurationTooLong", "Validity period must be shorter than or equal to 30 years."),
    DEVICE_TYPE_IN_USE_BY_CREATION_RULE(231, "DeviceTypeInUseByCreationRule", "Cannot change life cycle ''{1}'' for device type ''{0}'', this is in use by alarm creation rule ''{2}''"),
    NO_SUCH_CUSTOMPROPERTYSET(232, "noSuchCPS", "No custom property set with id {0}."),

    ;

    private final int number;
    private final String key;
    private final String format;

    MessageSeeds(int number, String key, String format) {
        this.number = number;
        this.key = key;
        this.format = format;
    }

    @Override
    public String getModule() {
        return DeviceConfigurationApplication.COMPONENT_NAME;
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return format;
    }

    @Override
    public Level getLevel() {
        return Level.SEVERE;
    }

    private static class Keys {
        public static final String FILE_IO = "file.IO";
        public static final String CALENDAR_OPTION_SEND = ProtocolSupportedCalendarOptions.SEND_ACTIVITY_CALENDAR.getId();
        public static final String CALENDAR_OPTION_SEND_WITH_DATE = ProtocolSupportedCalendarOptions.SEND_ACTIVITY_CALENDAR_WITH_DATE.getId();
        public static final String CALENDAR_OPTION_SEND_WITH_DATE_TYPE = ProtocolSupportedCalendarOptions.SEND_ACTIVITY_CALENDAR_WITH_DATE_AND_TYPE.getId();
        public static final String CALENDAR_OPTION_SEND_WITH_DATE_CONTRACT = ProtocolSupportedCalendarOptions.SEND_ACTIVITY_CALENDAR_WITH_DATE_AND_CONTRACT.getId();
        public static final String CALENDAR_OPTION_SEND_WITH_DATETIME = ProtocolSupportedCalendarOptions.SEND_ACTIVITY_CALENDAR_WITH_DATETIME.getId();
        public static final String CALENDAR_OPTION_SEND_SPECIAL_DAYS = ProtocolSupportedCalendarOptions.SEND_SPECIAL_DAYS_CALENDAR.getId();
        public static final String CALENDAR_OPTION_SEND_SPECIAL_DAYS_TYPE = ProtocolSupportedCalendarOptions.SEND_SPECIAL_DAYS_CALENDAR_WITH_TYPE.getId();
        public static final String CALENDAR_OPTION_SEND_SPECIAL_DAYS_CONTRACT_DATE = ProtocolSupportedCalendarOptions.SEND_SPECIAL_DAYS_CALENDAR_WITH_CONTRACT_AND_DATE.getId();
        public static final String CALENDAR_OPTION_CLEAR_DISABLE_TARIFF = ProtocolSupportedCalendarOptions.CLEAR_AND_DISABLE_PASSIVE_TARIFF.getId();
        public static final String CALENDAR_OPTION_ACTIVATE_PASSIVE = ProtocolSupportedCalendarOptions.ACTIVATE_PASSIVE_CALENDAR.getId();
        public static final String DATALOGGER_SLAVES_AT_LEAST_ONE_DATASOURCE = "datalogger.slave.at.least.one.datasource";
        public static final String DATALOGGER_ENABLEMENTS_AT_LEAST_ONE_DATASOURCE = "datalogger.enablements.at.least.one.datasource";
        public static final String MULTI_ELEMENT_SLAVE_AT_LEAST_ONE_DATASOURCE = "multi.element.submeter.at.least.one.datasource";
        public static final String MULTI_ELEMENT_ENABLEMENTS_AT_LEAST_ONE_DATASOURCE = "multi.element.enablements.at.least.one.datasource";
    }
}