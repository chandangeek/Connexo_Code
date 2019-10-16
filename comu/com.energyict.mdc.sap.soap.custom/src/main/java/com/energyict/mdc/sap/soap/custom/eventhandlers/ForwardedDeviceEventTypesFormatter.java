/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.custom.eventhandlers;

import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreaterequestservice.ObjectFactory;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreaterequestservice.ObjectPropertyValue;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreaterequestservice.Text;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreaterequestservice.UtilitiesDeviceID;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreaterequestservice.UtilitiesSmartMeterEventCategoryCode;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreaterequestservice.UtilitiesSmartMeterEventID;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreaterequestservice.UtilitiesSmartMeterEventOriginTypeCode;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreaterequestservice.UtilitiesSmartMeterEventPropertyCode;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreaterequestservice.UtilitiesSmartMeterEventSeverityCode;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreaterequestservice.UtilitiesSmartMeterEventTypeCode;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreaterequestservice.UtilsSmrtMtrEvtERPCrteReqPrpty;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreaterequestservice.UtilsSmrtMtrEvtERPCrteReqUtilsSmrtMtrEvt;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

class ForwardedDeviceEventTypesFormatter {
    private static final Logger LOGGER = Logger.getLogger(ForwardedDeviceEventTypesFormatter.class.getName());
    private final Map<String, SAPDeviceEventType> forwardedEventTypesByEventCode = new HashMap<>();
    private final Map<String, SAPDeviceEventType> forwardedEventTypesByDeviceEventCode = new HashMap<>();
    private final ObjectFactory objectFactory = new ObjectFactory();
    private final SAPCustomPropertySets sapCustomPropertySets;

    ForwardedDeviceEventTypesFormatter(SAPCustomPropertySets sapCustomPropertySets) {
        this.sapCustomPropertySets = sapCustomPropertySets;
    }

    void add(SAPDeviceEventType eventType) {
        Optional<String> code = eventType.getEventCode();
        if (code.isPresent()) {
            forwardedEventTypesByEventCode.put(code.get(), eventType);
        } else {
            eventType.getDeviceEventCode().ifPresent(deviceEventCode -> forwardedEventTypesByDeviceEventCode.put(deviceEventCode, eventType));
        }
    }

    Optional<UtilsSmrtMtrEvtERPCrteReqUtilsSmrtMtrEvt> filterAndFormat(EndDeviceEventRecord eventRecord) {
        SAPDeviceEventType eventType = forwardedEventTypesByEventCode.get(eventRecord.getEventTypeCode());
        if (eventType == null) {
            eventType = forwardedEventTypesByDeviceEventCode.get(eventRecord.getDeviceEventType());
        }
        if (eventType == null) {
            return Optional.empty(); // this event type is either not present in mapping file, or has 'forwarded' = false
        }
        Optional<String> sapDeviceId = sapCustomPropertySets.getSapDeviceId(eventRecord.getEndDevice());
        if (!sapDeviceId.isPresent()) {
            LOGGER.warning("SAP device id isn't found for device '" + eventRecord.getEndDevice().getName() + "'.");
            return Optional.empty();
        }
        UtilsSmrtMtrEvtERPCrteReqUtilsSmrtMtrEvt info = objectFactory.createUtilsSmrtMtrEvtERPCrteReqUtilsSmrtMtrEvt();
        info.setUtilitiesDeviceID(formatDeviceId(sapDeviceId.get()));
        info.setID(formatEventId(sapDeviceId.get(), eventType, eventRecord.getCreatedDateTime()));
        info.setCategoryCode(formatCategoryCode(eventType));
        info.setTypeCode(formatTypeCode(eventType));
        info.setSeverityCode(formatSeverityCode(eventType));
        info.setOriginTypeCode(formatOriginTypeCode(eventType));
        info.setDateTime(eventRecord.getCreatedDateTime());
        eventRecord.getProperties().forEach((key, value) -> info.getProperty().add(formatProperty(key, value)));
        return Optional.of(info);
    }

    private UtilitiesSmartMeterEventCategoryCode formatCategoryCode(SAPDeviceEventType eventType) {
        UtilitiesSmartMeterEventCategoryCode categoryCode = objectFactory.createUtilitiesSmartMeterEventCategoryCode();
        categoryCode.setValue(Integer.toString(eventType.getCategoryCode()));
        return categoryCode;
    }

    private UtilitiesSmartMeterEventTypeCode formatTypeCode(SAPDeviceEventType eventType) {
        UtilitiesSmartMeterEventTypeCode typeCode = objectFactory.createUtilitiesSmartMeterEventTypeCode();
        typeCode.setValue(Integer.toString(eventType.getTypeCode()));
        return typeCode;
    }

    private UtilitiesSmartMeterEventSeverityCode formatSeverityCode(SAPDeviceEventType eventType) {
        UtilitiesSmartMeterEventSeverityCode severityCode = objectFactory.createUtilitiesSmartMeterEventSeverityCode();
        severityCode.setValue(Integer.toString(eventType.getSeverityCode()));
        return severityCode;
    }

    private UtilitiesSmartMeterEventOriginTypeCode formatOriginTypeCode(SAPDeviceEventType eventType) {
        UtilitiesSmartMeterEventOriginTypeCode originTypeCode = objectFactory.createUtilitiesSmartMeterEventOriginTypeCode();
        originTypeCode.setValue(Integer.toString(eventType.getOriginTypeCode()));
        return originTypeCode;
    }

    private UtilsSmrtMtrEvtERPCrteReqPrpty formatProperty(String key, String value) {
        UtilsSmrtMtrEvtERPCrteReqPrpty utilsSmrtMtrEvtERPCrteReqPrpty = objectFactory.createUtilsSmrtMtrEvtERPCrteReqPrpty();
        UtilitiesSmartMeterEventPropertyCode utilitiesSmartMeterEventPropertyCode = objectFactory.createUtilitiesSmartMeterEventPropertyCode();
        utilitiesSmartMeterEventPropertyCode.setValue(key);
        utilsSmrtMtrEvtERPCrteReqPrpty.setCode(utilitiesSmartMeterEventPropertyCode);
        Text text = objectFactory.createText();
        text.setValue(value);
        ObjectPropertyValue objectPropertyValue = objectFactory.createObjectPropertyValue();
        objectPropertyValue.setText(text);
        utilsSmrtMtrEvtERPCrteReqPrpty.setObjectPropertyValue(objectPropertyValue);
        return utilsSmrtMtrEvtERPCrteReqPrpty;
    }

    private UtilitiesDeviceID formatDeviceId(String sapDeviceId) {
        UtilitiesDeviceID utilitiesDeviceID = objectFactory.createUtilitiesDeviceID();
        utilitiesDeviceID.setValue(sapDeviceId);
        return utilitiesDeviceID;
    }

    private UtilitiesSmartMeterEventID formatEventId(String sapDeviceId, SAPDeviceEventType eventType, Instant time) {
        UtilitiesSmartMeterEventID utilitiesSmartMeterEventID = objectFactory.createUtilitiesSmartMeterEventID();
        utilitiesSmartMeterEventID.setValue(sapDeviceId + '_'
                + eventType.getCategoryCode() + '.' + eventType.getTypeCode() + '.' + eventType.getSeverityCode() + '.' + eventType.getOriginTypeCode()
                + '_' + time.toEpochMilli());
        return utilitiesSmartMeterEventID;
    }
}
