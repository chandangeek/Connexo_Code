/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.Checks;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.SAPWebServiceException;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationnotification.BusinessDocumentMessageHeader;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationnotification.BusinessDocumentMessageID;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationnotification.InstallationPointID;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationnotification.UtilitiesDeviceERPSmartMeterLocationNotificationCIn;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationnotification.UtilitiesDeviceID;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationnotification.UtilsDvceERPSmrtMtrLocNotifLoc;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationnotification.UtilsDvceERPSmrtMtrLocNotifMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationnotification.UtilsDvceERPSmrtMtrLocNotifUtilsDvce;

import javax.inject.Inject;
import java.util.Optional;

public class UtilitiesDeviceLocationNotificationEndpoint implements UtilitiesDeviceERPSmartMeterLocationNotificationCIn {

    private final SAPCustomPropertySets sapCustomPropertySets;
    private final ThreadPrincipalService threadPrincipalService;
    private final UserService userService;
    private final TransactionService transactionService;
    private final Thesaurus thesaurus;

    @Inject
    UtilitiesDeviceLocationNotificationEndpoint(SAPCustomPropertySets sapCustomPropertySets, ThreadPrincipalService threadPrincipalService,
                                                UserService userService, TransactionService transactionService, Thesaurus thesaurus) {
        this.sapCustomPropertySets = sapCustomPropertySets;
        this.threadPrincipalService = threadPrincipalService;
        this.userService = userService;
        this.transactionService = transactionService;
        this.thesaurus = thesaurus;
    }

    @Override
    public void utilitiesDeviceERPSmartMeterLocationNotificationCIn(UtilsDvceERPSmrtMtrLocNotifMsg request) {
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

    private void handleMessage(UtilsDvceERPSmrtMtrLocNotifMsg msg) {
        LocationMessage locationMsg = new LocationMessage(msg);
        if (locationMsg.isValid()) {
            try (TransactionContext context = transactionService.getContext()) {
                Optional<Device> device = sapCustomPropertySets.getDevice(locationMsg.deviceId);
                if (device.isPresent()) {
                    sapCustomPropertySets.setLocation(device.get(), locationMsg.locationId);
                }else{
                    throw new SAPWebServiceException(thesaurus, MessageSeeds.NO_DEVICE_FOUND_BY_SAP_ID, locationMsg.deviceId);
                }
                context.commit();
            }
        }else{
            throw new SAPWebServiceException(thesaurus, MessageSeeds.INVALID_MESSAGE_FORMAT);
        }
    }

    private class LocationMessage {
        private String requestId;
        private String deviceId;
        private String locationId;

        private LocationMessage(UtilsDvceERPSmrtMtrLocNotifMsg msg) {
            requestId = getRequestId(msg);
            deviceId = getDeviceId(msg);
            locationId = getLocationId(msg);
        }

        private boolean isValid() {
            return requestId != null && deviceId != null && locationId != null;
        }

        private String getRequestId(UtilsDvceERPSmrtMtrLocNotifMsg msg) {
            return Optional.ofNullable(msg.getMessageHeader())
                    .map(BusinessDocumentMessageHeader::getID)
                    .map(BusinessDocumentMessageID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getDeviceId(UtilsDvceERPSmrtMtrLocNotifMsg msg) {
            return Optional.ofNullable(msg.getUtilitiesDevice())
                    .map(UtilsDvceERPSmrtMtrLocNotifUtilsDvce::getID)
                    .map(UtilitiesDeviceID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getLocationId(UtilsDvceERPSmrtMtrLocNotifMsg msg) {
            return Optional.ofNullable(msg.getUtilitiesDevice())
                    .map(UtilsDvceERPSmrtMtrLocNotifUtilsDvce::getLocation)
                    .map(location -> location.get(0))
                    .map(UtilsDvceERPSmrtMtrLocNotifLoc::getInstallationPointID)
                    .map(InstallationPointID::getValue)
                    .orElse(null);
        }
    }
}
