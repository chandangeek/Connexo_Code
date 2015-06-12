package com.energyict.mdc.channels.serial.modem.serialio;

import com.energyict.cbo.TimeDuration;
import com.energyict.dialer.coreimpl.PEMPModemConfiguration;
import com.energyict.mdc.ManagerFactory;
import com.energyict.mdc.SerialComponentFactory;
import com.energyict.mdc.ServerManager;
import com.energyict.mdc.channels.serial.SerialPortConfiguration;
import com.energyict.mdc.channels.serial.ServerSerialPort;
import com.energyict.mdc.channels.serial.SignalController;
import com.energyict.mdc.channels.serial.direct.serialio.SioSerialPort;
import com.energyict.mdc.channels.serial.modem.AbstractModemTests;
import com.energyict.mdc.channels.serial.modem.AbstractPEMPModemProperties;
import com.energyict.mdc.channels.serial.modem.PEMPModemComponent;
import com.energyict.mdc.channels.serial.modem.TypedPEMPModemProperties;
import com.energyict.mdc.exceptions.ModemException;
import com.energyict.mdc.ports.ComPort;
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
 * @author sva
 * @since 29/04/13 - 16:30
 */
@RunWith(MockitoJUnitRunner.class)
public class SioPEMPModemConnectionTypeTest extends AbstractModemTests{

    private static final int DTR_TOGGLE_DELAY_VALUE = 100;
    private static final String MODEM_CONFIGURATION_KEY = PEMPModemConfiguration.WWS.getKey();

    protected final List<String> OK_LIST = Arrays.asList(
            RUBBISH_FOR_FLUSH,
            "\r\n*\r\n",
            "\r\nFENS\r\n", "\r\nFENS\r\n", "\r\nFENS\r\n", "\r\nFENS\r\n",
            "\r\nXX COM\r\nYY\r\n"
    );

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

    private List<ConnectionTaskProperty> getProperProperties() {
        ConnectionTaskPropertyImpl delayBeforeSendProperty = new ConnectionTaskPropertyImpl(TypedPEMPModemProperties.DELAY_BEFORE_SEND);
        delayBeforeSendProperty.setValue(new TimeDuration(10, TimeDuration.MILLISECONDS));
        ConnectionTaskPropertyImpl atCommandTimeout = new ConnectionTaskPropertyImpl(TypedPEMPModemProperties.COMMAND_TIMEOUT);
        atCommandTimeout.setValue(new TimeDuration(COMMAND_TIMEOUT_VALUE, TimeDuration.MILLISECONDS));
        ConnectionTaskPropertyImpl atCommandTries = new ConnectionTaskPropertyImpl(TypedPEMPModemProperties.COMMAND_TRIES);
        atCommandTries.setValue(new BigDecimal(1));
        ConnectionTaskPropertyImpl atModemInitStrings = new ConnectionTaskPropertyImpl(TypedPEMPModemProperties.MODEM_INIT_STRINGS);
        atModemInitStrings.setValue("");
        ConnectionTaskPropertyImpl delayAfterConnect = new ConnectionTaskPropertyImpl(TypedPEMPModemProperties.DELAY_AFTER_CONNECT);
        delayAfterConnect.setValue(new TimeDuration(10, TimeDuration.MILLISECONDS));
        ConnectionTaskPropertyImpl connectTimeOut = new ConnectionTaskPropertyImpl(TypedPEMPModemProperties.CONNECT_TIMEOUT);
        connectTimeOut.setValue(new TimeDuration(COMMAND_TIMEOUT_VALUE, TimeDuration.MILLISECONDS));
        ConnectionTaskPropertyImpl dialPrefix = new ConnectionTaskPropertyImpl(TypedPEMPModemProperties.MODEM_DIAL_PREFIX);
        dialPrefix.setValue("");
        ConnectionTaskPropertyImpl dtrToggleDelay = new ConnectionTaskPropertyImpl(TypedPEMPModemProperties.DTR_TOGGLE_DELAY);
        dtrToggleDelay.setValue(new TimeDuration(DTR_TOGGLE_DELAY_VALUE, TimeDuration.MILLISECONDS));
        ConnectionTaskPropertyImpl phoneNumber = new ConnectionTaskPropertyImpl(TypedPEMPModemProperties.PHONE_NUMBER_PROPERTY_NAME);
        phoneNumber.setValue(PHONE_NUMBER); 
        ConnectionTaskPropertyImpl modemConfigurationKey = new ConnectionTaskPropertyImpl(TypedPEMPModemProperties.MODEM_CONFIGURATION_KEY);
        modemConfigurationKey.setValue(MODEM_CONFIGURATION_KEY);

        return Arrays.<ConnectionTaskProperty>asList(delayBeforeSendProperty, atCommandTimeout, atCommandTries, atModemInitStrings,
                delayAfterConnect, connectTimeOut, dialPrefix, dtrToggleDelay, phoneNumber, modemConfigurationKey);
    }

    private ComPort getProperlyMockedComPort(AbstractModemTests.TestableSerialComChannel serialComChannel, SioSerialPort sioSerialPort) throws Exception {
        ComPort comPort = mock(ComPort.class);

        when(comPort.getName()).thenReturn(comPortName);
        when(this.serialComponentFactory.newSioSerialPort(any(SerialPortConfiguration.class))).thenReturn(sioSerialPort);
        when(this.serialComponentFactory.newSerialComChannel(any(ServerSerialPort.class))).thenReturn(serialComChannel);
        return comPort;
    }

