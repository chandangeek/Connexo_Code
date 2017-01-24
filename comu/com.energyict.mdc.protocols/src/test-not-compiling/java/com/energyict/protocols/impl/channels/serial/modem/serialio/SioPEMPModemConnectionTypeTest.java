package com.energyict.protocols.impl.channels.serial.modem.serialio;

import com.energyict.mdc.io.PEMPModemConfiguration;
import com.energyict.mdc.channels.serial.ServerSerialPort;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.protocol.exceptions.ConnectionException;
import com.energyict.mdc.protocol.api.SerialConnectionPropertyNames;
import com.energyict.mdc.protocol.api.dynamic.ConnectionProperty;
import com.energyict.protocols.impl.channels.serial.modem.AbstractModemTests;
import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
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
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author sva
 * @since 29/04/13 - 16:30
 */
@RunWith(MockitoJUnitRunner.class)
public class SioPEMPModemConnectionTypeTest extends AbstractModemTests{

    private static final int TEST_TIMEOUT_MILLIS = 8000;
    private static final int DTR_TOGGLE_DELAY_VALUE = 100;
    private static final String MODEM_CONFIGURATION_KEY = PEMPModemConfiguration.WWS.getKey();

    protected final List<String> OK_LIST = Arrays.asList(RUBBISH_FOR_FLUSH, "\r\n*\r\n", "\r\nFENS\r\n", "\r\nXX COM\r\nYY\r\n");

    @Mock
    private ServerManager manager;
    @Mock
    private SerialComponentFactory serialComponentFactory;

    @Before
    public void initializeMocksAndFactories() {
        when(this.manager.getSerialComponentFactory()).thenReturn(this.serialComponentFactory);
        ManagerFactory.setCurrent(this.manager);
    }

    private AbstractModemTests.TestableSerialComChannel getTestableComChannel() {
        InputStream inputStream = mock(InputStream.class);
        OutputStream outputStream = mock(OutputStream.class);
        ServerSerialPort serialPort = mock(ServerSerialPort.class);
        SignalController signalController = mock(SignalController.class);
        when(serialPort.getInputStream()).thenReturn(inputStream);
        when(serialPort.getOutputStream()).thenReturn(outputStream);
        when(serialPort.getSerialPortSignalController()).thenReturn(signalController);
        return new AbstractModemTests.TestableSerialComChannel(serialPort);
    }

    private List<ConnectionProperty> getProperProperties (ComPort comPort) {
        ConnectionTaskPropertyImpl delayBeforeSendProperty = new ConnectionTaskPropertyImpl(TypedPEMPModemProperties.DELAY_BEFORE_SEND);
        delayBeforeSendProperty.setValue(new TimeDuration(10, TimeDuration.TimeUnit.MILLISECONDS));
        ConnectionTaskPropertyImpl atCommandTimeout = new ConnectionTaskPropertyImpl(TypedPEMPModemProperties.COMMAND_TIMEOUT);
        atCommandTimeout.setValue(new TimeDuration(COMMAND_TIMEOUT_VALUE, TimeDuration.TimeUnit.MILLISECONDS));
        ConnectionTaskPropertyImpl atCommandTries = new ConnectionTaskPropertyImpl(TypedPEMPModemProperties.COMMAND_TRIES);
        atCommandTries.setValue(new BigDecimal(1));
        ConnectionTaskPropertyImpl atModemInitStrings = new ConnectionTaskPropertyImpl(TypedPEMPModemProperties.MODEM_INIT_STRINGS);
        atModemInitStrings.setValue("");
        ConnectionTaskPropertyImpl delayAfterConnect = new ConnectionTaskPropertyImpl(TypedPEMPModemProperties.DELAY_AFTER_CONNECT);
        delayAfterConnect.setValue(new TimeDuration(10, TimeDuration.TimeUnit.MILLISECONDS));
        ConnectionTaskPropertyImpl connectTimeOut = new ConnectionTaskPropertyImpl(TypedPEMPModemProperties.CONNECT_TIMEOUT);
        connectTimeOut.setValue(new TimeDuration(COMMAND_TIMEOUT_VALUE, TimeDuration.TimeUnit.MILLISECONDS));
        ConnectionTaskPropertyImpl dialPrefix = new ConnectionTaskPropertyImpl(TypedPEMPModemProperties.MODEM_DIAL_PREFIX);
        dialPrefix.setValue("");
        ConnectionTaskPropertyImpl dtrToggleDelay = new ConnectionTaskPropertyImpl(TypedPEMPModemProperties.DTR_TOGGLE_DELAY);
        dtrToggleDelay.setValue(new TimeDuration(DTR_TOGGLE_DELAY_VALUE, TimeDuration.TimeUnit.MILLISECONDS));
        ConnectionTaskPropertyImpl phoneNumber = new ConnectionTaskPropertyImpl(TypedPEMPModemProperties.PHONE_NUMBER_PROPERTY_NAME);
        phoneNumber.setValue(PHONE_NUMBER);
        ConnectionTaskPropertyImpl modemConfigurationKey = new ConnectionTaskPropertyImpl(TypedPEMPModemProperties.MODEM_CONFIGURATION_KEY);
        modemConfigurationKey.setValue(MODEM_CONFIGURATION_KEY);
        ConnectionProperty comPortConnectionProperty = mock(ConnectionProperty.class);
        when(comPortConnectionProperty.getName()).thenReturn(SerialConnectionPropertyNames.COMPORT_NAME_PROPERTY_NAME.propertyName());
        when(comPortConnectionProperty.getValue()).thenReturn(this.comPortName);

        return Arrays.asList(
                delayBeforeSendProperty,
                atCommandTimeout,
                atCommandTries,
                atModemInitStrings,
                delayAfterConnect,
                connectTimeOut,
                dialPrefix,
                dtrToggleDelay,
                phoneNumber,
                modemConfigurationKey,
                comPortConnectionProperty);
    }

