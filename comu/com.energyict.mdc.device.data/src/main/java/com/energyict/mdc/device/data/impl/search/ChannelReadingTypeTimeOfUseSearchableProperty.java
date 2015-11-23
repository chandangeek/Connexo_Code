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
