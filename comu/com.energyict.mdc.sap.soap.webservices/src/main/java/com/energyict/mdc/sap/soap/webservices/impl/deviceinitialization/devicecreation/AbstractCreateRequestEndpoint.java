/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.devicecreation;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.metering.CimAttributeNames;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallBuilder;
import com.elster.jupiter.servicecall.ServiceCallFilter;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractInboundEndPoint;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.LogLevel;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.sap.soap.webservices.SapAttributeNames;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallCommands;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallTypes;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.MasterUtilitiesDeviceCreateRequestDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.UtilitiesDeviceCreateRequestDomainExtension;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import javax.inject.Inject;
import java.time.Clock;
import java.util.Arrays;
import java.util.Optional;

import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.APPLICATION_NAME;

public abstract class AbstractCreateRequestEndpoint extends AbstractInboundEndPoint implements ApplicationSpecific {
    private final Thesaurus thesaurus;
    private final ServiceCallCommands serviceCallCommands;
    private final EndPointConfigurationService endPointConfigurationService;
    private final Clock clock;
    private final WebServiceActivator webServiceActivator;
    private final DeviceService deviceService;
    private final ServiceCallService serviceCallService;


    @Inject
    public AbstractCreateRequestEndpoint(ServiceCallCommands serviceCallCommands, EndPointConfigurationService endPointConfigurationService,
                                         Clock clock, WebServiceActivator webServiceActivator, DeviceService deviceService, ServiceCallService serviceCallService) {
        this.thesaurus = webServiceActivator.getThesaurus();
        this.serviceCallCommands = serviceCallCommands;
        this.endPointConfigurationService = endPointConfigurationService;
        this.clock = clock;
        this.webServiceActivator = webServiceActivator;
        this.deviceService = deviceService;
        this.serviceCallService = serviceCallService;
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

        validateConfiguredEndpoints();

        createServiceCallAndTransition(requestMessage);
    }

    abstract void validateConfiguredEndpoints();

    private void createServiceCallAndTransition(UtilitiesDeviceCreateRequestMessage message) {
        if (message.isValid()) {
            if (hasOpenUtilDeviceRequestServiceCall(message.getRequestID(), message.getUuid())) {
                sendProcessError(message, MessageSeeds.MESSAGE_ALREADY_EXISTS);
            } else {
                serviceCallCommands.getServiceCallType(ServiceCallTypes.MASTER_UTILITIES_DEVICE_CREATE_REQUEST).ifPresent(serviceCallType -> {
                    createServiceCall(serviceCallType, message);
                });
            }
        } else {
            sendProcessError(message, MessageSeeds.INVALID_MESSAGE_FORMAT, message.getMissingFields());
        }
    }

    boolean isAnyActiveEndpoint(String name) {
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
            sendProcessError(requestMessage, MessageSeeds.INVALID_MESSAGE_FORMAT, requestMessage.getMissingFields());
        }
    }

    private void sendProcessError(UtilitiesDeviceCreateRequestMessage message, MessageSeeds messageSeed, Object... messageSeedArgs) {
        log(LogLevel.WARNING, thesaurus.getFormat(messageSeed).format(messageSeedArgs));
        UtilitiesDeviceCreateConfirmationMessage confirmationMessage =
                UtilitiesDeviceCreateConfirmationMessage.builder()
                        .from(message, messageSeed, webServiceActivator.getMeteringSystemId(), clock.instant(), messageSeedArgs)
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
        Optional.ofNullable((webServiceActivator.getExternalSystemName().equals(webServiceActivator.EXTERNAL_SYSTEM_EDA))?message.getManufacturerModel():webServiceActivator.getDeviceTypesMap().get(message.getMaterialId())).ifPresent(childDomainExtension::setDeviceType);
        childDomainExtension.setShipmentDate(message.getShipmentDate());
        childDomainExtension.setManufacturer(message.getManufacturer());
        childDomainExtension.setManufacturerSerialId(message.getManufacturerSerialId());


        ServiceCallBuilder serviceCallBuilder = parent.newChildCall(serviceCallType)
                .extendedWith(childDomainExtension);
        deviceService.findDeviceByName(message.getSerialId()).ifPresent(serviceCallBuilder::targetObject);
        serviceCallBuilder.create();
    }

    private boolean hasOpenUtilDeviceRequestServiceCall(String id, String uuid) {
        return findAvailableOpenServiceCalls(ServiceCallTypes.MASTER_UTILITIES_DEVICE_CREATE_REQUEST)
                .stream()
                .map(serviceCall -> serviceCall.getExtension(MasterUtilitiesDeviceCreateRequestDomainExtension.class))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .anyMatch(domainExtension -> {
                    if (id != null) {
                        return domainExtension.getRequestID() != null && domainExtension.getRequestID().equals(id);
                    } else {
                        return domainExtension.getUuid() != null && domainExtension.getUuid().equals(uuid);
                    }
                });
    }

    public Finder<ServiceCall> findAvailableOpenServiceCalls(ServiceCallTypes serviceCallType) {
        ServiceCallFilter filter = new ServiceCallFilter();
        filter.types.add(serviceCallType.getTypeName());
        Arrays.stream(DefaultState.values()).filter(DefaultState::isOpen).map(DefaultState::name).forEach(filter.states::add);
        return serviceCallService.getServiceCallFinder(filter);
    }
}
