/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.PropertySpecService;

import com.google.inject.Inject;

public class TransitionDeactivationDateSearchableProperty extends AbstractTransitionSearchableProperty<TransitionDeactivationDateSearchableProperty> {

    static final String PROPERTY_NAME = "device.transition.deactivation";

    @Inject
    public TransitionDeactivationDateSearchableProperty(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(TransitionDeactivationDateSearchableProperty.class, propertySpecService, thesaurus);
    }

    @Override
    protected TranslationKey getNameTranslationKey() {
        return PropertyTranslationKeys.TRANSITION_DEACTIVATION;
    }

    protected String getCIMDateColumnAlias() {
        return "REMOVEDDATE";
    }

    @Override
    public String getName() {
        return TransitionDeactivationDateSearchableProperty.PROPERTY_NAME;
    }
}
