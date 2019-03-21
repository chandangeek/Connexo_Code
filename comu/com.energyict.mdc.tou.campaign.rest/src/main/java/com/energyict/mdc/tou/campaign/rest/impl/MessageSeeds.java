/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tou.campaign.rest.impl;

import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {

    NO_TIME_OF_USE_CAMPAIGN_IS_FOUND(1, "NoTimeOfUseCampaignIsFound", "No time of use campaign is found."),
    DEVICE_WITH_ID_ISNT_FOUND(2, "DeviceWithIdIsntFound", "Device with id {0} not found."),
    DEVICETYPE_WITH_ID_ISNT_FOUND(3, "DeviceTypeWithIdIsntFound", "DeviceType with id {0} is''nt found."),
    TOU_ITEM_WITH_ID_ISNT_FOUND(4, "TimeOfUseItemWithIdIsntFound", "Time of use item with id {0} is''nt found."),
    CAMPAIGN_WITH_ID_ISNT_FOUND(5, "CampaignWithIdIsntFound", "Campaign with id {0} is''nt found."),
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
