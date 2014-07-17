package com.energyict.mdc.masterdata.exceptions;

import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.masterdata.MasterDataService;

import java.util.logging.Level;

import static java.util.logging.Level.SEVERE;

/**
 * Defines all the {@link MessageSeed}s of the master data module.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-11 (16:49)
 */
public enum MessageSeeds implements MessageSeed {

    NAME_IS_REQUIRED(1000, Keys.NAME_REQUIRED, "The name is required", SEVERE),
    NAME_IS_UNIQUE(1001, Keys.NAME_UNIQUE, "The name must be unique", SEVERE),

    LOG_BOOK_TYPE_NAME_IS_REQUIRED(1102, "logBookType.name.required", "The name of a log book type is required", SEVERE),
    LOG_BOOK_TYPE_ALREADY_EXISTS(1103, "logBookType.duplicateNameX", "A log book type with name \"{0}\" already exists", SEVERE),
    LOG_BOOK_TYPE_OBIS_CODE_IS_REQUIRED(1104, Keys.LOG_BOOK_TYPE_OBIS_CODE_IS_REQUIRED, "The obis code of a log book type is required", SEVERE),
    LOG_BOOK_TYPE_STILL_IN_USE_BY_LOG_BOOK_SPECS(1105, "logBookType.XstillInUseByLogBookSpecsY", "The log book type {0} cannot be deleted because it is still in use by the following log book spec(s): {1}", SEVERE),
    LOG_BOOK_TYPE_STILL_IN_USE_BY_DEVICE_TYPES(1106, "logBookType.XstillInUseByDeviceTypesY", "The log book type {0} cannot be deleted because it is still in use by the following device type(s): {1}", SEVERE),

    REGISTER_GROUP_STILL_IN_USE(1200, "registerGroup.XstillInUseByY", "The register group with name \"{0}\" cannot be deleted because it is still in use by the following register mappings: {1}", SEVERE),

    PHENOMENON_NAME_IS_REQUIRED(1300, "phenomenon.name.required", "The name of a phenomenon is required", SEVERE),

    REGISTER_MAPPING_NAME_IS_REQUIRED(1400, "registerMapping.name.required", "The name of a register type is required", SEVERE),
    REGISTER_MAPPING_OBIS_CODE_TOU_PHENOMENON_ALREADY_EXISTS(1401, "registerMapping.duplicateObisCodeX", "A register type with obis code \"{0}\", phenomenon \"{1}\" and time of use \"{2}\" already exists", SEVERE),
    REGISTER_MAPPING_OBIS_CODE_IS_REQUIRED(1402, Keys.REGISTER_MAPPING_OBIS_CODE_IS_REQUIRED, "The obis code of a register type is required", SEVERE),
    PRODUCT_SPEC_IS_REQUIRED(1403, Keys.PRODUCT_SPEC_IS_REQUIRED, "The product spec of a register type is required", SEVERE),
    REGISTER_MAPPING_OBIS_CODE_CANNOT_BE_UPDATED(1404, "registerMapping.cannotUpdateObisCode", "The obis code of the register mapping \"{0}\" cannot be updated because it is in use", SEVERE),
    REGISTER_MAPPING_UNIT_IS_REQUIRED(1405, Keys.REGISTER_MAPPING_UNIT_IS_REQUIRED, "The unit of a register type is required", SEVERE),
    REGISTER_MAPPING_READING_TYPE_IS_REQUIRED(1406, Keys.REGISTER_MAPPING_READING_TYPE_IS_REQUIRED, "The reading type of a register mapping is required", SEVERE),
    REGISTER_MAPPING_READING_TYPE_ALREADY_USED(1407, Keys.REGISTER_MAPPING_DUPLICATE_READING_TYPE, "Reading type is already used by a register type", SEVERE),
    REGISTER_MAPPING_TIME_OF_USE_TOO_SMALL(1408, Keys.REGISTER_MAPPING_TIMEOFUSE_TOO_SMALL, "The time of use must be a positive number", SEVERE),
    REGISTER_MAPPING_STILL_USED_BY_LOAD_PROFILE_TYPE(1409, "registerMapping.usedBy.loadProfileType", "The register type {0} cannot be deleted because it is still in use by the following load profile type(s): {1}", SEVERE),
    REGISTER_MAPPING_UNIT_DOES_NOT_MATCH_PHENOMENON(1410, "registerMapping.unit.noMatchingPhenomenon" , "The unit {0} could not be associated with an existing phenomenon", SEVERE),
    REGISTER_GROUP_REQUIRES_REGISTER_TYPES(1411, "registerGroup.items.noRegisterType" , "The register group requires at least a register type", SEVERE),

