package com.energyict.mdc.engine.impl.events.datastorage;

import com.energyict.mdc.engine.events.Category;
import com.energyict.mdc.engine.impl.events.AbstractComServerEventImpl;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.CollectedRegisterList;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.RegisterIdentifier;
import com.energyict.obis.ObisCode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Copyrights EnergyICT
 * Date: 24/02/2016
 * Time: 16:19
 */
@RunWith(MockitoJUnitRunner.class)
public class CollectedRegisterListEventTest {

    @Mock
    private AbstractComServerEventImpl.ServiceProvider serviceProvider;

    @Before
    public void initMocks(){
        when(serviceProvider.clock()).thenReturn(Clock.systemDefaultZone());
    }

    @Test
    public void testCategory(){
        CollectedRegisterList registerList = mock(CollectedRegisterList.class);

        // Business method
        CollectedRegisterListEvent event = new CollectedRegisterListEvent(serviceProvider, registerList);
        assertThat(event.getCategory()).isEqualTo(Category.COLLECTED_DATA_PROCESSING);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoPayload(){
        // Business method
        new CollectedRegisterListEvent(serviceProvider, null);
    }

    @Test
    public void testToString(){
        Instant readTime = Instant.now();
        Instant toTime = readTime.minus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS);

        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(deviceIdentifier.toString()).thenReturn("My Device identifier");

        RegisterIdentifier firstIdentifier = mock(RegisterIdentifier.class);
        when(firstIdentifier.getRegisterObisCode()).thenReturn(new ObisCode(1,2,3,4,5,6));
        when(firstIdentifier.getDeviceIdentifier()).thenReturn(deviceIdentifier);
        RegisterIdentifier secondIdentifier = mock(RegisterIdentifier.class);
        when(secondIdentifier.getRegisterObisCode()).thenReturn(new ObisCode(9,8,7,6,5,4));
        when(secondIdentifier.getDeviceIdentifier()).thenReturn(deviceIdentifier);

        CollectedRegister firstRegister = mock(CollectedRegister.class);
        when(firstRegister.getRegisterIdentifier()).thenReturn(firstIdentifier);
        when(firstRegister.getCollectedQuantity()).thenReturn(new com.energyict.cbo.Quantity(new BigDecimal(265), com.energyict.cbo.Unit.get("kWh")));
        when(firstRegister.getReadTime()).thenReturn(Date.from(readTime));
        when(firstRegister.getToTime()).thenReturn(Date.from(toTime));

        CollectedRegisterList registerList = mock(CollectedRegisterList.class);
        when(registerList.getDeviceIdentifier()).thenReturn(deviceIdentifier);
        when(registerList.getCollectedRegisters()).thenReturn(Collections.singletonList(firstRegister));

        // Business method
        CollectedRegisterListEvent event = new CollectedRegisterListEvent(serviceProvider, registerList);

        // Business method
        String eventString = event.toString();

        // Asserts
        assertThat(eventString).matches("\\{.*\\}");
    }

    @Test
    public void testToStringWithoutCollectedRegister(){
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(deviceIdentifier.toString()).thenReturn("My Device identifier");

        CollectedRegisterList registerList = mock(CollectedRegisterList.class);
        when(registerList.getDeviceIdentifier()).thenReturn(deviceIdentifier);
        when(registerList.getCollectedRegisters()).thenReturn(Collections.emptyList());

        // Business method
        CollectedRegisterListEvent event = new CollectedRegisterListEvent(serviceProvider, registerList);

        // Business method
        String eventString = event.toString();

        // Asserts
        assertThat(eventString).matches("\\{.*\\}");
    }

    @Test
    public void testToStringWithEmptyCollectedRegister(){
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(deviceIdentifier.toString()).thenReturn("My Device identifier");

        CollectedRegister register = mock(CollectedRegister.class);

        CollectedRegisterList registerList = mock(CollectedRegisterList.class);
        when(registerList.getDeviceIdentifier()).thenReturn(deviceIdentifier);
        when(registerList.getCollectedRegisters()).thenReturn(Collections.singletonList(register));

        // Business method
        CollectedRegisterListEvent event = new CollectedRegisterListEvent(serviceProvider, registerList);

        // Business method
        String eventString = event.toString();

        // Asserts
        assertThat(eventString).matches("\\{.*\\}");
    }
}
