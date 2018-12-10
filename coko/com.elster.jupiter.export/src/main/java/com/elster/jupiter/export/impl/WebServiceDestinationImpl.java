/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.export.DataExportWebService;
import com.elster.jupiter.export.ExportData;
import com.elster.jupiter.export.WebServiceDestination;
import com.elster.jupiter.export.impl.webservicecall.ServiceCallStatusImpl;
import com.elster.jupiter.export.impl.webservicecall.WebServiceDataExportServiceCallHandler;
import com.elster.jupiter.export.webservicecall.DataExportServiceCallType;
import com.elster.jupiter.export.webservicecall.ServiceCallStatus;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointProperty;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.streams.Predicates;

import javax.inject.Inject;
import java.nio.file.FileSystem;
import java.security.Principal;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

class WebServiceDestinationImpl extends AbstractDataExportDestination implements WebServiceDestination, DataDestination {
    enum Fields {
        CREATE_ENDPOINT("createEndPoint"),
        CHANGE_ENDPOINT("changeEndPoint");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        String javaFieldName() {
            return javaFieldName;
        }
    }

    private final ThreadPrincipalService threadPrincipalService;
    private final DataExportServiceCallType dataExportServiceCallType;

    @IsPresent(message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}", groups = {Save.Create.class, Save.Update.class})
    private Reference<EndPointConfiguration> createEndPoint = Reference.empty();
    private Reference<EndPointConfiguration> changeEndPoint = Reference.empty();

    @Inject
    WebServiceDestinationImpl(DataModel dataModel, Clock clock, Thesaurus thesaurus, IDataExportService dataExportService,
                              FileSystem fileSystem, TransactionService transactionService, ThreadPrincipalService threadPrincipalService) {
        super(dataModel, clock, thesaurus, dataExportService, fileSystem, transactionService);
        dataExportServiceCallType = dataExportService.getDataExportServiceCallType();
        this.threadPrincipalService = threadPrincipalService;
    }

    WebServiceDestinationImpl init(IExportTask task, EndPointConfiguration createEndPoint, EndPointConfiguration changeEndPoint) {
        initTask(task);
        setCreateWebServiceEndpoint(createEndPoint);
        setChangeWebServiceEndpoint(changeEndPoint);
        return this;
    }

    static WebServiceDestinationImpl from(IExportTask task, DataModel dataModel, EndPointConfiguration createEndPoint, EndPointConfiguration changeEndPoint) {
        return dataModel.getInstance(WebServiceDestinationImpl.class).init(task, createEndPoint, changeEndPoint);
    }

    @Override
    public void send(List<ExportData> data, TagReplacerFactory tagReplacerFactory, Logger logger) {
        EndPointConfiguration createEndPoint = getCreateWebServiceEndpoint();
        DataExportWebService createService = getExportWebService(createEndPoint);
        TimeDuration timeout = getTimeout(createEndPoint);
        List<ServiceCallStatus> serviceCallStates = new ArrayList<>(2);
        List<CompletableFuture<Void>> serviceCalls = new ArrayList<>(2);
        if (getChangeWebServiceEndpoint().filter(Predicates.not(createEndPoint::equals)).isPresent()) {
            EndPointConfiguration changeEndPoint = getChangeWebServiceEndpoint().get();
            DataExportWebService changeService = getExportWebService(changeEndPoint);
            TimeDuration changeTimeout = getTimeout(changeEndPoint);
            List<ExportData> createList = new ArrayList<>();
            List<ExportData> changeList = new ArrayList<>();
            data.forEach(exportData ->
                    (exportData.getStructureMarker().endsWith("update") ? changeList : createList)
                            .add(exportData));
            if (!createList.isEmpty()) {
                serviceCalls.add(callServiceAsync(createService, createEndPoint, createList, serviceCallStates, !timeout.isEmpty()));
            }
            if (!changeList.isEmpty()) {
                serviceCalls.add(callServiceAsync(changeService, changeEndPoint, changeList, serviceCallStates, !changeTimeout.isEmpty()));
            }
            if (createList.isEmpty() || !changeList.isEmpty() && changeTimeout.compareTo(timeout) > 0) {
                timeout = changeTimeout;
            }
        } else {
            serviceCalls.add(callServiceAsync(createService, createEndPoint, data, serviceCallStates, !timeout.isEmpty()));
        }
        execute(serviceCalls, serviceCallStates, timeout);
        processErrors(serviceCallStates);
    }

    @Override
    public void setCreateWebServiceEndpoint(EndPointConfiguration endPointConfiguration) {
        createEndPoint.set(endPointConfiguration);
    }

    @Override
    public EndPointConfiguration getCreateWebServiceEndpoint() {
        return createEndPoint.get();
    }

