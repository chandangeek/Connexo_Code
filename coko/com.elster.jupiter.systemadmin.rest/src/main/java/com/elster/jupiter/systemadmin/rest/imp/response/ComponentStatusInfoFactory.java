/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.systemadmin.rest.imp.response;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.system.ComponentStatus;
import com.elster.jupiter.systemadmin.rest.imp.resource.ComponentStatusTranslationKeys;

import javax.inject.Inject;

public class ComponentStatusInfoFactory {
    private final Thesaurus thesaurus;

    @Inject
    public ComponentStatusInfoFactory(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public ComponentStatusInfo asInfo(ComponentStatus componentStatus) {
        ComponentStatusInfo componentStatusInfo = new ComponentStatusInfo();
        componentStatusInfo.id = componentStatus.getId();
        componentStatusInfo.name = thesaurus.getFormat(ComponentStatusTranslationKeys.getTranslatedName(componentStatus)).format();
        return componentStatusInfo;
    }
}
