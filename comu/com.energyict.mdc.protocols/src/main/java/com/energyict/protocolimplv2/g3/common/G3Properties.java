package com.energyict.protocolimplv2.g3.common;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;

import com.energyict.protocolimplv2.dlms.DlmsProperties;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Common G3 identification properties for the DeviceProtocol versions
 *
 * Copyrights EnergyICT
 * Date: 1/6/15
 * Time: 10:56 AM
 */
public class G3Properties extends DlmsProperties {

    public static final String G3_MAC_ADDRESS_PROP_NAME = "MAC_address";
    public static final String G3_SHORT_ADDRESS_PROP_NAME = "Short_MAC_address";
    public static final String G3_LOGICAL_DEVICE_ID_PROP_NAME = "Logical_device_id";

    public G3Properties(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(propertySpecService, thesaurus);
    }

    public PropertySpec getMacAddressPropertySPec() {
        return getPropertySpecService().stringPropertySpec(G3_MAC_ADDRESS_PROP_NAME, false, "");
    }

    public PropertySpec getShortAddressPropertySpec() {
        return getPropertySpecService().bigDecimalPropertySpec(G3_SHORT_ADDRESS_PROP_NAME, false, BigDecimal.valueOf(-1));
    }

    public PropertySpec getLogicalDeviceIdPropertySpec() {
        return getPropertySpecService().bigDecimalPropertySpec(G3_LOGICAL_DEVICE_ID_PROP_NAME, false, BigDecimal.ZERO);
    }

    @Override
    public int getServerUpperMacAddress() {
        return parseBigDecimalProperty(G3_LOGICAL_DEVICE_ID_PROP_NAME, DEFAULT_UPPER_SERVER_MAC_ADDRESS);
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getPropertySpecs());
        propertySpecs.addAll(Arrays.asList(
                getMacAddressPropertySPec(),
                getShortAddressPropertySpec(),
                getLogicalDeviceIdPropertySpec()));
        return propertySpecs;
    }
}
