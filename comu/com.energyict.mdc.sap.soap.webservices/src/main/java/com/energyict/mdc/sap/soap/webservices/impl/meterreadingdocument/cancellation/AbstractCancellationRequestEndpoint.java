package com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument.cancellation;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallFilter;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractInboundEndPoint;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.LogLevel;

import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallTypes;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreadingdocument.MeterReadingDocumentCreateRequestDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreadingdocument.MeterReadingDocumentCreateResultDomainExtension;

import javax.inject.Inject;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

public class AbstractCancellationRequestEndpoint extends AbstractInboundEndPoint
        implements ApplicationSpecific {

    private final EndPointConfigurationService endPointConfigurationService;
    private final ServiceCallService serviceCallService;
    private final Thesaurus thesaurus;
    private final Clock clock;

    @Inject
    public AbstractCancellationRequestEndpoint(EndPointConfigurationService endPointConfigurationService, ServiceCallService serviceCallService,
                                               Thesaurus thesaurus, Clock clock) {
        this.endPointConfigurationService = endPointConfigurationService;
        this.serviceCallService = serviceCallService;
        this.thesaurus = thesaurus;
        this.clock = clock;
    }

    @Override
    public String getApplication() {
        return ApplicationSpecific.WebServiceApplicationName.MULTISENSE.getName();
    }

    Thesaurus getThesaurus() {
        return thesaurus;
    }

    boolean isAnyActiveEndpoint(String name) {
        return endPointConfigurationService
                .getEndPointConfigurationsForWebService(name)
                .stream()
                .filter(EndPointConfiguration::isActive)
                .findAny().isPresent();
    }

    void handleMessage(MeterReadingDocumentCancellationRequestMessage message) {
        if(message.isValid()){
            List<CancelledMeterReadingDocument> documents = cancelServiceCalls(message.getMeterReadingDocumentIds());

            MeterReadingDocumentCancellationConfirmationMessage confirmationMessage =
                    MeterReadingDocumentCancellationConfirmationMessage.builder()
                            .from(message.getRequestID(), documents, clock.instant(), message.isBulk())
                            .build();
            sendMessage(confirmationMessage, message.isBulk());
        }
        else{
            sendProcessError(message, MessageSeeds.INVALID_MESSAGE_FORMAT);
        }
    }

    private List<CancelledMeterReadingDocument> cancelServiceCalls(List<String> meterReadingDocumentIds){
        List<CancelledMeterReadingDocument> documents = new ArrayList<>();

        Finder<ServiceCall> serviceCallFinder = findOpenServiceCalls(ServiceCallTypes.METER_READING_DOCUMENT_CREATE_REQUEST);

        serviceCallFinder.stream().forEach(serviceCall -> {

            MeterReadingDocumentCreateRequestDomainExtension requestExtension = serviceCall.getExtension(MeterReadingDocumentCreateRequestDomainExtension.class).get();
            meterReadingDocumentIds.stream().forEach(
                    item -> {
                        if (item.equals(requestExtension.getMeterReadingDocumentId())) {
                            try {
                                if(serviceCall.getState().isOpen()) {
                                    lock(serviceCall);
                                    serviceCall.log(com.elster.jupiter.servicecall.LogLevel.INFO, "Cancel the service call by request from SAP.");
                                    requestExtension.setCancelledBySap("Yes");
                                    serviceCall.update(requestExtension);
                                    if (serviceCall.getState().equals(DefaultState.CREATED)) {
                                        serviceCall.requestTransition(DefaultState.PENDING);
                                    }
                                    serviceCall.requestTransition(DefaultState.CANCELLED);
                                    //receivedMessages.remove(item);
                                    meterReadingDocumentIds.remove(item);
                                    documents.add(new CancelledMeterReadingDocument(item, true));
                                }
                            } catch (Exception ex) {
                                meterReadingDocumentIds.remove(item);
                                documents.add(new CancelledMeterReadingDocument(item, true, MessageSeeds.ERROR_CANCELLING_METER_READING_DOCUMENT, ex.getLocalizedMessage()));
                            }
                        }
                    }
            );
        });

        serviceCallFinder = findOpenServiceCalls(ServiceCallTypes.METER_READING_DOCUMENT_CREATE_RESULT);

        serviceCallFinder.stream().forEach(serviceCall -> {

            MeterReadingDocumentCreateResultDomainExtension requestExtension = serviceCall.getExtension(MeterReadingDocumentCreateResultDomainExtension.class).get();
            meterReadingDocumentIds.stream().forEach(
                    item -> {
                        if (item.equals(requestExtension.getMeterReadingDocumentId())) {
                            try {
                                lock(serviceCall);
                                meterReadingDocumentIds.remove(item);
                                requestExtension.setCancelledBySap("Yes");
                                serviceCall.update(requestExtension);
                                serviceCall.log(com.elster.jupiter.servicecall.LogLevel.INFO, "Cancel the service call by request from SAP.");
                                if(serviceCall.getState().isOpen()) {
                                    if (serviceCall.getState().equals(DefaultState.CREATED)) {
                                        serviceCall.requestTransition(DefaultState.PENDING);
                                    }
                                    serviceCall.requestTransition(DefaultState.CANCELLED);
                                    meterReadingDocumentIds.remove(item);
                                    documents.add(new CancelledMeterReadingDocument(item, true));
                                   // successMrdIds.add(new MeterReadingDocument(item));
                                }else{
                                    meterReadingDocumentIds.remove(item);
                                    documents.add(new CancelledMeterReadingDocument(item, true, MessageSeeds.METER_READING_DOCUMENT_IS_PROCESSED));
                                }
                            } catch (Exception ex) {
                                meterReadingDocumentIds.remove(item);
                                documents.add(new CancelledMeterReadingDocument(item, true, MessageSeeds.ERROR_CANCELLING_METER_READING_DOCUMENT, ex.getLocalizedMessage()));
                            }
                        }
                    }
            );
        });

        meterReadingDocumentIds.stream().forEach(item->{
            meterReadingDocumentIds.remove(item);
            documents.add(new CancelledMeterReadingDocument(item, true, MessageSeeds.NO_METER_READING_DOCUMENT));
        });

        return documents;
    }

    private void sendProcessError(MeterReadingDocumentCancellationRequestMessage message, MessageSeeds messageSeed) {
        log(LogLevel.WARNING, thesaurus.getFormat(messageSeed).format());
        MeterReadingDocumentCancellationConfirmationMessage confirmationMessage =
                MeterReadingDocumentCancellationConfirmationMessage.builder()
                        .from(message, messageSeed, clock.instant())
                        .build();
        sendMessage(confirmationMessage, message.isBulk());
    }

    private void sendMessage(MeterReadingDocumentCancellationConfirmationMessage confirmationMessage, boolean bulk) {
        if (bulk) {
            WebServiceActivator.METER_READING_DOCUMENT_BULK_CANCELLATION_CONFIRMATION
                    .forEach(service -> service.call(confirmationMessage));
        } else {
            WebServiceActivator.METER_READING_DOCUMENT_CANCELLATION_CONFIRMATION
                    .forEach(service -> service.call(confirmationMessage));
        }
    }

    private Finder<ServiceCall> findOpenServiceCalls(ServiceCallTypes serviceCallType) {
        ServiceCallFilter filter = new ServiceCallFilter();
        filter.types.add(serviceCallType.getTypeName());
        /*filter.states.addAll(Arrays.stream(DefaultState.values())
                .filter(state -> state.isOpen())
                .map(state -> state.name())
                .collect(Collectors.toSet()));*/
        return serviceCallService.getServiceCallFinder(filter);
    }

    private ServiceCall lock(ServiceCall serviceCall) {
        return serviceCallService.lockServiceCall(serviceCall.getId())
                .orElseThrow(() -> new IllegalStateException("Service call " + serviceCall.getNumber() + " disappeared."));
    }
}
