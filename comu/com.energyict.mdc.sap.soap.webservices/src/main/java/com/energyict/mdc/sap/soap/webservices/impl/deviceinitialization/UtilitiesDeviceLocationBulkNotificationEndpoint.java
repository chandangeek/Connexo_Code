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
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationbulknotification.BusinessDocumentMessageHeader;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationbulknotification.BusinessDocumentMessageID;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationbulknotification.InstallationPointID;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationbulknotification.UtilitiesDeviceERPSmartMeterLocationBulkNotificationCIn;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationbulknotification.UtilitiesDeviceID;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationbulknotification.UtilsDvceERPSmrtMtrLocBulkNotifMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationbulknotification.UtilsDvceERPSmrtMtrLocNotifLoc;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationbulknotification.UtilsDvceERPSmrtMtrLocNotifMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationbulknotification.UtilsDvceERPSmrtMtrLocNotifUtilsDvce;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class UtilitiesDeviceLocationBulkNotificationEndpoint implements UtilitiesDeviceERPSmartMeterLocationBulkNotificationCIn {

    private static final Logger LOGGER = Logger.getLogger(UtilitiesDeviceLocationBulkNotificationEndpoint.class.getName());

    private final SAPCustomPropertySets sapCustomPropertySets;
    private final ThreadPrincipalService threadPrincipalService;
    private final UserService userService;
    private final TransactionService transactionService;
    private final Thesaurus thesaurus;

    @Inject
    UtilitiesDeviceLocationBulkNotificationEndpoint(SAPCustomPropertySets sapCustomPropertySets, ThreadPrincipalService threadPrincipalService,
                                                    UserService userService, TransactionService transactionService, Thesaurus thesaurus) {
        this.sapCustomPropertySets = sapCustomPropertySets;
        this.threadPrincipalService = threadPrincipalService;
        this.userService = userService;
        this.transactionService = transactionService;
        this.thesaurus = thesaurus;
    }

    @Override
    public void utilitiesDeviceERPSmartMeterLocationBulkNotificationCIn(UtilsDvceERPSmrtMtrLocBulkNotifMsg request) {
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

    private void handleMessage(UtilsDvceERPSmrtMtrLocBulkNotifMsg msg) {
        LocationBulkMessage bulkMsg = new LocationBulkMessage(msg);
        if (bulkMsg.isValid()) {
            bulkMsg.locationMessages.forEach(message -> {
                if (message.isValid()) {
                    try (TransactionContext context = transactionService.getContext()) {
                        Optional<Device> device = sapCustomPropertySets.getDevice(message.deviceId);
                        if (device.isPresent()) {
                            sapCustomPropertySets.setLocation(device.get(), message.locationId);
                        }else{
                            LOGGER.severe("No device found with SAP id " + message.deviceId);
                        }
                        context.commit();
                    }
                }else{
                    LOGGER.severe("Invalid message format");
                }
            });
        }else{
            throw new SAPWebServiceException(thesaurus, MessageSeeds.INVALID_MESSAGE_FORMAT);
        }
    }

    private class LocationBulkMessage {
        private String requestId;
        private List<LocationMessage> locationMessages = new ArrayList<>();

        private LocationBulkMessage(UtilsDvceERPSmrtMtrLocBulkNotifMsg msg) {
            requestId = getRequestId(msg);
            msg.getUtilitiesDeviceERPSmartMeterLocationNotificationMessage()
                    .forEach(message -> {
                        LocationMessage locationMsg = new LocationMessage(message);
                        if (locationMsg.isValid()) {
                            locationMessages.add(locationMsg);
                        }
                    });
        }

        private boolean isValid() {
            return requestId != null;
        }

        private String getRequestId(UtilsDvceERPSmrtMtrLocBulkNotifMsg msg) {
            return Optional.ofNullable(msg.getMessageHeader())
                    .map(BusinessDocumentMessageHeader::getID)
                    .map(BusinessDocumentMessageID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }
    }

    private class LocationMessage {
        private String deviceId;
        private String locationId;

        private LocationMessage(UtilsDvceERPSmrtMtrLocNotifMsg msg) {
            deviceId = getDeviceId(msg);
            locationId = getLocationId(msg);
        }

        private boolean isValid() {
            return deviceId != null && locationId != null;
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
