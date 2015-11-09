package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpecService;
import com.google.inject.Inject;

public class TransitionDeactivationDateSearchableProperty extends AbstractTransitionSearchableProperty<TransitionDeactivationDateSearchableProperty> {

    static final String PROPERTY_NAME = "device.transition.deactivation";

    @Inject
    public TransitionDeactivationDateSearchableProperty(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(TransitionDeactivationDateSearchableProperty.class, propertySpecService, thesaurus);
    }

    @Override
    public String getDisplayName() {
        return getThesaurus().getFormat(PropertyTranslationKeys.TRANSITION_DEACTIVATION).format();
    }

    protected String getCIMDateColumnAlias() {
        return "REMOVEDDATE";
    }

    @Override
    public String getName() {
        return TransitionDeactivationDateSearchableProperty.PROPERTY_NAME;
    }
}
