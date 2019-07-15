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

public class PointOfDeliveryAssignedNotification implements SmartMeterUtilitiesMeasurementTaskERPPointOfDeliveryAssignedNotificationCIn {

    private final SAPCustomPropertySets sapCustomPropertySets;
    private final ThreadPrincipalService threadPrincipalService;
    private final UserService userService;
    private final TransactionService transactionService;

    @Inject
    PointOfDeliveryAssignedNotification(SAPCustomPropertySets sapCustomPropertySets, ThreadPrincipalService threadPrincipalService, UserService userService, TransactionService transactionService) {
        this.sapCustomPropertySets = sapCustomPropertySets;
        this.threadPrincipalService = threadPrincipalService;
        this.userService = userService;
        this.transactionService = transactionService;
    }

    @Override
    public void smartMeterUtilitiesMeasurementTaskERPPointOfDeliveryAssignedNotificationCIn(SmrtMtrUtilsMsmtTskERPPtDelivAssgndNotifMsg request) {
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

    private void handleMessage(SmrtMtrUtilsMsmtTskERPPtDelivAssgndNotifMsg msg) {
        PodMessage podMsg = new PodMessage(msg);
        if (podMsg.isValid()) {
            try (TransactionContext context = transactionService.getContext()) {
                Optional<Device> device = sapCustomPropertySets.getDevice(podMsg.deviceId);
                if (device.isPresent()) {
                    sapCustomPropertySets.setPod(device.get(), podMsg.podId);
                }
                context.commit();
            }
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
                    .map(pod -> pod.get(0))
                    .map(SmrtMtrUtilsMsmtTskERPPtDelivAssgndNotifUtilsPtDeliv::getUtilitiesPointOfDeliveryPartyID)
                    .map(UtilitiesPointOfDeliveryPartyID::getValue)
                    .orElse(null);
        }
    }
}
