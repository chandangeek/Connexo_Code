/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.custom.eventhandlers;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.metering.EventType;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.sap.soap.custom.TranslationInstaller;
import com.energyict.mdc.sap.soap.webservices.MeterEventCreateRequestProvider;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreaterequestservice.BusinessDocumentMessageHeader;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreaterequestservice.ObjectFactory;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreaterequestservice.UtilsSmrtMtrEvtERPBulkCrteReqMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreaterequestservice.UtilsSmrtMtrEvtERPCrteReqMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreaterequestservice.UtilsSmrtMtrEvtERPCrteReqUtilsSmrtMtrEvt;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Scopes;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.nio.file.FileSystem;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;


@Component(name = CustomSAPDeviceEventHandler.NAME, service = {CustomSAPDeviceEventHandler.class, TopicHandler.class}, immediate = true)
public class CustomSAPDeviceEventHandler implements TopicHandler {
    public static final String COMPONENT_NAME = "CSE";
    static final String APPLICATION_NAME = "MultiSense";
    static final String NAME = "com.energyict.mdc.sap.soap.custom.eventhandlers.CustomSAPDeviceEventHandler";
    private static final Logger LOGGER = Logger.getLogger(NAME);
    private static final Function<EndDeviceEventRecord, Optional<UtilsSmrtMtrEvtERPCrteReqUtilsSmrtMtrEvt>> NO_MAPPING_FORMATTER = event -> {
        throw new RuntimeException("Failed to send notification about event " + toString(event) + ": mapping csv hadn't been loaded properly.");
    };

    private static final String METERING_SYSTEM_ID = "sap.soap.metering.system.id";
    private static final String DEFAULT_METERING_SYSTEM_ID = "HON";

    private volatile BundleContext bundleContext;
    private volatile MeterEventCreateRequestProvider meterEventCreateRequestProvider;
    private volatile SAPCustomPropertySets sapCustomPropertySets;
    private volatile Clock clock;
    private volatile FileSystem fileSystem;
    private volatile ServiceCallService serviceCallService;
    private volatile CustomPropertySetService customPropertySetService;
    private volatile Thesaurus thesaurus;
    private volatile PropertySpecService propertySpecService;
    private volatile UserService userService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile TransactionService transactionService;

    private ObjectFactory objectFactory = new ObjectFactory();
    private CustomPropertySet mappingStatusCustomPropertySet;
    private Function<EndDeviceEventRecord, Optional<UtilsSmrtMtrEvtERPCrteReqUtilsSmrtMtrEvt>> eventFormatter;
    private String meteringSystemId;

    // For OSGi purposes
    public CustomSAPDeviceEventHandler() {
        super();
    }

    // For testing purposes only
    @Inject
    public CustomSAPDeviceEventHandler(BundleContext bundleContext,
                                       MeterEventCreateRequestProvider meterEventCreateRequestProvider,
                                       SAPCustomPropertySets sapCustomPropertySets,
                                       Clock clock,
                                       FileSystem fileSystem,
                                       ServiceCallService serviceCallService,
                                       CustomPropertySetService customPropertySetService,
                                       Thesaurus thesaurus,
                                       PropertySpecService propertySpecService,
                                       UserService userService,
                                       ThreadPrincipalService threadPrincipalService,
                                       TransactionService transactionService) {
        this();
        setMeterEventCreateRequest(meterEventCreateRequestProvider);
        setSapCustomPropertySets(sapCustomPropertySets);
        setClock(clock);
        setFileSystem(fileSystem);
        setServiceCallService(serviceCallService);
        setCustomPropertySetService(customPropertySetService);
        this.thesaurus = thesaurus;
        setPropertySpecService(propertySpecService);
        setUserService(userService);
        setThreadPrincipalService(threadPrincipalService);
        setTransactionService(transactionService);
        activate(bundleContext);
    }

    @Activate
    public void activate(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        Injector injector = Guice.createInjector(getModule());
        mappingStatusCustomPropertySet = injector.getInstance(SAPDeviceEventMappingStatusCustomPropertySet.class);
        customPropertySetService.addCustomPropertySet(mappingStatusCustomPropertySet);
        SAPDeviceEventMappingLoader eventMappingLoader = injector.getInstance(SAPDeviceEventMappingLoader.class);
        meteringSystemId = getProperty(METERING_SYSTEM_ID, DEFAULT_METERING_SYSTEM_ID);
        try {
            ForwardedDeviceEventTypesFormatter formatter = eventMappingLoader.loadMapping();
            eventFormatter = formatter::filterAndFormat;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Couldn't load SAP device event mapping csv: " + e.getLocalizedMessage(), e);
            eventFormatter = NO_MAPPING_FORMATTER;
        }
    }

