/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.outbound.soap.enddeviceevents;

import com.elster.jupiter.export.DataExportWebService;
import com.elster.jupiter.export.ExportData;
import com.elster.jupiter.export.MeterEventData;
import com.elster.jupiter.metering.CimAttributeNames;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.nls.LocalizedException;
import com.energyict.mdc.cim.webservices.outbound.soap.AbstractOutboundWebserviceTest;
import com.energyict.mdc.cim.webservices.outbound.soap.EndDeviceEventsServiceProvider;

import ch.iec.tc57._2011.enddeviceeventsmessage.EndDeviceEventsEventMessageType;
import ch.iec.tc57._2011.enddeviceeventsmessage.EndDeviceEventsResponseMessageType;
import ch.iec.tc57._2011.schema.message.ReplyType;
import ch.iec.tc57._2011.sendenddeviceevents.EndDeviceEventsPort;
import ch.iec.tc57._2011.sendenddeviceevents.FaultMessage;
import ch.iec.tc57._2011.sendenddeviceevents.SendEndDeviceEvents;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;

import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EndDeviceEventsServiceProviderImplTest extends AbstractOutboundWebserviceTest<EndDeviceEventsPort> {
    private static final String METER_MRID = "Meter mrid";
    private static final String METER_NAME = "Meter name";
    private static final ZonedDateTime JULY_1ST = ZonedDateTime.of(2020, 7, 1, 0, 0, 0, 0, ZoneId.of("Europe/Moscow"));
    private static final SetMultimap<String, String> EXPECTED_ATTRIBUTES = ImmutableSetMultimap.of(
            CimAttributeNames.CIM_DEVICE_NAME.getAttributeName(), METER_NAME,
            CimAttributeNames.CIM_DEVICE_MR_ID.getAttributeName(), METER_MRID);

    @Mock
    private Clock clock;
    @Mock
    private MeterReading meterReading;
    @Mock
    private EndDeviceEventRecord endDeviceEvent;
    @Mock
    private EndDevice endDevice;
    @Mock
    private EndDeviceEventsResponseMessageType response;
    @Mock
    private ReplyType replyType;
    @Mock
    private DataExportWebService.ExportContext context;
    private EndDeviceEventsServiceProviderImpl provider;

    @Before
    public void setup() throws FaultMessage {
        provider = getProviderInstance(EndDeviceEventsServiceProviderImpl.class);
        when(endpoint.createdEndDeviceEvents(any(EndDeviceEventsEventMessageType.class))).thenReturn(response);
        when(response.getReply()).thenReturn(replyType);
        when(replyType.getResult()).thenReturn(ReplyType.Result.OK);

        when(endDeviceEvent.getMRID()).thenReturn("EVENT_MRID");
        when(endDeviceEvent.getCreatedDateTime()).thenReturn(JULY_1ST.toInstant());
        when(endDeviceEvent.getIssuerID()).thenReturn("1");
        when(endDeviceEvent.getIssuerTrackingID()).thenReturn("2");
        when(endDeviceEvent.getDescription()).thenReturn("Description");
        when(endDeviceEvent.getUserID()).thenReturn("userId");
        when(endDeviceEvent.getSeverity()).thenReturn("medium");
        when(endDeviceEvent.getEventTypeCode()).thenReturn("eventTypeCode");
        when(endDeviceEvent.getType()).thenReturn("type");
        when(endDeviceEvent.getEventData()).thenReturn(Collections.emptyMap());
        when(endDeviceEvent.getEndDevice()).thenReturn(endDevice);

        when(endDevice.getMRID()).thenReturn(METER_MRID);
        when(endDevice.getName()).thenReturn(METER_NAME);
    }

    @Test
    public void testCallWithExportData() throws FaultMessage {
        Stream<? extends ExportData> exportData = mockExportData();
        provider.call(outboundEndPointConfiguration, exportData, context);

        verify(endpoint).createdEndDeviceEvents(any(EndDeviceEventsEventMessageType.class));
        verify(webServiceCallOccurrence).saveRelatedAttributes(EXPECTED_ATTRIBUTES);
    }

    @Test
    public void testCallWithExportDataBadResponse() throws FaultMessage {
        Stream<? extends ExportData> exportData = mockExportData();
        when(replyType.getResult()).thenReturn(ReplyType.Result.FAILED);
        when(outboundEndPointConfiguration.getName()).thenReturn("figue");

        assertThatThrownBy(() -> provider.call(outboundEndPointConfiguration, exportData, context))
                .isInstanceOf(LocalizedException.class)
                .hasMessage("Failed to send message to the following web service endpoint(s): 'figue'.");

        verify(endpoint).createdEndDeviceEvents(any(EndDeviceEventsEventMessageType.class));
        verify(webServiceCallOccurrence).saveRelatedAttributes(EXPECTED_ATTRIBUTES);
    }

    private Stream<MeterEventData> mockExportData() {
        when(meterReading.getEvents()).thenReturn(Collections.singletonList(endDeviceEvent));

        TestDefaultStructureMarker structureMarker = TestDefaultStructureMarker.createRoot(clock, METER_MRID);
        structureMarker = structureMarker.child(METER_NAME);
        return Stream.of(new MeterEventData(meterReading, structureMarker));
    }

    @Test
    public void testGetService() {
        EndDeviceEventsServiceProviderImpl provider = new EndDeviceEventsServiceProviderImpl();
        assertThat(provider.getService()).isSameAs(EndDeviceEventsPort.class);
    }

    @Test
    public void testGet() {
        EndDeviceEventsServiceProviderImpl provider = new EndDeviceEventsServiceProviderImpl();
        assertThat(provider.get()).isNotNull().isInstanceOf(SendEndDeviceEvents.class);
    }

    @Test
    public void testCallWithOneEventRecordAndSpecifiedEndpoint() throws FaultMessage {
        provider.call(endDeviceEvent, outboundEndPointConfiguration);

        verify(endpoint).createdEndDeviceEvents(any(EndDeviceEventsEventMessageType.class));
        verify(webServiceCallOccurrence).saveRelatedAttributes(EXPECTED_ATTRIBUTES);
    }

    @Test
    public void testCallWithOneEventRecordAndSpecifiedEndpointBadResponse() throws FaultMessage {
        when(replyType.getResult()).thenReturn(ReplyType.Result.FAILED);
        when(outboundEndPointConfiguration.getName()).thenReturn("figue");

        assertThatThrownBy(() -> provider.call(endDeviceEvent, outboundEndPointConfiguration))
                .isInstanceOf(LocalizedException.class)
                .hasMessage("Failed to send message to the following web service endpoint(s): 'figue'.");

        verify(endpoint).createdEndDeviceEvents(any(EndDeviceEventsEventMessageType.class));
        verify(webServiceCallOccurrence).saveRelatedAttributes(EXPECTED_ATTRIBUTES);
    }

    @Test
    public void testCallWithOneEventRecord() throws FaultMessage {
        provider.call(endDeviceEvent);

        verify(endpoint).createdEndDeviceEvents(any(EndDeviceEventsEventMessageType.class));
        verify(webServiceCallOccurrence).saveRelatedAttributes(EXPECTED_ATTRIBUTES);
    }

    @Test
    public void testCallWithOneEventRecordBadResponse() throws FaultMessage {
        when(replyType.getResult()).thenReturn(ReplyType.Result.FAILED);
        when(outboundEndPointConfiguration.getName()).thenReturn("figue");

        assertThatThrownBy(() -> provider.call(endDeviceEvent))
                .isInstanceOf(LocalizedException.class)
                .hasMessage("Failed to send message to the following web service endpoint(s): 'figue'.");

        verify(endpoint).createdEndDeviceEvents(any(EndDeviceEventsEventMessageType.class));
        verify(webServiceCallOccurrence).saveRelatedAttributes(EXPECTED_ATTRIBUTES);
    }

    @Test
    public void testCallWithOneEventRecordNoEndpoints() throws FaultMessage {
        when(endPointConfigurationService.getEndPointConfigurationsForWebService(EndDeviceEventsServiceProvider.NAME)).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> provider.call(endDeviceEvent))
                .isInstanceOf(LocalizedException.class)
                .hasMessage("No web service endpoints are available to send the request using 'CIM SendEndDeviceEvents'.");

        verify(endpoint, never()).createdEndDeviceEvents(any(EndDeviceEventsEventMessageType.class));
        verify(webServiceCallOccurrence, never()).saveRelatedAttributes(EXPECTED_ATTRIBUTES);
    }
}
