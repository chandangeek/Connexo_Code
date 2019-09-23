/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractInboundEndPoint;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.elster.jupiter.soap.whiteboard.cxf.LogLevel;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceRequestAttributesNames;
import com.elster.jupiter.util.Checks;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterutilitiespodnotification.BusinessDocumentMessageHeader;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterutilitiespodnotification.BusinessDocumentMessageID;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterutilitiespodnotification.SmartMeterUtilitiesMeasurementTaskERPPointOfDeliveryAssignedNotificationCIn;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterutilitiespodnotification.SmrtMtrUtilsMsmtTskERPPtDelivAssgndNotifMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterutilitiespodnotification.SmrtMtrUtilsMsmtTskERPPtDelivAssgndNotifUtilsDvce;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterutilitiespodnotification.SmrtMtrUtilsMsmtTskERPPtDelivAssgndNotifUtilsMsmtTsk;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterutilitiespodnotification.SmrtMtrUtilsMsmtTskERPPtDelivAssgndNotifUtilsPtDeliv;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterutilitiespodnotification.UtilitiesDeviceID;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterutilitiespodnotification.UtilitiesPointOfDeliveryPartyID;

import javax.inject.Inject;
import java.util.Optional;

public class PointOfDeliveryAssignedNotificationEndpoint extends AbstractInboundEndPoint implements SmartMeterUtilitiesMeasurementTaskERPPointOfDeliveryAssignedNotificationCIn, ApplicationSpecific {

    private final SAPCustomPropertySets sapCustomPropertySets;
    private final Thesaurus thesaurus;

    @Inject
    PointOfDeliveryAssignedNotificationEndpoint(SAPCustomPropertySets sapCustomPropertySets, Thesaurus thesaurus) {
        this.sapCustomPropertySets = sapCustomPropertySets;
        this.thesaurus = thesaurus;
    }

    @Override
    public String getApplication() {
        return ApplicationSpecific.WebServiceApplicationName.MULTISENSE.getName();
    }

    @Override
    public void smartMeterUtilitiesMeasurementTaskERPPointOfDeliveryAssignedNotificationCIn(SmrtMtrUtilsMsmtTskERPPtDelivAssgndNotifMsg request) {
        runInTransactionWithOccurrence(() -> {
            Optional.ofNullable(request)
                    .ifPresent(requestMessage -> handleMessage(requestMessage));
            return null;
        });
    }

    private void handleMessage(SmrtMtrUtilsMsmtTskERPPtDelivAssgndNotifMsg msg) {
        PodMessage podMsg = new PodMessage(msg);
        if (podMsg.isValid()) {
            createRelatedObject(WebServiceRequestAttributesNames.SAP_UTILITIES_DEVICE_ID.getAttributeName(), podMsg.deviceId);
            Optional<Device> device = sapCustomPropertySets.getDevice(podMsg.deviceId);
            if (device.isPresent()) {
                try {

                    sapCustomPropertySets.setPod(device.get(), podMsg.podId);
                } catch (LocalizedException ex) {
                    log(LogLevel.WARNING, thesaurus.getFormat(ex.getMessageSeed()).format(ex.getMessageArgs()));
                }
            } else {
                log(LogLevel.WARNING, thesaurus.getFormat(MessageSeeds.NO_DEVICE_FOUND_BY_SAP_ID).format(podMsg.deviceId));
            }
        } else {
            log(LogLevel.WARNING, thesaurus.getFormat(MessageSeeds.INVALID_MESSAGE_FORMAT).format());
        }
    }

    private class PodMessage {
        private String requestId;
        private String deviceId;
        private String podId;

        private PodMessage(SmrtMtrUtilsMsmtTskERPPtDelivAssgndNotifMsg msg) {
            requestId = getRequestId(msg);
            deviceId = getDeviceId(msg);
            podId = getPodId(msg);
        }

        private boolean isValid() {
            return requestId != null && deviceId != null && podId != null;
        }

        private String getRequestId(SmrtMtrUtilsMsmtTskERPPtDelivAssgndNotifMsg msg) {
            return Optional.ofNullable(msg.getMessageHeader())
                    .map(BusinessDocumentMessageHeader::getID)
                    .map(BusinessDocumentMessageID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
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
