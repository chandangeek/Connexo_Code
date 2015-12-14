package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpecService;
import com.google.inject.Inject;

public class TransitionShipmentDateSearchableProperty extends AbstractTransitionSearchableProperty<TransitionShipmentDateSearchableProperty> {

    static final String PROPERTY_NAME = "device.transition.shipment";

    @Inject
    public TransitionShipmentDateSearchableProperty(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(TransitionShipmentDateSearchableProperty.class, propertySpecService, thesaurus);
    }

    @Override
    public String getDisplayName() {
        return getThesaurus().getFormat(PropertyTranslationKeys.TRANSITION_SHIPMENT).format();
    }

    protected String getCIMDateColumnAlias() {
        return "RECEIVEDDATE";
    }

    @Override
    public String getName() {
        return TransitionShipmentDateSearchableProperty.PROPERTY_NAME;
    }
}
