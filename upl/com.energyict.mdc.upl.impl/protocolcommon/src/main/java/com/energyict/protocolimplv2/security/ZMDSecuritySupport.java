package com.energyict.protocolimplv2.security;

import com.energyict.cpo.PropertySpec;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cisac on 2/10/2016.
 */
public class ZMDSecuritySupport extends DlmsSecuritySupport{

    @Override
    protected List<PropertySpec> getManufactureSpecificSecurityProperties() {
        List<PropertySpec> propertySpecs = new ArrayList<>();
        propertySpecs.add(DeviceSecurityProperty.PASSWORD.getPropertySpec());
        propertySpecs.add(getClientMacAddressPropertySpec());
        return propertySpecs;
    }
}
