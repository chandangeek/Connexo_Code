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
import com.elster.jupiter.util.streams.Functions;
import com.energyict.mdc.sap.soap.webservices.SapAttributeNames;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.SAPWebServiceException;
import com.energyict.mdc.sap.soap.webservices.impl.StatusChangeRequestCancellationConfirmation;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallHelper;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.enddeviceconnection.ConnectionStatusChangeDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.enddeviceconnection.ConnectionStatusChangePersistenceSupport;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuscancellationconfirmation.SmrtMtrUtilsConncnStsChgReqERPCanclnConfMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuscancellationrequest.SmartMeterUtilitiesConnectionStatusChangeRequestERPCancellationRequestCIn;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuscancellationrequest.SmrtMtrUtilsConncnStsChgReqERPCanclnReqDvceConncnSts;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuscancellationrequest.SmrtMtrUtilsConncnStsChgReqERPCanclnReqMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuscancellationrequest.SmrtMtrUtilsConncnStsChgReqERPCanclnReqUtilsConncnStsChgReq;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuscancellationrequest.UtilitiesDeviceID;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import javax.inject.Inject;
import java.time.Clock;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.elster.jupiter.util.conditions.Where.where;

public class StatusChangeRequestCancellationEndpoint extends AbstractInboundEndPoint implements SmartMeterUtilitiesConnectionStatusChangeRequestERPCancellationRequestCIn, ApplicationSpecific {

    private final Thesaurus thesaurus;
    private final EndPointConfigurationService endPointConfigurationService;
    private final ServiceCallService serviceCallService;
    private final Clock clock;
    private final OrmService ormService;
    private final WebServiceActivator webServiceActivator;
    private static final CancellationConfirmationMessageFactory MESSAGE_FACTORY = new CancellationConfirmationMessageFactory();

    @Inject
    StatusChangeRequestCancellationEndpoint(EndPointConfigurationService endPointConfigurationService,
                                            ServiceCallService serviceCallService,
                                            Clock clock,
                                            OrmService ormService,
                                            Thesaurus thesaurus,
                                            WebServiceActivator webServiceActivator) {
        this.endPointConfigurationService = endPointConfigurationService;
        this.serviceCallService = serviceCallService;
        this.clock = clock;
        this.ormService = ormService;
        this.thesaurus = thesaurus;
        this.webServiceActivator = webServiceActivator;
    }

    @Override
    public void smartMeterUtilitiesConnectionStatusChangeRequestERPCancellationRequestCIn(SmrtMtrUtilsConncnStsChgReqERPCanclnReqMsg request) {
        runInTransactionWithOccurrence(() -> {
            Optional.ofNullable(request)
                    .ifPresent(requestMessage -> {
                        SetMultimap<String, String> values = HashMultimap.create();
                        getDeviceConnectionStatuses(requestMessage.getUtilitiesConnectionStatusChangeRequest())
                                .stream()
                                .map(StatusChangeRequestCancellationEndpoint::getDeviceId)
                                .flatMap(Functions.asStream())
                                .forEach(value -> values.put(SapAttributeNames.SAP_UTILITIES_DEVICE_ID.getAttributeName(), value));
                        saveRelatedAttributes(values);

                        if (!isAnyActiveEndpoint(StatusChangeRequestCancellationConfirmation.NAME)) {
                            throw new SAPWebServiceException(thesaurus, MessageSeeds.NO_REQUIRED_OUTBOUND_END_POINT,
                                    StatusChangeRequestCancellationConfirmation.NAME);
                        }
                        handleMessage(StatusChangeRequestCancellationRequestMessage.builder(thesaurus)
                                .from(requestMessage)
                                .build());
                    });
            return null;
        });
    }

    private static List<SmrtMtrUtilsConncnStsChgReqERPCanclnReqDvceConncnSts> getDeviceConnectionStatuses(SmrtMtrUtilsConncnStsChgReqERPCanclnReqUtilsConncnStsChgReq request) {
        return Optional.ofNullable(request)
                .map(SmrtMtrUtilsConncnStsChgReqERPCanclnReqUtilsConncnStsChgReq::getDeviceConnectionStatus)
                .orElse(Collections.emptyList());
    }

    private static Optional<String> getDeviceId(SmrtMtrUtilsConncnStsChgReqERPCanclnReqDvceConncnSts status) {
        return Optional.ofNullable(status)
                .map(SmrtMtrUtilsConncnStsChgReqERPCanclnReqDvceConncnSts::getUtilitiesDeviceID)
                .map(UtilitiesDeviceID::getValue);
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
        try {
            if (message.isValid()) {
                CancelledStatusChangeRequestDocument document = cancelRequestServiceCalls(message);
                sendMessage(MESSAGE_FACTORY.createMessage(message.getRequestId(), message.getUuid(), document, webServiceActivator.getMeteringSystemId(), clock.instant()));
            } else {
                sendProcessError(message, MessageSeeds.INVALID_MESSAGE_FORMAT, message.getMissingFields());
            }
        } catch (BaseException be) {
            sendProcessError(message, be.getMessageSeed().getDefaultFormat());
        } catch (Exception e) {
            sendProcessError(message, MessageSeeds.UNEXPECTED_EXCEPTION.getDefaultFormat(e.getLocalizedMessage()));
        }
    }

    private CancelledStatusChangeRequestDocument cancelRequestServiceCalls(StatusChangeRequestCancellationRequestMessage message) {
        Optional<ConnectionStatusChangeDomainExtension> extension = getRequestExtension(message.getId(), message.getCategoryCode());

        if (!extension.isPresent()) {
            return new CancelledStatusChangeRequestDocument(message.getId(), message.getCategoryCode(), 0, 0, 0);
        }
        String documentId = extension.get().getId();
        int cancelledRequests = 0;
        int notCancelledRequests = 0;
        ServiceCall parent = extension.get().getServiceCall();
        List<ServiceCall> serviceCalls = ServiceCallHelper.findChildren(parent);
        if (!parent.getState().isOpen()) {
            // send already processed message
            return new CancelledStatusChangeRequestDocument(message.getId(), message.getCategoryCode(), serviceCalls.size(), 0, 0);
        }
        for (ServiceCall serviceCall : serviceCalls) {
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

    private void sendProcessError(StatusChangeRequestCancellationRequestMessage message, MessageSeeds messageSeed, Object... messageSeedArgs) {
        log(LogLevel.SEVERE, thesaurus.getSimpleFormat(messageSeed).format(messageSeedArgs));
        sendMessage(MESSAGE_FACTORY.createFailedMessage(message, messageSeed, webServiceActivator.getMeteringSystemId(), clock.instant(), messageSeedArgs));
    }

    private void sendProcessError(StatusChangeRequestCancellationRequestMessage message, String msg) {
        log(LogLevel.SEVERE, msg);
        sendMessage(MESSAGE_FACTORY.createFailedMessage(message, msg, webServiceActivator.getMeteringSystemId(), clock.instant()));
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