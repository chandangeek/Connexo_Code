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
    PRODUCT_SPEC_STILL_IN_USE(1003, "productSpec.XstillInUseByY", "The product spec with reading type {0} cannot be deleted because it is still in use by the following register mappigs: {1}", Level.SEVERE),
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