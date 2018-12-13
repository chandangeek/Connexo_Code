/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.PropertySpecService;

import com.google.inject.Inject;

public class TransitionInstallationDateSearchableProperty extends AbstractTransitionSearchableProperty<TransitionInstallationDateSearchableProperty> {

    static final String PROPERTY_NAME = "device.transition.installation";

    @Inject
    public TransitionInstallationDateSearchableProperty(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(TransitionInstallationDateSearchableProperty.class, propertySpecService, thesaurus);
    }

    @Override
    protected TranslationKey getNameTranslationKey() {
        return PropertyTranslationKeys.TRANSITION_INSTALLATION;
    }

    protected String getCIMDateColumnAlias() {
        return "INSTALLEDDATE";
    }

    @Override
    public String getName() {
        return TransitionInstallationDateSearchableProperty.PROPERTY_NAME;
    }

}