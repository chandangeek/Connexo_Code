/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.servicecall;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ami.CompletionOptions;
import com.elster.jupiter.metering.ami.EndDeviceCommand;
import com.elster.jupiter.metering.ami.HeadEndInterface;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallBuilder;
import com.elster.jupiter.servicecall.ServiceCallFilter;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.devicecreation.UtilitiesDeviceCreateConfirmationMessage;
import com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.devicecreation.UtilitiesDeviceCreateMessage;
import com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.devicecreation.UtilitiesDeviceCreateRequestMessage;
import com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.registercreation.UtilitiesDeviceRegisterCreateConfirmationMessage;
import com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.registercreation.UtilitiesDeviceRegisterCreateMessage;
import com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.registercreation.UtilitiesDeviceRegisterCreateRequestMessage;
import com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.registercreation.UtilitiesDeviceRegisterMessage;
import com.energyict.mdc.sap.soap.webservices.impl.enddeviceconnection.CategoryCode;
import com.energyict.mdc.sap.soap.webservices.impl.enddeviceconnection.ProcessingResultCode;
import com.energyict.mdc.sap.soap.webservices.impl.enddeviceconnection.StatusChangeRequestCreateConfirmationMessage;
import com.energyict.mdc.sap.soap.webservices.impl.enddeviceconnection.StatusChangeRequestCreateMessage;
import com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument.MeterReadingDocumentCreateMessage;
import com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument.MeterReadingDocumentCreateRequestMessage;
import com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument.MeterReadingDocumentRequestConfirmationMessage;
import com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument.MeterReadingDocumentResultCreateConfirmationRequestMessage;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.MasterUtilitiesDeviceCreateRequestDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.MasterUtilitiesDeviceRegisterCreateRequestDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.SubMasterUtilitiesDeviceRegisterCreateRequestDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.UtilitiesDeviceCreateRequestDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.UtilitiesDeviceRegisterCreateRequestDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.enddeviceconnection.ConnectionStatusChangeDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreadingdocument.MasterMeterReadingDocumentCreateRequestDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreadingdocument.MasterMeterReadingDocumentCreateResultDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreadingdocument.MeterReadingDocumentCreateRequestDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.task.ConnectionStatusChangeMessageHandlerFactory;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.security.Principal;
import java.time.Clock;
import java.util.Objects;
import java.util.Optional;

import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.APPLICATION_NAME;

public class ServiceCallCommands {

    private final Clock clock;
    private final MessageService messageService;
    private final MeteringService meteringService;
    private final SAPCustomPropertySets sapCustomPropertySets;
    private final ServiceCallService serviceCallService;
    private final Thesaurus thesaurus;
    private final ThreadPrincipalService threadPrincipalService;
    private final TransactionService transactionService;
    private final UserService userService;

    @Inject
    public ServiceCallCommands(Clock clock, MessageService messageService, MeteringService meteringService,
                               SAPCustomPropertySets sapCustomPropertySets, ServiceCallService serviceCallService,
                               Thesaurus thesaurus, ThreadPrincipalService threadPrincipalService,
                               TransactionService transactionService, UserService userService) {
        this.clock = clock;
        this.messageService = messageService;
        this.meteringService = meteringService;
        this.sapCustomPropertySets = sapCustomPropertySets;
        this.serviceCallService = serviceCallService;
        this.thesaurus = thesaurus;
        this.threadPrincipalService = threadPrincipalService;
        this.transactionService = transactionService;
        this.userService = userService;
    }

    public void createServiceCallAndTransition(StatusChangeRequestCreateMessage message) {
        setSecurityContext();
        if (message.isValid()) {
            if (!hasConnectionStatusChangeServiceCall(message.getId())) {
                getServiceCallType(ServiceCallTypes.CONNECTION_STATUS_CHANGE).ifPresent(serviceCallType -> {
                    try (TransactionContext context = transactionService.getContext()) {
                        createServiceCall(serviceCallType, message);
                        context.commit();
                    }
                });
            } else {
                sendProcessError(MessageSeeds.MESSAGE_ALREADY_EXISTS, message);
            }
        } else {
            sendProcessError(MessageSeeds.INVALID_MESSAGE_FORMAT, message);
        }
    }

