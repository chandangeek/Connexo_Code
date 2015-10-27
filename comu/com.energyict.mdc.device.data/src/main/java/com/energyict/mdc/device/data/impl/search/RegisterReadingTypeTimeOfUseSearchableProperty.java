package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpecService;

import javax.inject.Inject;

public class RegisterReadingTypeTimeOfUseSearchableProperty extends AbstractReadingTypeTimeOfUseSearchableProperty<RegisterReadingTypeTimeOfUseSearchableProperty> {

    static final String PROPERTY_NAME = "device.register.reading.type.tou";

    @Inject
    public RegisterReadingTypeTimeOfUseSearchableProperty(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(RegisterReadingTypeTimeOfUseSearchableProperty.class, propertySpecService, thesaurus);
    }

    @Override
    public void appendJoinClauses(JoinClauseBuilder builder) {
        builder.addRegisterSpec();
    }

    @Override
    public String getSpecTableAlias() {
        return "reg_msr_type";
    }

    @Override
    public String getName() {
        return PROPERTY_NAME;
    }
}
