/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization;

import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.Checks;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
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

public class PointOfDeliveryBulkAssignedNotification implements SmartMeterUtilitiesMeasurementTaskERPPointOfDeliveryBulkAssignedNotificationCIn {

    private final SAPCustomPropertySets sapCustomPropertySets;
    private final ThreadPrincipalService threadPrincipalService;
    private final UserService userService;
    private final TransactionService transactionService;

    @Inject
    PointOfDeliveryBulkAssignedNotification(SAPCustomPropertySets sapCustomPropertySets, ThreadPrincipalService threadPrincipalService, UserService userService, TransactionService transactionService) {
        this.sapCustomPropertySets = sapCustomPropertySets;
        this.threadPrincipalService = threadPrincipalService;
        this.userService = userService;
        this.transactionService = transactionService;
    }

    @Override
    public void smartMeterUtilitiesMeasurementTaskERPPointOfDeliveryBulkAssignedNotificationCIn(SmrtMtrUtilsMsmtTskERPPtDelivBulkAssgndNotifMsg request) {
        setPrincipal();
        Optional.ofNullable(request)
                .ifPresent(requestMessage -> handleMessage(requestMessage));
    }

    private void setPrincipal() {
        if (threadPrincipalService.getPrincipal() == null) {
            userService.findUser(WebServiceActivator.BATCH_EXECUTOR_USER_NAME, userService.getRealm())
                    .ifPresent(threadPrincipalService::set);
        }
    }

    private void handleMessage(SmrtMtrUtilsMsmtTskERPPtDelivBulkAssgndNotifMsg msg) {
        PodBulkMessage bulkMsg = new PodBulkMessage(msg);
        if (bulkMsg.isValid()) {
            bulkMsg.podMessages.forEach(message -> {
                if (message.isValid()) {
                    try (TransactionContext context = transactionService.getContext()) {
                        Optional<Device> device = sapCustomPropertySets.getDevice(message.deviceId);
                        if (device.isPresent()) {
                            sapCustomPropertySets.setPod(device.get(), message.podId);
                        }
                        context.commit();
                    }
                }
            });
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
