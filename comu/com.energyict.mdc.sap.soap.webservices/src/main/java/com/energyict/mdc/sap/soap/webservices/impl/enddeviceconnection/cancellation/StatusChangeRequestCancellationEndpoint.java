/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.enddeviceconnection.cancellation;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractInboundEndPoint;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.LogLevel;
import com.elster.jupiter.util.exception.BaseException;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.SAPWebServiceException;
import com.energyict.mdc.sap.soap.webservices.impl.StatusChangeRequestCancellationConfirmation;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallHelper;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.enddeviceconnection.ConnectionStatusChangeDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.enddeviceconnection.ConnectionStatusChangePersistenceSupport;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuscancellationconfirmation.SmrtMtrUtilsConncnStsChgReqERPCanclnConfMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuscancellationrequest.SmartMeterUtilitiesConnectionStatusChangeRequestERPCancellationRequestCIn;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuscancellationrequest.SmrtMtrUtilsConncnStsChgReqERPCanclnReqMsg;

import javax.inject.Inject;
import java.security.Principal;
import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

import static com.elster.jupiter.util.conditions.Where.where;

public class StatusChangeRequestCancellationEndpoint extends AbstractInboundEndPoint implements SmartMeterUtilitiesConnectionStatusChangeRequestERPCancellationRequestCIn, ApplicationSpecific {

    private final Thesaurus thesaurus;
    private final EndPointConfigurationService endPointConfigurationService;
    private final ServiceCallService serviceCallService;
    private final Clock clock;
    private final OrmService ormService;
    private static final CancellationConfirmationMessageFactory MESSAGE_FACTORY = new CancellationConfirmationMessageFactory();

    @Inject
    StatusChangeRequestCancellationEndpoint(EndPointConfigurationService endPointConfigurationService,
                                            ServiceCallService serviceCallService,
                                            Clock clock,
                                            OrmService ormService,
                                            Thesaurus thesaurus) {
        this.endPointConfigurationService = endPointConfigurationService;
        this.serviceCallService = serviceCallService;
        this.clock = clock;
        this.ormService = ormService;
        this.thesaurus = thesaurus;
    }

    @Override
    public void smartMeterUtilitiesConnectionStatusChangeRequestERPCancellationRequestCIn(SmrtMtrUtilsConncnStsChgReqERPCanclnReqMsg request) {
        runInTransactionWithOccurrence(() -> {
            if (!isAnyActiveEndpoint(StatusChangeRequestCancellationConfirmation.NAME)) {
                throw new SAPWebServiceException(thesaurus, MessageSeeds.NO_REQUIRED_OUTBOUND_END_POINT,
                        StatusChangeRequestCancellationConfirmation.NAME);
            }

            Optional.ofNullable(request)
                    .ifPresent(requestMessage -> handleMessage(StatusChangeRequestCancellationRequestMessage.builder()
                            .from(requestMessage)
                            .build()));
            return null;
        });
    }

    @Override
    public String getApplication() {
        return WebServiceApplicationName.MULTISENSE.getName();
    }

    private boolean isAnyActiveEndpoint(String name) {
        return endPointConfigurationService
                .getEndPointConfigurationsForWebService(name)
                .stream()
                .anyMatch(EndPointConfiguration::isActive);
    }

    void handleMessage(StatusChangeRequestCancellationRequestMessage message) {
        Principal principal = threadPrincipalService.getPrincipal();
        CompletableFuture.runAsync(() -> {
            threadPrincipalService.set(principal);
            transactionService.run(() -> {
                try {
                    if (message.isValid()) {
                        CancelledStatusChangeRequestDocument document = cancelRequestServiceCalls(message);

                        sendMessage(MESSAGE_FACTORY.createMessage(message.getRequestId(), message.getUuid(), document, clock.instant()));
                    } else {
                        sendProcessError(message, MessageSeeds.INVALID_MESSAGE_FORMAT);
                    }
                } catch (BaseException be) {
                    sendProcessError(message, be.getMessageSeed().getDefaultFormat(), be.getMessageSeed().getNumber());
                } catch (Exception e) {
                    sendProcessError(message, MessageSeeds.UNEXPECTED_EXCEPTION.getDefaultFormat(e.getLocalizedMessage()), MessageSeeds.UNEXPECTED_EXCEPTION.getNumber());
                }
            });
        }, Executors.newSingleThreadExecutor());
    }

