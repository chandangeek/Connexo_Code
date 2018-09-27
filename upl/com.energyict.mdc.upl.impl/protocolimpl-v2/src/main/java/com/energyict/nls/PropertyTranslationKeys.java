package com.energyict.nls;

import com.energyict.mdc.upl.nls.TranslationKey;

public enum PropertyTranslationKeys implements TranslationKey {

    V2_DLMS_CLIENT_MAC_ADDRESS("upl.property.v2.dlms.clientMacAddress", "Client MAC address"),
    V2_DLMS_CLIENT_MAC_ADDRESS_DESCRIPTION("upl.property.v2.dlms.clientMacAddress.description", "Client MAC address"),
    V2_DLMS_SERVER_MAC_ADDRESS("upl.property.v2.dlms.serverMacAddress", "Server MAC address"),
    V2_DLMS_SERVER_MAC_ADDRESS_DESCRIPTION("upl.property.v2.dlms.serverMacAddress.description", "Server MAC address"),
    V2_DLMS_DEVICE_ID_OBISCCODE("upl.property.v2.dlms.deviceIdObisCode", "Device Id obiscode"),
    V2_DLMS_DEVICE_ID_OBISCCODE_DESCRIPTION("upl.property.v2.dlms.deviceIdObisCode.description", "Device Id obiscode"),
    V2_G3_PROVIDE_PROTOCOL_JAVA_CLASS_NAME("upl.property.v2.g3.provideProtocolJavaClassName", "Provide protocol java classname"),
    V2_G3_PROVIDE_PROTOCOL_JAVA_CLASS_NAME_DESCRIPTION("upl.property.v2.g3.provideProtocolJavaClassName.description", "Provide protocol java classname"),
    V2_INBOUND_TIMEOUT("upl.property.v2.inbound.timeout", "Timeout"),
    V2_INBOUND_TIMEOUT_DESCRIPTION("upl.property.v2.inbound.timeout.description", "Timeout"),
    V2_INBOUND_RETRIES("upl.property.v2.inbound.retries", "Retries"),
    V2_INBOUND_RETRIES_DESCRIPTION("upl.property.v2.inbound.retries.description", "Retries"),
    V2_TASKS_TIMEOUT("upl.property.v2.tasks.timeout", "Timeout"),
    V2_TASKS_TIMEOUT_DESCRIPTION("upl.property.v2.tasks.timeout.description", "Timeout"),
    V2_TASKS_RETRIES("upl.property.v2.tasks.retries", "Retries"),
    V2_TASKS_RETRIES_DESCRIPTION("upl.property.v2.tasks.retries.description", "Retries"),

    V2_TASKS_DELAY_AFTER_ERROR("upl.property.v2.tasks.delayAfterError", "Delay after error"),
    V2_TASKS_DELAY_AFTER_ERROR_DESCRIPTION("upl.property.v2.tasks.delayAfterError.description", "Delay after error"),
    V2_TASKS_FORCED_DELAY("upl.property.v2.tasks.forcedDelay", "Forced delay"),
    V2_TASKS_FORCED_DELAY_DESCRIPTION("upl.property.v2.tasks.forcedDelay.description", "Forced delay"),
    V2_TASKS_ADDRESS("upl.property.v2.tasks.address", "Node address"),
    V2_TASKS_ADDRESS_DESCRIPTION("upl.property.v2.tasks.address.description", "Node address"),
    V2_TASKS_SEND_END_OF_SESSION("upl.property.v2.tasks.sendEndOfSession", "Send end of session"),
    V2_TASKS_SEND_END_OF_SESSION_DESCRIPTION("upl.property.v2.tasks.sendEndOfSession.description", "Send end of session"),
    V2_TASKS_MAX_ALLOWED_INVALID_PROFILE_RESPONSES("upl.property.v2.tasks.maxAllowedInvalidProfileResponses", "Max allowed invalid profile responses"),
    V2_TASKS_MAX_ALLOWED_INVALID_PROFILE_RESPONSES_DESCRIPTION("upl.property.v2.tasks.maxAllowedInvalidProfileResponses.description", "Max allowed invalid profile responses"),
    V2_TASKS_SERVER_LOG_LEVEL("upl.property.v2.tasks.serverLogLevel", "Server log level"),
    V2_TASKS_SERVER_LOG_LEVEL_DESCRIPTION("upl.property.v2.tasks.serverLogLevel.description", "Server log level"),
    V2_TASKS_PORT_LOG_LEVEL("upl.property.v2.tasks.portLogLevel", "Port log level"),
    V2_TASKS_PORT_LOG_LEVEL_DESCRIPTION("upl.property.v2.tasks.portLogLevel.description", "Port log level"),
    V2_TASKS_ROUNDTRIPCORRECTION("upl.property.v2.tasks.roundTripCorrection", "Roundtrip correction"),
    V2_TASKS_ROUNDTRIPCORRECTION_DESCRIPTION("upl.property.v2.tasks.roundTripCorrection.description", "Roundtrip correction"),
    V2_TASKS_ADDRESSING_MODE("upl.property.v2.tasks.addressingMode", "Addressing mode"),
    V2_TASKS_ADDRESSING_MODE_DESCRIPTION("upl.property.v2.tasks.addressingMode.description", "Addressing mode"),
    V2_TASKS_INFORMATION_FIELD_SIZE("upl.property.v2.tasks.informationFieldSize", "Information field size"),
    V2_TASKS_INFORMATION_FIELD_SIZE_DESCRIPTION("upl.property.v2.tasks.informationFieldSize.description", "Information field size"),

    PREVENT_CROSSING_INTERVAL_BOUNDARY_WHEN_READING_PROFILES("PREVENT_CROSSING_INTERVAL_BOUNDARY_WHEN_READING_PROFILES", "Prevent crossing interval boundary when reading profiles"),

