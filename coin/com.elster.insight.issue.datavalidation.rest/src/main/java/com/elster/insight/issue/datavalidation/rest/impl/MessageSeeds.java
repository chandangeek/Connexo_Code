/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.insight.issue.datavalidation.rest.impl;

import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {

    NO_METROLOGYCONFIG_FOR_USAGEPOINT(25, "NoMetrologyConfigForUsagePoint", "Usage point {0} doesn''t have a link to metrology configuration."),
    NO_METROLOGYCONTRACT_FOR_USAGEPOINT(26, "NoMetrologyPurposeNotLinkedToUsagePoint", "No Metrology contract was found on usage point {1}.")
    ;
    private final int number;
    private final String key;
    private final String format;

    MessageSeeds(int number, String key, String format) {
        this.number = number;
        this.key = key;
        this.format = format;
    }

    @Override
    public String getModule() {
        return UsagePointIssueDataValidationApplication.ISSUEDATAVALIDATION_REST_COMPONENT;
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
        return format;
    }

    @Override
    public Level getLevel() {
        return Level.SEVERE;
    }

    public static class Keys {

    }
}
