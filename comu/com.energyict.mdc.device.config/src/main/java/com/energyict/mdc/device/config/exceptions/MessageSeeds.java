package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.pluggable.PluggableService;

import java.util.logging.Level;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-29 (16:04)
 */
public enum MessageSeeds implements MessageSeed {
    REGISTER_GROUP_NAME_IS_REQUIRED(1001, "registerGroup.name.required", "The name of a register group is required", Level.SEVERE),
    REGISTER_GROUP_ALREADY_EXISTS(1002, "registerGroup.duplicateNameX", "A register group with name '{0}' already exists", Level.SEVERE),
    REGISTER_GROUP_STILL_IN_USE(1003, "registerGroup.XstillInUseByY", "The register group with name '{0}' cannot be deleted because it is still in use by the following register mappigs: {1}", Level.SEVERE),
    READING_TYPE_IS_REQUIRED(2001, "productSpec.readingType.required", "The reading type of a product spec is required", Level.SEVERE),
    READING_TYPE_ALREADY_EXISTS(2002, "productSpec.duplicateReadingTypeX", "The product spec with the reading type {0} already exists", Level.SEVERE),
    DEFAULT_PRODUCT_SPEC_CANNOT_BE_DELETED(2003, "productSpec.cannotDeleteDefault", "The default product spec cannot be deleted", Level.SEVERE),
    PRODUCT_SPEC_STILL_IN_USE(1003, "productSpec.XstillInUseByY", "The product spec with reading type {0} cannot be deleted because it is still in use by the following register mappings: {1}", Level.SEVERE),
    REGISTER_MAPPING_NAME_IS_REQUIRED(3001, "registerMapping.name.required", "The name of a register mapping is required", Level.SEVERE),
    REGISTER_MAPPING_ALREADY_EXISTS(3002, "registerMapping.duplicateNameX", "A register mapping with name '{0}' already exists", Level.SEVERE),
    REGISTER_MAPPING_OBIS_CODE_ALREADY_EXISTS(3003, "registerMapping.duplicateObisCodeX", "A register mapping with obis code '{0}' already exists", Level.SEVERE),
    OBIS_CODE_IS_REQUIRED(3004, "registerMapping.obisCode.required", "The obis code of a register mapping is required", Level.SEVERE),
    PRODUCT_SPEC_IS_REQUIRED(3005, "registerMapping.productSpec.required", "The product spec of a register mapping is required", Level.SEVERE),
    REGISTER_MAPPING_OBIS_CODE_CANNOT_BE_UPDATED(3006, "registerMapping.cannotUpdateObisCode", "The obis code of the register mapping '{0}' cannot be updated because it is in use", Level.SEVERE),
    REGISTER_MAPPING_PRODUCT_SPEC_CANNOT_BE_UPDATED(3006, "registerMapping.cannotUpdateProductSpec", "The product spec of the register mapping '{0}' cannot be updated because it is in use", Level.SEVERE),
    LOAD_PROFILE_TYPE_NAME_IS_REQUIRED(4001, "loadProfileType.name.required", "The name of a load profile type is required", Level.SEVERE),
    LOAD_PROFILE_TYPE_ALREADY_EXISTS(4002, "loadProfileType.duplicateNameX", "A load profile type with name '{0}' already exists", Level.SEVERE),
    INTERVAL_IS_REQUIRED(4002, "loadProfileType.interval.required", "The interval of a load profile type is required", Level.SEVERE),
    INTERVAL_IN_WEEKS_IS_NOT_SUPPORTED(4003, "loadProfileType.interval.notsupported.weeks", "The interval of a load profile type cannot be expressed in number of weeks", Level.SEVERE),
    INTERVAL_IN_DAYS_MUST_BE_ONE(4004, "loadProfileType.interval.notsupported.multipledays", "The number of days of the interval of a load profile type cannot be greater than 1 but got {0}", Level.SEVERE),
    INTERVAL_IN_MONTHS_MUST_BE_ONE(4005, "loadProfileType.interval.notsupported.multiplemonths", "The number of months of the interval of a load profile type cannot be greater than 1 but got {0}", Level.SEVERE),
    INTERVAL_IN_YEARS_MUST_BE_ONE(4006, "loadProfileType.interval.notsupported.multipleyears", "The number of years of the interval of a load profile type cannot be greater than 1 but got {0}", Level.SEVERE),
    INTERVAL_MUST_BE_STRICTLY_POSITIVE(4007, "loadProfileType.interval.notsupported.negative", "The value of the interval of a load profile type must be a strictly positive number and not {0}", Level.SEVERE),
    DUPLICATE_REGISTER_MAPPING_IN_LOAD_PROFILE_TYPE(4008, "loadProfileType.registerMapping.duplicate", "The register mapping {0} was already added to the load profile type {1}", Level.SEVERE),
    LOAD_PROFILE_TYPE_OBIS_CODE_CANNOT_BE_UPDATED(4009, "loadProfileType.cannotUpdateObisCode", "The obis code of the load profile type '{0}' cannot be updated because it is in use", Level.SEVERE),
    LOAD_PROFILE_TYPE_INTERVAL_CANNOT_BE_UPDATED(4010, "loadProfileType.cannotUpdateInterval", "The interval of the load profile type '{0}' cannot be updated because it is in use", Level.SEVERE),
    LOAD_PROFILE_TYPE_STILL_IN_USE_BY_LOAD_PROFILE_SPECS(4011, "loadProfileType.XstillInUseByLoadProfileSpecsY", "The product spec with reading type {0} cannot be deleted because it is still in use by the following load profile spec(s): {1}", Level.SEVERE),
    LOAD_PROFILE_TYPE_STILL_IN_USE_BY_DEVICE_TYPES(4012, "loadProfileType.XstillInUseByDeviceTypesY", "The product spec with reading type {0} cannot be deleted because it is still in use by the following device type(s): {1}", Level.SEVERE),
    ;

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
        return PluggableService.COMPONENTNAME;
    }

}