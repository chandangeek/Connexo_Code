/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.search;

import com.elster.jupiter.metering.MeteringTranslationService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.QuantityValueFactory;
import com.elster.jupiter.util.units.Quantity;

import javax.inject.Inject;
import java.math.BigDecimal;

class WaterLoadLimitSearchableProperty extends LoadLimitSearchableProperty {

    private static final String FIELD_NAME = "detail.loadLimit";

    @Inject
    WaterLoadLimitSearchableProperty(PropertySpecService propertySpecService, MeteringTranslationService meteringTranslationService, Thesaurus thesaurus) {
        super(propertySpecService, meteringTranslationService, thesaurus);
    }

    @Override
    public PropertySpec getSpecification() {
        return this.getPropertySpecService()
                .specForValuesOf(new QuantityValueFactory())
                .named(FIELD_NAME + ".serviceKind.water", PropertyTranslationKeys.USAGEPOINT_PHYSICAL_CAPACITY)
                .fromThesaurus(this.getThesaurus())
                .addValues(Quantity.create(BigDecimal.ZERO, 0, "m3/h"))
                .finish();
    }

}