/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.meterreplacement;

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
import com.energyict.mdc.sap.soap.webservices.impl.AdditionalProperties;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.MeterRegisterBulkChangeConfirmation;
import com.energyict.mdc.sap.soap.webservices.impl.SAPWebServiceException;
import com.energyict.mdc.sap.soap.webservices.impl.UtilitiesDeviceRegisteredBulkNotification;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallCommands;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallHelper;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallTypes;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreplacement.MasterMeterRegisterChangeRequestDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreplacement.MeterRegisterChangeRequestDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreplacement.SubMasterMeterRegisterChangeRequestDomainExtension;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacementbulkrequest.UtilitiesDeviceERPSmartMeterRegisterBulkChangeRequestCIn;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacementbulkrequest.UtilsDvceERPSmrtMtrRegBulkChgReqMsg;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import javax.inject.Inject;
import java.time.Clock;
import java.util.Arrays;
import java.util.Optional;

import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.APPLICATION_NAME;

public class MeterRegisterBulkChangeRequestEndpoint extends AbstractInboundEndPoint implements UtilitiesDeviceERPSmartMeterRegisterBulkChangeRequestCIn, ApplicationSpecific {

    private final ServiceCallCommands serviceCallCommands;
    private final EndPointConfigurationService endPointConfigurationService;
    private final Thesaurus thesaurus;
    private final Clock clock;
    private final SAPCustomPropertySets sapCustomPropertySets;
    private final WebServiceActivator webServiceActivator;
    private final ServiceCallService serviceCallService;

    @Inject
    MeterRegisterBulkChangeRequestEndpoint(ServiceCallCommands serviceCallCommands, EndPointConfigurationService endPointConfigurationService,
                                           Thesaurus thesaurus, Clock clock, SAPCustomPropertySets sapCustomPropertySets,
                                           WebServiceActivator webServiceActivator, ServiceCallService serviceCallService) {
        this.serviceCallCommands = serviceCallCommands;
        this.endPointConfigurationService = endPointConfigurationService;
        this.thesaurus = thesaurus;
        this.clock = clock;
        this.sapCustomPropertySets = sapCustomPropertySets;
        this.webServiceActivator = webServiceActivator;
        this.serviceCallService = serviceCallService;
    }

    @Override
    public String getApplication() {
        return ApplicationSpecific.WebServiceApplicationName.MULTISENSE.getName();
    }

    @Override
    public void utilitiesDeviceERPSmartMeterRegisterBulkChangeRequestCIn(UtilsDvceERPSmrtMtrRegBulkChgReqMsg request) {
        runInTransactionWithOccurrence(() -> {
            Optional.ofNullable(request)
                    .ifPresent(requestMessage -> {
                        MeterRegisterBulkChangeRequestMessage message = MeterRegisterBulkChangeRequestMessage
                                .builder(webServiceActivator.getSapProperty(AdditionalProperties.LRN_END_INTERVAL), thesaurus)
                                .from(requestMessage)
                                .build();
                        SetMultimap<String, String> values = HashMultimap.create();
                        message.getMeterRegisterChangeMessages().forEach(msg -> {
                            msg.getRegisters().forEach(register -> {
                                Optional.ofNullable(register.getLrn())
                                        .ifPresent(value -> values.put(SapAttributeNames.SAP_UTILITIES_MEASUREMENT_TASK_ID.getAttributeName(), value));
                            });
                            Optional.ofNullable(msg.getDeviceId())
                                    .ifPresent(value -> values.put(SapAttributeNames.SAP_UTILITIES_DEVICE_ID.getAttributeName(), value));
                        });
                        saveRelatedAttributes(values);
                        if (!isAnyActiveEndpoint(MeterRegisterBulkChangeConfirmation.NAME)) {
                            throw new SAPWebServiceException(thesaurus, MessageSeeds.NO_REQUIRED_OUTBOUND_END_POINT,
                                    MeterRegisterBulkChangeConfirmation.NAME);
                        }
                        if (message.getMeterRegisterChangeMessages().stream().anyMatch(m -> m.getRegisters().size() > 1)) {
                            if (!isAnyActiveEndpoint(UtilitiesDeviceRegisteredBulkNotification.NAME)) {
                                throw new SAPWebServiceException(thesaurus, MessageSeeds.NO_REQUIRED_OUTBOUND_END_POINT,
                                        UtilitiesDeviceRegisteredBulkNotification.NAME);
                            }
                        }
                        createServiceCallAndTransition(message);
                    });
            return null;
        });
    }

