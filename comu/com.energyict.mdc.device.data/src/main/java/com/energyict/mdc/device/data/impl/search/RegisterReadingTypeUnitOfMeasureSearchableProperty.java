package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpecService;

import javax.inject.Inject;

public class RegisterReadingTypeUnitOfMeasureSearchableProperty extends AbstractReadingTypeUnitOfMeasureSearchableProperty<RegisterReadingTypeUnitOfMeasureSearchableProperty> {

    static final String PROPERTY_NAME = "device.register.reading.type.unit";

    @Inject
    public RegisterReadingTypeUnitOfMeasureSearchableProperty(MeteringService meteringService, PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(RegisterReadingTypeUnitOfMeasureSearchableProperty.class, meteringService, propertySpecService, thesaurus);
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