    @Test(timeout = TEST_LONG_TIMEOUT_MILLIS, expected = ConnectionException.class)
    public void testInitializePEMPCommandStateFails() throws Exception {
        PEMPModemComponent modemComponent = new PEMPModemComponent(new TypedPEMPModemProperties(getProperProperties()));
        when(this.serialComponentFactory.newPEMPModemComponent(any(AbstractPEMPModemProperties.class))).thenReturn(modemComponent);
        AbstractModemTests.TestableSerialComChannel serialComChannel = getTestableComChannel();
        serialComChannel.setResponses(Arrays.asList("   ", "\r\n*\r\n",
                "NotValidResponse", "NotValidResponse", "NotValidResponse", "NotValidResponse", "NotValidResponse",
                "NotValidResponse", "NotValidResponse", "NotValidResponse", "NotValidResponse", "NotValidResponse",
                "NotValidResponse", "NotValidResponse", "NotValidResponse", "NotValidResponse", "NotValidResponse",
                "NotValidResponse", "NotValidResponse", "NotValidResponse", "NotValidResponse", "NotValidResponse"));
        SioSerialPort sioSerialPort = mock(SioSerialPort.class);
        ComPort comPort = getProperlyMockedComPort(serialComChannel, sioSerialPort);

        SioPEMPModemConnectionType modemConnectionType = new SioPEMPModemConnectionType();

        try {
            modemConnectionType.connect(comPort, getProperProperties());
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
        PEMPModemComponent modemComponent = new PEMPModemComponent(new TypedPEMPModemProperties(getProperProperties()));
        when(this.serialComponentFactory.newPEMPModemComponent(any(AbstractPEMPModemProperties.class))).thenReturn(modemComponent);
        AbstractModemTests.TestableSerialComChannel serialComChannel = getTestableComChannel();
        serialComChannel.setResponses(Arrays.asList("   ", "\r\n*\r\n"));
        SioSerialPort sioSerialPort = mock(SioSerialPort.class);
        ComPort comPort = getProperlyMockedComPort(serialComChannel, sioSerialPort);

        SioPEMPModemConnectionType modemConnectionType = new SioPEMPModemConnectionType();

        try {
            modemConnectionType.connect(comPort, getProperProperties());
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

        PEMPModemComponent modemComponent = spy(new PEMPModemComponent(new TypedPEMPModemProperties(getProperProperties())));
        when(this.serialComponentFactory.newPEMPModemComponent(any(AbstractPEMPModemProperties.class))).thenReturn(modemComponent);
        SioPEMPModemConnectionType modemConnectionType = spy(new SioPEMPModemConnectionType());

        modemConnectionType.connect(comPort, getProperProperties());

        verify(modemComponent, times(1)).initializeAfterConnect(comChannel);
    }

    @Test(timeout = TEST_TIMEOUT_MILLIS)
    public void verifyConnectSuccess() throws Exception {
        AbstractModemTests.TestableSerialComChannel comChannel = getTestableComChannel();
        comChannel.setResponses(OK_LIST);
        SioSerialPort sioSerialPort = mock(SioSerialPort.class);
        ComPort comPort = getProperlyMockedComPort(comChannel, sioSerialPort);

        PEMPModemComponent modemComponent = spy(new PEMPModemComponent(new TypedPEMPModemProperties(getProperProperties())));
        when(this.serialComponentFactory.newPEMPModemComponent(any(AbstractPEMPModemProperties.class))).thenReturn(modemComponent);
        SioPEMPModemConnectionType modemConnectionType = spy(new SioPEMPModemConnectionType());

        modemConnectionType.connect(comPort, getProperProperties());

        verify(modemComponent, times(1)).dialModem(comChannel);
        verify(modemComponent, times(1)).initializeAfterConnect(comChannel);
    }

    @Test(timeout = TEST_TIMEOUT_MILLIS)
    public void verifyConnectSuccessAfterRetry() throws Exception {
        AbstractModemTests.TestableSerialComChannel comChannel = getTestableComChannel();
        comChannel.setResponses(Arrays.asList(RUBBISH_FOR_FLUSH, "\r\n*\r\n", "\r\nFENS\r\n", "\r\nFENS\r\n", "\r\nFENS\r\n", "\r\nFENS\r\n", "RUBBISH", "\r\nXX COM\r\nYY\r\n"));
        SioSerialPort sioSerialPort = mock(SioSerialPort.class);
        ComPort comPort = getProperlyMockedComPort(comChannel, sioSerialPort);

        PEMPModemComponent modemComponent = spy(new PEMPModemComponent(new TypedPEMPModemProperties(getProperProperties())));
        when(this.serialComponentFactory.newPEMPModemComponent(any(AbstractPEMPModemProperties.class))).thenReturn(modemComponent);
        SioPEMPModemConnectionType modemConnectionType = spy(new SioPEMPModemConnectionType());

        modemConnectionType.connect(comPort, getProperProperties());

        verify(modemComponent, times(1)).dialModem(comChannel);
        verify(modemComponent, times(1)).initializeAfterConnect(comChannel);
    }
}