    public void createServiceCallAndTransition(MeterReadingDocumentCreateRequestMessage message) {
        setSecurityContext();
        if (message.isValid()) {
            if (hasMeterReadingRequestServiceCall(message.getId())) {
                sendProcessError(message, MessageSeeds.MESSAGE_ALREADY_EXISTS);
            } else {
                getServiceCallType(ServiceCallTypes.MASTER_METER_READING_DOCUMENT_CREATE_REQUEST).ifPresent(serviceCallType -> {
                    try (TransactionContext context = transactionService.getContext()) {
                        sendMessage(createServiceCall(serviceCallType, message), message.isBulk());
                        context.commit();
                    }
                });
            }
        } else {
            sendProcessError(message, MessageSeeds.INVALID_MESSAGE_FORMAT);
        }
    }

    public void createServiceCallAndTransition(UtilitiesDeviceCreateRequestMessage message) {
        setSecurityContext();
        if (message.isValid()) {
            if (hasUtilDeviceRequestServiceCall(message.getRequestID())) {
                sendProcessError(message, MessageSeeds.MESSAGE_ALREADY_EXISTS);
            } else {
                getServiceCallType(ServiceCallTypes.MASTER_UTILITIES_DEVICE_CREATE_REQUEST).ifPresent(serviceCallType -> {
                    try (TransactionContext context = transactionService.getContext()) {
                        createServiceCall(serviceCallType, message);
                        context.commit();
                    }
                });
            }
        } else {
            sendProcessError(message, MessageSeeds.INVALID_MESSAGE_FORMAT);
        }
    }

    public void createServiceCallAndTransition(UtilitiesDeviceRegisterCreateRequestMessage message) {
        setSecurityContext();
        if (message.isValid()) {
            if(hasUtilDeviceRegisterRequestServiceCall(message.getRequestID())){
                sendProcessError(message, MessageSeeds.MESSAGE_ALREADY_EXISTS);
            }else {
                getServiceCallType(ServiceCallTypes.MASTER_UTILITIES_DEVICE_REGISTER_CREATE_REQUEST).ifPresent(serviceCallType -> {
                    try (TransactionContext context = transactionService.getContext()) {
                        createServiceCall(serviceCallType, message);
                        context.commit();
                    }
                });
            }
        } else {
            sendProcessError(message, MessageSeeds.INVALID_MESSAGE_FORMAT);
        }
    }

    public void updateServiceCallTransition(MeterReadingDocumentResultCreateConfirmationRequestMessage message) {
        setSecurityContext();
        ServiceCallFilter filter = new ServiceCallFilter();
        filter.types.add(ServiceCallTypes.MASTER_METER_READING_DOCUMENT_CREATE_RESULT.getTypeName());
        filter.states.add(DefaultState.WAITING.name());
        try (TransactionContext context = transactionService.getContext()) {
            serviceCallService.getServiceCallFinder(filter)
                    .stream()
                    .forEach(serviceCall -> {
                        MasterMeterReadingDocumentCreateResultDomainExtension extension =
                                serviceCall.getExtension(MasterMeterReadingDocumentCreateResultDomainExtension.class).get();
                        if (extension.getRequestUUID().equals(message.getUuid())) {
                            if (message.getProcessingResultCode().equals(ProcessingResultCode.FAILED.getCode())) {
                                serviceCall.requestTransition(DefaultState.ONGOING);
                                serviceCall.requestTransition(DefaultState.FAILED);
                            } else {
                                serviceCall.requestTransition(DefaultState.ONGOING);
                                serviceCall.requestTransition(DefaultState.SUCCESSFUL);
                            }
                        }
                    });
            context.commit();
        }
    }

