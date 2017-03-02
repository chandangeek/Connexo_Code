package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.search.SearchablePropertyGroup;

import javax.inject.Inject;

public class DeviceAttributesPropertyGroup implements SearchablePropertyGroup {

    static final String GROUP_NAME = "device.attributes";

    private final Thesaurus thesaurus;

    @Inject
    public DeviceAttributesPropertyGroup(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    @Override
    public String getId() {
        return GROUP_NAME;
    }

    @Override
    public String getDisplayName() {
        return this.thesaurus.getFormat(PropertyTranslationKeys.DEVICE_ATTRIBUTES).format();
    }
}
