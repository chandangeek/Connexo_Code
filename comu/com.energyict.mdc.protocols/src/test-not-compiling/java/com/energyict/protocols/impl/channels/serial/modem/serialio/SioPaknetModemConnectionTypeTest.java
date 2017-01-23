package com.energyict.protocols.impl.channels.serial.modem.serialio;

import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.io.impl.PaknetModemProperties;
import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.mdc.protocol.api.SerialConnectionPropertyNames;
import com.energyict.mdc.protocol.api.dynamic.ConnectionProperty;
import com.energyict.mdc.io.ModemException;
import com.energyict.mdc.tasks.ConnectionTaskPropertyImpl;
import com.energyict.mdc.channels.serial.SerialPortConfiguration;
import com.energyict.mdc.channels.serial.ServerSerialPort;
import com.energyict.mdc.channels.serial.direct.serialio.SioSerialPort;
import com.energyict.protocols.impl.channels.serial.modem.AbstractModemTests;

import com.energyict.mdc.io.impl.PaknetModemComponent;
import com.energyict.mdc.channels.serial.SignalController;
import com.energyict.mdc.io.impl.TypedPaknetModemProperties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for the {@link com.energyict.protocols.impl.channels.serial.modem.serialio.SioPaknetModemConnectionType} component
 *
 * @author sva
 * @since 15/04/13 - 11:33
 */
@RunWith(MockitoJUnitRunner.class)
public class SioPaknetModemConnectionTypeTest extends AbstractModemTests {

    private static final int TEST_TIMEOUT_MILLIS = 5000;
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

    private List<ConnectionProperty> getProperProperties() {
        ConnectionTaskPropertyImpl delayBeforeSendProperty = new ConnectionTaskPropertyImpl(TypedPaknetModemProperties.DELAY_BEFORE_SEND);
        delayBeforeSendProperty.setValue(new TimeDuration(10, TimeDuration.TimeUnit.MILLISECONDS));
        ConnectionTaskPropertyImpl atCommandTimeout = new ConnectionTaskPropertyImpl(TypedPaknetModemProperties.COMMAND_TIMEOUT);
        atCommandTimeout.setValue(new TimeDuration(COMMAND_TIMEOUT_VALUE, TimeDuration.TimeUnit.MILLISECONDS));
        ConnectionTaskPropertyImpl atCommandTries = new ConnectionTaskPropertyImpl(TypedPaknetModemProperties.COMMAND_TRIES);
        atCommandTries.setValue(new BigDecimal(1));
        ConnectionTaskPropertyImpl atModemInitStrings = new ConnectionTaskPropertyImpl(TypedPaknetModemProperties.MODEM_INIT_STRINGS);
        atModemInitStrings.setValue("1:0,2:0,3:0,4:10,5:0,6:5");
        ConnectionTaskPropertyImpl delayAfterConnect = new ConnectionTaskPropertyImpl(TypedPaknetModemProperties.DELAY_AFTER_CONNECT);
        delayAfterConnect.setValue(new TimeDuration(10, TimeDuration.TimeUnit.MILLISECONDS));
        ConnectionTaskPropertyImpl connectTimeOut = new ConnectionTaskPropertyImpl(TypedPaknetModemProperties.CONNECT_TIMEOUT);
        connectTimeOut.setValue(new TimeDuration(COMMAND_TIMEOUT_VALUE, TimeDuration.TimeUnit.MILLISECONDS));
        ConnectionTaskPropertyImpl dialPrefix = new ConnectionTaskPropertyImpl(TypedPaknetModemProperties.MODEM_DIAL_PREFIX);
        dialPrefix.setValue("");
        ConnectionTaskPropertyImpl dtrToggleDelay = new ConnectionTaskPropertyImpl(TypedPaknetModemProperties.DTR_TOGGLE_DELAY);
        dtrToggleDelay.setValue(new TimeDuration(DTR_TOGGLE_DELAY_VALUE, TimeDuration.TimeUnit.MILLISECONDS));
        ConnectionTaskPropertyImpl phoneNumber = new ConnectionTaskPropertyImpl(TypedPaknetModemProperties.PHONE_NUMBER_PROPERTY_NAME);
        phoneNumber.setValue(PHONE_NUMBER);

        List<ConnectionProperty> connectionProperties = new ArrayList<>();
        connectionProperties.add(delayBeforeSendProperty);
        connectionProperties.add(atCommandTimeout);
        connectionProperties.add(atCommandTries);
        connectionProperties.add(atModemInitStrings);
        connectionProperties.add(delayAfterConnect);
        connectionProperties.add(connectTimeOut);
        connectionProperties.add(dialPrefix);
        connectionProperties.add(dtrToggleDelay);
        connectionProperties.add(phoneNumber);
        return connectionProperties;
    }

