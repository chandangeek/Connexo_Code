package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.search.SearchablePropertyGroup;

import javax.inject.Inject;

public class ZonesSearchablePropertyGroup implements SearchablePropertyGroup {

    static final String GROUP_NAME = "device.zone";

    private final Thesaurus thesaurus;

    @Inject
    public ZonesSearchablePropertyGroup(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    @Override
    public String getId() {
        return GROUP_NAME;
    }

    @Override
    public String getDisplayName() {
        return this.thesaurus.getFormat(PropertyTranslationKeys.ZONE).format();
    }
}