    V2_ABNT_READER_SERIAL_NUMBER("upl.property.v2.abnt.readerSerialNumber", "Reader serial number"),
    V2_ABNT_READER_SERIAL_NUMBER_DESCRIPTION("upl.property.v2.abnt.readerSerialNumber.description", "Reader serial number"),
    V2_ABNT_RETRIES("upl.property.v2.abnt.retries", "Retries"),
    V2_ABNT_RETRIES_DESCRIPTION("upl.property.v2.abnt.retries.description", "Retries"),
    V2_ABNT_TIMEOUT("upl.property.v2.abnt.timeOut", "Timeout"),
    V2_ABNT_TIMEOUT_DESCRIPTION("upl.property.v2.abnt.timeOut.description", "Timeout"),
    V2_ABNT_FORCED_DELAY("upl.property.v2.abnt.forcedDelay", "Forced delay"),
    V2_ABNT_FORCED_DELAY_DESCRIPTION("upl.property.v2.abnt.forcedDelay.description", "Forced delay"),
    V2_ABNT_DELAY_AFTER_ERROR("upl.property.v2.abnt.delayAfterError", "Delay after error"),
    V2_ABNT_DELAY_AFTER_ERROR_DESCRIPTION("upl.property.v2.abnt.delayAfterError.description", "Delay after error"),
    V2_ACE4000_CALL_HOME_ID("upl.property.v2.ace4000.callHomeId", "Call home id"),
    V2_ACE4000_CALL_HOME_ID_DESCRIPTION("upl.property.v2.ace4000.callHomeId.description", "Call home id"),
    V2_ACE4000_TIMEOUT("upl.property.v2.ace4000.timeOut", "Timeout"),
    V2_ACE4000_TIMEOUT_DESCRIPTION("upl.property.v2.ace4000.timeOut.description", "Timeout"),
    V2_ACE4000_RETRIES("upl.property.v2.ace4000.retries", "Retries"),
    V2_ACE4000_RETRIES_DESCRIPTION("upl.property.v2.ace4000.retries.description", "Retries"),

    V2_DLMS_CALL_HOME_ID("upl.property.v2.dlms.callHomeId", "Call home id"),
    V2_DLMS_CALL_HOME_ID_DESCRIPTION("upl.property.v2.dlms.callHomeId.description", "Call home id"),
    V2_DLMS_MIRROR_LOGICAL_DEVICE_ID("upl.property.v2.dlms.mirrorLogicalDeviceId", "Mirror logical device id"),
    V2_DLMS_MIRROR_LOGICAL_DEVICE_ID_DESCRIPTION("upl.property.v2.dlms.mirrorLogicalDeviceId.description", "Mirror logical device id"),
    V2_DLMS_GATEWAY_LOGICAL_DEVICE_ID("upl.property.v2.dlms.gatewayLogicalDeviceId", "Gateway logical device id"),
    V2_DLMS_GATEWAY_LOGICAL_DEVICE_ID_DESCRIPTION("upl.property.v2.dlms.gatewayLogicalDeviceId.description", "Gateway logical device id"),
    V2_DLMS_PSK("upl.property.v2.dlms.psk", "PSK"),
    V2_IP_V6_ADDRESS("upl.property.v2.dlms.IP_V6_ADDRESS", "IPv6 address"),
    V2_DEVICE_SYSTEM_TITLE("upl.property.v2.dlms.DEVICE_SYSTEM_TITLE", "Device system title"),
    V2_IP_V4_ADDRESS("upl.property.v2.dlms.IP_V4_ADDRESS", "IPv4 address"),
    V2_DLMS_PSK_DESCRIPTION("upl.property.v2.dlms.psk.description", "PSK"),
    V2_DLMS_AARQ_TIMEOUT("upl.property.v2.dlms.aarqTimeout", "AARQ timeout"),
    V2_DLMS_AARQ_TIMEOUT_DESCRIPTION("upl.property.v2.dlms.aarqTimeout.description", "AARQ timeout"),
    V2_DLMS_READCACHE("upl.property.v2.dlms.readCache", "Read cache"),
    V2_DLMS_READCACHE_DESCRIPTION("upl.property.v2.dlms.readCache.description", "Read cache"),
    V2_DLMS_AARQ_RETRIES("upl.property.v2.dlms.aarqRetries", "AARQ retries"),
    V2_DLMS_AARQ_RETRIES_DESCRIPTION("upl.property.v2.dlms.aarqRetries.description", "AARQ retries"),
    V2_DLMS_MAX_REC_PDU_SIZE("upl.property.v2.dlms.maxRexPduSize", "Maximum received PDU size"),
    V2_DLMS_MAX_REC_PDU_SIZE_DESCRIPTION("upl.property.v2.dlms.maxRexPduSize.description", "Maximum received PDU size"),
    V2_DLMS_SERVER_UPPER_MAC_ADDRESS("upl.property.v2.dlms.serverUpperMacAddress", "Server upper MAC address"),
    V2_DLMS_SERVER_UPPER_MAC_ADDRESS_DESCRIPTION("upl.property.v2.dlms.serverUpperMacAddress.description", "Server upper MAC address"),
    V2_DLMS_USE_GBT("upl.property.v2.dlms.useGBT", "Use GBT"),
    V2_DLMS_USE_GBT_DESCRIPTION("upl.property.v2.dlms.useGBT.description", "Use GBT"),
    V2_DLMS_GBT_WINDOW_SIZE("upl.property.v2.dlms.gBTWindowSize", "GBT window size"),
    V2_DLMS_GBT_WINDOW_SIZE_DESCRIPTION("upl.property.v2.dlms.gBTWindowSize.description", "GBT window size"),
    V2_DLMS_CIPHERING_TYPE("upl.property.v2.dlms.cipheringType", "Ciphering type"),
    V2_DLMS_CIPHERING_TYPE_DESCRIPTION("upl.property.v2.dlms.cipheringType.description", "Ciphering type"),
    V2_DLMS_IDIS_CALLING_AP_TITLE("upl.property.v2.dlms.idis.callingApTitle", "Calling AP title"),
    V2_DLMS_IDIS_CALLING_AP_TITLE_DESCRIPTION("upl.property.v2.dlms.idis.callingApTitle.description", "Calling AP title"),
    V2_DLMS_LIMIT_MAX_NR_OF_DAYS("upl.property.v2.dlms.limitMaxNrOFDays", "Limit max nr of days"),
    V2_DLMS_LIMIT_MAX_NR_OF_DAYS_DESCRIPTION("upl.property.v2.dlms.limitMaxNrOFDays.description", "Limit max nr of days"),
    V2_DLMS_TIMEZONE("upl.property.v2.dlms.timeZone", "Timezone"),
    V2_DLMS_TIMEZONE_DESCRIPTION("upl.property.v2.dlms.timeZone.description", "Timezone"),
    V2_DLMS_VALIDATE_INVOKE_ID("upl.property.v2.dlms.validateInvokeId", "Validate invoke id"),
    V2_DLMS_VALIDATE_INVOKE_ID_DESCRIPTION("upl.property.v2.dlms.validateInvokeId.description", "Validate invoke id"),
    V2_DLMS_FORCED_DELAY("upl.property.v2.dlms.forcedDelay", "Forced delay"),
    V2_DLMS_FORCED_DELAY_DESCRIPTION("upl.property.v2.dlms.forcedDelay.description", "Forced delay"),