    @Deactivate
    public void deactivate() {
        customPropertySetService.removeCustomPropertySet(mappingStatusCustomPropertySet);
        eventFormatter = NO_MAPPING_FORMATTER;
        mappingStatusCustomPropertySet = null;
        bundleContext = null;
    }

    private Module getModule() {
        return new AbstractModule() {
            @Override
            protected void configure() {
                bind(CustomSAPDeviceEventHandler.class).toInstance(CustomSAPDeviceEventHandler.this);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(PropertySpecService.class).toInstance(propertySpecService);
                bind(BundleContext.class).toInstance(bundleContext);
                bind(FileSystem.class).toInstance(fileSystem);
                bind(ServiceCallService.class).toInstance(serviceCallService);
                bind(CustomPropertySetService.class).toInstance(customPropertySetService);
                bind(SAPCustomPropertySets.class).toInstance(sapCustomPropertySets);
                bind(UserService.class).toInstance(userService);
                bind(ThreadPrincipalService.class).toInstance(threadPrincipalService);
                bind(TransactionService.class).toInstance(transactionService);
                bind(SAPDeviceEventMappingStatusCustomPropertySet.class).in(Scopes.SINGLETON);
            }
        };
    }

    private static String toString(EndDeviceEventRecord eventRecord) {
        return eventRecord.getEventType().getMRID() + " (" + eventRecord.getDeviceEventType() + ") on device " + eventRecord.getEndDevice().getName();
    }

    @Override
    public String getTopicMatcher() {
        return EventType.END_DEVICE_EVENT_CREATED.topic();
    }

    @Override
    public void handle(LocalEvent localEvent) {
        try {
            EndDeviceEventRecord source = (EndDeviceEventRecord) localEvent.getSource();
            createBulkMessage(source).ifPresent(meterEventCreateRequestProvider::send);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
        }
    }

    public void handle(EndDeviceEventRecord eventRecord) {
        try {
            createBulkMessage(eventRecord).ifPresent(meterEventCreateRequestProvider::send);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
        }
    }

    private Optional<UtilsSmrtMtrEvtERPBulkCrteReqMsg> createBulkMessage(EndDeviceEventRecord event) {
        Instant time = clock.instant();
        return createSingleItem(event, time)
                .map(item -> {
                    UtilsSmrtMtrEvtERPBulkCrteReqMsg info = objectFactory.createUtilsSmrtMtrEvtERPBulkCrteReqMsg();
                    info.setMessageHeader(createMessageHeader(time));
                    info.getUtilitiesSmartMeterEventERPCreateRequestMessage().add(item);
                    return info;
                });
    }

    private Optional<UtilsSmrtMtrEvtERPCrteReqMsg> createSingleItem(EndDeviceEventRecord event, Instant time) {
        return eventFormatter.apply(event)
                .map(eventInfo -> {
                    UtilsSmrtMtrEvtERPCrteReqMsg info = objectFactory.createUtilsSmrtMtrEvtERPCrteReqMsg();
                    info.setMessageHeader(createMessageHeader(time));
                    info.setUtilitiesSmartMeterEvent(eventInfo);
                    return info;
                });
    }

    private BusinessDocumentMessageHeader createMessageHeader(Instant time) {
        BusinessDocumentMessageHeader messageHeader = objectFactory.createBusinessDocumentMessageHeader();
        messageHeader.setCreationDateTime(time);
        messageHeader.setUUID(createUUID());
        messageHeader.setSenderBusinessSystemID(getMeteringSystemId());
        messageHeader.setReconciliationIndicator(true);
        return messageHeader;
    }

    private com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreaterequestservice.UUID createUUID() {
        com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreaterequestservice.UUID uuid = objectFactory.createUUID();
        uuid.setValue(UUID.randomUUID().toString());
        return uuid;
    }

    public String getMeteringSystemId() {
        return meteringSystemId;
    }

    private String getProperty(String name, String defaultValue) {
        String value = bundleContext.getProperty(name);
        return value == null ? defaultValue : value;
    }

    @Reference
    public void setMeterEventCreateRequest(MeterEventCreateRequestProvider meterEventCreateRequestProvider) {
        this.meterEventCreateRequestProvider = meterEventCreateRequestProvider;
    }

    @Reference
    public void setSapCustomPropertySets(SAPCustomPropertySets customPropertySets) {
        this.sapCustomPropertySets = customPropertySets;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setFileSystem(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }

    @Reference
    public void setServiceCallService(ServiceCallService serviceCallService) {
        this.serviceCallService = serviceCallService;
    }

    @Reference
    public void setCustomPropertySetService(CustomPropertySetService customPropertySetService) {
        this.customPropertySetService = customPropertySetService;
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setThesaurus(TranslationInstaller translationInstaller) {
        this.thesaurus = translationInstaller.getThesaurus();
    }
}
