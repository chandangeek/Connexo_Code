/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.channels.serial.modem.serialio;

import com.energyict.mdc.channel.serial.SerialPortConfiguration;
import com.energyict.mdc.channel.serial.ServerSerialPort;
import com.energyict.mdc.channel.serial.SignalController;
import com.energyict.mdc.channel.serial.direct.serialio.SioSerialPort;
import com.energyict.mdc.channels.serial.modem.AbstractModemTests;
import com.energyict.mdc.channels.serial.modem.TypedPaknetModemProperties;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.ComChannelType;
import com.energyict.mdc.upl.io.ModemException;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.protocol.exceptions.ConnectionException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for the {@link com.energyict.mdc.channels.serial.modem.serialio.SioPaknetModemConnectionType} component
 *
 * @author sva
 * @since 15/04/13 - 11:33
 */
@RunWith(MockitoJUnitRunner.class)
public class SioPaknetModemConnectionTypeTest extends AbstractModemTests {

    private static final int DTR_TOGGLE_DELAY_VALUE = 100;

    protected final List<String> OK_LIST = Arrays.asList(RUBBISH_FOR_FLUSH, "\r\n*\r\n", "\r\n*\r\n", "\r\nXX COM\r\nYY\r\n");

    private TestableSerialComChannel getTestableComChannel() {
        InputStream inputStream = mock(InputStream.class);
        OutputStream outputStream = mock(OutputStream.class);
        ServerSerialPort serialPort = mock(ServerSerialPort.class);
        SignalController signalController = mock(SignalController.class);
        when(serialPort.getInputStream()).thenReturn(inputStream);
        when(serialPort.getOutputStream()).thenReturn(outputStream);
        when(serialPort.getSerialPortSignalController()).thenReturn(signalController);
        return new TestableSerialComChannel(serialPort);
    }

    private TimeoutSerialComChannel getTimeoutSerialComChannel(long sleepTime) {
        InputStream inputStream = mock(InputStream.class);
        OutputStream outputStream = mock(OutputStream.class);
        ServerSerialPort serialPort = mock(ServerSerialPort.class);
        SignalController signalController = mock(SignalController.class);
        when(serialPort.getInputStream()).thenReturn(inputStream);
        when(serialPort.getOutputStream()).thenReturn(outputStream);
        when(serialPort.getSerialPortSignalController()).thenReturn(signalController);
        return new TimeoutSerialComChannel(serialPort, sleepTime);
    }

    private TypedProperties getProperProperties() {
        com.energyict.protocolimpl.properties.TypedProperties result = com.energyict.protocolimpl.properties.TypedProperties.empty();

        result.setProperty(TypedPaknetModemProperties.DELAY_BEFORE_SEND, Duration.ofSeconds(10));
        result.setProperty(TypedPaknetModemProperties.COMMAND_TIMEOUT, Duration.ofMillis(COMMAND_TIMEOUT_VALUE));
        result.setProperty(TypedPaknetModemProperties.COMMAND_TRIES, new BigDecimal(1));
        result.setProperty(TypedPaknetModemProperties.MODEM_INIT_STRINGS, "1:0,2:0,3:0,4:10,5:0,6:5");
        result.setProperty(TypedPaknetModemProperties.DELAY_AFTER_CONNECT, Duration.ofMillis(10));
        result.setProperty(TypedPaknetModemProperties.CONNECT_TIMEOUT, Duration.ofMillis(COMMAND_TIMEOUT_VALUE));
        result.setProperty(TypedPaknetModemProperties.MODEM_DIAL_PREFIX, "");
        result.setProperty(TypedPaknetModemProperties.DTR_TOGGLE_DELAY, Duration.ofMillis(DTR_TOGGLE_DELAY_VALUE));
        result.setProperty(TypedPaknetModemProperties.PHONE_NUMBER_PROPERTY_NAME, PHONE_NUMBER);

        return result;
    }

    private void getProperlyMockedComPort(TestableSerialComChannel serialComChannel, SioSerialPort sioSerialPort) throws Exception {
        when(serialComponentService.newSerialPort(any(SerialPortConfiguration.class))).thenReturn(sioSerialPort);
        when(serialComponentService.newSerialComChannel(any(ServerSerialPort.class), any(ComChannelType.class))).thenReturn(serialComChannel);
    }

