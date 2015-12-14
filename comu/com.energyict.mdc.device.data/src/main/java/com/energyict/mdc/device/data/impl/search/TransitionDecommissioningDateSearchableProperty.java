package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpecService;
import com.google.inject.Inject;

public class TransitionDecommissioningDateSearchableProperty extends AbstractTransitionSearchableProperty<TransitionDecommissioningDateSearchableProperty> {

    static final String PROPERTY_NAME = "device.transition.decommissioning";

    @Inject
    public TransitionDecommissioningDateSearchableProperty(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(TransitionDecommissioningDateSearchableProperty.class, propertySpecService, thesaurus);
    }

    @Override
    public String getDisplayName() {
        return getThesaurus().getFormat(PropertyTranslationKeys.TRANSITION_DECOMMISSIONING).format();
    }

    protected String getCIMDateColumnAlias() {
        return "RETIREDDATE";
    }

    @Override
    public String getName() {
        return TransitionDecommissioningDateSearchableProperty.PROPERTY_NAME;
    }
}
