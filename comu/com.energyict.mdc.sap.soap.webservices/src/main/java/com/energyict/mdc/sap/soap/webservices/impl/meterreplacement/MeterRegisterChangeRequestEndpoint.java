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
import com.energyict.mdc.sap.soap.webservices.UtilitiesDeviceRegisteredNotification;
import com.energyict.mdc.sap.soap.webservices.impl.AdditionalProperties;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.MeterRegisterChangeConfirmation;
import com.energyict.mdc.sap.soap.webservices.impl.SAPWebServiceException;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallCommands;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallHelper;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallTypes;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreplacement.MasterMeterRegisterChangeRequestDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreplacement.MeterRegisterChangeRequestDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreplacement.SubMasterMeterRegisterChangeRequestDomainExtension;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacementrequest.UtilitiesDeviceERPSmartMeterRegisterChangeRequestCIn;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacementrequest.UtilsDvceERPSmrtMtrRegChgReqMsg;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import javax.inject.Inject;
import java.time.Clock;
import java.util.Arrays;
import java.util.Optional;

import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.APPLICATION_NAME;

public class MeterRegisterChangeRequestEndpoint extends AbstractInboundEndPoint implements UtilitiesDeviceERPSmartMeterRegisterChangeRequestCIn, ApplicationSpecific {

    private final ServiceCallCommands serviceCallCommands;
    private final EndPointConfigurationService endPointConfigurationService;
    private final Thesaurus thesaurus;
    private final Clock clock;
    private final SAPCustomPropertySets sapCustomPropertySets;
    private final WebServiceActivator webServiceActivator;
    private final ServiceCallService serviceCallService;

    @Inject
    MeterRegisterChangeRequestEndpoint(ServiceCallCommands serviceCallCommands, EndPointConfigurationService endPointConfigurationService,
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
    public void utilitiesDeviceERPSmartMeterRegisterChangeRequestCIn(UtilsDvceERPSmrtMtrRegChgReqMsg request) {
        runInTransactionWithOccurrence(() -> {
            Optional.ofNullable(request)
                    .ifPresent(requestMessage -> {
                        MeterRegisterChangeMessage message = MeterRegisterChangeMessageBuilder
                                .builder(webServiceActivator.getSapProperty(AdditionalProperties.LRN_END_INTERVAL))
                                .from(requestMessage)
                                .build(thesaurus);
                        SetMultimap<String, String> values = HashMultimap.create();
                        message.getRegisters().forEach(register -> {
                            Optional.ofNullable(register.getLrn())
                                    .ifPresent(value -> values.put(SapAttributeNames.SAP_UTILITIES_MEASUREMENT_TASK_ID.getAttributeName(), value));
                        });
                        Optional.ofNullable(message.getDeviceId())
                                .ifPresent(value -> values.put(SapAttributeNames.SAP_UTILITIES_DEVICE_ID.getAttributeName(), value));
                        saveRelatedAttributes(values);
                        if (!isAnyActiveEndpoint(MeterRegisterChangeConfirmation.NAME)) {
                            throw new SAPWebServiceException(thesaurus, MessageSeeds.NO_REQUIRED_OUTBOUND_END_POINT,
                                    MeterRegisterChangeConfirmation.NAME);
                        }
                        if (message.getRegisters().size() > 1) {
                            if (!isAnyActiveEndpoint(UtilitiesDeviceRegisteredNotification.NAME)) {
                                throw new SAPWebServiceException(thesaurus, MessageSeeds.NO_REQUIRED_OUTBOUND_END_POINT,
                                        UtilitiesDeviceRegisteredNotification.NAME);
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

    private void createServiceCallAndTransition(MeterRegisterChangeMessage message) {
        if (message.isValid()) {
            if (hasOpenMeterChangeRequestServiceCall(message.getId(), message.getUuid())) {
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

    private void createServiceCall(ServiceCallType serviceCallType, MeterRegisterChangeMessage requestMessage) {
        MasterMeterRegisterChangeRequestDomainExtension masterMeterRegisterChangeRequestDomainExtension =
                new MasterMeterRegisterChangeRequestDomainExtension();
        masterMeterRegisterChangeRequestDomainExtension.setRequestId(requestMessage.getId());
        masterMeterRegisterChangeRequestDomainExtension.setUuid(requestMessage.getUuid());
        masterMeterRegisterChangeRequestDomainExtension.setBulk(false);

        ServiceCall serviceCall = serviceCallType.newServiceCall()
                .origin(APPLICATION_NAME)
                .extendedWith(masterMeterRegisterChangeRequestDomainExtension)
                .create();

        if (requestMessage.isValid()) {
            createSubParentServiceCall(serviceCall, requestMessage);
        }
        if (!serviceCall.findChildren().paged(0, 0).find().isEmpty()) {
            serviceCall.requestTransition(DefaultState.PENDING);
        } else {
            serviceCall.requestTransition(DefaultState.REJECTED);
            sendProcessError(requestMessage, MessageSeeds.INVALID_MESSAGE_FORMAT, requestMessage.getMissingFields());
        }
    }

    private void sendProcessError(MeterRegisterChangeMessage message, MessageSeeds messageSeed, Object... messageSeedArgs) {
        log(LogLevel.WARNING, thesaurus.getFormat(messageSeed).format(messageSeedArgs));
        MeterRegisterChangeConfirmationMessage confirmationMessage =
                MeterRegisterChangeConfirmationMessage.builder()
                        .from(message, messageSeed, webServiceActivator.getMeteringSystemId(), clock.instant(), messageSeedArgs)
                        .build();
        sendMessage(confirmationMessage);
    }

    private void sendMessage(MeterRegisterChangeConfirmationMessage message) {
        WebServiceActivator.METER_REGISTER_CHANGE_CONFIRMATIONS
                .forEach(service -> service.call(message));
    }

    private void createSubParentServiceCall(ServiceCall parent, MeterRegisterChangeMessage message) {
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
            sendProcessError(message, MessageSeeds.INVALID_MESSAGE_FORMAT, message.getMissingFields());
            subParent.requestTransition(DefaultState.REJECTED);
            return;
        }
        if (register.isValid()) {
            createChildServiceCall(subParent, register);
        } else {
            sendProcessError(message, MessageSeeds.INVALID_MESSAGE_FORMAT, register.getMissingFields());
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
        childDomainExtension.setFractionDigitNumberValue(message.getFractionDigitNumberValue());
        childDomainExtension.setTotalDigitNumberValue(message.getTotalDigitNumberValue());

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
