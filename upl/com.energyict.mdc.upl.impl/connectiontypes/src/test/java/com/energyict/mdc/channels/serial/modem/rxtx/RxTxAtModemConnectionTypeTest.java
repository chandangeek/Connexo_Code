/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.channels.serial.modem.rxtx;

import com.energyict.mdc.channel.serial.SerialPortConfiguration;
import com.energyict.mdc.channel.serial.ServerSerialPort;
import com.energyict.mdc.channel.serial.direct.rxtx.RxTxSerialPort;
import com.energyict.mdc.channel.serial.modemproperties.TypedAtModemProperties;
import com.energyict.mdc.channels.serial.modem.AbstractModemTests;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.ComChannelType;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.protocol.exception.ModemException;
import com.energyict.protocol.exception.ProtocolExceptionReference;
import com.energyict.protocol.exceptions.ConnectionException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RxTxAtModemConnectionTypeTest extends AbstractModemTests {

    private AbstractModemTests.TestableSerialComChannel getTestableComChannel() {
        InputStream inputStream = mock(InputStream.class);
        OutputStream outputStream = mock(OutputStream.class);
        ServerSerialPort serialPort = mock(ServerSerialPort.class);
        when(serialPort.getInputStream()).thenReturn(inputStream);
        when(serialPort.getOutputStream()).thenReturn(outputStream);
        return new AbstractModemTests.TestableSerialComChannel(serialPort);
    }

    private AbstractModemTests.TimeoutSerialComChannel getTimeoutSerialComChannel(long sleepTime) {
        InputStream inputStream = mock(InputStream.class);
        OutputStream outputStream = mock(OutputStream.class);
        ServerSerialPort serialPort = mock(ServerSerialPort.class);
        when(serialPort.getInputStream()).thenReturn(inputStream);
        when(serialPort.getOutputStream()).thenReturn(outputStream);
        return new AbstractModemTests.TimeoutSerialComChannel(serialPort, sleepTime);
    }

    private TypedProperties getProperProperties() {
        com.energyict.protocolimpl.properties.TypedProperties result = com.energyict.protocolimpl.properties.TypedProperties.empty();

        result.setProperty(TypedAtModemProperties.DELAY_BEFORE_SEND, Duration.ofMillis(10));
        result.setProperty(TypedAtModemProperties.COMMAND_TIMEOUT, Duration.ofMillis(COMMAND_TIMEOUT_VALUE));
        result.setProperty(TypedAtModemProperties.COMMAND_TRIES, new BigDecimal(1));
        result.setProperty(TypedAtModemProperties.MODEM_GLOBAL_INIT_STRINGS, "ATS0=0E0V1");
        result.setProperty(TypedAtModemProperties.DELAY_AFTER_CONNECT, Duration.ofMillis(10));
        result.setProperty(TypedAtModemProperties.CONNECT_TIMEOUT, Duration.ofMillis(COMMAND_TIMEOUT_VALUE));
        result.setProperty(TypedAtModemProperties.MODEM_DIAL_PREFIX, "");
        result.setProperty(TypedAtModemProperties.MODEM_ADDRESS_SELECTOR, "");
        result.setProperty(TypedAtModemProperties.MODEM_POST_DIAL_COMMANDS, "");
        result.setProperty(TypedAtModemProperties.PHONE_NUMBER_PROPERTY_NAME, PHONE_NUMBER);

        return result;
    }

    private void getProperlyMockedComPort(AbstractModemTests.TestableSerialComChannel serialComChannel, RxTxSerialPort rxTxSerialPort) throws Exception {
        when(serialComponentService.newSerialPort(any(SerialPortConfiguration.class))).thenReturn(rxTxSerialPort);
        when(serialComponentService.newSerialComChannel(any(ServerSerialPort.class), any(ComChannelType.class))).thenReturn(serialComChannel);
    }

    @Test(timeout = TEST_TIMEOUT_MILLIS, expected = ConnectionException.class)
    public void readTimeOutExceptionTest() throws Exception {
        AbstractModemTests.TestableSerialComChannel serialComChannel = getTestableComChannel();
        RxTxSerialPort rxTxSerialPort = mock(RxTxSerialPort.class);
        getProperlyMockedComPort(serialComChannel, rxTxSerialPort);

        RxTxAtModemConnectionType atModemConnectionType = new RxTxAtModemConnectionType(propertySpecService);
        atModemConnectionType.setUPLProperties(getProperProperties());
        try {
            atModemConnectionType.connect();
        } catch (ConnectionException e) {
            if (!((ModemException) e.getCause()).getExceptionReference().equals(ProtocolExceptionReference.MODEM_COULD_NOT_HANG_UP)) {
                fail("Should have gotten exception indicating the hang up of the modem failed, but was " + e.getMessage());
            }
            throw e;
        }
    }

    @Test(timeout = TEST_TIMEOUT_MILLIS, expected = ConnectionException.class)
    public void testHangUpModemFails() throws Exception {
        AbstractModemTests.TestableSerialComChannel serialComChannel = getTestableComChannel();
        serialComChannel.setResponses(Arrays.asList("   ", "NotValidResponse"));
        RxTxSerialPort rxTxSerialPort = mock(RxTxSerialPort.class);
        getProperlyMockedComPort(serialComChannel, rxTxSerialPort);

        RxTxAtModemConnectionType atModemConnectionType = new RxTxAtModemConnectionType(propertySpecService);
        atModemConnectionType.setUPLProperties(getProperProperties());

        try {
            atModemConnectionType.connect();
        } catch (ConnectionException e) {
            if (!((ModemException) e.getCause()).getExceptionReference().equals(ProtocolExceptionReference.MODEM_COULD_NOT_HANG_UP)) {
                fail("Should have gotten exception indicating that the modem hangup failed, but was " + e.getMessage());
            }
            assertThat(((ModemException) e.getCause()).getMessageArguments()).contains(comPortName);
            throw e;
        }
    }

    @Test(timeout = TEST_TIMEOUT_MILLIS)
    public void testHangUpModemSucceeds() throws Exception {
        AbstractModemTests.TestableSerialComChannel comChannel = getTestableComChannel();
        comChannel.setResponses(OK_LIST);
        RxTxSerialPort rxTxSerialPort = mock(RxTxSerialPort.class);
        getProperlyMockedComPort(comChannel, rxTxSerialPort);

        RxTxAtModemConnectionType atModemConnectionType = spy(new RxTxAtModemConnectionType(propertySpecService));
        atModemConnectionType.setUPLProperties(getProperProperties());

        atModemConnectionType.connect();

        verify(atModemConnectionType.atModemComponent, times(1)).hangUpComChannel(comChannel);
    }

    @Test(timeout = TEST_TIMEOUT_MILLIS)
    public void testRestoreDefaultProfileSucceeds() throws Exception {
        AbstractModemTests.TestableSerialComChannel comChannel = getTestableComChannel();
        comChannel.setResponses(OK_LIST);
        RxTxSerialPort rxTxSerialPort = mock(RxTxSerialPort.class);
        getProperlyMockedComPort(comChannel, rxTxSerialPort);

        RxTxAtModemConnectionType atModemConnectionType = spy(new RxTxAtModemConnectionType(propertySpecService));
        atModemConnectionType.setUPLProperties(getProperProperties());

        atModemConnectionType.connect();

        verify(atModemConnectionType.atModemComponent, times(1)).reStoreProfile(comChannel);
    }

    @Test(timeout = TEST_TIMEOUT_MILLIS, expected = ConnectionException.class)
    public void testRestoreDefaultProfileFails() throws Exception {
        AbstractModemTests.TestableSerialComChannel comChannel = getTestableComChannel();
        comChannel.setResponses(Arrays.asList(RUBBISH_FOR_FLUSH, "OK", "NokToDefaultProfileRestore"));
        RxTxSerialPort rxTxSerialPort = mock(RxTxSerialPort.class);
        getProperlyMockedComPort(comChannel, rxTxSerialPort);

        RxTxAtModemConnectionType atModemConnectionType = spy(new RxTxAtModemConnectionType(propertySpecService));
        atModemConnectionType.setUPLProperties(getProperProperties());

        try {
            atModemConnectionType.connect();
        } catch (ConnectionException e) {
            if (!((ModemException) e.getCause()).getExceptionReference().equals(ProtocolExceptionReference.MODEM_COULD_NOT_RESTORE_DEFAULT_PROFILE)) {
                fail("Should have gotten exception indicating that the modem could not restore his default profile, but was " + e.getMessage());
            }
            assertThat(((ModemException) e.getCause()).getMessageArguments()).contains(comPortName, "ATZ", "NokToDefaultProfileRestore");
            throw e;
        }
    }

    @Test(timeout = TEST_LONG_TIMEOUT_MILLIS, expected = ConnectionException.class)
    public void testRetriesExceededForHangUp() throws Exception {
        AbstractModemTests.TimeoutSerialComChannel comChannel = getTimeoutSerialComChannel(COMMAND_TIMEOUT_VALUE + 10);
        comChannel.setResponses(Arrays.asList(RUBBISH_FOR_FLUSH, "First_Not_CorrectAnswer", "Second_Not_CorrectAnswer", "Third_Not_CorrectAnswer"));
        RxTxSerialPort rxTxSerialPort = mock(RxTxSerialPort.class);
        getProperlyMockedComPort(comChannel, rxTxSerialPort);

        com.energyict.protocolimpl.properties.TypedProperties typedProperties = com.energyict.protocolimpl.properties.TypedProperties.empty();
        typedProperties.setProperty(TypedAtModemProperties.DELAY_BEFORE_SEND, Duration.ofMillis(10));
        typedProperties.setProperty(TypedAtModemProperties.COMMAND_TIMEOUT, Duration.ofMillis(COMMAND_TIMEOUT_VALUE));
        typedProperties.setProperty(TypedAtModemProperties.COMMAND_TRIES, new BigDecimal(3));

        RxTxAtModemConnectionType atModemConnectionType = spy(new RxTxAtModemConnectionType(propertySpecService));
        atModemConnectionType.setUPLProperties(typedProperties);

        final int numberOfTries = 3;
        try {
            atModemConnectionType.connect();
        } catch (ConnectionException e) {
            if (!((ModemException) e.getCause()).getExceptionReference().equals(ProtocolExceptionReference.MODEM_COULD_NOT_HANG_UP)) {
                fail("Should have gotten exception indicating that the modem hangup failed, but was " + e.getMessage());
            }
            assertThat(((ModemException) e.getCause()).getMessageArguments()).contains(comPortName);
            verify(atModemConnectionType.atModemComponent, times(numberOfTries)).readAndVerify(any(ComChannel.class), any(String.class), any(Long.class));
            throw e;
        }
    }

    @Test(timeout = TEST_TIMEOUT_MILLIS)
    public void writeSuccessfulInitStringsTest() throws Exception {
        AbstractModemTests.TestableSerialComChannel comChannel = getTestableComChannel();
        comChannel.setResponses(OK_LIST);
        RxTxSerialPort rxTxSerialPort = mock(RxTxSerialPort.class);
        getProperlyMockedComPort(comChannel, rxTxSerialPort);

        RxTxAtModemConnectionType atModemConnectionType = spy(new RxTxAtModemConnectionType(propertySpecService));
        atModemConnectionType.setUPLProperties(getProperProperties());

        atModemConnectionType.connect();

        verify(atModemConnectionType.atModemComponent, times(1)).sendInitStrings(comChannel);
    }

    @Test(timeout = TEST_TIMEOUT_MILLIS, expected = ConnectionException.class)
    public void writeFailingInitStringTest() throws Exception {
        AbstractModemTests.TestableSerialComChannel comChannel = getTestableComChannel();
        comChannel.setResponses(Arrays.asList(RUBBISH_FOR_FLUSH, "OK", "OK", "Not_CorrectAnswer"));
        RxTxSerialPort rxTxSerialPort = mock(RxTxSerialPort.class);
        getProperlyMockedComPort(comChannel, rxTxSerialPort);


        RxTxAtModemConnectionType atModemConnectionType = spy(new RxTxAtModemConnectionType(propertySpecService));
        atModemConnectionType.setUPLProperties(getProperProperties());

        try {
            atModemConnectionType.connect();
        } catch (ConnectionException e) {
            if (!((ModemException) e.getCause()).getExceptionReference().equals(ProtocolExceptionReference.MODEM_COULD_NOT_SEND_INIT_STRING)) {
                fail("Should have gotten exception indicating that the modem init string could not be sent, but was " + e.getMessage());
            }
            assertThat(((ModemException) e.getCause()).getMessageArguments()).contains(comPortName, "Not_CorrectAnswer", "ATS0=0E0V1");
            throw e;
        }
    }

    @Test(timeout = TEST_TIMEOUT_MILLIS)
    public void writeMultipleInitStringsTest() throws Exception {
        AbstractModemTests.TestableSerialComChannel comChannel = getTestableComChannel();
        comChannel.setResponses(Arrays.asList(RUBBISH_FOR_FLUSH, "OK", "OK", "OK", "OK", "OK", "OK", "CONNECT 9600", "OK", "OK"));
        RxTxSerialPort rxTxSerialPort = mock(RxTxSerialPort.class);
        getProperlyMockedComPort(comChannel, rxTxSerialPort);

        com.energyict.protocolimpl.properties.TypedProperties typedProperties = com.energyict.protocolimpl.properties.TypedProperties.empty();
        typedProperties.setProperty(TypedAtModemProperties.DELAY_BEFORE_SEND, Duration.ofMillis(10));
        typedProperties.setProperty(TypedAtModemProperties.COMMAND_TIMEOUT, Duration.ofMillis(COMMAND_TIMEOUT_VALUE));
        typedProperties.setProperty(TypedAtModemProperties.COMMAND_TRIES, new BigDecimal(1));
        typedProperties.setProperty(TypedAtModemProperties.MODEM_INIT_STRINGS, "ATS0=0E0V1;ATM0;ATS1=0");
        typedProperties.setProperty(TypedAtModemProperties.DELAY_AFTER_CONNECT, Duration.ofMillis(10));
        typedProperties.setProperty(TypedAtModemProperties.CONNECT_TIMEOUT, Duration.ofMillis(COMMAND_TIMEOUT_VALUE));
        typedProperties.setProperty(TypedAtModemProperties.MODEM_DIAL_PREFIX, "");
        typedProperties.setProperty(TypedAtModemProperties.MODEM_ADDRESS_SELECTOR, "");
        typedProperties.setProperty(TypedAtModemProperties.MODEM_POST_DIAL_COMMANDS, "");
        typedProperties.setProperty(TypedAtModemProperties.PHONE_NUMBER_PROPERTY_NAME, PHONE_NUMBER);

        RxTxAtModemConnectionType atModemConnectionType = spy(new RxTxAtModemConnectionType(propertySpecService));

        atModemConnectionType.setUPLProperties(typedProperties);
        atModemConnectionType.connect();

        verify(atModemConnectionType.atModemComponent, times(1)).sendInitStrings(comChannel);
        verify(atModemConnectionType.atModemComponent, times(4)).writeSingleInitString(any(ComChannel.class), any(String.class)); // 1 global init string and 3 user defined init strings have been send out
    }

    @Test(timeout = TEST_TIMEOUT_MILLIS, expected = ConnectionException.class)
    public void busyErrorTest() throws Exception {
        AbstractModemTests.TestableSerialComChannel comChannel = getTestableComChannel();
        comChannel.setResponses(Arrays.asList(RUBBISH_FOR_FLUSH, "OK", "BUSY"));
        RxTxSerialPort rxTxSerialPort = mock(RxTxSerialPort.class);
        getProperlyMockedComPort(comChannel, rxTxSerialPort);

        RxTxAtModemConnectionType atModemConnectionType = spy(new RxTxAtModemConnectionType(propertySpecService));
        atModemConnectionType.setUPLProperties(getProperProperties());

        try {
            atModemConnectionType.connect();
        } catch (ConnectionException e) {
            if (!((ModemException) e.getCause()).getExceptionReference().equals(ProtocolExceptionReference.AT_MODEM_BUSY)) {
                fail("Should have gotten exception indicating that the modem received a BUSY signal, but was " + e.getMessage());
            }
            assertThat(((ModemException) e.getCause()).getMessageArguments()).contains(comPortName);
            verify(atModemConnectionType.atModemComponent, times(1)).hangUpComChannel(comChannel);
            verify(atModemConnectionType.atModemComponent, times(1)).reStoreProfile(comChannel);
            verify(atModemConnectionType.atModemComponent, never()).sendInitStrings(comChannel);
            throw e;
        }
    }

    @Test(timeout = TEST_TIMEOUT_MILLIS, expected = ConnectionException.class)
    public void errorAnswerTest() throws Exception {
        AbstractModemTests.TestableSerialComChannel comChannel = getTestableComChannel();
        comChannel.setResponses(Arrays.asList(RUBBISH_FOR_FLUSH, "OK", "ERROR"));
        RxTxSerialPort rxTxSerialPort = mock(RxTxSerialPort.class);
        getProperlyMockedComPort(comChannel, rxTxSerialPort);

        RxTxAtModemConnectionType atModemConnectionType = spy(new RxTxAtModemConnectionType(propertySpecService));
        atModemConnectionType.setUPLProperties(getProperProperties());

        try {
            atModemConnectionType.connect();
        } catch (ConnectionException e) {
            if (!((ModemException) e.getCause()).getExceptionReference().equals(ProtocolExceptionReference.AT_MODEM_ERROR)) {
                fail("Should have gotten exception indicating that the modem received a ERROR signal, but was " + e.getMessage());
            }
            assertThat(((ModemException) e.getCause()).getMessageArguments()).contains(comPortName);
            verify(atModemConnectionType.atModemComponent, times(1)).hangUpComChannel(comChannel);
            verify(atModemConnectionType.atModemComponent, times(1)).reStoreProfile(comChannel);
            verify(atModemConnectionType.atModemComponent, never()).sendInitStrings(comChannel);
            throw e;
        }
    }

    @Test(timeout = TEST_TIMEOUT_MILLIS, expected = ConnectionException.class)
    public void noAnswerErrorTest() throws Exception {
        AbstractModemTests.TestableSerialComChannel comChannel = getTestableComChannel();
        comChannel.setResponses(Arrays.asList(RUBBISH_FOR_FLUSH, "OK", "NO ANSWER"));
        RxTxSerialPort rxTxSerialPort = mock(RxTxSerialPort.class);
        getProperlyMockedComPort(comChannel, rxTxSerialPort);

        RxTxAtModemConnectionType atModemConnectionType = spy(new RxTxAtModemConnectionType(propertySpecService));
        atModemConnectionType.setUPLProperties(getProperProperties());

        try {
            atModemConnectionType.connect();
        } catch (ConnectionException e) {
            if (!((ModemException) e.getCause()).getExceptionReference().equals(ProtocolExceptionReference.AT_MODEM_NO_ANSWER)) {
                fail("Should have gotten exception indicating that the modem received a NO ANSWER signal, but was " + e.getMessage());
            }
            assertThat(((ModemException) e.getCause()).getMessageArguments()).contains(comPortName);
            verify(atModemConnectionType.atModemComponent, times(1)).hangUpComChannel(comChannel);
            verify(atModemConnectionType.atModemComponent, times(1)).reStoreProfile(comChannel);
            verify(atModemConnectionType.atModemComponent, never()).sendInitStrings(comChannel);
            throw e;
        }
    }


    @Test(timeout = TEST_TIMEOUT_MILLIS, expected = ConnectionException.class)
    public void noCarrierErrorTest() throws Exception {
        AbstractModemTests.TestableSerialComChannel comChannel = getTestableComChannel();
        comChannel.setResponses(Arrays.asList(RUBBISH_FOR_FLUSH, "OK", "NO CARRIER"));
        RxTxSerialPort rxTxSerialPort = mock(RxTxSerialPort.class);
        getProperlyMockedComPort(comChannel, rxTxSerialPort);

        RxTxAtModemConnectionType atModemConnectionType = spy(new RxTxAtModemConnectionType(propertySpecService));
        atModemConnectionType.setUPLProperties(getProperProperties());

        try {
            atModemConnectionType.connect();
        } catch (ConnectionException e) {
            if (!((ModemException) e.getCause()).getExceptionReference().equals(ProtocolExceptionReference.AT_MODEM_NO_CARRIER)) {
                fail("Should have gotten exception indicating that the modem received a NO CARRIER signal, but was " + e.getMessage());
            }
            assertThat(((ModemException) e.getCause()).getMessageArguments()).contains(comPortName);
            verify(atModemConnectionType.atModemComponent, times(1)).hangUpComChannel(comChannel);
            verify(atModemConnectionType.atModemComponent, times(1)).reStoreProfile(comChannel);
            verify(atModemConnectionType.atModemComponent, never()).sendInitStrings(comChannel);
            throw e;
        }
    }

    @Test(timeout = TEST_TIMEOUT_MILLIS, expected = ConnectionException.class)
    public void noDialtoneErrorTest() throws Exception {
        AbstractModemTests.TestableSerialComChannel comChannel = getTestableComChannel();
        comChannel.setResponses(Arrays.asList(RUBBISH_FOR_FLUSH, "OK", "NO DIALTONE"));
        RxTxSerialPort rxTxSerialPort = mock(RxTxSerialPort.class);
        getProperlyMockedComPort(comChannel, rxTxSerialPort);

        RxTxAtModemConnectionType atModemConnectionType = spy(new RxTxAtModemConnectionType(propertySpecService));
        atModemConnectionType.setUPLProperties(getProperProperties());

        try {
            atModemConnectionType.connect();
        } catch (ConnectionException e) {
            if (!((ModemException) e.getCause()).getExceptionReference().equals(ProtocolExceptionReference.AT_MODEM_NO_DIALTONE)) {
                fail("Should have gotten exception indicating that the modem received a NO DIALTONE signal, but was " + e.getMessage());
            }
            assertThat(((ModemException) e.getCause()).getMessageArguments()).contains(comPortName);
            verify(atModemConnectionType.atModemComponent, times(1)).hangUpComChannel(comChannel);
            verify(atModemConnectionType.atModemComponent, times(1)).reStoreProfile(comChannel);
            verify(atModemConnectionType.atModemComponent, never()).sendInitStrings(comChannel);
            throw e;
        }
    }

    @Test(timeout = TEST_TIMEOUT_MILLIS)
    public void verifyConnectSuccess() throws Exception {
        AbstractModemTests.TestableSerialComChannel comChannel = getTestableComChannel();
        comChannel.setResponses(OK_LIST);
        RxTxSerialPort rxTxSerialPort = mock(RxTxSerialPort.class);
        getProperlyMockedComPort(comChannel, rxTxSerialPort);

        RxTxAtModemConnectionType atModemConnectionType = spy(new RxTxAtModemConnectionType(propertySpecService));
        atModemConnectionType.setUPLProperties(getProperProperties());

        atModemConnectionType.connect();

        verify(atModemConnectionType.atModemComponent, times(1)).dialModem(comChannel);
    }

    @Test(timeout = TEST_TIMEOUT_MILLIS, expected = ConnectionException.class)
    public void verifyConnectBusy() throws Exception {
        AbstractModemTests.TestableSerialComChannel comChannel = getTestableComChannel();
        comChannel.setResponses(Arrays.asList(RUBBISH_FOR_FLUSH, "OK", "OK", "OK", "BUSY", "OK", "OK"));
        RxTxSerialPort rxTxSerialPort = mock(RxTxSerialPort.class);
        getProperlyMockedComPort(comChannel, rxTxSerialPort);

        RxTxAtModemConnectionType atModemConnectionType = spy(new RxTxAtModemConnectionType(propertySpecService));
        atModemConnectionType.setUPLProperties(getProperProperties());

        try {
            atModemConnectionType.connect();
        } catch (ConnectionException e) {
            if (!((ModemException) e.getCause()).getExceptionReference().equals(ProtocolExceptionReference.AT_MODEM_BUSY)) {
                fail("Should have gotten exception indicating that the connect failed with a busy command, but was " + e.getMessage());
            }
            assertThat(((ModemException) e.getCause()).getMessageArguments()).contains(comPortName);
            verify(atModemConnectionType.atModemComponent, times(1)).dialModem(comChannel);
            throw e;
        }
    }

    @Test(timeout = TEST_TIMEOUT_MILLIS)
    public void testWithNoSelector() throws Exception {
        AbstractModemTests.TestableSerialComChannel comChannel = getTestableComChannel();
        comChannel.setResponses(OK_LIST);
        RxTxSerialPort rxTxSerialPort = mock(RxTxSerialPort.class);
        getProperlyMockedComPort(comChannel, rxTxSerialPort);

        RxTxAtModemConnectionType atModemConnectionType = spy(new RxTxAtModemConnectionType(propertySpecService));
        atModemConnectionType.setUPLProperties(getProperProperties());

        atModemConnectionType.connect();

        verify(atModemConnectionType.atModemComponent, times(1)).dialModem(comChannel);
        verify(atModemConnectionType.atModemComponent, never()).sendAddressSelector(comChannel);
    }

    @Test(timeout = TEST_TIMEOUT_MILLIS)
    public void testWithAddressSelector() throws Exception {
        AbstractModemTests.TestableSerialComChannel comChannel = getTestableComChannel();
        comChannel.setResponses(OK_LIST);
        RxTxSerialPort rxTxSerialPort = mock(RxTxSerialPort.class);
        getProperlyMockedComPort(comChannel, rxTxSerialPort);

        TypedProperties typedProperties = getProperProperties();
        typedProperties.setProperty(TypedAtModemProperties.MODEM_ADDRESS_SELECTOR, "AddressSelect_01");
        RxTxAtModemConnectionType atModemConnectionType = spy(new RxTxAtModemConnectionType(propertySpecService));
        atModemConnectionType.setUPLProperties(typedProperties);

        atModemConnectionType.connect();

        verify(atModemConnectionType.atModemComponent, times(1)).dialModem(comChannel);
        verify(atModemConnectionType.atModemComponent, times(1)).sendAddressSelector(comChannel);
    }

}