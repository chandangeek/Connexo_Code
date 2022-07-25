/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.custom.eventhandlers;

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
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreaterequestservice.UtilsSmrtMtrEvtERPCrteReqUtilsSmrtMtrEvt;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.nio.file.FileSystem;
import java.time.Clock;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;


@Component(name = CustomSAPDeviceEventHandler.NAME, service = {CustomSAPDeviceEventHandler.class, TopicHandler.class}, immediate = true)
public class CustomSAPDeviceEventHandler implements TopicHandler {
    public static final String COMPONENT_NAME = "CSE";
    static final String NAME = "com.energyict.mdc.sap.soap.custom.eventhandlers.CustomSAPDeviceEventHandler";
    private static final Logger LOGGER = Logger.getLogger(NAME);
    private static final Function<EndDeviceEventRecord, Optional<UtilsSmrtMtrEvtERPCrteReqUtilsSmrtMtrEvt>> NO_MAPPING_FORMATTER = event -> {
        throw new RuntimeException("Failed to send notification about event " + toString(event) + ": mapping csv hadn't been loaded properly.");
    };

    private volatile MeterEventCreateRequestProvider meterEventCreateRequestProvider;
    private volatile SAPCustomPropertySets sapCustomPropertySets;
    private volatile Clock clock;
    private volatile FileSystem fileSystem;
    private volatile ServiceCallService serviceCallService;
    private volatile Thesaurus thesaurus;
    private volatile PropertySpecService propertySpecService;
    private volatile UserService userService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile TransactionService transactionService;

    // For OSGi purposes
    public CustomSAPDeviceEventHandler() {
        super();
    }

    // For testing purposes only
    @Inject
    public CustomSAPDeviceEventHandler(MeterEventCreateRequestProvider meterEventCreateRequestProvider,
                                       SAPCustomPropertySets sapCustomPropertySets,
                                       Clock clock,
                                       FileSystem fileSystem,
                                       ServiceCallService serviceCallService,
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
        this.thesaurus = thesaurus;
        setPropertySpecService(propertySpecService);
        setUserService(userService);
        setThreadPrincipalService(threadPrincipalService);
        setTransactionService(transactionService);
    }

    private Module getModule() {
        return new AbstractModule() {
            @Override
            protected void configure() {
                bind(CustomSAPDeviceEventHandler.class).toInstance(CustomSAPDeviceEventHandler.this);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(PropertySpecService.class).toInstance(propertySpecService);
                bind(FileSystem.class).toInstance(fileSystem);
                bind(ServiceCallService.class).toInstance(serviceCallService);
                bind(SAPCustomPropertySets.class).toInstance(sapCustomPropertySets);
                bind(UserService.class).toInstance(userService);
                bind(ThreadPrincipalService.class).toInstance(threadPrincipalService);
                bind(TransactionService.class).toInstance(transactionService);
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
            if (!isPushEventsToSap(source)) {
                // skip if the 'Push events to SAP' attribute isn't set on the device
                return;
            }
            meterEventCreateRequestProvider.createBulkMessage(source).ifPresent(meterEventCreateRequestProvider::send);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
        }
    }

    public void handle(EndDeviceEventRecord eventRecord) {
        try {
            if (!isPushEventsToSap(eventRecord)) {
                // skip if the 'Push events to SAP' attribute isn't set on the device
                return;
            }
            meterEventCreateRequestProvider.createBulkMessage(eventRecord).ifPresent(meterEventCreateRequestProvider::send);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
        }
    }

    private boolean isPushEventsToSap(EndDeviceEventRecord eventRecord) {
        return sapCustomPropertySets.isPushEventsToSapFlagSet(eventRecord.getEndDevice());
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
