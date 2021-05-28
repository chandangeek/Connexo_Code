/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.channels.serial.modem.rxtx;

import com.energyict.mdc.channel.serial.SerialPortConfiguration;
import com.energyict.mdc.channel.serial.ServerSerialPort;
import com.energyict.mdc.channels.serial.modem.AbstractModemTests;
import com.energyict.mdc.channels.serial.modem.AtModemComponent;
import com.energyict.mdc.channels.serial.modem.TypedAtModemProperties;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.upl.io.ConnectionType;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.protocol.exception.ModemException;
import com.energyict.protocol.exceptions.ConnectionException;

import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
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
        com.energyict.mdc.upl.TypedProperties result = com.energyict.mdc.upl.TypedProperties.empty();

        result.setProperty(TypedAtModemProperties.DELAY_BEFORE_SEND, Duration.ofMillis(10));
        result.setProperty(TypedAtModemProperties.COMMAND_TIMEOUT, Duration.ofMillis(COMMAND_TIMEOUT_VALUE));
        result.setProperty(TypedAtModemProperties.COMMAND_TRIES, new BigDecimal(1));
        result.setProperty(TypedAtModemProperties.MODEM_GLOBAL_INIT_STRINGS, "ATS0=0E0V1");
        result.setProperty(TypedAtModemProperties.DELAY_AFTER_CONNECT, Duration.ofMillis(10));
        result.setProperty(TypedAtModemProperties.CONNECT_TIMEOUT, Duration.ofMillis(COMMAND_TIMEOUT_VALUE));
        result.setProperty(TypedAtModemProperties.MODEM_DIAL_PREFIX, "");
        result.setProperty(TypedAtModemProperties.MODEM_ADDRESS_SELECTOR, "");
        result.setProperty(TypedAtModemProperties.MODEM_ADDRESS_SELECTOR, "");
        result.setProperty(TypedAtModemProperties.MODEM_POST_DIAL_COMMANDS, "");
        result.setProperty(ConnectionType.Property.COMP_PORT_NAME.getName(), comPortName);

        return result;
    }

    @Test(timeout = TEST_TIMEOUT_MILLIS, expected = ConnectionException.class)
    public void readTimeOutExceptionTest() throws Exception {
        AbstractModemTests.TestableSerialComChannel serialComChannel = getTestableComChannel();

        RxTxAtModemConnectionType atModemConnectionType = createConnectionType(serialComChannel);

        try {
            atModemConnectionType.connect();
        } catch (ConnectionException e) {
            if (!((ModemException) e.getCause()).getType().equals(ModemException.Type.MODEM_COULD_NOT_HANG_UP)) {
                fail("Should have gotten exception indicating the hang up of the modem failed, but was " + e.getMessage());
            }
            throw e;
        }
    }

    @Test(timeout = TEST_TIMEOUT_MILLIS, expected = ConnectionException.class)
    public void testHangUpModemFails() throws Exception {
        AbstractModemTests.TestableSerialComChannel serialComChannel = getTestableComChannel();
        serialComChannel.setResponses(Arrays.asList("   ", "NotValidResponse"));

        RxTxAtModemConnectionType atModemConnectionType = createConnectionType(serialComChannel);

        try {
            atModemConnectionType.connect();
        } catch (ConnectionException e) {
            if (!((ModemException) e.getCause()).getType().equals(ModemException.Type.MODEM_COULD_NOT_HANG_UP)) {
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

        RxTxAtModemConnectionType atModemConnectionType = createConnectionType(comChannel);
        atModemConnectionType.connect();

        verify(atModemConnectionType.getModemComponent(), times(1)).hangUpComChannel(comChannel, true);
    }

    private RxTxAtModemConnectionType createConnectionType(TestableSerialComChannel comChannel) throws ConnectionException, PropertyValidationException {
        return createConnectionType(comChannel, com.energyict.mdc.upl.TypedProperties.empty());
    }

    private RxTxAtModemConnectionType createConnectionType(TestableSerialComChannel comChannel, TypedProperties overrides) throws ConnectionException, PropertyValidationException {
        RxTxAtModemConnectionType atModemConnectionType = spy(new RxTxAtModemConnectionType(propertySpecService));
        TypedProperties properProperties = getProperProperties();
        properProperties.setAllProperties(overrides);
        atModemConnectionType.setUPLProperties(properProperties);
        AtModemComponent atModemComponent = spy(atModemConnectionType.getModemComponent());
        doReturn(atModemComponent).when(atModemConnectionType).getModemComponent();
        doReturn(comChannel).when(atModemConnectionType).newRxTxSerialConnection(any(SerialPortConfiguration.class));
        return atModemConnectionType;
    }

    @Test(timeout = TEST_TIMEOUT_MILLIS)
    public void testRestoreDefaultProfileSucceeds() throws Exception {
        AbstractModemTests.TestableSerialComChannel comChannel = getTestableComChannel();
        comChannel.setResponses(OK_LIST);

        RxTxAtModemConnectionType atModemConnectionType = createConnectionType(comChannel);

        atModemConnectionType.connect();

        verify(atModemConnectionType.getModemComponent(), times(1)).reStoreProfile(comChannel, true);
    }

    @Test(timeout = TEST_TIMEOUT_MILLIS, expected = ConnectionException.class)
    public void testRestoreDefaultProfileFails() throws Exception {
        AbstractModemTests.TestableSerialComChannel comChannel = getTestableComChannel();
        comChannel.setResponses(Arrays.asList(RUBBISH_FOR_FLUSH, "OK", "NokToDefaultProfileRestore"));

        RxTxAtModemConnectionType atModemConnectionType = createConnectionType(comChannel);
        try {
            atModemConnectionType.connect();
        } catch (ConnectionException e) {
            if (!((ModemException) e.getCause()).getType().equals(ModemException.Type.MODEM_COULD_NOT_RESTORE_DEFAULT_PROFILE)) {
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

        com.energyict.mdc.upl.TypedProperties typedProperties = com.energyict.mdc.upl.TypedProperties.empty();
        typedProperties.setProperty(TypedAtModemProperties.DELAY_BEFORE_SEND, Duration.ofMillis(10));
        typedProperties.setProperty(TypedAtModemProperties.COMMAND_TIMEOUT, Duration.ofMillis(COMMAND_TIMEOUT_VALUE));
        typedProperties.setProperty(TypedAtModemProperties.COMMAND_TRIES, new BigDecimal(3));

        RxTxAtModemConnectionType atModemConnectionType = createConnectionType(comChannel, typedProperties);
        final int numberOfTries = 3;
        try {
            atModemConnectionType.connect();
        } catch (ConnectionException e) {
            if (!((ModemException) e.getCause()).getType().equals(ModemException.Type.MODEM_COULD_NOT_HANG_UP)) {
                fail("Should have gotten exception indicating that the modem hangup failed, but was " + e.getMessage());
            }
            assertThat(((ModemException) e.getCause()).getMessageArguments()).contains(comPortName);
            verify(atModemConnectionType.getModemComponent(), times(numberOfTries)).readAndVerify(any(ComChannel.class), any(String.class), any(String.class), any(Long.class));
            throw e;
        }
    }

    @Test(timeout = TEST_TIMEOUT_MILLIS)
    public void writeSuccessfulInitStringsTest() throws Exception {
        AbstractModemTests.TestableSerialComChannel comChannel = getTestableComChannel();
        comChannel.setResponses(OK_LIST);

        RxTxAtModemConnectionType atModemConnectionType = createConnectionType(comChannel);
        atModemConnectionType.connect();

        verify(atModemConnectionType.getModemComponent(), times(1)).sendInitStrings(comChannel);
    }

    @Test(timeout = TEST_TIMEOUT_MILLIS, expected = ConnectionException.class)
    public void writeFailingInitStringTest() throws Exception {
        AbstractModemTests.TestableSerialComChannel comChannel = getTestableComChannel();
        comChannel.setResponses(Arrays.asList(RUBBISH_FOR_FLUSH, "OK", "OK", "Not_CorrectAnswer"));

        RxTxAtModemConnectionType atModemConnectionType = createConnectionType(comChannel);
        try {
            atModemConnectionType.connect();
        } catch (ConnectionException e) {
            if (!((ModemException) e.getCause()).getType().equals(ModemException.Type.MODEM_COULD_NOT_SEND_INIT_STRING)) {
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

        com.energyict.mdc.upl.TypedProperties typedProperties = com.energyict.mdc.upl.TypedProperties.empty();
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

        RxTxAtModemConnectionType atModemConnectionType = createConnectionType(comChannel, typedProperties);
        atModemConnectionType.connect();

        verify(atModemConnectionType.getModemComponent(), times(1)).sendInitStrings(comChannel);
        verify(atModemConnectionType.getModemComponent(), times(4)).writeSingleInitString(any(ComChannel.class), any(String.class)); // 1 global init string and 3 user defined init strings have been send out
    }

    @Test(timeout = TEST_TIMEOUT_MILLIS, expected = ConnectionException.class)
    public void busyErrorTest() throws Exception {
        AbstractModemTests.TestableSerialComChannel comChannel = getTestableComChannel();
        comChannel.setResponses(Arrays.asList(RUBBISH_FOR_FLUSH, "OK", "BUSY"));

        RxTxAtModemConnectionType atModemConnectionType = createConnectionType(comChannel);
        try {
            atModemConnectionType.connect();
        } catch (ConnectionException e) {
            if (!((ModemException) e.getCause()).getType().equals(ModemException.Type.AT_MODEM_BUSY)) {
                fail("Should have gotten exception indicating that the modem received a BUSY signal, but was " + e.getMessage());
            }
            assertThat(((ModemException) e.getCause()).getMessageArguments()).contains(comPortName);
            verify(atModemConnectionType.getModemComponent(), times(1)).hangUpComChannel(comChannel, true);
            verify(atModemConnectionType.getModemComponent(), times(1)).reStoreProfile(comChannel, true);
            verify(atModemConnectionType.getModemComponent(), never()).sendInitStrings(comChannel);
            throw e;
        }
    }

    @Test(timeout = TEST_TIMEOUT_MILLIS, expected = ConnectionException.class)
    public void errorAnswerTest() throws Exception {
        AbstractModemTests.TestableSerialComChannel comChannel = getTestableComChannel();
        comChannel.setResponses(Arrays.asList(RUBBISH_FOR_FLUSH, "OK", "ERROR"));

        RxTxAtModemConnectionType atModemConnectionType = createConnectionType(comChannel);
        try {
            atModemConnectionType.connect();
        } catch (ConnectionException e) {
            if (!((ModemException) e.getCause()).getType().equals(ModemException.Type.AT_MODEM_ERROR)) {
                fail("Should have gotten exception indicating that the modem received a ERROR signal, but was " + e.getMessage());
            }
            assertThat(((ModemException) e.getCause()).getMessageArguments()).contains(comPortName);
            verify(atModemConnectionType.getModemComponent(), times(1)).hangUpComChannel(comChannel, true);
            verify(atModemConnectionType.getModemComponent(), times(1)).reStoreProfile(comChannel, true);
            verify(atModemConnectionType.getModemComponent(), never()).sendInitStrings(comChannel);
            throw e;
        }
    }

    @Test(timeout = TEST_TIMEOUT_MILLIS, expected = ConnectionException.class)
    public void noAnswerErrorTest() throws Exception {
        AbstractModemTests.TestableSerialComChannel comChannel = getTestableComChannel();
        comChannel.setResponses(Arrays.asList(RUBBISH_FOR_FLUSH, "OK", "NO ANSWER"));

        RxTxAtModemConnectionType atModemConnectionType = createConnectionType(comChannel);
        try {
            atModemConnectionType.connect();
        } catch (ConnectionException e) {
            if (!((ModemException) e.getCause()).getType().equals(ModemException.Type.AT_MODEM_NO_ANSWER)) {
                fail("Should have gotten exception indicating that the modem received a NO ANSWER signal, but was " + e.getMessage());
            }
            assertThat(((ModemException) e.getCause()).getMessageArguments()).contains(comPortName);
            verify(atModemConnectionType.getModemComponent(), times(1)).hangUpComChannel(comChannel, true);
            verify(atModemConnectionType.getModemComponent(), times(1)).reStoreProfile(comChannel, true);
            verify(atModemConnectionType.getModemComponent(), never()).sendInitStrings(comChannel);
            throw e;
        }
    }


    @Test(timeout = TEST_TIMEOUT_MILLIS, expected = ConnectionException.class)
    public void noCarrierErrorTest() throws Exception {
        AbstractModemTests.TestableSerialComChannel comChannel = getTestableComChannel();
        comChannel.setResponses(Arrays.asList(RUBBISH_FOR_FLUSH, "OK", "NO CARRIER"));

        RxTxAtModemConnectionType atModemConnectionType = createConnectionType(comChannel);
        try {
            atModemConnectionType.connect();
        } catch (ConnectionException e) {
            if (!((ModemException) e.getCause()).getType().equals(ModemException.Type.AT_MODEM_NO_CARRIER)) {
                fail("Should have gotten exception indicating that the modem received a NO CARRIER signal, but was " + e.getMessage());
            }
            assertThat(((ModemException) e.getCause()).getMessageArguments()).contains(comPortName);
            verify(atModemConnectionType.getModemComponent(), times(1)).hangUpComChannel(comChannel, true);
            verify(atModemConnectionType.getModemComponent(), times(1)).reStoreProfile(comChannel, true);
            verify(atModemConnectionType.getModemComponent(), never()).sendInitStrings(comChannel);
            throw e;
        }
    }

    @Test(timeout = TEST_TIMEOUT_MILLIS, expected = ConnectionException.class)
    public void noDialtoneErrorTest() throws Exception {
        AbstractModemTests.TestableSerialComChannel comChannel = getTestableComChannel();
        comChannel.setResponses(Arrays.asList(RUBBISH_FOR_FLUSH, "OK", "NO DIALTONE"));

        RxTxAtModemConnectionType atModemConnectionType = createConnectionType(comChannel);
        try {
            atModemConnectionType.connect();
        } catch (ConnectionException e) {
            if (!((ModemException) e.getCause()).getType().equals(ModemException.Type.AT_MODEM_NO_DIALTONE)) {
                fail("Should have gotten exception indicating that the modem received a NO DIALTONE signal, but was " + e.getMessage());
            }
            assertThat(((ModemException) e.getCause()).getMessageArguments()).contains(comPortName);
            verify(atModemConnectionType.getModemComponent(), times(1)).hangUpComChannel(comChannel, true);
            verify(atModemConnectionType.getModemComponent(), times(1)).reStoreProfile(comChannel, true);
            verify(atModemConnectionType.getModemComponent(), never()).sendInitStrings(comChannel);
            throw e;
        }
    }

    @Test(timeout = TEST_TIMEOUT_MILLIS)
    public void verifyConnectSuccess() throws Exception {
        AbstractModemTests.TestableSerialComChannel comChannel = getTestableComChannel();
        comChannel.setResponses(OK_LIST);

        RxTxAtModemConnectionType atModemConnectionType = createConnectionType(comChannel);
        atModemConnectionType.connect();

        verify(atModemConnectionType.getModemComponent(), times(1)).dialModem(comChannel);
    }

    @Test(timeout = TEST_TIMEOUT_MILLIS, expected = ConnectionException.class)
    public void verifyConnectBusy() throws Exception {
        AbstractModemTests.TestableSerialComChannel comChannel = getTestableComChannel();
        comChannel.setResponses(Arrays.asList(RUBBISH_FOR_FLUSH, "OK", "OK", "OK", "BUSY", "OK", "OK"));

        RxTxAtModemConnectionType atModemConnectionType = createConnectionType(comChannel);
        try {
            atModemConnectionType.connect();
        } catch (ConnectionException e) {
            if (!((ModemException) e.getCause()).getType().equals(ModemException.Type.AT_MODEM_BUSY)) {
                fail("Should have gotten exception indicating that the connect failed with a busy command, but was " + e.getMessage());
            }
            assertThat(((ModemException) e.getCause()).getMessageArguments()).contains(comPortName);
            verify(atModemConnectionType.getModemComponent(), times(1)).dialModem(comChannel);
            throw e;
        }
    }

    @Test(timeout = TEST_TIMEOUT_MILLIS)
    public void testWithNoSelector() throws Exception {
        AbstractModemTests.TestableSerialComChannel comChannel = getTestableComChannel();
        comChannel.setResponses(OK_LIST);

        RxTxAtModemConnectionType atModemConnectionType = createConnectionType(comChannel);
        doReturn(comChannel).when(atModemConnectionType).newRxTxSerialConnection(any(SerialPortConfiguration.class));

        atModemConnectionType.connect();

        verify(atModemConnectionType.getModemComponent(), times(1)).dialModem(comChannel);
        verify(atModemConnectionType.getModemComponent(), never()).sendAddressSelector(comChannel);
    }

    @Test(timeout = TEST_TIMEOUT_MILLIS)
    public void testWithAddressSelector() throws Exception {
        AbstractModemTests.TestableSerialComChannel comChannel = getTestableComChannel();
        comChannel.setResponses(OK_LIST);

        TypedProperties typedProperties = com.energyict.mdc.upl.TypedProperties.empty();
        typedProperties.setProperty(TypedAtModemProperties.MODEM_ADDRESS_SELECTOR, "AddressSelect_01");
        RxTxAtModemConnectionType atModemConnectionType = createConnectionType(comChannel, typedProperties);
        atModemConnectionType.connect();

        verify(atModemConnectionType.getModemComponent(), times(1)).dialModem(comChannel);
        verify(atModemConnectionType.getModemComponent(), times(1)).sendAddressSelector(comChannel);
    }

}