    V2_DLMS_SWAP_SERVER_AND_CLIENT_ADDRESS("upl.property.v2.dlms.swapServerAndClientAddress", "Swap server and client address"),
    V2_DLMS_SWAP_SERVER_AND_CLIENT_ADDRESS_DESCRIPTION("upl.property.v2.dlms.swapServerAndClientAddress.description", "Swap server and client address"),
    V2_DLMS_IGNORE_CALLING_AP_TITLE("upl.property.v2.dlms.ignoreCallingApTitle", "Ignore calling AP title"),
    V2_DLMS_IGNORE_CALLING_AP_TITLE_DESCRIPTION("upl.property.v2.dlms.ignoreCallingApTitle.description", "Ignore calling AP title"),
    V2_DLMS_USE_LOGICAL_DEVICE_NAME_AS_SERIAL("upl.property.v2.dlms.useLogicalDeviceNameAsSerial", "Use logical device name as serial"),
    V2_DLMS_USE_LOGICAL_DEVICE_NAME_AS_SERIAL_DESCRIPTION("upl.property.v2.dlms.useLogicalDeviceNameAsSerial.description", "Use logical device name as serial"),
    V2_DLMS_USE_DEFINED_AS_TIME_DEVIATION("upl.property.v2.dlms.useDefinedAsTimeDeviation", "Use defined as time deviation"),
    V2_DLMS_USE_DEFINED_AS_TIME_DEVIATION_DESCRIPTION("upl.property.v2.dlms.useDefinedAsTimeDeviation.description", "Use defined as time deviation"),


    V2_DLMS_CONNECTION_MODE("upl.property.dlms.connectionMode", "Connection mode"),
    V2_DLMS_VALIDATE_CACHED_FRAMECOUNTER("upl.property.v2.dlms.validateCachedFramecounter", "Validate cached framecounter"),
    V2_SKIP_FC_AUTH_TAG_VALIDATION("upl.property.v2.dlms.SKIP_FC_AUTH_TAG_VALIDATION", "Skip FC authentication tag validation"),
    V2_SKIP_SLAVE_DEVICES("upl.property.v2.dlms.SKIP_SLAVE_DEVICES", "Skip slave devices"),
    V2_USE_UNDEFINED_AS_CLOCK_STATUS("upl.property.v2.dlms.V2_USE_UNDEFINED_AS_CLOCK_STATUS", "Use undefined as clock status"),
    V2_USE_UNDEFINED_AS_TIME_DEVIATION("upl.property.v2.dlms.USE_UNDEFINED_AS_TIME_DEVIATION", "Use undefined as time deviation"),
    V2_USE_FIXED_OBJECT_LIST("upl.property.v2.dlms.USE_FIXED_OBJECT_LIST", "Use fixed object list"),
    V2_SUPPORTS_HUNDRETHS_TIMEFIELD("upl.property.v2.dlms.SUPPORTS_HUNDRETHS_TIMEFIELD", "Supports hundredths timefield"),
    V2_DLMS_VALIDATE_CACHED_FRAMECOUNTER_DESCRIPTION("upl.property.v2.dlms.validateCachedFramecounter.description", "Validate cached framecounter"),
    V2_DLMS_USE_CACHED_FRAMECOUNTER("upl.property.v2.dlms.useCachedFrameCounter", "Use cached framecounter"),
    V2_VALIDATE_LOAD_PROFILE_CHANNELS("upl.property.v2.dlms.VALIDATE_LOAD_PROFILE_CHANNELS", "Validate load profile channels"),
    V2_DLMS_USE_CACHED_FRAMECOUNTER_DESCRIPTION("upl.property.v2.dlms.useCachedFrameCounter.description", "Use cached framecounter"),
    V2_DLMS_REQUEST_AUTHENTICATE_FRAME_COUNTER("upl.property.v2.dlms.requestAuthenticateFrameCounter", "Request authentication framecounter"),
    V2_DLMS_REQUEST_AUTHENTICATE_FRAME_COUNTER_DESCRIPTION("upl.property.v2.dlms.requestAuthenticateFrameCounter.description", "Request authentication framecounter"),
    V2_DLMS_LAST_SEENDATE("upl.property.v2.dlms.lastSeenDate", "Last seen date"),
    V2_SHORT_ADDRESS_PAN("upl.property.v2.dlms.SHORT_ADDRESS_PAN", "Short address PAN"),
    V2_DLMS_LAST_SENDDATE_DESCRIPTION("upl.property.v2.dlms.lastSendDate.description", "Last senddate"),
    V2_DLMS_NODEID("upl.property.v2.dlms.nodeId", "NodeAddress"),
    V2_DLMS_NODEID_DESCRIPTION("upl.property.v2.dlms.nodeId.description", "NodeAddress"),
    V2_DLMS_USE_EQUIPMENT_IDENTIFIER_AS_SERIAL("upl.property.v2.dlms.useEquipmentIdentifierAsSerial", "Use equipment identifier as serial"),
    V2_DLMS_USE_EQUIPMENT_IDENTIFIER_AS_SERIAL_DESCRIPTION("upl.property.v2.dlms.useEquipmentIdentifierAsSerial.description", "Use equipment identifier as serial"),
    V2_DLMS_INITIAL_FRAME_COUNTER("upl.property.v2.dlms.initialFrameCounter", "Initial framecounter"),
    V2_DLMS_INITIAL_FRAME_COUNTER_DESCRIPTION("upl.property.v2.dlms.initialFrameCounter.description", "Initial framecounter"),
    V2_DLMS_USE_METER_IN_TRANSPARENT_MODE("upl.property.v2.dlms.useMeterInTransparentMode", "Use meter in transparent mode"),
    V2_DLMS_USE_METER_IN_TRANSPARENT_MODE_DESCRIPTION("upl.property.v2.dlms.useMeterInTransparentMode.description", "Use meter in transparent mode"),
    V2_DLMS_METER_SECURITY_LEVEL("upl.property.v2.dlms.meterSecurityLevel", "Meter security level"),
    V2_DLMS_METER_SECURITY_LEVEL_DESCRIPTION("upl.property.v2.dlms.meterSecurityLevel.description", "Meter security level"),
    V2_DLMS_PASSWORD("upl.property.v2.dlms.password", "Password"),
    V2_DLMS_PASSWORD_DESCRIPTION("upl.property.v2.dlms.password.description", "Password"),
    V2_DLMS_SERVER_LOWER_MAC_ADDRESS("upl.property.v2.dlms.serverLowerMacAddress", "Server lower MAC address"),
    V2_DLMS_SERVER_LOWER_MAC_ADDRESS_DESCRIPTION("upl.property.v2.sdlms.erverLowerMacAddress.description", "Server lower MAC address"),
    V2_DLMS_TRANSP_CONNECT_TIME("upl.property.v2.dlms.transpConnectTime", "TransparentConnectTime"),
    V2_DLMS_TRANSP_CONNECT_TIME_DESCRIPTION("upl.property.v2.dlms.transpConnectTime.description", "TransparentConnectTime"),
    V2_DLMS_FRAME_COUNTER_RECOVERY_RETRIES("upl.property.v2.dlms.frameCounterRecoveryRetries", "Framecounter recovery retries"),
    V2_DLMS_FRAME_COUNTER_RECOVERY_RETRIES_DESCRIPTION("upl.property.v2.dlms.frameCounterRecoveryRetries.description", "Framecounter recovery retries"),
    V2_DLMS_FRAME_COUNTER_RECOVERY_STEP("upl.property.v2.dlms.frameCounterRecoveryStep", "Framecounter recovery step"),
    V2_DLMS_FRAME_COUNTER_RECOVERY_STEP_DESCRIPTION("upl.property.v2.dlms.frameCounterRecoveryStep.description", "Framecounter recovery step"),
    V2_DLMS_POLLING_DELAY("upl.property.v2.dlms.pollingDelay", "Polling delay"),
    V2_DLMS_POLLING_DELAY_DESCRIPTION("upl.property.v2.dlms.pollingDelay.description", "Polling delay"),

