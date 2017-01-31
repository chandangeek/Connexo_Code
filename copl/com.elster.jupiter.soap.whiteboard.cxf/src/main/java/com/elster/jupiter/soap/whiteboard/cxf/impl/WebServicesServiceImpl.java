/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EventType;
import com.elster.jupiter.soap.whiteboard.cxf.InboundRestEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.InboundSoapEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.LogLevel;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundRestEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.SoapProviderSupportFactory;
import com.elster.jupiter.soap.whiteboard.cxf.WebService;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceProtocol;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.elster.jupiter.soap.whiteboard.cxf.impl.rest.InboundRestEndPointFactoryImpl;
import com.elster.jupiter.soap.whiteboard.cxf.impl.rest.OutboundRestEndPointFactoryImpl;
import com.elster.jupiter.soap.whiteboard.cxf.impl.soap.InboundSoapEndPointFactoryImpl;
import com.elster.jupiter.soap.whiteboard.cxf.impl.soap.OutboundSoapEndPointFactoryImpl;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.osgi.BundleWaiter;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.name.Names;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import static java.util.stream.Collectors.toList;

/**
 * Created by bvn on 4/29/16.
 */
@Component(name = "com.elster.jupiter.soap.webservices.cxf", service = {}, immediate = true)
public class WebServicesServiceImpl implements WebServicesService , BundleWaiter.Startable{
    private static final Logger logger = Logger.getLogger("WebServicesServiceImpl");

    private volatile ServiceRegistration<WebServicesService> registration;
    private Map<String, EndPointFactory> webServices = new ConcurrentHashMap<>();
    private final Map<EndPointConfiguration, ManagedEndpoint> endpoints = new ConcurrentHashMap<>();
    private volatile SoapProviderSupportFactory soapProviderSupportFactory;
    private volatile BundleContext bundleContext;
    private volatile DataModel dataModel;
    private volatile UpgradeService upgradeService;
    private volatile EventService eventService;
    private volatile Thesaurus thesaurus;
    private volatile UserService userService;
    private volatile TransactionService transactionService;
    private volatile HttpService httpService;

    // OSGi
    public WebServicesServiceImpl() {
    }

    @Inject // For test purposes only
    public WebServicesServiceImpl(SoapProviderSupportFactory soapProviderSupportFactory, OrmService ormService,
                                  UpgradeService upgradeService, BundleContext bundleContext, EventService eventService,
                                  UserService userService, NlsService nlsService, TransactionService transactionService,
                                  HttpService httpService) {
        setSoapProviderSupportFactory(soapProviderSupportFactory);
        setOrmService(ormService);
        setUpgradeService(upgradeService);
        setEventService(eventService);
        setNlsService(nlsService);
        setUserService(userService);
        setTransactionService(transactionService);
        setHttpService(httpService);
        activate(bundleContext);
    }

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    @Reference
    public void setSoapProviderSupportFactory(SoapProviderSupportFactory soapProviderSupportFactory) {
        this.soapProviderSupportFactory = soapProviderSupportFactory;
    }

