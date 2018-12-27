/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tou.campaign.rest.impl;

import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {

    NO_TIME_OF_USE_CAMPAIGN_IS_FOUND(1, "NoTimeOfUseCampaignIsFound", "No time of use campaign is found."),

    STATUS_SUCCESSFUL(1001, "successful","Successful"),
    STATUS_FAILED(1002, "failed","Failed"),
    STATUS_CONFIGURATION_ERROR(1003, "configurationError","Configuration Error"),
    STATUS_ONGOING(1004, "ongoing","Ongoing"),
    STATUS_PENDING(1005, "pending","Pending"),
    STATUS_CANCELED(1006, "canceled","Canceled");

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
        return TimeOfUseApplication.COMPONENT_NAME;
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
}
