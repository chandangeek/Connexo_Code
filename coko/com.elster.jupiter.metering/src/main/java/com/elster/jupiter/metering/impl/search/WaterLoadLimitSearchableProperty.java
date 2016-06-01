package com.elster.jupiter.metering.impl.search;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.QuantityValueFactory;
import com.elster.jupiter.util.units.Quantity;

import javax.inject.Inject;
import java.math.BigDecimal;

public class WaterLoadLimitSearchableProperty extends LoadLimitSearchableProperty {

    private final PropertySpecService propertySpecService;
    private final Thesaurus thesaurus;
    private static final String FIELD_NAME = "detail.loadLimit";

    @Inject
    public WaterLoadLimitSearchableProperty(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(propertySpecService, thesaurus);
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
    }

    @Override
    public PropertySpec getSpecification() {
        return this.propertySpecService
                .specForValuesOf(new QuantityValueFactory())
                .named(FIELD_NAME + "." + "serviceKind.water", PropertyTranslationKeys.USAGEPOINT_PHYSICAL_CAPACITY)
                .fromThesaurus(this.thesaurus)
                .addValues(Quantity.create(BigDecimal.ZERO, 0, "m3/h"))
                .finish();
    }
}
