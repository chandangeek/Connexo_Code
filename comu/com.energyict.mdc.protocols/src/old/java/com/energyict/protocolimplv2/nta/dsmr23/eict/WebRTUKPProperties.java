/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.nta.dsmr23.eict;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;

import com.energyict.protocolimplv2.dlms.DlmsProperties;

import java.util.ArrayList;
import java.util.List;

public class WebRTUKPProperties extends DlmsProperties {

    public WebRTUKPProperties(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(propertySpecService, thesaurus);
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getPropertySpecs());
        propertySpecs.add(booleanSpec(TranslationKeys.IGNORE_DST_STATUS_CODE, false));
        return propertySpecs;
    }

    @Override
    public boolean isIgnoreDSTStatusCode() {
        return getProperties().<Boolean>getTypedProperty(TranslationKeys.IGNORE_DST_STATUS_CODE.getPropertySpecName(), false);
    }
}