    private ComPort getProperlyMockedComPort(TestableSerialComChannel serialComChannel, SioSerialPort sioSerialPort) throws Exception {
        ComPort comPort = mock(ComPort.class);

        when(comPort.getName()).thenReturn(comPortName);
        when(this.serialComponentFactory.newSioSerialPort(any(SerialPortConfiguration.class))).thenReturn(sioSerialPort);
        when(this.serialComponentFactory.newSerialComChannel(any(ServerSerialPort.class))).thenReturn(serialComChannel);
        return comPort;
    }

    @Test(timeout = TEST_TIMEOUT_MILLIS)
    public void testDisconnectModemBeforeNewSessionSucceeds() throws Exception {
        TestableSerialComChannel comChannel = getTestableComChannel();
        comChannel.setResponses(OK_LIST);
        SioSerialPort sioSerialPort = mock(SioSerialPort.class);
        ComPort comPort = getProperlyMockedComPort(comChannel, sioSerialPort);

        PaknetModemComponent modemComponent = spy(new PaknetModemComponent(new TypedPaknetModemProperties(getProperProperties())));
        when(this.serialComponentFactory.newPaknetModemComponent(any(PaknetModemProperties.class))).thenReturn(modemComponent);
        SioPaknetModemConnectionType modemConnectionType = spy(new SioPaknetModemConnectionType());

        modemConnectionType.connect(getProperProperties());

        verify(modemComponent, times(1)).disconnectModemBeforeNewSession(comChannel);
    }

