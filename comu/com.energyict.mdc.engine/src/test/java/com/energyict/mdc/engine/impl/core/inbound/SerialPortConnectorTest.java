package com.energyict.mdc.engine.impl.core.inbound;

import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.channels.serial.BaudrateValue;
import com.energyict.mdc.channels.serial.FlowControl;
import com.energyict.mdc.channels.serial.NrOfDataBits;
import com.energyict.mdc.channels.serial.NrOfStopBits;
import com.energyict.mdc.channels.serial.Parities;
import com.energyict.mdc.channels.serial.SerialPortConfiguration;
import com.energyict.mdc.channels.serial.ServerSerialPort;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.engine.config.ModemBasedInboundComPort;
import com.energyict.mdc.engine.impl.events.EventPublisher;
import com.energyict.mdc.io.ModemException;
import com.energyict.mdc.io.impl.MessageSeeds;
import com.energyict.mdc.io.impl.SerialIOAtModemComponentServiceImpl;
import com.energyict.mdc.io.serial.SerialComponentService;
import com.energyict.mdc.protocol.ComChannelType;
import com.energyict.mdc.protocol.SerialPortComChannel;
import com.energyict.mdc.protocol.api.impl.HexServiceImpl;
import com.energyict.mdc.protocol.api.services.HexService;
import com.energyict.mdc.upl.properties.TypedProperties;

import org.joda.time.DateTimeConstants;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
 * Tests the {@link SerialPortConnector} component.
 *
 * @author sva
 * @since 21/11/12 (13:56)
 */
@RunWith(MockitoJUnitRunner.class)
public class SerialPortConnectorTest {

    @Mock
    private ModemBasedInboundComPort comPort;
    @Mock
    private EventPublisher eventPublisher;
    @Mock
    private DeviceMessageService deviceMessageService;

    private SerialComponentService serialComponentService;
    private HexService hexService;
    private Clock clock = Clock.systemUTC();

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
        when(comPort.getModemInitStrings()).thenReturn(Collections.singletonList("ATM0"));
        when(comPort.getAtCommandTry()).thenReturn(new BigDecimal(3));
        when(comPort.getAddressSelector()).thenReturn("");
        when(comPort.getPostDialCommands()).thenReturn("");

