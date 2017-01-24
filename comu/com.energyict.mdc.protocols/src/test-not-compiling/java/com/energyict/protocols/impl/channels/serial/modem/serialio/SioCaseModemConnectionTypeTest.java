package com.energyict.protocols.impl.channels.serial.modem.serialio;

import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.io.impl.CaseModemProperties;
import com.energyict.protocol.exceptions.ConnectionException;
import com.energyict.mdc.protocol.api.SerialConnectionPropertyNames;
import com.energyict.mdc.protocol.api.dynamic.ConnectionProperty;
import com.energyict.mdc.io.ModemException;
import com.energyict.mdc.channels.serial.SerialPortConfiguration;
import com.energyict.mdc.channels.serial.ServerSerialPort;
import com.energyict.mdc.channels.serial.direct.serialio.SioSerialPort;

import com.energyict.protocols.impl.channels.serial.modem.AbstractModemTests;
import com.energyict.mdc.io.impl.CaseModemComponent;
import com.energyict.mdc.channels.serial.SignalController;
import com.energyict.mdc.io.impl.TypedCaseModemProperties;
import com.energyict.mdc.io.impl.TypedPaknetModemProperties;

import com.energyict.mdc.channels.serial.modem.CaseModemComponent;
import com.energyict.mdc.channels.serial.modem.TypedCaseModemProperties;
import com.energyict.mdc.channels.serial.modem.TypedPaknetModemProperties;
import com.energyict.mdc.exceptions.ModemException;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.tasks.ConnectionTaskPropertyImpl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for the {@link com.energyict.protocols.impl.channels.serial.modem.serialio.SioCaseModemConnectionType} component
 *
 * @author sva
 * @since 30/04/13 - 14:43
 */
@RunWith(MockitoJUnitRunner.class)
public class SioCaseModemConnectionTypeTest extends AbstractModemTests {

    private static final int TEST_TIMEOUT_MILLIS = 5000;
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

    private List<ConnectionProperty> getProperProperties (ComPort comPort) {
        ConnectionTaskPropertyImpl delayBeforeSendProperty = new ConnectionTaskPropertyImpl(TypedCaseModemProperties.DELAY_BEFORE_SEND);
        delayBeforeSendProperty.setValue(new TimeDuration(10, TimeDuration.TimeUnit.MILLISECONDS));
        ConnectionTaskPropertyImpl commandTimeout = new ConnectionTaskPropertyImpl(TypedCaseModemProperties.COMMAND_TIMEOUT);
        commandTimeout.setValue(new TimeDuration(COMMAND_TIMEOUT_VALUE, TimeDuration.TimeUnit.MILLISECONDS));
        ConnectionTaskPropertyImpl commandTries = new ConnectionTaskPropertyImpl(TypedCaseModemProperties.COMMAND_TRIES);
        commandTries.setValue(new BigDecimal(1));
        ConnectionTaskPropertyImpl modemInitStrings = new ConnectionTaskPropertyImpl(TypedCaseModemProperties.MODEM_INIT_STRINGS);
        modemInitStrings.setValue("");
        ConnectionTaskPropertyImpl delayAfterConnect = new ConnectionTaskPropertyImpl(TypedCaseModemProperties.DELAY_AFTER_CONNECT);
        delayAfterConnect.setValue(new TimeDuration(10, TimeDuration.TimeUnit.MILLISECONDS));
        ConnectionTaskPropertyImpl connectTimeOut = new ConnectionTaskPropertyImpl(TypedCaseModemProperties.CONNECT_TIMEOUT);
        connectTimeOut.setValue(new TimeDuration(COMMAND_TIMEOUT_VALUE, TimeDuration.TimeUnit.MILLISECONDS));
        ConnectionTaskPropertyImpl dialPrefix = new ConnectionTaskPropertyImpl(TypedCaseModemProperties.MODEM_DIAL_PREFIX);
        dialPrefix.setValue("");
        ConnectionTaskPropertyImpl addressSelector = new ConnectionTaskPropertyImpl(TypedCaseModemProperties.MODEM_ADDRESS_SELECTOR);
        addressSelector.setValue("");
        ConnectionTaskPropertyImpl dtrToggleDelay = new ConnectionTaskPropertyImpl(TypedPaknetModemProperties.DTR_TOGGLE_DELAY);
        dtrToggleDelay.setValue(new TimeDuration(DTR_TOGGLE_DELAY_VALUE, TimeDuration.TimeUnit.MILLISECONDS));
        ConnectionTaskPropertyImpl phoneNumber = new ConnectionTaskPropertyImpl(TypedCaseModemProperties.PHONE_NUMBER_PROPERTY_NAME);
        phoneNumber.setValue(PHONE_NUMBER);
        ConnectionProperty comPortConnectionProperty = mock(ConnectionProperty.class);
        when(comPortConnectionProperty.getName()).thenReturn(SerialConnectionPropertyNames.COMPORT_NAME_PROPERTY_NAME.propertyName());
        when(comPortConnectionProperty.getValue()).thenReturn(this.comPortName);

        return Arrays.asList(
                delayBeforeSendProperty,
                commandTimeout,
                commandTries,
                modemInitStrings,
                delayAfterConnect,
                connectTimeOut,
                dialPrefix,
                addressSelector,
                dtrToggleDelay,
                phoneNumber,
                comPortConnectionProperty);
    }

