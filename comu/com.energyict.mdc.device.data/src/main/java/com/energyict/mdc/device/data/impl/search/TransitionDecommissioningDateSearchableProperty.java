/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.PropertySpecService;

import com.google.inject.Inject;

public class TransitionDecommissioningDateSearchableProperty extends AbstractTransitionSearchableProperty<TransitionDecommissioningDateSearchableProperty> {

    static final String PROPERTY_NAME = "device.transition.decommissioning";

    @Inject
    public TransitionDecommissioningDateSearchableProperty(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(TransitionDecommissioningDateSearchableProperty.class, propertySpecService, thesaurus);
    }

    @Override
    protected TranslationKey getNameTranslationKey() {
        return PropertyTranslationKeys.TRANSITION_DECOMMISSIONING;
    }

    protected String getCIMDateColumnAlias() {
        return "RETIREDDATE";
    }

    @Override
    public String getName() {
        return TransitionDecommissioningDateSearchableProperty.PROPERTY_NAME;
    }
}