    @Reference
    public void setHttpService(HttpService httpService) {
        this.httpService = httpService;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.dataModel = ormService.newDataModel("WebServicesService", "Injector for web services");
        for (TableSpecs tableSpecs : TableSpecs.values()) {
            tableSpecs.addTo(dataModel);
        }
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        thesaurus = nlsService.getThesaurus(COMPONENT_NAME, Layer.DOMAIN);
    }

    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Override
    public Optional<WebService> getWebService(String webServiceName) {
        final EndPointFactory endPointFactory = webServices.get(webServiceName);
        if (endPointFactory != null) {
            return Optional.of(new WebService() {
                @Override
                public String getName() {
                    return webServiceName;
                }

                @Override
                public boolean isInbound() {
                    return endPointFactory.isInbound();
                }

                @Override
                public WebServiceProtocol getProtocol() {
                    return webServices.get(webServiceName).getProtocol();
                }
            });
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void removeAllEndPoints() {
        endpoints.entrySet().stream().forEach(entry -> {
            EndPointConfiguration endPointConfiguration = entry.getKey();
            ManagedEndpoint managedEndpoint = entry.getValue();
            String msg = "Stopping WebService " + endPointConfiguration.getWebServiceName() + " with config " + endPointConfiguration
                    .getName();
            logger.info(msg);
            endPointConfiguration.log(LogLevel.FINE, msg);
            managedEndpoint.stop();
        });
        endpoints.clear();
    }

    @Override
    public List<WebService> getWebServices() {
        return webServices.entrySet().stream().map(e -> new WebService() {

            @Override
            public String getName() {
                return e.getKey();
            }

            @Override
            public boolean isInbound() {
                return e.getValue().isInbound();
            }

            @Override
            public WebServiceProtocol getProtocol() {
                return e.getValue().getProtocol();
            }
        }).collect(toList());
    }

    @Override
    public void publishEndPoint(EndPointConfiguration endPointConfiguration) {
        EndPointFactory endPointFactory = webServices.get(endPointConfiguration.getWebServiceName());
        if (endPointFactory != null) {
            try {
                ManagedEndpoint managedEndpoint = endPointFactory.createEndpoint(endPointConfiguration);
                managedEndpoint.publish();
                endpoints.put(endPointConfiguration, managedEndpoint);
            } catch (Exception e) {
                endPointConfiguration.log("Failed to publish endpoint " + endPointConfiguration.getName(), e);
            }
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
    public List<EndPointConfiguration> getPublishedEndPoints() {
        return endpoints.entrySet()
                .stream()
                .filter(set -> set.getValue().isPublished())
                .map(Map.Entry::getKey)
                .collect(toList());
    }

    @Override
    public boolean isInbound(String webServiceName) {
        EndPointFactory endPointFactory = webServices.get(webServiceName);
        if (endPointFactory != null) {
            return endPointFactory.isInbound();
        } else {
            throw new IllegalArgumentException("No such web service");
        }
    }

    @Override
    public boolean isPublished(EndPointConfiguration endPointConfiguration) {
        ManagedEndpoint managedEndpoint = endpoints.get(endPointConfiguration);
        return managedEndpoint != null && managedEndpoint.isPublished();
    }

    // called by whiteboard
    public void register(String name, InboundSoapEndPointProvider endPointProvider) {
        webServices.put(name, dataModel.getInstance(InboundSoapEndPointFactoryImpl.class).init(name, endPointProvider));
        eventService.postEvent(EventType.WEBSERVICE_REGISTERED.topic(), name);
    }

    // called by whiteboard
    public void register(String name, InboundRestEndPointProvider endPointProvider) {
        webServices.put(name, dataModel.getInstance(InboundRestEndPointFactoryImpl.class).init(name, endPointProvider));
        eventService.postEvent(EventType.WEBSERVICE_REGISTERED.topic(), name);
    }

    // called by whiteboard
    public void register(String name, OutboundSoapEndPointProvider endPointProvider) {
        webServices.put(name, dataModel.getInstance(OutboundSoapEndPointFactoryImpl.class)
                .init(name, endPointProvider));
        eventService.postEvent(EventType.WEBSERVICE_REGISTERED.topic(), name);
    }

    // called by whiteboard
    public void register(String name, OutboundRestEndPointProvider endPointProvider) {
        webServices.put(name, dataModel.getInstance(OutboundRestEndPointFactoryImpl.class)
                .init(name, endPointProvider));
        eventService.postEvent(EventType.WEBSERVICE_REGISTERED.topic(), name);
    }

    // called by whiteboard
    public void unregister(String webServiceName) {
        if (webServices.remove(webServiceName) != null) {
            List<EndPointConfiguration> endPointConfigurations = endpoints.keySet()
                    .stream()
                    .filter(e -> e.getWebServiceName().equals(webServiceName))
                    .collect(toList());
            for (EndPointConfiguration endPointConfiguration : endPointConfigurations) {
                endpoints.remove(endPointConfiguration).stop();
            }
        }
    }

    @Activate
    public void activate(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        String logDirectory = this.bundleContext.getProperty("com.elster.jupiter.webservices.log.directory");
        if (logDirectory == null) {
            logDirectory = System.getProperty("java.io.tmpdir");
        }
        if(!logDirectory.endsWith(File.separator)){
            logDirectory=logDirectory + File.separator;
        }
        this.dataModel.register(this.getModule(logDirectory));
        upgradeService.register(InstallIdentifier.identifier("Pulse", WebServicesService.COMPONENT_NAME), dataModel,
                Installer.class, Collections.emptyMap());
        Class<?> clazz = org.glassfish.hk2.osgiresourcelocator.ServiceLoader.class;
        clazz.getAnnotations();
        BundleWaiter.wait(this,bundleContext,"org.glassfish.hk2.osgi-resource-locator");
    }

    @Override
    public void start(BundleContext context) {
        registration = bundleContext.registerService(WebServicesService.class, this, null);
    }

    @Deactivate
    public void stop(BundleContext bundleContext) {
        endpoints.values().stream().forEach(ManagedEndpoint::stop);
        endpoints.clear();
        if (registration != null) {
            registration.unregister();
        }
    }

    private Module getModule(String logDirectory) {
        return new AbstractModule() {
            @Override
            public void configure() {
                bind(DataModel.class).toInstance(dataModel);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(BundleContext.class).toInstance(bundleContext);
                bind(SoapProviderSupportFactory.class).toInstance(soapProviderSupportFactory);
                bind(EventService.class).toInstance(eventService);
                bind(UserService.class).toInstance(userService);
                bind(TransactionService.class).toInstance(transactionService);
                bind(String.class).annotatedWith(Names.named("LogDirectory")).toInstance(logDirectory);
                bind(HttpService.class).toInstance(httpService);
            }
        };
    }

}
