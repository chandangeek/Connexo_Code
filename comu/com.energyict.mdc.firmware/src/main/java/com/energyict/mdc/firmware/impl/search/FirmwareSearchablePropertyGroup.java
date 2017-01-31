/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl.search;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.search.SearchablePropertyGroup;

import javax.inject.Inject;

public class FirmwareSearchablePropertyGroup implements SearchablePropertyGroup {

    static final String GROUP_NAME = "device.firmware";

    private final Thesaurus thesaurus;

    @Inject
    public FirmwareSearchablePropertyGroup(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    @Override
    public String getId() {
        return GROUP_NAME;
    }

    @Override
    public String getDisplayName() {
        return this.thesaurus.getFormat(PropertyTranslationKeys.FIRMWARE).format();
    }
}
