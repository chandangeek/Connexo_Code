package com.elster.jupiter.metering.impl.search;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.search.SearchDomain;

public class CollarWaterSearchableProperty extends CollarSearchableProperty {
    public CollarWaterSearchableProperty(SearchDomain domain, PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(domain, propertySpecService, new WaterAttributesSearchablePropertyGroup(thesaurus), thesaurus);
    }
}
