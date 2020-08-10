/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.outbound.soap.enddeviceevents;

import ch.iec.tc57._2011.enddeviceeventsmessage.EndDeviceEventsEventMessageType;
import ch.iec.tc57._2011.enddeviceeventsmessage.EndDeviceEventsResponseMessageType;
import ch.iec.tc57._2011.schema.message.ReplyType;
import ch.iec.tc57._2011.sendenddeviceevents.EndDeviceEventsPort;
import ch.iec.tc57._2011.sendenddeviceevents.FaultMessage;
import ch.iec.tc57._2011.sendenddeviceevents.SendEndDeviceEvents;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.export.DataExportWebService;
import com.elster.jupiter.export.ExportData;
import com.elster.jupiter.export.MeterEventData;
import com.elster.jupiter.metering.CimAttributeNames;
import com.elster.jupiter.metering.readings.EndDeviceEvent;
import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractOutboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrence;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EndDeviceEventsServiceProviderImplTest {

    private static final String METER_MRID = "Meter mrid";
    private static final String METER_NAME = "Meter name";
    private static final ZonedDateTime JULY_1ST = ZonedDateTime.of(2020, 7, 1, 0, 0, 0, 0, ZoneId.of("Europe/Moscow"));

    @Mock
    private EndDeviceEventsPort endDeviceEventsPort;
    @Mock
    private EndPointConfiguration endPointConfiguration;
    @Mock
    private EventService eventService;
    @Mock
    private EndPointConfigurationService endPointConfigurationService;
    @Mock
    private WebServicesService webServicesService;
    @Mock
    private WebServiceCallOccurrence webServiceCallOccurrence;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private OutboundEndPointProvider.RequestSender requestSender;
    @Mock
    private EndDeviceEventsResponseMessageType response;
    @Mock
    private Stream<? extends ExportData> exportData;
    @Mock
    private DataExportWebService.ExportContext context;
    @Mock
    private Clock clock;
    @Mock
    private MeterReading meterReading;
    @Mock
    private EndDeviceEvent endDeviceEvent;
    private EndDeviceEventsServiceProviderImpl provider;

    @Before
    public void setup() {
        provider = spy(new EndDeviceEventsServiceProviderImpl());
        when(webServiceCallOccurrence.getId()).thenReturn(1l);
        when(webServicesService.startOccurrence(any(EndPointConfiguration.class), anyString(), anyString())).thenReturn(webServiceCallOccurrence);
        when(thesaurus.getSimpleFormat(any(MessageSeed.class))).thenReturn(mock(NlsMessageFormat.class));
        inject(AbstractOutboundEndPointProvider.class, provider, "eventService", eventService);
        inject(AbstractOutboundEndPointProvider.class, provider, "webServicesService", webServicesService);
        inject(AbstractOutboundEndPointProvider.class, provider, "endPointConfigurationService", endPointConfigurationService);
        inject(AbstractOutboundEndPointProvider.class, provider, "thesaurus", thesaurus);
        when(webServiceCallOccurrence.getId()).thenReturn(1l);
        when(thesaurus.getSimpleFormat(any(MessageSeed.class))).thenReturn(mock(NlsMessageFormat.class));
        when(provider.using(anyString())).thenReturn(requestSender);
        when(requestSender.toEndpoints(endPointConfiguration)).thenReturn(requestSender);
        Map<EndPointConfiguration, EndDeviceEventsResponseMessageType> responseMap = new HashMap();
        responseMap.put(endPointConfiguration, response);
        when(requestSender.withRelatedAttributes(any())).thenReturn(requestSender);
        when(requestSender.send(any())).thenReturn((Map)responseMap);
    }

    @Test
    public void testCallWithExportData() throws FaultMessage {
        when(clock.instant()).thenReturn(JULY_1ST.toInstant());

        when(endDeviceEvent.getMRID()).thenReturn("EVENT_MRID");
        when(endDeviceEvent.getCreatedDateTime()).thenReturn(JULY_1ST.toInstant());
        when(endDeviceEvent.getIssuerID()).thenReturn("1");
        when(endDeviceEvent.getIssuerTrackingID()).thenReturn("2");
        when(endDeviceEvent.getDescription()).thenReturn("Description");
        when(endDeviceEvent.getUserID()).thenReturn("userId");
        when(endDeviceEvent.getSeverity()).thenReturn("medium");
        when(endDeviceEvent.getEventTypeCode()).thenReturn("eventTypeCode");
        when(endDeviceEvent.getType()).thenReturn("type");
        when(endDeviceEvent.getEventData()).thenReturn(Collections.EMPTY_MAP);
        List<EndDeviceEvent> events = new ArrayList<>();
        events.add(endDeviceEvent);
        when(meterReading.getEvents()).thenReturn(events);

        TestDefaultStructureMarker structureMarker = TestDefaultStructureMarker.createRoot(clock, METER_MRID);
        structureMarker = structureMarker.child(METER_NAME);
        exportData = Stream.of(new MeterEventData(meterReading, structureMarker));
        ReplyType reply = mock(ReplyType.class);
        when(response.getReply()).thenReturn(reply);
        when(reply.getResult()).thenReturn(ReplyType.Result.OK);
        provider.call(endPointConfiguration, exportData, context);

        SetMultimap<String, String> values = HashMultimap.create();
        values.put(CimAttributeNames.CIM_DEVICE_NAME.getAttributeName(), METER_NAME);
        values.put(CimAttributeNames.CIM_DEVICE_MR_ID.getAttributeName(), METER_MRID);

        verify(provider).using("createdEndDeviceEvents");
        verify(requestSender).toEndpoints(endPointConfiguration);
        verify(requestSender).withRelatedAttributes(values);
        verify(requestSender).send(any(EndDeviceEventsEventMessageType.class));
    }

    @Test
    public void testGetService() {
        EndDeviceEventsServiceProviderImpl provider = new EndDeviceEventsServiceProviderImpl();
        Assert.assertEquals(provider.getService(), EndDeviceEventsPort.class);
    }

    @Test
    public void testGet() {
        EndDeviceEventsServiceProviderImpl provider = new EndDeviceEventsServiceProviderImpl();
        Assert.assertEquals(provider.get().getClass(), SendEndDeviceEvents.class);
    }

    private static void inject(Class<?> clazz, Object instance, String fieldName, Object value) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(instance, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
