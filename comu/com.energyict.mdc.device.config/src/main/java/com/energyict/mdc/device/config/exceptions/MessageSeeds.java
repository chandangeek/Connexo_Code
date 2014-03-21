package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import java.util.logging.Level;

import static java.util.logging.Level.SEVERE;

/**
 * Defines all the {@link MessageSeed}s of the device config module.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-29 (16:04)
 */
public enum MessageSeeds implements MessageSeed {
    VETO_DEVICEPROTOCOLPLUGGABLECLASS_DELETION(999, "deviceProtocolPluggableClass.XstillInUseByDeviceTypesY", "The device protocol pluggable class {0} is still used by the following device types: {1}", SEVERE),
    DEVICE_TYPE(1, "DTC.deviceType.with.article", "a device type", SEVERE),
    NAME_IS_REQUIRED(1000, Constants.NAME_REQUIRED_KEY, "The name of {0} is required", SEVERE),
    REGISTER_GROUP_NAME_IS_REQUIRED(1501, "registerGroup.name.required", "The name of a register group is required", SEVERE),
    REGISTER_GROUP_ALREADY_EXISTS(1502, "registerGroup.duplicateNameX", "A register group with name '{0}' already exists", SEVERE),
    REGISTER_GROUP_STILL_IN_USE(1503, "registerGroup.XstillInUseByY", "The register group with name '{0}' cannot be deleted because it is still in use by the following register mappings: {1}", SEVERE),
    READING_TYPE_IS_REQUIRED(2001, Constants.READING_TYPE_IS_REQUIRED_KEY, "The reading type of a product spec is required", SEVERE),
    READING_TYPE_ALREADY_EXISTS(2002, Constants.READING_TYPE_ALREADY_EXISTS_KEY, "The product spec with the reading type {0} already exists", SEVERE),
    DEFAULT_PRODUCT_SPEC_CANNOT_BE_DELETED(2003, "productSpec.cannotDeleteDefault", "The default product spec cannot be deleted", SEVERE),
    PRODUCT_SPEC_STILL_IN_USE(2004, "productSpec.XstillInUseByY", "The product spec with reading type {0} cannot be deleted because it is still in use by the following register mappings: {1}", SEVERE),
    REGISTER_MAPPING_NAME_IS_REQUIRED(3001, "registerMapping.name.required", "The name of a register mapping is required", SEVERE),
    REGISTER_MAPPING_ALREADY_EXISTS(3002, "registerMapping.duplicateNameX", "A register mapping with name '{0}' already exists", SEVERE),
    REGISTER_MAPPING_OBIS_CODE_TOU_PEHNOMENON_ALREADY_EXISTS(3003, "registerMapping.duplicateObisCodeX", "A register mapping with obis code '{0}', unit '{1}' and time of use '{2}' already exists", SEVERE),
    REGISTER_MAPPING_OBIS_CODE_IS_REQUIRED(3004, Constants.REGISTER_MAPPING_OBIS_CODE_IS_REQUIRED_KEY, "The obis code of a register mapping is required", SEVERE),
    PRODUCT_SPEC_IS_REQUIRED(3005, Constants.PRODUCT_SPEC_IS_REQUIRED_KEY, "The product spec of a register mapping is required", SEVERE),
    REGISTER_MAPPING_OBIS_CODE_CANNOT_BE_UPDATED(3006, "registerMapping.cannotUpdateObisCode", "The obis code of the register mapping '{0}' cannot be updated because it is in use", SEVERE),
    REGISTER_MAPPING_PHENOMENON_CANNOT_BE_UPDATED(3007, "registerMapping.cannotUpdatePhenomenon", "The phenomenon of the register mapping '{0}' cannot be updated because it is in use", SEVERE),
    REGISTER_MAPPING_STILL_USED_BY_REGISTER_SPEC(3008, "registerMapping.usedBy.registerSpec", "The register mapping {0} cannot be deleted because it is still in use by the following register spec(s): {1}", SEVERE),
    REGISTER_MAPPING_STILL_USED_BY_CHANNEL_SPEC(3009, "registerMapping.usedBy.channelSpec", "The register mapping {0} cannot be deleted because it is still in use by the following channel spec(s): {1}", SEVERE),
    REGISTER_MAPPING_STILL_USED_BY_LOAD_PROFILE_TYPE(3010, "registerMapping.usedBy.loadProfileType", "The register mapping {0} cannot be deleted because it is still in use by the following load profile type(s): {1}", SEVERE),
    REGISTER_MAPPING_STILL_USED_BY_DEVICE_TYPE(3011, "registerMapping.usedBy.deviceType", "The register mapping {0} cannot be deleted because it is still in use by the following device type(s): {1}", SEVERE),
    UNIT_IS_REQUIRED(3013, Constants.UNIT_IS_REQUIRED_KEY, "The unit of a register mapping is required", SEVERE),
    TOME_OF_USE_TOO_SMALL(3014, Constants.TIMEOFUSE_TOO_SMALL, "The time of use must be a positive number", SEVERE),
    LOAD_PROFILE_TYPE_NAME_IS_REQUIRED(4001, "loadProfileType.name.required", "The name of a load profile type is required", SEVERE),
    LOAD_PROFILE_TYPE_ALREADY_EXISTS(4002, "loadProfileType.duplicateNameX", "A load profile type with name '{0}' already exists", SEVERE),
    LOAD_PROFILE_TYPE_INTERVAL_IN_WEEKS_IS_NOT_SUPPORTED(4003, "loadProfileType.interval.notsupported.weeks", "The interval of a load profile type cannot be expressed in number of weeks", SEVERE),
    INTERVAL_IN_DAYS_MUST_BE_ONE(4004, "loadProfileType.interval.notsupported.multipledays", "The number of days of the interval of a load profile type cannot be greater than 1 but got {0}", SEVERE),
    INTERVAL_IN_MONTHS_MUST_BE_ONE(4005, "loadProfileType.interval.notsupported.multiplemonths", "The number of months of the interval of a load profile type cannot be greater than 1 but got {0}", SEVERE),
    INTERVAL_IN_YEARS_MUST_BE_ONE(4006, "loadProfileType.interval.notsupported.multipleyears", "The number of years of the interval of a load profile type cannot be greater than 1 but got {0}", SEVERE),
    INTERVAL_MUST_BE_STRICTLY_POSITIVE(4007, "loadProfileType.interval.notsupported.negative", "The value of the interval of a load profile type must be a strictly positive number and not {0}", SEVERE),
    DUPLICATE_REGISTER_MAPPING_IN_LOAD_PROFILE_TYPE(4008, "loadProfileType.registerMapping.duplicate", "The register mapping {0} was already added to the load profile type {1}", SEVERE),
    LOAD_PROFILE_TYPE_OBIS_CODE_CANNOT_BE_UPDATED(4009, "loadProfileType.cannotUpdateObisCode", "The obis code of the load profile type '{0}' cannot be updated because it is in use", SEVERE),
    LOAD_PROFILE_TYPE_INTERVAL_CANNOT_BE_UPDATED(4010, "loadProfileType.cannotUpdateInterval", "The interval of the load profile type '{0}' cannot be updated because it is in use", SEVERE),
    LOAD_PROFILE_TYPE_STILL_IN_USE_BY_LOAD_PROFILE_SPECS(4011, "loadProfileType.XstillInUseByLoadProfileSpecsY", "The product spec with reading type {0} cannot be deleted because it is still in use by the following load profile spec(s): {1}", SEVERE),
    LOAD_PROFILE_TYPE_STILL_IN_USE_BY_DEVICE_TYPES(4012, "loadProfileType.XstillInUseByDeviceTypesY", "The product spec with reading type {0} cannot be deleted because it is still in use by the following device type(s): {1}", SEVERE),
    LOAD_PROFILE_TYPE_OBIS_CODE_IS_REQUIRED(4013, Constants.LOAD_PROFILE_TYPE_OBIS_CODE_IS_REQUIRED_KEY, "The obis code of a load profile type is required", SEVERE),
    LOAD_PROFILE_TYPE_INTERVAL_IS_REQUIRED(4014, "loadProfileType.interval.required", "The interval of a load profile type is required", SEVERE),
    LOG_BOOK_TYPE_NAME_IS_REQUIRED(5001, "logBookType.name.required", "The name of a log book type is required", SEVERE),
    LOG_BOOK_TYPE_ALREADY_EXISTS(5002, "logBookType.duplicateNameX", "A log book type with name '{0}' already exists", SEVERE),
    LOG_BOOK_TYPE_OBIS_CODE_IS_REQUIRED(5003, Constants.LOG_BOOK_TYPE_OBIS_CODE_IS_REQUIRED_KEY, "The obis code of a log book type is required", SEVERE),
    LOG_BOOK_TYPE_OBIS_CODE_CANNOT_BE_UPDATED(5004, "logBookType.cannotUpdateObisCode", "The obis code of the log book type '{0}' cannot be updated because it is in use", SEVERE),
    LOG_BOOK_TYPE_STILL_IN_USE_BY_LOG_BOOK_SPECS(5005, "logBookType.XstillInUseByLogBookSpecsY", "The log book type {0} cannot be deleted because it is still in use by the following log book spec(s): {1}", SEVERE),
    LOG_BOOK_TYPE_STILL_IN_USE_BY_DEVICE_TYPES(5006, "logBookType.XstillInUseByDeviceTypesY", "The log book type {0} cannot be deleted because it is still in use by the following device type(s): {1}", SEVERE),
    REGISTER_SPEC_NUMBER_OF_DIGITS_LARGER_THAN_ONE(6001, "registerSpec.invalidNumberOfDigits", "Invalid number of digits. At least 1 digit is required", SEVERE),
    REGISTER_SPEC_REGISTER_MAPPING_IS_REQUIRED(6003, Constants.REGISTER_SPEC_REGISTER_MAPPING_IS_REQUIRED_KEY,"The register mapping of a register specification is required", SEVERE),
    REGISTER_SPEC_CHANNEL_SPEC_OF_ANOTHER_DEVICE_CONFIG(6004, "registerSpec.channelSpec.fromOtherConfig","The provide channel spec '{0}' has a different device configuration '{1}' than the device configuration '{2}' of the register mapping", SEVERE),
    REGISTER_SPEC_OVERFLOW_LARGER_THAN_NUMBER_OF_DIGITS(6005, "registerSpec.overflow.exceed","The provided overflow value '{0}' may not exceed '{1}' (according to the provided number of digits '{2}')", SEVERE),
    REGISTER_SPEC_OVERFLOW_LARGER_THAN_ZERO(6006, "registerSpec.overflow.invalidValue","The provided overflow value '{0}' must be larger then zero (0))", SEVERE),
    REGISTER_SPEC_OVERFLOW_INCORRECT_FRACTION_DIGITS(6007, "registerSpec.overflow.fractionDigits","The provided overflow value '{0}' more fraction digits '{1}' than provided '{2}')", SEVERE),
    REGISTER_SPEC_PRIME_CHANNEL_SPEC_ALREADY_EXISTS(6008, "registerSpec.duplicatePrimeRegisterSpecForChannelSpec","Linked channel spec (id={0,number}) already has a PRIME register spec (id={1,number})", SEVERE),
    REGISTER_SPEC_CANNOT_DELETE_FOR_ACTIVE_CONFIG(6009, "registerSpec.delete.active.config","It is not allowed to delete a register spec from an active device configuration", SEVERE),
    REGISTER_SPEC_CANNOT_ADD_TO_ACTIVE_CONFIG(6010, "registerSpec.add.active.config","You can not add a register spec to an active device configuration", SEVERE),
    REGISTER_SPEC_REGISTER_MAPPING_IS_NOT_ON_DEVICE_TYPE(6011, "registerSpec.not.deviceType","The register spec contains a register mapping {0} which is not configured on the device type", SEVERE),
    DEVICE_TYPE_NAME_IS_REQUIRED(7001, "deviceType.name.required", "The name of a device type is required", SEVERE),
    DEVICE_TYPE_ALREADY_EXISTS(7002, "deviceType.duplicateNameX", "A device type with name '{0}' already exists", SEVERE),
    DEVICE_TYPE_STILL_HAS_ACTIVE_CONFIGURATIONS(7003, Constants.DEVICE_TYPE_XSTILL_HAS_ACTIVE_CONFIGURATIONS_KEY, "The device type {0} cannot be deleted because it still has active configurations", SEVERE),
    DEVICE_PROTOCOL_IS_REQUIRED(7004, Constants.DEVICE_PROTOCOL_IS_REQUIRED_KEY, "The protocol of a device type is required", SEVERE),
    DUPLICATE_LOAD_PROFILE_TYPE_IN_DEVICE_TYPE(7005, "deviceType.loadProfileType.duplicate", "The load profile type {0} was already added to the device type {1}", SEVERE),
    DUPLICATE_LOG_BOOK_TYPE_IN_DEVICE_TYPE(7006, "deviceType.logBookType.duplicate", "The log book type {0} was already added to the device type {1}", SEVERE),
    DEVICE_PROTOCOL_CANNOT_CHANGE_WITH_EXISTING_CONFIGURATIONS(7007, Constants.DEVICE_PROTOCOL_CANNOT_CHANGE_WITH_EXISTING_CONFIGURATIONS_KEY, "The protocol of a device type cannot change when the device type has configurations", SEVERE),
    DUPLICATE_REGISTER_MAPPING_IN_DEVICE_TYPE(7008, "deviceType.registerMapping.duplicate", "The register mapping {0} was already added to the device type {1}", SEVERE),
    DUPLICATE_DEVICE_CONFIGURATION(7009, Constants.DUPLICATE_DEVICE_CONFIGURATION_KEY, "All device configurations must have a unique name", SEVERE),
    LOAD_PROFILE_SPEC_LOAD_PROFILE_TYPE_IS_NOT_ON_DEVICE_TYPE(8002, "loadProfileSpec.cannotAddLoadProfileSpecOfTypeXBecauseRtuTypeYDoesNotContainIt", "The load profile spec contains a load profile type {0} which is not configured on the device type", SEVERE),
    LOAD_PROFILE_SPEC_CANNOT_ADD_TO_ACTIVE_CONFIGURATION(8003, "loadProfileSpec.active.configuration", "You can not add a load profile spec to an active device configuration", SEVERE),
    LOAD_PROFILE_SPEC_CANNOT_CHANGE_DEVICE_CONFIG(8004, "loadProfileSpec.change.configuration", "You can not change the device configuration of an existing load profile specification", SEVERE),
    LOAD_PROFILE_SPEC_CANNOT_CHANGE_LOAD_PROFILE_TYPE(8005, "loadProfileSpec.change.loadProfileType", "You can not change the load profile type of an existing load profile spec", SEVERE),
    LOAD_PROFILE_SPEC_CANNOT_DELETE_FROM_ACTIVE_CONFIG(8006, "loadProfileSpec.cannot.delete.active.config", "You can not delete a load profile spec '{0}' from an active device configuration '{1}'", SEVERE),
    LOAD_PROFILE_SPEC_CANNOT_DELETE_STILL_LINKED_CHANNEL_SPECS(8007, "loadProfileSpec.cannot.delete.linked.channel.specs", "Cannot delete Load profile spec because there are still channel specs linked", SEVERE),
    LOAD_PROFILE_SPEC_LOAD_PROFILE_TYPE_IS_REQUIRED(8008, Constants.LOAD_PROFILE_SPEC_LOAD_PROFILE_TYPE_IS_REQUIRED_KEY, "The load profile type of a load profile specification is required", SEVERE),
    LOGBOOK_SPEC_DEVICE_CONFIG_IS_REQUIRED(9001, "logBookSpec.deviceConfig.required", "The device configuration of a logbook specification is required", SEVERE),
    LOGBOOK_SPEC_LOGBOOK_TYPE_IS_REQUIRED(9002, Constants.LOGBOOK_SPEC_LOGBOOK_TYPE_IS_REQUIRED_KEY, "The logbook type of a logbook specification is required", SEVERE),
    LOGBOOK_SPEC_LOGBOOK_TYPE_IS_NOT_ON_DEVICE_TYPE(9003, "logBookSpec.cannotAddLogBookSpecOfTypeXBecauseRtuTypeYDoesNotContainIt", "The logbook specification contains a logbook type {0} which is not configured on the device type", SEVERE),
    LOGBOOK_SPEC_CANNOT_CHANGE_DEVICE_CONFIG(9004, "logBookSpec.change.configuration", "You can not change the device configuration of an existing logbook specification", SEVERE),
    LOGBOOK_SPEC_CANNOT_CHANGE_LOGBOOK_TYPE(9005, "logBookSpec.change.logbookType", "You can not change the logbook type of an existing logbook specification", SEVERE),
    LOGBOOK_SPEC_CANNOT_DELETE_FROM_ACTIVE_CONFIG(9006, "logBookSpec.cannot.delete.active.config", "You can not delete a logbook specification '{0}' from an active device configuration '{1}'", SEVERE),
    LOGBOOK_SPEC_CANNOT_ADD_TO_ACTIVE_CONFIGURATION(9007, "logBookSpec.cannot.add.active.config", "You can not add a logbook spec to an active device configuration", SEVERE),
    PHENOMENON_NAME_IS_REQUIRED(10001, "phenomenon.name.required", "The name of a phenomenon is required", SEVERE),
    PHENOMENON_STILL_IN_USE(10002, "phenomenon.stillInUse", "You can not delete a phenomenon when it is still in use", SEVERE),
    PHENOMENON_ALREADY_EXISTS(10003, "phenomenon.duplicateNameX", "A phenomenon with name '{0}' already exists", SEVERE),
    CHANNEL_SPEC_NAME_IS_REQUIRED(11001, "channelSpec.name.required", "The name of the channel specification is required", SEVERE),
    CHANNEL_SPEC_CANNOT_ADD_TO_ACTIVE_CONFIGURATION(11003, "channelSpec.active.configuration", "You can not add a channel spec to an active device configuration", SEVERE),
    CHANNEL_SPEC_LOAD_PROFILE_SPEC_IS_NOT_ON_DEVICE_CONFIGURATION(11004, "channelSpec.cannotAddChannelSpecOfTypeXBecauseDeviceConfigYDoesNotContainIt", "The channel specification is linked to a load profile specification '{0}' which is not configuration on the device type", SEVERE),
    CHANNEL_SPEC_CANNOT_DELETE_FROM_ACTIVE_CONFIG(11005, "channelSpec.cannot.delete.active.config", "You can not delete a channel specification '{0}' from an active device configuration '{1}'", SEVERE),
    CHANNEL_SPEC_REGISTER_MAPPING_IS_REQUIRED(11006, Constants.CHANNEL_SPEC_REGISTER_MAPPING_IS_REQUIRED_KEY, "The register mapping of a channel specification is required", SEVERE),
    CHANNEL_SPEC_REGISTER_MAPPING_IS_NOT_IN_LOAD_PROFILE_SPEC(11007, "channelSpec.registerMapping.not.configured.loadProfileSpec","The channel specification '{0}' is linked to a register '{1}' which is not configured for the linked load profile specification '{2}'", SEVERE),
    CHANNEL_SPEC_REGISTER_MAPPING_IS_NOT_ON_DEVICE_TYPE(11008, "channelSpec.registerMapping.not.configured.deviceType","The channel specification '{0}' is linked to a register '{1}' which is not configured for the device type '{2}'", SEVERE),
    CHANNEL_SPEC_PHENOMENON_IS_REQUIRED(11009, Constants.CHANNEL_SPEC_PHENOMENON_IS_REQUIRED_KEY, "The phenomenon of a channel specification is required", SEVERE),
    CHANNEL_SPEC_UNITS_NOT_COMPATIBLE(11010, "channelSpec.units.not.compatible","The channel specification defines a phenomenon '{0}' which is not compatible with the unit of the linked register mapping '{1}'", SEVERE),
    CHANNEL_SPEC_READING_METHOD_IS_REQUIRED(11011, Constants.CHANNEL_SPEC_READING_METHOD_IS_REQUIRED_KEY, "The reading method of a channel specification is required", SEVERE),
    CHANNEL_SPEC_MULTIPLIER_MODE_IS_REQUIRED(11012, Constants.CHANNEL_SPEC_MULTIPLIER_MODE_IS_REQUIRED_KEY, "The multiplier mode of a channel specification is required", SEVERE),
    CHANNEL_SPEC_VALUE_CALCULATION_METHOD_IS_REQUIRED(11013, Constants.CHANNEL_SPEC_VALUE_CALCULATION_METHOD_IS_REQUIRED_KEY, "The value calculation method of a channel specification is required", SEVERE),
    CHANNEL_SPEC_MULTIPLIER_IS_REQUIRED_WHEN(11014, Constants.CHANNEL_SPEC_MULTIPLIER_IS_REQUIRED_WHEN_KEY, "The multiplier of a channel specification '{0}' is required when the multiplier mode is set to '{1}'", SEVERE),
    CHANNEL_SPEC_DUPLICATE_REGISTER_MAPPING_IN_LOAD_PROFILE_SPEC(11015, "channelSpec.duplicate.registerMapping.loadProfileSpec","The load profile specification '{0}' already contains a channel specification '{1}' with the given register mapping '{2}'", SEVERE),
    CHANNEL_SPEC_WITHOUT_LOAD_PROFILE_SPEC_INTERVAL_IS_REQUIRED(11016, "channelSpec.interval.required.loadProfileSpec","The interval of a channel specification is required when no load profile specification is defined", SEVERE),
    CHANNEL_SPEC_INVALID_INTERVAL_COUNT(11017, "channelSpec.interval.invalid.count","The amount in the interval of a channel specification should be larger than zero, but was '{0}'", SEVERE),
    CHANNEL_SPEC_INVALID_INTERVAL_COUNT_LARGE_UNIT(11018, "channelSpec.interval.invalid.count.large.unit","The amount in the interval of a channel specification should be '1' if the interval unit is larger than 'hours', but was '{0}'", SEVERE),
    CHANNEL_SPEC_INTERVAL_IN_WEEKS_IS_NOT_SUPPORTED(11019, "channelSpec.interval.notsupported.weeks", "The interval of a channel specification cannot be expressed in number of weeks", SEVERE),
    CHANNEL_SPEC_CANNOT_CHANGE_DEVICE_CONFIG(11020, "channelSpec.change.configuration", "You can not change the device configuration of an existing channel specification", SEVERE),
    CHANNEL_SPEC_ALREADY_EXISTS(11021, "channelSpec.duplicateName", "A channel specification with the name '{0}' already exists", SEVERE),
    CHANNEL_SPEC_ALREADY_EXISTS_27_CHAR(11022, "channelSpec.duplicateName.27", "A channel specification with the same first 27 characters '{0}' already exists", SEVERE),
    CHANNEL_SPEC_CANNOT_CHANGE_REGISTER_MAPPING(11023, "channelSpec.change.registerMapping", "You can not change the register mapping of an existing channel specification", SEVERE),
    CHANNEL_SPEC_CANNOT_CHANGE_LOAD_PROFILE_SPEC(11024, "channelSpec.change.loadProfileSpec", "You can not change the load profile specification of an existing channel specification", SEVERE),
    DEVICE_CONFIGURATION_NAME_IS_REQUIRED(12001, "deviceConfig.name.required", "The name of the device configuration is required", SEVERE),
    DEVICE_CONFIGURATION_DEVICE_TYPE_IS_REQUIRED(12002, "deviceConfig.deviceType.required", "The device type of the device configuration is required", SEVERE),
    DEVICE_CONFIGURATION_IS_ACTIVE_CAN_NOT_DELETE(12003, "deviceConfig.active", "You can not delete an active device configuration", SEVERE),
    DEVICE_CONFIGURATION_DUPLICATE_LOAD_PROFILE_TYPE_IN_SPEC(12004, "deviceConfig.duplicate.loadProfileType", "The device configuration '{0}' already contains a load profile specification with this load profile type '{1}'", SEVERE),
    DEVICE_CONFIGURATION_DUPLICATE_OBIS_CODE_FOR_LOAD_PROFILE_SPEC(12005, "deviceConfig.duplicate.obisCode.loadProfileSpec", "The device configuration '{0}' already contains a load profile specification this obis code '{1}'", SEVERE),
    DEVICE_CONFIGURATION_DUPLICATE_LOG_BOOK_TYPE_IN_SPEC(12006, "deviceConfig.duplicate.logBookType", "The device configuration '{0}' already contains a logbook specification this logbook type '{1}'", SEVERE),
    DEVICE_CONFIGURATION_DUPLICATE_OBIS_CODE_FOR_LOGBOOK_SPEC(12007, "deviceConfig.duplicate.obisCode.logBookSpec", "The device configuration '{0}' already contains a logbook specification this obis code '{1}'", SEVERE),
    DEVICE_CONFIGURATION_DUPLICATE_OBIS_CODE_FOR_REGISTER_SPEC(12008, "deviceConfig.duplicate.obisCode.registerSpec", "The device configuration '{0}' already contains a register specification this obis code '{1}'", SEVERE),
    DEVICE_CONFIGURATION_DUPLICATE_OBIS_CODE_FOR_CHANNEL_SPEC_IN_LOAD_PROFILE_SPEC(12009, "deviceConfig.duplicate.obisCode.channelSpec.loadProfileSpec", "Load profile specification '{0}' in device configuration '{1}' already contains a channel specification this obis code '{2}'", SEVERE),
    DEVICE_CONFIGURATION_DUPLICATE_OBIS_CODE_FOR_CHANNEL_SPEC(12010, "deviceConfig.duplicate.obisCode.channelSpec", "The device configuration '{0}' already contains a channel specification this obis code '{1}'", SEVERE),
    UNIT_DOES_NOT_MATCH_PHENOMENON(12011, "registerMapping.unit.noMatchingPhenomenon" , "The unit {0} could not be associated with an existing phenomenon", SEVERE),
    NEXT_EXECUTION_SPECS_TEMPORAL_EXPRESSION_REQUIRED(13000, Constants.NEXT_EXECUTION_SPECS_TEMPORAL_EXPRESSION_REQUIRED_KEY, "The temporal expression of a NextExecutionSpec is required", SEVERE),
    TEMPORAL_EXPRESSION_FREQUENCY_REQUIRED(13001, Constants.TEMPORAL_EXPRESSION_FREQUENCY_REQUIRED_KEY, "The frequency of a temporal expression is required", SEVERE),
    TEMPORAL_EXPRESSION_UNKNOWN_UNIT(13002, Constants.TEMPORAL_EXPRESSION_UNKNOWN_UNIT_KEY, "The unit {0} is unknown or unsupported for temporal expressions", SEVERE),
    TEMPORAL_EXPRESSION_FREQUENCY_MUST_BE_STRICTLY_POSITIVE(13003, Constants.TEMPORAL_EXPRESSION_FREQUENCY_MUST_BE_STRICTLY_POSITIVE_KEY, "The frequency value of a temporal expression must be a strictly positive number", SEVERE),
    TEMPORAL_EXPRESSION_OFFSET_MUST_BE_POSITIVE(13004, Constants.TEMPORAL_EXPRESSION_OFFSET_MUST_BE_POSITIVE_KEY, "The offset value of a temporal expression must be a positive number", SEVERE),
    DUPLICATE_PROTOCOL_CONFIGURATION_PROPERTIES(13005, "protocolConfigurationProperties.duplicateName", "A protocolDialectConfigurationProperties with the name {0} already exists.", SEVERE),
    PROTOCOL_DIALECT_REQUIRED(13006, Constants.PROTOCOLDIALECT_REQUIRED_KEY, "The protocol dialect name is required for a protocolDialectConfigurationProperties", SEVERE),
    PROTOCOL_DIALECT_HAS_NO_SUCH_PROPERTY(13007, "protocolDialectConfigurationProperties.noSuchProperty", "The protocol dialect {0} does not have a configuration property with name {1}", SEVERE),
    PROTOCOL_DIALECT_DUPLICATE(13008, Constants.PROTOCOLDIALECT_CONF_PROPS_DUPLICATE_KEY, "A dialect configuration properties having device configuration  \"{0}\" and device protocol dialect \"{1}\" already exists.", SEVERE),
    PROTOCOLDIALECT_CONF_PROPS_MISSING_REQUIRED(13009, Constants.PROTOCOLDIALECT_CONF_PROPS_MISSING_REQUIRED, "The protocol dialect {0} is missing required configuration property with name {1}", SEVERE),
    DUPLICATE_PARTIAL_INBOUND_CONNECTION_TYPE(13010, "partialInboundConnectionTask.duplicate", "A Partial Inbound Connection Task by name {0} already exists.", SEVERE),
    DUPLICATE_PARTIAL_OUTBOUND_CONNECTION_TYPE(13011, "partialOutboundConnectionTask.duplicate", "A Partial Outbound Connection Task by name {0} already exists.", SEVERE),
    DUPLICATE_PARTIAL_CONNECTION_INITIATION_TYPE(13012, "partialConnectionInitiationTask.duplicate", "A Partial Connection Initiation Task by name {0} already exists.", SEVERE),
    PROTOCOL_DIALECT_NAME_DOES_NOT_EXIST(13013, "protocolConfigurationProperties.doesNotExistName", "A protocolDialectConfigurationProperties with name {0} does not exist", Level.SEVERE),
    PROTOCOL_DIALECT_ID_DOES_NOT_EXIST(13014, "protocolConfigurationProperties.doesNotExistId", "A protocolDialectConfigurationProperties with id {0} does not exist", Level.SEVERE),
    PARTIAL_CONNECTION_TASK_NAME_DOES_NOT_EXIST(13015, "partialConnectionTask.doesNotExistName", "There is no Partial Connection Task by name {0}", SEVERE),
    PARTIAL_CONNECTION_TASK_ID_DOES_NOT_EXIST(13016, "partialConnectionTask.doesNotExistId", "There is no Partial Connection Task with id {0}", SEVERE),
    PARTIAL_CONNECTION_TASK_DUPLICATE(13017, Constants.PARTIAL_CONNECTION_TASK_DUPLICATE_KEY, "There is already a Connection task with name {0}", SEVERE),
    PARTIAL_CONNECTION_TASK_PROPERTY_HAS_NO_SPEC(13018, Constants.PARTIAL_CONNECTION_TASK_PROPERTY_HAS_NO_SPEC_KEY, "There is no spec for connection type property with name {0}", SEVERE),
    PARTIAL_CONNECTION_TASK_PROPERTY_VALUE_OF_WRONG_TYPE(13019, Constants.PARTIAL_CONNECTION_TASK_PROPERTY_VALUE_OF_WRONG_TYPE_KEY, "The value for property {0} is of the wrong type.", SEVERE),
    CONNECTION_STRATEGY_REQUIRED(13020, Constants.CONNECTION_STRATEGY_REQUIRED_KEY, "Connection Strategy is required", SEVERE),
    NEXT_EXECUTION_SPEC_REQUIRED_FOR_MINIMIZE_CONNECTIONS(13021, Constants.NEXT_EXECUTION_SPEC_REQUIRED_FOR_MINIMIZE_CONNECTIONS_KEY, "Next Execution Spec is required for OutboundConnectionTasks that minimize connections.", SEVERE),
    NEXT_EXECUTION_SPEC_INVALID_FOR_COM_WINDOW_KEY(13022, Constants.NEXT_EXECUTION_SPEC_INVALID_FOR_COM_WINDOW_KEY, "Next Execution Spec is invalid for Communication Window.", SEVERE);

