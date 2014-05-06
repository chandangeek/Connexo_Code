package com.energyict.mdc.engine.impl.core.inbound;

import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.engine.model.ModemBasedInboundComPort;
import com.energyict.mdc.exceptions.ModemException;
import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.mdc.protocol.api.channels.serial.BaudrateValue;
import com.energyict.mdc.protocol.api.channels.serial.FlowControl;
import com.energyict.mdc.protocol.api.channels.serial.NrOfDataBits;
import com.energyict.mdc.protocol.api.channels.serial.NrOfStopBits;
import com.energyict.mdc.protocol.api.channels.serial.Parities;
import com.energyict.mdc.protocol.api.dialer.core.DialerException;
import com.energyict.protocols.mdc.channels.serial.SerialComChannel;
import com.energyict.protocols.mdc.channels.serial.SerialComponentService;
import com.energyict.protocols.mdc.channels.serial.SerialPortConfiguration;
import com.energyict.protocols.mdc.channels.serial.ServerSerialPort;
import com.energyict.protocols.mdc.channels.serial.modem.AtModemComponent;
import org.joda.time.DateTimeConstants;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author sva
 * @since 21/11/12 (13:56)
 */
@RunWith(MockitoJUnitRunner.class)
public class SerialPortConnectorTest {

    @Mock
    private ModemBasedInboundComPort comPort;
    @Mock
    private SerialComponentService serialComponentService;

    @Before
    public void initializeMocks() throws IOException {
        when(comPort.getName()).thenReturn("COM3");
        when(comPort.getSerialPortConfiguration()).thenReturn(
                new SerialPortConfiguration("Unknown",
                        BaudrateValue.BAUDRATE_9600,
                        NrOfDataBits.EIGHT,
                        NrOfStopBits.ONE,
                        Parities.NONE,
                        FlowControl.NONE
                ));
        when(comPort.getRingCount()).thenReturn(3);
        when(comPort.getAtCommandTimeout()).thenReturn(new TimeDuration(1));
        when(comPort.getConnectTimeout()).thenReturn(new TimeDuration(3));
        when(comPort.getDelayAfterConnect()).thenReturn(new TimeDuration(1));
        when(comPort.getDelayBeforeSend()).thenReturn(new TimeDuration(0));
        when(comPort.getModemInitStrings()).thenReturn(
                new ArrayList<String>() {{
                    add("ATM0");
                }});
        when(comPort.getAtCommandTry()).thenReturn(new BigDecimal(3));
        when(comPort.getAddressSelector()).thenReturn("");
        when(comPort.getPostDialCommands()).thenReturn("");
    }

    @Test
    public void testProperAccept() throws IOException, DialerException {
        TestableSerialComChannel serialComChannel = getTestableComChannel();
        SerialPortConnector portConnector = Mockito.spy(new SerialPortConnector(comPort, serialComponentService));
        doReturn(serialComChannel).when(portConnector).getNewComChannel();

        serialComChannel.setResponses(new ArrayList<String>() {{
            add("OK");      // Answer at modem hang up command
            add("OK");      // Answer at modem restore profile settings
            add("OK");      // Answer at modem init string
            add("RING");    // First ring
            add("RING");    // 2th ring
            add("RING");    // 3th ring
            add("CONNECT"); // CONNECT
        }});

        serialComChannel.setResponseTimings(new ArrayList<Integer>() {{
            add(0);    // Answer at modem hang up command
            add(0);    // Answer at modem restore profile settings
            add(0);    // Answer at modem init string
            add(0);    // First ring
            add(0);    // 2th ring
            add(0);    // 3th ring
            add(0);    // CONNECT
        }});
        int expectedTime = calculateExpectedAcceptTime(comPort, serialComChannel.getResponseTimings());
        long timeBeforeConnect = System.currentTimeMillis();

        // Business methods
        portConnector.accept();

        long timeAfterConnect = System.currentTimeMillis();
        long connectTime = timeAfterConnect - timeBeforeConnect;
        long secs = connectTime / DateTimeConstants.MILLIS_PER_SECOND;

        // Asserts
        assertEquals("We should have received 3 rings.", 3, portConnector.getCurrentRingCount());
        assertThat(secs).isEqualTo(expectedTime);
    }

