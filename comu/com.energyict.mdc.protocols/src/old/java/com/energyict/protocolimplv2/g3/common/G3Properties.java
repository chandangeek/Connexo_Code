/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.g3.common;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;

import com.energyict.protocolimplv2.dlms.DlmsProperties;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class G3Properties extends DlmsProperties {

    public static final String G3_MAC_ADDRESS_PROP_NAME = TranslationKeys.G3_MAC_ADDRESS_PROP_NAME_TK.getPropertySpecName();
    public static final String G3_SHORT_ADDRESS_PROP_NAME = TranslationKeys.G3_SHORT_ADDRESS_PROP_NAME_TK.getPropertySpecName();
    public static final String G3_LOGICAL_DEVICE_ID_PROP_NAME = TranslationKeys.G3_LOGICAL_DEVICE_ID_PROP_NAME_TK.getPropertySpecName();

    public G3Properties(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(propertySpecService, thesaurus);
    }

    public PropertySpec getMacAddressPropertySPec() {
        return this.stringSpec(TranslationKeys.G3_MAC_ADDRESS_PROP_NAME_TK, "");
    }

    public PropertySpec getShortAddressPropertySpec() {
        return this.bigDecimalSpec(TranslationKeys.G3_SHORT_ADDRESS_PROP_NAME_TK, BigDecimal.valueOf(-1));
    }

    public PropertySpec getLogicalDeviceIdPropertySpec() {
        return this.bigDecimalSpec(TranslationKeys.G3_LOGICAL_DEVICE_ID_PROP_NAME_TK, BigDecimal.ZERO);
    }

    @Override
    public int getServerUpperMacAddress() {
        return parseBigDecimalProperty(G3_LOGICAL_DEVICE_ID_PROP_NAME, DEFAULT_UPPER_SERVER_MAC_ADDRESS);
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getPropertySpecs());
        Collections.addAll(
                propertySpecs,
                getMacAddressPropertySPec(),
                getShortAddressPropertySpec(),
                getLogicalDeviceIdPropertySpec());
        return propertySpecs;
    }

}