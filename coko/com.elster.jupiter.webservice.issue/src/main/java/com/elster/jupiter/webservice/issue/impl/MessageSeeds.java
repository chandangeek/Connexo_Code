/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservice.issue.impl;

import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.webservice.issue.WebServiceIssueService;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {

    UNABLE_TO_CREATE_EVENT(1, "WebServiceIssueUnableToMapEvent", "Unable to create event from event payload: {0}", Level.INFO),
    UNABLE_TO_CREATE_ISSUE_MISSING_WSC_OCCURRENCE(2, "WebServiceIssueUnableToFindWSCOccurrence", "Unable to create issue: couldn''t find web service call occurrence with id {0}.", Level.WARNING),

    END_POINT_CONFIG_IN_USE(1001, "EndPointConfigInUseByIssueCreationRule", "Web service end point ''{0}'' is in use by an issue creation rule.", Level.SEVERE),

    CLOSE_ACTION_WRONG_STATUS(2001, "action.wrong.status", "You are trying to apply the incorrect status", Level.SEVERE),
    CLOSE_ACTION_ISSUE_CLOSED(2002, "action.issue.closed", "Issue closed", Level.INFO),
    CLOSE_ACTION_ISSUE_ALREADY_CLOSED(2003, "action.issue.already.closed", "Issue already closed", Level.SEVERE);

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
        return WebServiceIssueService.COMPONENT_NAME;
    }

}
