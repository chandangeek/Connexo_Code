/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.metering;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallRelatedAttributesTypeProvider;

import com.google.common.collect.ImmutableMap;
import org.osgi.service.component.annotations.Component;

import java.util.Arrays;
import java.util.List;

@Component(name = "CimDeviceAttributesProvider",
        service = {WebServiceCallRelatedAttributesTypeProvider.class, TranslationKeyProvider.class},
        property = "name=CimDeviceAttributesProvider", immediate = true)
public class CimAttributesProvider implements WebServiceCallRelatedAttributesTypeProvider {

    public static final String COMPONENT_NAME = "WSS";

    public String getComponentName(){
        return COMPONENT_NAME;
    }

    public Layer getLayer(){
        return Layer.DOMAIN;
    }

    @Override
    public List<TranslationKey> getKeys(){
        return Arrays.asList(DeviceAttributesTranslations.values());
    }

    @Override
    public ImmutableMap<String, TranslationKey> getAttributeTranslations(){
        ImmutableMap<String, TranslationKey> types = ImmutableMap.of(
                "CimDeviceName", DeviceAttributesTranslations.DEVICE_NAME,
                "CimDeviceMrID", DeviceAttributesTranslations.DEVICE_MRID,
                "CimDeviceSerialNumber", DeviceAttributesTranslations.DEVICE_SERIAL_NUMBER);
        return types;
    }
}
