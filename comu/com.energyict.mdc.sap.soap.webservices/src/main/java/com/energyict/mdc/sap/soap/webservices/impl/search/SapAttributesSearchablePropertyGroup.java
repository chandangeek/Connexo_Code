/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.search;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.search.SearchablePropertyGroup;

import javax.inject.Inject;

public class SapAttributesSearchablePropertyGroup implements SearchablePropertyGroup {

    static final String GROUP_NAME = "device.sap";

    private final Thesaurus thesaurus;

    @Inject
    public SapAttributesSearchablePropertyGroup(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    @Override
    public String getId() {
        return GROUP_NAME;
    }

    @Override
    public String getDisplayName() {
        return thesaurus.getFormat(PropertyTranslationKeys.SAP).format();
    }
}
