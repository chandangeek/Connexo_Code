package com.energyict.mdc.masterdata.exceptions;

import com.elster.jupiter.nls.TranslationKey;
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
public enum MessageSeeds implements MessageSeed, TranslationKey {

    NAME_IS_REQUIRED(1000, Keys.NAME_REQUIRED, "The name is required", SEVERE),
    NAME_IS_UNIQUE(1001, Keys.NAME_UNIQUE, "The name must be unique", SEVERE),
    FIELD_CONTAINS_INVALID_CHARS(1002, Keys.FIELD_CONTAINS_INVALID_CHARS, "Field contains invalid chars", SEVERE),
    FIELD_TOO_LONG(1003, Keys.FIELD_TOO_LONG, "Field must not exceed 80 characters", SEVERE),

    LOG_BOOK_TYPE_NAME_IS_REQUIRED(1102, "logBookType.name.required", "The name of a logbook type is required", SEVERE),
    LOG_BOOK_TYPE_ALREADY_EXISTS(1103, "logBookType.duplicateNameX", "A logbook type with name \"{0}\" already exists", SEVERE),
    LOG_BOOK_TYPE_OBIS_CODE_IS_REQUIRED(1104, Keys.LOG_BOOK_TYPE_OBIS_CODE_IS_REQUIRED, "The obis code of a logbook type is required", SEVERE),
    LOG_BOOK_TYPE_STILL_IN_USE_BY_LOG_BOOK_SPECS(1105, "logBookType.XstillInUseByLogBookSpecsY", "The logbook type {0} cannot be removed because it is still in use by the following logbook spec(s): {1}", SEVERE),
    LOG_BOOK_TYPE_STILL_IN_USE_BY_DEVICE_TYPES(1106, "logBookType.XstillInUseByDeviceTypesY", "The logbook type {0} cannot be removed because it is still in use by the following device type(s): {1}", SEVERE),

    REGISTER_MAPPING_NAME_IS_REQUIRED(1400, "registerType.name.required", "The name of a register type is required", SEVERE),
    REGISTER_MAPPING_OBIS_CODE_IS_REQUIRED(1402, Keys.REGISTER_TYPE_OBIS_CODE_IS_REQUIRED, "The obis code of a register type is required", SEVERE),
    REGISTER_MAPPING_OBIS_CODE_CANNOT_BE_UPDATED(1404, "registerType.cannotUpdateObisCode", "The obis code of the register mapping \"{0}\" cannot be updated because it is in use", SEVERE),
    REGISTER_MAPPING_READING_TYPE_IS_REQUIRED(1406, Keys.REGISTER_TYPE_READING_TYPE_IS_REQUIRED, "The reading type of a register mapping is required", SEVERE),
    REGISTER_MAPPING_READING_TYPE_ALREADY_USED(1407, Keys.REGISTER_TYPE_DUPLICATE_READING_TYPE, "Reading type is already used by a register type", SEVERE),
    REGISTER_MAPPING_STILL_USED_BY_LOAD_PROFILE_TYPE(1409, Keys.REGISTER_MAPPING_STILL_USED_BY_LOADPROFILE, "The register mapping {0} cannot be removed because it is still in use by the following load profile type(s): {1}", SEVERE),
    REGISTER_GROUP_REQUIRES_REGISTER_TYPES(1411, "registerGroup.items.noRegisterType" , "The register group requires at least a register type", SEVERE),
    CHANNEL_TYPE_SHOULD_BE_LINKED_TO_REGISTER_TYPE(1412, Keys.CHANNEL_TYPE_SHOULD_BE_LINKED_TO_REGISTER_TYPE, "A channel type should have a link to it's corresponding register type", SEVERE),
    CHANNEL_TYPE_INTERVAL_REQUIRED(1413, Keys.CHANNEL_TYPE_INTERVAL_IS_REQUIRED, "The interval is required", SEVERE),
    CHANNEL_TYPE_WITH_REGISTER_TYPE_AND_INTERVAL_DUPLICATE(1414, Keys.CHANNEL_TYPE_WITH_REGISTER_TYPE_AND_INTERVAL_DUPLICATE, "There is already a channel type with this interval and register type", SEVERE),
    REGISTER_TYPES_AND_LOAD_PROFILE_TYPE_INTERVAL_NOT_SUPPORTED(1415, Keys.REGISTER_TYPES_AND_LOAD_PROFILE_TYPE_INTERVAL_NOT_SUPPORTED, "The following register types could not be mapped to the load profile type''s interval: {0}", SEVERE),

