/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.http.whiteboard.TokenService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.InboundRestEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.InboundSoapEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.LogLevel;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundRestEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.PayloadSaveStrategy;
import com.elster.jupiter.soap.whiteboard.cxf.SoapProviderSupportFactory;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrenceService;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrenceStatus;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallRelatedAttributeTypeProvider;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.elster.jupiter.soap.whiteboard.cxf.impl.rest.ServletWrapper;
import com.elster.jupiter.soap.whiteboard.cxf.security.Privileges;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.upgrade.V10_4SimpleUpgrader;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.util.osgi.BundleWaiter;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.name.Names;
import org.apache.cxf.transport.servlet.CXFNonSpringServlet;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.validation.MessageInterpolator;
import java.io.File;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by bvn on 5/4/16.
 */
@Component(name = "com.elster.jupiter.soap.webservices.installer",
        service = {MessageSeedProvider.class, TranslationKeyProvider.class},
        property = "name=" + WebServicesService.COMPONENT_NAME,
        immediate = true)
public class WebServicesDataModelServiceImpl implements WebServicesDataModelService, MessageSeedProvider, TranslationKeyProvider, BundleWaiter.Startable {
    private volatile BundleContext bundleContext;
    private volatile DataModel dataModel;
    private volatile Thesaurus thesaurus;
    private volatile EventService eventService;
    private volatile UserService userService;
    private volatile TokenService tokenService;
    private volatile TransactionService transactionService;
    private volatile HttpService httpService;
    private volatile Clock clock;
    private volatile UpgradeService upgradeService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile SoapProviderSupportFactory soapProviderSupportFactory;

    private volatile List<ServiceRegistration> registrations = new ArrayList<>();

    private volatile WebServicesServiceImpl webServicesService;
    private volatile EndPointConfigurationServiceImpl endPointConfigurationService;
    private volatile WebServiceCallOccurrenceServiceImpl webServiceCallOccurrenceService;
    private volatile NlsService nlsService;

    public WebServicesDataModelServiceImpl() {
        // for OSGi
    }

    @Inject // Test purposes only
    public WebServicesDataModelServiceImpl(EventService eventService, NlsService nlsService, OrmService ormService,
                                           UserService userService, TransactionService transactionService,
                                           HttpService httpService, BundleContext bundleContext, UpgradeService upgradeService,
                                           SoapProviderSupportFactory soapProviderSupportFactory, ThreadPrincipalService threadPrincipalService,
                                           Clock clock, TokenService tokenService) {
        setEventService(eventService);
        setNlsService(nlsService);
        setOrmService(ormService);
        setUserService(userService);
        setTransactionService(transactionService);
        setHttpService(httpService);
        setUpgradeService(upgradeService);
        setSoapProviderSupportFactory(soapProviderSupportFactory);
        setThreadPrincipalService(threadPrincipalService);
        setClock(clock);
        setTokenService(tokenService);
        activate(bundleContext);
    }

