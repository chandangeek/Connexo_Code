/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.eict.rtuplusserver.idis.properties;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;

import com.energyict.protocolimpl.dlms.idis.IDIS;
import com.energyict.protocolimplv2.dlms.DlmsProperties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A collection of general DLMS properties that are relevant for the IDIS gateway protocol.
 * These properties are not related to the security or the protocol dialects.
 * The parsing and the usage of the property values is done in an implementation of {@link com.energyict.dlms.protocolimplv2.DlmsSessionProperties}
 * <p>
 *
 * @author sva
 * @since 15/10/2014 - 11:16
 */
public class IDISGatewayDynamicPropertySupportSupport extends DlmsProperties {


    public IDISGatewayDynamicPropertySupportSupport(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(propertySpecService, thesaurus);
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getPropertySpecs());
        Collections.addAll(propertySpecs, this.callingAPTitlePropertySpec(), this.nodeAddressPropertySpec());
        return propertySpecs;
    }

    public PropertySpec callingAPTitlePropertySpec() {
        return this.stringSpec(TranslationKeys.CALLING_AP_TITLE_TK, IDIS.CALLING_AP_TITLE_DEFAULT);
    }

    public PropertySpec nodeAddressPropertySpec() {
        return this.getPropertySpecService()
                .stringSpec()
                .named(TranslationKeys.NODEID_TK)
                .fromThesaurus(this.getThesaurus())
                .finish();
    }

}