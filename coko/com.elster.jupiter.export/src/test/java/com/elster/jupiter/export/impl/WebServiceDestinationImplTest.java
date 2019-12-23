/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.devtools.persistence.test.TransactionVerifier;
import com.elster.jupiter.export.DataExportWebService;
import com.elster.jupiter.export.ExportData;
import com.elster.jupiter.export.MeterReadingData;
import com.elster.jupiter.export.MeterReadingValidationData;
import com.elster.jupiter.export.ReadingTypeDataExportItem;
import com.elster.jupiter.export.impl.webservicecall.WebServiceDataExportServiceCallHandler;
import com.elster.jupiter.export.webservicecall.DataExportServiceCallType;
import com.elster.jupiter.export.webservicecall.ServiceCallStatus;
import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointProperty;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.transaction.TransactionService;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import java.nio.file.FileSystem;
import java.security.Principal;
import java.time.Clock;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.elster.jupiter.devtools.tests.MatchersExtension.anyListContaining;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollectionOf;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class WebServiceDestinationImplTest {
    private static final String WEB_SERVICE_CREATE = "CreateWebService";
    private static final String WEB_SERVICE_CHANGE = "ChangeWebService";
    private static final String CREATE_SERVICE_CALL_ID = "SC_00048576";
    private static final String CHANGE_SERVICE_CALL_ID = "SC_00029162";
    private static final Principal PRINCIPAL = () -> "Batch executor";

    private Clock clock = Clock.systemDefaultZone();
    private TagReplacerFactory tagReplacerFactory = structureMarker -> TagReplacerImpl.asTagReplacer(clock, structureMarker, 17);
    private Thesaurus thesaurus = NlsModule.FakeThesaurus.INSTANCE;
    private TransactionService transactionService = new TransactionVerifier();
    @Mock
    private Logger logger;
    @Mock
    private DataModel dataModel;
    @Mock
    private IDataExportService dataExportService;
    @Mock
    private ThreadPrincipalService threadPrincipalService;
    @Mock
    private IExportTask exportTask;
    @Mock
    private FileSystem fileSystem;
    @Mock
    private EndPointConfiguration createEndPoint, changeEndPoint;
    private MeterReadingData newData, updatedData;
    @Mock
    private EndPointProperty timeout;
    @Mock
    private DataExportWebService webServiceCreate, webServiceChange;
    @Mock
    private ServiceCall createServiceCall, changeServiceCall;
    @Mock
    private DataExportServiceCallType serviceCallType;
    @Captor
    private ArgumentCaptor<Stream<ExportData>> dataStreamCaptor;
    @Mock
    private ReadingTypeDataExportItem source1, source2;
    @Mock
    private MeterReading meterReading;

    private Map<ServiceCall, ServiceCallStatus> serviceCallStatuses; // container for current stubbing of service call statuses in a test method

    @Before
    public void setUp() {
        serviceCallStatuses = new HashMap<>();
        when(serviceCallType.getStatuses(anyCollectionOf(ServiceCall.class))).thenAnswer(invocation ->
                ((Collection<ServiceCall>) invocation.getArgumentAt(0, Collection.class)).stream()
                        .map(serviceCallStatuses::get)
                        .collect(Collectors.toList()));
        newData = createData(source1, false);
        updatedData = createData(source2, true);

        when(createEndPoint.getProperties()).thenReturn(Collections.singletonList(timeout));
        when(changeEndPoint.getProperties()).thenReturn(Collections.singletonList(timeout));
        when(timeout.getName()).thenReturn(DataExportWebService.TIMEOUT_PROPERTY_KEY);
        when(timeout.getValue()).thenReturn(TimeDuration.seconds((int) (.5 * WebServiceDataExportServiceCallHandler.CHECK_PAUSE_IN_SECONDS)));

        when(createEndPoint.getWebServiceName()).thenReturn(WEB_SERVICE_CREATE);
        when(changeEndPoint.getWebServiceName()).thenReturn(WEB_SERVICE_CHANGE);
        when(dataExportService.getExportWebService(WEB_SERVICE_CREATE)).thenReturn(Optional.of(webServiceCreate));
        when(dataExportService.getExportWebService(WEB_SERVICE_CHANGE)).thenReturn(Optional.of(webServiceChange));

        when(serviceCallType.startServiceCallAsync(eq("uuidCr"), eq(1L), anyCollectionOf(ReadingTypeDataExportItem.class))).thenReturn(createServiceCall);
        doAnswer(invocation -> invocation.getArgumentAt(2, DataExportWebService.ExportContext.class).startAndRegisterServiceCall("uuidCr", 1, Collections.singleton(source1)))
                .when(webServiceCreate).call(any(EndPointConfiguration.class), any(), any(DataExportWebService.ExportContext.class));
        when(serviceCallType.getDataSources(Collections.singleton(createServiceCall))).thenReturn(Collections.singleton(source1));
        when(serviceCallType.startServiceCallAsync(eq("uuidCh"), eq(2L), anyCollectionOf(ReadingTypeDataExportItem.class))).thenReturn(changeServiceCall);
        doAnswer(invocation -> invocation.getArgumentAt(2, DataExportWebService.ExportContext.class).startAndRegisterServiceCall("uuidCh", 2, Collections.singleton(source2)))
                .when(webServiceChange).call(any(EndPointConfiguration.class), any(), any(DataExportWebService.ExportContext.class));
        when(serviceCallType.getDataSources(Collections.singleton(changeServiceCall))).thenReturn(Collections.singleton(source2));
        when(dataExportService.getDataExportServiceCallType()).thenReturn(serviceCallType);
        stubStatus(createServiceCall, DefaultState.SUCCESSFUL, null);
        when(createServiceCall.getNumber()).thenReturn(CREATE_SERVICE_CALL_ID);
        stubStatus(changeServiceCall, DefaultState.SUCCESSFUL, null);
        when(changeServiceCall.getNumber()).thenReturn(CHANGE_SERVICE_CALL_ID);

        when(threadPrincipalService.getPrincipal()).thenReturn(PRINCIPAL);
    }

    @Test
    public void testSendMultipleData() {
        WebServiceDestinationImpl destination = getDestination(createEndPoint, changeEndPoint);

        DataSendingStatus status = destination.send(ImmutableList.of(newData, updatedData), tagReplacerFactory, logger);

        assertThat(status.isFailed()).isFalse();
        assertThat(status.isFailedForNewData(source1)).isFalse();
        assertThat(status.isFailedForChangedData(source1)).isFalse();
        assertThat(status.isFailedForNewData(source2)).isFalse();
        assertThat(status.isFailedForChangedData(source2)).isFalse();

        verify(threadPrincipalService, times(2)).set(PRINCIPAL); // per each of 2 started threads
        verify(webServiceCreate).call(eq(createEndPoint), dataStreamCaptor.capture(), any(DataExportWebService.ExportContext.class));
        assertThat(dataStreamCaptor.getValue().collect(Collectors.toList())).containsOnly(newData);
        verifyNoMoreInteractions(webServiceCreate);
        verify(webServiceChange).call(eq(changeEndPoint), dataStreamCaptor.capture(), any(DataExportWebService.ExportContext.class));
        assertThat(dataStreamCaptor.getValue().collect(Collectors.toList())).containsOnly(updatedData);
        verifyNoMoreInteractions(webServiceChange);
        verify(serviceCallType).startServiceCallAsync("uuidCr", 1, Collections.singleton(source1));
        verify(serviceCallType).startServiceCallAsync("uuidCh", 2, Collections.singleton(source2));
        verify(serviceCallType, atLeastOnce()).getStatuses(anySetOf(ServiceCall.class));
        verifyNoMoreInteractions(serviceCallType);
    }

    @Test
    public void testSendMultipleDataWithNoTimeout() {
        when(createEndPoint.getProperties()).thenReturn(Collections.emptyList());
        when(changeEndPoint.getProperties()).thenReturn(Collections.emptyList());

        WebServiceDestinationImpl destination = getDestination(createEndPoint, changeEndPoint);
        DataSendingStatus status = destination.send(ImmutableList.of(newData, updatedData), tagReplacerFactory, logger);

        assertThat(status.isFailed()).isFalse();
        assertThat(status.isFailedForNewData(source1)).isFalse();
        assertThat(status.isFailedForChangedData(source1)).isFalse();
        assertThat(status.isFailedForNewData(source2)).isFalse();
        assertThat(status.isFailedForChangedData(source2)).isFalse();

        verify(threadPrincipalService, times(2)).set(PRINCIPAL); // per each of 2 started threads
        verify(webServiceCreate).call(eq(createEndPoint), dataStreamCaptor.capture(), any(DataExportWebService.ExportContext.class));
        assertThat(dataStreamCaptor.getValue().collect(Collectors.toList())).containsOnly(newData);
        verifyNoMoreInteractions(webServiceCreate);
        verify(webServiceChange).call(eq(changeEndPoint), dataStreamCaptor.capture(), any(DataExportWebService.ExportContext.class));
        assertThat(dataStreamCaptor.getValue().collect(Collectors.toList())).containsOnly(updatedData);
        verifyNoMoreInteractions(webServiceChange);
        verify(serviceCallType).startServiceCallAsync("uuidCr", 1, Collections.singleton(source1));
        verify(serviceCallType).startServiceCallAsync("uuidCh", 2, Collections.singleton(source2));
        verify(serviceCallType, atLeastOnce()).getStatuses(Collections.emptySet());
        verifyNoMoreInteractions(serviceCallType);
    }

    @Test
    public void testSendMultipleDataWithZeroTimeout() {
        when(timeout.getValue()).thenReturn(TimeDuration.seconds(0));

        WebServiceDestinationImpl destination = getDestination(createEndPoint, changeEndPoint);
        DataSendingStatus status = destination.send(ImmutableList.of(newData, updatedData), tagReplacerFactory, logger);

        assertThat(status.isFailed()).isFalse();
        assertThat(status.isFailedForNewData(source1)).isFalse();
        assertThat(status.isFailedForChangedData(source1)).isFalse();
        assertThat(status.isFailedForNewData(source2)).isFalse();
        assertThat(status.isFailedForChangedData(source2)).isFalse();

        verify(threadPrincipalService, times(2)).set(PRINCIPAL); // per each of 2 started threads
        verify(webServiceCreate).call(eq(createEndPoint), dataStreamCaptor.capture(), any(DataExportWebService.ExportContext.class));
        assertThat(dataStreamCaptor.getValue().collect(Collectors.toList())).containsOnly(newData);
        verifyNoMoreInteractions(webServiceCreate);
        verify(webServiceChange).call(eq(changeEndPoint), dataStreamCaptor.capture(), any(DataExportWebService.ExportContext.class));
        assertThat(dataStreamCaptor.getValue().collect(Collectors.toList())).containsOnly(updatedData);
        verifyNoMoreInteractions(webServiceChange);
        verify(serviceCallType).startServiceCallAsync("uuidCr", 1, Collections.singleton(source1));
        verify(serviceCallType).startServiceCallAsync("uuidCh", 2, Collections.singleton(source2));
        verify(serviceCallType, atLeastOnce()).getStatuses(Collections.emptySet());
        verifyNoMoreInteractions(serviceCallType);
    }

    @Test
    public void testSendMultipleDataWithoutServiceCalls() {
        doNothing().when(webServiceCreate).call(any(EndPointConfiguration.class), any(), any(DataExportWebService.ExportContext.class));
        doNothing().when(webServiceChange).call(any(EndPointConfiguration.class), any(), any(DataExportWebService.ExportContext.class));

        WebServiceDestinationImpl destination = getDestination(createEndPoint, changeEndPoint);
        DataSendingStatus status = destination.send(ImmutableList.of(newData, updatedData), tagReplacerFactory, logger);

        assertThat(status.isFailed()).isFalse();
        assertThat(status.isFailedForNewData(source1)).isFalse();
        assertThat(status.isFailedForChangedData(source1)).isFalse();
        assertThat(status.isFailedForNewData(source2)).isFalse();
        assertThat(status.isFailedForChangedData(source2)).isFalse();

        verify(threadPrincipalService, times(2)).set(PRINCIPAL); // per each of 2 started threads
        verify(webServiceCreate).call(eq(createEndPoint), dataStreamCaptor.capture(), any(DataExportWebService.ExportContext.class));
        assertThat(dataStreamCaptor.getValue().collect(Collectors.toList())).containsOnly(newData);
        verifyNoMoreInteractions(webServiceCreate);
        verify(webServiceChange).call(eq(changeEndPoint), dataStreamCaptor.capture(), any(DataExportWebService.ExportContext.class));
        assertThat(dataStreamCaptor.getValue().collect(Collectors.toList())).containsOnly(updatedData);
        verifyNoMoreInteractions(webServiceChange);
        verify(serviceCallType, atLeastOnce()).getStatuses(Collections.emptySet());
        verifyNoMoreInteractions(serviceCallType);
    }

    @Test
    public void testSendToOneEndpoint() {
        WebServiceDestinationImpl destination = getDestination(changeEndPoint, changeEndPoint);
        DataSendingStatus status = destination.send(ImmutableList.of(newData, updatedData), tagReplacerFactory, logger);

        assertThat(status.isFailed()).isFalse();
        assertThat(status.isFailedForNewData(source1)).isFalse();
        assertThat(status.isFailedForChangedData(source1)).isFalse();
        assertThat(status.isFailedForNewData(source2)).isFalse();
        assertThat(status.isFailedForChangedData(source2)).isFalse();

        verify(threadPrincipalService, times(2)).set(PRINCIPAL); // per each of 2 started threads
        verify(webServiceChange, times(2)).call(eq(changeEndPoint), dataStreamCaptor.capture(), any(DataExportWebService.ExportContext.class));
        assertThat(dataStreamCaptor.getAllValues().stream().map(stream -> stream.collect(Collectors.toList())).collect(Collectors.toList()))
                .containsOnly(Collections.singletonList(newData), Collections.singletonList(updatedData));
        verifyNoMoreInteractions(webServiceChange);
        verifyZeroInteractions(webServiceCreate);
        verify(serviceCallType, times(2)).startServiceCallAsync("uuidCh", 2, Collections.singleton(source2));
        verify(serviceCallType, atLeastOnce()).getStatuses(anySetOf(ServiceCall.class));
        verifyNoMoreInteractions(serviceCallType);
    }

    @Test
    public void testNoUpdatedData() {
        WebServiceDestinationImpl destination = getDestination(createEndPoint, changeEndPoint);
        DataSendingStatus status = destination.send(ImmutableList.of(newData, newData), tagReplacerFactory, logger);

        assertThat(status.isFailed()).isFalse();
        assertThat(status.isFailedForNewData(source1)).isFalse();
        assertThat(status.isFailedForChangedData(source1)).isFalse();
        assertThat(status.isFailedForNewData(source2)).isFalse();
        assertThat(status.isFailedForChangedData(source2)).isFalse();

        verify(threadPrincipalService, times(1)).set(PRINCIPAL); // per 1 started thread
        verify(webServiceCreate).call(eq(createEndPoint), dataStreamCaptor.capture(), any(DataExportWebService.ExportContext.class));
        assertThat(dataStreamCaptor.getValue().collect(Collectors.toList())).containsOnly(newData, newData);
        verifyNoMoreInteractions(webServiceCreate);
        verifyZeroInteractions(webServiceChange);
        verify(serviceCallType).startServiceCallAsync("uuidCr", 1, Collections.singleton(source1));
        verify(serviceCallType, atLeastOnce()).getStatuses(anySetOf(ServiceCall.class));
        verifyNoMoreInteractions(serviceCallType);
    }

    @Test
    public void testNoNewData() {
        WebServiceDestinationImpl destination = getDestination(createEndPoint, changeEndPoint);
        DataSendingStatus status = destination.send(ImmutableList.of(updatedData, updatedData), tagReplacerFactory, logger);

        assertThat(status.isFailed()).isFalse();
        assertThat(status.isFailedForNewData(source1)).isFalse();
        assertThat(status.isFailedForChangedData(source1)).isFalse();
        assertThat(status.isFailedForNewData(source2)).isFalse();
        assertThat(status.isFailedForChangedData(source2)).isFalse();

        verify(threadPrincipalService, times(1)).set(PRINCIPAL); // per 1 started thread
        verify(webServiceChange).call(eq(changeEndPoint), dataStreamCaptor.capture(), any(DataExportWebService.ExportContext.class));
        assertThat(dataStreamCaptor.getValue().collect(Collectors.toList())).containsOnly(updatedData, updatedData);
        verifyNoMoreInteractions(webServiceChange);
        verifyZeroInteractions(webServiceCreate);
        verify(serviceCallType).startServiceCallAsync("uuidCh", 2, Collections.singleton(source2));
        verify(serviceCallType, atLeastOnce()).getStatuses(anySetOf(ServiceCall.class));
        verifyNoMoreInteractions(serviceCallType);
    }

    @Test
    public void testExportOnlyNewData() {
        WebServiceDestinationImpl destination = getDestination(createEndPoint, null);
        DataSendingStatus status = destination.send(ImmutableList.of(newData, newData), tagReplacerFactory, logger);

        assertThat(status.isFailed()).isFalse();
        assertThat(status.isFailedForNewData(source1)).isFalse();
        assertThat(status.isFailedForChangedData(source1)).isFalse();
        assertThat(status.isFailedForNewData(source2)).isFalse();
        assertThat(status.isFailedForChangedData(source2)).isFalse();

        verify(threadPrincipalService, times(1)).set(PRINCIPAL); // per 1 started thread
        verify(webServiceCreate).call(eq(createEndPoint), dataStreamCaptor.capture(), any(DataExportWebService.ExportContext.class));
        assertThat(dataStreamCaptor.getValue().collect(Collectors.toList())).containsOnly(newData, newData);
        verifyNoMoreInteractions(webServiceCreate);
        verifyZeroInteractions(webServiceChange);
        verify(serviceCallType).startServiceCallAsync("uuidCr", 1, Collections.singleton(source1));
        verify(serviceCallType, atLeastOnce()).getStatuses(anySetOf(ServiceCall.class));
        verifyNoMoreInteractions(serviceCallType);
    }

    @Test
    public void testOngoingServiceCall() {
        stubStatus(createServiceCall, DefaultState.ONGOING, null);

        WebServiceDestinationImpl destination = getDestination(createEndPoint, changeEndPoint);
        DataSendingStatus status = destination.send(ImmutableList.of(newData, updatedData), tagReplacerFactory, logger);

        assertThat(status.isFailed()).isTrue();
        assertThat(status.isFailedForNewData(source1)).isTrue();
        assertThat(status.isFailedForChangedData(source1)).isFalse();
        assertThat(status.isFailedForNewData(source2)).isFalse();
        assertThat(status.isFailedForChangedData(source2)).isFalse();

        verify(logger).severe("Data export via web service isn't confirmed for service call " + CREATE_SERVICE_CALL_ID +
                ": No data export confirmation has been received in the configured timeout.");
        verify(threadPrincipalService, times(2)).set(PRINCIPAL); // per each of 2 started threads
        verify(webServiceCreate).call(eq(createEndPoint), dataStreamCaptor.capture(), any(DataExportWebService.ExportContext.class));
        assertThat(dataStreamCaptor.getValue().collect(Collectors.toList())).containsOnly(newData);
        verifyNoMoreInteractions(webServiceCreate);
        verify(webServiceChange).call(eq(changeEndPoint), dataStreamCaptor.capture(), any(DataExportWebService.ExportContext.class));
        assertThat(dataStreamCaptor.getValue().collect(Collectors.toList())).containsOnly(updatedData);
        verifyNoMoreInteractions(webServiceChange);
        verify(serviceCallType).startServiceCallAsync("uuidCr", 1, Collections.singleton(source1));
        verify(serviceCallType).startServiceCallAsync("uuidCh", 2, Collections.singleton(source2));
        verify(serviceCallType, atLeastOnce()).getStatuses(anySetOf(ServiceCall.class));
        verify(serviceCallType).getDataSources(Collections.singleton(createServiceCall));
        verifyNoMoreInteractions(serviceCallType);
    }

    @Test
    public void testErrorInServiceCall() {
        stubStatus(changeServiceCall, DefaultState.FAILED, "Error!");

        WebServiceDestinationImpl destination = getDestination(createEndPoint, changeEndPoint);
        DataSendingStatus status = destination.send(ImmutableList.of(newData, updatedData), tagReplacerFactory, logger);

        assertThat(status.isFailed()).isTrue();
        assertThat(status.isFailedForNewData(source1)).isFalse();
        assertThat(status.isFailedForChangedData(source1)).isFalse();
        assertThat(status.isFailedForNewData(source2)).isFalse();
        assertThat(status.isFailedForChangedData(source2)).isTrue();

        verify(logger).severe("Data export via web service isn't confirmed for service call " + CHANGE_SERVICE_CALL_ID +
                ": Error!");
        verify(threadPrincipalService, times(2)).set(PRINCIPAL); // per each of 2 started threads
        verify(webServiceCreate).call(eq(createEndPoint), dataStreamCaptor.capture(), any(DataExportWebService.ExportContext.class));
        assertThat(dataStreamCaptor.getValue().collect(Collectors.toList())).containsOnly(newData);
        verifyNoMoreInteractions(webServiceCreate);
        verify(webServiceChange).call(eq(changeEndPoint), dataStreamCaptor.capture(), any(DataExportWebService.ExportContext.class));
        assertThat(dataStreamCaptor.getValue().collect(Collectors.toList())).containsOnly(updatedData);
        verifyNoMoreInteractions(webServiceChange);
        verify(serviceCallType).startServiceCallAsync("uuidCr", 1, Collections.singleton(source1));
        verify(serviceCallType).startServiceCallAsync("uuidCh", 2, Collections.singleton(source2));
        verify(serviceCallType, atLeastOnce()).getStatuses(anySetOf(ServiceCall.class));
        verify(serviceCallType).getDataSources(Collections.singleton(changeServiceCall));
        verifyNoMoreInteractions(serviceCallType);
    }

    @Test
    public void testBothServiceCallsFailed() {
        stubStatus(createServiceCall, DefaultState.FAILED, "Error!");
        stubStatus(changeServiceCall, DefaultState.FAILED, "Failure!");

        WebServiceDestinationImpl destination = getDestination(createEndPoint, changeEndPoint);
        DataSendingStatus status = destination.send(ImmutableList.of(newData, updatedData), tagReplacerFactory, logger);

        assertThat(status.isFailed()).isTrue();
        assertThat(status.isFailedForNewData(source1)).isTrue();
        assertThat(status.isFailedForChangedData(source1)).isFalse();
        assertThat(status.isFailedForNewData(source2)).isFalse();
        assertThat(status.isFailedForChangedData(source2)).isTrue();

        verify(logger).severe("Data export via web service isn't confirmed for service call " + CREATE_SERVICE_CALL_ID +
                ": Error!");
        verify(logger).severe("Data export via web service isn't confirmed for service call " + CHANGE_SERVICE_CALL_ID +
                ": Failure!");
        verify(threadPrincipalService, times(2)).set(PRINCIPAL); // per each of 2 started threads
        verify(webServiceCreate).call(eq(createEndPoint), dataStreamCaptor.capture(), any(DataExportWebService.ExportContext.class));
        assertThat(dataStreamCaptor.getValue().collect(Collectors.toList())).containsOnly(newData);
        verifyNoMoreInteractions(webServiceCreate);
        verify(webServiceChange).call(eq(changeEndPoint), dataStreamCaptor.capture(), any(DataExportWebService.ExportContext.class));
        assertThat(dataStreamCaptor.getValue().collect(Collectors.toList())).containsOnly(updatedData);
        verifyNoMoreInteractions(webServiceChange);
        verify(serviceCallType).startServiceCallAsync("uuidCr", 1, Collections.singleton(source1));
        verify(serviceCallType).startServiceCallAsync("uuidCh", 2, Collections.singleton(source2));
        verify(serviceCallType, atLeastOnce()).getStatuses(anySetOf(ServiceCall.class));
        verify(serviceCallType).getDataSources(Collections.singleton(createServiceCall));
        verify(serviceCallType).getDataSources(Collections.singleton(changeServiceCall));
        verifyNoMoreInteractions(serviceCallType);
    }

    @Test
    public void testErrorInServiceCallWithNoMessage() {
        stubStatus(createServiceCall, DefaultState.FAILED, null);

        WebServiceDestinationImpl destination = getDestination(createEndPoint, changeEndPoint);
        DataSendingStatus status = destination.send(ImmutableList.of(newData, updatedData), tagReplacerFactory, logger);

        assertThat(status.isFailed()).isTrue();
        assertThat(status.isFailedForNewData(source1)).isTrue();
        assertThat(status.isFailedForChangedData(source1)).isFalse();
        assertThat(status.isFailedForNewData(source2)).isFalse();
        assertThat(status.isFailedForChangedData(source2)).isFalse();

        verify(logger).severe("Data export via web service isn't confirmed for service call " + CREATE_SERVICE_CALL_ID +
                ": Received error code, but no error has been provided.");
        verify(threadPrincipalService, times(2)).set(PRINCIPAL); // per each of 2 started threads
        verify(webServiceCreate).call(eq(createEndPoint), dataStreamCaptor.capture(), any(DataExportWebService.ExportContext.class));
        assertThat(dataStreamCaptor.getValue().collect(Collectors.toList())).containsOnly(newData);
        verifyNoMoreInteractions(webServiceCreate);
        verify(webServiceChange).call(eq(changeEndPoint), dataStreamCaptor.capture(), any(DataExportWebService.ExportContext.class));
        assertThat(dataStreamCaptor.getValue().collect(Collectors.toList())).containsOnly(updatedData);
        verifyNoMoreInteractions(webServiceChange);
        verify(serviceCallType).startServiceCallAsync("uuidCr", 1, Collections.singleton(source1));
        verify(serviceCallType).startServiceCallAsync("uuidCh", 2, Collections.singleton(source2));
        verify(serviceCallType, atLeastOnce()).getStatuses(anySetOf(ServiceCall.class));
        verify(serviceCallType).getDataSources(Collections.singleton(createServiceCall));
        verifyNoMoreInteractions(serviceCallType);
    }

    @Test
    public void testUnknownStateOfServiceCall() {
        stubStatus(changeServiceCall, DefaultState.REJECTED, null);

        WebServiceDestinationImpl destination = getDestination(createEndPoint, changeEndPoint);
        DataSendingStatus status = destination.send(ImmutableList.of(newData, updatedData), tagReplacerFactory, logger);

        assertThat(status.isFailed()).isTrue();
        assertThat(status.isFailedForNewData(source1)).isFalse();
        assertThat(status.isFailedForChangedData(source1)).isFalse();
        assertThat(status.isFailedForNewData(source2)).isFalse();
        assertThat(status.isFailedForChangedData(source2)).isTrue();

        verify(logger).severe("Data export via web service isn't confirmed for service call " + CHANGE_SERVICE_CALL_ID +
                ": Unexpected state of the service call: REJECTED.");
        verify(threadPrincipalService, times(2)).set(PRINCIPAL); // per each of 2 started threads
        verify(webServiceCreate).call(eq(createEndPoint), dataStreamCaptor.capture(), any(DataExportWebService.ExportContext.class));
        assertThat(dataStreamCaptor.getValue().collect(Collectors.toList())).containsOnly(newData);
        verifyNoMoreInteractions(webServiceCreate);
        verify(webServiceChange).call(eq(changeEndPoint), dataStreamCaptor.capture(), any(DataExportWebService.ExportContext.class));
        assertThat(dataStreamCaptor.getValue().collect(Collectors.toList())).containsOnly(updatedData);
        verifyNoMoreInteractions(webServiceChange);
        verify(serviceCallType).startServiceCallAsync("uuidCr", 1, Collections.singleton(source1));
        verify(serviceCallType).startServiceCallAsync("uuidCh", 2, Collections.singleton(source2));
        verify(serviceCallType, atLeastOnce()).getStatuses(anySetOf(ServiceCall.class));
        verify(serviceCallType).getDataSources(Collections.singleton(changeServiceCall));
        verifyNoMoreInteractions(serviceCallType);
    }

    @Test
    public void testManyServiceCalls() {
        ServiceCall createServiceCall1 = mock(ServiceCall.class);
        ServiceCall changeServiceCall1 = mock(ServiceCall.class);
        stubStatus(createServiceCall, DefaultState.FAILED, "Error!");
        stubStatus(createServiceCall1, DefaultState.SUCCESSFUL, null);
        stubStatus(changeServiceCall, DefaultState.ONGOING, null);
        stubStatus(changeServiceCall1, DefaultState.SUCCESSFUL, null);
        ReadingTypeDataExportItem source3 = mock(ReadingTypeDataExportItem.class);
        ReadingTypeDataExportItem source4 = mock(ReadingTypeDataExportItem.class);
        when(serviceCallType.getDataSources(Collections.singleton(createServiceCall))).thenReturn(ImmutableSet.of(source1, source3, source4));
        when(serviceCallType.getDataSources(Collections.singleton(createServiceCall1))).thenReturn(ImmutableSet.of(source2));
        when(serviceCallType.getDataSources(Collections.singleton(changeServiceCall))).thenReturn(ImmutableSet.of(source1));
        when(serviceCallType.getDataSources(Collections.singleton(changeServiceCall1))).thenReturn(ImmutableSet.of(source2, source4));
        when(serviceCallType.startServiceCallAsync("uuidCr1", 3, Collections.singleton(source2))).thenReturn(createServiceCall1);
        doAnswer(invocation -> {
            DataExportWebService.ExportContext context = invocation.getArgumentAt(2, DataExportWebService.ExportContext.class);
            context.startAndRegisterServiceCall("uuidCr", 1, ImmutableSet.of(source1, source3, source4));
            context.startAndRegisterServiceCall("uuidCr1", 3, Collections.singleton(source2));
            return null;
        })
                .when(webServiceCreate).call(any(EndPointConfiguration.class), any(), any(DataExportWebService.ExportContext.class));
        when(serviceCallType.startServiceCallAsync("uuidCh1", 4, ImmutableSet.of(source2, source4))).thenReturn(changeServiceCall1);
        doAnswer(invocation -> {
            DataExportWebService.ExportContext context = invocation.getArgumentAt(2, DataExportWebService.ExportContext.class);
            context.startAndRegisterServiceCall("uuidCh", 2, Collections.singleton(source1));
            context.startAndRegisterServiceCall("uuidCh1", 4, ImmutableSet.of(source2, source4));
            return null;
        })
                .when(webServiceChange).call(any(EndPointConfiguration.class), any(), any(DataExportWebService.ExportContext.class));

        WebServiceDestinationImpl destination = getDestination(createEndPoint, changeEndPoint);
        ExportData newData2 = createData(source2, false);
        ExportData newData3 = createData(source3, false);
        ExportData newData4 = createData(source4, false);
        ExportData updatedData1 = createData(source1, true);
        ExportData updatedData4 = createData(source4, true);
        List<ExportData> dataList = ImmutableList.of(updatedData4, updatedData1, newData, updatedData, newData4, newData2, newData3);
        DataSendingStatus status = destination.send(dataList, tagReplacerFactory, logger);

        assertThat(status.isFailed()).isTrue();
        assertThat(status.isFailedForNewData(source1)).isTrue();
        assertThat(status.isFailedForChangedData(source1)).isTrue();
        assertThat(status.isFailedForNewData(source2)).isFalse();
        assertThat(status.isFailedForChangedData(source2)).isFalse();
        assertThat(status.isFailedForNewData(source3)).isTrue();
        assertThat(status.isFailedForChangedData(source3)).isFalse();
        assertThat(status.isFailedForNewData(source4)).isTrue();
        assertThat(status.isFailedForChangedData(source4)).isFalse();

        verify(logger).severe("Data export via web service isn't confirmed for service call " + CREATE_SERVICE_CALL_ID +
                ": Error!");
        verify(logger).severe("Data export via web service isn't confirmed for service call " + CHANGE_SERVICE_CALL_ID +
                ": No data export confirmation has been received in the configured timeout.");
        verify(threadPrincipalService, times(2)).set(PRINCIPAL); // per each of 2 started threads
        verify(webServiceCreate).call(eq(createEndPoint), dataStreamCaptor.capture(), any(DataExportWebService.ExportContext.class));
        assertThat(dataStreamCaptor.getValue().collect(Collectors.toList())).containsOnly(newData, newData2, newData3, newData4);
        verifyNoMoreInteractions(webServiceCreate);
        verify(webServiceChange).call(eq(changeEndPoint), dataStreamCaptor.capture(), any(DataExportWebService.ExportContext.class));
        assertThat(dataStreamCaptor.getValue().collect(Collectors.toList())).containsOnly(updatedData, updatedData1, updatedData4);
        verifyNoMoreInteractions(webServiceChange);
        verify(serviceCallType).startServiceCallAsync("uuidCr", 1, ImmutableSet.of(source1, source3, source4));
        verify(serviceCallType).startServiceCallAsync("uuidCh", 2, Collections.singleton(source1));
        verify(serviceCallType).startServiceCallAsync("uuidCr1", 3, Collections.singleton(source2));
        verify(serviceCallType).startServiceCallAsync("uuidCh1", 4, ImmutableSet.of(source2, source4));
        verify(serviceCallType, atLeastOnce()).getStatuses(anySetOf(ServiceCall.class));
        verify(serviceCallType).getDataSources(Collections.singleton(createServiceCall)); // per failed created data
        verify(serviceCallType).getDataSources(Collections.singleton(changeServiceCall)); // per failed changed data
        verifyNoMoreInteractions(serviceCallType);
    }

    @Test
    public void testWebServiceIsNotFound() {
        when(dataExportService.getExportWebService(WEB_SERVICE_CHANGE)).thenReturn(Optional.empty());
        when(changeEndPoint.getName()).thenReturn(WEB_SERVICE_CHANGE);

        WebServiceDestinationImpl destination = getDestination(createEndPoint, changeEndPoint);
        assertThatThrownBy(() -> destination.send(ImmutableList.of(newData, updatedData), tagReplacerFactory, logger))
                .isInstanceOf(DestinationFailedException.class)
                .hasMessage("No data export web service is found for endpoint '" + WEB_SERVICE_CHANGE + "'.");

        verifyZeroInteractions(threadPrincipalService);
        verifyZeroInteractions(webServiceCreate);
        verifyZeroInteractions(webServiceChange);
        verifyZeroInteractions(serviceCallType);
    }

    @Test
    public void testExceptionFromWebService() {
        doThrow(new UnsupportedOperationException("Exception!"))
                .when(webServiceChange).call(any(EndPointConfiguration.class), any(), any(DataExportWebService.ExportContext.class));

        WebServiceDestinationImpl destination = getDestination(createEndPoint, changeEndPoint);
        assertThatThrownBy(() -> destination.send(ImmutableList.of(newData, updatedData), tagReplacerFactory, logger))
                .isInstanceOf(DestinationFailedException.class)
                .hasMessage("Failure while exporting data via web service: Exception!");

        verify(threadPrincipalService, times(2)).set(PRINCIPAL); // per each of 2 started threads
        verify(webServiceCreate).call(eq(createEndPoint), dataStreamCaptor.capture(), any(DataExportWebService.ExportContext.class));
        assertThat(dataStreamCaptor.getValue().collect(Collectors.toList())).containsOnly(newData);
        verifyNoMoreInteractions(webServiceCreate);
        verify(webServiceChange).call(eq(changeEndPoint), dataStreamCaptor.capture(), any(DataExportWebService.ExportContext.class));
        assertThat(dataStreamCaptor.getValue().collect(Collectors.toList())).containsOnly(updatedData);
        verifyNoMoreInteractions(webServiceChange);
        verify(serviceCallType, never()).getStatus(changeServiceCall);
        verify(serviceCallType, never()).getStatuses(anyListContaining(changeServiceCall));
        verify(serviceCallType, never()).tryFailingServiceCall(any(ServiceCall.class), anyString());
        verify(serviceCallType, never()).tryPassingServiceCall(any(ServiceCall.class));
    }

    @Test
    public void testMultipleDataNotFullySentDueToTimeout() {
        doAnswer(invocation -> {
            Thread.sleep(WebServiceDataExportServiceCallHandler.CHECK_PAUSE_IN_SECONDS * 1000);
            invocation.getArgumentAt(2, DataExportWebService.ExportContext.class).startAndRegisterServiceCall("uuidCr", 1, Collections.singleton(source1));
            return null;
        })
                .when(webServiceCreate).call(any(EndPointConfiguration.class), any(), any(DataExportWebService.ExportContext.class));
        WebServiceDestinationImpl destination = getDestination(createEndPoint, changeEndPoint);

        DataSendingStatus status = destination.send(ImmutableList.of(newData, updatedData), tagReplacerFactory, logger);

        assertThat(status.isFailed()).isTrue();
        assertThat(status.isFailedForNewData(source1)).isTrue();
        assertThat(status.isFailedForChangedData(source1)).isFalse();
        assertThat(status.isFailedForNewData(source2)).isFalse();
        assertThat(status.isFailedForChangedData(source2)).isFalse();

        verify(threadPrincipalService, times(2)).set(PRINCIPAL); // per each of 2 started threads
        verify(webServiceCreate).call(eq(createEndPoint), dataStreamCaptor.capture(), any(DataExportWebService.ExportContext.class));
        assertThat(dataStreamCaptor.getValue().collect(Collectors.toList())).containsOnly(newData);
        verifyNoMoreInteractions(webServiceCreate);
        verify(webServiceChange).call(eq(changeEndPoint), dataStreamCaptor.capture(), any(DataExportWebService.ExportContext.class));
        assertThat(dataStreamCaptor.getValue().collect(Collectors.toList())).containsOnly(updatedData);
        verifyNoMoreInteractions(webServiceChange);
        verify(serviceCallType).startServiceCallAsync("uuidCh", 2, Collections.singleton(source2));
        verify(serviceCallType, atLeastOnce()).getStatuses(anySetOf(ServiceCall.class));
        verify(serviceCallType).getDataSources(Collections.emptySet()); // per passed service calls for created data to find untracked data sources
        verifyNoMoreInteractions(serviceCallType);
    }

    @Test
    public void testManyServiceCallsWithDataNotFullySentDueToTimeout() {
        ServiceCall createServiceCall1 = mock(ServiceCall.class);
        ServiceCall changeServiceCall1 = mock(ServiceCall.class);
        stubStatus(createServiceCall, DefaultState.SUCCESSFUL, null);
        stubStatus(createServiceCall1, DefaultState.SUCCESSFUL, null);
        stubStatus(changeServiceCall, DefaultState.SUCCESSFUL, null);
        stubStatus(changeServiceCall1, DefaultState.SUCCESSFUL, null);
        ReadingTypeDataExportItem source3 = mock(ReadingTypeDataExportItem.class);
        ReadingTypeDataExportItem source4 = mock(ReadingTypeDataExportItem.class);
        when(serviceCallType.getDataSources(Collections.singleton(createServiceCall))).thenReturn(Sets.newHashSet(source1, source3, source4));
        when(serviceCallType.getDataSources(Collections.singleton(createServiceCall1))).thenReturn(Sets.newHashSet(source2));
        when(serviceCallType.getDataSources(Collections.singleton(changeServiceCall))).thenReturn(Sets.newHashSet(source1));
        when(serviceCallType.getDataSources(Collections.singleton(changeServiceCall1))).thenReturn(Sets.newHashSet(source2, source4));
        when(serviceCallType.startServiceCallAsync("uuidCr1", 3, Collections.singleton(source2))).thenReturn(createServiceCall1);
        doAnswer(invocation -> {
            DataExportWebService.ExportContext context = invocation.getArgumentAt(2, DataExportWebService.ExportContext.class);
            context.startAndRegisterServiceCall("uuidCr", 1, ImmutableSet.of(source1, source3, source4));
            context.startAndRegisterServiceCall("uuidCr1", 3, Collections.singleton(source2));
            return null;
        })
                .when(webServiceCreate).call(any(EndPointConfiguration.class), any(), any(DataExportWebService.ExportContext.class));
        when(serviceCallType.startServiceCallAsync("uuidCh1", 4, ImmutableSet.of(source2, source4))).thenReturn(changeServiceCall1);
        doAnswer(invocation -> {
            DataExportWebService.ExportContext context = invocation.getArgumentAt(2, DataExportWebService.ExportContext.class);
            context.startAndRegisterServiceCall("uuidCh", 2, Collections.singleton(source1));
            Thread.sleep(WebServiceDataExportServiceCallHandler.CHECK_PAUSE_IN_SECONDS * 1000);
            context.startAndRegisterServiceCall("uuidCh1", 4, ImmutableSet.of(source2, source4));
            return null;
        })
                .when(webServiceChange).call(any(EndPointConfiguration.class), any(), any(DataExportWebService.ExportContext.class));

        WebServiceDestinationImpl destination = getDestination(createEndPoint, changeEndPoint);
        ExportData newData2 = createData(source2, false);
        ExportData newData3 = createData(source3, false);
        ExportData newData4 = createData(source4, false);
        ExportData updatedData1 = createData(source1, true);
        ExportData updatedData4 = createData(source4, true);
        List<ExportData> dataList = ImmutableList.of(updatedData4, updatedData1, newData, updatedData, newData4, newData2, newData3);
        DataSendingStatus status = destination.send(dataList, tagReplacerFactory, logger);

        assertThat(status.isFailed()).isTrue();
        assertThat(status.isFailedForNewData(source1)).isFalse();
        assertThat(status.isFailedForChangedData(source1)).isFalse();
        assertThat(status.isFailedForNewData(source2)).isFalse();
        assertThat(status.isFailedForChangedData(source2)).isTrue();
        assertThat(status.isFailedForNewData(source3)).isFalse();
        assertThat(status.isFailedForChangedData(source3)).isFalse();
        assertThat(status.isFailedForNewData(source4)).isFalse();
        assertThat(status.isFailedForChangedData(source4)).isTrue();

        verify(threadPrincipalService, times(2)).set(PRINCIPAL); // per each of 2 started threads
        verify(webServiceCreate).call(eq(createEndPoint), dataStreamCaptor.capture(), any(DataExportWebService.ExportContext.class));
        assertThat(dataStreamCaptor.getValue().collect(Collectors.toList())).containsOnly(newData, newData2, newData3, newData4);
        verifyNoMoreInteractions(webServiceCreate);
        verify(webServiceChange).call(eq(changeEndPoint), dataStreamCaptor.capture(), any(DataExportWebService.ExportContext.class));
        assertThat(dataStreamCaptor.getValue().collect(Collectors.toList())).containsOnly(updatedData, updatedData1, updatedData4);
        verifyNoMoreInteractions(webServiceChange);
        verify(serviceCallType).startServiceCallAsync("uuidCr", 1, ImmutableSet.of(source1, source3, source4));
        verify(serviceCallType).startServiceCallAsync("uuidCh", 2, Collections.singleton(source1));
        verify(serviceCallType).startServiceCallAsync("uuidCr1", 3, Collections.singleton(source2));
        verify(serviceCallType, atLeastOnce()).getStatuses(anySetOf(ServiceCall.class));
        verify(serviceCallType).getDataSources(Collections.singleton(changeServiceCall)); // per passed changed data, called to find out untracked data sources
        verifyNoMoreInteractions(serviceCallType);
    }

    private void stubStatus(ServiceCall serviceCall, DefaultState status, String error) {
        ServiceCallStatus result = mock(ServiceCallStatus.class);
        when(result.getServiceCall()).thenReturn(serviceCall);
        when(result.getErrorMessage()).thenReturn(Optional.ofNullable(error));
        when(result.getState()).thenReturn(status);
        when(result.isFailed()).thenReturn(status == DefaultState.FAILED);
        when(result.isSuccessful()).thenReturn(status == DefaultState.SUCCESSFUL);
        when(result.isOpen()).thenReturn(status.isOpen());
        when(serviceCallType.getStatus(serviceCall)).thenReturn(result);
        serviceCallStatuses.put(serviceCall, result);
    }

    private WebServiceDestinationImpl getDestination(EndPointConfiguration createEndPoint, EndPointConfiguration changeEndPoint) {
        WebServiceDestinationImpl destination = new WebServiceDestinationImpl(dataModel, clock, thesaurus, dataExportService, fileSystem, transactionService, threadPrincipalService);
        return destination.init(exportTask, createEndPoint, changeEndPoint);
    }

    private MeterReadingData createData(ReadingTypeDataExportItem dataSource, boolean updated) {
        MeterReadingValidationData validationData = new MeterReadingValidationData(Collections.emptyMap());
        return new MeterReadingData(dataSource, meterReading,
                validationData, Collections.emptyMap(), DefaultStructureMarker.createRoot(clock, updated ? "update" : "create"));
    }
}