    V2_EDP_SERVER_UPPER_MAC_ADDRESS("upl.property.v2.edp.serverUpperMacAddress", "Server upper MAC address"),
    V2_EDP_SERVER_UPPER_MAC_ADDRESS_DESCRIPTION("upl.property.v2.edp.serverUpperMacAddress.description", "Server upper MAC address"),
    V2_EDP_SERVER_LOWER_MAC_ADDRESS("upl.property.v2.edp.serverLowerMacAddress", "Lower upper MAC address"),
    V2_EDP_SERVER_LOWER_MAC_ADDRESS_DESCRIPTION("upl.property.v2.edp.serverLowerMacAddress.description", "Lower upper MAC address"),
    V2_EDP_READCACHE("upl.property.v2.edp.readCache", "Read cache"),
    V2_EDP_READCACHE_DESCRIPTION("upl.property.v2.edp.readCache.description", "Read cache"),
    V2_EDP_FORCED_DELAY("upl.property.v2.edp.forcedDelay", "Forced delay"),
    V2_EDP_FORCED_DELAY_DESCRIPTION("upl.property.v2.edp.forcedDelay.description", "Forced delay"),
    V2_EDP_MAX_REC_PDU_SIZE("upl.property.v2.edp.maxRexPduSize", "Maximum received PDU size"),
    V2_EDP_MAX_REC_PDU_SIZE_DESCRIPTION("upl.property.v2.edp.maxRexPduSize.description", "Maximum received PDU size"),

    V2_EICT_PHONE_NUMBER("upl.property.v2.eict.phoneNumber", "Phone number"),
    V2_EICT_PHONE_NUMBER_DESCRIPTION("upl.property.v2.eict.phoneNumber.description", "Phone number"),
    V2_EICT_MAX_IDLE_TIME("upl.property.v2.eict.maxIdleTime", "Max idle time"),
    V2_EICT_MAX_IDLE_TIME_DESCRIPTION("upl.property.v2.eict.maxIdleTime.description", "Max idle time"),
    V2_EICT_SERVER_UPPER_MAC_ADDRESS("upl.property.v2.eict.serverUpperMacAddress", "Server upper MAC address"),
    V2_EICT_SERVER_UPPER_MAC_ADDRESS_DESCRIPTION("upl.property.v2.eict.serverUpperMacAddress.description", "Server upper MAC address"),
    V2_EICT_VALIDATE_INVOKE_ID("upl.property.v2.eict.validateInvokeId", "Validate invoke Id"),
    V2_EICT_VALIDATE_INVOKE_ID_DESCRIPTION("upl.property.v2.eict.validateInvokeId.description", "Validate invoke Id"),
    V2_EICT_AARQ_TIMEOUT("upl.property.v2.eict.aARQTimeout", "AARQ timeout"),
    V2_EICT_AARQ_TIMEOUT_DESCRIPTION("upl.property.v2.eict.aARQTimeout.description", "AARQ timeout"),
    V2_EICT_FORCED_DELAY("upl.property.v2.eict.forcedDelay", "Forced delay"),
    V2_EICT_FORCED_DELAY_DESCRIPTION("upl.property.v2.eict.forcedDelay.description", "Forced delay"),
    V2_EICT_TIMEZONE("upl.property.v2.eict.timezone", "Timezone"),
    V2_EICT_TIMEZONE_DESCRIPTION("upl.property.v2.eict.timezone.description", "Timezone"),
    V2_EICT_SERVER_LOWER_MAC_ADDRESS("upl.property.v2.eict.serverLowerMacAddress", "Server lower MAC address"),
    V2_EICT_SERVER_LOWER_MAC_ADDRESS_DESCRIPTION("upl.property.v2.eict.serverLowerMacAddress.description", "Server lower MAC address"),
    V2_EICT_MAX_REC_PDU_SIZE("upl.property.v2.eict.maxRecPduSize", "Maximum received PDU size"),
    V2_EICT_MAX_REC_PDU_SIZE_DESCRIPTION("upl.property.v2.eict.maxRecPduSize.description", "Maximum received PDU size"),
    V2_EICT_BULK_REQUEST("upl.property.v2.eict.bulkRequest", "Bulk request"),
    V2_EICT_BULK_REQUEST_DESCRIPTION("upl.property.v2.eict.bulkRequest.description", "Bulk request"),

