package com.energyict.mdc.device.data.impl.search;


import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.search.SearchablePropertyGroup;

import javax.inject.Inject;

public class ConnectionDynamicSearchableGroup implements SearchablePropertyGroup {

    static final String GROUP_NAME = ConnectionMethodSearchableProperty.PROPERTY_NAME + ".dynamic";

    private final Thesaurus thesaurus;

    @Inject
    public ConnectionDynamicSearchableGroup(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    @Override
    public String getId() {
        return GROUP_NAME;
    }

    @Override
    public String getDisplayName() {
        return this.thesaurus.getFormat(PropertyTranslationKeys.CONNECTION_DYNAMIC_PROP).format();
    }
}

