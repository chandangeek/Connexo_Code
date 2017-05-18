package com.energyict.protocolimplv2.security;

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import java.util.Collections;
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
        return Collections.singletonList(DeviceSecurityProperty.PASSWORD.getPropertySpec(propertySpecService));
    }

}