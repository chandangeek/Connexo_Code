package com.elster.jupiter.systemadmin.rest.imp.response;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.system.Component;
import com.elster.jupiter.system.RuntimeComponent;
import com.elster.jupiter.system.BundleTypeTranslationKeys;
import com.elster.jupiter.system.ComponentStatusTranslationKeys;
import javax.inject.Inject;

public class ComponentInfoFactory {
    private final Thesaurus thesaurus;

    @Inject
    public ComponentInfoFactory(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public ComponentInfo asInfo(RuntimeComponent runtimeComponent) {
        ComponentInfo info = new ComponentInfo();
        Component component = runtimeComponent.getComponent();
        info.id = String.valueOf(runtimeComponent.getId());
        info.application = component.getSubsystem().getName();
        info.bundleType = thesaurus.getFormat(BundleTypeTranslationKeys.getTranslatedName(component.getBundleType())).format();
        info.name = runtimeComponent.getName();
        info.version = component.getVersion();
        info.status = thesaurus.getFormat(ComponentStatusTranslationKeys.getTranslatedName(runtimeComponent.getStatus())).format();
        return info;
    }
}
