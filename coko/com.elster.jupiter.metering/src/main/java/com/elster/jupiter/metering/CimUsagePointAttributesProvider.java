/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.metering;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;

import com.google.common.collect.ImmutableMap;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Arrays;
import java.util.List;

import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallRelatedAttributesTypeProvider;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;

@Component(name = "CimUsagePointAttributesProvider",
        service = {WebServiceCallRelatedAttributesTypeProvider.class, TranslationKeyProvider.class},
        property = "name=CimUsagePointAttributesProvider", immediate = true)
public class CimUsagePointAttributesProvider implements WebServiceCallRelatedAttributesTypeProvider {

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
        return Arrays.asList(UsagePointAttributesTranslations.values());
    }


    @Override
    public ImmutableMap<String, TranslationKey> getAttributeTranslations() {
        ImmutableMap<String, TranslationKey> types = ImmutableMap.of("CimUsagePointName", UsagePointAttributesTranslations.USAGE_POINT_NAME,
                "CimUsagePointMrID", UsagePointAttributesTranslations.USAGE_POINT_MRID);
        return types;
    }
}
