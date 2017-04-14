/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.protocolimplv2.nta.dsmr23.eict;


import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;

import com.energyict.protocolimplv2.dlms.DlmsProperties;
import com.energyict.protocolimplv2.nta.dsmr23.DlmsConfigurationSupport;

import java.util.ArrayList;
import java.util.List;

public class WebRTUKPConfigurationSupport extends DlmsConfigurationSupport {


    private final Thesaurus thesaurus;

    public WebRTUKPConfigurationSupport(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(propertySpecService);
        this.thesaurus = thesaurus;
    }


    @Override
    public List<PropertySpec> getOptionalProperties() {
        ArrayList<PropertySpec> props = new ArrayList<>(super.getOptionalProperties());
        props.add(getPropertySpecService().booleanSpec()
                .named(DlmsProperties.TranslationKeys.IGNORE_DST_STATUS_CODE.getPropertySpecName(), DlmsProperties.TranslationKeys.IGNORE_DST_STATUS_CODE)
                .fromThesaurus(thesaurus)
                .setDefaultValue(false)
                .finish());
        return props;
    }
}
