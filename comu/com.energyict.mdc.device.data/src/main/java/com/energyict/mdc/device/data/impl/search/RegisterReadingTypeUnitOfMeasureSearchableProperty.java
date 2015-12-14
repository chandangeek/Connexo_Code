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
    public String getMeasurementTypeJoinSql() {
        return "DTC_REGISTERSPEC join MDS_MEASUREMENTTYPE on MDS_MEASUREMENTTYPE.ID = DTC_REGISTERSPEC.REGISTERTYPEID";
    }

    @Override
    public String getName() {
        return PROPERTY_NAME;
    }
}