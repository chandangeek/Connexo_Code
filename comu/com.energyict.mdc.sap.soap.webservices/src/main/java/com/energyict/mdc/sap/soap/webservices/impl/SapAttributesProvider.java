/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
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

            types.put("SapUtilitiesDeviceID", SapAttributesTranslations.DEVICE_NAME);
            types.put("SapUtilitiesMeasurementTaskID", SapAttributesTranslations.DEVICE_MRID);
            types.put("SapSerialID", SapAttributesTranslations.DEVICE_SERIAL_NUMBER);
            types.put("SapUtilitiesTimeSeriesID", SapAttributesTranslations.TIME_SERIES_ID);

            return types;
        }
}