    private ComPort getProperlyMockedComPort(AbstractModemTests.TestableSerialComChannel serialComChannel, SioSerialPort sioSerialPort) throws Exception {
        ComPort comPort = mock(ComPort.class);

        when(comPort.getName()).thenReturn(comPortName);
        when(this.serialComponentFactory.newSioSerialPort(any(SerialPortConfiguration.class))).thenReturn(sioSerialPort);
        when(this.serialComponentFactory.newSerialComChannel(any(ServerSerialPort.class))).thenReturn(serialComChannel);
        return comPort;
    }

    @Test(timeout = TEST_TIMEOUT_MILLIS, expected = ConnectionException.class)
    public void testInitializePEMPCommandStateFails() throws Exception {
        AbstractModemTests.TestableSerialComChannel serialComChannel = getTestableComChannel();
        serialComChannel.setResponses(Arrays.asList("   ", "\r\n*\r\n",
                "NotValidResponse", "NotValidResponse", "NotValidResponse", "NotValidResponse", "NotValidResponse",
                "NotValidResponse", "NotValidResponse", "NotValidResponse", "NotValidResponse", "NotValidResponse",
                "NotValidResponse", "NotValidResponse", "NotValidResponse", "NotValidResponse", "NotValidResponse",
                "NotValidResponse", "NotValidResponse", "NotValidResponse", "NotValidResponse", "NotValidResponse"));
        SioSerialPort sioSerialPort = mock(SioSerialPort.class);
        ComPort comPort = getProperlyMockedComPort(serialComChannel, sioSerialPort);
        PEMPModemComponent modemComponent = new PEMPModemComponent(new TypedPEMPModemProperties(getProperProperties(comPort)));
        when(this.serialComponentFactory.newPEMPModemComponent(any(AbstractPEMPModemProperties.class))).thenReturn(modemComponent);

        SioPEMPModemConnectionType modemConnectionType = new SioPEMPModemConnectionType();

        try {
            modemConnectionType.connect(getProperProperties(comPort));
        } catch (ConnectionException e) {
            if (!((ModemException) e.getCause()).getMessageId().equals("CSM-COM-215")) {
                fail("Should have gotten exception indicating that the modem could not initialize the command prompt, but was " + e.getMessage());
            }
            assertThat(((ModemException) e.getCause()).getMessageArguments()).contains(comPortName);
            throw e;
        }
    }