    private ComPort getProperlyMockedComPort(TestableSerialComChannel serialComChannel, SioSerialPort sioSerialPort) throws Exception {
        ComPort comPort = mock(ComPort.class);
        when(comPort.getName()).thenReturn(comPortName);
        when(this.serialComponentFactory.newSioSerialPort(any(SerialPortConfiguration.class))).thenReturn(sioSerialPort);
        when(this.serialComponentFactory.newSerialComChannel(any(ServerSerialPort.class))).thenReturn(serialComChannel);
        return comPort;
    }

    @Test(timeout = TEST_TIMEOUT_MILLIS, expected = ConnectionException.class)
    public void readTimeOutExceptionTest() throws Exception {
        TestableSerialComChannel serialComChannel = getTestableComChannel();
        SioSerialPort sioSerialPort = mock(SioSerialPort.class);
        ComPort comPort = getProperlyMockedComPort(serialComChannel, sioSerialPort);

        CaseModemComponent caseModemComponent = spy(new CaseModemComponent(new TypedCaseModemProperties(getProperProperties(comPort))));
        when(this.serialComponentFactory.newCaseModemComponent(any(CaseModemProperties.class))).thenReturn(caseModemComponent);
        SioCaseModemConnectionType caseModemConnectionType = new SioCaseModemConnectionType();

        try {
            caseModemConnectionType.connect(getProperProperties(comPort));
        } catch (ConnectionException e) {
            if (!((ModemException) e.getCause()).getMessageId().equals("CSM-COM-207")) {
                fail("Should have gotten exception indicating that the modem init string could not be sent, but was " + e.getMessage());
            }
            throw e;
        }
    }

    @Test(timeout = TEST_TIMEOUT_MILLIS)
    public void writeSuccessfulInitStringsTest() throws Exception {
        TestableSerialComChannel comChannel = getTestableComChannel();
        comChannel.setResponses(OK_LIST);
        SioSerialPort sioSerialPort = mock(SioSerialPort.class);
        ComPort comPort = getProperlyMockedComPort(comChannel, sioSerialPort);

        CaseModemComponent atModemComponent = spy(new CaseModemComponent(new TypedCaseModemProperties(getProperProperties(comPort))));
        when(this.serialComponentFactory.newCaseModemComponent(any(CaseModemProperties.class))).thenReturn(atModemComponent);
        SioCaseModemConnectionType caseModemConnectionType = spy(new SioCaseModemConnectionType());

        caseModemConnectionType.connect(getProperProperties(comPort));

        verify(atModemComponent, times(1)).sendInitStrings(comChannel);
    }

