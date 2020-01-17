package com.elster.jupiter.demo.impl.builders.type;

import com.elster.jupiter.cps.CustomPropertySetService;

import javax.inject.Inject;

public class AttachDeviceSAPInfoCPSPostBuilder extends AbstractAttachDeviceSAPInfoCPSPostBuilder {

    public static final String CPS_ID = "com.energyict.mdc.sap.soap.webservices.impl.custompropertyset.DeviceSAPInfoCustomPropertySet";

    @Inject
    public AttachDeviceSAPInfoCPSPostBuilder(CustomPropertySetService customPropertySetService) {
        super(customPropertySetService);
    }

    @Override
    protected String getCpsId() {
        return CPS_ID;
    }
}
