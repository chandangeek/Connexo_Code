/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.devicecreation;

import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.Checks;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.impl.SAPWebServiceException;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreateconfirmation.BusinessDocumentMessageHeader;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreateconfirmation.BusinessDocumentMessageID;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreateconfirmation.Log;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreateconfirmation.LogItem;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreateconfirmation.LogItemCategoryCode;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreateconfirmation.ObjectFactory;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreateconfirmation.UtilitiesDeviceID;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreateconfirmation.UtilsDvceERPSmrtMtrCrteConfMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreateconfirmation.UtilsDvceERPSmrtMtrCrteConfUtilsDvce;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreaterequest.UtilitiesDeviceERPSmartMeterCreateRequestCIn;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreaterequest.UtilsDvceERPSmrtMtrCrteReqMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreaterequest.UtilsDvceERPSmrtMtrCrteReqUtilsDvce;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UtilitiesDeviceCreateRequestEndpoint implements UtilitiesDeviceERPSmartMeterCreateRequestCIn {

    private final DeviceService deviceService;
    private final Clock clock;
    private final SAPCustomPropertySets sapCustomPropertySets;
    private final ThreadPrincipalService threadPrincipalService;
    private final UserService userService;
    private final TransactionService transactionService;
    private final ObjectFactory objectFactory = new ObjectFactory();

    @Inject
    UtilitiesDeviceCreateRequestEndpoint(DeviceService deviceService, Clock clock, SAPCustomPropertySets sapCustomPropertySets,
                                         ThreadPrincipalService threadPrincipalService, UserService userService, TransactionService transactionService) {
        this.deviceService = deviceService;
        this.clock = clock;
        this.sapCustomPropertySets = sapCustomPropertySets;
        this.threadPrincipalService = threadPrincipalService;
        this.userService = userService;
        this.transactionService = transactionService;
    }

    @Override
    public void utilitiesDeviceERPSmartMeterCreateRequestCIn(UtilsDvceERPSmrtMtrCrteReqMsg request) {
        setPrincipal();
        Optional.ofNullable(request)
                .ifPresent(requestMessage -> handleMessage(requestMessage));
    }

    private void handleMessage(UtilsDvceERPSmrtMtrCrteReqMsg msg) {
        if (isValid(msg)) {
            String serialId = getSerialId(msg);
            List<Device> devices = deviceService.findDevicesBySerialNumber(serialId);
            if (!devices.isEmpty()) {
                if (devices.size() == 1) {
                    Device device = devices.get(0);
                    String sapDeviceId = getDeviceId(msg);

                    try (TransactionContext context = transactionService.getContext()) {
                        sapCustomPropertySets.addSapDeviceId(device, sapDeviceId);
                        context.commit();
                    } catch (SAPWebServiceException ex) {
                        sendProcessError(msg, (MessageSeeds) ex.getMessageSeed(), ex.getMessageArgs());
                        return;
                    }

                    UtilsDvceERPSmrtMtrCrteConfMsg confirmMsg = createConfirmationMessage(getRequestId(msg), sapDeviceId);
                    sendMessage(confirmMsg);
                } else {
                    sendProcessError(msg, MessageSeeds.SEVERAL_DEVICES, serialId);
                }
            } else {
                sendProcessError(msg, MessageSeeds.NO_DEVICE_FOUND_BY_SERIAL_ID, serialId);
            }
        } else {
            sendProcessError(msg, MessageSeeds.INVALID_MESSAGE_FORMAT);
        }
    }

    private void sendProcessError(UtilsDvceERPSmrtMtrCrteReqMsg msg, MessageSeeds messageSeed, Object... args) {
        UtilsDvceERPSmrtMtrCrteConfMsg confirmMsg = objectFactory.createUtilsDvceERPSmrtMtrCrteConfMsg();
        confirmMsg.setMessageHeader(createMessageHeader(getRequestId(msg), clock.instant()));
        confirmMsg.setUtilitiesDevice(createUtilitiesDevice(getDeviceId(msg)));
        confirmMsg.setLog(createFailedLog(messageSeed, args));

        sendMessage(confirmMsg);
    }

    private void sendMessage(UtilsDvceERPSmrtMtrCrteConfMsg msg) {
        WebServiceActivator.UTILITIES_DEVICE_CREATE_CONFIRMATION
                .forEach(service -> service.call(msg));
    }

    private UtilsDvceERPSmrtMtrCrteConfMsg createConfirmationMessage(String requestId, String sapDeviceId) {
        Instant now = clock.instant();
        UtilsDvceERPSmrtMtrCrteConfMsg confirmMsg = objectFactory.createUtilsDvceERPSmrtMtrCrteConfMsg();
        confirmMsg.setMessageHeader(createMessageHeader(requestId, now));
        confirmMsg.setUtilitiesDevice(createUtilitiesDevice(sapDeviceId));
        confirmMsg.setLog(createSuccessfulLog());
        return confirmMsg;
    }

    private BusinessDocumentMessageHeader createMessageHeader(String requestId, Instant now) {
        String uuid = UUID.randomUUID().toString();

        BusinessDocumentMessageHeader header = objectFactory.createBusinessDocumentMessageHeader();
        header.setReferenceID(createID(requestId));
        header.setUUID(createUUID(uuid));
        header.setCreationDateTime(now);
        return header;
    }

    private UtilsDvceERPSmrtMtrCrteConfUtilsDvce createUtilitiesDevice(String strId) {
        UtilsDvceERPSmrtMtrCrteConfUtilsDvce device = objectFactory.createUtilsDvceERPSmrtMtrCrteConfUtilsDvce();
        UtilitiesDeviceID utDevice = objectFactory.createUtilitiesDeviceID();
        utDevice.setValue(strId);
        device.setID(utDevice);
        return device;
    }

    private Log createSuccessfulLog() {
        Log log = objectFactory.createLog();
        log.setBusinessDocumentProcessingResultCode("3");
        return log;
    }

    private Log createFailedLog(MessageSeeds messageSeeds, Object... args) {
        Log log = objectFactory.createLog();
        log.setBusinessDocumentProcessingResultCode("5");
        log.getItem().add(createLogItem(messageSeeds, args));
        return log;
    }

    private LogItem createLogItem(MessageSeeds messageSeeds, Object... args) {
        LogItemCategoryCode logItemCategoryCode = objectFactory.createLogItemCategoryCode();
        logItemCategoryCode.setValue("PRE");

        LogItem logItem = objectFactory.createLogItem();
        logItem.setTypeID(String.valueOf(messageSeeds.getNumber()));
        logItem.setCategoryCode(logItemCategoryCode);
        logItem.setNote(messageSeeds.getDefaultFormat(args));

        return logItem;
    }

    private BusinessDocumentMessageID createID(String id) {
        BusinessDocumentMessageID messageID = objectFactory.createBusinessDocumentMessageID();
        messageID.setValue(id);
        return messageID;
    }

    private com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreateconfirmation.UUID createUUID(String uuid) {
        com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreateconfirmation.UUID messageUUID
                = objectFactory.createUUID();
        messageUUID.setValue(uuid);
        return messageUUID;
    }

    private boolean isValid(UtilsDvceERPSmrtMtrCrteReqMsg msg) {
        return getRequestId(msg) != null && getSerialId(msg) != null && getDeviceId(msg) != null;
    }

    private String getDeviceId(UtilsDvceERPSmrtMtrCrteReqMsg msg) {
        return Optional.ofNullable(msg.getUtilitiesDevice())
                .map(UtilsDvceERPSmrtMtrCrteReqUtilsDvce::getID)
                .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreaterequest.UtilitiesDeviceID::getValue)
                .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                .orElse(null);
    }

    private String getSerialId(UtilsDvceERPSmrtMtrCrteReqMsg msg) {
        return Optional.ofNullable(msg.getUtilitiesDevice())
                .map(UtilsDvceERPSmrtMtrCrteReqUtilsDvce::getSerialID)
                .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                .orElse(null);
    }

    private String getRequestId(UtilsDvceERPSmrtMtrCrteReqMsg msg) {
        return Optional.ofNullable(msg.getMessageHeader())
                .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreaterequest.BusinessDocumentMessageHeader::getID)
                .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreaterequest.BusinessDocumentMessageID::getValue)
                .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                .orElse(null);
    }

    private void setPrincipal() {
        if (threadPrincipalService.getPrincipal() == null) {
            userService.findUser(WebServiceActivator.BATCH_EXECUTOR_USER_NAME, userService.getRealm())
                    .ifPresent(threadPrincipalService::set);
        }
    }
}
