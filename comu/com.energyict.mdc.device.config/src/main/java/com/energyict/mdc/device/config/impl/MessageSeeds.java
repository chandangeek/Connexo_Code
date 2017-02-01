/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.config.DeviceConfigurationService;

import aQute.bnd.annotation.ProviderType;

import java.util.logging.Level;

/**
 * Defines all the {@link MessageSeed}s of the device config module.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-29 (16:04)
 */
@ProviderType
public enum MessageSeeds implements MessageSeed {
    VETO_LOGBOOKTYPE_DELETION(998, "logBookType.XstillInUseByDeviceTypesY", "The logbook type {0} is still used by the following device types: {1}"),
    VETO_DEVICEPROTOCOLPLUGGABLECLASS_DELETION(999, "deviceProtocolPluggableClass.XstillInUseByDeviceTypesY", "The device protocol pluggable class {0} is still used by the following device types: {1}"),
    NAME_IS_REQUIRED(1000, Keys.NAME_REQUIRED, "The name is required"),
    NAME_IS_UNIQUE(1001, Keys.NAME_UNIQUE, "Name must be unique"),
    FIELD_TOO_LONG(1003, Keys.FIELD_TOO_LONG, "Field length must not exceed {max} characters"),
    FIELD_IS_REQUIRED(1004, Keys.FIELD_IS_REQUIRED, "This field is required"),
    DUPLICATE_DEVICE_MESSAGE_FILE_IN_DEVICE_TYPE(1005, "deviceType.deviceMessageFile.duplicate", "Another device message file with the name ''{0}'' already added to the device type ''{1}''"),
    MAX_FILE_SIZE_EXCEEDED(1006, Keys.MAX_FILE_SIZE_EXCEEDED, "File size should be less than {0} MB"),
    REGISTER_GROUP_NAME_IS_REQUIRED(1501, "registerGroup.name.required", "The name of a register group is required"),
    READING_TYPE_ALREADY_EXISTS(2002, Keys.READING_TYPE_ALREADY_EXISTS, "Reading type {0} already exists"),
    DEFAULT_PRODUCT_SPEC_CANNOT_BE_DELETED(2003, "productSpec.cannotDeleteDefault", "The default product spec can''t be removed"),
    MEASUREMENT_TYPE_OBIS_CODE_CANNOT_BE_UPDATED(3006, "measurementType.cannotUpdateObisCode", "The obis code of the measurement type \"{0}\" can''t be updated because it is in use"),
    REGISTER_TYPE_STILL_USED_BY_REGISTER_SPEC(3008, "measurementType.usedBy.registerSpec", "The register type {0} can''t be removed because it is still in use by the following register configuration(s): {1}"),
    CHANNEL_TYPE_STILL_USED_BY_CHANNEL_SPEC(3009, "measurementType.usedBy.channelSpec", "The register type {0} can''t be removed because it is still in use by the following channel configuration(s): {1}"),
    REGISTER_TYPE_STILL_USED_BY_DEVICE_TYPE(3011, "measurementType.usedBy.deviceType", "The register type {0} can''t be removed because it is still in use by the following device configuration(s): {1}"),
    LOAD_PROFILE_TYPE_OBIS_CODE_CANNOT_BE_UPDATED(4009, "loadProfileType.cannotUpdateObisCode", "The obis code of the load profile type \"{0}\" can''t be updated because it is in use"),
    LOAD_PROFILE_TYPE_INTERVAL_CANNOT_BE_UPDATED(4010, "loadProfileType.cannotUpdateInterval", "The interval of the load profile type \"{0}\" can''t be updated because it is in use"),
    LOAD_PROFILE_TYPE_STILL_IN_USE_BY_LOAD_PROFILE_SPECS(4011, "loadProfileType.XstillInUseByLoadProfileSpecsY", "The load profile type with reading type {0} can''t be removed because it is still in use by the following load profile spec(s): {1}"),
    LOAD_PROFILE_TYPE_STILL_IN_USE_BY_DEVICE_TYPES(4012, "loadProfileType.XstillInUseByDeviceTypesY", "The load profile type {0} can''t be removed because it is still in use by the following device type(s): {1}"),
    LOG_BOOK_TYPE_OBIS_CODE_CANNOT_BE_UPDATED(5000, "logBookType.cannotUpdateObisCode", "The obis code of the log book type \"{0}\" can''t be updated because it is in use"),
    LOG_BOOK_TYPE_STILL_IN_USE_BY_LOG_BOOK_SPECS(5001, "logBookType.XstillInUseByLogBookSpecsY", "The log book type {0} can''t be removed because it is still in use by the following log book spec(s): {1}"),
    REGISTER_SPEC_OVERFLOW_DECREASED(6002, Keys.REGISTER_SPEC_OVERFLOW_DECREASED, "The overflow value can''t be decreased"),
    REGISTER_SPEC_REGISTER_TYPE_IS_REQUIRED(6003, Keys.REGISTER_SPEC_REGISTER_TYPE_IS_REQUIRED,"The register type of a register configuration is required"),
    REGISTER_SPEC_NUMBER_OF_FRACTION_DIGITS_DECREASED(6004, Keys.REGISTER_SPEC_NUMBER_OF_FRACTION_DIGITS_DECREASED, "The number of fraction digits can''t be decreased"),
    REGISTER_SPEC_OVERFLOW_LARGER_THAN_ZERO(6006, "registerSpec.overflow.invalidValue","The provided overflow value must be larger then zero (0))"),
    REGISTER_SPEC_OVERFLOW_INCORRECT_FRACTION_DIGITS(6007, Keys.REGISTER_SPEC_OVERFLOW_INCORRECT_FRACTION_DIGITS, "The provided overflow value \"{0}\" more fraction digits \"{1}\" than provided \"{2}\""),
    REGISTER_SPEC_CANNOT_DELETE_FOR_ACTIVE_CONFIG(6009, "registerSpec.delete.active.config","It is not allowed to remove a register configuration from an active device configuration"),
    REGISTER_SPEC_CANNOT_ADD_TO_ACTIVE_CONFIG(6010, "registerSpec.add.active.config","You can''t add a register configuration to an active device configuration"),
    REGISTER_SPEC_REGISTER_TYPE_IS_NOT_ON_DEVICE_TYPE(6011, "registerSpec.not.deviceType","The register configuration contains a register type {0} which is not configured on the device type"),
    REGISTER_SPEC_REGISTER_TYPE_CAN_NOT_CHANGE_FOR_ACTIVE_CONFIG(6012, Keys.REGISTER_SPEC_REGISTER_TYPE_ACTIVE_DEVICE_CONFIG,"The register type can''t be modified if the device configuration is active"),
    REGISTER_SPEC_NUMBER_OF_FRACTION_DIGITS_LARGER_THAN_ONE(6014, Keys.REGISTER_SPEC_INVALID_NUMBER_OF_FRACTION_DIGITS, "Invalid number of fraction digits."),
    REGISTER_SPEC_OVERFLOW_INVALID(6015, Keys.REGISTER_SPEC_INVALID_OVERFLOW_VALUE, "Invalid overflow value, must be above larger than 0 and smaller than " + Integer.MAX_VALUE),
    REGISTER_SPEC_DUPLICATE_REGISTER_TYPE(6016, Keys.REGISTER_SPEC_DUPLICATE_REGISTER_TYPE, "Device configuration already has a register configuration with given reading type."),
    DEVICE_TYPE_NAME_IS_REQUIRED(7001, "deviceType.name.required", "The name of a device type is required"),
    DEVICE_TYPE_STILL_HAS_ACTIVE_CONFIGURATIONS(7003, Keys.DEVICE_TYPE_XSTILL_HAS_ACTIVE_CONFIGURATIONS, "The device type {0} can''t be removed because it still has active configurations"),
    DEVICE_PROTOCOL_IS_REQUIRED(7004, Keys.DEVICE_PROTOCOL_IS_REQUIRED, "The protocol of a device type is required"),
    DUPLICATE_LOAD_PROFILE_TYPE_IN_DEVICE_TYPE(7005, "deviceType.loadProfileType.duplicate", "The load profile type ''{0}'' was already added to the device type ''{1}''"),
    DUPLICATE_LOG_BOOK_TYPE_IN_DEVICE_TYPE(7006, "deviceType.logBookType.duplicate", "The logbook type ''{0}'' was already added to the device type ''{1}''"),
    DEVICE_PROTOCOL_CANNOT_CHANGE_WITH_EXISTING_CONFIGURATIONS(7007, Keys.DEVICE_PROTOCOL_CANNOT_CHANGE_WITH_EXISTING_CONFIGURATIONS, "The protocol of a device type can''t change when the device type has configurations"),
    DUPLICATE_REGISTER_TYPE_IN_DEVICE_TYPE(7008, "deviceType.registerType.duplicate", "The register type ''{0}'' was already added to the device type ''{1}''"),
    DEVICE_LIFE_CYCLE_REQUIRED(7009, Keys.DEVICE_LIFE_CYCLE_REQUIRED, "Device life cycle is required"),
    CANNOT_CHANGE_DEVICE_TYPE_PURPOSE_WHEN_CONFIGS(7011, Keys.CANNOT_CHANGE_DEVICE_TYPE_PURPOSE_WHEN_CONFIGS, "You can''t change the type of the device type when you already have device configurations defined"),
    DATALOGGER_SLAVE_NO_LOGBOOKTYPE_SUPPORT(7012, Keys.DATALOGGER_SLAVE_NO_LOGBOOKTYPE_SUPPORT, "A datalogger slave device type does not support logbook types"),
    CANNOT_CHANGE_DEVICE_TYPE_PURPOSE_TO_DATALOGGER_SLAVE_WHEN_LOGBOOK_SPECS_EXIST(7013, Keys.CANNOT_CHANGE_DEVICE_TYPE_PURPOSE_TO_DATALOGGER_SLAVE_WHEN_LOGBOOK_SPECS_EXIST, "You can''t change the type of the device type when you already have device configurations with a logbook configuration"),
    DATALOGGER_SLAVE_NO_PROTOCOL_PLUGGABLE_CLASS(7014, Keys.DATALOGGER_SLAVE_NO_PROTOCOL_PLUGGABLE_CLASS, "A datalogger slave device type does not support a device protocol pluggable class"),
    DATALOGGER_SLAVE_NO_LOGBOOKSPEC_SUPPORT(7015, Keys.DATALOGGER_SLAVE_NO_LOGBOOKSPEC_SUPPORT, "A datalogger slave device configuration does not support logbook configurations"),
    DATALOGGER_ENABLED_CANNOT_CHANGE_ON_ACTIVE_CONFIG(7016, Keys.DATALOGGER_ENABLED_CANNOT_CHANGE_ON_ACTIVE_CONFIG, "You can''t change the datalogger enabled functionality on an active device configuration"),
    LOAD_PROFILE_SPEC_LOAD_PROFILE_TYPE_IS_NOT_ON_DEVICE_TYPE(8002, "loadProfileSpec.cannotAddLoadProfileSpecOfTypeXBecauseDeviceTypeYDoesNotContainIt", "The load profile configuration contains a load profile type {0} which is not configured on the device type"),
    LOAD_PROFILE_SPEC_CANNOT_ADD_TO_ACTIVE_CONFIGURATION(8003, "loadProfileSpec.active.configuration", "You can''t add a load profile configuration to an active device configuration"),
    LOAD_PROFILE_SPEC_CANNOT_CHANGE_DEVICE_CONFIG(8004, "loadProfileSpec.change.configuration", "You can''t change the device configuration of an existing load profile configuration"),
    LOAD_PROFILE_SPEC_CANNOT_CHANGE_LOAD_PROFILE_TYPE(8005, "loadProfileSpec.change.loadProfileType", "You can''t change the load profile type of an existing load profile configuration"),
    LOAD_PROFILE_SPEC_CANNOT_DELETE_FROM_ACTIVE_CONFIG(8006, "loadProfileSpec.cannot.delete.active.config", "It is not allowed to remove a load profile configuration from an active device configuration"),
    LOAD_PROFILE_SPEC_CANNOT_DELETE_STILL_LINKED_CHANNEL_SPECS(8007, "loadProfileSpec.cannot.delete.linked.channel.specs", "Can''t remove load profile configuration because there are still channel specs linked"),
    LOAD_PROFILE_SPEC_LOAD_PROFILE_TYPE_IS_REQUIRED(8008, Keys.LOAD_PROFILE_SPEC_LOAD_PROFILE_TYPE_IS_REQUIRED, "The load profile type of a load profile configuration is required"),
    LOGBOOK_SPEC_DEVICE_CONFIG_IS_REQUIRED(9001, "logBookSpec.deviceConfig.required", "The device configuration of a logbook configuration is required"),
    LOGBOOK_SPEC_LOGBOOK_TYPE_IS_REQUIRED(9002, Keys.LOGBOOK_SPEC_LOGBOOK_TYPE_IS_REQUIRED, "The logbook type of a logbook configuration is required"),
    LOGBOOK_SPEC_LOGBOOK_TYPE_IS_NOT_ON_DEVICE_TYPE(9003, "logBookSpec.cannotAddLogBookSpecOfTypeXBecauseDeviceTypeYDoesNotContainIt", "The logbook configuration contains a logbook type {0} which is not configured on the device type"),
    LOGBOOK_SPEC_CANNOT_CHANGE_DEVICE_CONFIG(9004, "logBookSpec.change.configuration", "You can''t change the device configuration of an existing logbook configuration"),
    LOGBOOK_SPEC_CANNOT_CHANGE_LOGBOOK_TYPE(9005, "logBookSpec.change.logbookType", "You can''t change the logbook type of an existing logbook configuration"),
    LOGBOOK_SPEC_CANNOT_DELETE_FROM_ACTIVE_CONFIG(9006, "logBookSpec.cannot.delete.active.config", "It is not allowed to remove a logbook configuration from an active device configuration"),
    LOGBOOK_SPEC_CANNOT_ADD_TO_ACTIVE_CONFIGURATION(9007, "logBookSpec.cannot.add.active.config", "You can''t add a logbook configuration to an active device configuration"),
    CHANNEL_SPEC_NAME_IS_REQUIRED(11001, "channelSpec.name.required", "The name of the channel configuration is required"),
    CHANNEL_SPEC_CANNOT_ADD_TO_ACTIVE_CONFIGURATION(11003, "channelSpec.active.configuration", "You can''t add a channel configuration to an active device configuration"),
    CHANNEL_SPEC_LOAD_PROFILE_SPEC_IS_NOT_ON_DEVICE_CONFIGURATION(11004, "channelSpec.cannotAddChannelSpecOfTypeXBecauseDeviceConfigYDoesNotContainIt", "The channel configuration is linked to a load profile configuration \"{0}\" which is not configuration on the device type"),
    CHANNEL_SPEC_CANNOT_DELETE_FROM_ACTIVE_CONFIG(11005, "channelSpec.cannot.delete.active.config", "It is not allowed to remove a channel configuration from an active device configuration"),
    CHANNEL_SPEC_CHANNEL_TYPE_IS_REQUIRED(11006, Keys.CHANNEL_SPEC_CHANNEL_TYPE_IS_REQUIRED, "The register type of a channel configuration is required"),
    CHANNEL_SPEC_CHANNEL_TYPE_IS_NOT_IN_LOAD_PROFILE_SPEC(11007, "channelSpec.measurementType.not.configured.loadProfileSpec","The channel configuration \"{0}\" is linked to a register \"{1}\" which is not configured for the linked load profile configuration \"{2}\""),
    CHANNEL_SPEC_CHANNEL_TYPE_IS_NOT_ON_DEVICE_TYPE(11008, "channelSpec.measurementType.not.configured.deviceType","The channel configuration \"{0}\" is linked to a register \"{1}\" which is not configured for the device type \"{2}\""),
    CHANNEL_SPEC_DUPLICATE_CHANNEL_TYPE_IN_LOAD_PROFILE_SPEC(11015, "channelSpec.duplicate.measurementType.deviceconfiguration","The device configuration \"{0}\" already contains a channel configuration \"{1}\" with the given register type \"{2}\""),
    CHANNEL_SPEC_WITHOUT_LOAD_PROFILE_SPEC_INTERVAL_IS_REQUIRED(11016, "channelSpec.interval.required.loadProfileSpec","The interval of a channel configuration is required when no load profile configuration is defined"),
    CHANNEL_SPEC_INVALID_INTERVAL_COUNT(11017, "channelSpec.interval.invalid.count","The amount in the interval of a channel configuration should be larger than zero, but was \"{0}\""),
    CHANNEL_SPEC_INVALID_INTERVAL_COUNT_LARGE_UNIT(11018, "channelSpec.interval.invalid.count.large.unit","The amount in the interval of a channel configuration should be '1' if the interval unit is larger than 'hours', but was \"{0}\""),
    CHANNEL_SPEC_INTERVAL_IN_WEEKS_IS_NOT_SUPPORTED(11019, "channelSpec.interval.notsupported.weeks", "The interval of a channel configuration can''t be expressed in number of weeks"),
    CHANNEL_SPEC_CANNOT_CHANGE_DEVICE_CONFIG(11020, "channelSpec.change.configuration", "You can''t change the device configuration of an existing channel configuration"),
    CHANNEL_SPEC_CANNOT_CHANGE_CHANNEL_TYPE(11023, "channelSpec.change.measurementType", "You can''t change the register type of an existing channel configuration"),
    CHANNEL_SPEC_CANNOT_CHANGE_LOAD_PROFILE_SPEC(11024, "channelSpec.change.loadProfileSpec", "You can''t change the load profile configuration of an existing channel configuration"),
    CHANNEL_SPEC_NUMBER_OF_FRACTION_DIGITS_LARGER_THAN_ONE(11025, Keys.CHANNEL_SPEC_INVALID_NUMBER_OF_FRACTION_DIGITS, "Invalid number of fraction digits."),
    CHANNEL_SPEC_OVERFLOW_VALUE_INVALID(11026, Keys.CHANNEL_SPEC_INVALID_OVERFLOW_VALUE, "Invalid overflow value, must be larger than 0 and smaller than " + Integer.MAX_VALUE),
    DEVICE_CONFIGURATION_NAME_IS_REQUIRED(12001, "deviceConfig.name.required", "The name of the device configuration is required"),
    DEVICE_CONFIGURATION_DEVICE_TYPE_IS_REQUIRED(12002, "deviceConfig.deviceType.required", "The device type of the device configuration is required"),
    DEVICE_CONFIGURATION_IS_ACTIVE_CAN_NOT_DELETE(12003, "deviceConfig.active", "You can''t remove an active device configuration"),
    DEVICE_CONFIGURATION_DUPLICATE_LOAD_PROFILE_TYPE_IN_SPEC(12004, "deviceConfig.duplicate.loadProfileType", "The device configuration \"{0}\" already contains a load profile configuration with the load profile type \"{1}\""),
    DEVICE_CONFIGURATION_DUPLICATE_OBIS_CODE_FOR_LOAD_PROFILE_SPEC(12005, "deviceConfig.duplicate.obisCode.loadProfileSpec", "The device configuration \"{0}\" already contains a load profile configuration with the OBIS code \"{1}\""),
    DEVICE_CONFIGURATION_DUPLICATE_LOG_BOOK_TYPE_IN_SPEC(12006, "deviceConfig.duplicate.logBookType", "The device configuration \"{0}\" already contains a logbook configuration with the logbook type \"{1}\""),
    DEVICE_CONFIGURATION_DUPLICATE_OBIS_CODE_FOR_LOGBOOK_SPEC(12007, "deviceConfig.duplicate.obisCode.logBookSpec", "The device configuration \"{0}\" can''t contain multiple logbook configurations with the OBIS code \"{1}\""),
    DEVICE_CONFIGURATION_DUPLICATE_OBIS_CODE_FOR_REGISTER_SPEC(12008, "deviceConfig.duplicate.obisCode.registerSpec", "The device configuration \"{0}\" already contains a register configuration with the OBIS code \"{1}\""),
    DEVICE_CONFIGURATION_DUPLICATE_OBIS_CODE_FOR_CHANNEL_SPEC_IN_LOAD_PROFILE_SPEC(12009, "deviceConfig.duplicate.obisCode.channelSpec.loadProfileSpec", "Load profile configuration \"{0}\" in device configuration \"{1}\" already contains a channel configuration with the OBIS code \"{2}\""),
    DEVICE_CONFIGURATION_DUPLICATE_OBIS_CODE_FOR_CHANNEL_SPEC(12010, "deviceConfig.duplicate.obisCode.channelSpec", "The device configuration \"{0}\" already contains a channel configuration with the OBIS code \"{1}\""),
    DEVICE_CONFIGURATION_CAN_NOT_BE_GATEWAY(12012, Keys.DEVICE_CONFIG_GATEWAY_NOT_ALLOWED, "The device configuration can''t be gateway as the device protocol does not allow it"),
    DEVICE_CONFIGURATION_CAN_NOT_BE_DIRECTLY_ADDRESSED(12013, Keys.DEVICE_CONFIG_DIRECT_ADDRESS_NOT_ALLOWED, "The device configuration can''t be directly addressable as the device protocol does not allow it"),
    DEVICE_CONFIGURATION_IS_ACTIVE_CAN_NOT_CHANGE_FIELD(12014, Keys.DEVICE_CONFIG_ACTIVE_FIELD_IMMUTABLE, "You can''t change this field for an active configuration"),
    DEVICE_CONFIGURATION_IS_NOT_DIRECTLY_ADDRESSABLE(12015, Keys.DEVICE_CONFIGURATION_IS_NOT_DIRECTLY_ADDRESSABLE, "It is not allowed to create connection methods because the device configuration is not directly addressable"),
    DEVICE_CONFIGURATION_CALCULATED_READINGTYPE_CANNOT_BE_EMPTY(12016, Keys.CALCULATED_READINGTYPE_CANNOT_BE_EMPTY, "The calculated readingtype can''t be empty when you define to use a multiplier"),
    DEVICE_CONFIGURATION_CALCULATED_READINGTYPE_DOES_NOT_MATCH_CRITERIA(12017, Keys.CALCULATED_READINGTYPE_DOES_NOT_MATCH_CRITERIA, "The calculated readingtype does not match the criteria according to the selected value"),
    READINGTYPE_CAN_NOT_BE_MULTIPLIED(12018, Keys.READINGTYPE_CAN_NOT_BE_MULTIPLIED, "Readingtype can''t be multiplied"),
    CANNOT_CHANGE_THE_USAGE_OF_THE_MULTIPLIER_OF_ACTIVE_CONFIG(12019, Keys.CANNOT_CHANGE_THE_USAGE_OF_THE_MULTIPLIER_OF_ACTIVE_CONFIG, "You can''t change the usage of the multiplier of an active configuration"),
    CANNOT_CHANGE_MULTIPLIER_OF_ACTIVE_CONFIG(12020, Keys.CANNOT_CHANGE_MULTIPLIER_OF_ACTIVE_CONFIG, "You can''t change the multiplier readingtype of an active configuration"),
    NEXT_EXECUTION_SPECS_TEMPORAL_EXPRESSION_REQUIRED(13000, Keys.NEXT_EXECUTION_SPECS_TEMPORAL_EXPRESSION_REQUIRED, "The temporal expression of a NextExecutionSpec is required"),
    TEMPORAL_EXPRESSION_FREQUENCY_REQUIRED(13001, Keys.TEMPORAL_EXPRESSION_FREQUENCY_REQUIRED, "The frequency of a temporal expression is required"),
    TEMPORAL_EXPRESSION_UNKNOWN_UNIT(13002, Keys.TEMPORAL_EXPRESSION_UNKNOWN_UNIT, "The unit {0} is unknown or unsupported for temporal expressions"),
    TEMPORAL_EXPRESSION_FREQUENCY_MUST_BE_STRICTLY_POSITIVE(13003, Keys.TEMPORAL_EXPRESSION_FREQUENCY_MUST_BE_STRICTLY_POSITIVE, "The frequency value of a temporal expression must be a strictly positive number"),
    TEMPORAL_EXPRESSION_OFFSET_MUST_BE_POSITIVE(13004, Keys.TEMPORAL_EXPRESSION_OFFSET_MUST_BE_POSITIVE, "The offset value of a temporal expression must be a positive number"),
    PROTOCOL_DIALECT_REQUIRED(13006, Keys.PROTOCOLDIALECT_REQUIRED, "The protocol dialect name is required for a protocolDialectConfigurationProperties"),
    PROTOCOL_DIALECT_HAS_NO_SUCH_PROPERTY(13007, "protocolDialectConfigurationProperties.noSuchProperty", "The protocol dialect {0} does not have a configuration property with name {1}"),
    PROTOCOL_DIALECT_DUPLICATE(13008, Keys.PROTOCOLDIALECT_CONF_PROPS_DUPLICATE, "A dialect configuration properties having device configuration  \"{0}\" and device protocol dialect \"{1}\" already exists."),
    PROTOCOLDIALECT_CONF_PROPS_MISSING_REQUIRED(13009, Keys.PROTOCOLDIALECT_CONF_PROPS_MISSING_REQUIRED, "The protocol dialect {0} is missing required configuration property with name {1}"),
    PROTOCOL_DIALECT_NAME_DOES_NOT_EXIST(13013, "protocolConfigurationProperties.doesNotExistName", "A protocolDialectConfigurationProperties with name {0} does not exist"),
    PROTOCOL_DIALECT_ID_DOES_NOT_EXIST(13014, "protocolConfigurationProperties.doesNotExistId", "A protocolDialectConfigurationProperties with id {0} does not exist"),
    PROTOCOLDIALECT_CONF_PROPS_CANT_DROP_REQUIRED(13015, "protocolDialectConfigurationProperties.cannotDropRequired", "ProtocolDialectConfigurationProperties {0} can''t drop property {1} since it is required."),
    PROTOCOLDIALECT_CONF_PROPS_IN_USE(13016, "protocolDialectConfigurationProperties.inUse", "ProtocolDialectConfigurationProperties ''{0}'' of device configuration ''{1}'' can''t be removed because they are still in use"),
    PROTOCOL_INVALID_NAME(13017,"deviceType.no.such.protocol", "A protocol with name {0} does not exist"),
    PROTOCOL_HAS_NO_SUCH_PROPERTY(13018, "protocolConfigurationProperties.noSuchProperty", "The protocol {0} does not have a configuration property with name {1}"),
    UNSUPPORTED_SECURITY_LEVEL(13030, Keys.UNSUPPORTED_SECURITY_LEVEL, "Security level is not supported"),
    SECURITY_PROPERTY_SET_IN_USE(13031, Keys.SECURITY_PROPERTY_SET_IN_USE, "Security property set ''{0}'' of device configuration ''{1}'' is still in use"),
    INCORRECT_CONNECTION_TYPE_FOR_CONNECTION_METHOD(13032, Keys.INCORRECT_CONNECTION_TYPE_FOR_CONNECTION_METHOD, "The connection type has an incorrect direction."),
    NEXT_EXECUTION_SPEC_FORBIDDEN_FOR_ASAP(13033, Keys.NEXT_EXECUTION_SPEC_NOT_ALLOWED_FOR_ASAP, "Next execution spec is not allowed for connection strategy ''as soon as possible''."),
    COM_TASK_ENABLEMENT_COM_TASK_REQUIRED(14000, Keys.COM_TASK_ENABLEMENT_COM_TASK_REQUIRED, "You need to specify the communication task that you want to enable on a device configuration"),
    COM_TASK_CAN_ONLY_BE_ENABLED_ONCE(14001, Keys.COM_TASK_CAN_ONLY_BE_ENABLED_ONCE, "A communication task can only be enabled once per device configuration"),
    COM_TASK_ENABLEMENT_CONFIGURATION_REQUIRED(14002, Keys.COM_TASK_ENABLEMENT_CONFIGURATION_REQUIRED, "You need to specify the device configuration on which you want to enable the communication task"),
    COM_TASK_ENABLEMENT_SECURITY_PROPERTY_SET_REQUIRED(14003, Keys.COM_TASK_ENABLEMENT_SECURITY_PROPERTY_SET_REQUIRED, "You need to specify a security property set to enable a communication task on a device configuration"),
    COM_TASK_ENABLEMENT_CANNOT_USE_DEFAULT_AND_PARTIAL_CONNECTION_TASK(14004, Keys.COM_TASK_ENABLEMENT_CANNOT_USE_DEFAULT_AND_PARTIAL_CONNECTION_TASK, "When a partial connection task is specified, you can''t use the default"),
    COM_TASK_ENABLEMENT_PRIORITY_RANGE(14005, Keys.COM_TASK_ENABLEMENT_PRIORITY_RANGE, "The priority of a communication task enablement should be between {min} and {max}"),
    COM_TASK_ENABLEMENT_PROTOCOL_DIALECT_CONFIGURATION_PROPERTIES_MUST_BE_FROM_SAME_CONFIGURATION(14006, Keys.COM_TASK_ENABLEMENT_PROTOCOL_DIALECT_CONFIGURATION_PROPERTIES_MUST_BE_FROM_SAME_CONFIGURATION, "The protocol dialect properties must be from the same device configuration"),
    COM_TASK_ENABLEMENT_SECURITY_PROPERTY_SET_MUST_BE_FROM_SAME_CONFIGURATION(14007, Keys.COM_TASK_ENABLEMENT_SECURITY_PROPERTY_SET_MUST_BE_FROM_SAME_CONFIGURATION, "The protocol dialect properties must be from the same device configuration"),
    COM_TASK_ENABLEMENT_DOES_NOT_EXIST(14008, Keys.COM_TASK_ENABLEMENT_DOES_NOT_EXIST, "The communication task ''{0}'' is not enabled on the device configuration ''{1}'' and can therefore not be disabled"),
    INCORRECT_GATEWAY_TYPE(14009, Keys.INCORRECT_GATEWAY_TYPE, "You must specify the gateway type if your configuration can act as gateway"),
    VETO_COMTASK_DELETION(14010, "comTaskXstillInUse", "ComTask {0} is still in use by at least one device configuration"),
    VETO_CALENDAR_DELETION(14011, "calendarXStillInUse", "Calendar {0} is still in use by at least one device type"),
    PARTIAL_CONNECTION_TASK_NAME_DOES_NOT_EXIST(15001, "partialConnectionTask.doesNotExistName", "There is no Partial Connection Task by name {0}"),
    PARTIAL_CONNECTION_TASK_ID_DOES_NOT_EXIST(15002, "partialConnectionTask.doesNotExistId", "There is no Partial Connection Task with id {0}"),
    PARTIAL_CONNECTION_TASK_PROPERTY_HAS_NO_SPEC(15003, Keys.PARTIAL_CONNECTION_TASK_PROPERTY_HAS_NO_SPEC, "There is no spec for connection type property with name {0}"),
    PARTIAL_CONNECTION_TASK_PROPERTY_VALUE_OF_WRONG_TYPE(15004, Keys.PARTIAL_CONNECTION_TASK_PROPERTY_VALUE_OF_WRONG_TYPE, "The value for property {0} is of the wrong type."),
    CONNECTION_STRATEGY_REQUIRED(15005, Keys.CONNECTION_STRATEGY_REQUIRED, "Connection strategy is required"),
    NEXT_EXECUTION_SPEC_REQUIRED_FOR_MINIMIZE_CONNECTIONS(15006, Keys.NEXT_EXECUTION_SPEC_REQUIRED_FOR_MINIMIZE_CONNECTIONS, "Next Execution Spec is required for OutboundConnectionTasks that minimize connections."),
    NEXT_EXECUTION_SPEC_INVALID_FOR_COM_WINDOW_KEY(15007, Keys.NEXT_EXECUTION_SPEC_INVALID_FOR_COM_WINDOW, "Next Execution Spec is invalid for Communication Window."),
    NEXT_EXECUTION_SPEC_OFFSET_IS_GREATER_THAN_FREQUENCY(15008, Keys.NEXT_EXECUTION_SPEC_OFFSET_IS_GREATER_THAN_FREQUENCY, "Next Execution Spec's offset is greater than its frequency."),
    RETRY_DELAY_MUST_BE_HIGHER(15009, Keys.RETRY_DELAY_MUST_BE_HIGHER, "Retry delay must be higher than 1 minute."),
    VETO_CONNECTIONTYPE_PLUGGABLECLASS_DELETION(15010, "connectionTypePluggableClass.XstillInUseByY", "ConnectionType Pluggable Class {0} is still in use by {1}"),
    VETO_COMPORTPOOL_DELETION(15011, "comPortPoolXstillInUseByY", "ComPortPool {0} is still in use by connection method(s): {1}"),
    DESTINATION_DATA_SOURCE_IS_EMPTY(15012, Keys.DESTINATION_DATA_SOURCE_IS_EMPTY, "The value of the destination data source is empty while a 'map' solution was defined"),
    MULTIPLE_SOLUTIONS_FOR_SAME_CONFLICT(15013, Keys.MULTIPLE_SOLUTIONS_FOR_SAME_CONFLICT, "There are multiple solutions defined for the same device config change conflict"),
    DATALOGGER_SLAVES_AT_LEAST_ONE_DATASOURCE(15014, Keys.DATALOGGER_SLAVES_AT_LEAST_ONE_DATASOURCE, "You must define at least one channel configuration or register configuration before you can " +
            "activate a datalogger slave device configuration"),
    DATALOGGER_ENABLEMENTS_AT_LEAST_ONE_DATASOURCE(15015, Keys.DATALOGGER_ENABLEMENTS_AT_LEAST_ONE_DATASOURCE, "You must define at least one channel configuration or register configuration before " +
            "you can activate a datalogger device configuration"),
    INVALID_NUMBER_OF_SIMULTANEOUS_CONNECTIONS(15016, Keys.INVALID_NUMBER_OF_SIMULTANEOUS_CONNECTIONS, "Invalid number of simultaneous connections, should be between 1 and 16"),
    DEVICE_CONFIG_DIRECTLY_ADDRESSABLE_WHEN_CONNECTIONTASKS(15017, Keys.DEVICE_CONFIG_DIRECTLY_ADDRESSABLE_WHEN_CONNECTIONTASKS, "You can''t change this property because you already have connection tasks"),
    CONNECTION_TASK_USED_BY_COMTASK_ENABLEMENT(15018, Keys.CONNECTION_TASK_USED_BY_COMTASK_ENABLEMENT, "You can''t remove the connection method because it is used by a communication task"),
    TIME_OF_USE_CALENDAR_ONLY(15019, Keys.TIME_OF_USE_CALENDAR_ONLY, "You can only add Time of Use calendars to a device type"),
    ;

