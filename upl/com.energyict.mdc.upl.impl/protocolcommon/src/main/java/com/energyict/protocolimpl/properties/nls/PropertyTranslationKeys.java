package com.energyict.protocolimpl.properties.nls;

import com.energyict.mdc.upl.nls.TranslationKey;

/**
 * Contains all the {@link TranslationKey}s for the properties (and descriptions) of all the connection types.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-12-27 (10:28)
 */
public enum PropertyTranslationKeys implements TranslationKey {
    BASE_ADDRESS("upl.property.base.address", "DeviceId"),
    BASE_ADDRESS_DESCRIPTION("upl.property.base.address.description", "DeviceId"),
    BASE_PASSWORD("upl.property.base.password", "Password"),
    BASE_PASSWORD_DESCRIPTION("upl.property.base.password.description", "Password"),
    BASE_TIMEOUT("upl.property.base.timeout", "Timeout"),
    BASE_TIMEOUT_DESCRIPTION("upl.property.base.timeout.description", "Timeout"),
    BASE_RETRIES("upl.property.base.retries", "Retries"),
    BASE_RETRIES_DESCRIPTION("upl.property.base.retries.description", "Retries"),
    BASE_FORCED_DELAY("upl.property.base.forcedDelay", "ForcedDelay"),
    BASE_FORCED_DELAY_DESCRIPTION("upl.property.base.forcedDelay.description", "ForcedDelay"),
    BASE_EXTENDED_LOGGING("upl.property.base.extendedLogging", "ExtendedLogging"),
    BASE_EXTENDED_LOGGING_DESCRIPTION("upl.property.base.extendedLogging.description", "ExtendedLogging"),
    BASE_HALF_DUPLEX("upl.property.base.halfDuplex", "HalfDuplex"),
    BASE_HALF_DUPLEX_DESCRIPTION("upl.property.base.halfDuplex.description", "HalfDuplex"),
    BASE_ECHO_CANCELLING("upl.property.base.echoCancelling", "EchoCanncelling"),
    BASE_ECHO_CANCELLING_DESCRIPTION("upl.property.base.echoCancelling.description", "EchoCanncelling"),
    BASE_SECURITY_LEVEL("upl.property.base.securityLevel", "SecurityLevel"),
    BASE_SECURITY_LEVEL_DESCRIPTION("upl.property.base.securityLevel.description", "SecurityLevel"),
    BASE_PROTOCOL_COMPATABLE("upl.property.base.protocolCompatible", "ProtocolCompatible"),
    BASE_PROTOCOL_COMPATABLE_DESCRIPTION("upl.property.base.protocolCompatible.description", "ProtocolCompatible"),
    BASE_CHANNEL_MAP("upl.property.base.channelMap", "ChannelMap"),
    BASE_CHANNEL_MAP_DESCRIPTION("upl.property.base.channelMap.description", "ChannelMap"),
    BASE_DTR_BEHAVIOUR("upl.property.base.dtrBehaviour", "DTRBehaviour"),
    BASE_DTR_BEHAVIOUR_DESCRIPTION("upl.property.base.dtrBehaviour.description", "DTRBehaviour"),
    BASE_ADJUST_CHANNEL_MULTIPLIER("upl.property.base.adjustChannelMultiplier", "AdjustChannelMultiplier"),
    BASE_ADJUST_CHANNEL_MULTIPLIER_DESCRIPTION("upl.property.base.adjustChannelMultiplier.description", "AdjustChannelMultiplier"),
    BASE_ADJUST_REGISTER_MULTIPLIER("upl.property.base.adjustRegisterMultiplier", "AdjustRegisterMultiplier"),
    BASE_ADJUST_REGISTER_MULTIPLIER_DESCRIPTION("upl.property.base.adjustRegisterMultiplier.description", "AdjustRegisterMultiplier"),
    BASE_REQUEST_HEADER("upl.property.base.requestHeader", "Request header"),
    BASE_REQUEST_HEADER_DESCRIPTION("upl.property.base.requestHeader.description", "Request header"),
    BASE_SCALER("upl.property.base.scaler", "Scaler"),
    BASE_SCALER_DESCRIPTION("upl.property.base.scaler.description", "Scaler"),
    BASE_ROUNDTRIPCORRECTION("upl.property.base.roundtripCorrection", "RoundtripCorrection"),
    BASE_ROUNDTRIPCORRECTION_DESCRIPTION("upl.property.base.roundtripCorrection.description", "RoundtripCorrection"),
    BASE_NODEID("upl.property.base.nodeId", "NodeAddress"),
    BASE_NODEID_DESCRIPTION("upl.property.base.nodeId.description", "NodeAddress"),
    BASE_SERIALNUMBER("upl.property.base.serialNumber", "SerialNumber"),
    BASE_SERIALNUMBER_DESCRIPTION("upl.property.base.serialNumber.description", "SerialNumber"),
    BASE_PROFILE_INTERVAL("upl.property.base.profileInterval", "ProfileInterval"),
    BASE_PROFILE_INTERVAL_DESCRIPTION("upl.property.base.profileInterval.description", "ProfileInterval"),
    IEC1107_ADDRESS("upl.property.iec1107.address", "Address"),
    IEC1107_ADDRESS_DESCRIPTION("upl.property.iec1107.address.description", "Address"),
    IEC1107_PASSWORD("upl.property.iec1107.password", "Password"),
    IEC1107_PASSWORD_DESCRIPTION("upl.property.iec1107.password.description", "Password"),
    IEC1107_TIMEOUT("upl.property.iec1107.timeout", "Timeout"),
    IEC1107_TIMEOUT_DESCRIPTION("upl.property.iec1107.timeout.description", "Timeout"),
    IEC1107_RETRIES("upl.property.iec1107.retries", "Retries"),
    IEC1107_RETRIES_DESCRIPTION("upl.property.iec1107.retries.description", "Retries"),
    IEC1107_ROUNDTRIPCORRECTION("upl.property.iec1107.roundtripCorrection", "Roundtrip correction"),
    IEC1107_ROUNDTRIPCORRECTION_DESCRIPTION("upl.property.iec1107.roundtripCorrection.description", "Roundtrip correction"),
    IEC1107_SECURITYLEVEL("upl.property.iec1107.securityLevel", "Security level"),
    IEC1107_SECURITYLEVEL_DESCRIPTION("upl.property.iec1107.securityLevel.description", "Security level"),
    IEC1107_NODEID("upl.property.iec1107.nodeId", "Node address"),
    IEC1107_NODEID_DESCRIPTION("upl.property.iec1107.nodeId.description", "Node address"),
    IEC1107_ECHO_CANCELLING("upl.property.iec1107.echoCancelling", "Echo cancelling"),
    IEC1107_ECHO_CANCELLING_DESCRIPTION("upl.property.iec1107.echoCancelling.description", "Echo cancelling"),
    IEC1107_COMPATIBLE("upl.property.iec1107.compatible", "IEC1107 compatible"),
    IEC1107_COMPATIBLE_DESCRIPTION("upl.property.iec1107.compatible.description", "IEC1107 compatible"),
    IEC1107_EXTENDEDLOGGING("upl.property.iec1107.extendedLogging", "Extended logging"),
    IEC1107_EXTENDEDLOGGING_DESCRIPTION("upl.property.iec1107.extendedLogging.description", "Extended logging"),
    IEC1107_SERIALNUMBER("upl.property.iec1107.serialNumber", "Serialnumber"),
    IEC1107_SERIALNUMBER_DESCRIPTION("upl.property.iec1107.serialNumber.description", "Serialnumber"),
    IEC1107_PROFILEINTERVAL("upl.property.iec1107.profileInterval", "Profile interval"),
    IEC1107_PROFILEINTERVAL_DESCRIPTION("upl.property.iec1107.profileInterval.description", "Profile interval"),
    IEC1107_REQUESTHEADER("upl.property.iec1107.requestHeader", "Request header"),
    IEC1107_REQUESTHEADER_DESCRIPTION("upl.property.iec1107.requestHeader.description", "Request header"),
    IEC1107_SCALER("upl.property.iec1107.scaler", "Scaler"),
    IEC1107_SCALER_DESCRIPTION("upl.property.iec1107.scaler.description", "Scaler"),
    IEC1107_FORCEDDELAY("upl.property.iec1107.forcedDelay", "Forced delay"),
    IEC1107_FORCEDDELAY_DESCRIPTION("upl.property.iec1107.forcedDelay.description", "Forced delay"),
    IEC1107_SOFTWARE7E1("upl.property.iec1107.software7e1", "Software 7E1"),
    IEC1107_SOFTWARE7E1_DESCRIPTION("upl.property.iec1107.software7e1.description", "Software 7E1"),
    SDKSAMPLE_BREAKER_STATUS("upl.property.sdksample.breakerStatus", "Breaker status"),
    SDKSAMPLE_BREAKER_STATUS_DESCRIPTION("upl.property.sdksample.breakerStatus.description", "Breaker status"),
    SDKSAMPLE_ACTIVE_CALENDAR_NAME("upl.property.sdksample.activeCalendarName", "Active calendar name"),
    SDKSAMPLE_ACTIVE_CALENDAR_NAME_DESCRIPTION("upl.property.sdksample.activeCalendarName.description", "Active calendar name"),
    SDKSAMPLE_PASSIVE_CALENDAR_NAME("upl.property.sdksample.passiveCalendarName", "Passive calendar name"),
    SDKSAMPLE_PASSIVE_CALENDAR_NAME_DESCRIPTION("upl.property.sdksample.passiveCalendarName.description", "Passive calendar name"),
    SDKSAMPLE_DEFAULT_OPTIONAL_PROPERTY("upl.property.sdksample.defaultOptionalProperty", "Default optional property"),
    SDKSAMPLE_DEFAULT_OPTIONAL_PROPERTY_DESCRIPTION("upl.property.sdksample.defaultOptionalProperty.description", "Default optional property"),
    SDKSAMPLE_DELAY_AFTER_REQUEST_PROPERTY("upl.property.sdksample.delayAfterRequest", "Delay after request property"),
    SDKSAMPLE_DELAY_AFTER_REQUEST_PROPERTY_DESCRIPTION("upl.property.sdksample.delayAfterRequest.description", "Delay after request property"),
    SDKSAMPLE_ACTIVE_METER_FIRMWARE_VERSION("upl.property.sdksample.activeMeterFirmwareVersion", "Active meter firmware version"),
    SDKSAMPLE_ACTIVE_METER_FIRMWARE_VERSION_DESCRIPTION("upl.property.sdksample.activeMeterFirmwareVersion.description", "Active meter firmware version"),
    SDKSAMPLE_PASSIVE_METER_FIRMWARE_VERSION("upl.property.sdksample.passiveMeterFirmwareVersion", "Passive meter firmware version"),
    SDKSAMPLE_PASSIVE_METER_FIRMWARE_VERSION_DESCRIPTION("upl.property.sdksample.passiveMeterFirmwareVersion.description", "Passive meter firmware version"),
    SDKSAMPLE_ACTIVE_COMMUNICATION_FIRMWARE_VERSION("upl.property.sdksample.activeCommunicationFirmwareVersion", "Active communication firmware version"),
    SDKSAMPLE_ACTIVE_COMMUNICATION_FIRMWARE_VERSION_DESCRIPTION("upl.property.sdksample.activeCommunicationFirmwareVersion.description", "Active communication firmware version"),
    SDKSAMPLE_PASSIVE_COMMUNICATION_FIRMWARE_VERSION("upl.property.skdsample.passiveCommunicationFirmwareVersion", "Passive communication firmware version"),
    SDKSAMPLE_PASSIVE_COMMUNICATION_FIRMWARE_VERSION_DESCRIPTION("upl.property.skdsample.passiveCommunicationFirmwareVersion.description", "Passive communication firmware version"),
    SDKSAMPLE_NOT_SUPPORTED_LOADPROFILE_OBISCODE("upl.property.sdksample.notSupportedLoadprofile", "Not supported loadprofile"),
    SDKSAMPLE_NOT_SUPPORTED_LOADPROFILE_OBISCODE_DESCRIPTION("upl.property.sdksample.notSupportedLoadprofile.description", "Not supported loadprofile"),
    SDKSAMPLE_SLAVE_ONE_SERIAL_NUMBER("upl.property.sdksample.slaveOneSerialNumber", "Slave one serial number"),
    SDKSAMPLE_SLAVE_ONE_SERIAL_NUMBER_DESCRIPTION("upl.property.sdksample.slaveOneSerialNumber.description", "Slave one serial number"),
    SDKSAMPLE_SLAVE_TWO_SERIAL_NUMBER("upl.property.sdksample.slaveOneSerialNumber", "Slave two serial number"),
    SDKSAMPLE_SLAVE_TWO_SERIAL_NUMBER_DESCRIPTION("upl.property.sdksample.slaveOneSerialNumber.description", "Slave two serial number"),
    SDKSAMPLE_SIMILATE_REAL_COMMUNICATION("upl.property.sdksample.simulateRealCommunication", "Simulate real communication"),
    SDKSAMPLE_SIMILATE_REAL_COMMUNICATION_DESCRIPTION("upl.property.sdksample.simulateRealCommunication.description", "Simulate real communication"),
    MBUS_SECONDARY_ADDRESSING("upl.property.mbus.secondaryAdressing", "Secundary addressing"),
    MBUS_SECONDARY_ADDRESSING_DESCRIPTION("upl.property.mbus.secondaryAdressing.description", "Secundary addressing"),
    MBUS_VIRTUAL_LOAD_PROFILE("upl.property.mbus.virtualLoadProfile", "Virtual loadprofile"),
    MBUS_VIRTUAL_LOAD_PROFILE_DESCRIPTION("upl.property.mbus.virtualLoadProfile.description", "Virtual loadprofile"),
    MBUS_HEADER_MANUFACTURER_CODE("upl.property.mbus.headerManufacturerCode", "Header manufacturer code"),
    MBUS_HEADER_MANUFACTURER_CODE_DESCRIPTION("upl.property.mbus.headerManufacturerCode.description", "Header manufacturer code"),
    MBUS_DATA_QUANTITIES_ARE_ZERO_BASED("upl.property.mbus.dataQuantitiesAreZeroBased", "Data quantities are zero based"),
    MBUS_DATA_QUANTITIES_ARE_ZERO_BASED_DESCRIPTION("upl.property.mbus.dataQuantitiesAreZeroBased.description", "Data quantities are zero based"),
    MBUS_HEADER_MEDIUM("upl.property.mbus.headerMedium", "Header medium"),
    MBUS_HEADER_MEDIUM_DESCRIPTION("upl.property.mbus.headerMedium.description", "Header medium"),
    MBUS_HEADER_VERSION("upl.property.mbus.headerVersion", "Header version"),
    MBUS_HEADER_VERSION_DESCRIPTION("upl.property.mbus.headerVersion.description", "Header version"),
    SDKSAMPLE_SAMPLE("upl.property.sdksample.sample", "Sample"),
    SDKSAMPLE_SAMPLE_DESCRIPTION("upl.property.sdksample.sample.description", "Sample"),
    SDKSAMPLE_SIMULATE_REAL_COMMUNICATION("upl.property.sdksample.simulateRealCommunication", "Simulate real communication"),
    SDKSAMPLE_SIMULATE_REAL_COMMUNICATION_DESCRIPTION("upl.property.sdksample.simulateRealCommunication.description", "Simulate real communication"),
    SDKSAMPLE_LOAD_PROFILE_OBIS_CODE("upl.property.sdksample.loadProfileObisCode", "Loadprofile obiscode"),
    SDKSAMPLE_LOAD_PROFILE_OBIS_CODE_DESCRIPTION("upl.property.sdksample.loadProfileObisCode.description", "Loadprofile obiscode"),
    SDKSAMPLE_DEVICE_ALARM_EVENT_TYPE("upl.property.sdksample.deviceAlarmEventType", "Device alarm event type"),
    SDKSAMPLE_DEVICE_ALARM_EVENT_TYPE_DESCRIPTION("upl.property.sdksample.deviceAlarmEventType.description", "Device Alarm event type description");

    private final String key;
    private final String defaultFormat;

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
}