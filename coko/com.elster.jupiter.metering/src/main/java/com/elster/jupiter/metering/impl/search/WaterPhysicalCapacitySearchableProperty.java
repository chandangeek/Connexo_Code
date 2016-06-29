package com.elster.jupiter.metering.impl.search;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.QuantityValueFactory;
import com.elster.jupiter.util.units.Quantity;

import javax.inject.Inject;
import java.math.BigDecimal;

public class WaterPhysicalCapacitySearchableProperty extends PhysicalCapacitySearchableProperty {

    private final PropertySpecService propertySpecService;
    private final Thesaurus thesaurus;
    private static final String FIELD_NAME = "detail.physicalCapacity";
    private final String uniqueName = FIELD_NAME.concat(".")
            .concat("serviceKind.water");

    @Inject
    public WaterPhysicalCapacitySearchableProperty(PropertySpecService propertySpecService, Thesaurus thesaurus) {
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
                .addValues(Quantity.create(BigDecimal.ZERO, 0, "Wh"),
                        Quantity.create(BigDecimal.ZERO, 3, "Wh"),
                        Quantity.create(BigDecimal.ZERO, 6, "Wh"),
                        Quantity.create(BigDecimal.ZERO, 9, "Wh"))
                .finish();
    }
}
