package com.energyict.mdc.engine.impl.events.filtering;

import com.energyict.mdc.engine.events.ComServerEvent;
import com.energyict.mdc.engine.events.ComTaskExecutionEvent;
import com.energyict.mdc.engine.events.DeviceRelatedEvent;
import com.energyict.mdc.engine.impl.events.filtering.DeviceFilter;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import org.junit.*;

import java.util.Arrays;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link com.energyict.mdc.engine.impl.events.filtering.DeviceFilter} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-31 (12:22)
 */
public class DeviceFilterTest {

    @Test
    public void testMatchExpected () {
        BaseDevice interestedDevice = mock(BaseDevice.class);
        BaseDevice otherDevice = mock(BaseDevice.class);
        DeviceFilter filter = new DeviceFilter(Arrays.asList(interestedDevice));
        DeviceRelatedEvent event = mock(DeviceRelatedEvent.class);
        when(event.isDeviceRelated()).thenReturn(true);
        when(event.getDevice()).thenReturn(otherDevice);

        // Business method and assert
        assertThat(filter.matches(event)).isTrue();
    }

    @Test
    public void testNoMatchExpected () {
        BaseDevice interestedDevice = mock(BaseDevice.class);
        DeviceFilter filter = new DeviceFilter(Arrays.asList(interestedDevice));
        DeviceRelatedEvent event = mock(DeviceRelatedEvent.class);
        when(event.isDeviceRelated()).thenReturn(true);
        when(event.getDevice()).thenReturn(interestedDevice);

        // Business method and assert
        assertThat(filter.matches(event)).isFalse();
    }

    @Test
    public void testNoMatchExpectedForComTaskEventsOfTheInterestedDevices () {
        BaseDevice interestedDevice = mock(BaseDevice.class);
        DeviceFilter filter = new DeviceFilter(Arrays.asList(interestedDevice));
        ComTaskExecutionEvent event = mock(ComTaskExecutionEvent.class);
        when(event.isDeviceRelated()).thenReturn(true);
        when(event.getDevice()).thenReturn(interestedDevice);

        // Business method and assert
        assertThat(filter.matches(event)).isFalse();
    }

    @Test
    public void testMatchExpectedForComTaskEventOfOtherDevices () {
        BaseDevice interestedDevice = mock(BaseDevice.class);
        BaseDevice otherDevice = mock(BaseDevice.class);
        DeviceFilter filter = new DeviceFilter(Arrays.asList(interestedDevice));
        ComTaskExecutionEvent event = mock(ComTaskExecutionEvent.class);
        when(event.isDeviceRelated()).thenReturn(true);
        when(event.getDevice()).thenReturn(otherDevice);

        // Business method and assert
        assertThat(filter.matches(event)).isTrue();
    }

    @Test
    public void testNoMatchExpectedForNonDeviceEvents () {
        BaseDevice interestedDevice = mock(BaseDevice.class);
        DeviceFilter filter = new DeviceFilter(Arrays.asList(interestedDevice));
        ComServerEvent event = mock(ComServerEvent.class);
        when(event.isDeviceRelated()).thenReturn(false);

        // Business method and assert
        assertThat(filter.matches(event)).isFalse();
    }

    @Test
    public void testConstrutor () {
        BaseDevice interestedDevice = mock(BaseDevice.class);

        // Business method
        DeviceFilter filter = new DeviceFilter(Arrays.asList(interestedDevice));

        // Asserts
        assertThat(filter.getDevices()).containsOnly(interestedDevice);
    }

    @Test
    public void testUpdateDevice () {
        BaseDevice interestedDevice = mock(BaseDevice.class);
        BaseDevice otherDevice = mock(BaseDevice.class);
        DeviceFilter filter = new DeviceFilter(Arrays.asList(interestedDevice));

        // Business method
        filter.setDevices(Arrays.asList(otherDevice));

        // Asserts
        assertThat(filter.getDevices()).containsOnly(otherDevice);
    }

}