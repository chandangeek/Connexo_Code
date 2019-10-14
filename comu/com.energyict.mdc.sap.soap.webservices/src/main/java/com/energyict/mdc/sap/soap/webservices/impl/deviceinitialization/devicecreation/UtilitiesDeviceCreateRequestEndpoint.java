/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.devicecreation;

import com.elster.jupiter.metering.CimAttributeNames;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractInboundEndPoint;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.LogLevel;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceBuilder;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.SapAttributeNames;
import com.energyict.mdc.sap.soap.webservices.impl.ProcessingResultCode;
import com.energyict.mdc.sap.soap.webservices.impl.SAPWebServiceException;
import com.energyict.mdc.sap.soap.webservices.impl.UtilitiesDeviceCreateConfirmation;
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
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreaterequest.PartyInternalID;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreaterequest.ProductInternalID;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreaterequest.UtilitiesDeviceERPSmartMeterCreateRequestCIn;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreaterequest.UtilsDvceERPSmrtMtrCrteReqIndivMatlMfrInfo;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreaterequest.UtilsDvceERPSmrtMtrCrteReqMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreaterequest.UtilsDvceERPSmrtMtrCrteReqUtilsDvce;

import javax.inject.Inject;
import java.text.MessageFormat;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UtilitiesDeviceCreateRequestEndpoint extends AbstractInboundEndPoint implements UtilitiesDeviceERPSmartMeterCreateRequestCIn, ApplicationSpecific {

    private final DeviceService deviceService;
    private final Clock clock;
    private final SAPCustomPropertySets sapCustomPropertySets;
    private final EndPointConfigurationService endPointConfigurationService;
    private final Thesaurus thesaurus;
    private final DeviceConfigurationService deviceConfigurationService;

    private final ObjectFactory objectFactory = new ObjectFactory();

    @Inject
    UtilitiesDeviceCreateRequestEndpoint(DeviceService deviceService, Clock clock, SAPCustomPropertySets sapCustomPropertySets,
                                         EndPointConfigurationService endPointConfigurationService, Thesaurus thesaurus,
                                         DeviceConfigurationService deviceConfigurationService) {
        this.deviceService = deviceService;
        this.clock = clock;
        this.sapCustomPropertySets = sapCustomPropertySets;
        this.endPointConfigurationService = endPointConfigurationService;
        this.thesaurus = thesaurus;
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Override
    public void utilitiesDeviceERPSmartMeterCreateRequestCIn(UtilsDvceERPSmrtMtrCrteReqMsg request) {
        runInTransactionWithOccurrence(() -> {

            Optional.ofNullable(request).ifPresent(requestMsg->{
                saveRelatedAttribute(CimAttributeNames.SERIAL_ID.getAttributeName(), getSerialId(requestMsg));
                saveRelatedAttribute(SapAttributeNames.SAP_UTILITIES_DEVICE_ID.getAttributeName(), getDeviceId(requestMsg));
            });


            if (!isAnyActiveEndpoint(UtilitiesDeviceCreateConfirmation.NAME)) {
                throw new SAPWebServiceException(thesaurus, MessageSeeds.NO_REQUIRED_OUTBOUND_END_POINT,
                        UtilitiesDeviceCreateConfirmation.NAME);
            }

            Optional.ofNullable(request)
                    .ifPresent(requestMessage -> handleMessage(requestMessage));
            return null;
        });
    }

    private void handleMessage(UtilsDvceERPSmrtMtrCrteReqMsg msg) {
        if (isValid(msg)) {
            Device device;
            String serialId = getSerialId(msg);
            String sapDeviceId = getDeviceId(msg);
            List<Device> devices = deviceService.findDevicesBySerialNumber(serialId);

            try {
                if (!devices.isEmpty()) {
                    if (devices.size() == 1) {
                        device = devices.get(0);
                    } else {
                        sendProcessError(msg, MessageSeeds.SEVERAL_DEVICES, serialId);
                        return;
                    }
                } else {
                    device = createDevice(msg);
                }
                sapCustomPropertySets.setSapDeviceId(device, sapDeviceId);
            } catch (LocalizedException ex) {
                sendProcessError(msg, ex.getMessageSeed(), ex.getMessageArgs());
                return;
            }

            UtilsDvceERPSmrtMtrCrteConfMsg confirmMsg = createConfirmationMessage(getRequestId(msg), sapDeviceId);
            sendMessage(confirmMsg);
        } else {
            sendProcessError(msg, MessageSeeds.INVALID_MESSAGE_FORMAT);
        }
    }

    private Device createDevice(UtilsDvceERPSmrtMtrCrteReqMsg msg) {
        DeviceConfiguration deviceConfig = findDeviceConfiguration(getDeviceType(msg));
        DeviceBuilder deviceBuilder = deviceService.newDeviceBuilder(deviceConfig,
                getSerialId(msg), getShipmentDate(msg));
        deviceBuilder.withSerialNumber(getSerialId(msg));
        deviceBuilder.withManufacturer(getManufacturer(msg));
        deviceBuilder.withModelNumber(getModelNmber(msg));
        return deviceBuilder.create();
    }

    private DeviceConfiguration findDeviceConfiguration(String deviceTypeName) {
        DeviceConfiguration deviceConfiguration =
                deviceConfigurationService.findDeviceTypeByName(deviceTypeName)
                        .orElseThrow(() -> new SAPWebServiceException(thesaurus, MessageSeeds.NO_DEVICE_TYPE_FOUND, deviceTypeName))
                        .getConfigurations()
                        .stream()
                        .filter(config -> config.isDefault())
                        .findAny().orElseThrow(() -> new SAPWebServiceException(thesaurus, MessageSeeds.NO_DEFAULT_DEVICE_CONFIGURATION, deviceTypeName));
        return deviceConfiguration;
    }

    private void sendProcessError(UtilsDvceERPSmrtMtrCrteReqMsg msg, MessageSeed messageSeed, Object... args) {
        log(LogLevel.WARNING, thesaurus.getFormat(messageSeed).format(args));

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

    private boolean isAnyActiveEndpoint(String name) {
        return endPointConfigurationService
                .getEndPointConfigurationsForWebService(name)
                .stream()
                .filter(EndPointConfiguration::isActive)
                .findAny().isPresent();
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
        log.setBusinessDocumentProcessingResultCode(ProcessingResultCode.SUCCESSFUL.getCode());
        return log;
    }

    private Log createFailedLog(MessageSeed messageSeed, Object... args) {
        Log log = objectFactory.createLog();
        log.setBusinessDocumentProcessingResultCode(ProcessingResultCode.FAILED.getCode());
        log.getItem().add(createLogItem(messageSeed, args));
        return log;
    }

    private LogItem createLogItem(MessageSeed messageSeed, Object... args) {
        LogItemCategoryCode logItemCategoryCode = objectFactory.createLogItemCategoryCode();
        logItemCategoryCode.setValue("PRE");

        LogItem logItem = objectFactory.createLogItem();
        logItem.setTypeID(String.valueOf(messageSeed.getNumber()));
        logItem.setCategoryCode(logItemCategoryCode);
        logItem.setNote(MessageFormat.format(messageSeed.getDefaultFormat(), args));

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
        return getRequestId(msg) != null && getSerialId(msg) != null && getDeviceId(msg) != null &&
                getDeviceType(msg) != null && getShipmentDate(msg) != null;
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

    private String getDeviceType(UtilsDvceERPSmrtMtrCrteReqMsg msg) {
        return Optional.ofNullable(msg.getUtilitiesDevice())
                .map(UtilsDvceERPSmrtMtrCrteReqUtilsDvce::getMaterialID)
                .map(ProductInternalID::getValue)
                .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                .orElse(null);
    }

    private String getManufacturer(UtilsDvceERPSmrtMtrCrteReqMsg msg) {
        return Optional.ofNullable(msg.getUtilitiesDevice())
                .map(UtilsDvceERPSmrtMtrCrteReqUtilsDvce::getIndividualMaterialManufacturerInformation)
                .map(UtilsDvceERPSmrtMtrCrteReqIndivMatlMfrInfo::getPartyInternalID)
                .map(PartyInternalID::getValue)
                .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                .orElse(null);
    }

    private String getModelNmber(UtilsDvceERPSmrtMtrCrteReqMsg msg) {
        return Optional.ofNullable(msg.getUtilitiesDevice())
                .map(UtilsDvceERPSmrtMtrCrteReqUtilsDvce::getIndividualMaterialManufacturerInformation)
                .map(UtilsDvceERPSmrtMtrCrteReqIndivMatlMfrInfo::getSerialID)
                .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                .orElse(null);
    }

    private Instant getShipmentDate(UtilsDvceERPSmrtMtrCrteReqMsg msg) {
        return Optional.ofNullable(msg.getUtilitiesDevice())
                .map(UtilsDvceERPSmrtMtrCrteReqUtilsDvce::getStartDate)
                .orElse(null);
    }

    @Override
    public String getApplication() {
        return ApplicationSpecific.WebServiceApplicationName.MULTISENSE.getName();
    }
}