    @Test(timeout = TEST_TIMEOUT_MILLIS, expected = ConnectionException.class)
    public void testInitializeCommandStateFails() throws Exception {
        PaknetModemComponent modemComponent = new PaknetModemComponent(new TypedPaknetModemProperties(getProperProperties()));
        when(this.serialComponentFactory.newPaknetModemComponent(any(PaknetModemProperties.class))).thenReturn(modemComponent);
        TestableSerialComChannel serialComChannel = getTestableComChannel();
        serialComChannel.setResponses(Arrays.asList("   ", "NotValidResponse"));
        SioSerialPort sioSerialPort = mock(SioSerialPort.class);
        ComPort comPort = getProperlyMockedComPort(serialComChannel, sioSerialPort);

        SioPaknetModemConnectionType modemConnectionType = new SioPaknetModemConnectionType();

        try {
            modemConnectionType.connect(getProperProperties());
        } catch (ModemException e) {
            if (!((ModemException) e.getCause()).getMessageId().equals("CSM-COM-215")) {
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
        ComPort comPort = getProperlyMockedComPort(comChannel, sioSerialPort);

        PaknetModemComponent modemComponent = spy(new PaknetModemComponent(new TypedPaknetModemProperties(getProperProperties())));
        when(this.serialComponentFactory.newPaknetModemComponent(any(PaknetModemProperties.class))).thenReturn(modemComponent);
        SioPaknetModemConnectionType modemConnectionType = spy(new SioPaknetModemConnectionType());

        modemConnectionType.connect(getProperProperties());

        verify(modemComponent, times(1)).initializeAfterConnect(comChannel);
    }


    @Test(timeout = TEST_TIMEOUT_MILLIS, expected = ConnectionException.class)
    public void testSetModemParametersFails() throws Exception {
        PaknetModemComponent modemComponent = new PaknetModemComponent(new TypedPaknetModemProperties(getProperProperties()));
        when(this.serialComponentFactory.newPaknetModemComponent(any(PaknetModemProperties.class))).thenReturn(modemComponent);
        TestableSerialComChannel serialComChannel = getTestableComChannel();
        serialComChannel.setResponses(Arrays.asList("   ", "\r\n*\r\n", "NotValidResponse"));
        SioSerialPort sioSerialPort = mock(SioSerialPort.class);
        ComPort comPort = getProperlyMockedComPort(serialComChannel, sioSerialPort);

        SioPaknetModemConnectionType modemConnectionType = new SioPaknetModemConnectionType();

        try {
            modemConnectionType.connect(getProperProperties());
        } catch (ModemException e) {
            if (!((ModemException) e.getCause()).getMessageId().equals("CSM-COM-207")) {
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
        ComPort comPort = getProperlyMockedComPort(comChannel, sioSerialPort);

        PaknetModemComponent modemComponent = spy(new PaknetModemComponent(new TypedPaknetModemProperties(getProperProperties())));
        when(this.serialComponentFactory.newPaknetModemComponent(any(PaknetModemProperties.class))).thenReturn(modemComponent);
        SioPaknetModemConnectionType modemConnectionType = spy(new SioPaknetModemConnectionType());

        modemConnectionType.connect(getProperProperties());

        verify(modemComponent, times(1)).sendParameters(comChannel);
    }

    @Test(timeout = 10000, expected = ConnectionException.class)
    public void testRetriesExceededForInitializeParameters() throws Exception {
        TimeoutSerialComChannel comChannel = getTimeoutSerialComChannel(COMMAND_TIMEOUT_VALUE + 10);
        comChannel.setResponses(Arrays.asList(RUBBISH_FOR_FLUSH, "First_Not_CorrectAnswer", "Second_Not_CorrectAnswer", "Third_Not_CorrectAnswer"));
        SioSerialPort sioSerialPort = mock(SioSerialPort.class);
        ComPort comPort = getProperlyMockedComPort(comChannel, sioSerialPort);

        ConnectionTaskPropertyImpl delayBeforeSendProperty = new ConnectionTaskPropertyImpl(TypedPaknetModemProperties.DELAY_BEFORE_SEND);
        delayBeforeSendProperty.setValue(new TimeDuration(10, TimeDuration.TimeUnit.MILLISECONDS));
        ConnectionTaskPropertyImpl commandTimeout = new ConnectionTaskPropertyImpl(TypedPaknetModemProperties.COMMAND_TIMEOUT);
        commandTimeout.setValue(new TimeDuration(COMMAND_TIMEOUT_VALUE, TimeDuration.TimeUnit.MILLISECONDS));
        ConnectionTaskPropertyImpl commandTries = new ConnectionTaskPropertyImpl(TypedPaknetModemProperties.COMMAND_TRIES);
        commandTries.setValue(new BigDecimal(3));
        ConnectionTaskPropertyImpl dtrToggleDelay = new ConnectionTaskPropertyImpl(TypedPaknetModemProperties.DTR_TOGGLE_DELAY);
        dtrToggleDelay.setValue(new TimeDuration(DTR_TOGGLE_DELAY_VALUE, TimeDuration.TimeUnit.MILLISECONDS));
        ConnectionProperty comPortName = mock(ConnectionProperty.class);
        when(comPortName.getName()).thenReturn(SerialConnectionPropertyNames.COMPORT_NAME_PROPERTY_NAME.propertyName());
        when(comPortName.getValue()).thenReturn(this.comPortName);
        List<ConnectionProperty> properties = Arrays.asList(comPortName, delayBeforeSendProperty, commandTimeout, commandTries, dtrToggleDelay);

        PaknetModemComponent modemComponent = spy(new PaknetModemComponent(new TypedPaknetModemProperties(properties)));
        when(this.serialComponentFactory.newPaknetModemComponent(any(PaknetModemProperties.class))).thenReturn(modemComponent);
        SioPaknetModemConnectionType modemConnectionType = spy(new SioPaknetModemConnectionType());

        final int numberOfTries = 3;
        try {
            modemConnectionType.connect(properties);
        } catch (ConnectionException e) {
            if (!((ModemException) e.getCause()).getMessageId().equals("CSM-COM-215")) {
                fail("Should have gotten exception indicating that the modem could not initialize the command prompt, but was " + e.getMessage());
            }
            assertThat(((ModemException) e.getCause()).getMessageArguments()).contains(this.comPortName);
            verify(modemComponent, times(numberOfTries)).readAndVerify(any(ComChannel.class), any(String.class), any(Long.class));
            throw e;
        }
    }

    @Test(timeout = TEST_TIMEOUT_MILLIS, expected = ConnectionException.class)
    public void noDialtoneErrorTest() throws Exception {
        TestableSerialComChannel comChannel = getTestableComChannel();
        comChannel.setResponses(Arrays.asList(RUBBISH_FOR_FLUSH, "\r\n*\r\n", "\r\n*\r\n", "NO CONNECTION PROMPT"));
        SioSerialPort sioSerialPort = mock(SioSerialPort.class);
        ComPort comPort = getProperlyMockedComPort(comChannel, sioSerialPort);

        PaknetModemComponent modemComponent = spy(new PaknetModemComponent(new TypedPaknetModemProperties(getProperProperties())));
        when(this.serialComponentFactory.newPaknetModemComponent(any(PaknetModemProperties.class))).thenReturn(modemComponent);
        SioPaknetModemConnectionType modemConnectionType = spy(new SioPaknetModemConnectionType());

        try {
            modemConnectionType.connect(getProperProperties());
        } catch (ModemException e) {
            if (!((ModemException) e.getCause()).getMessageId().equals("CSM-COM-213")) {
                fail("Should have gotten exception indicating that a timeout occurred during the dial, but was " + e.getMessage());
            }
            assertThat(((ModemException) e.getCause()).getMessageArguments()).contains(comPortName);
            verify(modemComponent, times(1)).disconnectModemBeforeNewSession(comChannel);
            verify(modemComponent, times(1)).initializeCommandState(comChannel);
            verify(modemComponent, times(1)).sendParameters(comChannel);
            verify(modemComponent, times(1)).dialModem(comChannel);
            verify(modemComponent, never()).initializeAfterConnect(comChannel);
            throw e;
        }
    }

    @Test(timeout = TEST_TIMEOUT_MILLIS)
    public void verifyConnectSuccess() throws Exception {
        TestableSerialComChannel comChannel = getTestableComChannel();
        comChannel.setResponses(OK_LIST);
        SioSerialPort sioSerialPort = mock(SioSerialPort.class);
        ComPort comPort = getProperlyMockedComPort(comChannel, sioSerialPort);

        PaknetModemComponent modemComponent = spy(new PaknetModemComponent(new TypedPaknetModemProperties(getProperProperties())));
        when(this.serialComponentFactory.newPaknetModemComponent(any(PaknetModemProperties.class))).thenReturn(modemComponent);
        SioPaknetModemConnectionType modemConnectionType = spy(new SioPaknetModemConnectionType());

        modemConnectionType.connect(getProperProperties());

        verify(modemComponent, times(1)).dialModem(comChannel);
        verify(modemComponent, times(1)).initializeAfterConnect(comChannel);
    }

}