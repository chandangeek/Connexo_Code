/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.impl.adapters.meterprotocol;

import com.energyict.mdc.protocol.api.exceptions.LegacyProtocolException;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;

import java.io.IOException;
import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link MeterProtocolClockAdapter}
 *
 * @author gna
 * @since 4/04/12 - 15:54
 */
@RunWith(MockitoJUnitRunner.class)
public class MeterProtocolClockAdapterTest {

    /**
     * IOExceptions should be properly handled by the adapter
     *
     * @throws java.io.IOException if a direct call to {@link MeterProtocol#setTime()} is made
     */
    @Test(expected = LegacyProtocolException.class)
    public void setTimeTest() throws IOException {
        MeterProtocol meterProtocol = mock(MeterProtocol.class);
        doThrow(new IOException("Couldn't set the time")).when(meterProtocol).setTime();
        MeterProtocolClockAdapter meterProtocolClockAdapter = new MeterProtocolClockAdapter(meterProtocol);
        meterProtocolClockAdapter.setTime(new Date());
    }

    @Test(expected = LegacyProtocolException.class)
    public void getTimeExceptionTest() throws IOException {
        MeterProtocol meterProtocol = mock(MeterProtocol.class);
        when(meterProtocol.getTime()).thenThrow(new IOException("Failed to get the time"));
        MeterProtocolClockAdapter meterProtocolClockAdapter = new MeterProtocolClockAdapter(meterProtocol);
        meterProtocolClockAdapter.getTime();
    }

    @Test
    public void getCorrectTimeTest() throws IOException {
        final Long currentTime = System.currentTimeMillis();
        MeterProtocol meterProtocol = mock(MeterProtocol.class);
        when(meterProtocol.getTime()).thenReturn(new Date(currentTime));
        MeterProtocolClockAdapter meterProtocolClockAdapter = new MeterProtocolClockAdapter(meterProtocol);
        assertEquals(new Date(currentTime), meterProtocolClockAdapter.getTime());
    }
}
