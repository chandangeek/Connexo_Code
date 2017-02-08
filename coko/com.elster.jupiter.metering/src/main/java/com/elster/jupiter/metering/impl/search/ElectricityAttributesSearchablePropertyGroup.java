/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.search;

import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.nls.Thesaurus;

import javax.inject.Inject;

public class ElectricityAttributesSearchablePropertyGroup extends ServiceKindAwareSearchablePropertyGroup {

    static final String GROUP_NAME = "serviceKind.electricity";

    private final Thesaurus thesaurus;

    @Inject
    public ElectricityAttributesSearchablePropertyGroup(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    @Override
    public String getId() {
        return GROUP_NAME;
    }

    @Override
    public String getDisplayName() {
        return PropertyTranslationKeys.USAGEPOINT_GROUP_ELECTRICITY.getDisplayName(thesaurus);
    }

    @Override
    public ServiceKind getServiceKind() {
        return ServiceKind.ELECTRICITY;
    }
}
