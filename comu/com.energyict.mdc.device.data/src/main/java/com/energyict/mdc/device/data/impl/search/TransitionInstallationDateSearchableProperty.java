package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpecService;
import com.google.inject.Inject;

public class TransitionInstallationDateSearchableProperty extends AbstractTransitionSearchableProperty<TransitionInstallationDateSearchableProperty> {

    static final String PROPERTY_NAME = "device.transition.installation";

    @Inject
    public TransitionInstallationDateSearchableProperty(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(TransitionInstallationDateSearchableProperty.class, propertySpecService, thesaurus);
    }

    @Override
    public String getDisplayName() {
        return getThesaurus().getFormat(PropertyTranslationKeys.TRANSITION_INSTALLATION).format();
    }

    protected String getCIMDateColumnAlias() {
        return "INSTALLEDDATE";
    }

    @Override
    public String getName() {
        return TransitionInstallationDateSearchableProperty.PROPERTY_NAME;
    }
}