    LOAD_PROFILE_TYPE_OBIS_CODE_CANNOT_BE_UPDATED(1500, "loadProfileType.cannotUpdateObisCode", "The obis code of the load profile type \"{0}\" cannot be updated because it is in use", SEVERE),
    LOAD_PROFILE_TYPE_INTERVAL_CANNOT_BE_UPDATED(1501, "loadProfileType.cannotUpdateInterval", "The interval of the load profile type \"{0}\" cannot be updated because it is in use", SEVERE),
    LOAD_PROFILE_TYPE_OBIS_CODE_IS_REQUIRED(1502, Keys.LOAD_PROFILE_TYPE_OBIS_CODE_IS_REQUIRED, "The obis code of a load profile type is required", SEVERE),
    LOAD_PROFILE_TYPE_INTERVAL_IS_REQUIRED(1503, Keys.LOAD_PROFILE_TYPE_INTERVAL_IS_REQUIRED, "The interval of a load profile type is required", SEVERE),
    LOAD_PROFILE_TYPE_INTERVAL_IN_WEEKS_IS_NOT_SUPPORTED(1504, "loadProfileType.interval.notsupported.weeks", "The interval of a load profile type cannot be expressed in number of weeks", SEVERE),
    INTERVAL_IN_DAYS_MUST_BE_ONE(1505, "loadProfileType.interval.notsupported.multipledays", "The number of days of the interval of a load profile type cannot be greater than 1 but got {0}", SEVERE),
    INTERVAL_IN_MONTHS_MUST_BE_ONE(1506, "loadProfileType.interval.notsupported.multiplemonths", "The number of months of the interval of a load profile type cannot be greater than 1 but got {0}", SEVERE),
    INTERVAL_IN_YEARS_MUST_BE_ONE(1507, Keys.INTERVAL_IN_YEARS_MUST_BE_ONE, "The number of years of the interval of a load profile type cannot be greater than 1 but got {0}", SEVERE),
    INTERVAL_MUST_BE_STRICTLY_POSITIVE(1508, "loadProfileType.interval.notsupported.negative", "The value of the interval of a load profile type must be a strictly positive number and not {0}", SEVERE),
    DUPLICATE_REGISTER_TYPE_IN_LOAD_PROFILE_TYPE(1509, "loadProfileType.registerType.duplicate", "The register type {0} was already added to the load profile type {1}", SEVERE),
    AT_LEAST_ONE_REGISTER_TYPE_REQUIRED(1510, Keys.AT_LEAST_ONE_REGISTER_TYPE_REQUIRED, "You should select at least one register type", SEVERE),
    REGISTER_TYPE_SHOULD_NOT_HAVE_INTERVAL_READINGTYPE(1511, Keys.REGISTER_TYPE_SHOULD_NOT_HAVE_INTERVAL_READINGTYPE, "A register type should have a readingtype without an interval", SEVERE),
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
        public static final String REGISTER_TYPE_OBIS_CODE_IS_REQUIRED = "registerType.obisCode.required";
        public static final String REGISTER_TYPE_READING_TYPE_IS_REQUIRED = "registerType.readingType.required";
        public static final String REGISTER_TYPE_DUPLICATE_READING_TYPE = "registerType.duplicateReadingType";
        public static final String LOAD_PROFILE_TYPE_OBIS_CODE_IS_REQUIRED = "loadProfileType.obisCode.required";
        public static final String FIELD_TOO_LONG = "incorrect.field.size";
        public static final String CHANNEL_TYPE_SHOULD_BE_LINKED_TO_REGISTER_TYPE = "channelType.linked.registerType";
        public static final String CHANNEL_TYPE_INTERVAL_IS_REQUIRED = "channelType.interval.required";
        public static final String REGISTER_MAPPING_STILL_USED_BY_LOADPROFILE = "registerType.usedBy.loadProfileType";
        public static final String CHANNEL_TYPE_WITH_REGISTER_TYPE_AND_INTERVAL_DUPLICATE = "duplicate.channelType.interval.registerType";
        public static final String AT_LEAST_ONE_REGISTER_TYPE_REQUIRED = "loadProfileType.registerTypes.mustHaveAtLeastOne";
        public static final String REGISTER_TYPES_AND_LOAD_PROFILE_TYPE_INTERVAL_NOT_SUPPORTED = "registerType.inLoadProfileType.unsupportedInterval";
        public static final String LOAD_PROFILE_TYPE_INTERVAL_IS_REQUIRED = "loadProfileType.interval.required";
        public static final String INTERVAL_IN_YEARS_MUST_BE_ONE = "loadProfileType.interval.notsupported.multipleyears";
        public static final String REGISTER_TYPE_SHOULD_NOT_HAVE_INTERVAL_READINGTYPE = "registertype.readingtype.should.not.have.interval";
        public static final String FIELD_CONTAINS_INVALID_CHARS = "field.contains.invalid.chars";
    }

}