/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.ids.impl;

import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {

    VAULT_INACTIVE(1, "FailedToSaveMeasurementVaultInactive", "Failed to save measurement: the ''{0}'' vault is inactive. Can be changed in table IDS_VAULT.", Level.SEVERE),
    TIME_OUTSIDE_OF_RANGE(2, "FailedToSaveMeasurementTimeOutsideOfRange", "Failed to save measurement: measurement time {0} is outside the ''{1}'' vault range [{2}..{3}]. The range is defined by MINTIME and MAXTIME columns in IDS_VAULT table. These attributes are updated after successful data purge.", Level.SEVERE),
    INTERVAL_TIMESTAMP_IS_NOT_VALID(3, "IntervalTimestampIsNotValid", "Interval timestamp {0} isn''t valid. Time zone used to convert it is {1}.", Level.SEVERE),
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
    public String getModule() {
        return IdsService.COMPONENTNAME;
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

    public static String getString(MessageSeed messageSeed, Thesaurus thesaurus, Object... args) {
        return thesaurus.getFormat(messageSeed).format(args);
    }

}