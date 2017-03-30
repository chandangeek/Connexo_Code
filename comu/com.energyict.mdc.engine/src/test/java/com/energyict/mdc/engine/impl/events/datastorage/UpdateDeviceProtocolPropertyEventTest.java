/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.events.datastorage;

import com.energyict.mdc.engine.events.Category;
import com.energyict.mdc.engine.impl.events.AbstractComServerEventImpl;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;

import java.time.Clock;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UpdateDeviceProtocolPropertyEventTest {

    public static final String TEST_PROPERTY_NAME = "testProperty";
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
        String propertyValue = "the new value";

        UpdateDeviceProtocolPropertyEvent event = new UpdateDeviceProtocolPropertyEvent(serviceProvider, deviceIdentifier, TEST_PROPERTY_NAME, propertyValue);
        assertThat(event.getCategory()).isEqualTo(Category.COLLECTED_DATA_PROCESSING);
    }

    @Test
    public void testToString() {
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(deviceIdentifier.toString()).thenReturn("My Device identifier");
        String propertyValue = "The new value";

        UpdateDeviceProtocolPropertyEvent event = new UpdateDeviceProtocolPropertyEvent(serviceProvider, deviceIdentifier, TEST_PROPERTY_NAME, propertyValue);
        // Business method
        String eventString = event.toString();

        // Asserts
        assertThat(eventString).matches("\\{.*\\}");
    }

    @Test
    public void testToStringNoDeviceIdentifier() {
        String propertyValue = "the new value";

        UpdateDeviceProtocolPropertyEvent event = new UpdateDeviceProtocolPropertyEvent(serviceProvider, null, TEST_PROPERTY_NAME, propertyValue);
        // Business method
        String eventString = event.toString();

        // Asserts
        assertThat(eventString).matches("\\{.*\\}");
    }

    @Test
    public void testToStringNoPropertySpec() {
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(deviceIdentifier.toString()).thenReturn("My Device identifier");
        String propertyValue = "the new value";

        UpdateDeviceProtocolPropertyEvent event = new UpdateDeviceProtocolPropertyEvent(serviceProvider, deviceIdentifier, null, propertyValue);
        // Business method
        String eventString = event.toString();

        // Asserts
        assertThat(eventString).matches("\\{.*\\}");
    }

    @Test
    public void testToStringNoPropertyValue() {
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(deviceIdentifier.toString()).thenReturn("My Device identifier");

        UpdateDeviceProtocolPropertyEvent event = new UpdateDeviceProtocolPropertyEvent(serviceProvider, deviceIdentifier, TEST_PROPERTY_NAME, null);

        // Business method
        String eventString = event.toString();

        // Asserts
        assertThat(eventString).matches("\\{.*\\}");
    }

}