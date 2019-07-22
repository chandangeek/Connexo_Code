///*
// * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
// */
//
//package com.energyict.mdc.cim.webservices.inbound.soap.servicecall.getmeterreadings;
//
//import com.elster.jupiter.cim.webservices.outbound.soap.SendMeterReadingsProvider;
//import com.elster.jupiter.devtools.tests.rules.TimeZoneNeutral;
//import com.elster.jupiter.domain.util.Finder;
//import com.elster.jupiter.domain.util.Query;
//import com.elster.jupiter.domain.util.QueryParameters;
//import com.elster.jupiter.metering.EndDevice;
//import com.elster.jupiter.metering.MeteringService;
//import com.elster.jupiter.servicecall.DefaultState;
//import com.elster.jupiter.servicecall.LogLevel;
//import com.elster.jupiter.servicecall.ServiceCall;
//import com.elster.jupiter.servicecall.ServiceCallType;
//import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
//import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
//
//import com.energyict.mdc.cim.webservices.inbound.soap.meterreadings.MeterReadingsBuilder;
//
//import ch.iec.tc57._2011.meterreadings.MeterReading;
//import ch.iec.tc57._2011.meterreadings.MeterReadings;
//import ch.iec.tc57._2011.schema.message.HeaderType;
//import com.google.common.collect.Range;
//import com.google.common.collect.RangeSet;
//import com.google.common.collect.TreeRangeSet;
//
//import javax.inject.Provider;
//import java.text.MessageFormat;
//import java.time.Instant;
//import java.time.Month;
//import java.time.ZonedDateTime;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.List;
//import java.util.Optional;
//
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.Mock;
//import org.mockito.runners.MockitoJUnitRunner;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.Matchers.any;
//import static org.mockito.Matchers.anyInt;
//import static org.mockito.Matchers.anyObject;
//import static org.mockito.Matchers.anyString;
//import static org.mockito.Matchers.eq;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//@RunWith(MockitoJUnitRunner.class)
//public class ParentGetMeterReadingsServiceCallHandlerTest {
//    private static final long SERVICE_CALL_ID = 1l;
//    private static final String REPLY_ADDRESS = "some_url";
//    private static final String SOURCE = "Meter";
//    private static final ZonedDateTime MAY_1ST = ZonedDateTime.of(2017, 5, 1, 0, 0, 0, 0, TimeZoneNeutral.getMcMurdo());
//    private static final ZonedDateTime JUNE_1ST = MAY_1ST.with(Month.JUNE);
//    private static final Instant startDate = MAY_1ST.toInstant();
//    private static final Instant endDate = JUNE_1ST.toInstant();
//    private static final String END_DEVICE1_MRID = "f86cdede-c8ee-42c8-8c58-dc8f26fe41ac";
//    private static final String MIN15_MRID = "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0";
//
//    @Mock
//    private com.elster.jupiter.metering.Meter meter1;
//    @Mock
//    private ServiceCall serviceCall;
//    @Mock
//    private ServiceCall childServiceCall_1, childServiceCall_2;
//    @Mock
//    private ServiceCallType serviceCallType;
//    @Mock
//    private SendMeterReadingsProvider sendMeterReadingsProvider;
//    @Mock
//    private Provider<MeterReadingsBuilder> readingBuilderProvider;
//    @Mock
//    private EndPointConfigurationService endPointConfigurationService;
//    @Mock
//    private MeteringService meteringService;
//    @Mock
//    private MeterReadingsBuilder meterReadingsBuilder;
//    @Mock
//    private Query<EndDevice> endDeviceQuery;
//    @Mock
//    MeterReadings meterReadings;
//    private ParentGetMeterReadingsServiceCallHandler parentGetMeterReadingsServiceCallHandler;
//    private ParentGetMeterReadingsDomainExtension parentGetMeterReadingsDomainExtension;
//
//
//    @Before
//    public void setUp() throws Exception {
//        parentGetMeterReadingsDomainExtension = new ParentGetMeterReadingsDomainExtension();
//        parentGetMeterReadingsDomainExtension.setSource(SOURCE);
//        parentGetMeterReadingsDomainExtension.setCallbackUrl(REPLY_ADDRESS);
//        parentGetMeterReadingsDomainExtension.setTimePeriodStart(startDate);
//        parentGetMeterReadingsDomainExtension.setTimePeriodEnd(endDate);
//        parentGetMeterReadingsDomainExtension.setReadingTypes(MIN15_MRID);
////        parentGetMeterReadingsDomainExtension.setEndDevices(END_DEVICE1_MRID);
//        when(serviceCall.getExtensionFor(any(ParentGetMeterReadingsCustomPropertySet.class)))
//                .thenReturn(Optional.of(parentGetMeterReadingsDomainExtension));
//        when(serviceCall.getExtension(any())).thenReturn(Optional.of(parentGetMeterReadingsDomainExtension));
//
//
//        when(serviceCall.getId()).thenReturn(SERVICE_CALL_ID);
//        when(serviceCall.getState()).thenReturn(DefaultState.WAITING);
//        when(serviceCall.canTransitionTo(any(DefaultState.class))).thenReturn(true);
//        when(childServiceCall_1.canTransitionTo(any(DefaultState.class))).thenReturn(true);
//        when(childServiceCall_2.canTransitionTo(any(DefaultState.class))).thenReturn(true);
//
//        when(serviceCall.getType()).thenReturn(serviceCallType);
//        when(childServiceCall_1.getType()).thenReturn(serviceCallType);
//        when(childServiceCall_2.getType()).thenReturn(serviceCallType);
//        when(serviceCallType.getName()).thenReturn("ServiceCallType");
//
//        Finder<ServiceCall> childServiceCallFinder = mock(Finder.class);
//        List<ServiceCall> children = new ArrayList<>(2);
//        children.add(childServiceCall_1);
//        children.add(childServiceCall_2);
//        when(childServiceCallFinder.stream()).thenReturn(children.stream());
//        when(serviceCall.findChildren()).thenReturn(childServiceCallFinder);
//
//        parentGetMeterReadingsServiceCallHandler = new ParentGetMeterReadingsServiceCallHandler(meteringService,
//                sendMeterReadingsProvider, readingBuilderProvider, endPointConfigurationService);
//
//        when(sendMeterReadingsProvider.call(any(), eq(HeaderType.Verb.CREATED), any())).thenReturn(true);
//        when(readingBuilderProvider.get()).thenReturn(meterReadingsBuilder);
//        mockFindEndPointConfigurations();
//
//        when(meterReadingsBuilder.withEndDevices(any())).thenReturn(meterReadingsBuilder);
//        when(meterReadingsBuilder.ofReadingTypesWithMRIDs(any())).thenReturn(meterReadingsBuilder);
//        when(meterReadingsBuilder.inTimeIntervals(any())).thenReturn(meterReadingsBuilder);
//        when(meterReadingsBuilder.build()).thenReturn(meterReadings);
//    }
//
//    private void mockFindEndPointConfigurations() {
//        EndPointConfiguration endPointConfiguration = mockEndPointConfiguration(REPLY_ADDRESS);
//        when(endPointConfiguration.getUrl()).thenReturn(REPLY_ADDRESS);
//        Finder<EndPointConfiguration> finder = mockFinder(Collections.singletonList(endPointConfiguration));
//        when(endPointConfigurationService.findEndPointConfigurations()).thenReturn(finder);
//    }
//
//    private EndPointConfiguration mockEndPointConfiguration(String url) {
//        EndPointConfiguration mock = mock(EndPointConfiguration.class);
//        when(mock.getUrl()).thenReturn(url);
//        when(mock.isActive()).thenReturn(true);
//        when(mock.isInbound()).thenReturn(false);
//        return mock;
//    }
//
//    private <T> Finder<T> mockFinder(List<T> list) {
//        Finder<T> finder = mock(Finder.class);
//
//        when(finder.paged(anyInt(), anyInt())).thenReturn(finder);
//        when(finder.sorted(anyString(), any(Boolean.class))).thenReturn(finder);
//        when(finder.from(any(QueryParameters.class))).thenReturn(finder);
//        when(finder.find()).thenReturn(list);
//        when(finder.stream()).thenReturn(list.stream());
//        return finder;
//    }
//
//    private void mockFindEndDevices(EndDevice... endDevices) {
//        List<EndDevice> devices = new ArrayList<>();
//        devices.addAll(Arrays.asList(endDevices));
//        when(meteringService.getEndDeviceQuery()).thenReturn(endDeviceQuery);
//        when(meteringService.getEndDeviceQuery().select(anyObject())).thenReturn(devices);
//    }
//
//    private RangeSet<Instant> getTimeRangeSet(Instant start, Instant end) {
//        RangeSet<Instant> rangeSet = TreeRangeSet.create();
//        rangeSet.add(Range.openClosed(start, end));
//        return rangeSet;
//    }
//
//    @Test
//    public void testServiceCallFromCreatedToPending() {
//        parentGetMeterReadingsServiceCallHandler.onStateChange(serviceCall, DefaultState.CREATED, DefaultState.PENDING);
//        assertThat(serviceCall.getState().equals(DefaultState.PENDING));
//    }
//
//    @Test
//    public void testServiceCallFromPendingToOngoing() {
//        parentGetMeterReadingsServiceCallHandler.onStateChange(serviceCall, DefaultState.PENDING, DefaultState.ONGOING);
//        assertThat(serviceCall.getState().equals(DefaultState.ONGOING));
//    }
//
//    @Test
//    public void testServiceCallFromOngoingToWaiting() {
//        parentGetMeterReadingsServiceCallHandler.onStateChange(serviceCall, DefaultState.ONGOING, DefaultState.WAITING);
//        assertThat(serviceCall.getState().equals(DefaultState.WAITING));
//    }
//
//    @Test
//    public void testServiceCallFromOngoingToFailed() {
//        parentGetMeterReadingsServiceCallHandler.onStateChange(serviceCall, DefaultState.ONGOING, DefaultState.FAILED);
//        assertThat(serviceCall.getState().equals(DefaultState.FAILED));
//    }
//
//    @Test
//    public void testServiceCallFromOngoingToCanceled() {
//        parentGetMeterReadingsServiceCallHandler.onStateChange(serviceCall, DefaultState.ONGOING, DefaultState.CANCELLED);
//        assertThat(serviceCall.getState().equals(DefaultState.CANCELLED));
//    }
//
//    @Test
//    public void testServiceCallFromOngoingToRejected() {
//        parentGetMeterReadingsServiceCallHandler.onStateChange(serviceCall, DefaultState.ONGOING, DefaultState.REJECTED);
//        assertThat(serviceCall.getState().equals(DefaultState.REJECTED));
//    }
//
//    @Test
//    public void testServiceCallFromOngoingToPaused() {
//        parentGetMeterReadingsServiceCallHandler.onStateChange(serviceCall, DefaultState.ONGOING, DefaultState.PAUSED);
//        assertThat(serviceCall.getState().equals(DefaultState.PAUSED));
//    }
//
//    @Test
//    public void testServiceCallFromPausedToOngoingSuccessCase() {
//        mockFindEndDevices(meter1);
//        List<MeterReading> mrList = mock(List.class);
//        when(meterReadings.getMeterReading()).thenReturn(mrList);
//        when(mrList.isEmpty()).thenReturn(false);
//        parentGetMeterReadingsServiceCallHandler.onStateChange(serviceCall, DefaultState.PAUSED, DefaultState.ONGOING);
//        assertThat(serviceCall.getState().equals(DefaultState.ONGOING));
//        verify(sendMeterReadingsProvider).call(any(), eq(HeaderType.Verb.CREATED), any());
//        verify(serviceCall).requestTransition(DefaultState.SUCCESSFUL);
//        verify(serviceCall).log(LogLevel.FINE,"Data successfully sent for source 'Meter', time range [(2017-04-30T12:00:00Z‥2017-05-31T12:00:00Z]]");
//    }
//
//    @Test
//    public void testServiceCallFromOngoingToPartialSuccessCase() {
//        mockFindEndDevices(meter1);
//        List<MeterReading> mrList = mock(List.class);
//        when(meterReadings.getMeterReading()).thenReturn(mrList);
//        when(mrList.isEmpty()).thenReturn(false);
//        parentGetMeterReadingsServiceCallHandler.onStateChange(serviceCall, DefaultState.ONGOING, DefaultState.PARTIAL_SUCCESS);
//        assertThat(serviceCall.getState().equals(DefaultState.ONGOING));
//        verify(sendMeterReadingsProvider).call(any(), eq(HeaderType.Verb.CREATED), any());
//        verify(serviceCall).requestTransition(DefaultState.SUCCESSFUL);
//        verify(serviceCall).log(LogLevel.FINE,"Data successfully sent for source 'Meter', time range [(2017-04-30T12:00:00Z‥2017-05-31T12:00:00Z]]");
//
//    }
//
//    @Test
//    public void testOutboundCallFailedCase() {
//        mockFindEndDevices(meter1);
//        List<MeterReading> mrList = mock(List.class);
//        when(meterReadings.getMeterReading()).thenReturn(mrList);
//        when(mrList.isEmpty()).thenReturn(false);
//        when(sendMeterReadingsProvider.call(any(), eq(HeaderType.Verb.CREATED), any())).thenReturn(false);
//        parentGetMeterReadingsServiceCallHandler.onStateChange(serviceCall, DefaultState.PAUSED, DefaultState.ONGOING);
//        assertThat(serviceCall.getState().equals(DefaultState.ONGOING));
//        verify(sendMeterReadingsProvider).call(any(), eq(HeaderType.Verb.CREATED), any());
//        verify(serviceCall).log(LogLevel.SEVERE,"Unable to send meter readings data for source 'Meter', time range [(2017-04-30T12:00:00Z‥2017-05-31T12:00:00Z]]");
//    }
//}
