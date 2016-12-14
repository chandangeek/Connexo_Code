package com.energyict.mdc.engine.impl.events.datastorage;

import com.energyict.mdc.engine.events.Category;
import com.energyict.mdc.engine.impl.events.AbstractComServerEventImpl;
import com.energyict.mdc.engine.impl.meterdata.NoLogBooksForDevice;
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

/**
 * Copyrights EnergyICT
 * Date: 24/02/2016
 * Time: 16:49
 */
@RunWith(MockitoJUnitRunner.class)
public class CollectedNoLogBooksForDeviceEventTest {

    @Mock
    private AbstractComServerEventImpl.ServiceProvider serviceProvider;

    @Before
    public void initMocks(){
        when(serviceProvider.clock()).thenReturn(Clock.systemDefaultZone());
    }

    @Test
    public void testCategory(){
        NoLogBooksForDevice noLogBooksForDevice = mock(NoLogBooksForDevice.class);

        // Business method
        CollectedNoLogBooksForDeviceEvent event = new CollectedNoLogBooksForDeviceEvent(serviceProvider, noLogBooksForDevice);
        assertThat(event.getCategory()).isEqualTo(Category.COLLECTED_DATA_PROCESSING);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoPayload(){
        // Business method
        new CollectedNoLogBooksForDeviceEvent(serviceProvider, null);
    }

    @Test
    public void testToString(){
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(deviceIdentifier.toString()).thenReturn("My Device identifier");

        NoLogBooksForDevice noLogBooksForDevice = mock(NoLogBooksForDevice.class);
        when(noLogBooksForDevice.getDeviceIdentifier()).thenReturn(deviceIdentifier);

        // Business method
        CollectedNoLogBooksForDeviceEvent event = new CollectedNoLogBooksForDeviceEvent(serviceProvider, noLogBooksForDevice);

        // Business method
        String eventString = event.toString();

        // Asserts
        assertThat(eventString).matches("\\{.*\\}");
    }

}
