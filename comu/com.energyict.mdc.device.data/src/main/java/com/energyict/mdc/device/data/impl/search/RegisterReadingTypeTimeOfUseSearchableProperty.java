/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
    public String getMeasurementTypeJoinSql() {
        return "DTC_REGISTERSPEC join MDS_MEASUREMENTTYPE on MDS_MEASUREMENTTYPE.ID = DTC_REGISTERSPEC.REGISTERTYPEID";
    }

    @Override
    public String getName() {
        return PROPERTY_NAME;
    }
}
