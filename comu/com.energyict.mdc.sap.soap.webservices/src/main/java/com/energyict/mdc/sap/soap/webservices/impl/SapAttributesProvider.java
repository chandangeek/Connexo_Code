/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallRelatedAttributesTypeProvider;

import com.google.common.collect.ImmutableMap;
import org.osgi.service.component.annotations.Component;

import java.util.Arrays;
import java.util.List;

@Component(name = "SapAttributesProvider",
            service = {WebServiceCallRelatedAttributesTypeProvider.class, TranslationKeyProvider.class},
            property = "name=SapAttributesProvider", immediate = true)
public class SapAttributesProvider implements WebServiceCallRelatedAttributesTypeProvider {

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
        public ImmutableMap<String, TranslationKey> getAttributeTranslations(){
            ImmutableMap<String, TranslationKey> types = ImmutableMap.of(
                    "SapUtilitiesDeviceID", SapAttributesTranslations.DEVICE_UTIL_DEVICE_ID,
                    "SapUtilitiesMeasurementTaskID", SapAttributesTranslations.DEVICE_MRID,
                    "SapSerialID", SapAttributesTranslations.DEVICE_SERIAL_NUMBER,
                    "SapUtilitiesTimeSeriesID", SapAttributesTranslations.TIME_SERIES_ID
            );

            return types;
        }
}

