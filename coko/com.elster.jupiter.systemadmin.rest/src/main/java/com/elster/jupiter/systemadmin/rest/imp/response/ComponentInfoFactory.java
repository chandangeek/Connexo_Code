/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.systemadmin.rest.imp.response;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.system.Component;
import com.elster.jupiter.system.RuntimeComponent;
import com.elster.jupiter.systemadmin.rest.imp.resource.BundleTypeTranslationKeys;
import com.elster.jupiter.systemadmin.rest.imp.resource.ComponentStatusTranslationKeys;

import javax.inject.Inject;

public class ComponentInfoFactory {
    private final Thesaurus thesaurus;

    @Inject
    public ComponentInfoFactory(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public ComponentInfo asInfo(RuntimeComponent runtimeComponent) {
        ComponentInfo info = new ComponentInfo();
        info.bundleId = runtimeComponent.getId();
        info.application = runtimeComponent.getSubsystem().getName();
        info.name = runtimeComponent.getName();
        info.status = thesaurus.getFormat(ComponentStatusTranslationKeys.getTranslatedName(runtimeComponent.getStatus())).format();
        Component component = runtimeComponent.getComponent();
        info.bundleType = thesaurus.getFormat(BundleTypeTranslationKeys.getTranslatedName(component.getBundleType())).format();
        info.version = component.getVersion();
        return info;
    }
}