    @Test
    public void testProperAcceptWithAdditionalRubbish() throws ConnectionException, IOException, DialerException {
        TestableSerialComChannel serialComChannel = getTestableComChannel();
        SerialPortConnector portConnector = Mockito.spy(new SerialPortConnector(comPort, serialComponentService));
        doReturn(serialComChannel).when(portConnector).getNewComChannel();

        serialComChannel.setResponses(new ArrayList<String>() {{
            add("-RUBBISH-OK-RUBBISH");      // Answer at modem hang up command
            add("-RUBBISH-OK-RUBBISH-");      // Answer at modem restore profile settings
            add("-RUBBISH-OK-RUBBISH-");      // Answer at modem init string
            add("-RUBBISH-RING-RUBBISH-");    // First ring
            add("-RUBBISH-RING-RUBBISH-");    // 2th ring
            add("-RUBBISH-RING-RUBBISH-");    // 3th ring
            add("-RUBBISH--RUBBISH--RUBBISH-CONNECT-RUBBISH-"); // CONNECT
        }});

        serialComChannel.setResponseTimings(new ArrayList<Integer>() {{
            add(0);    // Answer at modem hang up command
            add(0);    // Answer at modem restore profile settings
            add(0);    // Answer at modem init string
            add(0);    // First ring
            add(0);    // 2th ring
            add(0);    // 3th ring
            add(0);    // CONNECT
        }});
        int expectedTime = calculateExpectedAcceptTime(comPort, serialComChannel.getResponseTimings());
        long timeBeforeConnect = System.currentTimeMillis();

        // Business methods
        portConnector.accept();

        long timeAfterConnect = System.currentTimeMillis();
        long connectTime = timeAfterConnect - timeBeforeConnect;
        long secs = connectTime / DateTimeConstants.MILLIS_PER_SECOND;

        // Asserts
        assertEquals("We should have received 3 rings.", 3, portConnector.getCurrentRingCount());
        assertEquals("InputStream of the comChannel should have been flushed.", -1, serialComChannel.available());
        assertThat(secs).isEqualTo(expectedTime);
    }

    @Test
    public void testProperAcceptWithDelayBeforeSend() throws ConnectionException, IOException, DialerException {
        TestableSerialComChannel serialComChannel = getTestableComChannel();
        SerialPortConnector portConnector = Mockito.spy(new SerialPortConnector(comPort, serialComponentService));
        doReturn(serialComChannel).when(portConnector).getNewComChannel();
        when(comPort.getDelayBeforeSend()).thenReturn(new TimeDuration(500, TimeDuration.MILLISECONDS));

        serialComChannel.setResponses(new ArrayList<String>() {{
            add("OK");      // Answer at modem hang up command
            add("OK");      // Answer at modem restore profile settings
            add("OK");      // Answer at modem init string
            add("RING");    // First ring
            add("RING");    // 2th ring
            add("RING");    // 3th ring
            add("CONNECT"); // CONNECT
        }});

        serialComChannel.setResponseTimings(new ArrayList<Integer>() {{
            add(0);    // Answer at modem hang up command
            add(0);    // Answer at modem restore profile settings
            add(0);    // Answer at modem init string
            add(0);    // First ring
            add(0);    // 2th ring
            add(0);    // 3th ring
            add(0);    // CONNECT
        }});
        int expectedTime = calculateExpectedAcceptTime(comPort, serialComChannel.getResponseTimings());
        long timeBeforeConnect = System.currentTimeMillis();

        // Business methods
        portConnector.accept();

        long timeAfterConnect = System.currentTimeMillis();
        long connectTime = timeAfterConnect - timeBeforeConnect;
        long secs = connectTime / DateTimeConstants.MILLIS_PER_SECOND;

        // Asserts
        assertEquals("We should have received 3 rings.", 3, portConnector.getCurrentRingCount());
        assertThat(secs).isEqualTo(expectedTime);
    }

