package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.config.security.Privileges;
import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {

    FIELD_IS_REQUIRED(1, "RequiredField", "Field is required"),
    PROTOCOL_INVALID_NAME(2,"deviceType.no.such.protocol", "A protocol with name ''{0}'' does not exist"),
    NO_LOGBOOK_TYPE_ID_FOR_ADDING(3,"NoLogBookTypeIdForAdding", "User should specify ids of LogBook Type for adding"),
    NO_LOGBOOK_TYPE_FOUND(4, "NoLogBookTypeFound", "No LogBook type with id {0}"),
    NO_LOGBOOK_SPEC_FOUND(5, "NoLogBookSpecFound", "No LogBook configuration with id {0}"),
    NO_LOAD_PROFILE_TYPE_ID_FOR_ADDING(6, "NoLoadProfileTypeIdForAdding", "User should specify ids of Load Profile Type for adding"),
    NO_LOAD_PROFILE_TYPE_FOUND(7, "NoLoadProfileTypeFound", "No Load Profile type with id {0}"),
    NO_PHENOMENON_FOUND(8, "NoPhenomenonFound", "No Phenomenon with id {0}"),
    NO_CHANNEL_SPEC_FOUND(9, "NoChannelSpecFound", "No channel specification with id {0}"),
    INVALID_REFERENCE_TO_REGISTER_TYPE(10, "NoSuchRegisterType", "Register type could not be found"),
    DUPLICATE_OBISCODE(11, "DuplicateObisCode", "A register mapping with obis code ''{0}'', phenomenon ''{1}'' and time of use ''{2}'' already exists"),
    AS_SOON_AS_POSSIBLE(12, "asSoonAsPossible", "As soon a possible"),
    MINIMIZE_CONNECTIONS(13, "minimizeConnections", "Minimize connections"),
    NO_SUCH_DEVICE(14, "noSuchDevice", "No device with id {0}"),
    DEVICE_DOES_NOT_MATCH_CONFIG(15, "deviceDoesNotMatchConfig", "Device does not match device configuration"),
    NO_SUCH_CONNECTION_TASK(16, "NoSuchConnectionTask", "No such connection task"),
    NO_DEVICECONFIG_ID_FOR_ADDING(17, "NoDeviceConfigurationIdForAdding", "User should specify ids of Device Configuration for adding"),
    CONNECTION_TYPE_UNKNOWN(18, "NoSuchConnectionType", "No connection type pluggable class could be found for ''{0}''"),
    NO_SUCH_PHENOMENON(19, "NoSuchPhenomenon", "No such phenomenon"),
    NO_VALIDATIONRULESET_ID_FOR_ADDING(20,"NoValidationRuleSetIdForAdding", "User should specify ids of Validation Ruleset for adding"),
    EXECUTE_COM_TASK_LEVEL1(21, Privileges.EXECUTE_COM_TASK_1, "Execute com task (level 1)"),
    EXECUTE_COM_TASK_LEVEL2(22, Privileges.EXECUTE_COM_TASK_2, "Execute com task (level 2)"),
    EXECUTE_COM_TASK_LEVEL3(23, Privileges.EXECUTE_COM_TASK_3, "Execute com task (level 3)"),
    EXECUTE_COM_TASK_LEVEL4(24, Privileges.EXECUTE_COM_TASK_4, "Execute com task (level 4)"),
    EDIT_DEVICE_SECURITY_PROPERTIES_1(25, Privileges.EDIT_DEVICE_SECURITY_PROPERTIES_1, "Edit device security properties (level 1)"),
    EDIT_DEVICE_SECURITY_PROPERTIES_2(26, Privileges.EDIT_DEVICE_SECURITY_PROPERTIES_2, "Edit device security properties (level 2)"),
    EDIT_DEVICE_SECURITY_PROPERTIES_3(27, Privileges.EDIT_DEVICE_SECURITY_PROPERTIES_3, "Edit device security properties (level 3)"),
    EDIT_DEVICE_SECURITY_PROPERTIES_4(28, Privileges.EDIT_DEVICE_SECURITY_PROPERTIES_4, "Edit device security properties (level 4)"),
    VIEW_DEVICE_SECURITY_PROPERTIES_1(29, Privileges.VIEW_DEVICE_SECURITY_PROPERTIES_1, "View device security properties (level 1)"),
    VIEW_DEVICE_SECURITY_PROPERTIES_2(30, Privileges.VIEW_DEVICE_SECURITY_PROPERTIES_2, "View device security properties (level 2)"),
    VIEW_DEVICE_SECURITY_PROPERTIES_3(31, Privileges.VIEW_DEVICE_SECURITY_PROPERTIES_3, "View device security properties (level 3)"),
    VIEW_DEVICE_SECURITY_PROPERTIES_4(32, Privileges.VIEW_DEVICE_SECURITY_PROPERTIES_4, "View device security properties (level 4)"),
    UNKNOWN_PRIVILEGE_ID(33, "NoSuchExecutionLevels", "No such execution levels: {0}"),
    EXECUTE_DEVICE_MESSAGE_1(34, Privileges.EXECUTE_DEVICE_MESSAGE_1, "Execute device message (level1)"),
    EXECUTE_DEVICE_MESSAGE_2(35, Privileges.EXECUTE_DEVICE_MESSAGE_2, "Execute device message (level2)"),
    EXECUTE_DEVICE_MESSAGE_3(36, Privileges.EXECUTE_DEVICE_MESSAGE_3, "Execute device message (level3)"),
    EXECUTE_DEVICE_MESSAGE_4(37, Privileges.EXECUTE_DEVICE_MESSAGE_4, "Execute device message (level4)"),
    ;

    private final int number;
    private final String key;
    private final String format;

    private MessageSeeds(int number, String key, String format) {
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

}
