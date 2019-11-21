/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.servicecall.getmeterreadings;

import com.elster.jupiter.cim.webservices.outbound.soap.SendMeterReadingsProvider;
import com.elster.jupiter.devtools.tests.rules.TimeZoneNeutral;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryParameters;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.cim.webservices.inbound.soap.meterreadings.MeterReadingsBuilder;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;

import ch.iec.tc57._2011.meterreadings.MeterReading;
import ch.iec.tc57._2011.meterreadings.MeterReadings;
import ch.iec.tc57._2011.schema.message.HeaderType;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

import javax.inject.Provider;
import java.time.Instant;
import java.time.Month;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ParentGetMeterReadingsServiceCallHandlerTest {
    private static final long SERVICE_CALL_ID = 1l;
    private static final String REPLY_ADDRESS = "some_url";
    private static final String SOURCE = "Meter";
    private static final ZonedDateTime MAY_1ST = ZonedDateTime.of(2017, 5, 1, 0, 0, 0, 0, TimeZoneNeutral.getMcMurdo());
    private static final ZonedDateTime JUNE_1ST = MAY_1ST.with(Month.JUNE);
    private static final Instant startDate = MAY_1ST.toInstant();
    private static final Instant endDate = JUNE_1ST.toInstant();
    private static final String END_DEVICE1_MRID = "f86cdede-c8ee-42c8-8c58-dc8f26fe41ac";
    private static final String END_DEVICE1_NAME = "Device 1";
    private static final String END_DEVICE2_MRID = "f86cdede-c8ee-42c8-8c58-dc8f26fe41dd";
    private static final String END_DEVICE2_NAME = "Device 2";
    private static final String MIN15_MRID = "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0";

    @Mock
    private com.elster.jupiter.metering.Meter meter1;
    @Mock
    private ServiceCall serviceCall;
    @Mock
    private ServiceCall subParentServiceCall_1, subParentServiceCall_2;
    @Mock
    private ServiceCallType serviceCallType;
    @Mock
    private SendMeterReadingsProvider sendMeterReadingsProvider;
    @Mock
    private Provider<MeterReadingsBuilder> readingBuilderProvider;
    @Mock
    private EndPointConfigurationService endPointConfigurationService;
    @Mock
    private DeviceService deviceService;
    @Mock
    private MeteringService meteringService;
    @Mock
    private MeterReadingsBuilder meterReadingsBuilder;
    @Mock
    private Query<Meter> meterQuery;
    @Mock
    MeterReadings meterReadings;
    private ParentGetMeterReadingsServiceCallHandler parentServiceCallHandler;
    private ParentGetMeterReadingsDomainExtension parentDomainExtension;


    @Before
    public void setUp() throws Exception {
        parentDomainExtension = new ParentGetMeterReadingsDomainExtension();
        parentDomainExtension.setSource(SOURCE);
        parentDomainExtension.setCallbackUrl(REPLY_ADDRESS);
        parentDomainExtension.setTimePeriodStart(startDate);
        parentDomainExtension.setTimePeriodEnd(endDate);
        parentDomainExtension.setReadingTypes(MIN15_MRID);
        parentDomainExtension.setResponseStatus(ParentGetMeterReadingsDomainExtension.ResponseStatus.NOT_SENT.getName());
        when(serviceCall.getExtensionFor(any(ParentGetMeterReadingsCustomPropertySet.class)))
                .thenReturn(Optional.of(parentDomainExtension));
        when(serviceCall.getExtension(any())).thenReturn(Optional.of(parentDomainExtension));


        when(serviceCall.getId()).thenReturn(SERVICE_CALL_ID);
        when(serviceCall.getState()).thenReturn(DefaultState.WAITING);

        when(serviceCall.getType()).thenReturn(serviceCallType);
        when(subParentServiceCall_1.getType()).thenReturn(serviceCallType);
        when(subParentServiceCall_2.getType()).thenReturn(serviceCallType);
        when(subParentServiceCall_1.getState()).thenReturn(DefaultState.WAITING);
        when(subParentServiceCall_2.getState()).thenReturn(DefaultState.WAITING);
        when(serviceCallType.getName()).thenReturn("ServiceCallType");

        mockSubParentDomainExtension(subParentServiceCall_1, END_DEVICE1_MRID, END_DEVICE1_NAME);
        mockSubParentDomainExtension(subParentServiceCall_2, END_DEVICE2_MRID, END_DEVICE2_NAME);

        Finder<ServiceCall> childServiceCallFinder = mock(Finder.class);
        when(childServiceCallFinder.stream()).then((i) -> Stream.of(subParentServiceCall_1, subParentServiceCall_2));
        when(serviceCall.findChildren()).thenReturn(childServiceCallFinder);
        when(serviceCall.findChildren(any())).thenReturn(childServiceCallFinder);

        Finder<ServiceCall> secondChildServiceCallFinder_1 = mock(Finder.class);
        when(secondChildServiceCallFinder_1.stream()).then((i) -> Stream.empty());
        when(subParentServiceCall_1.findChildren()).thenReturn(secondChildServiceCallFinder_1);
        when(subParentServiceCall_1.findChildren().paged(anyInt(), anyInt())).thenReturn(secondChildServiceCallFinder_1);

        Finder<ServiceCall> secondChildServiceCallFinder_2 = mock(Finder.class);
        when(secondChildServiceCallFinder_2.stream()).then((i) -> Stream.empty());
        when(subParentServiceCall_2.findChildren()).thenReturn(secondChildServiceCallFinder_2);
        when(subParentServiceCall_2.findChildren().paged(anyInt(), anyInt())).thenReturn(secondChildServiceCallFinder_2);

        Finder<Device> deviceFinder = mockFinder(Collections.emptyList());
        when(deviceService.findAllDevices(any(Condition.class))).thenReturn(deviceFinder);

        parentServiceCallHandler = new ParentGetMeterReadingsServiceCallHandler(meteringService,
                sendMeterReadingsProvider, readingBuilderProvider, endPointConfigurationService, deviceService);
        when(sendMeterReadingsProvider.call(any(), eq(getHeader(HeaderType.Verb.REPLY)), any())).thenReturn(true);
        when(readingBuilderProvider.get()).thenReturn(meterReadingsBuilder);
        mockFindEndPointConfigurations();

        when(meterReadingsBuilder.withEndDevices(any())).thenReturn(meterReadingsBuilder);
        when(meterReadingsBuilder.withLoadProfiles(any())).thenReturn(meterReadingsBuilder);
        when(meterReadingsBuilder.withRegisterGroups(any())).thenReturn(meterReadingsBuilder);
        when(meterReadingsBuilder.withReadingTypesMRIDsTimeRangeMap(any())).thenReturn(meterReadingsBuilder);
        when(meterReadingsBuilder.ofReadingTypesWithMRIDs(any())).thenReturn(meterReadingsBuilder);
        when(meterReadingsBuilder.inTimeIntervals(any())).thenReturn(meterReadingsBuilder);
        when(meterReadingsBuilder.withRegisterUpperBoundShift(anyInt())).thenReturn(meterReadingsBuilder);
        when(meterReadingsBuilder.build()).thenReturn(meterReadings);
    }

    private SubParentGetMeterReadingsDomainExtension mockSubParentDomainExtension(ServiceCall subParentServiceCall, String deviceMrid, String deviceName) {
        SubParentGetMeterReadingsDomainExtension subParentDomainExtension = new SubParentGetMeterReadingsDomainExtension();
        subParentDomainExtension.setEndDeviceMrid(deviceMrid);
        subParentDomainExtension.setEndDeviceName(deviceName);
        when(subParentServiceCall.getExtensionFor(any(SubParentGetMeterReadingsCustomPropertySet.class)))
                .thenReturn(Optional.of(subParentDomainExtension));
        when(subParentServiceCall.getExtension(any())).thenReturn(Optional.of(subParentDomainExtension));
        return subParentDomainExtension;
    }

    private void mockFindEndPointConfigurations() {
        EndPointConfiguration endPointConfiguration = mockEndPointConfiguration(REPLY_ADDRESS);
        when(endPointConfiguration.getUrl()).thenReturn(REPLY_ADDRESS);
        Finder<EndPointConfiguration> finder = mockFinder(Collections.singletonList(endPointConfiguration));
        when(endPointConfigurationService.findEndPointConfigurations()).thenReturn(finder);
    }

    private EndPointConfiguration mockEndPointConfiguration(String url) {
        EndPointConfiguration mock = mock(EndPointConfiguration.class);
        when(mock.getUrl()).thenReturn(url);
        when(mock.isActive()).thenReturn(true);
        when(mock.isInbound()).thenReturn(false);
        return mock;
    }

    private <T> Finder<T> mockFinder(List<T> list) {
        Finder<T> finder = mock(Finder.class);

        when(finder.paged(anyInt(), anyInt())).thenReturn(finder);
        when(finder.sorted(anyString(), any(Boolean.class))).thenReturn(finder);
        when(finder.from(any(QueryParameters.class))).thenReturn(finder);
        when(finder.find()).thenReturn(list);
        when(finder.stream()).thenReturn(list.stream());
        return finder;
    }

    private void mockFindEndDevices(Meter... endDevices) {
        List<Meter> devices = new ArrayList<>();
        devices.addAll(Arrays.asList(endDevices));
        when(meteringService.getMeterQuery()).thenReturn(meterQuery);
        when(meteringService.getMeterQuery().select(anyObject())).thenReturn(devices);
    }

    private RangeSet<Instant> getTimeRangeSet(Instant start, Instant end) {
        RangeSet<Instant> rangeSet = TreeRangeSet.create();
        rangeSet.add(Range.openClosed(start, end));
        return rangeSet;
    }

    private HeaderType getHeader(HeaderType.Verb requestVerb) {
        HeaderType header = new HeaderType();
        header.setVerb(requestVerb);
        header.setNoun("MeterReadings");
        return header;
    }

    @Test
    public void testServiceCallFromCreatedToPending() {
        parentServiceCallHandler.onStateChange(serviceCall, DefaultState.CREATED, DefaultState.PENDING);
        assertThat(serviceCall.getState().equals(DefaultState.PENDING));
        verify(serviceCall).log(LogLevel.FINE, "Parent service call is switched to state Pending");
    }

    @Test
    public void testServiceCallFromPendingToOngoing() {
        parentServiceCallHandler.onStateChange(serviceCall, DefaultState.PENDING, DefaultState.ONGOING);
        assertThat(serviceCall.getState().equals(DefaultState.ONGOING));
        verify(serviceCall).log(LogLevel.FINE, "Parent service call is switched to state Ongoing");
    }

    @Test
    public void testServiceCallFromOngoingToWaiting() {
        parentServiceCallHandler.onStateChange(serviceCall, DefaultState.ONGOING, DefaultState.WAITING);
        assertThat(serviceCall.getState().equals(DefaultState.WAITING));
        verify(serviceCall).log(LogLevel.FINE, "Parent service call is switched to state Waiting");
    }

    @Test
    public void testServiceCallFromWaitingToOngoing() {
        parentServiceCallHandler.onStateChange(serviceCall, DefaultState.WAITING, DefaultState.ONGOING);
        assertThat(serviceCall.getState().equals(DefaultState.ONGOING));
        verify(serviceCall).log(LogLevel.FINE, "Parent service call is switched to state Ongoing");
    }

    @Test
    public void testServiceCallFromOngoingToFailed() {
        parentServiceCallHandler.onStateChange(serviceCall, DefaultState.ONGOING, DefaultState.FAILED);
        assertThat(serviceCall.getState().equals(DefaultState.FAILED));
        serviceCall.findChildren().stream().forEach(subParentServiceCall -> {
            assertThat(subParentServiceCall.getState().equals(DefaultState.CANCELLED));
            verify(subParentServiceCall).requestTransition(DefaultState.CANCELLED);
        });
    }

    @Test
    public void testServiceCallFromOngoingToCancelled() {
        parentServiceCallHandler.onStateChange(serviceCall, DefaultState.ONGOING, DefaultState.CANCELLED);
        assertThat(serviceCall.getState().equals(DefaultState.CANCELLED));
        verify(serviceCall).log(LogLevel.FINE, "Parent service call is switched to state Cancelled");
        serviceCall.findChildren().stream().forEach(subParentServiceCall -> {
            assertThat(subParentServiceCall.getState().equals(DefaultState.CANCELLED));
            verify(subParentServiceCall).requestTransition(DefaultState.CANCELLED);
        });
    }

    @Test
    public void testServiceCallFromOngoingToRejected() {
        parentServiceCallHandler.onStateChange(serviceCall, DefaultState.ONGOING, DefaultState.REJECTED);
        assertThat(serviceCall.getState().equals(DefaultState.REJECTED));
        verify(serviceCall).log(LogLevel.FINE, "Parent service call is switched to state Rejected");
        serviceCall.findChildren().stream().forEach(subParentServiceCall -> {
            assertThat(subParentServiceCall.getState().equals(DefaultState.CANCELLED));
            verify(subParentServiceCall).requestTransition(DefaultState.CANCELLED);
        });
    }

    @Test
    public void testServiceCallFromOngoingToPaused() {
        parentServiceCallHandler.onStateChange(serviceCall, DefaultState.ONGOING, DefaultState.PAUSED);
        assertThat(serviceCall.getState().equals(DefaultState.PAUSED));
        verify(serviceCall).log(LogLevel.FINE, "Parent service call is switched to state Paused");
    }

    @Test
    public void testServiceCallFromOngoingToSuccessful() {
        parentServiceCallHandler.onStateChange(serviceCall, DefaultState.ONGOING, DefaultState.SUCCESSFUL);
        assertThat(serviceCall.getState().equals(DefaultState.SUCCESSFUL));
        verify(serviceCall).log(LogLevel.FINE, "Parent service call is switched to state Successful");
    }

    @Test
    public void testServiceCallFromPausedToOngoingSuccessCase() {
        mockFindEndDevices(meter1);
        List<MeterReading> mrList = mock(List.class);
        when(meterReadings.getMeterReading()).thenReturn(mrList);
        when(mrList.isEmpty()).thenReturn(false);
        when(sendMeterReadingsProvider.call(any(), any(), any())).thenReturn(true);
        when(subParentServiceCall_1.getState()).thenReturn(DefaultState.SUCCESSFUL);
        when(subParentServiceCall_2.getState()).thenReturn(DefaultState.SUCCESSFUL);
        parentServiceCallHandler.onStateChange(serviceCall, DefaultState.PAUSED, DefaultState.ONGOING);
        assertThat(serviceCall.getState().equals(DefaultState.ONGOING));
        verify(sendMeterReadingsProvider).call(any(), any(), any());
        verify(serviceCall).requestTransition(DefaultState.SUCCESSFUL);
        verify(serviceCall).log(LogLevel.FINE, "Parent service call is switched to state Ongoing");
        verify(serviceCall).log(LogLevel.FINE, "Result collection is started for source 'Meter', time range [(2017-04-30T12:00:00Z..2017-05-31T12:00:00Z]]");
        verify(serviceCall).log(LogLevel.FINE, "Data successfully sent for source 'Meter', time range [(2017-04-30T12:00:00Z..2017-05-31T12:00:00Z]]");
    }

    @Test
    public void testServiceCallFromOngoingToPartialSuccessCase() {
        mockFindEndDevices(meter1);
        List<MeterReading> mrList = mock(List.class);
        when(meterReadings.getMeterReading()).thenReturn(mrList);
        when(mrList.isEmpty()).thenReturn(false);
        parentServiceCallHandler.onStateChange(serviceCall, DefaultState.ONGOING, DefaultState.PARTIAL_SUCCESS);
        assertThat(serviceCall.getState().equals(DefaultState.ONGOING));
        verify(serviceCall).log(LogLevel.FINE, "Parent service call is switched to state Partial success");
    }

    @Test
    public void testServiceCallFromRendingToOngoingFailedOutboundCase() {
        mockFindEndDevices(meter1);
        List<MeterReading> mrList = mock(List.class);
        when(meterReadings.getMeterReading()).thenReturn(mrList);
        when(mrList.isEmpty()).thenReturn(false);
        when(sendMeterReadingsProvider.call(any(), eq(getHeader(HeaderType.Verb.REPLY)), any())).thenReturn(false);
        parentServiceCallHandler.onStateChange(serviceCall, DefaultState.PAUSED, DefaultState.ONGOING);
        assertThat(serviceCall.getState().equals(DefaultState.ONGOING));
        verify(sendMeterReadingsProvider).call(any(), any(), any());
        verify(serviceCall).log(LogLevel.FINE, "Parent service call is switched to state Ongoing");
        verify(serviceCall).log(LogLevel.FINE, "Result collection is started for source 'Meter', time range [(2017-04-30T12:00:00Z..2017-05-31T12:00:00Z]]");
        verify(serviceCall).log(LogLevel.SEVERE, "Unable to send meter readings data for source 'Meter', time range [(2017-04-30T12:00:00Z..2017-05-31T12:00:00Z]]");
    }
}