    private void createAndSendCommand(String id, EndDevice endDevice, ServiceCall serviceCall,
                                      HeadEndInterface headEndInterface, StatusChangeRequestCreateMessage message) {
        EndDeviceCommand deviceCommand;

        switch (CategoryCode.findByCode(message.getCategoryCode())) {
            case CONNECT:
                deviceCommand = headEndInterface.getCommandFactory().createConnectCommand(endDevice, null);
                break;
            case DISCONNECT:
                deviceCommand = headEndInterface.getCommandFactory().createDisconnectCommand(endDevice, null);
                break;
            case UNKNOWN:
            default:
                deviceCommand = null;
        }

        if (deviceCommand == null) {
            sendProcessErrorWithStatus(MessageSeeds.INVALID_CATEGORY_CODE, message, id);
        } else {
            CompletionOptions completionOptions = headEndInterface.sendCommand(deviceCommand,
                    message.getPlannedProcessingDateTime()
                            .isBefore(clock.instant()) ? clock.instant().plusSeconds(60) : message.getPlannedProcessingDateTime(),
                    serviceCall);
            messageService.getDestinationSpec(ConnectionStatusChangeMessageHandlerFactory.DESTINATION)
                    .ifPresent(destinationSpec -> completionOptions
                            .whenFinishedSendCompletionMessageWith(Long.toString(serviceCall.getId()),
                                    destinationSpec));
        }
    }

    private void sendCommand(ServiceCall serviceCall, String deviceId, StatusChangeRequestCreateMessage message) {
        serviceCall.log(LogLevel.INFO, "Handling breaker operations for device with SAP id " + deviceId);
        Optional<Device> device = sapCustomPropertySets.getDevice(deviceId);
        Optional<EndDevice> endDevice = device.isPresent()
                ? meteringService.findEndDeviceByMRID(device.get().getmRID())
                : Optional.empty();
        endDevice.ifPresent(ed -> {
            Optional<HeadEndInterface> headEndInterface = ed.getHeadEndInterface();
            Optional<ConnectionStatusChangeDomainExtension> connectionStatusChangeDomainExtension =
                    serviceCall.getExtension(ConnectionStatusChangeDomainExtension.class);

            connectionStatusChangeDomainExtension
                    .ifPresent(de -> headEndInterface
                            .ifPresent(hei -> {
                                try {
                                    createAndSendCommand(deviceId, ed, serviceCall, hei, message);
                                } catch (LocalizedException le) {
                                    sendProcessError(le.getErrorCode(), le.getLocalizedMessage(), message);
                                }
                            }));

            if (!headEndInterface.isPresent()) {
                sendProcessErrorWithStatus(MessageSeeds.NO_HEAD_END_INTERFACE_FOUND, message, deviceId);
            } else if (!connectionStatusChangeDomainExtension.isPresent()) {
                sendProcessErrorWithStatus(MessageSeeds.COULD_NOT_FIND_DOMAIN_EXTENSION, message, deviceId);
            }
        });
        if (!endDevice.isPresent()) {
            sendProcessErrorWithStatus(MessageSeeds.NO_DEVICE_FOUND_BY_SAP_ID, message, deviceId);
        }
    }