    private boolean isAnyActiveEndpoint(String name) {
        return endPointConfigurationService
                .getEndPointConfigurationsForWebService(name)
                .stream()
                .filter(EndPointConfiguration::isActive)
                .findAny().isPresent();
    }

    private void createServiceCallAndTransition(MeterRegisterBulkChangeRequestMessage message) {
        if (message.isValid()) {
            if (hasOpenMeterChangeRequestServiceCall(message.getRequestId(), message.getUuid())) {
                sendProcessError(message, MessageSeeds.MESSAGE_ALREADY_EXISTS);
            } else {
                serviceCallCommands.getServiceCallType(ServiceCallTypes.MASTER_METER_REGISTER_CHANGE_REQUEST).ifPresent(serviceCallType -> {
                    createServiceCall(serviceCallType, message);
                });
            }
        } else {
            sendProcessError(message, MessageSeeds.INVALID_MESSAGE_FORMAT, message.getMissingFields());
        }
    }

    private void createServiceCall(ServiceCallType serviceCallType, MeterRegisterBulkChangeRequestMessage requestMessage) {
        MasterMeterRegisterChangeRequestDomainExtension masterMeterRegisterChangeRequestDomainExtension =
                new MasterMeterRegisterChangeRequestDomainExtension();
        masterMeterRegisterChangeRequestDomainExtension.setRequestId(requestMessage.getRequestId());
        masterMeterRegisterChangeRequestDomainExtension.setUuid(requestMessage.getUuid());
        masterMeterRegisterChangeRequestDomainExtension.setBulk(true);

        ServiceCall serviceCall = serviceCallType.newServiceCall()
                .origin(APPLICATION_NAME)
                .extendedWith(masterMeterRegisterChangeRequestDomainExtension)
                .create();

        requestMessage.getMeterRegisterChangeMessages()
                .forEach(bodyMessage -> {
                    if (bodyMessage.isValid()) {
                        createSubParentServiceCall(serviceCall, requestMessage, bodyMessage);
                    } else {
                        sendProcessError(requestMessage, bodyMessage, MessageSeeds.INVALID_MESSAGE_FORMAT, bodyMessage.getMissingFields());
                    }
                });
        if (!serviceCall.findChildren().paged(0, 0).find().isEmpty()) {
            serviceCall.requestTransition(DefaultState.PENDING);
        } else {
            serviceCall.requestTransition(DefaultState.REJECTED);
        }
    }

    private void sendProcessError(MeterRegisterBulkChangeRequestMessage messages, MeterRegisterChangeMessage message, MessageSeeds messageSeed, Object... messageSeedArgs) {
        log(LogLevel.WARNING, thesaurus.getFormat(messageSeed).format(messageSeedArgs));
        MeterRegisterBulkChangeConfirmationMessage confirmationMessage =
                MeterRegisterBulkChangeConfirmationMessage.builder()
                        .from(messages, message, messageSeed, webServiceActivator.getMeteringSystemId(), clock.instant(), messageSeedArgs)
                        .build();
        sendMessage(confirmationMessage);
    }

    private void sendProcessError(MeterRegisterBulkChangeRequestMessage messages, MessageSeeds messageSeed, Object... messageSeedArgs) {
        log(LogLevel.WARNING, thesaurus.getFormat(messageSeed).format(messageSeedArgs));
        MeterRegisterBulkChangeConfirmationMessage confirmationMessage =
                MeterRegisterBulkChangeConfirmationMessage.builder()
                        .from(messages, messageSeed, webServiceActivator.getMeteringSystemId(), clock.instant(), messageSeedArgs)
                        .build();
        sendMessage(confirmationMessage);
    }