    LOAD_PROFILE_TYPE_OBIS_CODE_CANNOT_BE_UPDATED(1500, "loadProfileType.cannotUpdateObisCode", "The obis code of the load profile type \"{0}\" cannot be updated because it is in use", SEVERE),
    LOAD_PROFILE_TYPE_INTERVAL_CANNOT_BE_UPDATED(1501, "loadProfileType.cannotUpdateInterval", "The interval of the load profile type \"{0}\" cannot be updated because it is in use", SEVERE),
    LOAD_PROFILE_TYPE_OBIS_CODE_IS_REQUIRED(1502, Keys.LOAD_PROFILE_TYPE_OBIS_CODE_IS_REQUIRED, "The obis code of a load profile type is required", SEVERE),
    LOAD_PROFILE_TYPE_INTERVAL_IS_REQUIRED(1503, "loadProfileType.interval.required", "The interval of a load profile type is required", SEVERE),
    LOAD_PROFILE_TYPE_INTERVAL_IN_WEEKS_IS_NOT_SUPPORTED(1504, "loadProfileType.interval.notsupported.weeks", "The interval of a load profile type cannot be expressed in number of weeks", SEVERE),
    INTERVAL_IN_DAYS_MUST_BE_ONE(1505, "loadProfileType.interval.notsupported.multipledays", "The number of days of the interval of a load profile type cannot be greater than 1 but got {0}", SEVERE),
    INTERVAL_IN_MONTHS_MUST_BE_ONE(1506, "loadProfileType.interval.notsupported.multiplemonths", "The number of months of the interval of a load profile type cannot be greater than 1 but got {0}", SEVERE),
    INTERVAL_IN_YEARS_MUST_BE_ONE(1507, "loadProfileType.interval.notsupported.multipleyears", "The number of years of the interval of a load profile type cannot be greater than 1 but got {0}", SEVERE),
    INTERVAL_MUST_BE_STRICTLY_POSITIVE(1508, "loadProfileType.interval.notsupported.negative", "The value of the interval of a load profile type must be a strictly positive number and not {0}", SEVERE),
    DUPLICATE_REGISTER_MAPPING_IN_LOAD_PROFILE_TYPE(1509, "loadProfileType.registerMapping.duplicate", "The register type {0} was already added to the load profile type {1}", SEVERE),
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
        return MasterDataService.COMPONENTNAME;
    }

    public static class Keys {
        public static final String NAME_REQUIRED = "X.name.required";
        public static final String NAME_UNIQUE = "X.name.unique";
        public static final String LOG_BOOK_TYPE_OBIS_CODE_IS_REQUIRED = "logBookType.obisCode.required";
        public static final String PRODUCT_SPEC_IS_REQUIRED = "registerMapping.productSpec.required";
        public static final String REGISTER_MAPPING_OBIS_CODE_IS_REQUIRED = "registerMapping.obisCode.required";
        public static final String REGISTER_MAPPING_UNIT_IS_REQUIRED = "registerMapping.unit.required";
        public static final String REGISTER_MAPPING_READING_TYPE_IS_REQUIRED = "registerMapping.readingType.required";
        public static final String REGISTER_MAPPING_DUPLICATE_READING_TYPE = "registerMapping.duplicateReadingType";
        public static final String REGISTER_MAPPING_TIMEOFUSE_TOO_SMALL = "timeOfUse.tooSmall";
        public static final String LOAD_PROFILE_TYPE_OBIS_CODE_IS_REQUIRED = "loadProfileType.obisCode.required";
        public static final String FIELD_TOO_LONG = "incorrect.field.size";
    }

}