    V2_ELSTER_TIMEZONE("upl.property.v2.elster.timezone", "Timezone"),
    V2_ELSTER_TIMEZONE_DESCRIPTION("upl.property.v2.elster.timezone.description", "Timezone"),
    V2_ELSTER_DEBUG("upl.property.v2.elster.debug", "Debug"),
    V2_ELSTER_DEBUG_DESCRIPTION("upl.property.v2.elster.debug.description", "Debug"),
    V2_ELSTER_CHANNEL_BACKLOG("upl.property.v2.elster.channelBacklog", "Channel backlog"),
    V2_ELSTER_CHANNEL_BACKLOG_DESCRIPTION("upl.property.v2.elster.channelBacklog.description", "Channel backlog"),
    V2_ELSTER_EXTRACT_INSTALLATION_DATE("upl.property.v2.elster.extractInstallationDate", "Extract installation date"),
    V2_ELSTER_EXTRACT_INSTALLATION_DATE_DESCRIPTION("upl.property.v2.elster.extractInstallationDate.description", "Extract installation date"),
    V2_ELSTER_REMOVE_DAY_PROFILE_OFFSET("upl.property.v2.elster.removeDayProfileOffset", "Remove day profile offset"),
    V2_ELSTER_REMOVE_DAY_PROFILE_OFFSET_DESCRIPTION("upl.property.v2.elster.removeDayProfileOffset.description", "Remove day profile offset"),
    V2_ELSTER_CALL_HOME_ID("upl.property.v2.elster.callHomeId", "Call home id"),
    V2_ELSTER_CALL_HOME_ID_DESCRIPTION("upl.property.v2.elster.callHomeId.description", "Call home id"),
    V2_ELSTER_SOURCE("upl.property.v2.elster.source", "Source"),
    V2_ELSTER_SOURCE_DESCRIPTION("upl.property.v2.elster.source.description", "Source"),
    V2_ELSTER_AUTHENTICATION("upl.property.v2.elster.authentication", "Authentication"),
    V2_ELSTER_AUTHENTICATION_DESCRIPTION("upl.property.v2.elster.authentication.description", "Authentication"),
    V2_ELSTER_DEVICE_TYPE("upl.property.v2.elster.deviceType", "Device type"),
    V2_ELSTER_DEVICE_TYPE_DESCRIPTION("upl.property.v2.elster.deviceType.description", "Device type"),
    V2_ELSTER_DEVICE_ID("upl.property.v2.elster.deviceId", "Device id"),
    V2_ELSTER_DEVICE_ID_DESCRIPTION("upl.property.v2.elster.deviceId.description", "Device id"),
    V2_ELSTER_RETRIES("upl.property.v2.elster.retries", "Retries"),
    V2_ELSTER_RETRIES_DESCRIPTION("upl.property.v2.elster.retries.description", "Retries"),
    V2_ELSTER_TIMEOUT("upl.property.v2.elster.timeout", "Timeout"),
    V2_ELSTER_TIMEOUT_DESCRIPTION("upl.property.v2.elster.timeout.description", "Timeout"),
    V2_ELSTER_FORCED_DELAY("upl.property.v2.elster.forcedDelay", "Forced delay"),
    V2_ELSTER_FORCED_DELAY_DESCRIPTION("upl.property.v2.elster.forcedDelay.description", "Forced delay"),
    V2_ELSTER_DELAY_AFTER_ERROR("upl.property.v2.elster.delayAfterError", "Delay after error"),
    V2_ELSTER_DELAY_AFTER_ERROR_DESCRIPTION("upl.property.v2.elster.delayAfterError.description", "Delay after error"),
    V2_ELSTER_VALIDATE_INVOKE_ID("upl.property.v2.elster.validateInvokeId", "Validate invoke id"),
    V2_ELSTER_VALIDATE_INVOKE_ID_DESCRIPTION("upl.property.v2.elster.validateInvokeId.description", "Validate invoke id"),
    V2_ELSTER_REQUEST_TIMEZONE("upl.property.v2.elster.requestTimeZone", "Request timezone"),
    V2_ELSTER_REQUEST_TIMEZONE_DESCRIPTION("upl.property.v2.elster.requestTimeZone.description", "Request timezone"),
    V2_ELSTER_BULK_REQUEST("upl.property.v2.elster.bulkRequest", "Bulk request"),
    V2_ELSTER_BULK_REQUEST_DESCRIPTION("upl.property.v2.elster.bulkRequest.description", "Bulk request"),
    V2_ELSTER_SERVER_UPPER_MAC_ADDRESS("upl.property.v2.elster.serverUpperMacAddress", "Server upper MAC address"),
    V2_ELSTER_SERVER_UPPER_MAC_ADDRESS_DESCRIPTION("upl.property.v2.elster.serverUpperMacAddress.description", "Server upper MAC address"),
    V2_ELSTER_SERVER_LOWER_MAC_ADDRESS("upl.property.v2.elster.serverLowerMacAddress", "Server lower MAC address"),
    V2_ELSTER_SERVER_LOWER_MAC_ADDRESS_DESCRIPTION("upl.property.v2.elster.serverLowerMacAddress.description", "Server lower MAC address"),
    V2_ELSTER_CONFORMANCE_BLOCK_VALUE("upl.property.v2.elster.conformanceBlockValue", "Conformance block value"),
    V2_ELSTER_CONFORMANCE_BLOCK_VALUE_DESCRIPTION("upl.property.v2.elster.conformanceBlockValue.description", "Conformance block value"),
    V2_ELSTER_MANUFACTURER("upl.property.v2.elster.manufacturer", "Manufacturer"),
    V2_ELSTER_MANUFACTURER_DESCRIPTION("upl.property.v2.elster.manufacturer.description", "Manufacturer"),
    V2_ELSTER_MAX_REC_PDU_SIZE("upl.property.v2.elster.MaxRecPduSize", "Maximum received PDU size"),
    V2_ELSTER_MAX_REC_PDU_SIZE_DESCRIPTION("upl.property.v2.elster.MaxRecPduSize.description", "Maximum received PDU size"),
    V2_ELSTER_CIPHERING_TYPE("upl.property.v2.elster.cipheringType", "Ciphering type"),
    V2_ELSTER_CIPHERING_TYPE_DESCRIPTION("upl.property.v2.elster.cipheringType.description", "Ciphering type"),
    V2_ELSTER_NTA_SIMULATION_TOOL("upl.property.v2.elster.ntaSimulationToo", "NTA simulation tool"),
    V2_ELSTER_NTA_SIMULATION_TOOL_DESCRIPTION("upl.property.v2.elster.ntaSimulationToo.description", "NTA simulation tool"),
    V2_ELSTER_FIX_MBUS_HEX_SHORT_ID("upl.property.v2.elster.fixMbusHexShortId", "Fix MBUS hex shortId"),
    V2_ELSTER_FIX_MBUS_HEX_SHORT_ID_DESCRIPTION("upl.property.v2.elster.fixMbusHexShortId.description", "Fix MBUS hex shortId"),
    V2_ELSTER_PUBLIC_CLIENT_ASSOCIATION_PRE_ESTABLISHED("upl.property.v2.elster.publicClientAssociationPreEstablished", "Public client association pre-established"),
    V2_ELSTER_VALIDATE_LOAD_PROFILE_CHANNELS("upl.property.v2.elster.VALIDATE_LOAD_PROFILE_CHANNELS", "Validate load profile channels"),
    V2_ELSTER_PUBLIC_CLIENT_ASSOCIATION_PRE_ESTABLISHED_DESCRIPTION("upl.property.v2.elster.publicClientAssociationPreEstablished.description", "Public client association pre-established"),

