/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.registercreation;

import com.elster.jupiter.domain.util.Finder;
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
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.SapAttributeNames;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallCommands;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallTypes;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.MasterUtilitiesDeviceRegisterCreateRequestDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.SubMasterUtilitiesDeviceRegisterCreateRequestDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.UtilitiesDeviceRegisterCreateRequestDomainExtension;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import javax.inject.Inject;
import java.time.Clock;
import java.util.Arrays;
import java.util.Optional;

import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.APPLICATION_NAME;

public abstract class AbstractRegisterCreateRequestEndpoint extends AbstractInboundEndPoint implements ApplicationSpecific {

    private final ServiceCallCommands serviceCallCommands;
    private final EndPointConfigurationService endPointConfigurationService;
    private final Clock clock;
    private final SAPCustomPropertySets sapCustomPropertySets;
    private final Thesaurus thesaurus;
    private final WebServiceActivator webServiceActivator;
    private final ServiceCallService serviceCallService;

    @Inject
    AbstractRegisterCreateRequestEndpoint(ServiceCallCommands serviceCallCommands, EndPointConfigurationService endPointConfigurationService,
                                          Clock clock, SAPCustomPropertySets sapCustomPropertySets, Thesaurus thesaurus,
                                          WebServiceActivator webServiceActivator, ServiceCallService serviceCallService) {
        this.serviceCallCommands = serviceCallCommands;
        this.endPointConfigurationService = endPointConfigurationService;
        this.clock = clock;
        this.sapCustomPropertySets = sapCustomPropertySets;
        this.thesaurus = thesaurus;
        this.webServiceActivator = webServiceActivator;
        this.serviceCallService = serviceCallService;
    }

    @Override
    public String getApplication() {
        return ApplicationSpecific.WebServiceApplicationName.MULTISENSE.getName();
    }

    Thesaurus getThesaurus() {
        return thesaurus;
    }

    WebServiceActivator getWebServiceActivator() {
        return webServiceActivator;
    }

    void handleRequestMessage(UtilitiesDeviceRegisterCreateRequestMessage requestMessage) {

        SetMultimap<String, String> values = HashMultimap.create();

        requestMessage.getUtilitiesDeviceRegisterCreateMessages().forEach(device -> {
            values.put(SapAttributeNames.SAP_UTILITIES_DEVICE_ID.getAttributeName(), device.getDeviceId());
            device.getUtilitiesDeviceRegisterMessages().forEach(reg -> {
                values.put(SapAttributeNames.SAP_UTILITIES_MEASUREMENT_TASK_ID.getAttributeName(), reg.getLrn());
            });
        });

        saveRelatedAttributes(values);

        validateConfiguredEndpoints();

        createServiceCallAndTransition(requestMessage);
    }

    abstract void validateConfiguredEndpoints();