    private final int number;
    private final String key;
    private final String defaultFormat;
    private final Level level;

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

    public static enum Constants {
        ;
        public static final String NAME_REQUIRED_KEY = "DTC.X.name.required";
        public static final String DEVICE_TYPE_XSTILL_HAS_ACTIVE_CONFIGURATIONS_KEY = "DTC.deviceType.XstillHasActiveConfigurations";
        public static final String DUPLICATE_DEVICE_CONFIGURATION_KEY = "DTC.deviceType.deviceConfig.duplicateName";
        public static final String DEVICE_PROTOCOL_IS_REQUIRED_KEY = "DTC.deviceType.protocol.required";
        public static final String DEVICE_PROTOCOL_CANNOT_CHANGE_WITH_EXISTING_CONFIGURATIONS_KEY = "DTC.deviceType.protocol.noupdate";
        public static final String REGISTER_MAPPING_OBIS_CODE_IS_REQUIRED_KEY = "DTC.registerMapping.obisCode.required";
        public static final String PRODUCT_SPEC_IS_REQUIRED_KEY = "DTC.registerMapping.productSpec.required";
        public static final String UNIT_IS_REQUIRED_KEY = "DTC.registerMapping.unit.required";
        public static final String LOAD_PROFILE_TYPE_OBIS_CODE_IS_REQUIRED_KEY = "DTC.loadProfileType.obisCode.required";
        public static final String LOG_BOOK_TYPE_OBIS_CODE_IS_REQUIRED_KEY = "DTC.logBookType.obisCode.required";
        public static final String LOGBOOK_SPEC_LOGBOOK_TYPE_IS_REQUIRED_KEY = "DTC.logBookSpec.logbookType.required";
        public static final String LOAD_PROFILE_SPEC_LOAD_PROFILE_TYPE_IS_REQUIRED_KEY = "DTC.loadProfileSpec.loadProfileType.required";
        public static final String CHANNEL_SPEC_REGISTER_MAPPING_IS_REQUIRED_KEY = "DTC.channelSpec.registerMapping.required";
        public static final String CHANNEL_SPEC_PHENOMENON_IS_REQUIRED_KEY = "DTC.channelSpec.phenomenon.required";
        public static final String CHANNEL_SPEC_READING_METHOD_IS_REQUIRED_KEY = "DTC.channelSpec.readingMethod.required";
        public static final String CHANNEL_SPEC_MULTIPLIER_IS_REQUIRED_WHEN_KEY = "DTC.channelSpec.multiplier.required.when";
        public static final String CHANNEL_SPEC_VALUE_CALCULATION_METHOD_IS_REQUIRED_KEY = "DTC.channelSpec.valueCalculationMethod.required";
        public static final String CHANNEL_SPEC_MULTIPLIER_MODE_IS_REQUIRED_KEY = "DTC.channelSpec.multiplierMode.required";
        public static final String REGISTER_SPEC_REGISTER_MAPPING_IS_REQUIRED_KEY = "DTC.registerSpec.registerMapping.required";
        public static final String READING_TYPE_IS_REQUIRED_KEY = "DTC.productSpec.readingType.required";
        public static final String READING_TYPE_ALREADY_EXISTS_KEY = "DTC.productSpec.duplicateReadingTypeX";
        public static final String TIMEOFUSE_TOO_SMALL = "DTC.timeOfUse.tooSmall";
        public static final String NEXT_EXECUTION_SPECS_TEMPORAL_EXPRESSION_REQUIRED_KEY = "DTC.nextExecutionSpecs.temporalExpression.required";
        public static final String TEMPORAL_EXPRESSION_FREQUENCY_REQUIRED_KEY = "DTC.temporalExpression.every.required";
        public static final String TEMPORAL_EXPRESSION_UNKNOWN_UNIT_KEY = "DTC.temporalExpression.unknown.unit";
        public static final String TEMPORAL_EXPRESSION_FREQUENCY_MUST_BE_STRICTLY_POSITIVE_KEY = "DTC.temporalExpression.every.count.positive";
        public static final String TEMPORAL_EXPRESSION_OFFSET_MUST_BE_POSITIVE_KEY = "DTC.temporalExpression.offset.count.positive";
        public static final String PROTOCOLDIALECT_REQUIRED_KEY = "DTC.protocolDialectConfigurationProperties.dialectName.required";
        public static final String PROTOCOLDIALECT_CONF_PROPS_DUPLICATE_KEY = "DTC.protocolDialectConfigurationProperties.duplicate";
        public static final String PROTOCOLDIALECT_CONF_PROPS_MISSING_REQUIRED = "DTC.protocolDialectConfigurationProperties.missing.required.properties";
        public static final String PARTIAL_CONNECTION_TASK_DUPLICATE_KEY = "DTC.partialConnectionTask.duplicate";
        public static final String PARTIAL_CONNECTION_TASK_PROPERTY_HAS_NO_SPEC_KEY = "DTC.partialConnectionTaskProperty.hasNoSpec";
        public static final String PARTIAL_CONNECTION_TASK_PROPERTY_VALUE_OF_WRONG_TYPE_KEY = "DTC.partialConnectionTaskProperty.wrongValueType";
        public static final String CONNECTION_STRATEGY_REQUIRED_KEY = "DTC.partialOutboundConnectionTask.connectionStrategyRequired";
        public static final String NEXT_EXECUTION_SPEC_REQUIRED_FOR_MINIMIZE_CONNECTIONS_KEY = "DTC.partialOutboundConnectionTask.executionSpecRequiredForMinimizeConnections";
        public static final String NEXT_EXECUTION_SPEC_INVALID_FOR_COM_WINDOW_KEY = "DTC.partialOutboundConnectionTask.executionSpecInvalidForComWindow";
    }

}