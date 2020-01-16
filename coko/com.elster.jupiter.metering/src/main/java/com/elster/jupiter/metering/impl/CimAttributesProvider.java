/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.CimAttributeNames;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallRelatedAttributeTypeProvider;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;

import com.google.common.collect.ImmutableMap;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Arrays;
import java.util.List;

@Component(name = "CimDeviceAttributesProvider",
        service = {WebServiceCallRelatedAttributeTypeProvider.class, TranslationKeyProvider.class},
        property = "name=CimDeviceAttributesProvider", immediate = true)
public class CimAttributesProvider implements WebServiceCallRelatedAttributeTypeProvider {

    public static final String COMPONENT_NAME = "WSS";
    private volatile WebServicesService webServicesService;

    public String getComponentName(){
        return COMPONENT_NAME;
    }

    public Layer getLayer(){
        return Layer.DOMAIN;
    }

    @Reference
    public void setWebServicesService(WebServicesService webServicesService) {
        this.webServicesService = webServicesService;
    }

    @Override
    public List<TranslationKey> getKeys(){
        return Arrays.asList(DeviceAttributesTranslations.values());
    }

    @Override
    public ImmutableMap<String, TranslationKey> getAttributeTranslations(){
        ImmutableMap<String, TranslationKey> types = ImmutableMap.of(
                CimAttributeNames.CIM_DEVICE_NAME.getAttributeName(), DeviceAttributesTranslations.DEVICE_NAME,
                CimAttributeNames.CIM_DEVICE_MR_ID.getAttributeName(), DeviceAttributesTranslations.DEVICE_MRID,
                CimAttributeNames.SERIAL_ID.getAttributeName(), DeviceAttributesTranslations.DEVICE_SERIAL_ID,
                CimAttributeNames.CIM_DEVICE_SERIAL_NUMBER.getAttributeName(), DeviceAttributesTranslations.DEVICE_SERIAL_NUMBER);
        return types;
    }
}
