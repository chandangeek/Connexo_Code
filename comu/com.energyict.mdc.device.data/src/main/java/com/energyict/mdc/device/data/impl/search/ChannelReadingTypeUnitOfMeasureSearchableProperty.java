/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpecService;

import javax.inject.Inject;

public class ChannelReadingTypeUnitOfMeasureSearchableProperty extends AbstractReadingTypeUnitOfMeasureSearchableProperty<ChannelReadingTypeUnitOfMeasureSearchableProperty> {

    static final String PROPERTY_NAME = "device.channel.reading.type.unit";

    @Inject
    public ChannelReadingTypeUnitOfMeasureSearchableProperty(MeteringService meteringService, PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(ChannelReadingTypeUnitOfMeasureSearchableProperty.class, meteringService, propertySpecService, thesaurus);
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