    @Test
    public void testProperAcceptMultipleInitStrings() throws ConnectionException, IOException, DialerException {
        TestableSerialComChannel serialComChannel = getTestableComChannel();
        SerialPortConnector portConnector = Mockito.spy(new SerialPortConnector(comPort, serialComponentService));
        doReturn(serialComChannel).when(portConnector).getNewComChannel();
        when(comPort.getModemInitStrings()).thenReturn(
                new ArrayList<String>() {{
                    add("FIRST INIT");
                    add("2TH INIT");
                }});

        serialComChannel.setResponses(new ArrayList<String>() {{
            add("OK");      // Answer at modem hang up command
            add("OK");      // Answer at modem restore profile settings
            add("OK");      // Answer at modem init string 1
            add("OK");      // Answer at modem init string 2
            add("RING");    // First ring
            add("RING");    // 2th ring
            add("RING");    // 3th ring
            add("CONNECT"); // CONNECT
        }});

        serialComChannel.setResponseTimings(new ArrayList<Integer>() {{
            add(0);    // Answer at modem hang up command
            add(0);    // Answer at modem restore profile settings
            add(0);    // Answer at modem init string 1
            add(0);    // Answer at modem init string 2
            add(0);    // First ring
            add(0);    // 2th ring
            add(0);    // 3th ring
            add(0);    // CONNECT
        }});
        int expectedTime = calculateExpectedAcceptTime(comPort, serialComChannel.getResponseTimings());
        long timeBeforeConnect = System.currentTimeMillis();

        // Business methods
        portConnector.accept();

        long timeAfterConnect = System.currentTimeMillis();
        long connectTime = timeAfterConnect - timeBeforeConnect;
        long secs = connectTime / DateTimeConstants.MILLIS_PER_SECOND;

        // Asserts
        assertEquals("We should have received 3 rings.", 3, portConnector.getCurrentRingCount());
        assertThat(secs).isGreaterThanOrEqualTo(expectedTime);
    }

    @Test
    public void testProperAcceptWithLongConnectDelay() throws ConnectionException, IOException, DialerException {
        TestableSerialComChannel serialComChannel = getTestableComChannel();
        SerialPortConnector portConnector = Mockito.spy(new SerialPortConnector(comPort, serialComponentService));
        doReturn(serialComChannel).when(portConnector).getNewComChannel();

        serialComChannel.setResponses(new ArrayList<String>() {{
            add("OK");      // Answer at modem hang up command
            add("OK");      // Answer at modem restore profile settings
            add("OK");      // Answer at modem init string
            add("RING");    // First ring
            add("RING");    // 2th ring
            add("RING");    // 3th ring
            add("CONNECT"); // CONNECT
        }});

        serialComChannel.setResponseTimings(new ArrayList<Integer>() {{
            add(0);    // Answer at modem hang up command
            add(0);    // Answer at modem restore profile settings
            add(0);    // Answer at modem init string
            add(0);    // First ring
            add(0);    // 2th ring
            add(0);    // 3th ring
            add(2);    // CONNECT after 2 seconds (which is greater than AtCommandTimeout, but below the ConnectTimeout)
        }});
        int expectedRunTime = calculateExpectedAcceptTime(comPort, serialComChannel.getResponseTimings());
        long timeBeforeConnect = System.currentTimeMillis();

        // Business methods
        portConnector.accept();

        long timeAfterConnect = System.currentTimeMillis();
        long connectTime = timeAfterConnect - timeBeforeConnect;
        long secs = connectTime / DateTimeConstants.MILLIS_PER_SECOND;

        // Asserts
        assertEquals("We should have received 3 rings.", 3, portConnector.getCurrentRingCount());
        assertThat(secs).isGreaterThanOrEqualTo(expectedRunTime);
    }

    @Test
    public void testProperAcceptButTimeoutBetweenRings() throws ConnectionException, IOException, DialerException {
        TestableSerialComChannel serialComChannel = getTestableComChannel();
        SerialPortConnector portConnector = Mockito.spy(new SerialPortConnector(comPort, serialComponentService));
        doReturn(serialComChannel).when(portConnector).getNewComChannel();

        serialComChannel.setResponses(new ArrayList<String>() {{
            add("OK");      // Answer at modem hang up command
            add("OK");      // Answer at modem restore profile settings
            add("OK");      // Answer at modem init string
            add("RING");    // First ring
            add("RING");    // 2th ring
            // 3Th ring not received within time -> Timeout & discard of all previous rings received -> looking again for a sequence of 3 rings
            add("RING");    // 1th ring of new sequence
            add("RING");    // 2th ring
            add("RING");    // 3th ring
            add("CONNECT"); // CONNECT
        }});

        serialComChannel.setResponseTimings(new ArrayList<Integer>() {{
            add(0);    // Answer at modem hang up command
            add(0);    // Answer at modem restore profile settings
            add(0);    // Answer at modem init string
            add(0);    // First ring
            add(0);    // 2th ring
            add(3);    // 1th ring of new sequence
            add(0);    // 2th ring
            add(0);    // 3th ring
            add(0);    // CONNECT
        }});
        int expectedRunTime = calculateExpectedAcceptTime(comPort, serialComChannel.getResponseTimings());
        long timeBeforeConnect = System.currentTimeMillis();

        // Business methods
        portConnector.accept();

        long timeAfterConnect = System.currentTimeMillis();
        long connectTime = timeAfterConnect - timeBeforeConnect;
        long secs = connectTime / DateTimeConstants.MILLIS_PER_SECOND;

        // Asserts
        assertEquals("We should have received 3 rings.", 3, portConnector.getCurrentRingCount());
        assertThat(secs).isGreaterThanOrEqualTo(expectedRunTime);
    }

