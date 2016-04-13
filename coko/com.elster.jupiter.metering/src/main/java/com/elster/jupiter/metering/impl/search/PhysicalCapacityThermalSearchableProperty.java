package com.elster.jupiter.metering.impl.search;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.QuantityValueFactory;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.util.units.Quantity;

import java.math.BigDecimal;

public class PhysicalCapacityThermalSearchableProperty extends PhysicalCapacitySearchableProperty {
    private static final String FIELDNAME = "detail.physicalCapacity";

    public PhysicalCapacityThermalSearchableProperty(SearchDomain domain, PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(domain, propertySpecService, new ThermalAttributesSearchablePropertyGroup(thesaurus), thesaurus);
    }

    // TODO: do we need to add 2 quantities for this req?
    // TODO: no such unit: "kWh/day"
    // TODO: rework with ancestor if needed
//    @Override
//    public PropertySpec getSpecification() {
//        return this.propertySpecService
//                .specForValuesOf(new QuantityValueFactory())
//                .named(FIELDNAME, PropertyTranslationKeys.USAGEPOINT_PHYSICAL_CAPACITY)
//                .fromThesaurus(this.thesaurus)
//                .addValues(Quantity.create(new BigDecimal(0), 1, "m3/h"))
//                .addValues(Quantity.create(new BigDecimal(0), 1, "kWh/day"))
//                .finish();
//    }

}
