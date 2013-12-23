package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.ReadingStorer;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.metering.events.EndDeviceEventType;
import com.elster.jupiter.metering.impl.test.EndDeviceEventImpl;
import com.elster.jupiter.metering.impl.test.MeterReadingImpl;
import com.elster.jupiter.orm.DataMapper;
import com.google.common.base.Optional;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MeterReadingStorerTest {

    private static final String EVENTTYPECODE = "3.7.12.242";
    private static final Date DATE = new DateTime(2012, 12, 19, 11, 20, 33, 0).toDate();
    @Mock
    private Meter meter;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ServiceLocator serviceLocator;
    @Mock
    private ReadingStorer readingStorer;
    @Mock
    private DataMapper<EndDeviceEventType> endDeviceEventTypeFactory;
    @Mock
    private EndDeviceEventType eventType;
    @Mock
    private DataMapper<EndDeviceEventRecord> eventRecordFactory;

    @Before
    public void setUp() {
        Bus.setServiceLocator(serviceLocator);

        when(serviceLocator.getMeteringService().createOverrulingStorer()).thenReturn(readingStorer);
        when(serviceLocator.getOrmClient().getEndDeviceEventTypeFactory()).thenReturn(endDeviceEventTypeFactory);
        when(serviceLocator.getOrmClient().getEndDeviceEventRecordFactory()).thenReturn(eventRecordFactory);
        when(endDeviceEventTypeFactory.getOptional(EVENTTYPECODE)).thenReturn(Optional.of(eventType));
    }

    @After
    public void tearDown() {
        Bus.clearServiceLocator(serviceLocator);
    }

    @Test
    public void testMeterReadingStorer() {
        MeterReadingImpl meterReading = new MeterReadingImpl();
        EndDeviceEventImpl endDeviceEvent = new EndDeviceEventImpl();
        endDeviceEvent.createdDateTime = DATE;
        endDeviceEvent.eventTypeCode = EVENTTYPECODE;
        endDeviceEvent.eventData.put("A", "B");
        meterReading.addEndDeviceEvent(endDeviceEvent);
        MeterReadingStorer meterReadingStorer = new MeterReadingStorer(meter, meterReading);

        meterReadingStorer.store();

        ArgumentCaptor<List> listCaptor = ArgumentCaptor.forClass(List.class);
        verify(eventRecordFactory).persist(listCaptor.capture());

        assertThat(listCaptor.getValue()).hasSize(1);

        EndDeviceEventRecord actual = (EndDeviceEventRecord) listCaptor.getValue().get(0);
        assertThat(actual.getEndDevice()).isEqualTo(meter);
        assertThat(actual.getEventType()).isEqualTo(eventType);
        assertThat(actual.getCreatedDateTime()).isEqualTo(DATE);
        assertThat(actual.getProperties()).contains(entry("A", "B"));
    }


}
