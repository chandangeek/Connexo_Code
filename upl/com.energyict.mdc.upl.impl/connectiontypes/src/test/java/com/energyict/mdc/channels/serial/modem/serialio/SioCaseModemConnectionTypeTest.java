/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.channels.serial.modem.serialio;

import com.energyict.mdc.channel.serial.SerialPortConfiguration;
import com.energyict.mdc.channel.serial.ServerSerialPort;
import com.energyict.mdc.channel.serial.SignalController;
import com.energyict.mdc.channel.serial.modemproperties.TypedAtModemProperties;
import com.energyict.mdc.channels.serial.modem.AbstractModemTests;
import com.energyict.mdc.channels.serial.modem.CaseModemComponent;
import com.energyict.mdc.channels.serial.modem.TypedCaseModemProperties;
import com.energyict.mdc.upl.io.ConnectionType;
import com.energyict.mdc.upl.io.ModemException;
import com.energyict.mdc.upl.properties.PropertyValidationException;
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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for the {@link SioCaseModemConnectionType} component
 *
 * @author sva
 * @since 30/04/13 - 14:43
 */
@RunWith(MockitoJUnitRunner.class)
public class SioCaseModemConnectionTypeTest extends AbstractModemTests {

    private static final int DTR_TOGGLE_DELAY_VALUE = 100;

    private final List<String> OK_LIST = Arrays.asList(RUBBISH_FOR_FLUSH, "ECHO OFF", "DTR NORMAL", "ERROR CORRECTING MODE", "LINK ESTABLISHED");

    private AbstractModemTests.TestableSerialComChannel getTestableComChannel() {
        InputStream inputStream = mock(InputStream.class);
        OutputStream outputStream = mock(OutputStream.class);
        ServerSerialPort serialPort = mock(ServerSerialPort.class);
        SignalController signalController = mock(SignalController.class);
        when(serialPort.getInputStream()).thenReturn(inputStream);
        when(serialPort.getOutputStream()).thenReturn(outputStream);
        when(serialPort.getSerialPortSignalController()).thenReturn(signalController);
        return new TestableSerialComChannel(serialPort);
    }

    private TypedProperties getProperProperties() {
        com.energyict.protocolimpl.properties.TypedProperties result = com.energyict.protocolimpl.properties.TypedProperties.empty();

        result.setProperty(TypedAtModemProperties.DELAY_BEFORE_SEND, Duration.ofMillis(10));
        result.setProperty(TypedAtModemProperties.COMMAND_TIMEOUT, Duration.ofMillis(COMMAND_TIMEOUT_VALUE));
        result.setProperty(TypedAtModemProperties.COMMAND_TRIES, new BigDecimal(1));
        result.setProperty(TypedAtModemProperties.MODEM_INIT_STRINGS, "");
        result.setProperty(TypedAtModemProperties.DELAY_AFTER_CONNECT, Duration.ofMillis(10));
        result.setProperty(TypedAtModemProperties.CONNECT_TIMEOUT, Duration.ofMillis(COMMAND_TIMEOUT_VALUE));
        result.setProperty(TypedAtModemProperties.MODEM_DIAL_PREFIX, "");
        result.setProperty(TypedAtModemProperties.MODEM_ADDRESS_SELECTOR, "");
        result.setProperty(TypedAtModemProperties.DTR_TOGGLE_DELAY, Duration.ofMillis(DTR_TOGGLE_DELAY_VALUE));
        result.setProperty(TypedAtModemProperties.PHONE_NUMBER_PROPERTY_NAME, PHONE_NUMBER);
        result.setProperty(ConnectionType.Property.COMP_PORT_NAME.getName(), comPortName);

        return result;
    }

    @Test(timeout = TEST_TIMEOUT_MILLIS, expected = ConnectionException.class)
    public void readTimeOutExceptionTest() throws Exception {
        TestableSerialComChannel serialComChannel = getTestableComChannel();

        SioCaseModemConnectionType caseModemConnectionType = createConnectionType(serialComChannel);

        try {
            caseModemConnectionType.connect();
        } catch (ConnectionException e) {
            if (!((ModemException) e.getCause()).getType().equals(ModemException.Type.MODEM_COULD_NOT_SEND_INIT_STRING)) {
                fail("Should have gotten exception indicating that the modem init string could not be sent, but was " + e.getMessage());
            }
            throw e;
        }
    }

    @Test(timeout = TEST_TIMEOUT_MILLIS)
    public void writeSuccessfulInitStringsTest() throws Exception {
        TestableSerialComChannel comChannel = getTestableComChannel();
        comChannel.setResponses(OK_LIST);

        SioCaseModemConnectionType caseModemConnectionType = createConnectionType(comChannel);

        caseModemConnectionType.connect();

        verify(caseModemConnectionType.getModemComponent(), times(1)).sendInitStrings(comChannel);
    }

