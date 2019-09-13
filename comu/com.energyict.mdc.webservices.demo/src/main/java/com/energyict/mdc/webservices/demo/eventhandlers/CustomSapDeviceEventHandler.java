/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.webservices.demo.eventhandlers;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.metering.EventType;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.energyict.mdc.sap.soap.webservices.MeterEventCreateRequestProvider;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreaterequestservice.BusinessDocumentMessageHeader;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreaterequestservice.ObjectFactory;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreaterequestservice.UtilsSmrtMtrEvtERPBulkCrteReqMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreaterequestservice.UtilsSmrtMtrEvtERPCrteReqMsg;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component(name = CustomSapDeviceEventHandler.NAME, service = TopicHandler.class, immediate = true)
public class CustomSapDeviceEventHandler implements TopicHandler {
    static final String NAME = "com.energyict.mdc.webservices.demo.eventhandlers.CustomSapDeviceEventHandler";
    private static final Logger LOGGER = Logger.getLogger(NAME);

    private volatile MeterEventCreateRequestProvider meterEventCreateRequestProvider;
    private volatile SAPCustomPropertySets sapCustomPropertySets;
    private volatile Clock clock;
    private ObjectFactory objectFactory = new ObjectFactory();
    private ForwardedDeviceEventTypesFormatter eventFormatter;

    // For OSGi purposes
    public CustomSapDeviceEventHandler() {
        super();
    }

    // For testing purposes only
    public CustomSapDeviceEventHandler(MeterEventCreateRequestProvider meterEventCreateRequestProvider,
                                       SAPCustomPropertySets sapCustomPropertySets,
                                       Clock clock) {
        this();
        setMeterEventCreateRequest(meterEventCreateRequestProvider);
        setSapCustomPropertySets(sapCustomPropertySets);
        setClock(clock);
    }

    @Activate
    public void activate(BundleContext bundleContext) {
        eventFormatter = new ForwardedDeviceEventTypesFormatter(sapCustomPropertySets);
        // TODO
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
        return eventFormatter.filterAndFormat(event)
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
        return messageHeader;
    }

    private com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreaterequestservice.UUID createUUID() {
        com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreaterequestservice.UUID uuid = objectFactory.createUUID();
        uuid.setValue(UUID.randomUUID().toString());
        return uuid;
    }

    @Override
    public String getTopicMatcher() {
        return EventType.END_DEVICE_EVENT_CREATED.topic();
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
}
