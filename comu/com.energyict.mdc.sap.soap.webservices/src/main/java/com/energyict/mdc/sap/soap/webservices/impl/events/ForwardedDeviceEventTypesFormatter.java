/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.events;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
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

import java.time.Clock;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

public class ForwardedDeviceEventTypesFormatter {
    private static final Logger LOGGER = Logger.getLogger(ForwardedDeviceEventTypesFormatter.class.getName());
    private final Map<String, SAPDeviceEventType> forwardedEventTypesByEventCode = new HashMap<>();
    private final Map<String, SAPDeviceEventType> forwardedEventTypesByDeviceEventCode = new HashMap<>();
    private final ObjectFactory objectFactory = new ObjectFactory();
    private final SAPCustomPropertySets sapCustomPropertySets;
    private final DeviceService deviceService;
    private final MeteringGroupsService meteringGroupsService;
    private final Clock сlock;
    private boolean disablePropertyTag = true;

    public ForwardedDeviceEventTypesFormatter(SAPCustomPropertySets sapCustomPropertySets, DeviceService deviceService, MeteringGroupsService meteringGroupsService, Clock сlock) {
        this.sapCustomPropertySets = sapCustomPropertySets;
        this.deviceService = deviceService;
        this.meteringGroupsService = meteringGroupsService;
        this.сlock = сlock;
    }

    public void setDisablePropertyTag(boolean disablePropertyTag) {
        this.disablePropertyTag = disablePropertyTag;
    }

    void add(SAPDeviceEventType eventType) {
        Optional<String> code = eventType.getEventCode();
        if (code.isPresent()) {
            forwardedEventTypesByEventCode.put(code.get(), eventType);
        } else {
            eventType.getDeviceEventCode().ifPresent(deviceEventCode -> forwardedEventTypesByDeviceEventCode.put(deviceEventCode, eventType));
        }
    }

    boolean contains(SAPDeviceEventType eventType) {
        return eventType.getEventCode().filter(forwardedEventTypesByEventCode::containsKey).isPresent()
                || eventType.getDeviceEventCode().filter(forwardedEventTypesByDeviceEventCode::containsKey).isPresent();
    }

    boolean filterByDeviceTypeAndGroup(SAPDeviceEventType eventType, EndDevice endDevice) {
        Optional<Device> device = deviceService.findDeviceByMrid(endDevice.getMRID());
        if (device.isPresent()) {
            String deviceType = device.get().getDeviceType().getName();
            boolean deviceTypeMatched = eventType.getDeviceTypes().isEmpty() || eventType.getDeviceTypes().contains(deviceType);
            if (deviceTypeMatched) {
                return eventType.getDeviceGroups().isEmpty() || eventType.getDeviceGroups().stream().anyMatch(gN -> isDeviceInGroup(gN, endDevice));
            }
        } else {
            LOGGER.info("Device isn't found for the end device '" + endDevice.getName() + "'.");
        }
        return false;
    }

    boolean isDeviceInGroup(String groupName, EndDevice endDevice) {
        Optional<EndDeviceGroup> endDeviceGroup = meteringGroupsService.findEndDeviceGroupByName(groupName);
        if (endDeviceGroup.isPresent()) {
            return endDeviceGroup.get().isMember(endDevice, сlock.instant());
        }
        return false;
    }

    public Optional<UtilsSmrtMtrEvtERPCrteReqUtilsSmrtMtrEvt> filterAndFormat(EndDeviceEventRecord eventRecord) {
        SAPDeviceEventType eventType = forwardedEventTypesByEventCode.get(eventRecord.getEventTypeCode());
        if (eventType == null) {
            eventType = forwardedEventTypesByDeviceEventCode.get(eventRecord.getDeviceEventType());
        }
        if (eventType == null) {
            return Optional.empty(); // this event type is either not present in mapping file, or has 'forwarded' = false
        }
        if (!filterByDeviceTypeAndGroup(eventType, eventRecord.getEndDevice())) {
            String eventInfo = eventType.getEventCode().isPresent() ? " for event type with code '" + eventType.getEventCode().get() + "'" :
                    eventType.getDeviceEventCode().isPresent() ? " for event type with device code '" + eventType.getDeviceEventCode().get() + "'" : "";
            LOGGER.fine("End device '" + eventRecord.getEndDevice().getName() + "'" + eventInfo + " has been filtered out.");
            return Optional.empty();
        }
        Optional<String> sapDeviceId = sapCustomPropertySets.getRegisteredSapDeviceId(eventRecord.getEndDevice());
        if (!sapDeviceId.isPresent()) {
            LOGGER.warning("No id found on 'SAP device info' CAS of device '" + eventRecord.getEndDevice().getName() + "' with registered flag set to 'Yes'.");
            return Optional.empty();
        }
        if (!sapCustomPropertySets.isAnyLrnPresentForDate(Long.parseLong(eventRecord.getEndDevice().getAmrId()), eventRecord.getCreatedDateTime())) {
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
        if (!disablePropertyTag) {
            eventRecord.getProperties().forEach((key, value) -> info.getProperty().add(formatProperty(key, value)));
        }
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
        String eventId = sapDeviceId + '_'
                + eventType.getCategoryCode() + '.' + eventType.getTypeCode() + '.' + eventType.getSeverityCode() + '.' + eventType.getOriginTypeCode()
                + '_' + time.toEpochMilli();
        // adjust eventId so it can be used in the utilitiesSmartMeterEventID tag with length 1-22
        utilitiesSmartMeterEventID.setValue(Long.toString(eventId.hashCode() & Integer.MAX_VALUE));

        return utilitiesSmartMeterEventID;
    }
}