    private void sendMessage(MeterRegisterBulkChangeConfirmationMessage message) {
        WebServiceActivator.METER_REGISTER_BULK_CHANGE_CONFIRMATIONS
                .forEach(service -> service.call(message));
    }

    private void createSubParentServiceCall(ServiceCall parent, MeterRegisterBulkChangeRequestMessage messages, MeterRegisterChangeMessage message) {
        ServiceCallType serviceCallType = serviceCallCommands.getServiceCallTypeOrThrowException(ServiceCallTypes.SUB_MASTER_METER_REGISTER_CHANGE_REQUEST);

        SubMasterMeterRegisterChangeRequestDomainExtension subParentDomainExtension = new SubMasterMeterRegisterChangeRequestDomainExtension();
        subParentDomainExtension.setRequestId(message.getId());
        subParentDomainExtension.setUuid(message.getUuid());
        subParentDomainExtension.setDeviceId(message.getDeviceId());
        if (message.getRegisters().size() == 1) {
            subParentDomainExtension.setCreateRequest(false);
        } else if (message.getRegisters().size() > 1) {
            subParentDomainExtension.setCreateRequest(true);
        } else {
            subParentDomainExtension.setCreateRequest(false);
        }

        ServiceCallBuilder serviceCallBuilder = parent.newChildCall(serviceCallType)
                .extendedWith(subParentDomainExtension);
        sapCustomPropertySets.getDevice(message.getDeviceId()).ifPresent(serviceCallBuilder::targetObject);
        ServiceCall subParent = serviceCallBuilder.create();

        RegisterChangeMessage register;
        if (message.getRegisters().size() == 1) {
            register = message.getRegisters().get(0);
        } else if (message.getRegisters().size() > 1) {
            register = message.getRegisters().get(message.getRegisters().size() - 1);
        } else {
            sendProcessError(messages, message, MessageSeeds.INVALID_MESSAGE_FORMAT, message.getMissingFields());
            subParent.requestTransition(DefaultState.REJECTED);
            return;
        }
        if (register.isValid()) {
            createChildServiceCall(subParent, register);
        } else {
            sendProcessError(messages, message, MessageSeeds.INVALID_MESSAGE_FORMAT, register.getMissingFields());
        }
        if (!ServiceCallHelper.findChildren(subParent).isEmpty()) {
            subParent.requestTransition(DefaultState.PENDING);
        } else {
            subParent.requestTransition(DefaultState.REJECTED);
        }
    }


    private void createChildServiceCall(ServiceCall subParent, RegisterChangeMessage message) {
        ServiceCallType serviceCallType = serviceCallCommands.getServiceCallTypeOrThrowException(ServiceCallTypes.METER_REGISTER_CHANGE_REQUEST);

        MeterRegisterChangeRequestDomainExtension childDomainExtension = new MeterRegisterChangeRequestDomainExtension();
        childDomainExtension.setLrn(message.getLrn());
        childDomainExtension.setEndDate(message.getEndDate());
        childDomainExtension.setTimeZone(message.getTimeZone());
        childDomainExtension.setObis(message.getObis());
        childDomainExtension.setRecurrenceCode(message.getRecurrenceCode());
        childDomainExtension.setDivisionCategory(message.getDivisionCategory());
        childDomainExtension.setRegisterId(message.getRegisterId());
        childDomainExtension.setStartDate(message.getStartDate());

        ServiceCallBuilder serviceCallBuilder = subParent.newChildCall(serviceCallType)
                .extendedWith(childDomainExtension);
        serviceCallBuilder.create();
    }

    private boolean hasOpenMeterChangeRequestServiceCall(String id, String uuid) {
        return findAvailableOpenServiceCalls(ServiceCallTypes.MASTER_METER_REGISTER_CHANGE_REQUEST)
                .stream()
                .map(serviceCall -> serviceCall.getExtension(MasterMeterRegisterChangeRequestDomainExtension.class))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .anyMatch(domainExtension -> {
                    if (id != null) {
                        return domainExtension.getRequestId() != null && domainExtension.getRequestId().equals(id);
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
