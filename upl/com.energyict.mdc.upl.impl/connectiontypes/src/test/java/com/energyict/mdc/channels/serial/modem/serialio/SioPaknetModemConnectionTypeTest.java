package com.energyict.mdc.channels.serial.modem.serialio;

import com.energyict.cbo.TimeDuration;
import com.energyict.mdc.ManagerFactory;
import com.energyict.mdc.SerialComponentFactory;
import com.energyict.mdc.ServerManager;
import com.energyict.mdc.channels.serial.SerialPortConfiguration;
import com.energyict.mdc.channels.serial.ServerSerialPort;
import com.energyict.mdc.channels.serial.SignalController;
import com.energyict.mdc.channels.serial.direct.serialio.SioSerialPort;
import com.energyict.mdc.channels.serial.modem.AbstractModemTests;
import com.energyict.mdc.channels.serial.modem.AbstractPaknetModemProperties;
import com.energyict.mdc.channels.serial.modem.PaknetModemComponent;
import com.energyict.mdc.channels.serial.modem.TypedPaknetModemProperties;
import com.energyict.mdc.exceptions.ModemException;
import com.energyict.mdc.ports.ComPort;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.ConnectionException;
import com.energyict.mdc.tasks.ConnectionTaskProperty;
import com.energyict.mdc.tasks.ConnectionTaskPropertyImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

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

    @Mock
    private ServerManager manager;
    @Mock
    private SerialComponentFactory serialComponentFactory;

    @Before
    public void initializeMocksAndFactories() {
        when(this.manager.getSerialComponentFactory()).thenReturn(this.serialComponentFactory);
        ManagerFactory.setCurrent(this.manager);
    }

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

    private List<ConnectionTaskProperty> getProperProperties() {
        ConnectionTaskPropertyImpl delayBeforeSendProperty = new ConnectionTaskPropertyImpl(TypedPaknetModemProperties.DELAY_BEFORE_SEND);
        delayBeforeSendProperty.setValue(new TimeDuration(10, TimeDuration.MILLISECONDS));
        ConnectionTaskPropertyImpl atCommandTimeout = new ConnectionTaskPropertyImpl(TypedPaknetModemProperties.COMMAND_TIMEOUT);
        atCommandTimeout.setValue(new TimeDuration(COMMAND_TIMEOUT_VALUE, TimeDuration.MILLISECONDS));
        ConnectionTaskPropertyImpl atCommandTries = new ConnectionTaskPropertyImpl(TypedPaknetModemProperties.COMMAND_TRIES);
        atCommandTries.setValue(new BigDecimal(1));
        ConnectionTaskPropertyImpl atModemInitStrings = new ConnectionTaskPropertyImpl(TypedPaknetModemProperties.MODEM_INIT_STRINGS);
        atModemInitStrings.setValue("1:0,2:0,3:0,4:10,5:0,6:5");
        ConnectionTaskPropertyImpl delayAfterConnect = new ConnectionTaskPropertyImpl(TypedPaknetModemProperties.DELAY_AFTER_CONNECT);
        delayAfterConnect.setValue(new TimeDuration(10, TimeDuration.MILLISECONDS));
        ConnectionTaskPropertyImpl connectTimeOut = new ConnectionTaskPropertyImpl(TypedPaknetModemProperties.CONNECT_TIMEOUT);
        connectTimeOut.setValue(new TimeDuration(COMMAND_TIMEOUT_VALUE, TimeDuration.MILLISECONDS));
        ConnectionTaskPropertyImpl dialPrefix = new ConnectionTaskPropertyImpl(TypedPaknetModemProperties.MODEM_DIAL_PREFIX);
        dialPrefix.setValue("");
        ConnectionTaskPropertyImpl dtrToggleDelay = new ConnectionTaskPropertyImpl(TypedPaknetModemProperties.DTR_TOGGLE_DELAY);
        dtrToggleDelay.setValue(new TimeDuration(DTR_TOGGLE_DELAY_VALUE, TimeDuration.MILLISECONDS));
        ConnectionTaskPropertyImpl phoneNumber = new ConnectionTaskPropertyImpl(TypedPaknetModemProperties.PHONE_NUMBER_PROPERTY_NAME);
        phoneNumber.setValue(PHONE_NUMBER);

        return Arrays.<ConnectionTaskProperty>asList(delayBeforeSendProperty, atCommandTimeout, atCommandTries, atModemInitStrings,
                delayAfterConnect, connectTimeOut, dialPrefix, dtrToggleDelay, phoneNumber);
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
        when(this.serialComponentFactory.newPaknetModemComponent(any(AbstractPaknetModemProperties.class))).thenReturn(modemComponent);
        SioPaknetModemConnectionType modemConnectionType = spy(new SioPaknetModemConnectionType());

        modemConnectionType.connect(comPort, getProperProperties());

        verify(modemComponent, times(1)).disconnectModemBeforeNewSession(comChannel);
    }

    @Test(timeout = TEST_TIMEOUT_MILLIS, expected = ConnectionException.class)
    public void testInitializeCommandStateFails() throws Exception {
        PaknetModemComponent modemComponent = new PaknetModemComponent(new TypedPaknetModemProperties(getProperProperties()));
        when(this.serialComponentFactory.newPaknetModemComponent(any(AbstractPaknetModemProperties.class))).thenReturn(modemComponent);
        TestableSerialComChannel serialComChannel = getTestableComChannel();
        serialComChannel.setResponses(Arrays.asList("   ", "NotValidResponse"));
        SioSerialPort sioSerialPort = mock(SioSerialPort.class);
        ComPort comPort = getProperlyMockedComPort(serialComChannel, sioSerialPort);

        SioPaknetModemConnectionType modemConnectionType = new SioPaknetModemConnectionType();

        try {
            modemConnectionType.connect(comPort, getProperProperties());
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
        when(this.serialComponentFactory.newPaknetModemComponent(any(AbstractPaknetModemProperties.class))).thenReturn(modemComponent);
        SioPaknetModemConnectionType modemConnectionType = spy(new SioPaknetModemConnectionType());

        modemConnectionType.connect(comPort, getProperProperties());

        verify(modemComponent, times(1)).initializeAfterConnect(comChannel);
    }


    @Test(timeout = TEST_TIMEOUT_MILLIS, expected = ConnectionException.class)
    public void testSetModemParametersFails() throws Exception {
        PaknetModemComponent modemComponent = new PaknetModemComponent(new TypedPaknetModemProperties(getProperProperties()));
        when(this.serialComponentFactory.newPaknetModemComponent(any(AbstractPaknetModemProperties.class))).thenReturn(modemComponent);
        TestableSerialComChannel serialComChannel = getTestableComChannel();
        serialComChannel.setResponses(Arrays.asList("   ", "\r\n*\r\n", "NotValidResponse"));
        SioSerialPort sioSerialPort = mock(SioSerialPort.class);
        ComPort comPort = getProperlyMockedComPort(serialComChannel, sioSerialPort);

        SioPaknetModemConnectionType modemConnectionType = new SioPaknetModemConnectionType();

        try {
            modemConnectionType.connect(comPort, getProperProperties());
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
        when(this.serialComponentFactory.newPaknetModemComponent(any(AbstractPaknetModemProperties.class))).thenReturn(modemComponent);
        SioPaknetModemConnectionType modemConnectionType = spy(new SioPaknetModemConnectionType());

        modemConnectionType.connect(comPort, getProperProperties());

        verify(modemComponent, times(1)).sendParameters(comChannel);
    }

    @Test(timeout = 10000, expected = ConnectionException.class)
    public void testRetriesExceededForInitializeParameters() throws Exception {
        TimeoutSerialComChannel comChannel = getTimeoutSerialComChannel(COMMAND_TIMEOUT_VALUE + 10);
        comChannel.setResponses(Arrays.asList(RUBBISH_FOR_FLUSH, "First_Not_CorrectAnswer", "Second_Not_CorrectAnswer", "Third_Not_CorrectAnswer"));
        SioSerialPort sioSerialPort = mock(SioSerialPort.class);
        ComPort comPort = getProperlyMockedComPort(comChannel, sioSerialPort);

        ConnectionTaskPropertyImpl delayBeforeSendProperty = new ConnectionTaskPropertyImpl(TypedPaknetModemProperties.DELAY_BEFORE_SEND);
        delayBeforeSendProperty.setValue(new TimeDuration(10, TimeDuration.MILLISECONDS));
        ConnectionTaskPropertyImpl commandTimeout = new ConnectionTaskPropertyImpl(TypedPaknetModemProperties.COMMAND_TIMEOUT);
        commandTimeout.setValue(new TimeDuration(COMMAND_TIMEOUT_VALUE, TimeDuration.MILLISECONDS));
        ConnectionTaskPropertyImpl commandTries = new ConnectionTaskPropertyImpl(TypedPaknetModemProperties.COMMAND_TRIES);
        commandTries.setValue(new BigDecimal(3));
        ConnectionTaskPropertyImpl dtrToggleDelay = new ConnectionTaskPropertyImpl(TypedPaknetModemProperties.DTR_TOGGLE_DELAY);
        dtrToggleDelay.setValue(new TimeDuration(DTR_TOGGLE_DELAY_VALUE, TimeDuration.MILLISECONDS));
        List<ConnectionTaskProperty> properties = Arrays.<ConnectionTaskProperty>asList(delayBeforeSendProperty, commandTimeout, commandTries, dtrToggleDelay);

        PaknetModemComponent modemComponent = spy(new PaknetModemComponent(new TypedPaknetModemProperties(properties)));
        when(this.serialComponentFactory.newPaknetModemComponent(any(AbstractPaknetModemProperties.class))).thenReturn(modemComponent);
        SioPaknetModemConnectionType modemConnectionType = spy(new SioPaknetModemConnectionType());

        final int numberOfTries = 3;
        try {
            modemConnectionType.connect(comPort, properties);
        } catch (ConnectionException e) {
            if (!((ModemException) e.getCause()).getMessageId().equals("CSM-COM-215")) {
                fail("Should have gotten exception indicating that the modem could not initialize the command prompt, but was " + e.getMessage());
            }
            assertThat(((ModemException) e.getCause()).getMessageArguments()).contains(comPortName);
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
        when(this.serialComponentFactory.newPaknetModemComponent(any(AbstractPaknetModemProperties.class))).thenReturn(modemComponent);
        SioPaknetModemConnectionType modemConnectionType = spy(new SioPaknetModemConnectionType());

        try {
            modemConnectionType.connect(comPort, getProperProperties());
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
        when(this.serialComponentFactory.newPaknetModemComponent(any(AbstractPaknetModemProperties.class))).thenReturn(modemComponent);
        SioPaknetModemConnectionType modemConnectionType = spy(new SioPaknetModemConnectionType());

        modemConnectionType.connect(comPort, getProperProperties());

        verify(modemComponent, times(1)).dialModem(comChannel);
        verify(modemComponent, times(1)).initializeAfterConnect(comChannel);
    }
}