    @Test(timeout = TEST_TIMEOUT_MILLIS, expected = ConnectionException.class)
    public void testInitializePEMPCommandStateFailsWithTimeout() throws Exception {
        AbstractModemTests.TestableSerialComChannel serialComChannel = getTestableComChannel();
        serialComChannel.setResponses(Arrays.asList("   ", "\r\n*\r\n"));
        SioSerialPort sioSerialPort = mock(SioSerialPort.class);
        ComPort comPort = getProperlyMockedComPort(serialComChannel, sioSerialPort);
        PEMPModemComponent modemComponent = new PEMPModemComponent(new TypedPEMPModemProperties(getProperProperties(comPort)));
        when(this.serialComponentFactory.newPEMPModemComponent(any(AbstractPEMPModemProperties.class))).thenReturn(modemComponent);

        SioPEMPModemConnectionType modemConnectionType = new SioPEMPModemConnectionType();

        try {
            modemConnectionType.connect(getProperProperties(comPort));
        } catch (ModemException e) {
            if (!((ModemException) e.getCause()).getMessageId().equals("CSM-COM-205")) {
                fail("Should have gotten exception indicating a timeout, but was " + e.getMessage());
            }
            assertThat(((ModemException) e.getCause()).getMessageArguments()).contains(comPortName);
            throw e;
        }
    }

    @Test(timeout = TEST_TIMEOUT_MILLIS)
    public void testInitializePEMPCommandStateSucceeds() throws Exception {
        AbstractModemTests.TestableSerialComChannel comChannel = getTestableComChannel();
        comChannel.setResponses(OK_LIST);
        SioSerialPort sioSerialPort = mock(SioSerialPort.class);
        ComPort comPort = getProperlyMockedComPort(comChannel, sioSerialPort);

        PEMPModemComponent modemComponent = spy(new PEMPModemComponent(new TypedPEMPModemProperties(getProperProperties(comPort))));
        when(this.serialComponentFactory.newPEMPModemComponent(any(AbstractPEMPModemProperties.class))).thenReturn(modemComponent);
        SioPEMPModemConnectionType modemConnectionType = spy(new SioPEMPModemConnectionType());

        modemConnectionType.connect(getProperProperties(comPort));

        verify(modemComponent, times(1)).initializeAfterConnect(comChannel);
    }

    @Test(timeout = TEST_TIMEOUT_MILLIS)
    public void verifyConnectSuccess() throws Exception {
        AbstractModemTests.TestableSerialComChannel comChannel = getTestableComChannel();
        comChannel.setResponses(OK_LIST);
        SioSerialPort sioSerialPort = mock(SioSerialPort.class);
        ComPort comPort = getProperlyMockedComPort(comChannel, sioSerialPort);

        PEMPModemComponent modemComponent = spy(new PEMPModemComponent(new TypedPEMPModemProperties(getProperProperties(comPort))));
        when(this.serialComponentFactory.newPEMPModemComponent(any(AbstractPEMPModemProperties.class))).thenReturn(modemComponent);
        SioPEMPModemConnectionType modemConnectionType = spy(new SioPEMPModemConnectionType());

        modemConnectionType.connect(getProperProperties(comPort));

        verify(modemComponent, times(1)).dialModem(comChannel);
        verify(modemComponent, times(1)).initializeAfterConnect(comChannel);
    }

    @Test(timeout = TEST_TIMEOUT_MILLIS)
    public void verifyConnectSuccessAfterRetry() throws Exception {
        AbstractModemTests.TestableSerialComChannel comChannel = getTestableComChannel();
        comChannel.setResponses(Arrays.asList(RUBBISH_FOR_FLUSH, "\r\n*\r\n", "\r\nFENS\r\n", "RUBBISH", "\r\nXX COM\r\nYY\r\n"));
        SioSerialPort sioSerialPort = mock(SioSerialPort.class);
        ComPort comPort = getProperlyMockedComPort(comChannel, sioSerialPort);

        PEMPModemComponent modemComponent = spy(new PEMPModemComponent(new TypedPEMPModemProperties(getProperProperties(comPort))));
        when(this.serialComponentFactory.newPEMPModemComponent(any(AbstractPEMPModemProperties.class))).thenReturn(modemComponent);
        SioPEMPModemConnectionType modemConnectionType = spy(new SioPEMPModemConnectionType());

        modemConnectionType.connect(getProperProperties(comPort));

        verify(modemComponent, times(1)).dialModem(comChannel);
        verify(modemComponent, times(1)).initializeAfterConnect(comChannel);
    }
}