    @Test(timeout = TEST_TIMEOUT_MILLIS)
    public void testDisconnectModemBeforeNewSessionSucceeds() throws Exception {
        TestableSerialComChannel comChannel = getTestableComChannel();
        comChannel.setResponses(OK_LIST);
        SioSerialPort sioSerialPort = mock(SioSerialPort.class);
        getProperlyMockedComPort(comChannel, sioSerialPort);

        SioPaknetModemConnectionType modemConnectionType = spy(new SioPaknetModemConnectionType(propertySpecService));
        modemConnectionType.setUPLProperties(getProperProperties());
        modemConnectionType.connect();

        verify(modemConnectionType.paknetModemComponent, times(1)).disconnectModemBeforeNewSession(comChannel);
    }

    @Test(timeout = TEST_TIMEOUT_MILLIS, expected = ConnectionException.class)
    public void testInitializeCommandStateFails() throws Exception {
        TestableSerialComChannel serialComChannel = getTestableComChannel();
        serialComChannel.setResponses(Arrays.asList("   ", "NotValidResponse"));
        SioSerialPort sioSerialPort = mock(SioSerialPort.class);
        getProperlyMockedComPort(serialComChannel, sioSerialPort);

        SioPaknetModemConnectionType modemConnectionType = new SioPaknetModemConnectionType(propertySpecService);
        modemConnectionType.setUPLProperties(getProperProperties());

        try {
            modemConnectionType.connect();
        } catch (ConnectionException e) {
            if (!((ModemException) e.getCause()).getType().equals(ModemException.Type.MODEM_COULD_NOT_INITIALIZE_COMMAND_STATE)) {
                fail("Should have gotten exception indicating that the modem could not initialize the command prompt, but was " + e.getMessage());
            }
            assertThat(((ModemException) e.getCause()).getMessageArguments()).contains(comPortName);
            throw e;
        }
    }

    @Test(timeout = TEST_TIMEOUT_MILLIS)
    public void testInitializeCommandStateSucceeds() throws Exception {
        TestableSerialComChannel comChannel = getTestableComChannel();
        comChannel.setResponses(OK_LIST);
        SioSerialPort sioSerialPort = mock(SioSerialPort.class);
        getProperlyMockedComPort(comChannel, sioSerialPort);

        SioPaknetModemConnectionType modemConnectionType = spy(new SioPaknetModemConnectionType(propertySpecService));
        modemConnectionType.setUPLProperties(getProperProperties());
        modemConnectionType.connect();

        verify(modemConnectionType.paknetModemComponent, times(1)).initializeAfterConnect(comChannel);
    }


    @Test(timeout = TEST_TIMEOUT_MILLIS, expected = ConnectionException.class)
    public void testSetModemParametersFails() throws Exception {
        TestableSerialComChannel serialComChannel = getTestableComChannel();
        serialComChannel.setResponses(Arrays.asList("   ", "\r\n*\r\n", "NotValidResponse"));
        SioSerialPort sioSerialPort = mock(SioSerialPort.class);
        getProperlyMockedComPort(serialComChannel, sioSerialPort);

        SioPaknetModemConnectionType modemConnectionType = new SioPaknetModemConnectionType(propertySpecService);

        try {
            modemConnectionType.setUPLProperties(getProperProperties());
            modemConnectionType.connect();
        } catch (ConnectionException e) {
            if (!((ModemException) e.getCause()).getType().equals(ModemException.Type.MODEM_COULD_NOT_SEND_INIT_STRING)) {
                fail("Should have gotten exception indicating that the modem could not send the initialization parameters, but was " + e.getMessage());
            }
            assertThat(((ModemException) e.getCause()).getMessageArguments()).contains(comPortName);
            throw e;
        }
    }

    @Test(timeout = TEST_TIMEOUT_MILLIS)
    public void testSetModemParametersSucceeds() throws Exception {
        TestableSerialComChannel comChannel = getTestableComChannel();
        comChannel.setResponses(OK_LIST);
        SioSerialPort sioSerialPort = mock(SioSerialPort.class);
        getProperlyMockedComPort(comChannel, sioSerialPort);

        SioPaknetModemConnectionType modemConnectionType = spy(new SioPaknetModemConnectionType(propertySpecService));
        modemConnectionType.setUPLProperties(getProperProperties());
        modemConnectionType.connect();

        verify(modemConnectionType.paknetModemComponent, times(1)).sendParameters(comChannel);
    }

