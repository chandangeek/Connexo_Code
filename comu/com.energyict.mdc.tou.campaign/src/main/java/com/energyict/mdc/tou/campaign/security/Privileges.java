/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tou.campaign.security;

import com.elster.jupiter.nls.TranslationKey;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum Privileges implements TranslationKey {
    //Privileges
    VIEW_TOU_CAMPAIGNS(Constants.VIEW_TOU_CAMPAIGNS, "View campaigns"),
    ADMINISTER_TOU_CAMPAIGNS(Constants.ADMINISTER_TOU_CAMPAIGNS, "Administer campaigns");

    private final String key;
    private final String description;

    Privileges(String key, String description) {
        this.key = key;
        this.description = description;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return getDescription();
    }

    public String getDescription() {
        return description;
    }

    public interface Constants {
        String VIEW_TOU_CAMPAIGNS = "privilege.view.touCampaigns";
        String ADMINISTER_TOU_CAMPAIGNS = "privilege.administer.touCampaigns";
    }
}