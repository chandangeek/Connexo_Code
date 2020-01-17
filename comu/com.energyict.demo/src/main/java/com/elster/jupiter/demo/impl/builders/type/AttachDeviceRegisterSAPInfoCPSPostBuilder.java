/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.demo.impl.builders.type;

import com.elster.jupiter.cps.CustomPropertySetService;

import javax.inject.Inject;

public class AttachDeviceRegisterSAPInfoCPSPostBuilder extends AbstractAttachDeviceSAPInfoCPSPostBuilder {

    public static final String CPS_ID = "com.energyict.mdc.sap.soap.webservices.impl.custompropertyset.DeviceRegisterSAPInfoCustomPropertySet";

    @Inject
    public AttachDeviceRegisterSAPInfoCPSPostBuilder(CustomPropertySetService customPropertySetService) {
        super(customPropertySetService);
    }

    @Override
    protected String getCpsId() {
        return CPS_ID;
    }
}