    @Test(timeout = 10000, expected = ConnectionException.class)
    public void testRetriesExceededForInitializeParameters() throws Exception {
        TimeoutSerialComChannel comChannel = getTimeoutSerialComChannel(COMMAND_TIMEOUT_VALUE + 10);
        comChannel.setResponses(Arrays.asList(RUBBISH_FOR_FLUSH, "First_Not_CorrectAnswer", "Second_Not_CorrectAnswer", "Third_Not_CorrectAnswer"));
        SioSerialPort sioSerialPort = mock(SioSerialPort.class);
        getProperlyMockedComPort(comChannel, sioSerialPort);

        com.energyict.protocolimpl.properties.TypedProperties typedProperties = com.energyict.protocolimpl.properties.TypedProperties.empty();
        typedProperties.setProperty(TypedPaknetModemProperties.DELAY_BEFORE_SEND, Duration.ofMillis(10));
        typedProperties.setProperty(TypedPaknetModemProperties.COMMAND_TIMEOUT, Duration.ofMillis(COMMAND_TIMEOUT_VALUE));
        typedProperties.setProperty(TypedPaknetModemProperties.COMMAND_TRIES, new BigDecimal(3));
        typedProperties.setProperty(TypedPaknetModemProperties.DTR_TOGGLE_DELAY, Duration.ofMillis(DTR_TOGGLE_DELAY_VALUE));


        SioPaknetModemConnectionType modemConnectionType = spy(new SioPaknetModemConnectionType(propertySpecService));
        modemConnectionType.setUPLProperties(typedProperties);

        final int numberOfTries = 3;
        try {
            modemConnectionType.connect();
        } catch (ConnectionException e) {
            if (!((ModemException) e.getCause()).getType().equals(ModemException.Type.MODEM_COULD_NOT_INITIALIZE_COMMAND_STATE)) {
                fail("Should have gotten exception indicating that the modem could not initialize the command prompt, but was " + e.getMessage());
            }
            assertThat(((ModemException) e.getCause()).getMessageArguments()).contains(comPortName);
            verify(modemConnectionType.paknetModemComponent, times(numberOfTries)).readAndVerify(any(ComChannel.class), any(String.class), any(Long.class));
            throw e;
        }
    }

    @Test(timeout = TEST_TIMEOUT_MILLIS, expected = ConnectionException.class)
    public void noDialtoneErrorTest() throws Exception {
        TestableSerialComChannel comChannel = getTestableComChannel();
        comChannel.setResponses(Arrays.asList(RUBBISH_FOR_FLUSH, "\r\n*\r\n", "\r\n*\r\n", "NO CONNECTION PROMPT"));
        SioSerialPort sioSerialPort = mock(SioSerialPort.class);
        getProperlyMockedComPort(comChannel, sioSerialPort);

        SioPaknetModemConnectionType modemConnectionType = spy(new SioPaknetModemConnectionType(propertySpecService));

        try {
            modemConnectionType.setUPLProperties(getProperProperties());
            modemConnectionType.connect();
        } catch (ModemException e) {
            if (!((ModemException) e.getCause()).getType().equals(ModemException.Type.MODEM_CONNECT_TIMEOUT)) {
                fail("Should have gotten exception indicating that a timeout occurred during the dial, but was " + e.getMessage());
            }
            assertThat(((ModemException) e.getCause()).getMessageArguments()).contains(comPortName);
            verify(modemConnectionType.paknetModemComponent, times(1)).disconnectModemBeforeNewSession(comChannel);
            verify(modemConnectionType.paknetModemComponent, times(1)).initializeCommandState(comChannel);
            verify(modemConnectionType.paknetModemComponent, times(1)).sendParameters(comChannel);
            verify(modemConnectionType.paknetModemComponent, times(1)).dialModem(comChannel);
            verify(modemConnectionType.paknetModemComponent, never()).initializeAfterConnect(comChannel);
            throw e;
        }
    }

    @Test(timeout = TEST_TIMEOUT_MILLIS)
    public void verifyConnectSuccess() throws Exception {
        TestableSerialComChannel comChannel = getTestableComChannel();
        comChannel.setResponses(OK_LIST);
        SioSerialPort sioSerialPort = mock(SioSerialPort.class);
        getProperlyMockedComPort(comChannel, sioSerialPort);

        SioPaknetModemConnectionType modemConnectionType = spy(new SioPaknetModemConnectionType(propertySpecService));
        modemConnectionType.setUPLProperties(getProperProperties());
        modemConnectionType.connect();

        verify(modemConnectionType.paknetModemComponent, times(1)).dialModem(comChannel);
        verify(modemConnectionType.paknetModemComponent, times(1)).initializeAfterConnect(comChannel);
    }
}
