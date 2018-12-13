/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.search.SearchablePropertyGroup;

import javax.inject.Inject;

public class ChannelSearchablePropertyGroup implements SearchablePropertyGroup {

    static final String GROUP_NAME = "device.channel";

    private final Thesaurus thesaurus;

    @Inject
    public ChannelSearchablePropertyGroup(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    @Override
    public String getId() {
        return GROUP_NAME;
    }

    @Override
    public String getDisplayName() {
        return this.thesaurus.getFormat(PropertyTranslationKeys.CHANNEL).format();
    }
}