    @Test(expected = ModemException.class)
    public void testNoConnectDueToTimeout() throws ConnectionException, IOException, DialerException {
        TestableSerialComChannel serialComChannel = getTestableComChannel();
        SerialPortConnector portConnector = Mockito.spy(new SerialPortConnector(comPort, serialComponentService));
        doReturn(serialComChannel).when(portConnector).getNewComChannel();

        serialComChannel.setResponses(new ArrayList<String>() {{
            add("OK");      // Answer at modem hang up command
            add("OK");      // Answer at modem restore profile settings
            add("OK");      // Answer at modem init string
            add("RING");    // First ring
            add("RING");    // 2th ring
            add("RING");    // 3th ring
            add("RUBBISH -- RUBISH -- RUBISH");
            add("CONNECT AFTER CONNECT_TIMEOUT"); // CONNECT
        }});

        serialComChannel.setResponseTimings(new ArrayList<Integer>() {{
            add(0);    // Answer at modem hang up command
            add(0);    // Answer at modem restore profile settings
            add(0);    // Answer at modem init string
            add(0);    // First ring
            add(0);    // 2th ring
            add(0);    // 3th ring
            add(0);    // Rubbish
            add(4);    // CONNECT after 4 seconds (which is greater than AtCommandTimeout & ConnectTimeout)
        }});
        int expectedRunTime = 4;    // 1 second delay before modem hang up + 3 seconds before modem connect timeout should occur
        long timeBeforeConnect = System.currentTimeMillis();

        // Business methods
        try {
            portConnector.accept();
        } catch (ModemException e) {
            long timeAfterConnect = System.currentTimeMillis();
            long connectTime = timeAfterConnect - timeBeforeConnect;
            long secs = connectTime / DateTimeConstants.MILLIS_PER_SECOND;

            assertThat(secs).isGreaterThanOrEqualTo(expectedRunTime);
            throw e;
        }
    }


    @Test(expected = ModemException.class)
    public void testTimeoutAtModemCommandWithRetries() throws ConnectionException, IOException, DialerException {
        TestableSerialComChannel serialComChannel = getTestableComChannel();
        SerialPortConnector portConnector = Mockito.spy(new SerialPortConnector(comPort, serialComponentService));
        doReturn(serialComChannel).when(portConnector).getNewComChannel();

        serialComChannel.setResponses(new ArrayList<String>() {{
            add("RUBBISH - RUBBISH");      // Answer at modem hang up command (first try)
        }});

        serialComChannel.setResponseTimings(new ArrayList<Integer>() {{
            add(0);    // Answer at modem hang up command
        }});
        int expectedRunTime = 1 + (3*1);    // Delay before hang up + 3 tries to hang up
        long timeBeforeConnect = System.currentTimeMillis();

        // Business methods
        try {
            portConnector.accept();
        } catch (ModemException e) {
            long timeAfterConnect = System.currentTimeMillis();
            long connectTime = timeAfterConnect - timeBeforeConnect;
            long secs = connectTime / DateTimeConstants.MILLIS_PER_SECOND;

            assertThat(secs).isGreaterThanOrEqualTo(expectedRunTime);
            throw e;
        }
    }

