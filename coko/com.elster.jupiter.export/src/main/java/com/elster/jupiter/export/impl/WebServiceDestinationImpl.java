/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.export.DataExportWebService;
import com.elster.jupiter.export.ExportData;
import com.elster.jupiter.export.MeterReadingData;
import com.elster.jupiter.export.ReadingTypeDataExportItem;
import com.elster.jupiter.export.WebServiceDestination;
import com.elster.jupiter.export.webservicecall.DataExportServiceCallType;
import com.elster.jupiter.export.webservicecall.ServiceCallStatus;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.servicecall.DefaultState;
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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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

    enum Data {
        CREATED,
        CHANGED,
        CREATED_AND_CHANGED
    }

    private final ThreadPrincipalService threadPrincipalService;
    private final DataExportServiceCallType dataExportServiceCallType;
    static final int CHECK_PAUSE_IN_SECONDS = 10;

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
    public DataSendingStatus send(List<ExportData> data, TagReplacerFactory tagReplacerFactory, Logger logger) {
        EndPointConfiguration createEndPoint = getCreateWebServiceEndpoint();
        DataExportWebService createService = getExportWebService(createEndPoint);
        TimeDuration timeout = getTimeout(createEndPoint);
        DataSendingResult createDataResult = new DataSendingResult();
        DataSendingResult changeDataResult = new DataSendingResult();
        List<CompletableFuture<Void>> serviceCalls = new ArrayList<>();
        List<ExportData> createList = new ArrayList<>();
        List<ExportData> changeList = new ArrayList<>();
        boolean isCreateAndChange = false;
        if (getChangeWebServiceEndpoint().filter(Predicates.not(createEndPoint::equals)).isPresent()
                || (getChangeWebServiceEndpoint().isPresent() && !getDataExportService().shouldCombineCreatedAndUpdatedDataInOneWebRequest())) {
            EndPointConfiguration changeEndPoint = getChangeWebServiceEndpoint().get();
            DataExportWebService changeService = getExportWebService(changeEndPoint);
            TimeDuration changeTimeout = getTimeout(changeEndPoint);
            for (ExportData exportData : data) {
                (exportData.getStructureMarker().endsWith("update") ? changeList : createList)
                        .add(exportData);
            }
            if (!createList.isEmpty()) {
                serviceCalls.add(callServiceAsync(createService, createEndPoint, createList, createDataResult, !timeout.isEmpty()));
            }
            if (!changeList.isEmpty()) {
                serviceCalls.add(callServiceAsync(changeService, changeEndPoint, changeList, changeDataResult, !changeTimeout.isEmpty()));
            }
            if (createList.isEmpty() || !changeList.isEmpty() && changeTimeout.compareTo(timeout) > 0) {
                timeout = changeTimeout;
            }
        } else {
            if (getChangeWebServiceEndpoint().isPresent()) {
                isCreateAndChange = true;
            }
            createList = data;
            serviceCalls.add(callServiceAsync(createService, createEndPoint, createList, createDataResult, !timeout.isEmpty()));
        }
        execute(serviceCalls, timeout);
        DataSendingStatus.Builder dataSendingStatusBuilder = DataSendingStatus.builder();
        if (!createList.isEmpty()) {
            processErrors(createDataResult, createList, dataSendingStatusBuilder, logger, isCreateAndChange ? Data.CREATED_AND_CHANGED : Data.CREATED);
        }
        if (!changeList.isEmpty()) {
            processErrors(changeDataResult, changeList, dataSendingStatusBuilder, logger, Data.CHANGED);
        }
        return dataSendingStatusBuilder.build();
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

    private void processErrors(DataSendingResult dataSendingResult, List<ExportData> data, DataSendingStatus.Builder statusBuilder, Logger logger, Data operation) {
        getTransactionService().run(() -> doProcessErrors(dataSendingResult, data, statusBuilder, logger, operation));
    }

    private void doProcessErrors(DataSendingResult dataSendingResult, List<ExportData> data, DataSendingStatus.Builder statusBuilder, Logger logger, Data operation) {
        List<ServiceCallStatus> states = dataSendingResult.getFinalStatuses();
        Set<ServiceCall> unsuccessfulServiceCalls = states.stream()
                .filter(Predicates.not(ServiceCallStatus::isSuccessful))
                .peek(status -> {
                    String error;
                    switch (status.getState()) {
                        case FAILED:
                        case PARTIAL_SUCCESS:
                            error = status.getErrorMessage()
                                    .orElseGet(() -> getThesaurus().getSimpleFormat(MessageSeeds.WEB_SERVICE_EXPORT_NO_ERROR_MESSAGE).format());
                            break;
                        case CREATED:
                        case PENDING:
                        case ONGOING:
                            error = getThesaurus().getSimpleFormat(MessageSeeds.WEB_SERVICE_EXPORT_NO_CONFIRMATION).format();
                            dataExportServiceCallType.tryFailingServiceCall(status.getServiceCall(), error);
                            break;
                        default:
                            // this case should not happen actually
                            // if reproduced, there's some integration bug between data export destination & web service implementation
                            error = getThesaurus().getSimpleFormat(MessageSeeds.WEB_SERVICE_EXPORT_UNEXPECTED_STATE).format(status.getState().name());
                            break;
                    }
                    error = getThesaurus().getSimpleFormat(MessageSeeds.WEB_SERVICE_EXPORT_NOT_CONFIRMED).format(status.getServiceCall().getNumber(), error);
                    logger.severe(error);
                })
                .map(ServiceCallStatus::getServiceCall)
                .map(ServiceCall::findChildren)
                .flatMap(Finder::stream)
                .filter(s -> s.getState().equals(DefaultState.FAILED) ||
                        s.getState().equals(DefaultState.CANCELLED) ||
                        s.getState().equals(DefaultState.REJECTED))
                .collect(Collectors.toSet());

        Set<ReadingTypeDataExportItem> failedDataSources = Collections.emptySet();
        if (!unsuccessfulServiceCalls.isEmpty()) {
            failedDataSources = dataExportServiceCallType.getDataSources(unsuccessfulServiceCalls);
            if (failedDataSources.isEmpty()) { // service calls keep no track of data sources; need to fail them all
                updateStatusBuilder(statusBuilder, operation);
            } else {
                updateStatusBuilder(statusBuilder, operation, failedDataSources);
            }
        }
        if (!dataSendingResult.sent) {
            // some service calls are possibly not created yet
            Set<ServiceCall> successfulServiceCalls = states.stream()
                    .filter(state -> state.isSuccessful() || state.isPartiallySuccessful())
                    .map(ServiceCallStatus::getServiceCall)
                    .map(ServiceCall::findChildren)
                    .flatMap(Finder::stream)
                    .filter(s -> s.getState().equals(DefaultState.SUCCESSFUL))
                    .collect(Collectors.toSet());
            Set<ReadingTypeDataExportItem> trackedDataSources = dataExportServiceCallType.getDataSources(successfulServiceCalls);
            trackedDataSources.addAll(failedDataSources);
            Set<ReadingTypeDataExportItem> untrackedDataSources = new HashSet<>();
            for (ExportData exportData : data) {
                if (exportData instanceof MeterReadingData) {
                    ReadingTypeDataExportItem dataSource = ((MeterReadingData) exportData).getItem();
                    if (!trackedDataSources.contains(dataSource)) {
                        untrackedDataSources.add(dataSource);
                        logger.severe(getThesaurus().getSimpleFormat(MessageSeeds.WEB_SERVICE_EXPORT_NO_SERVICE_CALL).format(dataSource.getDescription()));
                    }
                } else {
                    // no data sources in context; need to fail all the data
                    updateStatusBuilder(statusBuilder, operation);
                    break; // no need to go on, status is completely failed
                }
            }
            updateStatusBuilder(statusBuilder, operation, untrackedDataSources);
        }
    }

    private void updateStatusBuilder(DataSendingStatus.Builder statusBuilder, Data operation) {
        switch (operation) {
            case CREATED:
                statusBuilder.withAllDataSourcesFailedForNewData();
                break;
            case CHANGED:
                statusBuilder.withAllDataSourcesFailedForChangedData();
                break;
            case CREATED_AND_CHANGED:
                statusBuilder.withAllDataSourcesFailed();
                break;
        }
    }

    private void updateStatusBuilder(DataSendingStatus.Builder statusBuilder, Data operation, Set<ReadingTypeDataExportItem> dataSources) {
        switch (operation) {
            case CREATED:
                statusBuilder.withFailedDataSourcesForNewData(dataSources);
                break;
            case CHANGED:
                statusBuilder.withFailedDataSourcesForChangedData(dataSources);
                break;
            case CREATED_AND_CHANGED:
                statusBuilder.withFailedDataSources(dataSources);
                break;
        }
    }

    private void execute(List<CompletableFuture<Void>> serviceCalls, TimeDuration timeout) {
        try {
            CompletableFuture<Void> allOperations = CompletableFuture.allOf(serviceCalls.toArray(new CompletableFuture[serviceCalls.size()]));
            if (timeout.isEmpty()) {
                allOperations.get();
            } else {
                allOperations.get(timeout.getMilliSeconds(), TimeUnit.MILLISECONDS);
            }
        } catch (InterruptedException | TimeoutException e) {
            // at least one of service calls hasn't managed to complete in time
            // or hasn't been created, just rely on DataSendingResult to resolve status
        } catch (CompletionException | ExecutionException e) {
            throw new DestinationFailedException(getThesaurus(), MessageSeeds.WEB_SERVICE_EXPORT_FAILURE, e, e.getCause().getLocalizedMessage());
        }
    }

    private CompletableFuture<Void> callServiceAsync(DataExportWebService service, EndPointConfiguration endPoint,
                                                     List<ExportData> data, DataSendingResult result, boolean waitForServiceCall) {
        Principal principal = threadPrincipalService.getPrincipal();
        return CompletableFuture.runAsync(() -> {
            threadPrincipalService.set(principal);
            callService(service, endPoint, data, result, waitForServiceCall);
        }, Executors.newSingleThreadExecutor());
    }

    private void callService(DataExportWebService service, EndPointConfiguration endPoint, List<ExportData> data,
                             DataSendingResult result, boolean waitForServiceCalls) {
        service.call(endPoint, data.stream(), result);
        result.sent = true;
        if (waitForServiceCalls && !result.openServiceCalls.isEmpty()) {
            while (result.hasOpenServiceCalls()) {
                try {
                    Thread.sleep(1000L * CHECK_PAUSE_IN_SECONDS);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            result.openServiceCalls.clear(); // success case as the timeout is not configured => not interested in confirmation
        }
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

    private class DataSendingResult implements DataExportWebService.ExportContext {
        private volatile boolean sent;
        private Set<ServiceCall> openServiceCalls = ConcurrentHashMap.newKeySet();
        private Set<ServiceCallStatus> closedServiceCallStatuses = ConcurrentHashMap.newKeySet();

        private boolean hasOpenServiceCalls() {
            List<ServiceCallStatus> statuses = dataExportServiceCallType.getStatuses(openServiceCalls);
            statuses.stream()
                    .filter(Predicates.not(ServiceCallStatus::isOpen))
                    .peek(closedServiceCallStatuses::add)
                    .map(ServiceCallStatus::getServiceCall)
                    .forEach(openServiceCalls::remove);
            return !openServiceCalls.isEmpty();
        }

        private List<ServiceCallStatus> getFinalStatuses() {
            List<ServiceCallStatus> result = dataExportServiceCallType.getStatuses(openServiceCalls);
            result.addAll(closedServiceCallStatuses);
            return result;
        }

        @Override
        public ServiceCall startAndRegisterServiceCall(String uuid, long timeout, Map<ReadingTypeDataExportItem, String> data) {
            ServiceCall serviceCall = dataExportServiceCallType.startServiceCallAsync(uuid, timeout, data);
            openServiceCalls.add(serviceCall);
            return serviceCall;
        }
    }
}
