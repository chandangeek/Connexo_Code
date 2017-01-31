/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.impl.adapters.smartmeterprotocol;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.events.EndDeviceEventType;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.issues.Problem;
import com.energyict.mdc.protocol.api.LogBookReader;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedLogBook;
import com.energyict.mdc.protocol.api.device.data.ResultType;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.LogBookIdentifier;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.mdc.protocol.api.legacy.SmartMeterProtocol;
import com.energyict.mdc.protocol.pluggable.impl.adapters.smartmeterprotocol.mocks.MockCollectedLogBook;

import org.joda.time.DateMidnight;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author sva
 * @since 20/12/12 - 10:01
 */
@RunWith(MockitoJUnitRunner.class)
public class SmartMeterProtocolLogBookAdapterTest {

    private static final ObisCode LOGBOOK1_OBIS = ObisCode.fromString("1.1.0.0.0.255");
    private static final ObisCode LOGBOOK_OBIS = ObisCode.fromString("0.0.99.98.0.255");

    private static final Instant LAST_LOGBOOK1 = Instant.ofEpochMilli(1354233600000L);   // Midnight of Nov 30, 2012
    private static final Instant LAST_LOGBOOK2 = Instant.ofEpochMilli(1351641600000L);   // Midnight of Oct 31, 2012
    private static final Instant LAST_LOGBOOK3 = Instant.ofEpochMilli(1348963200000L);   // Midnight of Sept 30, 2012

    private static final Date EVENT1_DATE = new DateMidnight(2012, 12, 5).toDate();
    private static final Date EVENT2_DATE = new DateMidnight(2012, 12, 10).toDate();

    private static final int PROTOCOL_CODE_EVENT1 = 11;
    private static final int PROTOCOL_CODE_EVENT2 = 22;

    private static final String SERIAL_NUMBER = "SerialNumber";

    @Mock
    private IssueService issueService;
    @Mock
    private CollectedDataFactory collectedDataFactory;
    @Mock
    private MeteringService meteringService;
    @Mock
    private EndDeviceEventType otherEndDeviceEventType;

    private DeviceIdentifier<?> serialNumberDeviceIdentifier = new TestSerialNumberDeviceIdentifier(SERIAL_NUMBER);
    private Clock clock = Clock.systemUTC();

    @Before
    public void initializeMocks () {
        when(this.collectedDataFactory.createCollectedLogBook(any(LogBookIdentifier.class))).
                thenAnswer(invocationOnMock -> {
                    LogBookIdentifier logBookIdentifier = (LogBookIdentifier) invocationOnMock.getArguments()[0];
                    MockCollectedLogBook collectedLoadProfile = new MockCollectedLogBook(logBookIdentifier);
                    collectedLoadProfile.setResultType(ResultType.Supported);
                    return collectedLoadProfile;
                });
        when(this.otherEndDeviceEventType.getName()).thenReturn("0.0.0.0");
        when(this.otherEndDeviceEventType.getMRID()).thenReturn("0.0.0.0");
        Optional<EndDeviceEventType> optionalEndDeviceEvent = Optional.of(otherEndDeviceEventType);
        when(this.meteringService.getEndDeviceEventType(anyString())).thenReturn(optionalEndDeviceEvent);
    }

    @Before
    public void initializeIssueService () {
        when(this.issueService.newProblem(any(), any(), anyVararg())).thenAnswer(invocationOnMock -> {
            Problem problem = mock(Problem.class);
            when(problem.getSource()).thenReturn(invocationOnMock.getArguments()[0]);
            when(problem.getDescription()).thenReturn(String.valueOf(invocationOnMock.getArguments()[1]));
            return problem;
        });
    }

