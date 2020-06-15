package com.elster.jupiter.cim.webservices.inbound.soap.servicecall;

import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig.bean.MeterInfo;

import ch.iec.tc57._2011.masterdatalinkageconfig.Meter;
import ch.iec.tc57._2011.masterdatalinkageconfig.Name;
import com.google.common.collect.ImmutableList;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MeterInfoTest {

    @Test
    public void testConstructorWithoutName() {
        String mrid = "my mrid";
        String role = "my role";
        Meter meter = mockMeter(mrid, role);

        MeterInfo meterInfo = new MeterInfo(meter);

        verifyMeter(mrid, role, meterInfo);
        assertNull(meterInfo.getName());
    }

    @Test
    public void testConstructorWithName() {
        String mrid = "my mrid";
        String role = "my role";
        Meter meter = mockMeter(mrid, role);
        Name nameObj = mock(Name.class);
        String name = "my name";
        when(nameObj.getName()).thenReturn(name);
        when(meter.getNames()).thenReturn(ImmutableList.of(nameObj));

        MeterInfo meterInfo = new MeterInfo(meter);

        verifyMeter(mrid, role, meterInfo);
        assertEquals(name, meterInfo.getName());
    }

    private Meter mockMeter(String mrid, String role) {
        Meter meter = mock(Meter.class);
        when(meter.getMRID()).thenReturn(mrid);
        when(meter.getRole()).thenReturn(role);
        return meter;
    }

    private void verifyMeter(String mrid, String role, MeterInfo meterInfo) {
        assertEquals(mrid, meterInfo.getMrid());
        assertEquals(role, meterInfo.getRole());
    }

}
