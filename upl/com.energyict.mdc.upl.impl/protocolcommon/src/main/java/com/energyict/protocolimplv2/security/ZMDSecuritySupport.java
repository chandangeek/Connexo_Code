package com.energyict.protocolimplv2.security;

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import java.util.Arrays;
import java.util.List;

/**
 * Created by cisac on 2/10/2016.
 */
public class ZMDSecuritySupport extends DlmsSecuritySupport{

    public ZMDSecuritySupport(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    protected List<PropertySpec> getManufactureSpecificSecurityProperties(PropertySpecService propertySpecService) {
        return Arrays.asList(
                    DeviceSecurityProperty.PASSWORD.getPropertySpec(propertySpecService),
                    getClientMacAddressPropertySpec(propertySpecService));
    }

}