package com.energyict.protocolimplv2.dlms.idis.am500.properties;

import com.energyict.mdc.upl.properties.PropertySpec;

import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimplv2.dlms.idis.am130.properties.AM130ConfigurationSupport;

import java.util.ArrayList;
import java.util.List;

/**
 * Same like the AM130 properties, adds 'CallingAPTitle' and 'SwapServerAndClientAddress'
 * <p/>
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 19/12/2014 - 15:53
 */
public class IDISConfigurationSupport extends AM130ConfigurationSupport {

    public static final String SWAP_SERVER_AND_CLIENT_ADDRESS_PROPERTY = "SwapServerAndClientAddress";
    public static final String IGNORE_CALLING_AP_TITLE = "IgnoreCallingAPTitle";
    public static final String USE_LOGICAL_DEVICE_NAME_AS_SERIAL = "UseLogicalDeviceNameAsSerialNumber";
    public static final String USE_UNDEFINED_AS_TIME_DEVIATION = "UseUndefinedAsTimeDeviation";

    public IDISConfigurationSupport() {
        super(propertySpecService);
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        List<PropertySpec> result = new ArrayList<>(super.getPropertySpecs());
        result.add(this.swapServerAndClientAddress());
        result.add(this.ignoreCallingAPTitle());
        result.add(this.useLogicalDeviceNameAsSerialNumber());
        result.add(this.useUndefinedAsTimeDeviation());

        // Not supported in IDIS P1
        result.remove(super.useGeneralBlockTransferPropertySpec());
        result.remove(super.generalBlockTransferWindowSizePropertySpec());
        result.remove(super.cipheringTypePropertySpec());
        return result;
    }

    protected PropertySpec swapServerAndClientAddress() {
        return UPLPropertySpecFactory.booleanValue(SWAP_SERVER_AND_CLIENT_ADDRESS_PROPERTY, false, true);
    }

    protected PropertySpec ignoreCallingAPTitle() {
        return UPLPropertySpecFactory.booleanValue(IGNORE_CALLING_AP_TITLE, false, false);
    }

    private PropertySpec useLogicalDeviceNameAsSerialNumber() {
        return UPLPropertySpecFactory.booleanValue(USE_LOGICAL_DEVICE_NAME_AS_SERIAL, false, false);
    }

    private PropertySpec useUndefinedAsTimeDeviation() {
        return UPLPropertySpecFactory.booleanValue(USE_UNDEFINED_AS_TIME_DEVIATION, false, false);
    }
}