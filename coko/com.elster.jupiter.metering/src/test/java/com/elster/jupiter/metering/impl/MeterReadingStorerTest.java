package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.Status;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.EventType;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingStorer;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.metering.events.EndDeviceEventType;
import com.elster.jupiter.metering.readings.beans.EndDeviceEventImpl;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import javax.inject.Provider;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MeterReadingStorerTest {

    private static final String EVENTTYPECODE = "3.7.12.242";
    private static final Instant DATE = ZonedDateTime.of(2012, 12, 19, 11, 20, 33, 0, ZoneId.systemDefault()).toInstant();
    private static final long METER_ID = 165;
    @Mock
    private Meter meter;
    @Mock
    private ReadingStorer readingStorer;
    @Mock
    private DataMapper<EndDeviceEventType> endDeviceEventTypeFactory;
    @Mock
    private EndDeviceEventType eventType;
    @Mock
    private DataMapper<EndDeviceEventRecord> eventRecordFactory;
    @Mock
    private DataModel dataModel;
    @Mock
    private MeteringService meteringService;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private Provider<EndDeviceEventRecordImpl> deviceEventFactory;
    @Mock
    private EventService eventService;
    @Mock
    private EndDeviceEventRecord existing;
    @Mock
    private DataMapper<ReadingQualityRecord> readingQualityRecordFactory;

    @Before
    public void setUp() {
        when(meteringService.createOverrulingStorer()).thenReturn(readingStorer);
        when(dataModel.mapper(EndDeviceEventType.class)).thenReturn(endDeviceEventTypeFactory);
        when(dataModel.mapper(EndDeviceEventRecord.class)).thenReturn(eventRecordFactory);
        when(dataModel.mapper(ReadingQualityRecord.class)).thenReturn(readingQualityRecordFactory);
        when(deviceEventFactory.get()).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return new EndDeviceEventRecordImpl(dataModel, null);
            }
        });
        when(endDeviceEventTypeFactory.getOptional(EVENTTYPECODE)).thenReturn(Optional.of(eventType));
        when(eventRecordFactory.getOptional(METER_ID, EVENTTYPECODE, DATE)).thenReturn(Optional.empty());
        when(meter.getId()).thenReturn(METER_ID);
        when(eventType.getMRID()).thenReturn(EVENTTYPECODE);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testMeterReadingStorerOfNewEndDeviceEvent() {
        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        EndDeviceEventImpl endDeviceEvent = EndDeviceEventImpl.of(EVENTTYPECODE, DATE);
        HashMap<String, String> eventData = new HashMap<>();
        eventData.put("A", "B");
        endDeviceEvent.setEventData(eventData);
        endDeviceEvent.setReason("reason");
        endDeviceEvent.setSeverity("INFO");
        endDeviceEvent.setStatus(Status.builder().at(DATE).reason("reason").value("3").build());
        endDeviceEvent.setIssuerId("issuerid");
        endDeviceEvent.setIssuerTrackingId("issuerTrackingid");
        endDeviceEvent.setName("name");
        endDeviceEvent.setDescription("description");
        endDeviceEvent.setAliasName("alias");
        meterReading.addEndDeviceEvent(endDeviceEvent);
        MeterReadingStorer meterReadingStorer = new MeterReadingStorer(dataModel, meteringService, meter, meterReading, thesaurus, eventService, deviceEventFactory);

        meterReadingStorer.store();

        ArgumentCaptor<List> listCaptor = ArgumentCaptor.forClass(List.class);
        verify(eventRecordFactory).persist(listCaptor.capture());

        assertThat(listCaptor.getValue()).hasSize(1);

        EndDeviceEventRecord actual = (EndDeviceEventRecord) listCaptor.getValue().get(0);
        assertThat(actual.getEndDevice()).isEqualTo(meter);
        assertThat(actual.getEventType()).isEqualTo(eventType);
        assertThat(actual.getCreatedDateTime()).isEqualTo(DATE);
        assertThat(actual.getProperties()).contains(entry("A", "B"));

        assertThat(actual.getReason()).isEqualTo("reason");
        assertThat(actual.getSeverity()).isEqualTo("INFO");
        assertThat(actual.getStatus()).isEqualTo(Status.builder().at(DATE).reason("reason").value("3").build());
        assertThat(actual.getIssuerID()).isEqualTo("issuerid");
        assertThat(actual.getIssuerTrackingID()).isEqualTo("issuerTrackingid");
        assertThat(actual.getName()).isEqualTo("name");
        assertThat(actual.getDescription()).isEqualTo("description");
        assertThat(actual.getAliasName()).isEqualTo("alias");

        ArgumentCaptor<EndDeviceEventRecordImpl> argumentCaptor = ArgumentCaptor.forClass(EndDeviceEventRecordImpl.class);
        verify(eventService).postEvent(eq(EventType.END_DEVICE_EVENT_CREATED.topic()), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(actual);
        verify(eventService).postEvent(eq(EventType.METERREADING_CREATED.topic()), anyObject());
    }

}
