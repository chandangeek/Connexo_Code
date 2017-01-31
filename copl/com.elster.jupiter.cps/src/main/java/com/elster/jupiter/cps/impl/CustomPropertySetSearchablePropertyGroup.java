/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cps.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.search.SearchablePropertyGroup;

class CustomPropertySetSearchablePropertyGroup implements SearchablePropertyGroup {
    private final CustomPropertySet<?, ?> customPropertySet;

    CustomPropertySetSearchablePropertyGroup(CustomPropertySet<?, ?> customPropertySet) {
        this.customPropertySet = customPropertySet;
    }

    @Override
    public String getId() {
        return customPropertySet.getId();
    }

    @Override
    public String getDisplayName() {
        return customPropertySet.getName();
    }
}
