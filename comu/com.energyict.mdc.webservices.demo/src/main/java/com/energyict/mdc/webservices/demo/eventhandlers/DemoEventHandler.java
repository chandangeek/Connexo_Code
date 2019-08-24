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
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreaterequestservice.ObjectFactory;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreaterequestservice.ObjectPropertyValue;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreaterequestservice.Text;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreaterequestservice.UtilitiesDeviceID;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreaterequestservice.UtilitiesSmartMeterEventCategoryCode;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreaterequestservice.UtilitiesSmartMeterEventID;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreaterequestservice.UtilitiesSmartMeterEventPropertyCode;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreaterequestservice.UtilitiesSmartMeterEventSeverityCode;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreaterequestservice.UtilitiesSmartMeterEventTypeCode;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreaterequestservice.UtilsSmrtMtrEvtERPBulkCrteReqMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreaterequestservice.UtilsSmrtMtrEvtERPCrteReqMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreaterequestservice.UtilsSmrtMtrEvtERPCrteReqPrpty;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreaterequestservice.UtilsSmrtMtrEvtERPCrteReqUtilsSmrtMtrEvt;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component(name = "DemoEventHandler", service = TopicHandler.class, immediate = true)
public class DemoEventHandler implements TopicHandler {

    private static final Logger LOGGER = Logger.getLogger(DemoEventHandler.class.getName());

    private volatile MeterEventCreateRequestProvider meterEventCreateRequestProvider;
    private volatile SAPCustomPropertySets sapCustomPropertySets;
    private volatile Clock clock;
    private ObjectFactory objectFactory = new ObjectFactory();

    // OSGi
    public DemoEventHandler() {
        super();
    }

    // For testing purposes only
    public DemoEventHandler(MeterEventCreateRequestProvider meterEventCreateRequestProvider,
                            SAPCustomPropertySets sapCustomPropertySets,
                            Clock clock) {
        this();
        setMeterEventCreateRequest(meterEventCreateRequestProvider);
        setSapCustomPropertySets(sapCustomPropertySets);
        setClock(clock);
    }

    @Override
    public void handle(LocalEvent localEvent) {
        try {
            EndDeviceEventRecord source = (EndDeviceEventRecord) localEvent.getSource();
            Instant createTime = clock.instant();
            UtilsSmrtMtrEvtERPBulkCrteReqMsg reqMsg = objectFactory.createUtilsSmrtMtrEvtERPBulkCrteReqMsg();
            reqMsg.setMessageHeader(objectFactory.createBusinessDocumentMessageHeader());
            reqMsg.getMessageHeader().setCreationDateTime(createTime);
            reqMsg.getUtilitiesSmartMeterEventERPCreateRequestMessage().add(objectFactory.createUtilsSmrtMtrEvtERPCrteReqMsg());
            //innerRequestMessage
            UtilsSmrtMtrEvtERPCrteReqMsg msg = reqMsg.getUtilitiesSmartMeterEventERPCreateRequestMessage().get(0);
            msg.setMessageHeader(objectFactory.createBusinessDocumentMessageHeader());
            msg.getMessageHeader().setCreationDateTime(createTime);
            msg.setUtilitiesSmartMeterEvent(objectFactory.createUtilsSmrtMtrEvtERPCrteReqUtilsSmrtMtrEvt());
            UtilsSmrtMtrEvtERPCrteReqUtilsSmrtMtrEvt mtrEvt = msg.getUtilitiesSmartMeterEvent();
            //ID
            UtilitiesSmartMeterEventID utilitiesSmartMeterEventID = objectFactory.createUtilitiesSmartMeterEventID();
            utilitiesSmartMeterEventID.setValue(UUID.randomUUID().toString().toUpperCase());
            mtrEvt.setID(utilitiesSmartMeterEventID);
            //deviceid
            UtilitiesDeviceID utilitiesDeviceID = objectFactory.createUtilitiesDeviceID();
            String utilDevID = sapCustomPropertySets.getSapDeviceId(source.getEndDevice().getName())
                    .orElseThrow(() -> new IllegalStateException("Device SAP id is not found."));
            utilitiesDeviceID.setValue(utilDevID);
            mtrEvt.setUtilitiesDeviceID(utilitiesDeviceID);
            //dateTime
            mtrEvt.setDateTime(source.getCreatedDateTime());
            //categoryCode
            UtilitiesSmartMeterEventCategoryCode utilitiesSmartMeterEventCategoryCode = objectFactory.createUtilitiesSmartMeterEventCategoryCode();
            utilitiesSmartMeterEventCategoryCode.setValue(source.getEventType().getMRID());
            mtrEvt.setCategoryCode(utilitiesSmartMeterEventCategoryCode);
            //typeCode
            UtilitiesSmartMeterEventTypeCode utilitiesSmartMeterEventTypeCode = objectFactory.createUtilitiesSmartMeterEventTypeCode();
            utilitiesSmartMeterEventTypeCode.setValue(source.getEventType().getName());
            mtrEvt.setTypeCode(utilitiesSmartMeterEventTypeCode);
            //SeverityCode
            UtilitiesSmartMeterEventSeverityCode utilitiesSmartMeterEventSeverityCode = objectFactory.createUtilitiesSmartMeterEventSeverityCode();
            utilitiesSmartMeterEventSeverityCode.setValue(source.getSeverity());
            mtrEvt.setSeverityCode(utilitiesSmartMeterEventSeverityCode);
            //Code and Text
            source.getProperties().forEach((propertyCode, value) ->
                    mtrEvt.getProperty().add(createRequestProperty(propertyCode, value)));
            meterEventCreateRequestProvider.send(reqMsg);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
        }
    }

    private UtilsSmrtMtrEvtERPCrteReqPrpty createRequestProperty(String propertyCode, String value) {
        UtilsSmrtMtrEvtERPCrteReqPrpty utilsSmrtMtrEvtERPCrteReqPrpty = objectFactory.createUtilsSmrtMtrEvtERPCrteReqPrpty();
        UtilitiesSmartMeterEventPropertyCode utilitiesSmartMeterEventPropertyCode = objectFactory.createUtilitiesSmartMeterEventPropertyCode();
        utilitiesSmartMeterEventPropertyCode.setValue(propertyCode);
        utilsSmrtMtrEvtERPCrteReqPrpty.setCode(utilitiesSmartMeterEventPropertyCode);
        Text text = objectFactory.createText();
        text.setValue(value);
        ObjectPropertyValue objectPropertyValue = objectFactory.createObjectPropertyValue();
        utilsSmrtMtrEvtERPCrteReqPrpty.setObjectPropertyValue(objectPropertyValue);
        utilsSmrtMtrEvtERPCrteReqPrpty.getObjectPropertyValue().setText(text);
        return utilsSmrtMtrEvtERPCrteReqPrpty;
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
