package com.elster.jupiter.properties.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.DurationValueFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.SimplePropertyType;

/**
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-12-09 (10:05)
 */
public class DurationPropertyValueConverter extends TemporalAmountPropertyValueConverter {

    public DurationPropertyValueConverter(Thesaurus thesaurus) {
        super(thesaurus);
    }

    @Override
    public boolean canProcess(PropertySpec propertySpec) {
        return propertySpec != null && propertySpec.getValueFactory() instanceof DurationValueFactory;
    }

    @Override
    public SimplePropertyType getPropertyType(PropertySpec propertySpec) {
        return SimplePropertyType.DURATION;
    }

}