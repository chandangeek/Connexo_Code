package com.energyict.mdc.sap.soap.webservices.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallRelatedObjectTypeProvider;
import com.energyict.mdc.device.data.DeviceAttributesTranslations;

import org.osgi.service.component.annotations.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component(name = "SapAttributesProvider",
            service = {WebServiceCallRelatedObjectTypeProvider.class, TranslationKeyProvider.class},
            property = "name=SapAttributesProvider", immediate = true)
public class SapAttributesProvider implements WebServiceCallRelatedObjectTypeProvider {

        public static final String COMPONENT_NAME = "SAP";
        //extends TranslationKeyProvider

        public String getComponentName(){
            return COMPONENT_NAME;
        };

        public Layer getLayer(){
            return Layer.DOMAIN;
        };

        @Override
        public List<TranslationKey> getKeys(){
            return Arrays.asList(SapAttributesTranslations.values());
        };

        @Override
        public Map<String, TranslationKey> getTypes(){
            Map<String, TranslationKey> types = new HashMap<>();

            types.put("SapUtilitiesDeviceID", DeviceAttributesTranslations.DEVICE_NAME);
            types.put("SapUtilitiesMeasurementTaskID", DeviceAttributesTranslations.DEVICE_MRID);
            types.put("SapSerialID", DeviceAttributesTranslations.DEVICE_SERIAL_NUMBER);

            return types;
        }
}

