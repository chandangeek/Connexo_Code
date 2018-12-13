/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.PropertySpecService;

import com.google.inject.Inject;

public class TransitionShipmentDateSearchableProperty extends AbstractTransitionSearchableProperty<TransitionShipmentDateSearchableProperty> {

    static final String PROPERTY_NAME = "device.transition.shipment";

    @Inject
    public TransitionShipmentDateSearchableProperty(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(TransitionShipmentDateSearchableProperty.class, propertySpecService, thesaurus);
    }

    @Override
    protected TranslationKey getNameTranslationKey() {
        return PropertyTranslationKeys.TRANSITION_SHIPMENT;
    }

    protected String getCIMDateColumnAlias() {
        return "RECEIVEDDATE";
    }

    @Override
    public String getName() {
        return TransitionShipmentDateSearchableProperty.PROPERTY_NAME;
    }

}