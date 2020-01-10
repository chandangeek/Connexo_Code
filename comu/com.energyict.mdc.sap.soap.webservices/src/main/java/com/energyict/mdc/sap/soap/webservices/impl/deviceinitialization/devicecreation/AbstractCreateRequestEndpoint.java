/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.devicecreation;

import com.elster.jupiter.metering.CimAttributeNames;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallBuilder;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractInboundEndPoint;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.LogLevel;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.sap.soap.webservices.SapAttributeNames;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.SAPWebServiceException;
import com.energyict.mdc.sap.soap.webservices.impl.UtilitiesDeviceCreateConfirmation;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallCommands;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallTypes;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.MasterUtilitiesDeviceCreateRequestCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.MasterUtilitiesDeviceCreateRequestDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.UtilitiesDeviceCreateRequestDomainExtension;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import javax.inject.Inject;
import java.time.Clock;
import java.util.Optional;

import static com.elster.jupiter.util.conditions.Where.where;
import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.APPLICATION_NAME;

public class AbstractCreateRequestEndpoint extends AbstractInboundEndPoint implements ApplicationSpecific {
    private final Thesaurus thesaurus;
    private final ServiceCallCommands serviceCallCommands;
    private final EndPointConfigurationService endPointConfigurationService;
    private final Clock clock;
    private final OrmService ormService;
    private final WebServiceActivator webServiceActivator;
    private final DeviceService deviceService;


    @Inject
    public AbstractCreateRequestEndpoint(ServiceCallCommands serviceCallCommands, EndPointConfigurationService endPointConfigurationService,
                                         Clock clock, OrmService ormService, WebServiceActivator webServiceActivator, DeviceService deviceService) {
        this.thesaurus = webServiceActivator.getThesaurus();
        this.serviceCallCommands = serviceCallCommands;
        this.endPointConfigurationService = endPointConfigurationService;
        this.clock = clock;
        this.ormService = ormService;
        this.webServiceActivator = webServiceActivator;
        this.deviceService = deviceService;
    }

    @Override
    public String getApplication() {
        return ApplicationSpecific.WebServiceApplicationName.MULTISENSE.getName();
    }

    Thesaurus getThesaurus() {
        return thesaurus;
    }

    void handleRequestMessage(UtilitiesDeviceCreateRequestMessage requestMessage) {
        SetMultimap<String, String> values = HashMultimap.create();
        requestMessage.getUtilitiesDeviceCreateMessages().forEach(message -> {
            values.put(CimAttributeNames.SERIAL_ID.getAttributeName(), message.getSerialId());
            values.put(SapAttributeNames.SAP_UTILITIES_DEVICE_ID.getAttributeName(), message.getDeviceId());
        });

        saveRelatedAttributes(values);

        if (!isAnyActiveEndpoint(UtilitiesDeviceCreateConfirmation.NAME)) {
            throw new SAPWebServiceException(getThesaurus(), MessageSeeds.NO_REQUIRED_OUTBOUND_END_POINT,
                    UtilitiesDeviceCreateConfirmation.NAME);
        }
        createServiceCallAndTransition(requestMessage);
    }

    private void createServiceCallAndTransition(UtilitiesDeviceCreateRequestMessage message) {
        if (message.isValid()) {
            if (hasUtilDeviceRequestServiceCall(message.getRequestID(), message.getUuid())) {
                sendProcessError(message, MessageSeeds.MESSAGE_ALREADY_EXISTS);
            } else {
                serviceCallCommands.getServiceCallType(ServiceCallTypes.MASTER_UTILITIES_DEVICE_CREATE_REQUEST).ifPresent(serviceCallType -> {
                    createServiceCall(serviceCallType, message);
                });
            }
        } else {
            sendProcessError(message, MessageSeeds.INVALID_MESSAGE_FORMAT);
        }
    }

    private boolean isAnyActiveEndpoint(String name) {
        return endPointConfigurationService
                .getEndPointConfigurationsForWebService(name)
                .stream()
                .anyMatch(EndPointConfiguration::isActive);
    }