    @Test(timeout = TEST_TIMEOUT_MILLIS, expected = ConnectionException.class)
    public void writeFailingInitStringTest() throws Exception {
        TestableSerialComChannel comChannel = getTestableComChannel();
        comChannel.setResponses(Arrays.asList(RUBBISH_FOR_FLUSH, "ECHO OFF", "DTR NORMAL", "Not_CorrectAnswer"));

        SioCaseModemConnectionType caseModemConnectionType = createConnectionType(comChannel);

        try {
            caseModemConnectionType.connect();
        } catch (ConnectionException e) {
            if (!((ModemException) e.getCause()).getType().equals(ModemException.Type.MODEM_COULD_NOT_SEND_INIT_STRING)) {
                fail("Should have gotten exception indicating that the modem init string could not be sent, but was " + e.getMessage());
            }
            assertThat(((ModemException) e.getCause()).getMessageArguments()).contains(comPortName, "Not_CorrectAnswer", "V0");
            throw e;
        }
    }

    @Test(timeout = TEST_TIMEOUT_MILLIS, expected = ConnectionException.class)
    public void errorAnswerTest() throws Exception {
        TestableSerialComChannel comChannel = getTestableComChannel();
        comChannel.setResponses(Arrays.asList(RUBBISH_FOR_FLUSH, "ECHO OFF", "CALL ABORTED"));

        SioCaseModemConnectionType caseModemConnectionType = createConnectionType(comChannel);

        try {
            caseModemConnectionType.connect();
        } catch (ConnectionException e) {
            if (!((ModemException) e.getCause()).getType().equals(ModemException.Type.MODEM_CALL_ABORTED)) {
                fail("Should have gotten exception indicating that the modem received a ERROR signal, but was " + e.getMessage());
            }
            assertThat(((ModemException) e.getCause()).getMessageArguments()).contains(comPortName);
            verify(caseModemConnectionType.getModemComponent()).sendInitStrings(comChannel);
            verify(caseModemConnectionType.getModemComponent(), never()).dialModem(comChannel);
            throw e;
        }
    }

    @Test(timeout = TEST_TIMEOUT_MILLIS)
    public void verifyConnectSuccess() throws Exception {
        TestableSerialComChannel comChannel = getTestableComChannel();
        comChannel.setResponses(OK_LIST);

        SioCaseModemConnectionType caseModemConnectionType = createConnectionType(comChannel);
        caseModemConnectionType.connect();

        verify(caseModemConnectionType.getModemComponent(), times(1)).dialModem(comChannel);
    }

    @Test(timeout = TEST_TIMEOUT_MILLIS)
    public void testWithNoSelector() throws Exception {
        TestableSerialComChannel comChannel = getTestableComChannel();
        comChannel.setResponses(OK_LIST);

        SioCaseModemConnectionType caseModemConnectionType = createConnectionType(comChannel);
        caseModemConnectionType.connect();

        verify(caseModemConnectionType.getModemComponent(), times(1)).dialModem(comChannel);
        verify(caseModemConnectionType.getModemComponent(), never()).sendAddressSelector(comChannel);
    }

    @Test(timeout = TEST_TIMEOUT_MILLIS)
    public void testWithAddressSelector() throws Exception {
        TestableSerialComChannel comChannel = getTestableComChannel();
        comChannel.setResponses(OK_LIST);

        TypedProperties typedProperties = com.energyict.protocolimpl.properties.TypedProperties.empty();
        typedProperties.setProperty(TypedCaseModemProperties.MODEM_ADDRESS_SELECTOR, "AddressSelect_01");

        SioCaseModemConnectionType caseModemConnectionType = createConnectionType(comChannel, typedProperties);
        caseModemConnectionType.connect();

        verify(caseModemConnectionType.getModemComponent(), times(1)).dialModem(comChannel);
        verify(caseModemConnectionType.getModemComponent(), times(1)).sendAddressSelector(comChannel);
    }

    private SioCaseModemConnectionType createConnectionType(TestableSerialComChannel comChannel) throws ConnectionException, PropertyValidationException {
        return createConnectionType(comChannel, com.energyict.protocolimpl.properties.TypedProperties.empty());
    }

    private SioCaseModemConnectionType createConnectionType(TestableSerialComChannel comChannel, TypedProperties overrides) throws ConnectionException, PropertyValidationException {
        SioCaseModemConnectionType atModemConnectionType = spy(new SioCaseModemConnectionType(propertySpecService));
        TypedProperties properProperties = getProperProperties();
        properProperties.setAllProperties(overrides);
        atModemConnectionType.setUPLProperties(properProperties);
        CaseModemComponent caseModemComponent = spy(atModemConnectionType.getModemComponent());
        doReturn(caseModemComponent).when(atModemConnectionType).getModemComponent();
        doReturn(comChannel).when(atModemConnectionType).newSioSerialConnection(any(SerialPortConfiguration.class));
        return atModemConnectionType;
    }
}