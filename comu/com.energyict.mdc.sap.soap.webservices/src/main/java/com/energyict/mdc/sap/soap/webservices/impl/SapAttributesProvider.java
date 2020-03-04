/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallRelatedAttributeTypeProvider;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;

import com.energyict.mdc.sap.soap.webservices.SapAttributeNames;

import com.google.common.collect.ImmutableMap;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Arrays;
import java.util.List;

@Component(name = "SapAttributesProvider",
            service = {WebServiceCallRelatedAttributeTypeProvider.class, TranslationKeyProvider.class},
            property = "name=SapAttributesProvider", immediate = true)
public class SapAttributesProvider implements WebServiceCallRelatedAttributeTypeProvider {

    public static final String COMPONENT_NAME = "SAP";
    private volatile WebServicesService webServicesService;

    public String getComponentName() {
        return COMPONENT_NAME;
    }

    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Reference
    public void setWebServicesService(WebServicesService webServicesService) {
        this.webServicesService = webServicesService;
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Arrays.asList(SapAttributesTranslations.values());
    }

    @Override
    public ImmutableMap<String, TranslationKey> getAttributeTranslations() {
        ImmutableMap<String, TranslationKey> types = ImmutableMap.of(
                SapAttributeNames.SAP_UTILITIES_DEVICE_ID.getAttributeName(), SapAttributesTranslations.SAP_DEVICE_UTIL_DEVICE_ID,
                SapAttributeNames.SAP_UTILITIES_MEASUREMENT_TASK_ID.getAttributeName(), SapAttributesTranslations.SAP_UTIL_MEASUREMENT_TASK_ID,
                SapAttributeNames.SAP_UTILITIES_TIME_SERIES_ID.getAttributeName(), SapAttributesTranslations.SAP_TIME_SERIES_ID,
                SapAttributeNames.SAP_METER_READING_DOCUMENT_ID.getAttributeName(), SapAttributesTranslations.SAP_METER_READING_DOCUMENT_ID
        );

        return types;
    }
}

