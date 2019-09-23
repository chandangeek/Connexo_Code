package com.energyict.mdc.device.data;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallRelatedObjectTypeProvider;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceRequestAttributesNames;

import org.osgi.service.component.annotations.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component(name = "CimDeviceAttributesProvider",
        service = {WebServiceCallRelatedObjectTypeProvider.class, TranslationKeyProvider.class},
        property = "name=CimDeviceAttributesProvider", immediate = true)
public class CimAttributesProvider implements WebServiceCallRelatedObjectTypeProvider {

    public static final String COMPONENT_NAME = "WSS";
    //extends TranslationKeyProvider

    public String getComponentName(){
        return COMPONENT_NAME;
    };

    public Layer getLayer(){
        return Layer.DOMAIN;
    };

    @Override
    public List<TranslationKey> getKeys(){
        return Arrays.asList(DeviceAttributesTranslations.values());
    };

    @Override
    public Map<String, TranslationKey> getTypes(){
        Map<String, TranslationKey> types = new HashMap<>();

        types.put("CimDeviceName", DeviceAttributesTranslations.DEVICE_NAME);
        types.put("CimDeviceMrID", DeviceAttributesTranslations.DEVICE_MRID);
        types.put("CimDeviceSerialNumber", DeviceAttributesTranslations.DEVICE_SERIAL_NUMBER);

        return types;
    }
}
