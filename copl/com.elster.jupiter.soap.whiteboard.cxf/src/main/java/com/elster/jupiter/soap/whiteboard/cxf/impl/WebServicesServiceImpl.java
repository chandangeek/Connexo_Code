package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.soap.whiteboard.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.InboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.OutboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.SoapProviderSupportFactory;
import com.elster.jupiter.soap.whiteboard.WebServicesService;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import java.util.ArrayList;
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

    private Map<String, WebService> webServices = new ConcurrentHashMap<>();
    private final Map<EndPointConfiguration, ManagedEndpoint> endpoints = new ConcurrentHashMap<>();
    private volatile SoapProviderSupportFactory soapProviderSupportFactory;
    private volatile BundleContext bundleContext;
    private volatile DataModel dataModel;

    @Reference
    public void setSoapProviderSupportFactory(SoapProviderSupportFactory soapProviderSupportFactory) {
        this.soapProviderSupportFactory = soapProviderSupportFactory;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.dataModel = ormService.newDataModel("WebServicesService", "Injector for web services");
    }

    @Override
    public List<String> getWebServices() {
        return new ArrayList<>(webServices.keySet());
    }

    @Override
    public void publishEndPoint(EndPointConfiguration endPointConfiguration) {
        if (webServices.containsKey(endPointConfiguration.getWebServiceName())) {
            ManagedEndpoint managedEndpoint = webServices.get(endPointConfiguration.getWebServiceName())
                    .createEndpoint();
            managedEndpoint.publish(endPointConfiguration);
            endpoints.put(endPointConfiguration, managedEndpoint);
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

    @Override
    public boolean isInbound(String webServiceName) {
        if (webServices.containsKey(webServiceName)) {
            return webServices.get(webServiceName).isInbound();
        } else {
            throw new IllegalArgumentException("No such web service");
        }
    }

    // called by whiteboard
    public void register(String name, InboundEndPointProvider endPointProvider) {
        webServices.put(name, dataModel.getInstance(InboundWebServiceImpl.class).init(name, endPointProvider));
    }

    // called by whiteboard
    public void register(String name, OutboundEndPointProvider endPointProvider) {
        webServices.put(name, dataModel.getInstance(OutboundWebServiceImpl.class).init(name, endPointProvider));
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

    @Activate
    public void start(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        this.dataModel.register(this.getModule());
    }

    @Deactivate
    public void stop(BundleContext bundleContext) {
        endpoints.values().stream().forEach(ManagedEndpoint::stop);
    }

    private Module getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                bind(DataModel.class).toInstance(dataModel);
                bind(BundleContext.class).toInstance(bundleContext);
                bind(SoapProviderSupportFactory.class).toInstance(soapProviderSupportFactory);
            }
        };
    }

}