        this.serialComponentService = new SerialIOAtModemComponentServiceImpl();
        this.hexService = new HexServiceImpl();
    }

    @Test
    public void testProperAccept() throws IOException {
        TestableSerialComChannel serialComChannel = getTestableComChannel();
        SerialPortConnector portConnector = Mockito.spy(new SerialPortConnector(comPort, serialComponentService, this.hexService, this.eventPublisher, this.clock, this.deviceMessageService));
        doReturn(serialComChannel).when(portConnector).getNewComChannel();

        serialComChannel.setResponses(Arrays.asList(
                "OK",      // Answer at modem hang up command
                "OK",      // Answer at modem restore profile settings
                "OK",      // Answer at modem init string
                "RING",    // First ring
                "RING",    // 2nd ring
                "RING",    // 3rd ring
                "CONNECT")); // CONNECT

        serialComChannel.setResponseTimings(Arrays.asList(
                0,    // Answer at modem hang up command
                0,    // Answer at modem restore profile settings
                0,    // Answer at modem init string
                0,    // First ring
                0,    // 2nd ring
                0,    // 3rd ring
                0));    // CONNECT
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
    public void testProperAcceptWithAdditionalRubbish() throws IOException {
        TestableSerialComChannel serialComChannel = getTestableComChannel();
        SerialPortConnector portConnector = Mockito.spy(new SerialPortConnector(comPort, serialComponentService, this.hexService, eventPublisher, this.clock, this.deviceMessageService));
        doReturn(serialComChannel).when(portConnector).getNewComChannel();

        serialComChannel.setResponses(Arrays.asList(
                "-RUBBISH-OK-RUBBISH",      // Answer at modem hang up command
                "-RUBBISH-OK-RUBBISH-",      // Answer at modem restore profile settings
                "-RUBBISH-OK-RUBBISH-",      // Answer at modem init string
                "-RUBBISH-RING-RUBBISH-",    // First ring
                "-RUBBISH-RING-RUBBISH-",    // 2nd ring
                "-RUBBISH-RING-RUBBISH-",    // 3rd ring
                "-RUBBISH--RUBBISH--RUBBISH-CONNECT-RUBBISH-")); // CONNECT

        serialComChannel.setResponseTimings(Arrays.asList(
                0,    // Answer at modem hang up command
                0,    // Answer at modem restore profile settings
                0,    // Answer at modem init string
                0,    // First ring
                0,    // 2nd ring
                0,    // 3rd ring
                0));  // CONNECT
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
    public void testProperAcceptWithDelayBeforeSend() throws IOException {
        TestableSerialComChannel serialComChannel = getTestableComChannel();
        SerialPortConnector portConnector = Mockito.spy(new SerialPortConnector(comPort, serialComponentService, this.hexService, eventPublisher, this.clock, this.deviceMessageService));
        doReturn(serialComChannel).when(portConnector).getNewComChannel();
        when(comPort.getDelayBeforeSend()).thenReturn(new TimeDuration(500, TimeDuration.TimeUnit.MILLISECONDS));

        serialComChannel.setResponses(Arrays.asList(
                "OK",      // Answer at modem hang up command
                "OK",      // Answer at modem restore profile settings
                "OK",      // Answer at modem init string
                "RING",    // First ring
                "RING",    // 2nd ring
                "RING",    // 3rd ring
                "CONNECT")); // CONNECT

        serialComChannel.setResponseTimings(Arrays.asList(
                0,    // Answer at modem hang up command
                0,    // Answer at modem restore profile settings
                0,    // Answer at modem init string
                0,    // First ring
                0,    // 2nd ring
                0,    // 3rd ring
                0));  // CONNECT
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
    public void testProperAcceptMultipleInitStrings() throws IOException {
        TestableSerialComChannel serialComChannel = getTestableComChannel();
        SerialPortConnector portConnector = Mockito.spy(new SerialPortConnector(comPort, serialComponentService, this.hexService, eventPublisher, this.clock, this.deviceMessageService));
        doReturn(serialComChannel).when(portConnector).getNewComChannel();
        when(comPort.getGlobalModemInitStrings()).thenReturn(Arrays.asList("GLOBAL INIT"));
        when(comPort.getModemInitStrings()).thenReturn(Arrays.asList("FIRST INIT", "2TH INIT"));

        serialComChannel.setResponses(Arrays.asList(
                "OK",      // Answer at modem hang up command
                "OK",      // Answer at modem restore profile settings
                "OK",      // Answer at global modem init string
                "OK",      // Answer at modem init string 1
                "OK",      // Answer at modem init string 2
                "RING",    // First ring
                "RING",    // 2nd ring
                "RING",    // 3rd ring
                "CONNECT"));// CONNECT

        serialComChannel.setResponseTimings(Arrays.asList(
                0,    // Answer at modem hang up command
                0,    // Answer at modem restore profile settings
                0,    // Answer at global modem init string
                0,    // Answer at modem init string 1
                0,    // Answer at modem init string 2
                0,    // First ring
                0,    // 2nd ring
                0,    // 3rd ring
                0));  // CONNECT
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
    public void testProperAcceptWithLongConnectDelay() throws IOException {
        TestableSerialComChannel serialComChannel = getTestableComChannel();
        SerialPortConnector portConnector = Mockito.spy(new SerialPortConnector(comPort, serialComponentService, this.hexService, eventPublisher, this.clock, this.deviceMessageService));
        doReturn(serialComChannel).when(portConnector).getNewComChannel();

        serialComChannel.setResponses(Arrays.asList(
                "OK",      // Answer at modem hang up command
                "OK",      // Answer at modem restore profile settings
                "OK",      // Answer at modem init string
                "RING",    // First ring
                "RING",    // 2nd ring
                "RING",    // 3rd ring
                "CONNECT")); // CONNECT

        serialComChannel.setResponseTimings(Arrays.asList(
                0,    // Answer at modem hang up command
                0,    // Answer at modem restore profile settings
                0,    // Answer at modem init string
                0,    // First ring
                0,    // 2nd ring
                0,    // 3rd ring
                2));  // CONNECT after 2 seconds (which is greater than AtCommandTimeout, but below the ConnectTimeout)
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
    public void testProperAcceptButTimeoutBetweenRings() throws IOException {
        TestableSerialComChannel serialComChannel = getTestableComChannel();
        SerialPortConnector portConnector = Mockito.spy(new SerialPortConnector(comPort, serialComponentService, this.hexService, eventPublisher, this.clock, this.deviceMessageService));
        doReturn(serialComChannel).when(portConnector).getNewComChannel();

        serialComChannel.setResponses(Arrays.asList(
                "OK",      // Answer at modem hang up command
                "OK",      // Answer at modem restore profile settings
                "OK",      // Answer at modem init string
                "RING",    // First ring
                "RING",    // 2nd ring
                // 3rd ring not received within time -> Timeout & discard of all previous rings received -> looking again for a sequence of 3 rings
                "RING",    // 1th ring of new sequence
                "RING",    // 2nd ring
                "RING",    // 3rd ring
                "CONNECT")); // CONNECT

        serialComChannel.setResponseTimings(Arrays.asList(
                0,    // Answer at modem hang up command
                0,    // Answer at modem restore profile settings
                0,    // Answer at modem init string
                0,    // First ring
                0,    // 2nd ring
                3,    // 1th ring of new sequence
                0,    // 2nd ring
                0,    // 3rd ring
                0));  // CONNECT
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
    public void testNoConnectDueToTimeout() throws IOException {
        TestableSerialComChannel serialComChannel = getTestableComChannel();
        SerialPortConnector portConnector = Mockito.spy(new SerialPortConnector(comPort, serialComponentService, this.hexService, eventPublisher, this.clock, this.deviceMessageService));
        doReturn(serialComChannel).when(portConnector).getNewComChannel();

        serialComChannel.setResponses(Arrays.asList(
                "OK",      // Answer at modem hang up command
                "OK",      // Answer at modem restore profile settings
                "OK",      // Answer at modem init string
                "RING",    // First ring
                "RING",    // 2nd ring
                "RING",    // 3rd ring
                "RUBBISH -- RUBISH -- RUBISH",
                "CONNECT AFTER CONNECT_TIMEOUT")); // CONNECT

        serialComChannel.setResponseTimings(Arrays.asList(
                0,    // Answer at modem hang up command
                0,    // Answer at modem restore profile settings
                0,    // Answer at modem init string
                0,    // First ring
                0,    // 2nd ring
                0,    // 3rd ring
                0,    // Rubbish
                4));    // CONNECT after 4 seconds (which is greater than AtCommandTimeout & ConnectTimeout)
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
    public void testTimeoutAtModemCommandWithRetries() throws IOException {
        TestableSerialComChannel serialComChannel = getTestableComChannel();
        SerialPortConnector portConnector = Mockito.spy(new SerialPortConnector(comPort, serialComponentService, this.hexService, eventPublisher, this.clock, this.deviceMessageService));
        doReturn(serialComChannel).when(portConnector).getNewComChannel();

//        serialComChannel.setResponses(Arrays.asList("RUBBISH - RUBBISH", "RUBBISH - RUBBISH", "RUBBISH - RUBBISH"));      // Answer at modem hang up command (first try)

        serialComChannel.setResponseTimings(Collections.singletonList(0));    // Answer at modem hang up command
        int expectedRunTime = 1 + (3 * 1);    // Delay before hang up + 3 tries to hang up
        long timeBeforeConnect = System.currentTimeMillis();

        // Business methods
        try {
            portConnector.accept();
        } catch (ModemException e) {
            assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.MODEM_COULD_NOT_HANG_UP);
            long timeAfterConnect = System.currentTimeMillis();
            long connectTime = timeAfterConnect - timeBeforeConnect;
            long secs = connectTime / DateTimeConstants.MILLIS_PER_SECOND;

            assertThat(secs).isGreaterThanOrEqualTo(expectedRunTime);
            throw e;
        }
    }

    //    @Test // this test can NOT run on Bamboo, only for local testing of you own ComPorts
    public void hardwareModemTest() throws IOException {
        List<String> initStrings = Collections.singletonList("ATM0");

        when(comPort.getRingCount()).thenReturn(3);
        when(comPort.getAtCommandTimeout()).thenReturn(new TimeDuration(5));
        when(comPort.getConnectTimeout()).thenReturn(new TimeDuration(30));
        when(comPort.getDelayAfterConnect()).thenReturn(new TimeDuration(4));
        when(comPort.getDelayBeforeSend()).thenReturn(new TimeDuration(0));
        when(comPort.getModemInitStrings()).thenReturn(initStrings);
        when(comPort.getAtCommandTry()).thenReturn(new BigDecimal(3));
        when(comPort.getAddressSelector()).thenReturn("");

        // Business methods
        SerialPortConnector portConnector = new SerialPortConnector(comPort, serialComponentService, this.hexService, eventPublisher, this.clock, this.deviceMessageService);
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
     *
     * @param comPort         The ModemBasedInboundComPort
     * @param responseTimings The response timings
     * @return The expected time
     */
    private int calculateExpectedAcceptTime(ModemBasedInboundComPort comPort, List<Integer> responseTimings) {
        long expectedTime = comPort.getAtCommandTimeout().getMilliSeconds();    // Delay before reinitialization of the modem
        expectedTime += (2 * comPort.getDelayBeforeSend().getMilliSeconds());   // Delay before the send of modem hang up & modem reinitialization
        expectedTime += (comPort.getGlobalModemInitStrings().size() * comPort.getDelayBeforeSend().getMilliSeconds()); // Delay before the send of all init strings
        expectedTime += (comPort.getModemInitStrings().size() * comPort.getDelayBeforeSend().getMilliSeconds()); // Delay before the send of all init strings
        for (Integer i : responseTimings) {                                     // Delay before the receive of each response
            expectedTime += (i * 1000);
        }
        expectedTime += comPort.getDelayBeforeSend().getMilliSeconds();         // Delay before send out of connect command
        expectedTime += comPort.getDelayAfterConnect().getMilliSeconds();       // Delay to wait after the actual connect
        return (int) (expectedTime / 1000);
    }

    protected class TestableSerialComChannel implements SerialPortComChannel {

        private int counter = 0;
        private int index = 0;

        /**
         * The list of responses
         */
        private List<String> responses = new ArrayList<>();
        private ServerSerialPort serverSerialPort;

        /**
         * The timing to respect - this is the timing BETWEEN responses.
         * Thus when handling response string responses[x], first there will be a delay of responseTimings seconds, simulating the real-world communication delays.
         */
        private List<Integer> responseTimings = new ArrayList<>();
        private long timingOfNextResponse;
        private boolean requestToFlushInputStream;

        public TestableSerialComChannel(ServerSerialPort serialPort) {
            this.serverSerialPort = serialPort;
        }

        public void setResponses(List<String> responses) {
            this.responses = responses;
        }

        public List<Integer> getResponseTimings() {
            return responseTimings;
        }

        public void setResponseTimings(List<Integer> responseTimings) {
            this.responseTimings = responseTimings;
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
        public boolean startWriting() {
            return true;
        }

        @Override
        public int write(int b) {
            return 0;
        }

        @Override
        public int write(byte[] bytes) {
            if ("+++".equals(new String(bytes))) {
                // Writing the disconnect sequence is always followed by a flush of the inputStream.
                requestToFlushInputStream = true;
            }
            // nothing else to do
            return bytes.length;
        }

        @Override
        public void close() {
            //Do nothing
        }

        @Override
        public void addProperties(TypedProperties typedProperties) {

        }

        @Override
        public TypedProperties getProperties() {
            return com.energyict.mdc.common.TypedProperties.empty();
        }

        @Override
        public void prepareForDisConnect() {
            //Do nothing
        }

        @Override
        public void setTimeout(long millis) {
            //Do nothing
        }

        @Override
        public boolean isVoid() {
            return false;
        }

        @Override
        public ComChannelType getComChannelType() {
            return ComChannelType.SerialComChannel;
        }

        @Override
        public boolean startReading() {
            return true;
        }

        @Override
        public int read() {
            if (responses.size() > counter) {
                return (int) responses.get(counter).charAt(index++);
            }
            return -1;
        }

        @Override
        public int read(byte[] buffer) {
            return 0;
        }

        @Override
        public int read(byte[] buffer, int offset, int length) {
            return 0;
        }

        @Override
        public void updatePortConfiguration(SerialPortConfiguration serialPortConfiguration) {
            //Do nothing
        }

        @Override
        public SerialPortConfiguration getSerialPortConfiguration() {
            return serverSerialPort.getSerialPortConfiguration();
        }

        @Override
        public ServerSerialPort getSerialPort() {
            return serverSerialPort;
        }

        @Override
        public void flush() throws IOException {
            //Do nothing
        }
    }
}
