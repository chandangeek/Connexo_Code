/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.ctr.MTU155;

import com.elster.jupiter.nls.TranslationKey;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-12-10 (10:08)
 */
public enum MTU155TranslationKeys implements TranslationKey {

    DEBUG("mtu155.debug", "Debug"),
    CHANNEL_BACKLOG("mtu155.channel.backlog", "Debug"),
    EXTRACT_INSTALLATION_DATE("mtu155.extract.installation.date", "Extract installation date"),
    REMOVE_DAY_PROFILE_OFFSET("mtu155.remove.day.profile.offset", "Remove day profile offset"),
    ;

    private final String key;
    private final String defaultFormat;

    MTU155TranslationKeys(String key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultFormat;
    }

}