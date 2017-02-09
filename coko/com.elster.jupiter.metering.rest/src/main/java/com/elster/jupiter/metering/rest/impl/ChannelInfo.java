/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ReadingType;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class ChannelInfo {

    public long id;
    public long version;
    public String mainReadingType;
    public String cumulativeReadingType;
    public List<String> readingTypes;

    public ChannelInfo() {
    }

    public ChannelInfo(Channel channel) {
        this.id = channel.getId();
        this.version = channel.getVersion();
        this.mainReadingType = channel.getMainReadingType() == null ? null : channel.getMainReadingType().getMRID();
        if (channel.getBulkQuantityReadingType().isPresent()) {
        	this.cumulativeReadingType = channel.getBulkQuantityReadingType().get().getMRID();
        } else {
        	this.cumulativeReadingType = null;
        }
        this.readingTypes = new ArrayList<>();
        for (ReadingType readingType : channel.getReadingTypes()) {
            readingTypes.add(readingType.getMRID());
        }
    }
}
