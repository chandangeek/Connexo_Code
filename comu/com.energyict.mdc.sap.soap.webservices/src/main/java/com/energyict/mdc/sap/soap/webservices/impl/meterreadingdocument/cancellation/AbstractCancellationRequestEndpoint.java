package com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument.cancellation;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
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
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreadingdocument.MeterReadingDocumentCreateRequestCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreadingdocument.MeterReadingDocumentCreateRequestDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreadingdocument.MeterReadingDocumentCreateResultCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreadingdocument.MeterReadingDocumentCreateResultDomainExtension;

import javax.inject.Inject;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;

public abstract class AbstractCancellationRequestEndpoint extends AbstractInboundEndPoint
        implements ApplicationSpecific {

    private final EndPointConfigurationService endPointConfigurationService;
    private final ServiceCallService serviceCallService;
    private final Thesaurus thesaurus;
    private final Clock clock;
    private final OrmService ormService;

    @Inject
    public AbstractCancellationRequestEndpoint(EndPointConfigurationService endPointConfigurationService, ServiceCallService serviceCallService,
                                               Thesaurus thesaurus, Clock clock, OrmService ormService) {
        this.endPointConfigurationService = endPointConfigurationService;
        this.serviceCallService = serviceCallService;
        this.thesaurus = thesaurus;
        this.clock = clock;
        this.ormService = ormService;
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
        if (message.isValid()) {
            List<CancelledMeterReadingDocument> documents = cancelMeterReadings(message);

            MeterReadingDocumentCancellationConfirmationMessage confirmationMessage =
                    MeterReadingDocumentCancellationConfirmationMessage.builder()
                            .from(message.getRequestID(), documents, clock.instant(), message.isBulk())
                            .build();
            sendMessage(confirmationMessage, message.isBulk());
        } else {
            sendProcessError(message, MessageSeeds.INVALID_MESSAGE_FORMAT);
        }
    }

    private List<CancelledMeterReadingDocument> cancelMeterReadings(MeterReadingDocumentCancellationRequestMessage message) {
        List<String> meterReadingDocumentIds = message.getMeterReadingDocumentIds();
        List<CancelledMeterReadingDocument> cancelledDocuments = new ArrayList<>();

        cancelRequestServiceCalls(meterReadingDocumentIds, cancelledDocuments);
        cancelResultServiceCalls(meterReadingDocumentIds, cancelledDocuments);

        //consider the remaining meter reading documents as not found
        for (Iterator<String> mrdIterator = meterReadingDocumentIds.iterator(); mrdIterator.hasNext(); ) {
            String item = mrdIterator.next();
            mrdIterator.remove();
            cancelledDocuments.add(new CancelledMeterReadingDocument(item, false, MessageSeeds.NO_METER_READING_DOCUMENT, item));
        }

        return cancelledDocuments;
    }

    private void cancelRequestServiceCalls(List<String> meterReadingDocumentIds, List<CancelledMeterReadingDocument> cancelledDocuments) {
        List<ServiceCall> serviceCallFinder = getRequestServiceCall(meterReadingDocumentIds);

        serviceCallFinder.stream().forEach(serviceCall -> {
            Optional<MeterReadingDocumentCreateRequestDomainExtension> requestExtension = serviceCall.getExtension(MeterReadingDocumentCreateRequestDomainExtension.class);
            if (requestExtension.isPresent()) {
                String documentId = requestExtension.get().getMeterReadingDocumentId();
                try {
                    if (serviceCall.getState().isOpen()) {
                        serviceCall = lock(serviceCall);
                        if (serviceCall.getState().isOpen()) {
                            serviceCall.log(com.elster.jupiter.servicecall.LogLevel.INFO, "Cancel the service call by request from SAP.");
                            requestExtension.get().setCancelledBySap(true);
                            serviceCall.update(requestExtension.get());
                            if (serviceCall.getState().equals(DefaultState.CREATED)) {
                                serviceCall.requestTransition(DefaultState.PENDING);
                            }
                            serviceCall.requestTransition(DefaultState.CANCELLED);
                            meterReadingDocumentIds.remove(documentId);
                            cancelledDocuments.add(new CancelledMeterReadingDocument(documentId, true));
                        } else {
                            // For closed state if we don't have result service call for this meter reading document,
                            // we don't need to created result service call in the future
                            cancelIfOnlyRequestServiceCall(serviceCall, documentId, meterReadingDocumentIds, cancelledDocuments);
                        }
                    } else {
                        // For closed state if we don't have result service call for this meter reading document,
                        // we don't need to created result service call in the future
                        cancelIfOnlyRequestServiceCall(serviceCall, documentId, meterReadingDocumentIds, cancelledDocuments);
                    }
                } catch (Exception ex) {
                    meterReadingDocumentIds.remove(documentId);
                    cancelledDocuments.add(new CancelledMeterReadingDocument(documentId, false, MessageSeeds.ERROR_CANCELLING_METER_READING_DOCUMENT, ex.getLocalizedMessage()));
                }
            }
        });
    }

    private void cancelResultServiceCalls(List<String> meterReadingDocumentIds, List<CancelledMeterReadingDocument> cancelledDocuments) {
        List<ServiceCall> serviceCallFinder = getResultServiceCall(meterReadingDocumentIds);

        serviceCallFinder.stream().forEach(serviceCall -> {
            Optional<MeterReadingDocumentCreateResultDomainExtension> requestExtension = serviceCall.getExtension(MeterReadingDocumentCreateResultDomainExtension.class);
            if (requestExtension.isPresent()) {
                String documentId = requestExtension.get().getMeterReadingDocumentId();
                try {
                    if (serviceCall.getState().isOpen()) {
                        serviceCall = lock(serviceCall);
                        if (serviceCall.getState().isOpen()) {
                            serviceCall.log(com.elster.jupiter.servicecall.LogLevel.INFO, "Cancel the service call by request from SAP.");
                            requestExtension.get().setCancelledBySap(true);
                            serviceCall.update(requestExtension.get());
                            if (serviceCall.getState().equals(DefaultState.CREATED)) {
                                serviceCall.requestTransition(DefaultState.PENDING);
                            }
                            serviceCall.requestTransition(DefaultState.CANCELLED);
                            meterReadingDocumentIds.remove(documentId);
                            cancelledDocuments.add(new CancelledMeterReadingDocument(documentId, true));
                        } else {
                            meterReadingDocumentIds.remove(documentId);
                            cancelledDocuments.add(new CancelledMeterReadingDocument(documentId, false, MessageSeeds.METER_READING_DOCUMENT_IS_PROCESSED, documentId));
                        }
                    } else {
                        meterReadingDocumentIds.remove(documentId);
                        cancelledDocuments.add(new CancelledMeterReadingDocument(documentId, false, MessageSeeds.METER_READING_DOCUMENT_IS_PROCESSED, documentId));
                    }
                } catch (Exception ex) {
                    meterReadingDocumentIds.remove(documentId);
                    cancelledDocuments.add(new CancelledMeterReadingDocument(documentId, false, MessageSeeds.ERROR_CANCELLING_METER_READING_DOCUMENT, ex.getLocalizedMessage()));
                }
            }
        });
    }

    private List<ServiceCall> getRequestServiceCall(List<String> meterReadingDocumentIds) {
        Optional<DataModel> dataModel = ormService.getDataModel(MeterReadingDocumentCreateRequestCustomPropertySet.MODEL_NAME);
        if (dataModel.isPresent()) {
            return dataModel.get().stream(MeterReadingDocumentCreateRequestDomainExtension.class)
                    .join(ServiceCall.class)
                    .filter(where(MeterReadingDocumentCreateRequestDomainExtension.FieldNames.METER_READING_DOCUMENT_ID.javaName()).in(meterReadingDocumentIds))
                    .map(MeterReadingDocumentCreateRequestDomainExtension::getServiceCall)
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    private List<ServiceCall> getResultServiceCall(List<String> meterReadingDocumentIds) {
        Optional<DataModel> dataModel = ormService.getDataModel(MeterReadingDocumentCreateResultCustomPropertySet.MODEL_NAME);
        if (dataModel.isPresent()) {
            return dataModel.get().stream(MeterReadingDocumentCreateResultDomainExtension.class)
                    .join(ServiceCall.class)
                    .filter(where(MeterReadingDocumentCreateResultDomainExtension.FieldNames.METER_READING_DOCUMENT_ID.javaName()).in(meterReadingDocumentIds))
                    .map(MeterReadingDocumentCreateResultDomainExtension::getServiceCall)
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    private void cancelIfOnlyRequestServiceCall(ServiceCall serviceCall, String documentId,
                                                List<String> meterReadingDocumentIds, List<CancelledMeterReadingDocument> cancelledDocuments) {
        if (getResultServiceCall(Arrays.asList(documentId)).isEmpty()) {
            serviceCall = lock(serviceCall);
            if (getResultServiceCall(Arrays.asList(documentId)).isEmpty()) {
                Optional<MeterReadingDocumentCreateRequestDomainExtension> requestExtension = serviceCall.getExtension(MeterReadingDocumentCreateRequestDomainExtension.class);
                requestExtension.get().setCancelledBySap(true);
                serviceCall.update(requestExtension.get());
                meterReadingDocumentIds.remove(documentId);
                cancelledDocuments.add(new CancelledMeterReadingDocument(documentId, true));
            }
        }
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

    private ServiceCall lock(ServiceCall serviceCall) {
        return serviceCallService.lockServiceCall(serviceCall.getId())
                .orElseThrow(() -> new IllegalStateException("Service call " + serviceCall.getNumber() + " disappeared."));
    }
}
