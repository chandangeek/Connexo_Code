/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.events.datastorage;

import com.energyict.mdc.engine.events.Category;
import com.energyict.mdc.engine.impl.events.AbstractComServerEventImpl;
import com.energyict.mdc.protocol.api.device.data.BreakerStatus;
import com.energyict.mdc.protocol.api.device.data.CollectedBreakerStatus;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;

import java.time.Clock;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author sva
 * @since 7/04/2016 - 16:12
 */
@RunWith(MockitoJUnitRunner.class)
public class CollectedBreakerStatusEventTest {

    @Mock
    private AbstractComServerEventImpl.ServiceProvider serviceProvider;

    @Before
    public void initMocks() {
        when(serviceProvider.clock()).thenReturn(Clock.systemDefaultZone());
    }

    @Test
    public void testCategory() {
        CollectedBreakerStatus breakerStatus = mock(CollectedBreakerStatus.class);

        CollectedBreakerStatusEvent event = new CollectedBreakerStatusEvent(serviceProvider, breakerStatus);
        assertThat(event.getCategory()).isEqualTo(Category.COLLECTED_DATA_PROCESSING);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoPayload() {
        // Business method
        new CollectedBreakerStatusEvent(serviceProvider, null);
    }

    @Test
    public void testToStringWithoutBreakerStatusInfo() {
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(deviceIdentifier.toString()).thenReturn("My Device identifier");
        CollectedBreakerStatus breakerStatus = mock(CollectedBreakerStatus.class);
        when(breakerStatus.getDeviceIdentifier()).thenReturn(deviceIdentifier);
        when(breakerStatus.getBreakerStatus()).thenReturn(Optional.empty());

        // Business method
        CollectedBreakerStatusEvent event = new CollectedBreakerStatusEvent(serviceProvider, breakerStatus);
        String eventString = event.toString();

        // Asserts
        assertThat(event.getCategory()).isEqualTo(Category.COLLECTED_DATA_PROCESSING);
        assertThat(eventString).matches("\\{.*\\}");
        assertThat(eventString).contains("Collected breaker status");
    }

    @Test
    public void testToStringWithBreakerStatusInfo() {
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(deviceIdentifier.toString()).thenReturn("My Device identifier");
        CollectedBreakerStatus breakerStatus = mock(CollectedBreakerStatus.class);
        when(breakerStatus.getDeviceIdentifier()).thenReturn(deviceIdentifier);
        when(breakerStatus.getBreakerStatus()).thenReturn(Optional.of(BreakerStatus.DISCONNECTED));

        // Business method
        CollectedBreakerStatusEvent event = new CollectedBreakerStatusEvent(serviceProvider, breakerStatus);
        String eventString = event.toString();

        // Asserts
        assertThat(event.getCategory()).isEqualTo(Category.COLLECTED_DATA_PROCESSING);
        assertThat(eventString).matches("\\{.*\\}");
        assertThat(eventString).contains("Collected breaker status");
        assertThat(eventString).contains("Collected breaker status");
        assertThat(eventString).contains("disconnected");
    }
}
