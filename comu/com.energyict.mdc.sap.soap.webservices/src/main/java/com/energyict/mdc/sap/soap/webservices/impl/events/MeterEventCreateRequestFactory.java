/*
 * Copyright (c) 2022 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.events;

import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreaterequestservice.BusinessDocumentMessageHeader;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreaterequestservice.ObjectFactory;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreaterequestservice.UtilsSmrtMtrEvtERPBulkCrteReqMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreaterequestservice.UtilsSmrtMtrEvtERPCrteReqMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreaterequestservice.UtilsSmrtMtrEvtERPCrteReqUtilsSmrtMtrEvt;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public class MeterEventCreateRequestFactory {
    private static final ObjectFactory OBJECT_FACTORY = new ObjectFactory();
    private static final Function<EndDeviceEventRecord, Optional<UtilsSmrtMtrEvtERPCrteReqUtilsSmrtMtrEvt>> NO_MAPPING_FORMATTER = event -> {
        throw new RuntimeException("Failed to send notification about event " + toString(event) + ": mapping csv hadn't been loaded properly.");
    };

    protected Function<EndDeviceEventRecord, Optional<UtilsSmrtMtrEvtERPCrteReqUtilsSmrtMtrEvt>> eventFormatter;

    public MeterEventCreateRequestFactory(ForwardedDeviceEventTypesFormatter formatter) {
        setEventFormatter(formatter);
    }

    public Optional<UtilsSmrtMtrEvtERPBulkCrteReqMsg> getMeterEventBulkMessage(Instant time, String meteringSystemId, EndDeviceEventRecord... events) {
        UtilsSmrtMtrEvtERPBulkCrteReqMsg message = OBJECT_FACTORY.createUtilsSmrtMtrEvtERPBulkCrteReqMsg();
        message.setMessageHeader(createMessageHeader(time, meteringSystemId));
        for (EndDeviceEventRecord event : events) {
            createSingleItem(event, time, meteringSystemId)
                    .ifPresent(message.getUtilitiesSmartMeterEventERPCreateRequestMessage()::add);
        }
        if (!message.getUtilitiesSmartMeterEventERPCreateRequestMessage().isEmpty()) {
            return Optional.of(message);
        }
        return Optional.empty();
    }

    private Optional<UtilsSmrtMtrEvtERPCrteReqMsg> createSingleItem(EndDeviceEventRecord event, Instant time, String meteringSystemId) {
        return this.eventFormatter.apply(event)
                .map(eventInfo -> {
                    UtilsSmrtMtrEvtERPCrteReqMsg info = OBJECT_FACTORY.createUtilsSmrtMtrEvtERPCrteReqMsg();
                    info.setMessageHeader(createMessageHeader(time, meteringSystemId));
                    info.setUtilitiesSmartMeterEvent(eventInfo);
                    return info;
                });
    }

    public BusinessDocumentMessageHeader createMessageHeader(Instant time, String meteringSystemId) {
        BusinessDocumentMessageHeader messageHeader = OBJECT_FACTORY.createBusinessDocumentMessageHeader();
        messageHeader.setCreationDateTime(time);
        messageHeader.setUUID(createUUID());
        messageHeader.setSenderBusinessSystemID(meteringSystemId);
        messageHeader.setReconciliationIndicator(true);
        return messageHeader;
    }

    private com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreaterequestservice.UUID createUUID() {
        com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreaterequestservice.UUID uuid = OBJECT_FACTORY.createUUID();
        uuid.setValue(UUID.randomUUID().toString());
        return uuid;
    }

    private static String toString(EndDeviceEventRecord eventRecord) {
        return eventRecord.getEventType().getMRID() + " (" + eventRecord.getDeviceEventType() + ") on device " + eventRecord.getEndDevice().getName();
    }

    private void setEventFormatter(ForwardedDeviceEventTypesFormatter formatter) {
        if (formatter != null) {
            this.eventFormatter = formatter::filterAndFormat;
        } else {
            this.eventFormatter = NO_MAPPING_FORMATTER;
        }
    }
}