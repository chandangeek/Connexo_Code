package com.elster.insight.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.Channel;

/**
 * Created by adrianlupan on 4/24/15.
 */
public class DetailedValidationChannelInfo {

    public Long id;
    public String name;
    public Long total;

    public DetailedValidationChannelInfo(Channel channel, Long count) {
        this.id = channel.getId();
        this.name = channel.getMainReadingType().getFullAliasName();
        this.total = count;
    }

    public DetailedValidationChannelInfo() {

    }
}