    private void createServiceCallAndTransition(UtilitiesDeviceRegisterCreateRequestMessage message) {
        if (message.isValid()) {
            if (hasOpenUtilDeviceRegisterRequestServiceCall(message.getRequestID(), message.getUuid())) {
                sendProcessError(message, MessageSeeds.MESSAGE_ALREADY_EXISTS);
            } else {
                serviceCallCommands.getServiceCallType(ServiceCallTypes.MASTER_UTILITIES_DEVICE_REGISTER_CREATE_REQUEST).ifPresent(serviceCallType -> {
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

    private void createServiceCall(ServiceCallType serviceCallType, UtilitiesDeviceRegisterCreateRequestMessage requestMessage) {
        MasterUtilitiesDeviceRegisterCreateRequestDomainExtension masterUtilitiesDeviceRegisterCreateRequestDomainExtension =
                new MasterUtilitiesDeviceRegisterCreateRequestDomainExtension();
        masterUtilitiesDeviceRegisterCreateRequestDomainExtension.setRequestID(requestMessage.getRequestID());
        masterUtilitiesDeviceRegisterCreateRequestDomainExtension.setUuid(requestMessage.getUuid());
        masterUtilitiesDeviceRegisterCreateRequestDomainExtension.setBulk(requestMessage.isBulk());

        ServiceCall serviceCall = serviceCallType.newServiceCall()
                .origin(APPLICATION_NAME)
                .extendedWith(masterUtilitiesDeviceRegisterCreateRequestDomainExtension)
                .create();

        requestMessage.getUtilitiesDeviceRegisterCreateMessages()
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

    private void createChildServiceCall(ServiceCall parent, UtilitiesDeviceRegisterCreateMessage message) {
        ServiceCallType serviceCallType = serviceCallCommands.getServiceCallTypeOrThrowException(ServiceCallTypes.SUB_MASTER_UTILITIES_DEVICE_REGISTER_CREATE_REQUEST);

        SubMasterUtilitiesDeviceRegisterCreateRequestDomainExtension childDomainExtension = new SubMasterUtilitiesDeviceRegisterCreateRequestDomainExtension();
        childDomainExtension.setRequestId(message.getRequestId());
        childDomainExtension.setUuid(message.getUuid());
        childDomainExtension.setDeviceId(message.getDeviceId());

        ServiceCallBuilder serviceCallBuilder = parent.newChildCall(serviceCallType)
                .extendedWith(childDomainExtension);
        sapCustomPropertySets.getDevice(message.getDeviceId()).ifPresent(serviceCallBuilder::targetObject);
        ServiceCall serviceCall = serviceCallBuilder.create();

        message.getUtilitiesDeviceRegisterMessages()
                .forEach(bodyMessage -> {
                    if (bodyMessage.isValid()) {
                        createSecondChildServiceCall(serviceCall, bodyMessage, message.getDeviceId());
                    }
                });
    }

    private void createSecondChildServiceCall(ServiceCall parent, UtilitiesDeviceRegisterMessage bodyMessage, String deviceId) {
        ServiceCallType serviceCallType = serviceCallCommands.getServiceCallTypeOrThrowException(ServiceCallTypes.UTILITIES_DEVICE_REGISTER_CREATE_REQUEST);

        UtilitiesDeviceRegisterCreateRequestDomainExtension childDomainExtension = new UtilitiesDeviceRegisterCreateRequestDomainExtension();
        childDomainExtension.setDeviceId(deviceId);
        childDomainExtension.setLrn(bodyMessage.getLrn());
        childDomainExtension.setObis(bodyMessage.getObis());
        childDomainExtension.setRecurrenceCode(bodyMessage.getRecurrenceCode());
        childDomainExtension.setDivisionCategory(bodyMessage.getDivisionCategory());
        childDomainExtension.setStartDate(bodyMessage.getStartDate());
        childDomainExtension.setEndDate(bodyMessage.getEndDate());
        childDomainExtension.setTimeZone(bodyMessage.getTimeZone());
        childDomainExtension.setRegisterId(bodyMessage.getRegisterId());

        ServiceCallBuilder serviceCallBuilder = parent.newChildCall(serviceCallType)
                .extendedWith(childDomainExtension);
        sapCustomPropertySets.getDevice(deviceId).ifPresent(serviceCallBuilder::targetObject);
        serviceCallBuilder.create();
    }

    private void sendProcessError(UtilitiesDeviceRegisterCreateRequestMessage message, MessageSeeds messageSeed, Object... messageSeedArgs) {
        log(LogLevel.WARNING, thesaurus.getFormat(messageSeed).format(messageSeedArgs));
        UtilitiesDeviceRegisterCreateConfirmationMessage confirmationMessage = null;
        confirmationMessage =
                UtilitiesDeviceRegisterCreateConfirmationMessage.builder()
                        .from(message, messageSeed, webServiceActivator.getMeteringSystemId(), clock.instant(), messageSeedArgs)
                        .build();
        sendMessage(confirmationMessage, message.isBulk());
    }

    private void sendMessage(UtilitiesDeviceRegisterCreateConfirmationMessage confirmationMessage, boolean bulk) {
        if (bulk) {
            WebServiceActivator.UTILITIES_DEVICE_REGISTER_BULK_CREATE_CONFIRMATION
                    .forEach(service -> service.call(confirmationMessage));
        } else {
            WebServiceActivator.UTILITIES_DEVICE_REGISTER_CREATE_CONFIRMATION
                    .forEach(service -> service.call(confirmationMessage));
        }
    }

    private boolean hasOpenUtilDeviceRegisterRequestServiceCall(String id, String uuid) {
        return findAvailableOpenServiceCalls(ServiceCallTypes.MASTER_UTILITIES_DEVICE_REGISTER_CREATE_REQUEST)
                .stream()
                .map(serviceCall -> serviceCall.getExtension(MasterUtilitiesDeviceRegisterCreateRequestDomainExtension.class))
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