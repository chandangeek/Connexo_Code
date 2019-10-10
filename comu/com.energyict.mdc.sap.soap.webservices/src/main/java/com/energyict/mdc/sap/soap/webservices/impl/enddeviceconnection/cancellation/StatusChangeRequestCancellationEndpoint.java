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
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.SAPWebServiceException;
import com.energyict.mdc.sap.soap.webservices.impl.StatusChangeRequestCancellationConfirmation;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.enddeviceconnection.ConnectionStatusChangeDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.enddeviceconnection.ConnectionStatusChangePersistenceSupport;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuscancellationrequest.SmartMeterUtilitiesConnectionStatusChangeRequestERPCancellationRequestCIn;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuscancellationrequest.SmrtMtrUtilsConncnStsChgReqERPCanclnReqMsg;

import javax.inject.Inject;
import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;

public class StatusChangeRequestCancellationEndpoint extends AbstractInboundEndPoint implements SmartMeterUtilitiesConnectionStatusChangeRequestERPCancellationRequestCIn, ApplicationSpecific {

    private final Thesaurus thesaurus;
    private final EndPointConfigurationService endPointConfigurationService;
    private final ServiceCallService serviceCallService;
    private final Clock clock;
    private final OrmService ormService;

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
        if (message.isValid()) {
            CancelledStatusChangeRequestDocument document = cancelRequestServiceCalls(message);

            StatusChangeRequestCancellationConfirmationMessage confirmationMessage =
                    StatusChangeRequestCancellationConfirmationMessage.builder()
                            .from(message.getRequestId(), document, clock.instant())
                            .build();
            sendMessage(confirmationMessage);
        } else {
            sendProcessError(message, MessageSeeds.INVALID_MESSAGE_FORMAT);
        }
    }

    private CancelledStatusChangeRequestDocument cancelRequestServiceCalls(StatusChangeRequestCancellationRequestMessage message) {
        ConnectionStatusChangeDomainExtension extension = getRequestExtensions(message.getRequestId(), message.getCategoryCode());

        if (extension == null) {
            return new CancelledStatusChangeRequestDocument(message.getRequestId(), message.getCategoryCode(), 0, 0, 0);
        }
        String documentId = extension.getId();
        int cancelledRequests = 0;
        int notCancelledRequests = 0;
        ServiceCall parent = extension.getServiceCall();
        if (!parent.getState().isOpen()) {
            return new CancelledStatusChangeRequestDocument(message.getRequestId(), message.getCategoryCode(), -1, 0, 0);
        }
        List<ServiceCall> serviceCalls = extension.getServiceCall().findChildren().stream().collect(Collectors.toList());
        for (ServiceCall serviceCall: serviceCalls) {
            try {
                if (serviceCall.getState().isOpen()) {
                    serviceCall = lock(serviceCall);
                    if (serviceCall.getState().isOpen()) {
                        serviceCall.log(com.elster.jupiter.servicecall.LogLevel.INFO, "Cancel the service call by request from SAP.");
                        if (serviceCall.getState().equals(DefaultState.CREATED)) {
                            serviceCall.requestTransition(DefaultState.PENDING);
                        }
                        if (serviceCall.canTransitionTo(DefaultState.CANCELLED)) {
                            extension.setCancelledBySap(true);
                            parent.update(extension);
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

    private ConnectionStatusChangeDomainExtension getRequestExtensions(String id, String categoryCode) {
        return ormService.getDataModel(ConnectionStatusChangePersistenceSupport.COMPONENT_NAME)
                .orElseThrow(() -> new IllegalStateException("Data model " + ConnectionStatusChangePersistenceSupport.COMPONENT_NAME + " isn't found."))
                .stream(ConnectionStatusChangeDomainExtension.class)
                .join(ServiceCall.class)
                .filter(where(ConnectionStatusChangeDomainExtension.FieldNames.ID.javaName()).isEqualTo(id))
                .filter(where(ConnectionStatusChangeDomainExtension.FieldNames.CATEGORY_CODE.javaName()).isEqualTo(categoryCode))
                .findFirst().orElse(null);

    }

    private void sendProcessError(StatusChangeRequestCancellationRequestMessage message, MessageSeeds messageSeed) {
        log(LogLevel.WARNING, thesaurus.getFormat(messageSeed).format());
        StatusChangeRequestCancellationConfirmationMessage confirmationMessage =
                StatusChangeRequestCancellationConfirmationMessage.builder()
                        .from(message, messageSeed, clock.instant())
                        .build();
        sendMessage(confirmationMessage);
    }

    private void sendMessage(StatusChangeRequestCancellationConfirmationMessage confirmationMessage) {
        WebServiceActivator.STATUS_CHANGE_REQUEST_CANCELLATION_CONFIRMATIONS
                .forEach(service -> service.call(confirmationMessage));
    }

    private ServiceCall lock(ServiceCall serviceCall) {
        return serviceCallService.lockServiceCall(serviceCall.getId())
                .orElseThrow(() -> new IllegalStateException("Service call " + serviceCall.getNumber() + " disappeared."));
    }

}