    V2_NTA_USE_EQUIPMENT_IDENTIFIER_AS_SERIAL("upl.property.v2.nta.useEquipmentIdentifierAsSerial", "Use equipment identifier as serial"),
    V2_NTA_USE_EQUIPMENT_IDENTIFIER_AS_SERIAL_DESCRIPTION("upl.property.v2.nta.useEquipmentIdentifierAsSerial.description", "Use equipment identifier as serial"),
    V2_NTA_IGNORE_DST_STATUS_CODE("upl.property.v2.nta.ignoreDstStatusCode", "Ignore DST status code"),
    V2_NTA_IGNORE_DST_STATUS_CODE_DESCRIPTION("upl.property.v2.nta.ignoreDstStatusCode.description", "Ignore DST status code"),
    V2_NTA_NTA_SIMULATION_TOOL("upl.property.v2.nta.ntaSimulationTool", "NTA simulation tool"),
    V2_NTA_NTA_SIMULATION_TOOL_DESCRIPTION("upl.property.v2.nta.ntaSimulationTool.description", "NTA simulation tool"),
    V2_NTA_BULK_REQUEST("upl.property.v2.nta.bulkRequest", "Bulk request"),
    V2_NTA_BULK_REQUEST_DESCRIPTION("upl.property.v2.nta.bulkRequest.description", "Bulk request"),
    V2_NTA_MAX_REC_PDU_SIZE("upl.property.v2.nta.maxRecPduSize", "Maximum received PDU size"),
    V2_NTA_MAX_REC_PDU_SIZE_DESCRIPTION("upl.property.v2.nta.maxRecPduSize.description", "Maximum received PDU size"),
    V2_NTA_FORCED_DELAY("upl.property.v2.nta.forcedDelay", "Forced delay"),
    V2_NTA_FORCED_DELAY_DESCRIPTION("upl.property.v2.nta.forcedDelay.description", "Forced delay"),
    V2_NTA_REQUEST_TIMEZONE("upl.property.v2.nta.requestTimeZone", "Request timezone"),
    V2_REQUEST_FRAMECOUNTER("upl.property.v2.nta.REQUEST_FRAMECOUNTER", "Request frame counter"),
    V2_NTA_REQUEST_TIMEZONE_DESCRIPTION("upl.property.v2.nta.requestTimeZone.description", "Request timezone"),
    V2_NTA_AARQ_RETRIES("upl.property.v2.nta.aarqRetries", "AARQ retries"),
    V2_NTA_AARQ_RETRIES_DESCRIPTION("upl.property.v2.nta.aarqRetries.description", "AARQ retries"),
    V2_NTA_AARQ_TIMEOUT("upl.property.v2.nta.aarqTimeout", "AARQ timeout"),
    V2_NTA_AARQ_TIMEOUT_DESCRIPTION("upl.property.v2.nta.aarqTimeout.description", "AARQ timeout"),
    V2_NTA_PSK("upl.property.v2.nta.psk", "PSK"),
    V2_NTA_PSK_DESCRIPTION("upl.property.v2.nta.psk.description", "PSK"),
    V2_NTA_READCACHE("upl.property.v2.nta.readCache", "Read cache"),
    V2_NTA_READCACHE_DESCRIPTION("upl.property.v2.nta.readCache.description", "Read cache"),
    V2_NTA_CUMULATIVE_CAPTURE_TIME_CHANNEL("upl.property.v2.nta.cumulativeCaptureTimeChannel", "Cumulative capture time channel"),
    V2_NTA_CUMULATIVE_CAPTURE_TIME_CHANNEL_DESCRIPTION("upl.property.v2.nta.cumulativeCaptureTimeChannel.description", "Cumulative capture time channel"),
    V2_NTA_VALIDATE_INVOKE_ID("upl.property.v2.nta.validateInvokeId", "Validate invoke id"),
    V2_NTA_VALIDATE_INVOKE_ID_DESCRIPTION("upl.property.v2.nta.validateInvokeId.description", "Validate invoke id"),
    V2_NTA_TIMEZONE("upl.property.v2.nta.timezone", "Timezone"),
    V2_NTA_TIMEZONE_DESCRIPTION("upl.property.v2.nta.timezone.description", "Timezone"),
    V2_NTA_CHECK_NUMBER_OF_BLOCKS_DURING_FIRMWARE_RESUME("upl.property.v2.nta.checkNumberOfBlocksDuringFirmwareResume", "Check number of blocks during firmware resume"),
    V2_NTA_CHECK_NUMBER_OF_BLOCKS_DURING_FIRMWARE_RESUME_DESCRIPTION("upl.property.v2.nta.checkNumberOfBlocksDuringFirmwareResume.description", "Check number of blocks during firmware resume"),
    V2_NTA_SERVER_LOWER_MAC_ADDRESS("upl.property.v2.nta.serverLowerMacAddress", "Server lower MAC address"),
    V2_NTA_SERVER_LOWER_MAC_ADDRESS_DESCRIPTION("upl.property.v2.nta.serverLowerMacAddress.description", "Server lower MAC address"),
    V2_NTA_NODEID("upl.property.v2.nta.nodeId", "Node address"),
    V2_NTA_NODEID_DESCRIPTION("upl.property.v2.nta.nodeId.description", "Node address"),
    V2_NTA_MASTERKEY("upl.property.v2.masterkey", "Master key"),
    V2_NTA_MASTERKEY_DESCRIPTION("upl.property.v2.masterkey.description", "Master key"),

