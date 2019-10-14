/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.CimUsagePointAttributeNames;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;

import com.google.common.collect.ImmutableMap;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Arrays;
import java.util.List;

import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallRelatedAttributeTypeProvider;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;

@Component(name = "CimUsagePointAttributesProvider",
        service = {WebServiceCallRelatedAttributeTypeProvider.class, TranslationKeyProvider.class},
        property = "name=CimUsagePointAttributesProvider", immediate = true)
public class CimUsagePointAttributesProvider implements WebServiceCallRelatedAttributeTypeProvider {

    public static final String COMPONENT_NAME = "WSS";
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
        return Arrays.asList(UsagePointAttributeTranslations.values());
    }

    @Override
    public ImmutableMap<String, TranslationKey> getAttributeTranslations() {
        ImmutableMap<String, TranslationKey> types = ImmutableMap.of(CimUsagePointAttributeNames.CIM_USAGE_POINT_NAME.getAttributeName(), UsagePointAttributeTranslations.USAGE_POINT_NAME,
                CimUsagePointAttributeNames.CIM_USAGE_POINT_MR_ID.getAttributeName(), UsagePointAttributeTranslations.USAGE_POINT_MRID);
        return types;
    }
}
