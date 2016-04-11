package com.elster.jupiter.metering.impl.search;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.search.SearchablePropertyGroup;

import javax.inject.Inject;

public class GasAttributesSearchablePropertyGroup implements SearchablePropertyGroup {

    static final String GROUP_NAME = "serviceKind.gas";

    private final Thesaurus thesaurus;

    @Inject
    public GasAttributesSearchablePropertyGroup(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    @Override
    public String getId() {
        return GROUP_NAME;
    }

    @Override
    public String getDisplayName() {
        return this.thesaurus.getFormat(PropertyTranslationKeys.USAGEPOINT_GROUP_GAS).format();
    }
}
