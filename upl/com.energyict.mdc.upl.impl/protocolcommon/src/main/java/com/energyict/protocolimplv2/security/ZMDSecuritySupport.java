package com.energyict.protocolimplv2.security;

import com.energyict.mdc.upl.properties.PropertySpec;

import java.util.Arrays;
import java.util.List;

/**
 * Created by cisac on 2/10/2016.
 */
public class ZMDSecuritySupport extends DlmsSecuritySupport{

    @Override
    protected List<PropertySpec> getManufactureSpecificSecurityProperties() {
        return Arrays.asList(
                    DeviceSecurityProperty.PASSWORD.getPropertySpec(),
                    getClientMacAddressPropertySpec());
    }

}