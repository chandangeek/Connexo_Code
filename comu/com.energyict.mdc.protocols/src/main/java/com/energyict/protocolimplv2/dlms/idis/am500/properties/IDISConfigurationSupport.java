package com.energyict.protocolimplv2.dlms.idis.am500.properties;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;

import com.energyict.protocolimplv2.dlms.idis.am130.properties.AM130ConfigurationSupport;

import java.util.ArrayList;
import java.util.List;

/**
 * Same like the AM130 properties, adds 'CallingAPTitle' and 'SwapServerAndClientAddress'
 * <p>
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

    public IDISConfigurationSupport(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        List<PropertySpec> result = new ArrayList<>();
        result.addAll(super.getOptionalProperties());
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
        return getPropertySpecService().booleanSpec()
                .named(SWAP_SERVER_AND_CLIENT_ADDRESS_PROPERTY, SWAP_SERVER_AND_CLIENT_ADDRESS_PROPERTY)
                .describedAs(SWAP_SERVER_AND_CLIENT_ADDRESS_PROPERTY)
                .setDefaultValue(false)
                .finish();
    }

    protected PropertySpec ignoreCallingAPTitle() {
        return getPropertySpecService().booleanSpec().named(IGNORE_CALLING_AP_TITLE, IGNORE_CALLING_AP_TITLE).describedAs(IGNORE_CALLING_AP_TITLE).setDefaultValue(false).finish();
    }

    private PropertySpec useLogicalDeviceNameAsSerialNumber() {
        return getPropertySpecService().booleanSpec()
                .named(USE_LOGICAL_DEVICE_NAME_AS_SERIAL, USE_LOGICAL_DEVICE_NAME_AS_SERIAL)
                .describedAs(USE_LOGICAL_DEVICE_NAME_AS_SERIAL)
                .setDefaultValue(false)
                .finish();
    }

    private PropertySpec useUndefinedAsTimeDeviation() {
        return getPropertySpecService().booleanSpec()
                .named(USE_UNDEFINED_AS_TIME_DEVIATION, USE_UNDEFINED_AS_TIME_DEVIATION)
                .describedAs(USE_UNDEFINED_AS_TIME_DEVIATION)
                .setDefaultValue(false)
                .finish();
    }
}