/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datavalidation.impl;

import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.issue.datavalidation.IssueDataValidationService;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {

    UNABLE_TO_CREATE_EVENT(2001, "DataValidationIssueUnableToMapEvent", "Unable to create event from event payload: {0}", Level.INFO),
    DEVICE_TYPE_DEVICE_CONFIG_IN_USE(3001, "deviceTypeInUseByIssueCreationRule", "Device type ''{0}'' has one or more configurations that are still in use by an issue creation rule.", Level.SEVERE),
    DEVICE_TYPE_IN_USE(3002, "deviceTypeInUseByIssueCreationRule", "Device type ''{0}'' in use by an issue creation rule.", Level.SEVERE),
    COULD_NOT_PARSE_THRESHOLD_WITH_RELATIVE_PERIOD(4001, "invalid.number.of.arguments", "Unable to process issue creation event: couldn''t parse suspect threshold with relative period.", Level.SEVERE),
    COULD_NOT_FIND_RELATIVE_PERIOD(4002, "EventBadDataNoRelativePeriod", "Unable to process issue creation event: couldn''t obtain relative period with id ''{0}''.", Level.SEVERE),
    RELATIVE_PERIOD_IN_USE(5001, "relativePeriodInUseByIssueCreationRule", "The relative period ''{0}'' is still in use by an issue creation rule.", Level.SEVERE);

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
        return IssueDataValidationService.COMPONENT_NAME;
    }

}
