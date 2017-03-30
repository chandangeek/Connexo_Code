/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.events.datastorage;

import com.energyict.mdc.engine.events.Category;
import com.energyict.mdc.engine.impl.events.AbstractComServerEventImpl;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Clock;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class StoreConfigurationEventTest {

    @Mock
    private AbstractComServerEventImpl.ServiceProvider serviceProvider;

    @Before
    public void initMocks(){
        when(serviceProvider.clock()).thenReturn(Clock.systemDefaultZone());
    }

    @Test
    public void testCategory(){
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(deviceIdentifier.toString()).thenReturn("My Device identifier");

        StoreConfigurationEvent event = new StoreConfigurationEvent(serviceProvider, deviceIdentifier);
        assertThat(event.getCategory()).isEqualTo(Category.COLLECTED_DATA_PROCESSING);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoPayload(){
        // Business method
        new StoreConfigurationEvent(serviceProvider, null);
    }

    @Test
    public void testToString() {
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(deviceIdentifier.toString()).thenReturn("My Device identifier");

        StoreConfigurationEvent event = new StoreConfigurationEvent(serviceProvider, deviceIdentifier);
        // Business method
        String eventString = event.toString();

        // Asserts
        assertThat(eventString).matches("\\{.*\\}");
    }

}
