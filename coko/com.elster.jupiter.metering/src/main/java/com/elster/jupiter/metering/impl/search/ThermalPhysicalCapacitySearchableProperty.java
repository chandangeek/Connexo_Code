package com.elster.jupiter.metering.impl.search;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.QuantityValueFactory;
import com.elster.jupiter.util.units.Quantity;

import javax.inject.Inject;
import java.math.BigDecimal;

public class ThermalPhysicalCapacitySearchableProperty extends PhysicalCapacitySearchableProperty {
    private final PropertySpecService propertySpecService;
    private final Thesaurus thesaurus;
    private static final String FIELD_NAME = "detail.loadLimit";
    private final String uniqueName = FIELD_NAME.concat(".").concat("serviceKind.heat");

    @Inject
    public ThermalPhysicalCapacitySearchableProperty(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(propertySpecService, thesaurus);
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
    }

    @Override
    public PropertySpec getSpecification() {
        return this.propertySpecService
                .specForValuesOf(new QuantityValueFactory())
                .named(this.uniqueName, PropertyTranslationKeys.USAGEPOINT_PHYSICAL_CAPACITY)
                .fromThesaurus(this.thesaurus)
                .addValues(Quantity.create(new BigDecimal(0), 0, "Wh"),
                        Quantity.create(new BigDecimal(0), 3, "Wh"),
                        Quantity.create(new BigDecimal(0), 6, "Wh"),
                        Quantity.create(new BigDecimal(0), 9, "Wh"))
                .finish();
    }
}
