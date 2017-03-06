/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.dataquality.rest.impl;

import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {

    INVALID_FILTER_FORMAT(1, "InvalidFilterFormat", "Invalid format of filter ''{0}'', expected format: {1}"),
    UNSUPPORTED_OPERATOR(2, "UnsupportedFilterOperator", "Unsupported filter operator ''{0}'', supported operators: {1}"),
    INVALID_OPERATOR_CRITERIA(3, "InvalidOperatorCriteria", "Invalid criteria for operator ''{0}'' of filter ''{1}''"),

    NO_SUCH_DEVICE_GROUP(4, "NoSuchDeviceGroup", "Device group with id ''{0}'' not found"),
    NO_SUCH_DEVICE_TYPE(5, "NoSuchDeviceType", "Device type with id ''{0}'' not found"),
    NO_SUCH_VALIDATOR(6, "NoSuchValidator", "Validator with implementation ''{0}'' not found"),
    NO_SUCH_ESTIMATOR(7, "NoSuchEstimator", "Estimator with implementation ''{0}'' not found");

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
    public String getModule() {
        return DeviceDataQualityApplication.COMPONENT_NAME;
    }

    @Override
    public int getNumber() {
        return this.number;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultFormat;
    }

    @Override
    public Level getLevel() {
        return this.level;
    }

}