    private void createChildServiceCall(ServiceCall parent, MeterReadingDocumentCreateMessage message) {
        ServiceCallType serviceCallType = getServiceCallTypeOrThrowException(ServiceCallTypes.METER_READING_DOCUMENT_CREATE_REQUEST);

        MeterReadingDocumentCreateRequestDomainExtension childDomainExtension = new MeterReadingDocumentCreateRequestDomainExtension();
        childDomainExtension.setParentServiceCallId(BigDecimal.valueOf(parent.getId()));
        childDomainExtension.setMeterReadingDocumentId(message.getId());
        childDomainExtension.setDeviceId(message.getDeviceId());
        childDomainExtension.setLrn(message.getLrn());
        childDomainExtension.setReadingReasonCode(message.getReadingReasonCode());
        childDomainExtension.setScheduledReadingDate(message.getScheduledMeterReadingDate());

        ServiceCallBuilder serviceCallBuilder = parent.newChildCall(serviceCallType)
                .extendedWith(childDomainExtension);
        sapCustomPropertySets.getDevice(message.getDeviceId()).ifPresent(serviceCallBuilder::targetObject);
        serviceCallBuilder.create();
    }

    private void createChildServiceCall(ServiceCall parent, UtilitiesDeviceCreateMessage message) {
        ServiceCallType serviceCallType = getServiceCallTypeOrThrowException(ServiceCallTypes.UTILITIES_DEVICE_CREATE_REQUEST);

        UtilitiesDeviceCreateRequestDomainExtension childDomainExtension = new UtilitiesDeviceCreateRequestDomainExtension();
        childDomainExtension.setParentServiceCallId(BigDecimal.valueOf(parent.getId()));
        childDomainExtension.setSerialId(message.getSerialId());
        childDomainExtension.setDeviceId(message.getDeviceId());


        ServiceCallBuilder serviceCallBuilder = parent.newChildCall(serviceCallType)
                .extendedWith(childDomainExtension);
        sapCustomPropertySets.getDevice(message.getDeviceId()).ifPresent(serviceCallBuilder::targetObject);
        serviceCallBuilder.create();
    }

    private void createChildServiceCall(ServiceCall parent, UtilitiesDeviceRegisterCreateMessage message) {
        ServiceCallType serviceCallType = getServiceCallTypeOrThrowException(ServiceCallTypes.SUB_MASTER_UTILITIES_DEVICE_REGISTER_CREATE_REQUEST);

        SubMasterUtilitiesDeviceRegisterCreateRequestDomainExtension childDomainExtension = new SubMasterUtilitiesDeviceRegisterCreateRequestDomainExtension();
        childDomainExtension.setParentServiceCallId(BigDecimal.valueOf(parent.getId()));
        childDomainExtension.setDeviceId(message.getDeviceId());

        ServiceCallBuilder serviceCallBuilder = parent.newChildCall(serviceCallType)
                .extendedWith(childDomainExtension);
        sapCustomPropertySets.getDevice(message.getDeviceId()).ifPresent(serviceCallBuilder::targetObject);
        ServiceCall serviceCall = serviceCallBuilder.create();

        message.getUtilitiesDeviceRegisterMessage()
                .forEach(bodyMessage -> {
                    if (bodyMessage.isValid()) {
                        createSecondChildServiceCall(serviceCall, bodyMessage, message.getDeviceId());
                    }
                });
    }

    private void createSecondChildServiceCall(ServiceCall parent, UtilitiesDeviceRegisterMessage bodyMessage, String deviceId) {
        ServiceCallType serviceCallType = getServiceCallTypeOrThrowException(ServiceCallTypes.UTILITIES_DEVICE_REGISTER_CREATE_REQUEST);

        UtilitiesDeviceRegisterCreateRequestDomainExtension childDomainExtension = new UtilitiesDeviceRegisterCreateRequestDomainExtension();
        childDomainExtension.setDeviceId(deviceId);
        childDomainExtension.setLrn(bodyMessage.getLrn());
        childDomainExtension.setRegisterId(bodyMessage.getOBIS());
        childDomainExtension.setStartDate(bodyMessage.getStartDate());
        childDomainExtension.setEndDate(bodyMessage.getEndDate());

        ServiceCallBuilder serviceCallBuilder = parent.newChildCall(serviceCallType)
                .extendedWith(childDomainExtension);
        sapCustomPropertySets.getDevice(bodyMessage.getOBIS()).ifPresent(serviceCallBuilder::targetObject);
        serviceCallBuilder.create();
    }