    @Test
    public void testGetLogBookData() throws IOException {
        List<LogBookReader> logBookReaders = new ArrayList<>();
        LogBookIdentifier logBookIdentifier1 = mock(LogBookIdentifier.class);
        LogBookIdentifier logBookIdentifier2 = mock(LogBookIdentifier.class);
        LogBookIdentifier logBookIdentifier3 = mock(LogBookIdentifier.class);
        logBookReaders.add(new LogBookReader(this.clock, LOGBOOK1_OBIS, Optional.of(LAST_LOGBOOK1), logBookIdentifier1, serialNumberDeviceIdentifier, SERIAL_NUMBER));
        logBookReaders.add(new LogBookReader(this.clock, LOGBOOK_OBIS, Optional.of(LAST_LOGBOOK2), logBookIdentifier2, serialNumberDeviceIdentifier, SERIAL_NUMBER));
        logBookReaders.add(new LogBookReader(this.clock, LOGBOOK_OBIS, Optional.of(LAST_LOGBOOK3), logBookIdentifier3, serialNumberDeviceIdentifier, SERIAL_NUMBER));

        List<MeterEvent> meterEvents = new ArrayList<>(2);
        meterEvents.add(new MeterEvent(EVENT1_DATE, MeterEvent.BATTERY_VOLTAGE_LOW, PROTOCOL_CODE_EVENT1));
        meterEvents.add(new MeterEvent(EVENT2_DATE, MeterEvent.TAMPER, PROTOCOL_CODE_EVENT2));

        SmartMeterProtocol deviceProtocol = mock(SmartMeterProtocol.class);
        when(deviceProtocol.getMeterEvents(Date.from(LAST_LOGBOOK2))).thenReturn(meterEvents);
        when(deviceProtocol.getMeterEvents(Date.from(LAST_LOGBOOK3))).thenThrow(new IOException("IOException while reading logBook 3."));

        SmartMeterProtocolLogBookAdapter smartMeterProtocolLogBookAdapter = new SmartMeterProtocolLogBookAdapter(deviceProtocol, issueService, collectedDataFactory, meteringService);

        // Business method
        List<CollectedLogBook> logBookData = smartMeterProtocolLogBookAdapter.getLogBookData(logBookReaders);

        // Asserts
        assertThat(logBookReaders.size()).isEqualTo(logBookData.size());
        assertThat(logBookReaders.get(0).getDeviceIdentifier().getIdentifier()).isEqualTo(SERIAL_NUMBER);
        assertThat(logBookReaders.get(0).getMeterSerialNumber()).isEqualTo(SERIAL_NUMBER);
        assertThat(logBookReaders.get(1).getDeviceIdentifier().getIdentifier()).isEqualTo(SERIAL_NUMBER);
        assertThat(logBookReaders.get(1).getMeterSerialNumber()).isEqualTo(SERIAL_NUMBER);
        assertThat(logBookReaders.get(2).getDeviceIdentifier().getIdentifier()).isEqualTo(SERIAL_NUMBER);
        assertThat(logBookReaders.get(2).getMeterSerialNumber()).isEqualTo(SERIAL_NUMBER);

        assertThat(logBookData.get(0).getLogBookIdentifier()).isEqualTo(logBookIdentifier1);
        assertThat(ResultType.NotSupported).isEqualTo(logBookData.get(0).getResultType());

        assertThat(logBookData.get(1).getLogBookIdentifier()).isEqualTo(logBookIdentifier2);
        assertThat(ResultType.Supported).isEqualTo(logBookData.get(1).getResultType());
        assertThat(logBookData.get(1).getIssues()).isEmpty();
        assertThat(meterEvents.size()).isEqualTo(logBookData.get(1).getCollectedMeterEvents().size());

        assertThat(EVENT1_DATE).isEqualTo(logBookData.get(1).getCollectedMeterEvents().get(0).getTime());
        assertThat(MeterEvent.BATTERY_VOLTAGE_LOW).isEqualTo(logBookData.get(1).getCollectedMeterEvents().get(0).getEiCode());
        assertThat(PROTOCOL_CODE_EVENT1).isEqualTo(logBookData.get(1).getCollectedMeterEvents().get(0).getProtocolCode());

        assertThat(EVENT2_DATE).isEqualTo(logBookData.get(1).getCollectedMeterEvents().get(1).getTime());
        assertThat(MeterEvent.TAMPER).isEqualTo(logBookData.get(1).getCollectedMeterEvents().get(1).getEiCode());
        assertThat(PROTOCOL_CODE_EVENT2).isEqualTo(logBookData.get(1).getCollectedMeterEvents().get(1).getProtocolCode());

        assertThat(logBookData.get(2).getLogBookIdentifier()).isEqualTo(logBookIdentifier3);
        assertThat(ResultType.InCompatible).isEqualTo(logBookData.get(2).getResultType());
        assertThat(logBookData.get(2).getIssues().get(0).getDescription()).isNotEmpty();
        assertEquals(logBookData.get(2).getIssues().get(0).getSource(), LOGBOOK_OBIS);
    }

}