    private CancelledStatusChangeRequestDocument cancelRequestServiceCalls(StatusChangeRequestCancellationRequestMessage message) {
        Optional<ConnectionStatusChangeDomainExtension> extension = getRequestExtension(message.getRequestId(), message.getCategoryCode());

        if (!extension.isPresent()) {
            return new CancelledStatusChangeRequestDocument(message.getRequestId(), message.getCategoryCode(), 0, 0, 0);
        }
        String documentId = extension.get().getId();
        int cancelledRequests = 0;
        int notCancelledRequests = 0;
        ServiceCall parent = extension.get().getServiceCall();
        List<ServiceCall> serviceCalls = ServiceCallHelper.findChildren(parent);
        if (!parent.getState().isOpen()) {
            // send already processed message
            return new CancelledStatusChangeRequestDocument(message.getRequestId(), message.getCategoryCode(), serviceCalls.size(), 0, 0);
        }
        for (ServiceCall serviceCall: serviceCalls) {
            try {
                if (serviceCall.getState().isOpen()) {
                    serviceCall = lock(serviceCall);
                    if (serviceCall.getState().isOpen()) {
                        serviceCall.log(com.elster.jupiter.servicecall.LogLevel.INFO, "Cancelling the service call by request from SAP.");
                        if (serviceCall.canTransitionTo(DefaultState.CANCELLED)) {
                            extension.get().setCancelledBySap(true);
                            parent.update(extension.get());
                            serviceCall.requestTransition(DefaultState.CANCELLED);
                            cancelledRequests++;
                        } else {
                            notCancelledRequests++;
                        }
                    } else {
                        notCancelledRequests++;
                    }
                } else {
                    notCancelledRequests++;
                }
            } catch (Exception ex) {
                serviceCall.log("Failed to cancel the service call by request from SAP.", ex);
                notCancelledRequests++;
            }
        }
        return new CancelledStatusChangeRequestDocument(documentId, message.getCategoryCode(), serviceCalls.size(), cancelledRequests, notCancelledRequests);
    }

    private Optional<ConnectionStatusChangeDomainExtension> getRequestExtension(String id, String categoryCode) {
        return ormService.getDataModel(ConnectionStatusChangePersistenceSupport.COMPONENT_NAME)
                .orElseThrow(() -> new IllegalStateException("Data model " + ConnectionStatusChangePersistenceSupport.COMPONENT_NAME + " isn't found."))
                .stream(ConnectionStatusChangeDomainExtension.class)
                .join(ServiceCall.class)
                .filter(where(ConnectionStatusChangeDomainExtension.FieldNames.ID.javaName()).isEqualTo(id))
                .filter(where(ConnectionStatusChangeDomainExtension.FieldNames.CATEGORY_CODE.javaName()).isEqualTo(categoryCode))
                .findFirst();

    }

    private void sendProcessError(StatusChangeRequestCancellationRequestMessage message, MessageSeeds messageSeed) {
        log(LogLevel.SEVERE, thesaurus.getSimpleFormat(messageSeed).format());
        sendMessage(MESSAGE_FACTORY.createFailedMessage(message, messageSeed, clock.instant()));
    }

    private void sendProcessError(StatusChangeRequestCancellationRequestMessage message, String msg, int number) {
        log(LogLevel.SEVERE, msg);
        sendMessage(MESSAGE_FACTORY.createFailedMessage(message, msg, number, clock.instant()));
    }

    private void sendMessage(SmrtMtrUtilsConncnStsChgReqERPCanclnConfMsg confirmationMessage) {
        WebServiceActivator.STATUS_CHANGE_REQUEST_CANCELLATION_CONFIRMATIONS
                .forEach(service -> service.call(confirmationMessage));
    }

    private ServiceCall lock(ServiceCall serviceCall) {
        return serviceCallService.lockServiceCall(serviceCall.getId())
                .orElseThrow(() -> new IllegalStateException("Service call " + serviceCall.getNumber() + " disappeared."));
    }

}