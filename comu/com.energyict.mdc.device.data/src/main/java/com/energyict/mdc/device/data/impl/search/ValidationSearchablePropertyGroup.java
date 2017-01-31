/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.search.SearchablePropertyGroup;

import javax.inject.Inject;

public class ValidationSearchablePropertyGroup implements SearchablePropertyGroup {

    static final String GROUP_NAME = "device.validation";

    private final Thesaurus thesaurus;

    @Inject
    public ValidationSearchablePropertyGroup(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    @Override
    public String getId() {
        return GROUP_NAME;
    }

    @Override
    public String getDisplayName() {
        return this.thesaurus.getFormat(PropertyTranslationKeys.VALIDATION).format();
    }
}
