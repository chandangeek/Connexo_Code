/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api;

import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

/**
 * Provides {@link MessageSeed}s ready to be used by {@link DeviceProtocol}
 * implementation classes to report {@link com.energyict.mdc.issues.Issue}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-02-10 (08:57)
 */
public enum MessageSeeds implements MessageSeed {

    LOADPROFILE_NOT_SUPPORTED(100, "issue.loadProfileXnotsupported", "Load profile with OBIS code '{0}' is not supported by the device"),
    LOADPROFILE_ISSUE(101, "issue.loadProfileXIssue", "Encountered an exception while reading loadprofile {0}"),
    LOGBOOK_NOT_SUPPORTED(102, "issue.logBookXnotsupported", "Logbook with OBIS code '{0}' is not supported by the device"),
    LOGBOOK_ISSUE(103, "issue.logBookXissue", "Encountered an exception while reading LogBook {0}: {1}"),
    UNSUPPORTED_CHANNEL_INFO(104, "issue.unsupportedChannelInfo", "The channel info ''{0}'' is not supported"),
    COULD_NOT_READOUT_LOGBOOK_DATA(105, "issue.couldNotReadoutLogBookData", "Could not correctly read the logbook data: {0}"),
    COULD_NOT_PARSE_LOGBOOK_DATA(106, "issue.couldNotParseLogBookData", "Could not correctly parse the logbook data: {0}"),
    COULD_NOT_READOUT_LOADPROFILE_DATA(107, "issue.couldNotReadoutLoadProfileData", "Could not correctly readout the load profile data, reason: {0}"),
    COULD_NOT_PARSE_LOADPROFILE_DATA(108, "issue.couldNotParseLoadProfileData", "Could not correctly parse the load profile data: {0}"),
    COULD_NOT_PARSE_REGISTER_DATA(109, "issue.couldNotParseRegisterData", "Could not correctly parse the register data"),
    COULD_NOT_PARSE_MESSAGE_DATA(110, "issue.couldNotParseMessageData", "Could not correctly parse the message data"),
    COULD_NOT_PARSE_TOPOLOGY_DATA(111, "issue.couldNotParseTopologyData", "Could not correctly parse the topology data"),
    REGISTER_NOT_SUPPORTED(112, "issue.registerXnotsupported", "Register with OBIS code '{0}' is not supported by the device"),
    REGISTER_ISSUE(113, "issue.registerXissue", "Encountered an exception while reading register {0}: {1}"),
    REGISTER_INCOMPATIBLE(114, "issue.registerXincompatible", "Register with OBIS code {0} is incompatible: {1}"),
    DEVICEPROTOCOL_LEGACY_ISSUE(115, "issue.deviceprotocol.legacy.issue", "An error occurred during the execution of a legacy protocol, see following stacktrace: {0}"),
    END_DEVICE_EVENT_TYPE_NOT_SUPPORTED(116, "issue.endDeviceEventTypeXnotsupported", "CIM end device event type {0} is currently not supported (yet) by the platform"),
    DEVICEMESSAGE_FAILED(117, "issue.DeviceMessage.failed", "Device message ({0}, {1} - {2})) failed: {3}"),
    DEVICEMESSAGE_NOT_SUPPORTED(118, "issue.DeviceMessage.notSupported", "Device message ({0}, {1} - {2})) is not supported by the protocol"),
    LOADPROFILE_CHANNEL_ISSUE(119, "issue.loadProfileXChannelYIssue", "Encountered an exception while reading load profile {0}: {1}"),
    DEVICETOPOLOGY_NOT_SUPPORTED(120, "issue.devicetopologynotsupported", "Device topology update not supported by the device"),
    OPERATION_NOT_SUPPORTED(121, "issue.operationNotSupported", "This operation is not supported by the device"),
    TOPOLOGY_MISMATCH(122, "issue.topologyMismatch", "The command is rejected by the device due to topology mismatch. The device's topology as configured in the system is probably wrong"),
    COMMAND_NOT_SUPPORTED(123, "issue.commandNotSupported", "The command is rejected by the device. It is probably unsupported by this device type"),
    PROTOCOL_DEPLOY_NOT_SUPPORTED(124, "issue.protocol.deploynotsupported", "Received deploy information for meter with serial number {0}, but this action is currently not supported (yet) by the platform"),
    PROTOCOL_DEVICE_NOT_FOUND(125, "issue.protocol.devicenotfound", "Received meter data for RTU with serial number {0}, but this RTU was not found uniquely in the database"),
    PROTOCOL_REGISTER_NOT_FOUND(126, "issue.protocol.registernotfound", "Received register data (with OBIS code = {0}) for device with serial number {1}, but this register is not defined on the device"),
    COULD_NOT_READ_FIRMWARE_VERSION(127, "issue.protocol.readingOfFirmwareFailed", "Could not correctly read the firmware version"),
    COULD_NOT_READ_CALENDAR_INFO(128, "issue.protocol.readingOfCalendarFailed", "Could not correctly read calendar information"),
    READ_CALENDAR_INFO_NOT_SUPPORTED(129, "issue.protocol.readingOfCalendarNotSupported", "Read calendar information not supported by meter firmware"),
    UNEXPECTED_PROTOCOL_ERROR(130, "issue.protocol.unexpected", "Unexpected error during protocol execution: {0}"),
    NUMBER_OF_RETRIES_REACHED_CONNECTION_STILL_INTACT(131, "issue.protocol.timeout.connection.still.intact", "Maximum number of retries ({1}) reached: {0}. The connection is still intact though."),
    CIPHERING_EXCEPTION(132, "issue.protocol.ciphering", "Encountered an exception related to the ciphering of data: {0}"),
    CONNECTION_DISCONNECT_ERROR(133, "connectionDisconnectError", "Disconnect of connection failed: {0}"),
    COULD_NOT_READ_BREAKER_STATE(134, "issue.protocol.readingOfBreakerStateFailed", "Could not correctly read the breaker state: {0}"),
    MISSING_PROPERTY(135, "protocol.required.property", "Required property \"{0}\" is missing"),
    DATA_ENCRYPTION_EXCEPTION(136, "data.encryption.exception", "Failure to decrypt encrypted data received from device"),
    DATA_ENCRYPTION_EXCEPTION_WITH_CAUSE(137, "data.encryption.with.cause", "Failure to decrypt encrypted data received from device: {0}"),
    INDEX_OUT_OF_BOUND_DATA_PARSE_EXCEPTION(138, "index.out.of.bound.parse.exception", "Referenced a non-existing index: {0}"),
    PROTOCOL_IO_PARSE_ERROR(139, "protocol.io.parse.error", "Protocol parse error: {0}"),
    GENERAL_PARSE_EXCEPTION(140, "general.parse.exception", "A general parsing error occurred: {0}"),
    INVALID_PROPERTY_VALUE(141, "invalid.property.value", "The property {0} has an invalid value {1}"),
//TODO complete this list with the entries of 9.1, will happen when the 9.1 codebase is used
    ;

    private final int number;
    private final String key;
    private final String defaultFormat;

    MessageSeeds(int number, String key, String defaultFormat) {
        this.number = number;
        this.key = key;
        this.defaultFormat = defaultFormat;
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
        return defaultFormat;
    }

    @Override
    public Level getLevel() {
        return Level.SEVERE;
    }

    @Override
    public String getModule() {
        return "PAC";
    }

}