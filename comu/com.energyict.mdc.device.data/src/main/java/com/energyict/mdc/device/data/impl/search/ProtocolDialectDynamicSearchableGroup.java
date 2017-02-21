/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.search;


import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.search.SearchablePropertyGroup;

import javax.inject.Inject;

public class ProtocolDialectDynamicSearchableGroup implements SearchablePropertyGroup {

    static final String GROUP_NAME = ProtocolDialectSearchableProperty.PROPERTY_NAME + ".dynamic";

    private final Thesaurus thesaurus;

    @Inject
    public ProtocolDialectDynamicSearchableGroup(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    @Override
    public String getId() {
        return GROUP_NAME;
    }

    @Override
    public String getDisplayName() {
        return this.thesaurus.getFormat(PropertyTranslationKeys.PROTOCOL_DIALECT_DYNAMIC_PROP).format();
    }
}

