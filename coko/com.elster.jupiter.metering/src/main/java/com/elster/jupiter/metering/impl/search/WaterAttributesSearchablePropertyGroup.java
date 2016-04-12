package com.elster.jupiter.metering.impl.search;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.search.SearchablePropertyGroup;

import javax.inject.Inject;

public class WaterAttributesSearchablePropertyGroup implements SearchablePropertyGroup {

    static final String GROUP_NAME = "serviceKind.water";

    private final Thesaurus thesaurus;

    @Inject
    public WaterAttributesSearchablePropertyGroup(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    @Override
    public String getId() {
        return GROUP_NAME;
    }

    @Override
    public String getDisplayName() {
        return this.thesaurus.getFormat(PropertyTranslationKeys.USAGEPOINT_GROUP_WATER).format();
    }
}
