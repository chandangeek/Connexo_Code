package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {
    NO_SUCH_DEVICE(14, "noSuchDevice", "No device with MRID {0}"),
    DEVICE_DOES_NOT_MATCH_CONFIG(15, "deviceDoesNotMatchConfig", "Device does not match device configuration"),
    NO_SUCH_PARTIAL_CONNECTION_TASK(16, "NoSuchPartialConnectionTask", "No such connection method on device config"),
    NO_SUCH_CONNECTION_METHOD(17, "NoSuchConnectionTask" , "Device {0} has no connection method {1}"),
    NO_SUCH_REGISTER(18, "NoSuchRegister" , "No register with id {0}"),
    DEVICE_VALIDATION_BULK_MSG(20, "DeviceValidationBulkMessage" , "This bulk operation for {0} schedule on {1} device is invalid"),
    NO_SUCH_READING(21, "NoSuchReading" , "Register {0} has no reading with id {1}"),
    INVALID_DATE(22, "InvalidDate", "Date should be less or equal to {0}"),
    NO_SUCH_LOAD_PROFILE_ON_DEVICE(23, "NoSuchLoadProfile", "Device {0} has no load profile {1}"),
    NO_SUCH_CHANNEL_ON_LOAD_PROFILE(30, "NoSuchChannel", "Load profile {0} has no channel {1}"),
    NO_CHANNELS_ON_REGISTER(72, "NoChannelsOnRegister", "Register {0} has no channels"),
    NO_SUCH_READING_ON_REGISTER(73, "NoSuchReadingOnRegister", "Register {0} has no reading with timestamp {1}"),
    NO_SUCH_LOG_BOOK_ON_DEVICE(24, "NoSuchLogBook", "Device {0} has no log book {1}"),
    CONNECTION_TYPE_STRATEGY_NOT_APPLICABLE(25, "connectionTypeStrategy.notApplicable", "Not applicable"),
    UPDATE_URGENCY_NOT_ALLOWED(26,"urgencyUpdateNotAllowed" ,"Urgency update not allowed"),
    UPDATE_DIALECT_PROPERTIES_NOT_ALLOWED(27,"updateDialectPropertiesNotAllowed" ,"Protocol dialect update not allowed"),
    UPDATE_CONNECTION_METHOD_NOT_ALLOWED(28,"updateConnectionMethodNotAllowed" ,"Connection method update not allowed"),
    RUN_COMTASK__NOT_ALLOWED(29,"runComTaskNotAllowed" ,"Running of this communication task is not allowed"),
    NULL_DATE(61, "NullDate", "Date must be filled in"),
    DEACTIVATE_VALIDATION_RULE_SET_NOT_POSSIBLE(62, "DeactivateValidationRuleSetNotPossible", "Deactivate of validation rule set {0} is currently not possible."),
    NO_SUCH_COM_SESSION_ON_CONNECTION_METHOD(88,"noSuchComSession" ,"No such communication session exists for this connection method"),
    NO_SUCH_COM_TASK(91, "NoSucComTaskOnDevice", "No such communication task exists for device ''{0}''"),
    COM_TASK_IS_NOT_ENABLED_FOR_THIS_DEVICE(92, "NoEnablementForDevice", "Communication task ''{0}'' is not enabled for device ''{1}''"),
    NO_SUCH_COM_TASK_EXEC_SESSION(93, "NoSuchComTaskExecSession", "The communication task logging could not be found"),
    DEVICEGROUPNAME_ALREADY_EXISTS(94, "deviceGroupNameAlreadyExists", "A devicegroup with name {0} already exists"),
    INCOMPLETE(96, "Incomplete", "Incomplete"),
    NO_SUCH_SECURITY_PROPERTY_SET_ON_DEVICE(97, "NoSuchSecurityPropertySetOnDevice", "No security settings with id {0} exist for device ''{1}''"),
    NO_SUCH_SECURITY_PROPERTY_SET(98, "NoSuchSecurityPropertySet", "No security settings with id {0} exist"),
    NO_SUCH_USER(211, "NoSuchUser", "No such user"),
    NO_SUCH_MESSAGE_SPEC(212, "NoSuchMessageSpec", "No such device message specification"),
    NO_SUCH_MESSAGE(213, "NoSuchMessage", "No such device message exists on the device" ),
    UPDATE_SECURITY_PROPERTY_SET_NOT_ALLOWED(217, "UpdateSecurityPropertySetNotAllowed", "Update security property set not allowed"),
    NO_SUCH_LABEL_CATEGORY(219, "NoSuchLabelCategory", "No such label category"),
    NO_SUCH_DEVICE_LABEL(220, "NoSuchDeviceLabel", "No such device label of category ''{0}'' on a device {1}"),
    NO_SUCH_CHANNEL_ON_DEVICE(221, "NoSuchChannelOnDevice", "Device {0} has no channel {1}"),
    RUN_CONNECTIONTASK_IMPOSSIBLE(224,"runConTaskImpossible", "Running of this connection task is impossible"),
    NO_SUCH_COMMUNICATION(225, "NoSuchComTaskExecution", "No such communication with id ''{0}'' on device ''{1}''"),
    NO_SUCH_KPI(226, "NoSuchKpi", "No data collection with id ''{0}'' could be found"),
    NO_SUCH_DEVICE_GROUP(227, "NoSuchDeviceGroup", "No end device group with id ''{0}''"),
    NO_UPDATE_ALLOWED(228, "NoUpdateAllowed", "You are not allowed to change this field"),
    IMPOSSIBLE_TO_SET_MASTER_DEVICE(229, "ImpossibleToSetMasterDevice", "Device {0} is directly addressable. It is not possible to set master device"),
    FIELD_CAN_NOT_BE_EMPTY(230, Keys.FIELD_CAN_NOT_BE_EMPTY, "Field can not be empty"),
    NO_SUCH_COM_TASK_EXEC(231, "NoSuchComTaskExec" , "No such communication task execution exists"),
    CAN_NOT_PERFORM_ACTION_ON_SYSTEM_COMTASK(232, "CanNotPerformActionOnSystemComTask" , "You can not perform an action on a system communication task"),
    ESTIMATOR_REQUIRED(233, "EstimatorRequired" , "Please select an estimator"),
    ESTIMATOR_NOT_FOUND(234, "EstimatorNotValid" , "Provided estimator is not valid"),
    METER_ACTIVATION_NOT_FOUND(235, "MeterActivationNotFound" , "No meter activation is found"),
    NO_SUCH_DEVICE_LIFE_CYCLE_ACTION(236, "NoSuchDeviceLifeCycleAction" , "No device life cycle action with id = {0}"),
    THIS_FIELD_IS_REQUIRED(237, "ThisFieldIsRequired" , "This field is required"),
    CIM_DATE_SHOULD_BE_AFTER_X(238, "CIMDateShouldBeAfterX" , "This date should be greater than (or equal) ''{0}''"),
    NO_APPSERVER(239, "NoAppServer", "There is no active application server that can handle this request"),
    NO_SUCH_MESSAGE_QUEUE(240, "NoSuchMessageQueue", "Unable to queue command: no message queue was found"),
    BAD_ACTION(241, "BadAction", "Expected action to be either 'add' or 'remove'"),
    NO_SUCH_COM_SCHEDULE(242, "NoSuchSchedule", "No communication schedule with id {0}"),
    CANT_ADD_READINGS_FOR_STATE(243, "cantAddReadingForState", "The state of the device at {0,date,long} {0,time,long} does not allow adding readings."),
    INVALID_ESTIMATOR_PROPERTY_VALUE(244, "invalidEstimatorPropertyValue", "Invalid property value");
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
        return DeviceApplication.COMPONENT_NAME;
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

    public static class Keys {
        public static final String FIELD_CAN_NOT_BE_EMPTY = "FieldCanNotBeEmpty";
    }

}