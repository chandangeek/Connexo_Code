package com.energyict.mdc.device.data.impl.search;


import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyGroup;

import javax.inject.Inject;

public class GeneralAttributesDynamicSearchableGroup implements SearchablePropertyGroup {

    static final String GROUP_NAME = DeviceTypeSearchableProperty.PROPERTY_NAME + ".attr.dynamic";

    private final Thesaurus thesaurus;

    @Inject
    public GeneralAttributesDynamicSearchableGroup(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    @Override
    public String getId() {
        return GROUP_NAME;
    }

    @Override
    public String getDisplayName() {
        return this.thesaurus.getFormat(PropertyTranslationKeys.GENERAL_ATTRIBUTES_DYNAMIC_PROP).format();
    }

    public SearchableProperty getDynamicSearchableProperty(SearchDomain searchDomain, PropertySpec propertySpec) {
        return new GeneralAttributeDynamicSearchableProperty().init(searchDomain, this, propertySpec);
    }
}

