/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.search;


import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.search.SearchablePropertyGroup;

import javax.inject.Inject;

public class CalendarSearchableGroup implements SearchablePropertyGroup {

    static final String GROUP_NAME = ActiveDeviceCalendarSearchableProperty.FIELD_NAME + ".group";

    private final Thesaurus thesaurus;

    @Inject
    public CalendarSearchableGroup(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    @Override
    public String getId() {
        return GROUP_NAME;
    }

    @Override
    public String getDisplayName() {
        return this.thesaurus.getFormat(PropertyTranslationKeys.DEVICE_TIME_OF_USE_GROUP).format();
    }
}

