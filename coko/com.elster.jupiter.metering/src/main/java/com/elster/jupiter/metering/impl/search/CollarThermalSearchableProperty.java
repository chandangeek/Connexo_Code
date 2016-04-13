package com.elster.jupiter.metering.impl.search;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.search.SearchDomain;

public class CollarThermalSearchableProperty extends CollarSearchableProperty {
    public CollarThermalSearchableProperty(SearchDomain domain, PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(domain, propertySpecService, new ThermalAttributesSearchablePropertyGroup(thesaurus), thesaurus);
    }
}
