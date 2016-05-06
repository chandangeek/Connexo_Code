package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.soap.whiteboard.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.EndPointProvider;
import com.elster.jupiter.soap.whiteboard.SoapProviderSupportFactory;
import com.elster.jupiter.soap.whiteboard.WebServicesService;
import com.elster.jupiter.util.osgi.ContextClassLoaderResource;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.xml.ws.Endpoint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    private Map<String, EndPointProvider> webServices = new HashMap<>();
    private final Map<EndPointProvider, List<Endpoint>> endpoints = new ConcurrentHashMap<>();
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
    public Optional<EndPointProvider> getWebService(String name) {
        return Optional.ofNullable(webServices.get(name));
    }

    @Override
    public void publishEndPoint(EndPointConfiguration endPointConfiguration) {
        if (webServices.containsKey(endPointConfiguration.getWebServiceName())) {
            EndPointProvider endPointProvider = webServices.get(endPointConfiguration.getWebServiceName());
            try (ContextClassLoaderResource ctx = soapProviderSupportFactory.create()) {
                Endpoint endpoint = Endpoint.publish(endPointConfiguration.getUrl(), endPointProvider.get());
                List<Endpoint> endpoints = this.endpoints.getOrDefault(endPointProvider, new ArrayList<>());
                endpoints.add(endpoint);
                this.endpoints.put(endPointProvider, endpoints);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            logger.warning("Could not publish " + endPointConfiguration.getName() + ": the required web service '" + endPointConfiguration
                    .getWebServiceName() + "' is not registered");
        }
    }

    @Override
    public void removeEndPoint(EndPointProvider provider) {
        List<Endpoint> endPoints = endpoints.remove(provider);
        if (endPoints != null) {
            endPoints.stream().forEach(Endpoint::stop);
        }
    }

    @Override
    public List<String> getEndPoints() {
        return endpoints.values()
                .stream()
                .flatMap(Collection::stream)
                .map(ep -> ep.getClass().getName())
                .collect(Collectors.toList());
    }

    public void register(String name, EndPointProvider endPointProvider) {
        webServices.put(name, endPointProvider);
    }

    public void unregister(EndPointProvider provider) {
        if (webServices.containsValue(provider)) {
            List<Endpoint> endPoints = endpoints.remove(provider);
            if (endPoints != null) {
                endPoints.stream().forEach(Endpoint::stop);
            }
            webServices.entrySet()
                    .stream()
                    .filter(s -> s.getValue().equals(provider))
                    .map(Map.Entry::getKey)
                    .findFirst()
                    .ifPresent(webServices::remove);
        }
    }
}