    V2_ELSTER_GATEWAY_LOGICAL_DEVICE_ID("upl.property.v2.elster.gatewayLogicalDeviceId", "Gateway logical device id"),
    V2_ELSTER_GATEWAY_LOGICAL_DEVICE_ID_DESCRIPTION("upl.property.v2.elster.gatewayLogicalDeviceId.description", "Gateway logical device id"),
    V2_ELSTER_POLLING_DELAY("upl.property.v2.elster.pollingDelay", "Polling delay"),
    V2_ELSTER_POLLING_DELAY_DESCRIPTION("upl.property.v2.elster.pollingDelay.description", "Polling delay"),
    V2_ELSTER_MIRROR_LOGICAL_DEVICE_ID("upl.property.v2.elster.mirrorLogicalDeviceId", "Mirror logical device id"),
    V2_ELSTER_MIRROR_LOGICAL_DEVICE_ID_DESCRIPTION("upl.property.v2.elster.mirrorLogicalDeviceId.description", "Mirror logical device id"),
    V2_ELSTER_LAST_SEND_DATE("upl.property.v2.elster.lastSendDate", "Last send date"),
    V2_ELSTER_LAST_SEND_DATE_DESCRIPTION("upl.property.v2.elster.lastSendDate.description", "Last send date"),

