package com.energyict.mdc.engine.impl.commands.store.legacy;

import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.mdc.channel.serial.BaudrateValue;
import com.energyict.mdc.channel.serial.FlowControl;
import com.energyict.mdc.channel.serial.NrOfDataBits;
import com.energyict.mdc.channel.serial.NrOfStopBits;
import com.energyict.mdc.channel.serial.Parities;
import com.energyict.mdc.channel.serial.SerialPortConfiguration;
import com.energyict.mdc.channel.serial.ServerSerialPort;
import com.energyict.mdc.protocol.SerialPortComChannel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for the {@link SerialCommunicationChannelAdapter} component
 *
 * Copyrights EnergyICT
 * Date: 24/08/12
 * Time: 11:11
 */
@RunWith(MockitoJUnitRunner.class)
public class SerialCommunicationChannelAdapterTest {

    private final String comPortName = "Test";
    private final BaudrateValue standardBaudrate = BaudrateValue.BAUDRATE_1200;
    private final NrOfDataBits standardNrOfDataBits = NrOfDataBits.SEVEN;
    private final NrOfStopBits standardNrOfStopBits = NrOfStopBits.ONE;
    private final Parities standardParity = Parities.EVEN;
    private final FlowControl standardFlowControl = FlowControl.NONE;

    @Mock
    private ServerSerialPort serverSerialPort;
    @Mock
    private SerialPortComChannel serialPortComChannel;
    @Mock
    private InputStream inputStream;

    private SerialCommunicationChannelAdapter createSerialCommunicationChannelAdapter() {
        when(serverSerialPort.getSerialPortConfiguration()).thenReturn(
                new SerialPortConfiguration(
                        comPortName, standardBaudrate, standardNrOfDataBits, standardNrOfStopBits, standardParity, standardFlowControl));
        when(serialPortComChannel.getSerialPort()).thenReturn(serverSerialPort);
        when(serverSerialPort.getInputStream()).thenReturn(inputStream);
        return new SerialCommunicationChannelAdapter(serialPortComChannel);
    }

    @Test
    public void parityNewFormatTest(){
        SerialCommunicationChannelAdapter serialCommunicationChannelAdapter = createSerialCommunicationChannelAdapter();

        // asserts
        assertEquals(Parities.EVEN.getParity(), serialCommunicationChannelAdapter.parityToNewFormat(SerialCommunicationChannel.PARITY_EVEN));
        assertEquals(Parities.ODD.getParity(), serialCommunicationChannelAdapter.parityToNewFormat(SerialCommunicationChannel.PARITY_ODD));
        assertEquals(Parities.SPACE.getParity(), serialCommunicationChannelAdapter.parityToNewFormat(SerialCommunicationChannel.PARITY_SPACE));
        assertEquals(Parities.MARK.getParity(), serialCommunicationChannelAdapter.parityToNewFormat(SerialCommunicationChannel.PARITY_MARK));
        assertEquals(Parities.NONE.getParity(), serialCommunicationChannelAdapter.parityToNewFormat(SerialCommunicationChannel.PARITY_NONE));

        assertEquals(Parities.NONE.getParity(), serialCommunicationChannelAdapter.parityToNewFormat(9999)); // any unknown format should return none
    }

    @Test
    public void stopBitsToNewFormat(){
        SerialCommunicationChannelAdapter serialCommunicationChannelAdapter = createSerialCommunicationChannelAdapter();

        // asserts
        assertEquals(NrOfStopBits.ONE.getNrOfStopBits(), serialCommunicationChannelAdapter.stopBitsToNewFormat(SerialCommunicationChannel.STOPBITS_1));
        assertEquals(NrOfStopBits.TWO.getNrOfStopBits(), serialCommunicationChannelAdapter.stopBitsToNewFormat(SerialCommunicationChannel.STOPBITS_2));
        assertEquals(NrOfStopBits.ONE_AND_HALF.getNrOfStopBits(), serialCommunicationChannelAdapter.stopBitsToNewFormat(SerialCommunicationChannel.STOPBITS_1_5));

        assertEquals(NrOfStopBits.ONE.getNrOfStopBits(), serialCommunicationChannelAdapter.stopBitsToNewFormat(654654));  // any unknown format should return one stopbit
    }

