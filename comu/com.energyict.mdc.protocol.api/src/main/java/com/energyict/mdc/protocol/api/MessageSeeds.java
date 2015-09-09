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
    PROTOCOL_REGISTER_NOT_FOUND(126, "issue.protocol.registernotfound", "Received register data (with OBIS code = {0}) for device with serial number {1}, but this register is not defined on the device");

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