    @Override
    public void setChangeWebServiceEndpoint(EndPointConfiguration endPointConfiguration) {
        changeEndPoint.set(endPointConfiguration);
    }

    @Override
    public Optional<EndPointConfiguration> getChangeWebServiceEndpoint() {
        return changeEndPoint.getOptional();
    }

    private DataExportWebService getExportWebService(EndPointConfiguration endPoint) {
        return getDataExportService().getExportWebService(endPoint.getWebServiceName())
                .orElseThrow(() -> new DestinationFailedException(getThesaurus(), MessageSeeds.NO_WEBSERVICE_FOUND, endPoint.getName()));
    }

    private void processErrors(List<ServiceCallStatus> serviceCallStates) {
        if (!serviceCallStates.stream().allMatch(ServiceCallStatus::isSuccessful)) {
            serviceCallStates.stream()
                    .filter(ServiceCallStatus::isFailed)
                    // TODO: treat all error messages?
                    .findAny()
                    .ifPresent(status -> {
                        String error = status.getErrorMessage()
                                .orElseGet(() -> getThesaurus().getSimpleFormat(MessageSeeds.WEB_SERVICE_EXPORT_NO_ERROR_MESSAGE).format());
                        throw new DestinationFailedException(getThesaurus(), MessageSeeds.WEB_SERVICE_EXPORT_NOT_CONFIRMED, error);
                    });
            if (serviceCallStates.stream().anyMatch(ServiceCallStatus::isOpen)) {
                String error = getThesaurus().getSimpleFormat(MessageSeeds.WEB_SERVICE_EXPORT_NO_CONFIRMATION).format();
                throw new DestinationFailedException(getThesaurus(), MessageSeeds.WEB_SERVICE_EXPORT_NOT_CONFIRMED, error);
            }
            // this case should not happen actually; if reproduced, there's some integration bug between data export destination & web service implementation
            serviceCallStates.stream()
                    .filter(Predicates.not(ServiceCallStatus::isSuccessful))
                    .findFirst()
                    .ifPresent(unexpectedState -> {
                        throw new DestinationFailedException(getThesaurus(), MessageSeeds.WEB_SERVICE_EXPORT_UNEXPECTED_STATE,
                                unexpectedState.getServiceCall().getNumber(), unexpectedState.getState().name());
                    });
        }
    }

    private void execute(List<CompletableFuture<Void>> serviceCalls, List<ServiceCallStatus> results, TimeDuration timeout) {
        try {
            CompletableFuture<Void> allOperations = CompletableFuture.allOf(serviceCalls.toArray(new CompletableFuture[serviceCalls.size()]));
            if (timeout.isEmpty()) {
                allOperations.get();
            } else {
                allOperations.get(timeout.getMilliSeconds(), TimeUnit.MILLISECONDS);
            }
        } catch (InterruptedException | TimeoutException e) {
            // at least one of service calls hasn't managed to complete in time
            results.add(ServiceCallStatusImpl.ONGOING);
        } catch (CompletionException | ExecutionException e) {
            throw new DestinationFailedException(getThesaurus(), MessageSeeds.WEB_SERVICE_EXPORT_FAILURE, e, e.getCause().getLocalizedMessage());
        }
    }

    private CompletableFuture<Void> callServiceAsync(DataExportWebService service, EndPointConfiguration endPoint,
                                                     List<ExportData> data, List<ServiceCallStatus> results, boolean waitForServiceCall) {
        Principal principal = threadPrincipalService.getPrincipal();
        return CompletableFuture.supplyAsync(() -> {
            threadPrincipalService.set(principal);
            return callService(service, endPoint, data, waitForServiceCall);
        }, Executors.newSingleThreadExecutor())
                .thenAccept(results::add);
    }

    private <T extends ExportData> ServiceCallStatus callService(DataExportWebService service, EndPointConfiguration endPoint, List<ExportData> data, boolean waitForServiceCall) {
        Optional<ServiceCall> serviceCall = service.call(endPoint, data.stream());
        if (waitForServiceCall && serviceCall.isPresent()) {
            ServiceCallStatus status;
            do {
                try {
                    Thread.sleep(1000L * WebServiceDataExportServiceCallHandler.CHECK_PAUSE_IN_SECONDS);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                status = dataExportServiceCallType.getStatus(serviceCall.get());
            } while (status.isOpen());
            return status;
        }
        return ServiceCallStatusImpl.SUCCESS;
    }

    private static TimeDuration getTimeout(EndPointConfiguration endPoint) {
        return endPoint.getProperties().stream()
                .filter(property -> DataExportWebService.TIMEOUT_PROPERTY_KEY.equals(property.getName()))
                .findAny()
                .map(EndPointProperty::getValue)
                .filter(TimeDuration.class::isInstance)
                .map(TimeDuration.class::cast)
                .orElse(TimeDuration.NONE);
    }
}
