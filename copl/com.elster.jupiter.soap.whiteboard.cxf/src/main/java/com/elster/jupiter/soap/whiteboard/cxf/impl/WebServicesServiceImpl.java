package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.InboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.SoapProviderSupportFactory;
import com.elster.jupiter.soap.whiteboard.cxf.WebService;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.users.UserService;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.name.Names;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

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
@Component(name = "com.elster.jupiter.soap.webservices.cxf", service = {WebServicesService.class}, immediate = true)
public class WebServicesServiceImpl implements WebServicesService {
    private static final Logger logger = Logger.getLogger("WebServicesServiceImpl");

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

    // OSGi
    public WebServicesServiceImpl() {
    }

    @Inject // For test purposes only
    public WebServicesServiceImpl(SoapProviderSupportFactory soapProviderSupportFactory, OrmService ormService,
                                  UpgradeService upgradeService, BundleContext bundleContext, EventService eventService,
                                  UserService userService, NlsService nlsService, TransactionService transactionService) {
        setSoapProviderSupportFactory(soapProviderSupportFactory);
        setOrmService(ormService);
        setUpgradeService(upgradeService);
        setEventService(eventService);
        setNlsService(nlsService);
        setUserService(userService);
        setTransactionService(transactionService);
        start(bundleContext);
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
        if (webServices.containsKey(webServiceName)) {
            return Optional.of(new WebService() {
                @Override
                public String getName() {
                    return webServiceName;
                }

                @Override
                public boolean isInbound() {
                    return webServices.get(webServiceName).isInbound();
                }
            });
        } else {
            return Optional.empty();
        }
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
        }).collect(toList());
    }

    @Override
    public void publishEndPoint(EndPointConfiguration endPointConfiguration) {
        if (webServices.containsKey(endPointConfiguration.getWebServiceName())) {
            try {
                ManagedEndpoint managedEndpoint = webServices.get(endPointConfiguration.getWebServiceName())
                        .createEndpoint(endPointConfiguration);
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
        if (webServices.containsKey(webServiceName)) {
            return webServices.get(webServiceName).isInbound();
        } else {
            throw new IllegalArgumentException("No such web service");
        }
    }

    @Override
    public boolean isPublished(EndPointConfiguration endPointConfiguration) {
        return endpoints.containsKey(endPointConfiguration) && endpoints.get(endPointConfiguration).isPublished();
    }

    // called by whiteboard
    public void register(String name, InboundEndPointProvider endPointProvider) {
        webServices.put(name, dataModel.getInstance(InboundEndPointFactoryImpl.class).init(name, endPointProvider));
    }

    // called by whiteboard
    public void register(String name, OutboundEndPointProvider endPointProvider) {
        webServices.put(name, dataModel.getInstance(OutboundEndPointFactoryImpl.class).init(name, endPointProvider));
    }

    // called by whiteboard
    public void unregister(String webServiceName) {
        if (webServices.containsKey(webServiceName)) {
            webServices.remove(webServiceName);
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
    public void start(BundleContext bundleContext) {
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
    }

    @Deactivate
    public void stop(BundleContext bundleContext) {
        endpoints.values().stream().forEach(ManagedEndpoint::stop);
        endpoints.clear();
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
            }
        };
    }

}