    //    @Test // this test can NOT run on Bamboo, only for local testing of you own ComPorts
    public void hardwareModemTest() throws ConnectionException, IOException, DialerException {
        List<String> initStrings = new ArrayList<>();
        initStrings.add("ATM0");

        when(comPort.getRingCount()).thenReturn(3);
        when(comPort.getAtCommandTimeout()).thenReturn(new TimeDuration(5));
        when(comPort.getConnectTimeout()).thenReturn(new TimeDuration(30));
        when(comPort.getDelayAfterConnect()).thenReturn(new TimeDuration(4));
        when(comPort.getDelayBeforeSend()).thenReturn(new TimeDuration(0));
        when(comPort.getModemInitStrings()).thenReturn(initStrings);
        when(comPort.getAtCommandTry()).thenReturn(new BigDecimal(3));
        when(comPort.getAddressSelector()).thenReturn("");

        // Business methods
        SerialPortConnector portConnector = new SerialPortConnector(comPort, serialComponentService);
        portConnector.accept();

        // Asserts
        assertEquals("We should have received 3 rings.", 3, portConnector.getCurrentRingCount());
    }

    private TestableSerialComChannel getTestableComChannel() {
        InputStream inputStream = mock(InputStream.class);
        OutputStream outputStream = mock(OutputStream.class);
        ServerSerialPort serialPort = mock(ServerSerialPort.class);
        when(serialPort.getInputStream()).thenReturn(inputStream);
        when(serialPort.getOutputStream()).thenReturn(outputStream);
        return new TestableSerialComChannel(serialPort);
    }

    /**
     * Calculate the expected time in which the accept should be completed - taking into account all possible timings/delays.
     * @param comPort
     * @param responseTimings
     * @return
     */
    private int calculateExpectedAcceptTime(ModemBasedInboundComPort comPort, List<Integer> responseTimings) {
        long expectedTime = comPort.getAtCommandTimeout().getMilliSeconds();    // Delay before reinitialization of the modem
        expectedTime += (2 * comPort.getDelayBeforeSend().getMilliSeconds());   // Delay before the send of modem hang up & modem reinitialization
        expectedTime += (comPort.getModemInitStrings().size() * comPort.getDelayBeforeSend().getMilliSeconds()); // Delay before the send of all init strings
        for (Integer i : responseTimings) {                                     // Delay before the receive of each response
            expectedTime += (i * 1000);
        }
        expectedTime += comPort.getDelayBeforeSend().getMilliSeconds();         // Delay before send out of connect command
        expectedTime += comPort.getDelayAfterConnect().getMilliSeconds();       // Delay to wait after the actual connect
        return (int) (expectedTime / 1000);
    }

    protected class TestableSerialComChannel extends SerialComChannel {

        private int counter = 0;
        private int index = 0;

        /**
         * The list of responses
         */
        private List<String> responses = new ArrayList<>();

        /**
         * The timing to respect - this is the timing BETWEEN responses.
         * Thus when handling response string responses[x], first there will be a delay of responseTimings seconds, simulating the real-world communication delays.
         */
        private List<Integer> responseTimings = new ArrayList<>();
        private long timingOfNextResponse;
        private boolean requestToFlushInputStream;

        public TestableSerialComChannel(ServerSerialPort serialPort) {
            super(serialPort);
        }

        public void setResponses(List<String> responses) {
            this.responses = responses;
        }

        public void setResponseTimings(List<Integer> responseTimings) {
            this.responseTimings = responseTimings;
        }

        public List<Integer> getResponseTimings() {
            return responseTimings;
        }

        @Override
        public int available() {
            if (requestToFlushInputStream) {
                requestToFlushInputStream = false;
                return -1;  // Flushing of inputStream is skipped.
            }

            // The next response is not yet available
            if (timingOfNextResponse > new Date().getTime()) {
                return -1;
            }

            if (responses.size() > counter) {
                int availabilities = responses.get(counter).length() - index;
                if (availabilities == 0) {
                    index = 0;
                    counter++;
                    if (responseTimings.size() > counter) {
                        timingOfNextResponse = new Date().getTime() + (responseTimings.get(counter) * DateTimeConstants.MILLIS_PER_SECOND);
                    } else {
                        timingOfNextResponse = 0;
                    }
                }
                return availabilities;
            }
            return -1;
        }

        @Override
        public int doWrite(byte[] bytes) {
            if (new String(bytes).equals(AtModemComponent.DISCONNECT_SEQUENCE)) {
                // Writing the disconnect sequence is always followed by a flush of the inputStream.
                requestToFlushInputStream = true;
            }
            // nothing else to do
            return bytes.length;
        }


        @Override
        public int doRead() {
            if (responses.size() > counter) {
                return (int) responses.get(counter).charAt(index++);
            }
            return -1;
        }
    }
}