    private void createServiceCall(ServiceCallType serviceCallType, StatusChangeRequestCreateMessage message) {
        ConnectionStatusChangeDomainExtension connectionStatusChangeDomainExtension =
                new ConnectionStatusChangeDomainExtension();
        connectionStatusChangeDomainExtension.setId(message.getId());
        connectionStatusChangeDomainExtension.setCategoryCode(message.getCategoryCode());
        connectionStatusChangeDomainExtension.setReasonCode(message.getUtilitiesServiceDisconnectionReasonCode());
        connectionStatusChangeDomainExtension.setProcessDate(message.getPlannedProcessingDateTime());

        ServiceCall serviceCall = serviceCallType.newServiceCall()
                .origin(APPLICATION_NAME)
                .extendedWith(connectionStatusChangeDomainExtension)
                .create();

        serviceCall.requestTransition(DefaultState.PENDING);
        serviceCall.requestTransition(DefaultState.ONGOING);

        message.getDeviceConnectionStatus()
                .forEach((key, value) -> sendCommand(serviceCall, key, message));

        serviceCall.requestTransition(DefaultState.WAITING);
    }

    private MeterReadingDocumentRequestConfirmationMessage createServiceCall(ServiceCallType serviceCallType, MeterReadingDocumentCreateRequestMessage requestMessage) {
        MasterMeterReadingDocumentCreateRequestDomainExtension meterReadingDocumentDomainExtension =
                new MasterMeterReadingDocumentCreateRequestDomainExtension();
        meterReadingDocumentDomainExtension.setRequestID(requestMessage.getId());
        meterReadingDocumentDomainExtension.setBulk(requestMessage.isBulk());

        ServiceCall serviceCall = serviceCallType.newServiceCall()
                .origin(APPLICATION_NAME)
                .extendedWith(meterReadingDocumentDomainExtension)
                .create();

        requestMessage.getMeterReadingDocumentCreateMessages()
                .forEach(bodyMessage -> {
                    if (bodyMessage.isValid() && bodyMessage.isReasonCodeSupported(requestMessage.isBulk())) {
                        createChildServiceCall(serviceCall, bodyMessage);
                    }
                });

        if (serviceCall.findChildren().stream().count() > 0) {
            serviceCall.requestTransition(DefaultState.PENDING);
        } else {
            serviceCall.requestTransition(DefaultState.REJECTED);
        }

        return MeterReadingDocumentRequestConfirmationMessage
                .builder()
                .from(requestMessage)
                .build();
    }

    private void createServiceCall(ServiceCallType serviceCallType, UtilitiesDeviceCreateRequestMessage requestMessage) {
        MasterUtilitiesDeviceCreateRequestDomainExtension masterUtilitiesDeviceCreateRequestDomainExtension =
                new MasterUtilitiesDeviceCreateRequestDomainExtension();
        masterUtilitiesDeviceCreateRequestDomainExtension.setRequestID(requestMessage.getRequestID());

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
        if (serviceCall.findChildren().stream().count() > 0) {
            serviceCall.requestTransition(DefaultState.PENDING);
        } else {
            sendProcessError(requestMessage, MessageSeeds.INVALID_MESSAGE_FORMAT);
        }
    }

    private void createServiceCall(ServiceCallType serviceCallType, UtilitiesDeviceRegisterCreateRequestMessage requestMessage) {
        MasterUtilitiesDeviceRegisterCreateRequestDomainExtension masterUtilitiesDeviceRegisterCreateRequestDomainExtension =
                new MasterUtilitiesDeviceRegisterCreateRequestDomainExtension();
        masterUtilitiesDeviceRegisterCreateRequestDomainExtension.setRequestID(requestMessage.getRequestID());
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

        if (serviceCall.findChildren().stream().count() > 0) {
            serviceCall.requestTransition(DefaultState.PENDING);
        } else {
            sendProcessError(requestMessage, MessageSeeds.INVALID_MESSAGE_FORMAT);
        }
    }

