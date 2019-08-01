/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractInboundEndPoint;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.elster.jupiter.soap.whiteboard.cxf.LogLevel;
import com.elster.jupiter.util.Checks;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.SAPWebServiceException;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterutilitiespodbulknotification.BusinessDocumentMessageHeader;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterutilitiespodbulknotification.BusinessDocumentMessageID;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterutilitiespodbulknotification.SmartMeterUtilitiesMeasurementTaskERPPointOfDeliveryBulkAssignedNotificationCIn;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterutilitiespodbulknotification.SmrtMtrUtilsMsmtTskERPPtDelivAssgndNotifMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterutilitiespodbulknotification.SmrtMtrUtilsMsmtTskERPPtDelivAssgndNotifUtilsDvce;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterutilitiespodbulknotification.SmrtMtrUtilsMsmtTskERPPtDelivAssgndNotifUtilsMsmtTsk;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterutilitiespodbulknotification.SmrtMtrUtilsMsmtTskERPPtDelivAssgndNotifUtilsPtDeliv;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterutilitiespodbulknotification.SmrtMtrUtilsMsmtTskERPPtDelivBulkAssgndNotifMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterutilitiespodbulknotification.UtilitiesDeviceID;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterutilitiespodbulknotification.UtilitiesPointOfDeliveryPartyID;

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
        if (bulkMsg.isValid()) {
            bulkMsg.podMessages.forEach(message -> {
                if (message.isValid()) {
                    Optional<Device> device = sapCustomPropertySets.getDevice(message.deviceId);
                    if (device.isPresent()) {
                        try {
                            sapCustomPropertySets.setPod(device.get(), message.podId);
                        } catch (SAPWebServiceException ex) {
                            log(LogLevel.WARNING, thesaurus.getFormat(ex.getMessageSeed()).format(ex.getMessageArgs()));
                        }
                    } else {
                        log(LogLevel.WARNING, thesaurus.getFormat(MessageSeeds.NO_DEVICE_FOUND_BY_SAP_ID).format(message.deviceId));
                    }
                } else {
                    log(LogLevel.WARNING, thesaurus.getFormat(MessageSeeds.INVALID_MESSAGE_FORMAT).format());
                }
            });
        } else {
            log(LogLevel.WARNING, thesaurus.getFormat(MessageSeeds.INVALID_MESSAGE_FORMAT).format());
        }
    }

    private class PodBulkMessage {
        private String requestId;
        private List<PodMessage> podMessages = new ArrayList<>();

        private PodBulkMessage(SmrtMtrUtilsMsmtTskERPPtDelivBulkAssgndNotifMsg msg) {
            requestId = getRequestId(msg);
            msg.getSmartMeterUtilitiesMeasurementTaskERPPointOfDeliveryAssignedNotificationMessage()
                    .forEach(message -> {
                        PodMessage podMsg = new PodMessage(message);
                        if (podMsg.isValid()) {
                            podMessages.add(podMsg);
                        }
                    });
        }

        private boolean isValid() {
            return requestId != null;
        }

        private String getRequestId(SmrtMtrUtilsMsmtTskERPPtDelivBulkAssgndNotifMsg msg) {
            return Optional.ofNullable(msg.getMessageHeader())
                    .map(BusinessDocumentMessageHeader::getID)
                    .map(BusinessDocumentMessageID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }
    }

    private class PodMessage {
        private String deviceId;
        private String podId;

        private PodMessage(SmrtMtrUtilsMsmtTskERPPtDelivAssgndNotifMsg msg) {
            deviceId = getDeviceId(msg);
            podId = getPodId(msg);
        }

        private boolean isValid() {
            return deviceId != null && podId != null;
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
                    .map(pod -> pod.get(0))
                    .map(SmrtMtrUtilsMsmtTskERPPtDelivAssgndNotifUtilsPtDeliv::getUtilitiesPointOfDeliveryPartyID)
                    .map(UtilitiesPointOfDeliveryPartyID::getValue)
                    .orElse(null);
        }
    }
}
