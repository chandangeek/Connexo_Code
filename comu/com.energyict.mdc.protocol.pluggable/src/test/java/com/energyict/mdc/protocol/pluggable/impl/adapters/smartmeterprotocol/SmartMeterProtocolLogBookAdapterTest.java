package com.energyict.mdc.protocol.pluggable.impl.adapters.smartmeterprotocol;

import com.elster.jupiter.util.time.impl.DefaultClock;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.issues.Bus;
import com.energyict.mdc.issues.impl.IssueServiceImpl;
import com.energyict.mdc.protocol.api.LogBookReader;
import com.energyict.mdc.protocol.api.device.data.CollectedLogBook;
import com.energyict.mdc.protocol.api.device.data.ResultType;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.mdc.protocol.api.legacy.SmartMeterProtocol;
import com.energyict.protocolimplv2.identifiers.LogBookIdentifierById;
import org.joda.time.DateMidnight;
import org.junit.*;
import org.junit.runner.*;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.*;
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

    private static final Date LAST_LOGBOOK1 = new DateMidnight(2012, 11, 30).toDate();
    private static final Date LAST_LOGBOOK2 = new DateMidnight(2012, 10, 31).toDate();
    private static final Date LAST_LOGBOOK3 = new DateMidnight(2012, 9, 30).toDate();

    private static final int LOGBOOK1_ID = 1;
    private static final int LOGBOOK2_ID = 2;
    private static final int LOGBOOK3_ID = 3;

    private static final Date EVENT1_DATE = new DateMidnight(2012, 12, 5).toDate();
    private static final Date EVENT2_DATE = new DateMidnight(2012, 12, 10).toDate();

    private static final int PROTOCOL_CODE_EVENT1 = 11;
    private static final int PROTOCOL_CODE_EVENT2 = 22;

    private static final String SERIAL_NUMBER = "SerialNumber";

    private IssueServiceImpl issueService;

    @Before
    public void initializeIssueService() {
        issueService = new IssueServiceImpl();
        issueService.setClock(new DefaultClock());
        Bus.setIssueService(issueService);
    }

    @After
    public void cleanupIssueService() {
        Bus.clearIssueService(issueService);
    }

    @Test
    public void testGetLogBookData() throws IOException {
        List<LogBookReader> logBookReaders = new ArrayList<>();
        logBookReaders.add(new LogBookReader(LOGBOOK1_OBIS, LAST_LOGBOOK1, new LogBookIdentifierById(LOGBOOK1_ID), SERIAL_NUMBER));
        logBookReaders.add(new LogBookReader(LOGBOOK_OBIS, LAST_LOGBOOK2, new LogBookIdentifierById(LOGBOOK2_ID), SERIAL_NUMBER));
        logBookReaders.add(new LogBookReader(LOGBOOK_OBIS, LAST_LOGBOOK3, new LogBookIdentifierById(LOGBOOK3_ID), SERIAL_NUMBER));

        List<MeterEvent> meterEvents = new ArrayList<>(2);
        meterEvents.add(new MeterEvent(EVENT1_DATE, MeterEvent.BATTERY_VOLTAGE_LOW, PROTOCOL_CODE_EVENT1));
        meterEvents.add(new MeterEvent(EVENT2_DATE, MeterEvent.TAMPER, PROTOCOL_CODE_EVENT2));

        SmartMeterProtocol deviceProtocol = mock(SmartMeterProtocol.class);
        when(deviceProtocol.getMeterEvents(LAST_LOGBOOK2)).thenReturn(meterEvents);
        when(deviceProtocol.getMeterEvents(LAST_LOGBOOK3)).thenThrow(new IOException("IOException while reading logBook 3."));

        SmartMeterProtocolLogBookAdapter smartMeterProtocolLogBookAdapter = new SmartMeterProtocolLogBookAdapter(deviceProtocol);

        // Business method
        List<CollectedLogBook> logBookData = smartMeterProtocolLogBookAdapter.getLogBookData(logBookReaders);

        // Asserts
        assertThat(logBookReaders.size()).isEqualTo(logBookData.size());
        assertThat(logBookReaders.get(0).getMeterSerialNumber()).isEqualTo(SERIAL_NUMBER);
        assertThat(logBookReaders.get(1).getMeterSerialNumber()).isEqualTo(SERIAL_NUMBER);
        assertThat(logBookReaders.get(2).getMeterSerialNumber()).isEqualTo(SERIAL_NUMBER);

        assertThat(LOGBOOK1_ID).isEqualTo(((LogBookIdentifierById) logBookData.get(0).getLogBookIdentifier()).getLogBookId());
        assertThat(ResultType.NotSupported).isEqualTo(logBookData.get(0).getResultType());
        assertThat(true).isEqualTo(logBookData.get(0).getIssues().get(0).getDescription().contains("logBookXnotsupported"));
        assertEquals(logBookData.get(0).getIssues().get(0).getSource(), LOGBOOK1_OBIS);

        assertThat(LOGBOOK2_ID).isEqualTo(((LogBookIdentifierById) logBookData.get(1).getLogBookIdentifier()).getLogBookId());
        assertThat(ResultType.Supported).isEqualTo(logBookData.get(1).getResultType());
        assertThat(logBookData.get(1).getIssues()).isEmpty();
        assertThat(meterEvents.size()).isEqualTo(logBookData.get(1).getCollectedMeterEvents().size());

        assertThat(EVENT1_DATE).isEqualTo(logBookData.get(1).getCollectedMeterEvents().get(0).getTime());
        assertThat(MeterEvent.BATTERY_VOLTAGE_LOW).isEqualTo(logBookData.get(1).getCollectedMeterEvents().get(0).getEiCode());
        assertThat(PROTOCOL_CODE_EVENT1).isEqualTo(logBookData.get(1).getCollectedMeterEvents().get(0).getProtocolCode());

        assertThat(EVENT2_DATE).isEqualTo(logBookData.get(1).getCollectedMeterEvents().get(1).getTime());
        assertThat(MeterEvent.TAMPER).isEqualTo(logBookData.get(1).getCollectedMeterEvents().get(1).getEiCode());
        assertThat(PROTOCOL_CODE_EVENT2).isEqualTo(logBookData.get(1).getCollectedMeterEvents().get(1).getProtocolCode());

        assertThat(LOGBOOK3_ID).isEqualTo(((LogBookIdentifierById) logBookData.get(2).getLogBookIdentifier()).getLogBookId());
        assertThat(ResultType.InCompatible).isEqualTo(logBookData.get(2).getResultType());
        assertThat(true).isEqualTo(logBookData.get(2).getIssues().get(0).getDescription().contains("logBookXissue"));
        assertEquals(logBookData.get(2).getIssues().get(0).getSource(), LOGBOOK_OBIS);
    }
}
