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

    NAME_IS_REQUIRED(1000, Constants.NAME_REQUIRED_KEY, "The name of {0} is required", SEVERE),
    NAME_IS_UNIQUE(1001, Constants.NAME_UNIQUE_KEY, "The name must be unique", SEVERE),
    LOG_BOOK_TYPE_NAME_IS_REQUIRED(1002, "MDS.logBookType.name.required", "The name of a log book type is required", SEVERE),
    LOG_BOOK_TYPE_ALREADY_EXISTS(1003, "MDS.logBookType.duplicateNameX", "A log book type with name \"{0}\" already exists", SEVERE),
    LOG_BOOK_TYPE_OBIS_CODE_IS_REQUIRED(1004, Constants.LOG_BOOK_TYPE_OBIS_CODE_IS_REQUIRED_KEY, "The obis code of a log book type is required", SEVERE),
    LOG_BOOK_TYPE_STILL_IN_USE_BY_LOG_BOOK_SPECS(1005, "MDS.logBookType.XstillInUseByLogBookSpecsY", "The log book type {0} cannot be deleted because it is still in use by the following log book spec(s): {1}", SEVERE),
    LOG_BOOK_TYPE_STILL_IN_USE_BY_DEVICE_TYPES(1006, "MDS.logBookType.XstillInUseByDeviceTypesY", "The log book type {0} cannot be deleted because it is still in use by the following device type(s): {1}", SEVERE),
    ;

    private final int number;
    private final String key;
    private final String defaultFormat;
    private final Level level;

    MessageSeeds(int number, String key, String defaultFormat, Level level) {
        this.number = number;
        this.key = stripComponentNameIfPresent(key);
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

    private String stripComponentNameIfPresent(String key) {
        if (key.startsWith(MasterDataService.COMPONENTNAME + ".")) {
            return key.substring(MasterDataService.COMPONENTNAME.length() + 1);
        }
        else {
            return key;
        }
    }

    public static class Constants {
        public static final String NAME_REQUIRED_KEY = "MDS.X.name.required";
        public static final String NAME_UNIQUE_KEY = "MDS.X.name.unique";
        public static final String LOG_BOOK_TYPE_OBIS_CODE_IS_REQUIRED_KEY = "MDS.logBookType.obisCode.required";
    }

}