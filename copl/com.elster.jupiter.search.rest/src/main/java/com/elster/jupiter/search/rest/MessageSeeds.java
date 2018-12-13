/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.search.rest;

import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {

    NO_SUCH_SEARCH_DOMAIN(1, "NoSuchSearchDomain", "Search domain {0} does not exist"),
    NO_SUCH_PROPERTY(2, "NoSuchProperty", "No search criterion with name {0} exists in this domain"),
    INVALID_VALUE(3, "InvalidValue", "Invalid value"),
    AT_LEAST_ONE_CRITERIA(4, "AtLeastOneCriteria" , "At least one search criterion has to be provided");

    private final int number;
    private final String key;
    private final String defaultFormat;

    MessageSeeds(int number, String key, String defaultFormat) {
        this.number = number;
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getModule() {
        return SearchService.COMPONENT_NAME;
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
        return Level.SEVERE;
    }

}
