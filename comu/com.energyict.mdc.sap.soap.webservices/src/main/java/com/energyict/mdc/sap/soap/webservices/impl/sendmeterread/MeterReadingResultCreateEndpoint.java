/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.sendmeterread;

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
import com.energyict.mdc.sap.soap.webservices.impl.MeterReadingResultCreateConfirmation;
import com.energyict.mdc.sap.soap.webservices.impl.SAPWebServiceException;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallCommands;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallTypes;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.receivemeterreadings.MasterMeterReadingResultCreateRequestDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.receivemeterreadings.MeterReadingResultCreateRequestDomainExtension;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingresultcreaterequest.SmartMeterMeterReadingDocumentERPResultCreateRequestCIn;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingresultcreaterequest.SmrtMtrMtrRdngDocERPRsltCrteReqMsg;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import javax.inject.Inject;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Optional;

import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.APPLICATION_NAME;

public class MeterReadingResultCreateEndpoint extends AbstractInboundEndPoint implements SmartMeterMeterReadingDocumentERPResultCreateRequestCIn, ApplicationSpecific {

    private final ServiceCallCommands serviceCallCommands;
    private final EndPointConfigurationService endPointConfigurationService;
    private final Thesaurus thesaurus;
    private final Clock clock;
    private final SAPCustomPropertySets sapCustomPropertySets;
    private final WebServiceActivator webServiceActivator;
    private final ServiceCallService serviceCallService;

