/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.builders.type;

import com.elster.jupiter.cps.CustomPropertySetService;

import javax.inject.Inject;

public class AttachEndDeviceSAPInfoCPSPostBuilder extends AbstractAttachDeviceSAPInfoCPSPostBuilder {

    public static final String CPS_ID = "com.energyict.mdc.device.config.cps.DeviceSAPInfoCustomPropertySet";

    @Inject
    public AttachEndDeviceSAPInfoCPSPostBuilder(CustomPropertySetService customPropertySetService) {
        super(customPropertySetService);
    }

    @Override
    protected String getCpsId() {
        return CPS_ID;
    }
}