    private void createServiceCall(ServiceCallType serviceCallType, UtilitiesDeviceCreateRequestMessage requestMessage) {
        MasterUtilitiesDeviceCreateRequestDomainExtension masterUtilitiesDeviceCreateRequestDomainExtension =
                new MasterUtilitiesDeviceCreateRequestDomainExtension();
        masterUtilitiesDeviceCreateRequestDomainExtension.setRequestID(requestMessage.getRequestID());
        masterUtilitiesDeviceCreateRequestDomainExtension.setUuid(requestMessage.getUuid());
        masterUtilitiesDeviceCreateRequestDomainExtension.setBulk(requestMessage.isBulk());

        ServiceCall serviceCall = serviceCallType.newServiceCall()
                .origin(APPLICATION_NAME)
                .extendedWith(masterUtilitiesDeviceCreateRequestDomainExtension)
                .create();

        requestMessage.getUtilitiesDeviceCreateMessages()
                .forEach(bodyMessage -> {
                    if (bodyMessage.isValid()) {
                        createChildServiceCall(serviceCall, bodyMessage);
                    }
                });
        if (!serviceCall.findChildren().paged(0, 0).find().isEmpty()) {
            serviceCall.requestTransition(DefaultState.PENDING);
        } else {
            serviceCall.requestTransition(DefaultState.REJECTED);
            sendProcessError(requestMessage, MessageSeeds.INVALID_MESSAGE_FORMAT);
        }
    }

    private boolean hasUtilDeviceRequestServiceCall(String id, String uuid) {
        Optional<DataModel> dataModel = ormService.getDataModel(MasterUtilitiesDeviceCreateRequestCustomPropertySet.MODEL_NAME);
        if (id != null) {
            return dataModel.map(dataModel1 -> dataModel1.stream(MasterUtilitiesDeviceCreateRequestDomainExtension.class)
                    .anyMatch(where(MasterUtilitiesDeviceCreateRequestDomainExtension.FieldNames.REQUEST_ID.javaName()).isEqualTo(id)))
                    .orElse(false);
        } else {
            return dataModel.map(dataModel1 -> dataModel1.stream(MasterUtilitiesDeviceCreateRequestDomainExtension.class)
                    .anyMatch(where(MasterUtilitiesDeviceCreateRequestDomainExtension.FieldNames.UUID.javaName()).isEqualTo(uuid)))
                    .orElse(false);
        }
    }

    private void sendProcessError(UtilitiesDeviceCreateRequestMessage message, MessageSeeds messageSeed) {
        log(LogLevel.WARNING, thesaurus.getFormat(messageSeed).format());
        UtilitiesDeviceCreateConfirmationMessage confirmationMessage =
                UtilitiesDeviceCreateConfirmationMessage.builder()
                        .from(message, messageSeed, webServiceActivator.getMeteringSystemId(), clock.instant())
                        .build();
        sendMessage(confirmationMessage, message.isBulk());
    }

    private void sendMessage(UtilitiesDeviceCreateConfirmationMessage confirmationMessage, boolean bulk) {
        if (bulk) {
            WebServiceActivator.UTILITIES_DEVICE_BULK_CREATE_CONFIRMATION
                    .forEach(service -> service.call(confirmationMessage));
        } else {
            WebServiceActivator.UTILITIES_DEVICE_CREATE_CONFIRMATION
                    .forEach(service -> service.call(confirmationMessage));
        }
    }

    private void createChildServiceCall(ServiceCall parent, UtilitiesDeviceCreateMessage message) {
        ServiceCallType serviceCallType = serviceCallCommands.getServiceCallTypeOrThrowException(ServiceCallTypes.UTILITIES_DEVICE_CREATE_REQUEST);

        UtilitiesDeviceCreateRequestDomainExtension childDomainExtension = new UtilitiesDeviceCreateRequestDomainExtension();
        childDomainExtension.setRequestId(message.getRequestId());
        childDomainExtension.setUuid(message.getUuid());
        childDomainExtension.setSerialId(message.getSerialId());
        childDomainExtension.setDeviceId(message.getDeviceId());
        childDomainExtension.setMaterialId(message.getMaterialId());
        Optional.ofNullable(webServiceActivator.getDeviceTypesMap().get(message.getMaterialId())).ifPresent(childDomainExtension::setDeviceType);
        childDomainExtension.setShipmentDate(message.getShipmentDate());
        childDomainExtension.setManufacturer(message.getManufacturer());
        childDomainExtension.setModelNumber(message.getModelNumber());


        ServiceCallBuilder serviceCallBuilder = parent.newChildCall(serviceCallType)
                .extendedWith(childDomainExtension);
        deviceService.findDeviceByName(message.getSerialId()).ifPresent(serviceCallBuilder::targetObject);
        serviceCallBuilder.create();
    }
}
