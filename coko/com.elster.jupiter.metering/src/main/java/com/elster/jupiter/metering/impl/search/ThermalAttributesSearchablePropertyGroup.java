package com.elster.jupiter.metering.impl.search;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.search.SearchablePropertyGroup;

import javax.inject.Inject;

public class ThermalAttributesSearchablePropertyGroup implements SearchablePropertyGroup {

    static final String GROUP_NAME = "serviceKind.heat";

    private final Thesaurus thesaurus;

    @Inject
    public ThermalAttributesSearchablePropertyGroup(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    @Override
    public String getId() {
        return GROUP_NAME;
    }

    @Override
    public String getDisplayName() {
        return PropertyTranslationKeys.USAGEPOINT_GROUP_HEAT.getDisplayName(thesaurus);
    }
}
