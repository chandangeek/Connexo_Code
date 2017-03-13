/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.impl.utils;

import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.multisense.api.impl.PublicRestApplication;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {

    IMPOSSIBLE_TO_SET_MASTER_DEVICE(1, "ImpossibleToSetMasterDevice", "Device {0} is directly addressable. It is not possible to set master device"),
    NO_SUCH_DEVICE_LIFE_CYCLE_ACTION(2, "NoSuchDeviceLifeCycleAction" , "No device life cycle action with id = {0}"),
    THIS_FIELD_IS_REQUIRED(3, "ThisFieldIsRequired" , "This field is required"),
    CAN_NOT_HANDLE_ACTION(4, "CanNotHandleAction", "The requested device life cycle action action can not be handled"),
    NOT_FOUND(5, "NotFound", "The resource could not be found"),
    NO_SUCH_PARTIAL_CONNECTION_TASK(6, "NoSuchPartialConnectionTask" , "The device configuration does not contain a connection method with that id"),
    MISSING_CONNECTION_TASK_TYPE(7, "MissingConnectionTaskType", "The type of the connection task is missing"),
    NO_SUCH_DEVICE_TYPE(8, "NoSuchDeviceType" , "Device type does not exist"),
    NO_SUCH_DEVICE_CONFIG(9, "NoSuchDeviceConfig" , "Device type does not contain a device configuration with that id"),
    MISSING_PARTIAL_CONNECTION_METHOD(10, "NoPartialConnectionTask" , "The connection method on the device is missing" ),
    EXPECTED_PARTIAL_INBOUND(11, "ExpectedPartialInbound", "Expected connection method on device configuration to be 'Inbound'-type"),
    EXPECTED_PARTIAL_OUTBOUND(12, "ExpectedPartialOutbound", "Expected connection method on device configuration to be 'Outbound'-type"),
    NO_SUCH_CONNECTION_TASK(13, "NoSuchConnectionTask" , "The device does not contain a connection method with that id"),
    EXPECTED_INBOUND(14, "ExpectedInbound", "Expected connection method on device to be 'Inbound'-type"),
    EXPECTED_OUTBOUND(15, "ExpectedOutbound", "Expected connection method on device to be 'Outbound'-type"),
    NO_SUCH_COM_TASK(16, "NoSuchComTask", "Communication task does not exist"),
    NO_SUCH_SECURITY_PROPERTY_SET(17, "NoSuchSecurityPropertySet" , "Security property set does not exist"),
    NO_SUCH_DEVICE(18, "NoSuchDevice", "Device does not exist"),
    CONTENT_EXPECTED(19, "ContentExpected", "This method expected content, but the body was empty"),
    NO_SUCH_MESSAGE_CATEGORY(20, "NoSuchMessageCategory", "Message category does not exist"),
    NO_SUCH_GATEWAY(21, "NuSuchGateway", "Gateway device does not exist"),
    NO_SUCH_DEVICE_PROTOCOL(22, "NoSuchDeviceProtocol", "Device protocol does not exist"),
    NO_SUCH_AUTH_DEVICE_ACCESS_LEVEL(23, "NoSuchAuthDevAccessLevel" , "The device protocol does not have an authentication access level with that id"),
    NO_SUCH_ENC_DEVICE_ACCESS_LEVEL(24, "NoSuchEncDevAccessLevel" , "The device protocol does not have an encryption access level with that id"),
    CONFLICT_ON_DEVICE(25, "ConflictOnDevice", "The device you attempted to edit was changed by someone els"),
    NO_SUCH_PROTOCOL_TASK(26, "NoSuchProtocolTask", "Protocol task does not exist"),
    NO_SUCH_COM_TASK_EXECUTION(27, "NoSuchComTaskExecution", "The device has no communication task with that id"),
    TYPE_DOES_NOT_SUPPORT_COM_TASK(28, "ComTaskNotSupporter", "Scheduled communications tasks can not be configured with communication task, only accept schedules"),
    COM_TASK_NOT_ENABLED(29, "ComTaskNotEnabled" , "The communication task has not been enabled on the configuration"),
    COM_TASK_EXPECTED(30, "ComTaskExpected", "Communication task was expected in the request"),
    SCHEDULE_SPEC_EXPECTED(31, "ScheduleSpecExpected", "A manually scheduled communication task requires a scheduling specification. Did you want to make an Ad-hoc communication task?"),
    TYPE_DOES_NOT_SUPPORT_SCHEDULE_SPEC(32, "SchedulingSpecNotSupported", "This communication task execution does does not support a scheduling specification."),
    SCHEDULE_EXPECTED(33, "ScheduleExpected", "Expected communication schedule on a scheduled communication task "),
    NO_SUCH_COM_SCHEDULE(34, "NoSuchComSchedule", "Communication schedule does not exist"),
    NO_SUCH_PROTOCOL_DIALECT_PROPERTIES(35, "NoSuchProtocolDialectProperties", "Protocol dialect properties do not exist"),
    NO_SUCH_COM_TASK_ENABLEMENT(36, "NoSuchComtaskEnablement", "Communication task enablement does not exist"),
    NOT_POSSIBLE_TO_SUPPLY_BOTH_OR_NONE(37, "EitherDefaultorExplicit", "The communication task execution should either use default connection task or an explicitly set connection task"),
    NO_SUCH_DEVICE_MESSAGE(38, "NoSuchDeviceMessage", "The device has no known device message with that id"),
    UNKNOWN_STATUS(39, "StatusUnknown", "Unknown device contacter status"),
    NO_COMTASK_FOR_COMMAND(40, "NoComtaskForCommand", "A comtask to execute the device messages could not be located"),
    NO_SUCH_DEVICE_MESSAGE_SPEC(41, "NoSuchDeviceMessageSpec", "The device message category does not contain a message specification with that id"),
    NO_SUCH_DEVICE_MESSAGE_CATEGORY(42 ,"NoSuchDeviceMessageCategory" , "No such device message category"),
    NO_SUCH_DEVICE_MESSAGE_ENABLEMENT(43, "NoSuchMessageEnablement", "The device configuration does not contain a device message enablement with that id"),
    EXPECTED_MESSAGE_ID(44, "ExpectedDeviceMessageId", "Device message id was expected in the request"),
    EXPECTED_CONTACTOR_STATUS(45, "ExpectedContacterStatus", "Device contactor status was expected in the request"),
    VERSION_MISSING(46, "VersionMissing", "Version value was expected for the ''{0}''-field"),
    EXPECTED_METHOD_ID(47, "MethodIdExpected" , "Connection method id was expected"),
    NOT_NULL_VALIDATION(48, "javax.validation.constraints.NotNull.message", "Expected field to be not null"),
    EXPECTED_MESSAGE_SPEC_ID(49, "ExpectedDeviceMessageSpecId", "Device message specification id was expected in the request"),
    EXPECTED_RELEASE_DATE(50, "ReleaseDateExpected", "Release date was expected in the request"),
    EXPECTED_PROTOCOL_INFO(51, "ExpectedProtocolInfo", "Protocol information was expected in the request"),
    EXPECTED_COM_TASK(52, "ExpectedComTask", "Communication task was expected in the request"),
    NO_SUCH_USAGE_POINT(53, "NoSuchUsagePoint", "No such usage point"),
    FIELD_MISSING(54, "NoServiceKind", "This field is required"),
    NO_SUCH_SERVICE_CATEGORY(55, "NoSuchServiceCategory", "No such service category"),
    NO_SUCH_PROPERTY_SET(56, "NoSuchPropertySet", "No such custom property set or the custom property set is not available on the usage point"),
    UNSUPPORTED_TYPE(57, "TypeNotSupported", "The service category is not supported"),
    NO_SUCH_METROLOGY_CONFIGURATION(58, "NoSuchMetrologyConfig", "No such metrology configuration"),
    NO_SUCH_METER_ACTIVATION(59, "NoSuchMeterActivation", "No such meter activation on the usage point"),
    EMPTY_REQUEST(60, "NoData", "No data found in your request"),
    NO_SUCH_METER(61, "NoSuchMeter", "No such meter"),
    NO_SUCH_ALARM(62, "NoSuchAlarm", "No such alarm with id {0}"),
    ALARM_ALREADY_CLOSED(63, "AlarmAlreadyClosed", "Alarm with id {0} already closed"),
    ALARM_LOCK_ATTEMPT_FAILED(64, "AlarmLockAttemptFailed", "Could not obtain a lock on alarm with id {0}"),
    NO_SUCH_STATUS(65, "NoSuchStatus", "No such Status with key {0}"),
    BAD_FIELD_VALUE(80, "BadFieldValue", "Bad field value for {0}"),
    NO_SUCH_USER(81, "NoSuchUser", "No such user with name {0}")

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
        return PublicRestApplication.COMPONENT_NAME;
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