    @Test(timeout = TEST_TIMEOUT_MILLIS, expected = ConnectionException.class)
    public void writeFailingInitStringTest() throws Exception {
        TestableSerialComChannel comChannel = getTestableComChannel();
        comChannel.setResponses(Arrays.asList(RUBBISH_FOR_FLUSH, "ECHO OFF", "DTR NORMAL", "Not_CorrectAnswer"));
        SioSerialPort sioSerialPort = mock(SioSerialPort.class);
        ComPort comPort = getProperlyMockedComPort(comChannel, sioSerialPort);


        CaseModemComponent atModemComponent = spy(new CaseModemComponent(new TypedCaseModemProperties(getProperProperties(comPort))));
        when(this.serialComponentFactory.newCaseModemComponent(any(CaseModemProperties.class))).thenReturn(atModemComponent);
        SioCaseModemConnectionType caseModemConnectionType = spy(new SioCaseModemConnectionType());

        try {
            caseModemConnectionType.connect(getProperProperties(comPort));
        } catch (ConnectionException e) {
            if (!((ModemException) e.getCause()).getMessageId().equals("CSM-COM-207")) {
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
        SioSerialPort sioSerialPort = mock(SioSerialPort.class);
        ComPort comPort = getProperlyMockedComPort(comChannel, sioSerialPort);

        CaseModemComponent modemComponent = spy(new CaseModemComponent(new TypedCaseModemProperties(getProperProperties(comPort))));
        when(this.serialComponentFactory.newCaseModemComponent(any(CaseModemProperties.class))).thenReturn(modemComponent);
        SioCaseModemConnectionType caseModemConnectionType = spy(new SioCaseModemConnectionType());

        try {
            caseModemConnectionType.connect(getProperProperties(comPort));
        } catch (ConnectionException e) {
            if (!((ModemException) e.getCause()).getMessageId().equals("CSM-COM-216")) {
                fail("Should have gotten exception indicating that the modem received a ERROR signal, but was " + e.getMessage());
            }
            assertThat(((ModemException) e.getCause()).getMessageArguments()).contains(comPortName);
            verify(modemComponent).sendInitStrings(comChannel);
            verify(modemComponent, never()).dialModem(comChannel);
            throw e;
        }
    }

    @Test(timeout = TEST_TIMEOUT_MILLIS)
    public void verifyConnectSuccess() throws Exception {
        TestableSerialComChannel comChannel = getTestableComChannel();
        comChannel.setResponses(OK_LIST);
        SioSerialPort sioSerialPort = mock(SioSerialPort.class);
        ComPort comPort = getProperlyMockedComPort(comChannel, sioSerialPort);

        CaseModemComponent modemComponent = spy(new CaseModemComponent(new TypedCaseModemProperties(getProperProperties(comPort))));
        when(this.serialComponentFactory.newCaseModemComponent(any(CaseModemProperties.class))).thenReturn(modemComponent);
        SioCaseModemConnectionType caseModemConnectionType = spy(new SioCaseModemConnectionType());

        caseModemConnectionType.connect(getProperProperties(comPort));

        verify(modemComponent, times(1)).dialModem(comChannel);
    }

    @Test(timeout = TEST_TIMEOUT_MILLIS)
    public void testWithNoSelector() throws Exception {
        TestableSerialComChannel comChannel = getTestableComChannel();
        comChannel.setResponses(OK_LIST);
        SioSerialPort sioSerialPort = mock(SioSerialPort.class);
        ComPort comPort = getProperlyMockedComPort(comChannel, sioSerialPort);

        CaseModemComponent modemComponent = spy(new CaseModemComponent(new TypedCaseModemProperties(getProperProperties(comPort))));
        when(this.serialComponentFactory.newCaseModemComponent(any(CaseModemProperties.class))).thenReturn(modemComponent);
        SioCaseModemConnectionType caseModemConnectionType = spy(new SioCaseModemConnectionType());

        caseModemConnectionType.connect(getProperProperties(comPort));

        verify(modemComponent, times(1)).dialModem(comChannel);
        verify(modemComponent, never()).sendAddressSelector(comChannel);
    }

    @Test(timeout = TEST_TIMEOUT_MILLIS)
    public void testWithAddressSelector() throws Exception {
        TestableSerialComChannel comChannel = getTestableComChannel();
        comChannel.setResponses(OK_LIST);
        SioSerialPort sioSerialPort = mock(SioSerialPort.class);
        ComPort comPort = getProperlyMockedComPort(comChannel, sioSerialPort);

        List<ConnectionProperty> properProperties = getProperProperties(comPort);
        for (ConnectionProperty properProperty : properProperties) {
            if (properProperty.getName().equals(TypedCaseModemProperties.MODEM_ADDRESS_SELECTOR)) {
                ((ConnectionTaskPropertyImpl) properProperty).setValue("AddressSelect_01");
            }
        }
        CaseModemComponent modemComponent = spy(new CaseModemComponent(new TypedCaseModemProperties(properProperties)));
        when(this.serialComponentFactory.newCaseModemComponent(any(CaseModemProperties.class))).thenReturn(modemComponent);
        SioCaseModemConnectionType caseModemConnectionType = spy(new SioCaseModemConnectionType());

        caseModemConnectionType.connect(properProperties);

        verify(modemComponent, times(1)).dialModem(comChannel);
        verify(modemComponent, times(1)).sendAddressSelector(comChannel);

    }
}