    @Test
    public void setParamsTest() throws IOException {
        final int baudrate = 9600;
        final int dataBits = 6;
        final int stopBits = SerialCommunicationChannel.STOPBITS_2;
        final int parity = SerialCommunicationChannel.PARITY_MARK;

        SerialCommunicationChannelAdapter serialCommunicationChannelAdapter = createSerialCommunicationChannelAdapter();

        serialCommunicationChannelAdapter.setParams(baudrate, dataBits, parity, stopBits);

        verify(serverSerialPort).updatePortConfiguration(Matchers.argThat(
                new SerialPortConfigurationMatcher(BaudrateValue.BAUDRATE_9600, NrOfDataBits.SIX, NrOfStopBits.TWO, Parities.MARK)));
    }

    @Test
    public void setParamsAndFlushTest() throws IOException {
        final int baudrate = 9600;
        final int dataBits = 6;
        final int stopBits = SerialCommunicationChannel.STOPBITS_2;
        final int parity = SerialCommunicationChannel.PARITY_MARK;

        SerialCommunicationChannelAdapter serialCommunicationChannelAdapter = spy(createSerialCommunicationChannelAdapter());

        serialCommunicationChannelAdapter.setParamsAndFlush(baudrate, dataBits, parity, stopBits);

        verify(serverSerialPort).updatePortConfiguration(any(SerialPortConfiguration.class));
        verify(serialCommunicationChannelAdapter).flushInputStream();
    }

    @Test
    public void setParityTest() throws IOException {
        final int dataBits = 6;
        final int stopBits = SerialCommunicationChannel.STOPBITS_2;
        final int parity = SerialCommunicationChannel.PARITY_MARK;

        SerialCommunicationChannelAdapter serialCommunicationChannelAdapter = createSerialCommunicationChannelAdapter();

        serialCommunicationChannelAdapter.setParity(dataBits, parity, stopBits);

        verify(serverSerialPort).updatePortConfiguration(Matchers.argThat(
                new SerialPortConfigurationMatcher(standardBaudrate, NrOfDataBits.SIX, NrOfStopBits.TWO, Parities.MARK)));

    }

    @Test
    public void setParityAndFlushTest() throws IOException {
        final int dataBits = 6;
        final int stopBits = SerialCommunicationChannel.STOPBITS_2;
        final int parity = SerialCommunicationChannel.PARITY_MARK;

        SerialCommunicationChannelAdapter serialCommunicationChannelAdapter = spy(createSerialCommunicationChannelAdapter());

        serialCommunicationChannelAdapter.setParityAndFlush(dataBits, parity, stopBits);

        verify(serverSerialPort).updatePortConfiguration(any(SerialPortConfiguration.class));
        verify(serialCommunicationChannelAdapter).flushInputStream();
    }

    @Test
    public void setBaudRateTest() throws IOException {
        final int baudrate = 9600;

        SerialCommunicationChannelAdapter serialCommunicationChannelAdapter = createSerialCommunicationChannelAdapter();

        serialCommunicationChannelAdapter.setBaudrate(baudrate);

        verify(serverSerialPort).updatePortConfiguration(Matchers.argThat(
                new SerialPortConfigurationMatcher(BaudrateValue.BAUDRATE_9600, standardNrOfDataBits, standardNrOfStopBits, standardParity)));

    }

    @Test
    public void setBaudRateAndFlushTest() throws IOException {
        final int baudrate = 9600;

        SerialCommunicationChannelAdapter serialCommunicationChannelAdapter = spy(createSerialCommunicationChannelAdapter());

        serialCommunicationChannelAdapter.setBaudrateAndFlush(baudrate);

        verify(serverSerialPort).updatePortConfiguration(any(SerialPortConfiguration.class));
        verify(serialCommunicationChannelAdapter).flushInputStream();
    }

    private class SerialPortConfigurationMatcher extends ArgumentMatcher<SerialPortConfiguration> {

        private final BaudrateValue baudrate;
        private final NrOfDataBits dataBits;
        private final NrOfStopBits stopBits;
        private final Parities parity;

        public SerialPortConfigurationMatcher(BaudrateValue baudrate, NrOfDataBits dataBits, NrOfStopBits stopBits, Parities parity) {
            this.baudrate = baudrate;
            this.dataBits = dataBits;
            this.stopBits = stopBits;
            this.parity = parity;
        }

        @Override
        public boolean matches(Object argument) {
            if(argument instanceof SerialPortConfiguration){
                SerialPortConfiguration configuration = (SerialPortConfiguration) argument;
                return configuration.getBaudrate().equals(baudrate)
                        && configuration.getNrOfDataBits().equals(dataBits)
                        && configuration.getNrOfStopBits().equals(stopBits)
                        && configuration.getParity().equals(parity);
            }
            return false;
        }
    }
}
