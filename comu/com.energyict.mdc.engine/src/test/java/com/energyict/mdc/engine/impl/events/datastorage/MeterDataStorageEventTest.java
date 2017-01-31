/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.events.datastorage;

import com.elster.jupiter.metering.readings.EndDeviceEvent;
import com.elster.jupiter.metering.readings.IntervalBlock;
import com.elster.jupiter.metering.readings.Reading;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.util.Pair;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.engine.events.Category;
import com.energyict.mdc.engine.impl.commands.store.MeterDataStoreCommandImpl;
import com.energyict.mdc.engine.impl.events.AbstractComServerEventImpl;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;

import java.time.Clock;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MeterDataStorageEventTest {

    @Mock
    private AbstractComServerEventImpl.ServiceProvider serviceProvider;

    @Before
    public void initMocks(){
        when(serviceProvider.clock()).thenReturn(Clock.systemDefaultZone());
    }

    @Test
    public void testCategory(){
        MeterDataStoreCommandImpl command = mock(MeterDataStoreCommandImpl.class);

        // Business method
        MeterDataStorageEvent event = new MeterDataStorageEvent(serviceProvider, command);
        assertThat(event.getCategory()).isEqualTo(Category.COLLECTED_DATA_PROCESSING);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoPayload(){
        // Business method
        new MeterDataStorageEvent(serviceProvider, null);
    }

    @Test
    public void testToString(){
        EndDeviceEvent event1 = mock(EndDeviceEvent.class);
        EndDeviceEvent event2 = mock(EndDeviceEvent.class);
        EndDeviceEvent event3 = mock(EndDeviceEvent.class);
        Reading reading1 = mock(Reading.class);
        Reading reading2 = mock(Reading.class);
        Reading reading3 = mock(Reading.class);
        Reading reading4 = mock(Reading.class);
        IntervalBlock intervalBlock1 = mock(IntervalBlock.class);
        IntervalBlock intervalBlock2 = mock(IntervalBlock.class);
        IntervalBlock intervalBlock3 = mock(IntervalBlock.class);
        IntervalBlock intervalBlock4 = mock(IntervalBlock.class);
        IntervalBlock intervalBlock5 = mock(IntervalBlock.class);

        MeterReadingImpl meterReading = mock(MeterReadingImpl.class);
        when(meterReading.getEvents()).thenReturn(Arrays.asList(event1, event2, event3));
        when(meterReading.getReadings()).thenReturn(Arrays.asList(reading1,reading2,reading3,reading4));
        when(meterReading.getIntervalBlocks()).thenReturn(Arrays.asList(intervalBlock1,intervalBlock2,intervalBlock3,intervalBlock4,intervalBlock5));

        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(deviceIdentifier.toString()).thenReturn("My Device identifier");
        HashMap<String, Pair<DeviceIdentifier<Device>, MeterReadingImpl>> readings = new HashMap<>();
        readings.put("MRID1", Pair.of(deviceIdentifier,meterReading ));
        MeterDataStoreCommandImpl command = mock(MeterDataStoreCommandImpl.class);
        when(command.getMeterReadings()).thenReturn(readings);

        MeterDataStorageEvent event = new MeterDataStorageEvent(serviceProvider, command);
        // Business method
        String eventString = event.toString();

        // Asserts
        assertThat(eventString).matches("\\{.*\\}");
    }

    @Test
    public void testToStringWithoutEvents(){
        Reading reading1 = mock(Reading.class);
        Reading reading2 = mock(Reading.class);
        Reading reading3 = mock(Reading.class);
        Reading reading4 = mock(Reading.class);
        IntervalBlock intervalBlock1 = mock(IntervalBlock.class);
        IntervalBlock intervalBlock2 = mock(IntervalBlock.class);
        IntervalBlock intervalBlock3 = mock(IntervalBlock.class);
        IntervalBlock intervalBlock4 = mock(IntervalBlock.class);
        IntervalBlock intervalBlock5 = mock(IntervalBlock.class);

        MeterReadingImpl meterReading = mock(MeterReadingImpl.class);
        when(meterReading.getEvents()).thenReturn(Collections.emptyList());
        when(meterReading.getReadings()).thenReturn(Arrays.asList(reading1,reading2,reading3,reading4));
        when(meterReading.getIntervalBlocks()).thenReturn(Arrays.asList(intervalBlock1,intervalBlock2,intervalBlock3,intervalBlock4,intervalBlock5));

        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(deviceIdentifier.toString()).thenReturn("My Device identifier");
        HashMap<String, Pair<DeviceIdentifier<Device>, MeterReadingImpl>> readings = new HashMap<>();
        readings.put("MRID1", Pair.of(deviceIdentifier,meterReading ));
        MeterDataStoreCommandImpl command = mock(MeterDataStoreCommandImpl.class);
        when(command.getMeterReadings()).thenReturn(readings);

        MeterDataStorageEvent event = new MeterDataStorageEvent(serviceProvider, command);
        // Business method
        String eventString = event.toString();

        // Asserts
        assertThat(eventString).matches("\\{.*\\}");
    }

    @Test
    public void testToStringWithNullEvents(){
        Reading reading1 = mock(Reading.class);
        Reading reading2 = mock(Reading.class);
        Reading reading3 = mock(Reading.class);
        Reading reading4 = mock(Reading.class);
        IntervalBlock intervalBlock1 = mock(IntervalBlock.class);
        IntervalBlock intervalBlock2 = mock(IntervalBlock.class);
        IntervalBlock intervalBlock3 = mock(IntervalBlock.class);
        IntervalBlock intervalBlock4 = mock(IntervalBlock.class);
        IntervalBlock intervalBlock5 = mock(IntervalBlock.class);

        MeterReadingImpl meterReading = mock(MeterReadingImpl.class);
        when(meterReading.getReadings()).thenReturn(Arrays.asList(reading1,reading2,reading3,reading4));
        when(meterReading.getIntervalBlocks()).thenReturn(Arrays.asList(intervalBlock1,intervalBlock2,intervalBlock3,intervalBlock4,intervalBlock5));

        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(deviceIdentifier.toString()).thenReturn("My Device identifier");
        HashMap<String, Pair<DeviceIdentifier<Device>, MeterReadingImpl>> readings = new HashMap<>();
        readings.put("MRID1", Pair.of(deviceIdentifier,meterReading ));
        MeterDataStoreCommandImpl command = mock(MeterDataStoreCommandImpl.class);
        when(command.getMeterReadings()).thenReturn(readings);

        MeterDataStorageEvent event = new MeterDataStorageEvent(serviceProvider, command);
        // Business method
        String eventString = event.toString();

        // Asserts
        assertThat(eventString).matches("\\{.*\\}");
    }

    @Test
    public void testToStringWithoutReadings(){
        EndDeviceEvent event1 = mock(EndDeviceEvent.class);
        EndDeviceEvent event2 = mock(EndDeviceEvent.class);
        EndDeviceEvent event3 = mock(EndDeviceEvent.class);
        IntervalBlock intervalBlock1 = mock(IntervalBlock.class);
        IntervalBlock intervalBlock2 = mock(IntervalBlock.class);
        IntervalBlock intervalBlock3 = mock(IntervalBlock.class);
        IntervalBlock intervalBlock4 = mock(IntervalBlock.class);
        IntervalBlock intervalBlock5 = mock(IntervalBlock.class);

        MeterReadingImpl meterReading = mock(MeterReadingImpl.class);
        when(meterReading.getEvents()).thenReturn(Arrays.asList(event1, event2, event3));
        when(meterReading.getReadings()).thenReturn(Collections.emptyList());
        when(meterReading.getIntervalBlocks()).thenReturn(Arrays.asList(intervalBlock1,intervalBlock2,intervalBlock3,intervalBlock4,intervalBlock5));

        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(deviceIdentifier.toString()).thenReturn("My Device identifier");
        HashMap<String, Pair<DeviceIdentifier<Device>, MeterReadingImpl>> readings = new HashMap<>();
        readings.put("MRID1", Pair.of(deviceIdentifier,meterReading ));
        MeterDataStoreCommandImpl command = mock(MeterDataStoreCommandImpl.class);
        when(command.getMeterReadings()).thenReturn(readings);

        MeterDataStorageEvent event = new MeterDataStorageEvent(serviceProvider, command);
        // Business method
        String eventString = event.toString();

        // Asserts
        assertThat(eventString).matches("\\{.*\\}");
    }

    @Test
    public void testToStringWithNullReadings(){
        EndDeviceEvent event1 = mock(EndDeviceEvent.class);
        EndDeviceEvent event2 = mock(EndDeviceEvent.class);
        EndDeviceEvent event3 = mock(EndDeviceEvent.class);
        IntervalBlock intervalBlock1 = mock(IntervalBlock.class);
        IntervalBlock intervalBlock2 = mock(IntervalBlock.class);
        IntervalBlock intervalBlock3 = mock(IntervalBlock.class);
        IntervalBlock intervalBlock4 = mock(IntervalBlock.class);
        IntervalBlock intervalBlock5 = mock(IntervalBlock.class);

        MeterReadingImpl meterReading = mock(MeterReadingImpl.class);
        when(meterReading.getEvents()).thenReturn(Arrays.asList(event1, event2, event3));
        when(meterReading.getIntervalBlocks()).thenReturn(Arrays.asList(intervalBlock1,intervalBlock2,intervalBlock3,intervalBlock4,intervalBlock5));

        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(deviceIdentifier.toString()).thenReturn("My Device identifier");
        HashMap<String, Pair<DeviceIdentifier<Device>, MeterReadingImpl>> readings = new HashMap<>();
        readings.put("MRID1", Pair.of(deviceIdentifier,meterReading ));
        MeterDataStoreCommandImpl command = mock(MeterDataStoreCommandImpl.class);
        when(command.getMeterReadings()).thenReturn(readings);

        MeterDataStorageEvent event = new MeterDataStorageEvent(serviceProvider, command);
        // Business method
        String eventString = event.toString();

        // Asserts
        assertThat(eventString).matches("\\{.*\\}");
    }

    @Test
    public void testToStringWithoutIntervalBlocks(){
        EndDeviceEvent event1 = mock(EndDeviceEvent.class);
        EndDeviceEvent event2 = mock(EndDeviceEvent.class);
        EndDeviceEvent event3 = mock(EndDeviceEvent.class);
        Reading reading1 = mock(Reading.class);
        Reading reading2 = mock(Reading.class);
        Reading reading3 = mock(Reading.class);
        Reading reading4 = mock(Reading.class);

        MeterReadingImpl meterReading = mock(MeterReadingImpl.class);
        when(meterReading.getEvents()).thenReturn(Arrays.asList(event1, event2, event3));
        when(meterReading.getReadings()).thenReturn(Arrays.asList(reading1,reading2,reading3,reading4));
        when(meterReading.getIntervalBlocks()).thenReturn(Collections.emptyList());

        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(deviceIdentifier.toString()).thenReturn("My Device identifier");
        HashMap<String, Pair<DeviceIdentifier<Device>, MeterReadingImpl>> readings = new HashMap<>();
        readings.put("MRID1", Pair.of(deviceIdentifier,meterReading ));
        MeterDataStoreCommandImpl command = mock(MeterDataStoreCommandImpl.class);
        when(command.getMeterReadings()).thenReturn(readings);

        MeterDataStorageEvent event = new MeterDataStorageEvent(serviceProvider, command);
        // Business method
        String eventString = event.toString();

        // Asserts
        assertThat(eventString).matches("\\{.*\\}");
    }

    @Test
    public void testToStringNullIntervalBlocks(){
        EndDeviceEvent event1 = mock(EndDeviceEvent.class);
        EndDeviceEvent event2 = mock(EndDeviceEvent.class);
        EndDeviceEvent event3 = mock(EndDeviceEvent.class);
        Reading reading1 = mock(Reading.class);
        Reading reading2 = mock(Reading.class);
        Reading reading3 = mock(Reading.class);
        Reading reading4 = mock(Reading.class);

        MeterReadingImpl meterReading = mock(MeterReadingImpl.class);
        when(meterReading.getEvents()).thenReturn(Arrays.asList(event1, event2, event3));
        when(meterReading.getReadings()).thenReturn(Arrays.asList(reading1,reading2,reading3,reading4));

        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(deviceIdentifier.toString()).thenReturn("My Device identifier");
        HashMap<String, Pair<DeviceIdentifier<Device>, MeterReadingImpl>> readings = new HashMap<>();
        readings.put("MRID1", Pair.of(deviceIdentifier,meterReading ));
        MeterDataStoreCommandImpl command = mock(MeterDataStoreCommandImpl.class);
        when(command.getMeterReadings()).thenReturn(readings);

        MeterDataStorageEvent event = new MeterDataStorageEvent(serviceProvider, command);
        // Business method
        String eventString = event.toString();

        // Asserts
        assertThat(eventString).matches("\\{.*\\}");
    }

    @Test
    public void testToStringCompletelyEmpty(){

        MeterDataStoreCommandImpl command = mock(MeterDataStoreCommandImpl.class);

        MeterDataStorageEvent event = new MeterDataStorageEvent(serviceProvider, command);
        // Business method
        String eventString = event.toString();

        // Asserts
        assertThat(eventString).matches("\\{.*\\}");
    }
}