    private final int number;
    private final String key;
    private final String defaultFormat;
    private final Level level;

    MessageSeeds(int number, String key, String defaultFormat) {
        this(number, key, defaultFormat, Level.SEVERE);
    }

    MessageSeeds(int number, String key, String defaultFormat, Level level) {
        this.number = number;
        this.key = key;
        this.defaultFormat = defaultFormat;
        this.level = level;
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
        return level;
    }

    @Override
    public String getModule() {
        return DeviceConfigurationService.COMPONENTNAME;
    }

    public static class Keys {
        public static final String NAME_REQUIRED = "X.name.required";
        public static final String NAME_UNIQUE = "X.name.unique";
        public static final String FIELD_TOO_LONG = "fieldTooLong";
        public static final String FIELD_IS_REQUIRED = "field.required";
        public static final String MAX_FILE_SIZE_EXCEEDED = "MaxFileSizeExceeded";
        public static final String INVALID_NUMBER_OF_SIMULTANEOUS_CONNECTIONS = "InvalidNumberOfSimultaneousConnections";
        public static final String DEVICE_TYPE_XSTILL_HAS_ACTIVE_CONFIGURATIONS = "deviceType.XstillHasActiveConfigurations";
        public static final String DEVICE_PROTOCOL_IS_REQUIRED = "deviceType.protocol.required";
        public static final String DEVICE_PROTOCOL_CANNOT_CHANGE_WITH_EXISTING_CONFIGURATIONS = "deviceType.protocol.noupdate";
        public static final String LOGBOOK_SPEC_LOGBOOK_TYPE_IS_REQUIRED = "logBookSpec.logbookType.required";
        public static final String LOAD_PROFILE_SPEC_LOAD_PROFILE_TYPE_IS_REQUIRED = "loadProfileSpec.loadProfileType.required";
        public static final String CHANNEL_SPEC_CHANNEL_TYPE_IS_REQUIRED = "channelSpec.channelType.required";
        public static final String REGISTER_SPEC_REGISTER_TYPE_IS_REQUIRED = "registerSpec.registerType.required";
        public static final String READING_TYPE_ALREADY_EXISTS = "productSpec.duplicateReadingTypeX";
        public static final String NEXT_EXECUTION_SPECS_TEMPORAL_EXPRESSION_REQUIRED = "nextExecutionSpecs.temporalExpression.required";
        public static final String TEMPORAL_EXPRESSION_FREQUENCY_REQUIRED = "temporalExpression.every.required";
        public static final String TEMPORAL_EXPRESSION_UNKNOWN_UNIT = "temporalExpression.unknown.unit";
        public static final String TEMPORAL_EXPRESSION_FREQUENCY_MUST_BE_STRICTLY_POSITIVE = "temporalExpression.every.count.positive";
        public static final String TEMPORAL_EXPRESSION_OFFSET_MUST_BE_POSITIVE = "temporalExpression.offset.count.positive";
        public static final String DEVICE_CONFIG_GATEWAY_NOT_ALLOWED = "deviceConfig.gateway.notAllowed";
        public static final String DEVICE_CONFIG_DIRECT_ADDRESS_NOT_ALLOWED = "deviceConfig.directAddress.notAllowed";
        public static final String DEVICE_CONFIGURATION_IS_NOT_DIRECTLY_ADDRESSABLE = "deviceConfig.isnot.directAddressable";
        public static final String REGISTER_SPEC_OVERFLOW_DECREASED = "registerSpec.overFlow.decreased";
        public static final String CHANNEL_SPEC_OVERFLOW_DECREASED = "channel.overFlow.decreased";
        public static final String REGISTER_SPEC_NUMBER_OF_FRACTION_DIGITS_DECREASED = "registerSpec.numberOfFractionDigits.decreased";
        public static final String CHANNEL_SPEC_NUMBER_OF_FRACTION_DIGITS_DECREASED = "channelSpec.numberOfFractionDigits.decreased";
        public static final String REGISTER_SPEC_REGISTER_TYPE_ACTIVE_DEVICE_CONFIG = "registerSpec.measurementType.activeDeviceConfig";
        public static final String PROTOCOLDIALECT_REQUIRED = "protocolDialectConfigurationProperties.dialectName.required";
        public static final String PROTOCOLDIALECT_CONF_PROPS_DUPLICATE = "protocolDialectConfigurationProperties.duplicate";
        public static final String PROTOCOLDIALECT_CONF_PROPS_MISSING_REQUIRED = "protocolDialectConfigurationProperties.missing.required.properties";
        public static final String PARTIAL_CONNECTION_TASK_PROPERTY_HAS_NO_SPEC = "partialConnectionTaskProperty.hasNoSpec";
        public static final String PARTIAL_CONNECTION_TASK_PROPERTY_VALUE_OF_WRONG_TYPE = "partialConnectionTaskProperty.wrongValueType";
        public static final String CONNECTION_STRATEGY_REQUIRED = "partialOutboundConnectionTask.connectionStrategyRequired";
        public static final String NEXT_EXECUTION_SPEC_REQUIRED_FOR_MINIMIZE_CONNECTIONS = "partialOutboundConnectionTask.executionSpecRequiredForMinimizeConnections";
        public static final String NEXT_EXECUTION_SPEC_NOT_ALLOWED_FOR_ASAP = "partialOutboundConnectionTask.executionSpecForbiddenForAsap";
        public static final String NEXT_EXECUTION_SPEC_INVALID_FOR_COM_WINDOW = "partialOutboundConnectionTask.executionSpecInvalidForComWindow";
        public static final String NEXT_EXECUTION_SPEC_OFFSET_IS_GREATER_THAN_FREQUENCY = "nextExecutionSpecs.offsetGreaterThanFrequency";
        public static final String RETRY_DELAY_MUST_BE_HIGHER = "partialScheduledConnectionTask.retryDelayMustBeHigher";
        public static final String REGISTER_SPEC_INVALID_NUMBER_OF_FRACTION_DIGITS = "registerSpec.invalidNumberOfFractionDigits";
        public static final String REGISTER_SPEC_INVALID_OVERFLOW_VALUE = "registerSpec.invalidOverflow";
        public static final String REGISTER_SPEC_DUPLICATE_REGISTER_TYPE = "register.spec.duplicate.register.type";
        public static final String CHANNEL_SPEC_INVALID_NUMBER_OF_FRACTION_DIGITS = "channelSpec.invalidNumberOfFractionDigits";
        public static final String CHANNEL_SPEC_INVALID_OVERFLOW_VALUE = "channelSpec.invalidOverflow";
        public static final String UNSUPPORTED_SECURITY_LEVEL = "securityPropertySet.unsupportedSecurityLevel";
        public static final String SECURITY_PROPERTY_SET_IN_USE = "securityPropertySet.inUse";
        public static final String COM_TASK_ENABLEMENT_COM_TASK_REQUIRED = "comTaskEnablement.comTask.required";
        public static final String COM_TASK_CAN_ONLY_BE_ENABLED_ONCE = "comTaskEnablement.unique";
        public static final String COM_TASK_ENABLEMENT_CONFIGURATION_REQUIRED = "comTaskEnablement.configuration.required";
        public static final String COM_TASK_ENABLEMENT_SECURITY_PROPERTY_SET_REQUIRED = "comTaskEnablement.securityPropertySet.required";
        public static final String COM_TASK_ENABLEMENT_CANNOT_USE_DEFAULT_AND_PARTIAL_CONNECTION_TASK = "comTaskEnablement.cannotUseDefault";
        public static final String COM_TASK_ENABLEMENT_PRIORITY_RANGE = "comTaskEnablement.invalidPriority";
        public static final String COM_TASK_ENABLEMENT_PROTOCOL_DIALECT_CONFIGURATION_PROPERTIES_MUST_BE_FROM_SAME_CONFIGURATION = "comTaskEnablement.protocolDialectConfigurationProperties.fromSameConfiguration";
        public static final String COM_TASK_ENABLEMENT_SECURITY_PROPERTY_SET_MUST_BE_FROM_SAME_CONFIGURATION = "comTaskEnablement.securityPropertySet.fromSameConfiguration";
        public static final String COM_TASK_ENABLEMENT_DOES_NOT_EXIST = "comTaskEnablement.doesNotExist";
        public static final String DEVICE_CONFIG_ACTIVE_FIELD_IMMUTABLE = "deviceConfig.active.field.immutable";
        public static final String INCORRECT_CONNECTION_TYPE_FOR_CONNECTION_METHOD = "incorrect.direction.connection.method";
        public static final String INCORRECT_GATEWAY_TYPE = "incorrect.gateway.type";
        public static final String DEVICE_LIFE_CYCLE_REQUIRED = "deviceType.device.life.cycle.required";
        public static final String REGISTER_SPEC_OVERFLOW_INCORRECT_FRACTION_DIGITS = "registerSpec.overflow.fractionDigits";
        public static final String DESTINATION_DATA_SOURCE_IS_EMPTY = "destinationDataSourceIsEmpty";
        public static final String MULTIPLE_SOLUTIONS_FOR_SAME_CONFLICT = "multipleSolutionsDefinedForSameConfig";
        public static final String CALCULATED_READINGTYPE_CANNOT_BE_EMPTY = "calculatedReadingType.notEmpty.whenMultiplier";
        public static final String CALCULATED_READINGTYPE_DOES_NOT_MATCH_CRITERIA = "calculatedReadingType.does.not.match.criteria";
        public static final String READINGTYPE_CAN_NOT_BE_MULTIPLIED = "readingtype.can.not.be.multiplied";
        public static final String CANNOT_CHANGE_THE_USAGE_OF_THE_MULTIPLIER_OF_ACTIVE_CONFIG = "cannot.change.the.usage.of.multiplier.of.active.config";
        public static final String CANNOT_CHANGE_MULTIPLIER_OF_ACTIVE_CONFIG = "cannot.change.multiplier.of.active.config";
        public static final String CANNOT_CHANGE_DEVICE_TYPE_PURPOSE_WHEN_CONFIGS = "cannot.change.device.type.purpose.when.configs";
        public static final String DATALOGGER_SLAVE_NO_LOGBOOKTYPE_SUPPORT = "datalogger.slave.device.type.not.support.logbook.types";
        public static final String CANNOT_CHANGE_DEVICE_TYPE_PURPOSE_TO_DATALOGGER_SLAVE_WHEN_LOGBOOK_SPECS_EXIST = "datalogger.slave.cannot.change.type.when.already.logbook.specs";
        public static final String DATALOGGER_SLAVE_NO_PROTOCOL_PLUGGABLE_CLASS = "datalogger.slave.no.protocol.pluggable.class";
        public static final String DATALOGGER_SLAVE_NO_LOGBOOKSPEC_SUPPORT = "datalogger.slave.device.config.not.support.logbook.config";
        public static final String DATALOGGER_ENABLED_CANNOT_CHANGE_ON_ACTIVE_CONFIG = "datalogger.enabled.change.on.active.config";
        public static final String DATALOGGER_SLAVES_AT_LEAST_ONE_DATASOURCE = "datalogger.slave.at.least.one.datasource";
        public static final String DATALOGGER_ENABLEMENTS_AT_LEAST_ONE_DATASOURCE = "datalogger.enablements.at.least.one.datasource";
        public static final String DEVICE_CONFIG_DIRECTLY_ADDRESSABLE_WHEN_CONNECTIONTASKS = "deviceConfig.active.directly.addressable.with.connectiontasks";
        public static final String CONNECTION_TASK_USED_BY_COMTASK_ENABLEMENT = "connection.task.used.by.comtask.enablement";
        public static final String TIME_OF_USE_CALENDAR_ONLY = "devicetype.tou.calendar.only";
    }

}