    @Inject
    MeterReadingResultCreateEndpoint(ServiceCallCommands serviceCallCommands, EndPointConfigurationService endPointConfigurationService,
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
    public void smartMeterMeterReadingDocumentERPResultCreateRequestCIn(SmrtMtrMtrRdngDocERPRsltCrteReqMsg request) {
        runInTransactionWithOccurrence(() -> {
            Optional.ofNullable(request)
                    .ifPresent(requestMessage -> {

                        MeterReadingResultCreateRequestMessage message = MeterReadingResultCreateRequestMessage
                                .builder(thesaurus)
                                .from(requestMessage)
                                .build();
                        SetMultimap<String, String> values = HashMultimap.create();
                        Optional.ofNullable(message.getMeterReadingResultCreateMessage().getLrn())
                                .ifPresent(value -> values.put(SapAttributeNames.SAP_UTILITIES_MEASUREMENT_TASK_ID.getAttributeName(), value));
                        Optional.ofNullable(message.getMeterReadingResultCreateMessage().getDeviceId())
                                .ifPresent(value -> values.put(SapAttributeNames.SAP_UTILITIES_DEVICE_ID.getAttributeName(), value));
                        Optional.ofNullable(message.getMeterReadingResultCreateMessage().getId())
                                .ifPresent(value -> values.put(SapAttributeNames.SAP_METER_READING_DOCUMENT_ID.getAttributeName(), value));
                        saveRelatedAttributes(values);
                        if (!isAnyActiveEndpoint(MeterReadingResultCreateConfirmation.NAME)) {
                            throw new SAPWebServiceException(thesaurus, MessageSeeds.NO_REQUIRED_OUTBOUND_END_POINT,
                                    MeterReadingResultCreateConfirmation.NAME);
                        }
                        createServiceCallAndTransition(message);
                    });
            return null;
        });
    }

    private void createServiceCallAndTransition(MeterReadingResultCreateRequestMessage message) {
        if (message.isValid()) {
            if (hasOpenMeterReadingResultCreateServiceCall(message.getId(), message.getUuid())) {
                sendProcessError(message, MessageSeeds.MESSAGE_ALREADY_EXISTS);
            } else {
                Optional<ServiceCallType> serviceCallType = serviceCallCommands.getServiceCallType(ServiceCallTypes.MASTER_METER_READING_RESULT_CREATE_REQUEST);
                if (serviceCallType.isPresent()) {
                    createServiceCall(serviceCallType.get(), message);
                } else {
                    sendProcessError(message, MessageSeeds.COULD_NOT_FIND_SERVICE_CALL_TYPE, ServiceCallTypes.MASTER_METER_READING_RESULT_CREATE_REQUEST.getTypeName(), ServiceCallTypes.MASTER_METER_READING_RESULT_CREATE_REQUEST
                            .getTypeVersion());
                }
            }
        } else {
            sendProcessError(message, MessageSeeds.INVALID_MESSAGE_FORMAT, message.getMissingFields());
        }
    }

    private void createServiceCall(ServiceCallType serviceCallType, MeterReadingResultCreateRequestMessage requestMessage) {
        MasterMeterReadingResultCreateRequestDomainExtension masterMeterReadingResultCreateRequestDomainExtension = new MasterMeterReadingResultCreateRequestDomainExtension();
        masterMeterReadingResultCreateRequestDomainExtension.setRequestId(requestMessage.getId());
        masterMeterReadingResultCreateRequestDomainExtension.setUuid(requestMessage.getUuid());

        ServiceCall serviceCall = serviceCallType.newServiceCall()
                .origin(APPLICATION_NAME)
                .extendedWith(masterMeterReadingResultCreateRequestDomainExtension)
                .create();
        MeterReadingResultCreateMessage message = requestMessage.getMeterReadingResultCreateMessage();

        if (message.isValid()) {
            createChildServiceCall(serviceCall, message);
        }

        if (!serviceCall.findChildren().paged(0, 0).find().isEmpty()) {
            serviceCall.requestTransition(DefaultState.PENDING);
        } else {
            serviceCall.requestTransition(DefaultState.REJECTED);
            sendProcessError(requestMessage, MessageSeeds.INVALID_MESSAGE_FORMAT, requestMessage.getMissingFields());
        }
    }

    private void createChildServiceCall(ServiceCall parent, MeterReadingResultCreateMessage message) {
        ServiceCallType serviceCallType = serviceCallCommands.getServiceCallTypeOrThrowException(ServiceCallTypes.METER_READING_RESULT_CREATE_REQUEST);

        MeterReadingResultCreateRequestDomainExtension childDomainExtension = new MeterReadingResultCreateRequestDomainExtension();
        childDomainExtension.setDeviceId(message.getDeviceId());
        childDomainExtension.setLrn(message.getLrn());
        childDomainExtension.setMeterReadingDocumentId(message.getId());
        childDomainExtension.setReadingReasonCode(message.getReadingReasonCode());
        childDomainExtension.setMeterReadingTypeCode(message.getMeterReadingResultMessage().getMeterReadingTypeCode());
        childDomainExtension.setMeterReadingValue(message.getMeterReadingResultMessage().getMeterReadingResultValue());

        LocalDate localDate = message.getMeterReadingResultMessage().getMeterReadingDate().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDateTime localDateTime = message.getMeterReadingResultMessage().getMeterReadingTime().atDate(localDate);

        childDomainExtension.setMeterReadingDateTime(localDateTime.atZone(ZoneId.systemDefault()).toInstant());

        ServiceCallBuilder serviceCallBuilder = parent.newChildCall(serviceCallType)
                .extendedWith(childDomainExtension);
        sapCustomPropertySets.getDevice(message.getDeviceId()).ifPresent(serviceCallBuilder::targetObject);
        serviceCallBuilder.create();
    }

    private void sendProcessError(MeterReadingResultCreateRequestMessage message, MessageSeeds messageSeed, Object... messageSeedArgs) {
        log(LogLevel.WARNING, thesaurus.getFormat(messageSeed).format(messageSeedArgs));
        MeterReadingResultCreateConfirmationMessage confirmationMessage = MeterReadingResultCreateConfirmationMessage.builder()
                .from(message, messageSeed, webServiceActivator.getMeteringSystemId(), clock.instant(), messageSeedArgs).build();
        sendMessage(confirmationMessage);
    }

    private void sendMessage(MeterReadingResultCreateConfirmationMessage message) {
        WebServiceActivator.METER_READING_RESULT_CREATE_CONFIRMATIONS
                .forEach(service -> service.call(message));
    }

    private boolean isAnyActiveEndpoint(String name) {
        return endPointConfigurationService
                .getEndPointConfigurationsForWebService(name)
                .stream()
                .filter(EndPointConfiguration::isActive)
                .findAny().isPresent();
    }

    private boolean hasOpenMeterReadingResultCreateServiceCall(String id, String uuid) {
        return findAvailableOpenServiceCalls(ServiceCallTypes.MASTER_METER_READING_RESULT_CREATE_REQUEST)
                .stream()
                .map(serviceCall -> serviceCall.getExtension(MasterMeterReadingResultCreateRequestDomainExtension.class))
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
