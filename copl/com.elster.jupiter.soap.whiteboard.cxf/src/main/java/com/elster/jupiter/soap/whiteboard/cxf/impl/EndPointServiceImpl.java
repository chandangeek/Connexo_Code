package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointService;
import com.elster.jupiter.soap.whiteboard.cxf.InboundSoapEndPointProvider;

import org.osgi.service.component.annotations.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component(name = "com.elster.jupiter.soap.webservices.endpointservice", service = {EndPointService.class}, property = {"name=" + EndPointService.COMPONENT_NAME}, immediate = true)
public class EndPointServiceImpl implements EndPointService {

    private Map<String, EndPointFactory> webServices = new ConcurrentHashMap<>();

    // OSGi
    public EndPointServiceImpl(){
    }

    @Override
    public Map<String, EndPointFactory> getWebServices() {
        return webServices;
    }

    @Override
    public void addWebService(String name, EndPointFactory factory) {
        webServices.put(name, factory);
    }

    @Override
    public EndPointFactory removeWebService(String serviceName) {
        return webServices.remove(serviceName);
    }

    @Override
    public EndPointFactory getEndPointFactory(String serviceName) {
        return webServices.get(serviceName);
    }

    @Override
    public List<PropertySpec> getWebServicePropertySpecs(String webServiceName) {
        final EndPointFactory endPointFactory = webServices.get(webServiceName);
        if (endPointFactory != null && endPointFactory.getEndPointProvider() instanceof InboundSoapEndPointProvider) {
            InboundSoapEndPointProvider provider = (InboundSoapEndPointProvider) endPointFactory.getEndPointProvider();
            return provider.get().getPropertySpecs();
        } else {
            return new ArrayList<>();
        }
    }
}
