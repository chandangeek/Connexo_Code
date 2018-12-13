/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpecService;

import javax.inject.Inject;

public class ChannelReadingTypeTimeOfUseSearchableProperty extends AbstractReadingTypeTimeOfUseSearchableProperty<ChannelReadingTypeTimeOfUseSearchableProperty> {

    static final String PROPERTY_NAME = "device.channel.reading.type.tou";

    @Inject
    public ChannelReadingTypeTimeOfUseSearchableProperty(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(ChannelReadingTypeTimeOfUseSearchableProperty.class, propertySpecService, thesaurus);
    }

    @Override
    public String getMeasurementTypeJoinSql() {
        return "DTC_CHANNELSPEC join MDS_MEASUREMENTTYPE on MDS_MEASUREMENTTYPE.ID = DTC_CHANNELSPEC.CHANNELTYPEID";
    }

    @Override
    public String getName() {
        return PROPERTY_NAME;
    }
}
