package com.elster.jupiter.mdm.usagepoint.data;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicy;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallRelatedObjectTypeProvider;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.elster.jupiter.soap.whiteboard.cxf.impl.WebServicesServiceImpl;

@Component(name = "CimUsagePointAttributesProvider",
        service = {WebServiceCallRelatedObjectTypeProvider.class, TranslationKeyProvider.class},
        property = "name=CimUsagePointAttributesProvider", immediate = true)
public class CimUsagePointAttributesProvider implements WebServiceCallRelatedObjectTypeProvider {

        public static final String COMPONENT_NAME = "WSS";
        private volatile WebServicesService webServicesService;

        public String getComponentName(){
            return COMPONENT_NAME;
        };

        public Layer getLayer(){
            return Layer.DOMAIN;
        };


        @Reference(policy = ReferencePolicy.STATIC)
        public void setWebServicesService(WebServicesService webServicesService){
            this.webServicesService = webServicesService;
        }

        @Override
        public List<TranslationKey> getKeys(){
            return Arrays.asList(UsagePointAttributesTranslations.values());
        };

        @Override
        public Map<String, TranslationKey> getTypes(){
            Map<String, TranslationKey> types = new HashMap<>();

            types.put("CimUsagePointName", UsagePointAttributesTranslations.USAGE_POINT_NAME);
            types.put("CimUsagePointMrID", UsagePointAttributesTranslations.USAGE_POINT_MRID);

            return types;
        }
}
