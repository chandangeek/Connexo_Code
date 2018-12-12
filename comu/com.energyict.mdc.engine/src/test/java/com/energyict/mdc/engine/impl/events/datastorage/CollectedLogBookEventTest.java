/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.events.datastorage;

import com.energyict.mdc.device.data.impl.identifiers.LogBookIdentifierById;
import com.energyict.mdc.engine.events.Category;
import com.energyict.mdc.engine.impl.events.AbstractComServerEventImpl;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.MeterProtocolEvent;

import java.time.Clock;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CollectedLogBookEventTest {

    @Mock
    private AbstractComServerEventImpl.ServiceProvider serviceProvider;

    @Before
    public void initMocks(){
        when(serviceProvider.clock()).thenReturn(Clock.systemDefaultZone());
    }

    @Test
    public void testCategory(){
        CollectedLogBook logBook = mock(CollectedLogBook.class);

        // Business method
        CollectedLogBookEvent event = new CollectedLogBookEvent(serviceProvider, logBook);
        assertThat(event.getCategory()).isEqualTo(Category.COLLECTED_DATA_PROCESSING);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoPayload(){
        // Business method
        new CollectedLogBookEvent(serviceProvider, null);
    }

    @Test
    public void testToString(){
        MeterProtocolEvent firstEvent = mock(MeterProtocolEvent.class);
        MeterProtocolEvent secondEvent = mock(MeterProtocolEvent.class);

        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(deviceIdentifier.toString()).thenReturn("My Device identifier");

        LogBookIdentifierById logBookId = new LogBookIdentifierById(625L, ObisCode.fromString("1.1.1.1.1.1"), deviceIdentifier);

        CollectedLogBook logBook = mock(CollectedLogBook.class);
        when(logBook.getLogBookIdentifier()).thenReturn(logBookId);
        when(logBook.getCollectedMeterEvents()).thenReturn(Arrays.asList(firstEvent, secondEvent));

        // Business method
        CollectedLogBookEvent event = new CollectedLogBookEvent(serviceProvider, logBook);

        // Business method
        String eventString = event.toString();

        // Asserts
        assertThat(eventString).matches("\\{.*\\}");
    }

    @Test
    public void testToStringWithoutEvents(){
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(deviceIdentifier.toString()).thenReturn("My Device identifier");

        LogBookIdentifierById logBookId = new LogBookIdentifierById(625L, ObisCode.fromString("1.1.1.1.1.1"), deviceIdentifier);

        CollectedLogBook logBook = mock(CollectedLogBook.class);
        when(logBook.getLogBookIdentifier()).thenReturn(logBookId);
        when(logBook.getCollectedMeterEvents()).thenReturn(Collections.emptyList());

        // Business method
        CollectedLogBookEvent event = new CollectedLogBookEvent(serviceProvider, logBook);

        // Business method
        String eventString = event.toString();

        // Asserts
        assertThat(eventString).matches("\\{.*\\}");
    }

}