    @Override
    public String getComponentName() {
        return WebServicesService.COMPONENT_NAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Stream.of(
                Privileges.values(),
                LogLevel.values(),
                PayloadSaveStrategy.values(),
                WebServiceCallOccurrenceStatus.values())
                .flatMap(Arrays::stream)
                .collect(Collectors.toList());
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        this.thesaurus = nlsService.getThesaurus(WebServicesService.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.dataModel = ormService.newDataModel(WebServicesService.COMPONENT_NAME, "Web services");
        for (TableSpecs tableSpecs : TableSpecs.values()) {
            tableSpecs.addTo(this.dataModel);
        }
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public void setTokenService(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
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
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Reference
    public void setHttpService(HttpService httpService) {
        this.httpService = httpService;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setConfiguration(WhiteBoardConfigurationProvider provider) {
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addAttributeTypes(WebServiceCallRelatedAttributeTypeProvider provider) {
        webServiceCallOccurrenceService.addRelatedObjectTypes(provider.getComponentName(),
                provider.getLayer(),
                provider.getAttributeTranslations());
    }

    public void removeAttributeTypes(WebServiceCallRelatedAttributeTypeProvider provider) {
        webServiceCallOccurrenceService.removeRelatedObjectTypes(provider.getAttributeTranslations());
    }

    private Module getModule(String logDirectory) {
        return new AbstractModule() {
            @Override
            public void configure() {
                bind(DataModel.class).toInstance(dataModel);
                bind(BundleContext.class).toInstance(bundleContext);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(EventService.class).toInstance(eventService);
                bind(UserService.class).toInstance(userService);
                bind(TokenService.class).toInstance(tokenService);
                bind(TransactionService.class).toInstance(transactionService);
                bind(HttpService.class).toInstance(httpService);
                bind(SoapProviderSupportFactory.class).toInstance(soapProviderSupportFactory);
                bind(String.class).annotatedWith(Names.named("LogDirectory")).toInstance(logDirectory);
                bind(ThreadPrincipalService.class).toInstance(threadPrincipalService);
                bind(Clock.class).toInstance(clock);
                bind(NlsService.class).toInstance(nlsService);

                bind(WebServicesService.class).toInstance(webServicesService);
                bind(WebServicesServiceImpl.class).toInstance(webServicesService);
                bind(EndPointConfigurationService.class).toInstance(endPointConfigurationService);
                bind(EndPointConfigurationServiceImpl.class).toInstance(endPointConfigurationService);
                bind(WebServiceCallOccurrenceService.class).toInstance(webServiceCallOccurrenceService);
                bind(WebServiceCallOccurrenceServiceImpl.class).toInstance(webServiceCallOccurrenceService);
            }
        };
    }

    @Activate
    public void activate(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        String logDirectory = this.bundleContext.getProperty("com.elster.jupiter.webservices.log.directory");
        if (logDirectory == null) {
            logDirectory = System.getProperty("java.io.tmpdir");
        }
        if (!logDirectory.endsWith(File.separator)) {
            logDirectory = logDirectory + File.separator;
        }

        HttpServlet servlet = new ServletWrapper(new CXFNonSpringServlet(), threadPrincipalService);
        try {
            httpService.registerServlet("/soap", servlet, null, null);
        } catch (NamespaceException | ServletException ex) {
            throw new RuntimeException(ex);
        }

        endPointConfigurationService = new EndPointConfigurationServiceImpl(dataModel, eventService);
        webServiceCallOccurrenceService = new WebServiceCallOccurrenceServiceImpl(dataModel, nlsService);
        webServicesService = new WebServicesServiceImpl(dataModel, eventService, transactionService, clock, thesaurus, endPointConfigurationService, webServiceCallOccurrenceService);
        this.dataModel.register(this.getModule(logDirectory));
        upgradeService.register(
                InstallIdentifier.identifier("Pulse", WebServicesService.COMPONENT_NAME),
                dataModel,
                Installer.class,
                ImmutableMap.<Version, Class<? extends Upgrader>>builder()
                        .put(V10_4SimpleUpgrader.VERSION, V10_4SimpleUpgrader.class)
                        .put(UpgraderV10_4_9.VERSION, UpgraderV10_4_9.class)
                        .put(UpgraderV10_5_1.VERSION, UpgraderV10_5_1.class)
                        .put(UpgraderV10_7.VERSION, UpgraderV10_7.class)
                        .put(UpgraderV10_7_1.VERSION, UpgraderV10_7_1.class)
                        .put(Version.version(10, 7, 4), UpgraderV10_7_4.class)
                        .put(UpgraderV10_8.VERSION, UpgraderV10_8.class)
                        .put(Version.version(10, 8, 7), UpgraderV10_8_7.class)
                        .put(Version.version(10, 8, 7, 1), UpgraderV10_8_7_1.class)
                        .put(UpgraderV10_9.VERSION, UpgraderV10_9.class)
                        .build());
        Class<?> clazz = org.glassfish.hk2.osgiresourcelocator.ServiceLoader.class;
        clazz.getAnnotations();
        BundleWaiter.wait(this, bundleContext, "org.glassfish.hk2.locator", "org.glassfish.hk2.osgi-resource-locator");
    }

    @Override
    public void start(BundleContext context) {
        registrations.add(bundleContext.registerService(WebServicesDataModelService.class, this, new Hashtable<>()));
        registrations.add(bundleContext.registerService(EndPointConfigurationService.class, endPointConfigurationService, new Hashtable<>()));
        registrations.add(bundleContext.registerService(WebServiceCallOccurrenceService.class, webServiceCallOccurrenceService, new Hashtable<>()));
        registrations.add(bundleContext.registerService(WebServicesService.class, webServicesService, new Hashtable<>()));
    }

    @Deactivate
    public void stop() {
        webServicesService.removeAllEndPoints();
        registrations.forEach(ServiceRegistration::unregister);
        registrations.clear();
    }

    @Reference(name = "ZInboundSoapEndPointProvider", cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addEndPoint(InboundSoapEndPointProvider provider, Map<String, Object> props) {
        String alias = getName(props);
        if (alias == null) {
            return;
        }
        webServicesService.register(alias, provider);
    }

    public void removeEndPoint(InboundSoapEndPointProvider provider, Map<String, Object> props) {
        String alias = getName(props);
        if (alias != null) {
            webServicesService.unregister(alias);
        }
    }

    @Reference(name = "ZInboundRestEndPointProvider", cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addEndPoint(InboundRestEndPointProvider provider, Map<String, Object> props) {
        String alias = getName(props);
        if (alias == null) {
            return;
        }
        webServicesService.register(alias, provider);
    }

    public void removeEndPoint(InboundRestEndPointProvider provider, Map<String, Object> props) {
        String alias = getName(props);
        if (alias != null) {
            webServicesService.unregister(alias);
        }
    }

    @Reference(name = "ZOutboundSoapEndPointProvider", cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addEndPoint(OutboundSoapEndPointProvider provider, Map<String, Object> props) {
        String alias = getName(props);
        if (alias == null) {
            return;
        }
        webServicesService.register(alias, dataModel.getInstance(AbstractEndPointInitializer.class).initializeOutboundEndPointProvider(provider));
    }

    public void removeEndPoint(OutboundSoapEndPointProvider provider, Map<String, Object> props) {
        String alias = getName(props);
        if (alias != null) {
            webServicesService.unregister(alias);
        }
    }

    @Reference(name = "ZOutboundRestEndPointProvider", cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addEndPoint(OutboundRestEndPointProvider provider, Map<String, Object> props) {
        String alias = getName(props);
        if (alias == null) {
            return;
        }
        webServicesService.register(alias, provider);
    }

    public void removeEndPoint(OutboundRestEndPointProvider provider, Map<String, Object> props) {
        String alias = getName(props);
        if (alias != null) {
            webServicesService.unregister(alias);
        }
    }

    private String getName(Map<String, Object> props) {
        return props == null ? null : (String) props.get("name");
    }

    @Override
    public WebServicesServiceImpl getWebServicesService() {
        return webServicesService;
    }

    @Override
    public EndPointConfigurationServiceImpl getEndPointConfigurationService() {
        return endPointConfigurationService;
    }

    @Override
    public WebServiceCallOccurrenceServiceImpl getWebServiceCallOccurrenceService() {
        return webServiceCallOccurrenceService;
    }

    @Override
    public DataModel getDataModel() {
        return dataModel;
    }

    @Override
    public Thesaurus getThesaurus() {
        return thesaurus;
    }
}