    V2_TEST_TIMEOUT("upl.property.v2.test.timeout", "Timeout"),
    V2_TEST_TIMEOUT_DESCRIPTION("upl.property.v2.test.timeout.description", "Timeout"),
    V2_TEST_RETRIES("upl.property.v2.test.retries", "Retries"),
    V2_TEST_RETRIES_DESCRIPTION("upl.property.v2.test.retries.description", "Retries"),
    V2_TEST_SCALE_A("upl.property.v2.test.scaleA", "Test scale A"),
    V2_TEST_SCALE_A_DESCRIPTION("upl.property.v2.test.scaleA.description", "Test scale A"),
    V2_TEST_SCALE_B("upl.property.v2.test.scaleB", "Test scale B"),
    V2_TEST_SCALE_B_DESCRIPTION("upl.property.v2.test.scaleB.description", "Test scale B"),
    V2_TEST_SCALE_C("upl.property.v2.test.scaleC", "Test scale C"),
    V2_TEST_SCALE_C_DESCRIPTION("upl.property.v2.test.scaleC.description", "Test scale C"),
    V2_TEST_SCALE_D("upl.property.v2.test.scaleD", "Test scale D"),
    V2_TEST_SCALE_D_DESCRIPTION("upl.property.v2.test.scaleD.description", "Test scale D"),
    V2_TEST_MULTIPLIER_A("upl.property.v2.test.multiplierA", "Test multiplier A"),
    V2_TEST_MULTIPLIER_A_DESCRIPTION("upl.property.v2.test.multiplierA.description", "Test multiplier A"),
    V2_TEST_MULTIPLIER_B("upl.property.v2.test.multiplierB", "Test multiplier B"),
    V2_TEST_MULTIPLIER_B_DESCRIPTION("upl.property.v2.test.multiplierB.description", "Test multiplier B"),
    V2_TEST_MULTIPLIER_C("upl.property.v2.test.multiplierC", "Test multiplier C"),
    V2_TEST_MULTIPLIER_C_DESCRIPTION("upl.property.v2.test.multiplierC.description", "Test multiplier C"),
    V2_TEST_MULTIPLIER_D("upl.property.v2.test.multiplierD", "Test multiplier D"),
    V2_TEST_MULTIPLIER_D_DESCRIPTION("upl.property.v2.test.multiplierD.description", "Test multiplier D"),
    V2_TEST_APPLICATION_STATUS_VARIANT("upl.property.v2.test.applicationStatusVariant", "Application status variant"),
    V2_TEST_APPLICATION_STATUS_VARIANT_DESCRIPTION("upl.property.v2.test.applicationStatusVariant.description", "Application status variant"),
    V2_TEST_INITIAL_RF_COMMAND("upl.property.v2.test.initialRfCommand", "Initial RF command"),
    V2_TEST_INITIAL_RF_COMMAND_DESCRIPTION("upl.property.v2.test.initialRfCommand.description", "Initial RF command"),
    V2_TEST_ROUND_DOWN_TO_NEAREST_INTERVAL("upl.property.v2.test.roundDownToNearestInterval", "Round down to nearest interval"),
    V2_TEST_ROUND_DOWN_TO_NEAREST_INTERVAL_DESCRIPTION("upl.property.v2.test.roundDownToNearestInterval.description", "Round down to nearest interval"),
    V2_TEST_ENABLE_MULTI_FRAME_MODE("upl.property.v2.test.enableMultiFrameMode", "Enable multiframe mode"),
    V2_TEST_ENABLE_MULTI_FRAME_MODE_DESCRIPTION("upl.property.v2.test.enableMultiFrameMode.description", "Enable multiframe mode"),
    V2_TEST_WAVENIS_BUBBLE_UP_INFO("upl.property.v2.test.wavenisBubbleUpInfo", "Wavenis bubble up info"),
    V2_TEST_WAVENIS_BUBBLE_UP_INFO_DESCRIPTION("upl.property.v2.test.wavenisBubbleUpInfo.description", "Wavenis bubble up info"),
    V2_EICT_REQUEST_AUTHENTCATED_FRAME_COUNTER("upl.property.v2.eict.requestAuthenticatedFrameCounter", "Request authenticated framecounter"),
    V2_USE_CACHED_FRAME_COUNTER("upl.property.v2.eict.USE_CACHED_FRAME_COUNTER", "Use cached frame counter"),
    V2_INITIAL_FRAME_COUNTER("upl.property.v2.eict.INITIAL_FRAME_COUNTER", "Initial frame counter"),
    V2_VALIDATE_CACHED_FRAMECOUNTER("upl.property.v2.eict.VALIDATE_CACHED_FRAMECOUNTER", "Validate cached frame counter"),
    V2_FRAME_COUNTER_RECOVERY_STEP("upl.property.v2.eict.FRAME_COUNTER_RECOVERY_STEP", "Frame counter recovery step"),
    V2_FRAME_COUNTER_RECOVERY_RETRIES("upl.property.v2.eict.FRAME_COUNTER_RECOVERY_RETRIES", "Frame counter recovery retries"),
    V2_DEFAULT_BACKLOG_LOADPROFILE("upl.property.v2.eict.DEFAULT_BACKLOG_LOADPROFILE", "Default backlog load profile"),
    V2_DEFAULT_BACKLOG_EVENTLOG("upl.property.v2.eict.DEFAULT_BACKLOG_EVENTLOG", "Default backlog event log"),
    V2_DEFAULT_BUFFERSIZE_REGISTERS("upl.property.v2.eict.DEFAULT_BUFFERSIZE_REGISTERS", "Default buffer size registers"),
    V2_READ_OLD_OBIS_CODES("upl.property.v2.eict.READ_OLD_OBIS_CODES", "Read old obis codes"),
    V2_EICT_REQUEST_AUTHENTCATED_FRAME_COUNTER_DESCRIPTION("upl.property.v2.eict.requestAuthenticatedFrameCounter.description", "Request authenticated framecounter"),
    V2_EICT_POLLING_DELAY("upl.property.v2.eict.pollingDelay", "Polling delay"),
    V2_EICT_POLLING_DELAY_DESCRIPTION("upl.property.v2.eict.pollingDelay.description", "Polling delay"),
    V2_EICT_CALLING_AP_TITLE("upl.property.v2.eict.callingApTitle", "Calling AP title"),
    V2_EICT_CALLING_AP_TITLE_DESCRIPTION("upl.property.v2.eict.callingApTitl.descriptione", "Calling AP title"),
    V2_EICT_GENERAL_CIPHERING_KEY_TYPE("upl.property.v2.eict.generalCipheringKeyType", "General ciphering keytype"),
    V2_EICT_GENERAL_CIPHERING_KEY_TYPE_DESCRIPTION("upl.property.v2.eict.generalCipheringKeyType.description", "General ciphering keytype"),
    V2_EICT_DLMS_WAN_KEK("upl.property.v2.eict.dlmsWanKek", "DLMS WAN KEK"),
    V2_EICT_DLMS_WAN_KEK_DESCRIPTION("upl.property.v2.eict.dlmsWanKek.description", "DLMS WAN KEK"),
    V2_EICT_READCACHE("upl.property.v2.eict.readCache", "Read cache"),
    V2_EICT_READCACHE_DESCRIPTION("upl.property.v2.eict.readCache.description", "Read cache"),
    V2_EICT_DLMS_METER_KEK("upl.property.v2.eict.dlmsMeterKek", "DLMS meter KEK"),
    V2_EICT_DLMS_DEVICE_SYSTEM_TITLE("upl.property.v2.eict.deviceSystemTitle", "Device system title"),
    V2_EICT_DLMS_METER_KEK_DESCRIPTION("upl.property.v2.eict.dlmsMeterKek.description", "DLMS meter KEK"),
    V2_EICT_PSK_ENCRYPTION_KEY("upl.property.v2.eict.pskEncryptionKey", "PSK encryption key"),
    V2_EICT_PSK_ENCRYPTION_KEY_DESCRIPTION("upl.property.v2.eict.pskEncryptionKey.description", "PSK encryption key"),
    V2_EICT_CIPHERING_TYPE("upl.property.v2.eict.cipheringType", "Ciphering type"),
    V2_EICT_CIPHERING_TYPE_DESCRIPTION("upl.property.v2.eict.cipheringType.description", "Ciphering type"),
    V2_EICT_CLIENT_PRIVATE_SIGNING_KEY("upl.property.v2.eict.clientPrivateSigningKey", "Client private signing key"),
    V2_EICT_CLIENT_PRIVATE_KEY_AGREEMENT_KEY("upl.property.v2.eict.clientPrivateKeyAgreementKey", "Client private key agreement key"),
    V2_EICT_SERVER_TLS_CERTIFICATE("upl.property.v2.eict.serverTLSCertificate", "Server TLS Certificate"),
    V2_BROADCAST_AUTHENTICATION_KEY("upl.property.v2.eict.broadcastAuthenticationKey", "Broadcast authentication Key"),
    V2_BROADCAST_ENCRYPTION_KEY("upl.property.v2.eict.broadcastEncryptionKey", "Broadcast encryption Key"),
    V2_INCREMENT_FRAMECOUNTER_FOR_REPLY_TO_HLS("upl.property.v2.eict.incrementFrameCounterForReplyToHLS", "Increment frame counter when replying to HLS"),
    IPV6_ADDRESS_AND_PREFIX_LENGTH("upl.property.v2.eict.IPV6_ADDRESS_AND_PREFIX_LENGTH", "IPv6 address and prefix length"),
    EEK_STORAGE_LABEL("upl.property.v2.eict.EEK_STORAGE_LABEL", "Ephemeral encryption key storage label");

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