    private Optional<ServiceCallType> getServiceCallType(ServiceCallTypes serviceCallType) {
        return serviceCallService.findServiceCallType(serviceCallType.getTypeName(), serviceCallType.getTypeVersion());
    }

    private Finder<ServiceCall> findAvailableServiceCalls(ServiceCallTypes serviceCallType) {
        ServiceCallFilter filter = new ServiceCallFilter();
        filter.types.add(serviceCallType.getTypeName());
        return serviceCallService.getServiceCallFinder(filter);
    }

    private boolean hasConnectionStatusChangeServiceCall(String id) {
        return findAvailableServiceCalls(ServiceCallTypes.CONNECTION_STATUS_CHANGE)
                .stream()
                .map(serviceCall -> serviceCall.getExtension(ConnectionStatusChangeDomainExtension.class))
                .filter(Objects::nonNull)
                .map(Optional::get)
                .anyMatch(domainExtension -> domainExtension.getId().equals(id));
    }

    private boolean hasMeterReadingRequestServiceCall(String id) {
        return findAvailableServiceCalls(ServiceCallTypes.MASTER_METER_READING_DOCUMENT_CREATE_REQUEST)
                .stream()
                .map(serviceCall -> serviceCall.getExtension(MasterMeterReadingDocumentCreateRequestDomainExtension.class))
                .filter(Objects::nonNull)
                .map(Optional::get)
                .anyMatch(domainExtension -> domainExtension.getRequestID().equals(id));
    }

    private boolean hasUtilDeviceRequestServiceCall(String id) {
        return findAvailableServiceCalls(ServiceCallTypes.MASTER_UTILITIES_DEVICE_CREATE_REQUEST)
                .stream()
                .map(serviceCall -> serviceCall.getExtension(MasterUtilitiesDeviceCreateRequestDomainExtension.class))
                .filter(Objects::nonNull)
                .map(Optional::get)
                .anyMatch(domainExtension -> domainExtension.getRequestID().equals(id));
    }

    private boolean hasUtilDeviceRegisterRequestServiceCall(String id) {
        return findAvailableServiceCalls(ServiceCallTypes.MASTER_UTILITIES_DEVICE_REGISTER_CREATE_REQUEST)
                .stream()
                .map(serviceCall -> serviceCall.getExtension(MasterUtilitiesDeviceRegisterCreateRequestDomainExtension.class))
                .filter(Objects::nonNull)
                .map(Optional::get)
                .anyMatch(domainExtension -> domainExtension.getRequestID().equals(id));
    }

    private void sendMessage(StatusChangeRequestCreateConfirmationMessage statusChangeRequestCreateConfirmationMessage) {
        WebServiceActivator.STATUS_CHANGE_REQUEST_CREATE_CONFIRMATIONS
                .forEach(service -> service.call(statusChangeRequestCreateConfirmationMessage));
    }

    private void sendMessage(MeterReadingDocumentRequestConfirmationMessage confirmationMessage, boolean bulk) {
        if (bulk) {
            WebServiceActivator.METER_READING_DOCUMENT_BULK_REQUEST_CONFIRMATIONS
                    .forEach(service -> service.call(confirmationMessage));
        } else {
            WebServiceActivator.METER_READING_DOCUMENT_REQUEST_CONFIRMATIONS
                    .forEach(service -> service.call(confirmationMessage));
        }
    }

    private void sendMessage(UtilitiesDeviceCreateConfirmationMessage confirmationMessage) {
        WebServiceActivator.UTILITIES_DEVICE_BULK_CREATE_CONFIRMATION
                .forEach(service -> service.call(confirmationMessage));
    }

