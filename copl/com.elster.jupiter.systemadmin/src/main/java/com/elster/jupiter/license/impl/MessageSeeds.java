package com.elster.jupiter.license.impl;

import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

/**
 * Copyrights EnergyICT
 * Date: 28/03/2014
 * Time: 16:47
 */
public enum MessageSeeds implements MessageSeed {
    INVALID_LICENSE(0, "invalid.license", "Invalid license file", Level.SEVERE),
    ALREADY_ACTIVE(1, "license.already.active", "License is already active", Level.SEVERE),
    NEWER_LICENSE_EXISTS(2, "newer.license.active", "A newer license is already active", Level.SEVERE),
    LICENSE_FOR_OTHER_APP(3, "license.for.different.app", "License is for different application", Level.SEVERE);

    private int errorNumber;
    private String key;
    private String defaultFormat;
    private Level level;

    MessageSeeds(int errorNumber, String key, String defaultFormat, Level level) {
        this.errorNumber = errorNumber;
        this.key = key;
        this.defaultFormat = defaultFormat;
        this.level = level;
    }

    @Override
    public String getModule() {
        return LicenseService.COMPONENTNAME;
    }

    @Override
    public int getNumber() {
        return errorNumber;
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
}
