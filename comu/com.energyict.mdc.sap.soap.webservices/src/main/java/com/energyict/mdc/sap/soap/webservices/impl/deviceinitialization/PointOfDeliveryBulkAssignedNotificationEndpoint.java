/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractInboundEndPoint;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.elster.jupiter.soap.whiteboard.cxf.LogLevel;
import com.elster.jupiter.util.Checks;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.SapAttributeNames;
import com.energyict.mdc.sap.soap.webservices.impl.AbstractSapMessage;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterutilitiespodbulknotification.BusinessDocumentMessageHeader;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterutilitiespodbulknotification.BusinessDocumentMessageID;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterutilitiespodbulknotification.SmartMeterUtilitiesMeasurementTaskERPPointOfDeliveryBulkAssignedNotificationCIn;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterutilitiespodbulknotification.SmrtMtrUtilsMsmtTskERPPtDelivAssgndNotifMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterutilitiespodbulknotification.SmrtMtrUtilsMsmtTskERPPtDelivAssgndNotifUtilsDvce;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterutilitiespodbulknotification.SmrtMtrUtilsMsmtTskERPPtDelivAssgndNotifUtilsMsmtTsk;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterutilitiespodbulknotification.SmrtMtrUtilsMsmtTskERPPtDelivAssgndNotifUtilsPtDeliv;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterutilitiespodbulknotification.SmrtMtrUtilsMsmtTskERPPtDelivBulkAssgndNotifMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterutilitiespodbulknotification.UUID;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterutilitiespodbulknotification.UtilitiesDeviceID;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterutilitiespodbulknotification.UtilitiesPointOfDeliveryPartyID;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PointOfDeliveryBulkAssignedNotificationEndpoint extends AbstractInboundEndPoint implements SmartMeterUtilitiesMeasurementTaskERPPointOfDeliveryBulkAssignedNotificationCIn, ApplicationSpecific {

    private final SAPCustomPropertySets sapCustomPropertySets;
    private final Thesaurus thesaurus;

    @Inject
    PointOfDeliveryBulkAssignedNotificationEndpoint(SAPCustomPropertySets sapCustomPropertySets, Thesaurus thesaurus) {
        this.sapCustomPropertySets = sapCustomPropertySets;
        this.thesaurus = thesaurus;
    }

    @Override
    public String getApplication() {
        return ApplicationSpecific.WebServiceApplicationName.MULTISENSE.getName();
    }

    @Override
    public void smartMeterUtilitiesMeasurementTaskERPPointOfDeliveryBulkAssignedNotificationCIn(SmrtMtrUtilsMsmtTskERPPtDelivBulkAssgndNotifMsg request) {
        runInTransactionWithOccurrence(() -> {
            Optional.ofNullable(request)
                    .ifPresent(requestMessage -> handleMessage(requestMessage));
            return null;
        });
    }

    private void handleMessage(SmrtMtrUtilsMsmtTskERPPtDelivBulkAssgndNotifMsg msg) {
        PodBulkMessage bulkMsg = new PodBulkMessage(msg);
        SetMultimap<String, String> values = HashMultimap.create();
        bulkMsg.podMessages.forEach(message -> values.put(SapAttributeNames.SAP_UTILITIES_DEVICE_ID.getAttributeName(), message.deviceId));
        saveRelatedAttributes(values);

        if (bulkMsg.isValid()) {
            bulkMsg.podMessages.forEach(message -> {
                if (message.isValid()) {
                    Optional<Device> device = sapCustomPropertySets.getDevice(message.deviceId);
                    if (device.isPresent()) {
                        try {
                            sapCustomPropertySets.setPod(device.get(), message.podId);
                        } catch (LocalizedException ex) {
                            log(LogLevel.WARNING, thesaurus.getFormat(ex.getMessageSeed()).format(ex.getMessageArgs()));
                        }
                    } else {
                        log(LogLevel.WARNING, thesaurus.getFormat(MessageSeeds.NO_DEVICE_FOUND_BY_SAP_ID).format(message.deviceId));
                    }
                } else {
                    log(LogLevel.WARNING, thesaurus.getFormat(MessageSeeds.INVALID_MESSAGE_FORMAT).format(message.getMissingFields()));
                }
            });
        } else {
            log(LogLevel.WARNING, thesaurus.getFormat(MessageSeeds.INVALID_MESSAGE_FORMAT).format(bulkMsg.getMissingFields()));
        }
    }

    private class PodBulkMessage extends AbstractSapMessage {
        private String requestId;
        private String uuid;
        private List<PodMessage> podMessages = new ArrayList<>();

        private PodBulkMessage(SmrtMtrUtilsMsmtTskERPPtDelivBulkAssgndNotifMsg msg) {
            requestId = getRequestId(msg);
            uuid = getUuid(msg);
            if (requestId == null && uuid == null) {
                addAtLeastOneMissingField(REQUEST_ID_XML_NAME, UUID_XML_NAME);
            }
            msg.getSmartMeterUtilitiesMeasurementTaskERPPointOfDeliveryAssignedNotificationMessage()
                    .forEach(message -> {
                        PodMessage podMsg = new PodMessage(message);
                        podMessages.add(podMsg);
                    });
        }

        private String getRequestId(SmrtMtrUtilsMsmtTskERPPtDelivBulkAssgndNotifMsg msg) {
            return Optional.ofNullable(msg.getMessageHeader())
                    .map(BusinessDocumentMessageHeader::getID)
                    .map(BusinessDocumentMessageID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getUuid(SmrtMtrUtilsMsmtTskERPPtDelivBulkAssgndNotifMsg msg) {
            return Optional.ofNullable(msg.getMessageHeader())
                    .map(BusinessDocumentMessageHeader::getUUID)
                    .map(UUID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }
    }

    private class PodMessage extends AbstractSapMessage {
        private static final String UTILITIES_DEVICE_ID_XML_NAME = "UtilitiesMeasurementTask." + AbstractSapMessage.UTILITIES_DEVICE_ID_XML_NAME;
        private static final String POD_ID_XML_NAME = "UtilitiesMeasurementTask.UtilitiesPointOfDeliveryAssignment.UtilitiesPointOfDeliveryPartyID";
        private String deviceId;
        private String podId;

        private PodMessage(SmrtMtrUtilsMsmtTskERPPtDelivAssgndNotifMsg msg) {
            deviceId = getDeviceId(msg);
            podId = getPodId(msg);
            if (deviceId == null) {
                addMissingField(UTILITIES_DEVICE_ID_XML_NAME);
            }
            if (podId == null) {
                addMissingField(POD_ID_XML_NAME);
            }
        }

        private String getDeviceId(SmrtMtrUtilsMsmtTskERPPtDelivAssgndNotifMsg msg) {
            return Optional.ofNullable(msg.getUtilitiesMeasurementTask())
                    .map(SmrtMtrUtilsMsmtTskERPPtDelivAssgndNotifUtilsMsmtTsk::getUtilitiesDevice)
                    .map(SmrtMtrUtilsMsmtTskERPPtDelivAssgndNotifUtilsDvce::getUtilitiesDeviceID)
                    .map(UtilitiesDeviceID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getPodId(SmrtMtrUtilsMsmtTskERPPtDelivAssgndNotifMsg msg) {
            return Optional.ofNullable(msg.getUtilitiesMeasurementTask())
                    .map(SmrtMtrUtilsMsmtTskERPPtDelivAssgndNotifUtilsMsmtTsk::getUtilitiesPointOfDeliveryAssignment)
                    .flatMap(pod -> pod.stream().findFirst())
                    .map(SmrtMtrUtilsMsmtTskERPPtDelivAssgndNotifUtilsPtDeliv::getUtilitiesPointOfDeliveryPartyID)
                    .map(UtilitiesPointOfDeliveryPartyID::getValue)
                    .orElse(null);
        }
    }
}