    private void sendMessage(UtilitiesDeviceRegisterCreateConfirmationMessage confirmationMessage, boolean bulk) {
        if (bulk) {
            WebServiceActivator.UTILITIES_DEVICE_REGISTER_BULK_CREATE_CONFIRMATION
                    .forEach(service -> service.call(confirmationMessage));
        } else {
            WebServiceActivator.UTILITIES_DEVICE_REGISTER_BULK_CREATE_CONFIRMATION
                    .forEach(service -> service.call(confirmationMessage));
        }
    }

    private void sendProcessError(MessageSeeds messageSeed, StatusChangeRequestCreateMessage message) {
        StatusChangeRequestCreateConfirmationMessage confirmationMessage =
                StatusChangeRequestCreateConfirmationMessage.builder()
                        .from(message, messageSeed.code(), messageSeed.translate(thesaurus))
                        .build();
        sendMessage(confirmationMessage);
    }

    private void sendProcessError(MeterReadingDocumentCreateRequestMessage message, MessageSeeds messageSeed) {
        MeterReadingDocumentRequestConfirmationMessage confirmationMessage =
                MeterReadingDocumentRequestConfirmationMessage.builder()
                        .from(message, messageSeed)
                        .build();
        sendMessage(confirmationMessage, message.isBulk());
    }

    private void sendProcessError(String exceptionCode, String exceptionInfo, StatusChangeRequestCreateMessage message) {
        StatusChangeRequestCreateConfirmationMessage confirmationMessage =
                StatusChangeRequestCreateConfirmationMessage.builder()
                        .from(message, exceptionCode, exceptionInfo)
                        .build();
        sendMessage(confirmationMessage);
    }

    private void sendProcessError(UtilitiesDeviceCreateRequestMessage message, MessageSeeds messageSeed) {
        UtilitiesDeviceCreateConfirmationMessage confirmationMessage =
                UtilitiesDeviceCreateConfirmationMessage.builder()
                        .from(message, messageSeed, clock.instant())
                        .build();
        sendMessage(confirmationMessage);
    }

    private void sendProcessError(UtilitiesDeviceRegisterCreateRequestMessage message, MessageSeeds messageSeed) {
        UtilitiesDeviceRegisterCreateConfirmationMessage confirmationMessage = null;
        confirmationMessage =
                UtilitiesDeviceRegisterCreateConfirmationMessage.builder()
                        .from(message, messageSeed, clock.instant())
                        .build();
        sendMessage(confirmationMessage, message.isBulk());
    }

    private void sendProcessErrorWithStatus(MessageSeeds messageSeed, StatusChangeRequestCreateMessage message,
                                            String deviceId) {
        StatusChangeRequestCreateConfirmationMessage confirmationMessage =
                StatusChangeRequestCreateConfirmationMessage.builder()
                        .from(message, messageSeed.code(), messageSeed.translate(thesaurus, deviceId))
                        .withStatus(deviceId, ProcessingResultCode.FAILED, clock.instant())
                        .build();
        sendMessage(confirmationMessage);
    }

    private void setSecurityContext() {
        Principal principal = threadPrincipalService.getPrincipal();
        if (principal == null) {
            try (TransactionContext context = transactionService.getContext()) {
                userService.findUser(WebServiceActivator.BATCH_EXECUTOR_USER_NAME, userService.getRealm()).ifPresent(user -> {
                    threadPrincipalService.set(user);
                    context.commit();
                });
            }
        }
    }

    public ServiceCallType getServiceCallTypeOrThrowException(ServiceCallTypes serviceCallType) {
        return serviceCallService.findServiceCallType(serviceCallType.getTypeName(), serviceCallType.getTypeVersion())
                .orElseThrow(() -> new IllegalStateException(thesaurus.getFormat(MessageSeeds.COULD_NOT_FIND_SERVICE_CALL_TYPE)
                        .format(serviceCallType.getTypeName(), serviceCallType.getTypeVersion())));
    }
}