package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.soap.whiteboard.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.InboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.OutboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.SoapProviderSupportFactory;
import com.elster.jupiter.soap.whiteboard.WebServicesService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by bvn on 4/29/16.
 */
@Component(name = "com.elster.jupiter.soap.webservices.cxf", service = {WebServicesService.class}, immediate = true)
public class WebServicesServiceImpl implements WebServicesService {
    private static final Logger logger = Logger.getLogger("WebServicesServiceImpl");

    public static final String COMPONENT_NAME = "WSCXF";

    private Map<String, ManagedEndpoint> webServices = new HashMap<>();
    private final Map<EndPointConfiguration, ManagedEndpoint> endpoints = new ConcurrentHashMap<>();
    private SoapProviderSupportFactory soapProviderSupportFactory;

    @Reference
    public void setSoapProviderSupportFactory(SoapProviderSupportFactory soapProviderSupportFactory) {
        this.soapProviderSupportFactory = soapProviderSupportFactory;
    }

    @Override
    public List<String> getWebServices() {
        return new ArrayList<>(webServices.keySet());
    }

    @Override
    public void publishEndPoint(EndPointConfiguration endPointConfiguration) {
        if (webServices.containsKey(endPointConfiguration.getWebServiceName())) {
            webServices.get(endPointConfiguration.getWebServiceName()).publish(endPointConfiguration);
        } else {
            logger.warning("Could not publish " + endPointConfiguration.getName() + ": the required web service '" + endPointConfiguration
                    .getWebServiceName() + "' is not registered");
        }
    }

    @Override
    public void removeEndPoint(EndPointConfiguration endPointConfiguration) {
        ManagedEndpoint endpoint = endpoints.remove(endPointConfiguration);
        if (endpoint != null) {
            endpoint.stop();
        }
    }

    @Override
    public List<String> getEndPoints() {
        return endpoints.values()
                .stream()
                .map(ep -> ep.getClass().getName())
                .collect(Collectors.toList());
    }

    // called by whiteboard
    public void register(String name, InboundEndPointProvider endPointProvider) {
        webServices.put(name, new InboundEndPoint(endPointProvider, soapProviderSupportFactory));
    }

    // called by whiteboard
    public void register(String name, OutboundEndPointProvider endPointProvider) {
        webServices.put(name, new OutboundEndPoint(endPointProvider, soapProviderSupportFactory));
    }

    // called by whiteboard
    public void unregister(String webServiceName) {
        if (webServices.containsKey(webServiceName)) {
            webServices.remove(webServiceName);
            endpoints.entrySet()
                    .stream()
                    .filter(e -> e.getKey().getWebServiceName().equals(webServiceName))
                    .forEach(e -> e.getValue().stop());
        }
    }
}
