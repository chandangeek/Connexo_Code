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
    public void appendJoinClauses(JoinClauseBuilder builder) {
        builder.addChannelSpec();
    }

    @Override
    public String getSpecTableAlias() {
        return "ch_msr_type";
    }

    @Override
    public String getName() {
        return PROPERTY_NAME;
    }
}