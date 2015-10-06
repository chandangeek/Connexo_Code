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
    NO_SUCH_COM_TASK_EXECUTION(27, "NoSuchComTaskExecution", "The device has no communication task with that id");

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
