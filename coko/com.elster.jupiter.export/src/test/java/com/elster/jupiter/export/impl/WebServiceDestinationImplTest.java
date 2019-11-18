/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.devtools.persistence.test.TransactionVerifier;
import com.elster.jupiter.export.DataExportWebService;
import com.elster.jupiter.export.ExportData;
import com.elster.jupiter.export.ReadingTypeDataExportItem;
import com.elster.jupiter.export.impl.webservicecall.WebServiceDataExportServiceCallHandler;
import com.elster.jupiter.export.webservicecall.DataExportServiceCallType;
import com.elster.jupiter.export.webservicecall.ServiceCallStatus;
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

import java.nio.file.FileSystem;
import java.security.Principal;
import java.time.Clock;
import java.util.Collections;
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
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
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
    @Mock
    private ExportData newData, updatedData;
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

    @Before
    public void setUp() {
        when(newData.getStructureMarker()).thenReturn(DefaultStructureMarker.createRoot(clock, "create"));
        when(updatedData.getStructureMarker()).thenReturn(DefaultStructureMarker.createRoot(clock, "update"));

        when(createEndPoint.getProperties()).thenReturn(Collections.singletonList(timeout));
        when(changeEndPoint.getProperties()).thenReturn(Collections.singletonList(timeout));
        when(timeout.getName()).thenReturn(DataExportWebService.TIMEOUT_PROPERTY_KEY);
        when(timeout.getValue()).thenReturn(TimeDuration.seconds((int) (.5 * WebServiceDataExportServiceCallHandler.CHECK_PAUSE_IN_SECONDS)));

        when(createEndPoint.getWebServiceName()).thenReturn(WEB_SERVICE_CREATE);
        when(changeEndPoint.getWebServiceName()).thenReturn(WEB_SERVICE_CHANGE);
        when(dataExportService.getExportWebService(WEB_SERVICE_CREATE)).thenReturn(Optional.of(webServiceCreate));
        when(dataExportService.getExportWebService(WEB_SERVICE_CHANGE)).thenReturn(Optional.of(webServiceChange));

        when(webServiceCreate.call(any(EndPointConfiguration.class), any())).thenReturn(Collections.singletonList(createServiceCall));
        when(webServiceChange.call(any(EndPointConfiguration.class), any())).thenReturn(Collections.singletonList(changeServiceCall));
        when(dataExportService.getDataExportServiceCallType()).thenReturn(serviceCallType);
        stubStatus(createServiceCall, DefaultState.SUCCESSFUL, null);
        when(createServiceCall.getNumber()).thenReturn(CREATE_SERVICE_CALL_ID);
        stubStatus(changeServiceCall, DefaultState.SUCCESSFUL, null);
        when(changeServiceCall.getNumber()).thenReturn(CHANGE_SERVICE_CALL_ID);
        when(serviceCallType.getDataSources(createServiceCall)).thenReturn(Collections.singleton(source1));
        when(serviceCallType.getDataSources(changeServiceCall)).thenReturn(Collections.singleton(source2));

        when(threadPrincipalService.getPrincipal()).thenReturn(PRINCIPAL);
    }

    @Test
    public void testSendMultipleData() {
        WebServiceDestinationImpl destination = getDestination(createEndPoint, changeEndPoint);

        DataSendingStatus status = destination.send(ImmutableList.of(newData, updatedData), tagReplacerFactory, logger);

        assertThat(status.isFailed()).isFalse();
        assertThat(status.isFailed(source1)).isFalse();
        assertThat(status.isFailed(source2)).isFalse();

        verify(threadPrincipalService, times(2)).set(PRINCIPAL); // per each of 2 started threads
        verify(webServiceCreate).call(eq(createEndPoint), dataStreamCaptor.capture());
        assertThat(dataStreamCaptor.getValue().collect(Collectors.toList())).containsOnly(newData);
        verifyNoMoreInteractions(webServiceCreate);
        verify(webServiceChange).call(eq(changeEndPoint), dataStreamCaptor.capture());
        assertThat(dataStreamCaptor.getValue().collect(Collectors.toList())).containsOnly(updatedData);
        verifyNoMoreInteractions(webServiceChange);
        verify(serviceCallType).getStatuses(Collections.singletonList(createServiceCall));
        verify(serviceCallType).getStatuses(Collections.singletonList(changeServiceCall));
        verifyNoMoreInteractions(serviceCallType);
    }

    @Test
    public void testSendMultipleDataWithNoTimeout() {
        when(createEndPoint.getProperties()).thenReturn(Collections.emptyList());
        when(changeEndPoint.getProperties()).thenReturn(Collections.emptyList());

        WebServiceDestinationImpl destination = getDestination(createEndPoint, changeEndPoint);
        DataSendingStatus status = destination.send(ImmutableList.of(newData, updatedData), tagReplacerFactory, logger);

        assertThat(status.isFailed()).isFalse();
        assertThat(status.isFailed(source1)).isFalse();
        assertThat(status.isFailed(source2)).isFalse();

        verify(threadPrincipalService, times(2)).set(PRINCIPAL); // per each of 2 started threads
        verify(webServiceCreate).call(eq(createEndPoint), dataStreamCaptor.capture());
        assertThat(dataStreamCaptor.getValue().collect(Collectors.toList())).containsOnly(newData);
        verifyNoMoreInteractions(webServiceCreate);
        verify(webServiceChange).call(eq(changeEndPoint), dataStreamCaptor.capture());
        assertThat(dataStreamCaptor.getValue().collect(Collectors.toList())).containsOnly(updatedData);
        verifyNoMoreInteractions(webServiceChange);
        verifyZeroInteractions(serviceCallType);
    }

    @Test
    public void testSendMultipleDataWithZeroTimeout() {
        when(timeout.getValue()).thenReturn(TimeDuration.seconds(0));

        WebServiceDestinationImpl destination = getDestination(createEndPoint, changeEndPoint);
        DataSendingStatus status = destination.send(ImmutableList.of(newData, updatedData), tagReplacerFactory, logger);

        assertThat(status.isFailed()).isFalse();
        assertThat(status.isFailed(source1)).isFalse();
        assertThat(status.isFailed(source2)).isFalse();

        verify(threadPrincipalService, times(2)).set(PRINCIPAL); // per each of 2 started threads
        verify(webServiceCreate).call(eq(createEndPoint), dataStreamCaptor.capture());
        assertThat(dataStreamCaptor.getValue().collect(Collectors.toList())).containsOnly(newData);
        verifyNoMoreInteractions(webServiceCreate);
        verify(webServiceChange).call(eq(changeEndPoint), dataStreamCaptor.capture());
        assertThat(dataStreamCaptor.getValue().collect(Collectors.toList())).containsOnly(updatedData);
        verifyNoMoreInteractions(webServiceChange);
        verifyZeroInteractions(serviceCallType);
    }

    @Test
    public void testSendMultipleDataWithoutServiceCalls() {
        when(webServiceCreate.call(any(EndPointConfiguration.class), any())).thenReturn(Collections.emptyList());
        when(webServiceChange.call(any(EndPointConfiguration.class), any())).thenReturn(Collections.emptyList());

        WebServiceDestinationImpl destination = getDestination(createEndPoint, changeEndPoint);
        DataSendingStatus status = destination.send(ImmutableList.of(newData, updatedData), tagReplacerFactory, logger);

        assertThat(status.isFailed()).isFalse();
        assertThat(status.isFailed(source1)).isFalse();
        assertThat(status.isFailed(source2)).isFalse();

        verify(threadPrincipalService, times(2)).set(PRINCIPAL); // per each of 2 started threads
        verify(webServiceCreate).call(eq(createEndPoint), dataStreamCaptor.capture());
        assertThat(dataStreamCaptor.getValue().collect(Collectors.toList())).containsOnly(newData);
        verifyNoMoreInteractions(webServiceCreate);
        verify(webServiceChange).call(eq(changeEndPoint), dataStreamCaptor.capture());
        assertThat(dataStreamCaptor.getValue().collect(Collectors.toList())).containsOnly(updatedData);
        verifyNoMoreInteractions(webServiceChange);
        verifyZeroInteractions(serviceCallType);
    }

    @Test
    public void testSendToOneEndpoint() {
        WebServiceDestinationImpl destination = getDestination(changeEndPoint, changeEndPoint);
        DataSendingStatus status = destination.send(ImmutableList.of(newData, updatedData), tagReplacerFactory, logger);

        assertThat(status.isFailed()).isFalse();
        assertThat(status.isFailed(source1)).isFalse();
        assertThat(status.isFailed(source2)).isFalse();

        verify(threadPrincipalService, times(2)).set(PRINCIPAL); // per each of 2 started threads
        verify(webServiceChange, times(2)).call(eq(changeEndPoint), dataStreamCaptor.capture());
        assertThat(dataStreamCaptor.getAllValues().stream().map(stream -> stream.collect(Collectors.toList())).collect(Collectors.toList()))
                .containsOnly(Collections.singletonList(newData), Collections.singletonList(updatedData));
        verifyNoMoreInteractions(webServiceChange);
        verifyZeroInteractions(webServiceCreate);
        verify(serviceCallType, times(2)).getStatuses(Collections.singletonList(changeServiceCall));
        verifyNoMoreInteractions(serviceCallType);
    }

    @Test
    public void testNoUpdatedData() {
        WebServiceDestinationImpl destination = getDestination(createEndPoint, changeEndPoint);
        DataSendingStatus status = destination.send(ImmutableList.of(newData, newData), tagReplacerFactory, logger);

        assertThat(status.isFailed()).isFalse();
        assertThat(status.isFailed(source1)).isFalse();
        assertThat(status.isFailed(source2)).isFalse();

        verify(threadPrincipalService, times(1)).set(PRINCIPAL); // per 1 started thread
        verify(webServiceCreate).call(eq(createEndPoint), dataStreamCaptor.capture());
        assertThat(dataStreamCaptor.getValue().collect(Collectors.toList())).containsOnly(newData, newData);
        verifyNoMoreInteractions(webServiceCreate);
        verifyZeroInteractions(webServiceChange);
        verify(serviceCallType).getStatuses(Collections.singletonList(createServiceCall));
        verifyNoMoreInteractions(serviceCallType);
    }

    @Test
    public void testNoNewData() {
        WebServiceDestinationImpl destination = getDestination(createEndPoint, changeEndPoint);
        DataSendingStatus status = destination.send(ImmutableList.of(updatedData, updatedData), tagReplacerFactory, logger);

        assertThat(status.isFailed()).isFalse();
        assertThat(status.isFailed(source1)).isFalse();
        assertThat(status.isFailed(source2)).isFalse();

        verify(threadPrincipalService, times(1)).set(PRINCIPAL); // per 1 started thread
        verify(webServiceChange).call(eq(changeEndPoint), dataStreamCaptor.capture());
        assertThat(dataStreamCaptor.getValue().collect(Collectors.toList())).containsOnly(updatedData, updatedData);
        verifyNoMoreInteractions(webServiceChange);
        verifyZeroInteractions(webServiceCreate);
        verify(serviceCallType).getStatuses(Collections.singletonList(changeServiceCall));
        verifyNoMoreInteractions(serviceCallType);
    }

    @Test
    public void testExportOnlyNewData() {
        WebServiceDestinationImpl destination = getDestination(createEndPoint, null);
        DataSendingStatus status = destination.send(ImmutableList.of(newData, newData), tagReplacerFactory, logger);

        assertThat(status.isFailed()).isFalse();
        assertThat(status.isFailed(source1)).isFalse();
        assertThat(status.isFailed(source2)).isFalse();

        verify(threadPrincipalService, times(1)).set(PRINCIPAL); // per 1 started thread
        verify(webServiceCreate).call(eq(createEndPoint), dataStreamCaptor.capture());
        assertThat(dataStreamCaptor.getValue().collect(Collectors.toList())).containsOnly(newData, newData);
        verifyNoMoreInteractions(webServiceCreate);
        verifyZeroInteractions(webServiceChange);
        verify(serviceCallType).getStatuses(Collections.singletonList(createServiceCall));
        verifyNoMoreInteractions(serviceCallType);
    }

    @Test
    public void testOngoingServiceCall() {
        stubStatus(createServiceCall, DefaultState.ONGOING, null);

        WebServiceDestinationImpl destination = getDestination(createEndPoint, changeEndPoint);
        DataSendingStatus status = destination.send(ImmutableList.of(newData, updatedData), tagReplacerFactory, logger);

        assertThat(status.isFailed()).isTrue();
        assertThat(status.isFailed(source1)).isTrue();
        assertThat(status.isFailed(source2)).isFalse();

        verify(logger).severe("Data export via web service isn't confirmed for service call " + CREATE_SERVICE_CALL_ID +
                ": No data export confirmation has been received in the configured timeout.");
        verify(threadPrincipalService, times(2)).set(PRINCIPAL); // per each of 2 started threads
        verify(webServiceCreate).call(eq(createEndPoint), dataStreamCaptor.capture());
        assertThat(dataStreamCaptor.getValue().collect(Collectors.toList())).containsOnly(newData);
        verifyNoMoreInteractions(webServiceCreate);
        verify(webServiceChange).call(eq(changeEndPoint), dataStreamCaptor.capture());
        assertThat(dataStreamCaptor.getValue().collect(Collectors.toList())).containsOnly(updatedData);
        verifyNoMoreInteractions(webServiceChange);
        verify(serviceCallType).getStatuses(Collections.singletonList(createServiceCall));
        verify(serviceCallType).getStatuses(Collections.singletonList(changeServiceCall));
        verify(serviceCallType).getDataSources(createServiceCall);
        verifyNoMoreInteractions(serviceCallType);
    }

    @Test
    public void testErrorInServiceCall() {
        stubStatus(changeServiceCall, DefaultState.FAILED, "Error!");

        WebServiceDestinationImpl destination = getDestination(createEndPoint, changeEndPoint);
        DataSendingStatus status = destination.send(ImmutableList.of(newData, updatedData), tagReplacerFactory, logger);

        assertThat(status.isFailed()).isTrue();
        assertThat(status.isFailed(source1)).isFalse();
        assertThat(status.isFailed(source2)).isTrue();

        verify(logger).severe("Data export via web service isn't confirmed for service call " + CHANGE_SERVICE_CALL_ID +
                ": Error!");
        verify(threadPrincipalService, times(2)).set(PRINCIPAL); // per each of 2 started threads
        verify(webServiceCreate).call(eq(createEndPoint), dataStreamCaptor.capture());
        assertThat(dataStreamCaptor.getValue().collect(Collectors.toList())).containsOnly(newData);
        verifyNoMoreInteractions(webServiceCreate);
        verify(webServiceChange).call(eq(changeEndPoint), dataStreamCaptor.capture());
        assertThat(dataStreamCaptor.getValue().collect(Collectors.toList())).containsOnly(updatedData);
        verifyNoMoreInteractions(webServiceChange);
        verify(serviceCallType).getStatuses(Collections.singletonList(createServiceCall));
        verify(serviceCallType).getStatuses(Collections.singletonList(changeServiceCall));
        verify(serviceCallType).getDataSources(changeServiceCall);
        verifyNoMoreInteractions(serviceCallType);
    }

    @Test
    public void testErrorInServiceCallWithNoMessage() {
        stubStatus(createServiceCall, DefaultState.FAILED, null);

        WebServiceDestinationImpl destination = getDestination(createEndPoint, changeEndPoint);
        DataSendingStatus status = destination.send(ImmutableList.of(newData, updatedData), tagReplacerFactory, logger);

        assertThat(status.isFailed()).isTrue();
        assertThat(status.isFailed(source1)).isTrue();
        assertThat(status.isFailed(source2)).isFalse();

        verify(logger).severe("Data export via web service isn't confirmed for service call " + CREATE_SERVICE_CALL_ID +
                ": Received error code, but no error has been provided.");
        verify(threadPrincipalService, times(2)).set(PRINCIPAL); // per each of 2 started threads
        verify(webServiceCreate).call(eq(createEndPoint), dataStreamCaptor.capture());
        assertThat(dataStreamCaptor.getValue().collect(Collectors.toList())).containsOnly(newData);
        verifyNoMoreInteractions(webServiceCreate);
        verify(webServiceChange).call(eq(changeEndPoint), dataStreamCaptor.capture());
        assertThat(dataStreamCaptor.getValue().collect(Collectors.toList())).containsOnly(updatedData);
        verifyNoMoreInteractions(webServiceChange);
        verify(serviceCallType).getStatuses(Collections.singletonList(createServiceCall));
        verify(serviceCallType).getStatuses(Collections.singletonList(changeServiceCall));
        verify(serviceCallType).getDataSources(createServiceCall);
        verifyNoMoreInteractions(serviceCallType);
    }

    @Test
    public void testUnknownStateOfServiceCall() {
        stubStatus(changeServiceCall, DefaultState.REJECTED, null);

        WebServiceDestinationImpl destination = getDestination(createEndPoint, changeEndPoint);
        DataSendingStatus status = destination.send(ImmutableList.of(newData, updatedData), tagReplacerFactory, logger);

        assertThat(status.isFailed()).isTrue();
        assertThat(status.isFailed(source1)).isFalse();
        assertThat(status.isFailed(source2)).isTrue();

        verify(logger).severe("Data export via web service isn't confirmed for service call " + CHANGE_SERVICE_CALL_ID +
                ": Unexpected state of the service call: REJECTED.");
        verify(threadPrincipalService, times(2)).set(PRINCIPAL); // per each of 2 started threads
        verify(webServiceCreate).call(eq(createEndPoint), dataStreamCaptor.capture());
        assertThat(dataStreamCaptor.getValue().collect(Collectors.toList())).containsOnly(newData);
        verifyNoMoreInteractions(webServiceCreate);
        verify(webServiceChange).call(eq(changeEndPoint), dataStreamCaptor.capture());
        assertThat(dataStreamCaptor.getValue().collect(Collectors.toList())).containsOnly(updatedData);
        verifyNoMoreInteractions(webServiceChange);
        verify(serviceCallType).getStatuses(Collections.singletonList(createServiceCall));
        verify(serviceCallType).getStatuses(Collections.singletonList(changeServiceCall));
        verify(serviceCallType).getDataSources(changeServiceCall);
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
        when(webServiceChange.call(any(EndPointConfiguration.class), any())).thenThrow(new UnsupportedOperationException("Exception!"));

        WebServiceDestinationImpl destination = getDestination(createEndPoint, changeEndPoint);
        assertThatThrownBy(() -> destination.send(ImmutableList.of(newData, updatedData), tagReplacerFactory, logger))
                .isInstanceOf(DestinationFailedException.class)
                .hasMessage("Failure while exporting data via web service: Exception!");

        verify(threadPrincipalService, times(2)).set(PRINCIPAL); // per each of 2 started threads
        verify(webServiceCreate).call(eq(createEndPoint), dataStreamCaptor.capture());
        assertThat(dataStreamCaptor.getValue().collect(Collectors.toList())).containsOnly(newData);
        verifyNoMoreInteractions(webServiceCreate);
        verify(webServiceChange).call(eq(changeEndPoint), dataStreamCaptor.capture());
        assertThat(dataStreamCaptor.getValue().collect(Collectors.toList())).containsOnly(updatedData);
        verifyNoMoreInteractions(webServiceChange);
        verify(serviceCallType, never()).getStatus(changeServiceCall);
        verify(serviceCallType, never()).getStatuses(anyListContaining(changeServiceCall));
        verify(serviceCallType, never()).tryFailingServiceCall(any(ServiceCall.class), anyString());
        verify(serviceCallType, never()).tryPassingServiceCall(any(ServiceCall.class));
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
        when(serviceCallType.getStatuses(anyListContaining(serviceCall))).thenReturn(Collections.singletonList(result));
    }

    private WebServiceDestinationImpl getDestination(EndPointConfiguration createEndPoint, EndPointConfiguration changeEndPoint) {
        WebServiceDestinationImpl destination = new WebServiceDestinationImpl(dataModel, clock, thesaurus, dataExportService, fileSystem, transactionService, threadPrincipalService);
        return destination.init(exportTask, createEndPoint, changeEndPoint);
    }
}
