package com.energyict.mdc.channels.serial.modem.rxtx;

import com.energyict.cbo.TimeDuration;
import com.energyict.mdc.ManagerFactory;
import com.energyict.mdc.SerialComponentFactory;
import com.energyict.mdc.ServerManager;
import com.energyict.mdc.channels.serial.SerialPortConfiguration;
import com.energyict.mdc.channels.serial.ServerSerialPort;
import com.energyict.mdc.channels.serial.direct.rxtx.RxTxSerialPort;
import com.energyict.mdc.channels.serial.modem.AbstractAtModemProperties;
import com.energyict.mdc.channels.serial.modem.AbstractModemTests;
import com.energyict.mdc.channels.serial.modem.AtModemComponent;
import com.energyict.mdc.channels.serial.modem.TypedAtModemProperties;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests for the {@link RxTxAtModemConnectionType} component
 *
 * Copyrights EnergyICT
 * Date: 22/11/12
 * Time: 12:18
 */
@RunWith(MockitoJUnitRunner.class)
public class RxTxAtModemConnectionTypeTest extends AbstractModemTests{

    @Mock
    private ServerManager manager;
    @Mock
    private SerialComponentFactory serialComponentFactory;

    @Before
    public void initializeMocksAndFactories () {
        when(this.manager.getSerialComponentFactory()).thenReturn(this.serialComponentFactory);
        ManagerFactory.setCurrent(this.manager);
    }

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

    private List<ConnectionTaskProperty> getProperProperties() {
        ConnectionTaskPropertyImpl delayBeforeSendProperty = new ConnectionTaskPropertyImpl(TypedAtModemProperties.DELAY_BEFORE_SEND);
        delayBeforeSendProperty.setValue(new TimeDuration(10, TimeDuration.MILLISECONDS));
        ConnectionTaskPropertyImpl atCommandTimeout = new ConnectionTaskPropertyImpl(TypedAtModemProperties.AT_COMMAND_TIMEOUT);
        atCommandTimeout.setValue(new TimeDuration(COMMAND_TIMEOUT_VALUE, TimeDuration.MILLISECONDS));
        ConnectionTaskPropertyImpl atCommandTries = new ConnectionTaskPropertyImpl(TypedAtModemProperties.AT_COMMAND_TRIES);
        atCommandTries.setValue(new BigDecimal(1));
        ConnectionTaskPropertyImpl atModemInitStrings = new ConnectionTaskPropertyImpl(TypedAtModemProperties.AT_MODEM_GLOBAL_INIT_STRINGS);
        atModemInitStrings.setValue("ATS0=0E0V1");
        ConnectionTaskPropertyImpl delayAfterConnect = new ConnectionTaskPropertyImpl(TypedAtModemProperties.DELAY_AFTER_CONNECT);
        delayAfterConnect.setValue(new TimeDuration(10, TimeDuration.MILLISECONDS));
        ConnectionTaskPropertyImpl connectTimeOut = new ConnectionTaskPropertyImpl(TypedAtModemProperties.AT_CONNECT_TIMEOUT);
        connectTimeOut.setValue(new TimeDuration(COMMAND_TIMEOUT_VALUE, TimeDuration.MILLISECONDS));
        ConnectionTaskPropertyImpl dialPrefix = new ConnectionTaskPropertyImpl(TypedAtModemProperties.AT_MODEM_DIAL_PREFIX);
        dialPrefix.setValue("");
        ConnectionTaskPropertyImpl addressSelector = new ConnectionTaskPropertyImpl(TypedAtModemProperties.AT_MODEM_ADDRESS_SELECTOR);
        addressSelector.setValue("");
        ConnectionTaskPropertyImpl postDialCommands = new ConnectionTaskPropertyImpl(TypedAtModemProperties.AT_MODEM_POST_DIAL_COMMANDS);
        postDialCommands.setValue("");
        ConnectionTaskPropertyImpl phoneNumber = new ConnectionTaskPropertyImpl(TypedAtModemProperties.PHONE_NUMBER_PROPERTY_NAME);
        phoneNumber.setValue(PHONE_NUMBER);

        return Arrays.<ConnectionTaskProperty>asList(delayBeforeSendProperty, atCommandTimeout, atCommandTries, atModemInitStrings,
                delayAfterConnect, connectTimeOut, dialPrefix, addressSelector, postDialCommands, phoneNumber);
    }

    private ComPort getProperlyMockedComPort(AbstractModemTests.TestableSerialComChannel serialComChannel, RxTxSerialPort rxTxSerialPort) throws Exception {
        ComPort comPort = mock(ComPort.class);
        when(comPort.getName()).thenReturn(comPortName);
        when(this.serialComponentFactory.newRxTxSerialPort(any(SerialPortConfiguration.class))).thenReturn(rxTxSerialPort);
        when(this.serialComponentFactory.newSerialComChannel(any(ServerSerialPort.class))).thenReturn(serialComChannel);
        return comPort;
    }

    @Test(timeout = TEST_TIMEOUT_MILLIS, expected = ConnectionException.class)
    public void readTimeOutExceptionTest() throws Exception {
        AbstractModemTests.TestableSerialComChannel serialComChannel = getTestableComChannel();
        RxTxSerialPort rxTxSerialPort = mock(RxTxSerialPort.class);
        ComPort comPort = getProperlyMockedComPort(serialComChannel, rxTxSerialPort);

        AtModemComponent atModemComponent = new AtModemComponent(new TypedAtModemProperties(getProperProperties()));
        when(this.serialComponentFactory.newAtModemComponent(any(AbstractAtModemProperties.class))).thenReturn(atModemComponent);

        RxTxAtModemConnectionType atModemConnectionType = new RxTxAtModemConnectionType();
        try {
            atModemConnectionType.connect(comPort, getProperProperties());
        } catch (ConnectionException e) {
            if (!((ModemException) e.getCause()).getMessageId().equals("CSM-COM-204")) {
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
        ComPort comPort = getProperlyMockedComPort(serialComChannel, rxTxSerialPort);

        AtModemComponent atModemComponent = new AtModemComponent(new TypedAtModemProperties(getProperProperties()));
        when(this.serialComponentFactory.newAtModemComponent(any(AbstractAtModemProperties.class))).thenReturn(atModemComponent);

        RxTxAtModemConnectionType atModemConnectionType = new RxTxAtModemConnectionType();

        try {
            atModemConnectionType.connect(comPort, getProperProperties());
        } catch (ConnectionException e) {
            if (!((ModemException) e.getCause()).getMessageId().equals("CSM-COM-204")) {
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
        ComPort comPort = getProperlyMockedComPort(comChannel, rxTxSerialPort);

        AtModemComponent atModemComponent = spy(new AtModemComponent(new TypedAtModemProperties(getProperProperties())));
        when(this.serialComponentFactory.newAtModemComponent(any(AbstractAtModemProperties.class))).thenReturn(atModemComponent);
        RxTxAtModemConnectionType atModemConnectionType = spy(new RxTxAtModemConnectionType());

        atModemConnectionType.connect(comPort, getProperProperties());

        verify(atModemComponent, times(1)).hangUpComChannel(comChannel);
    }

    @Test(timeout = TEST_TIMEOUT_MILLIS)
    public void testRestoreDefaultProfileSucceeds() throws Exception {
        AbstractModemTests.TestableSerialComChannel comChannel = getTestableComChannel();
        comChannel.setResponses(OK_LIST);
        RxTxSerialPort rxTxSerialPort = mock(RxTxSerialPort.class);
        ComPort comPort = getProperlyMockedComPort(comChannel, rxTxSerialPort);

        AtModemComponent atModemComponent = spy(new AtModemComponent(new TypedAtModemProperties(getProperProperties())));
        when(this.serialComponentFactory.newAtModemComponent(any(AbstractAtModemProperties.class))).thenReturn(atModemComponent);
        RxTxAtModemConnectionType atModemConnectionType = spy(new RxTxAtModemConnectionType());

        atModemConnectionType.connect(comPort, getProperProperties());

        verify(atModemComponent, times(1)).reStoreProfile(comChannel);
    }

    @Test(timeout = TEST_TIMEOUT_MILLIS, expected = ConnectionException.class)
    public void testRestoreDefaultProfileFails() throws Exception {
        AbstractModemTests.TestableSerialComChannel comChannel = getTestableComChannel();
        comChannel.setResponses(Arrays.asList(RUBBISH_FOR_FLUSH, "OK", "NokToDefaultProfileRestore"));
        RxTxSerialPort rxTxSerialPort = mock(RxTxSerialPort.class);
        ComPort comPort = getProperlyMockedComPort(comChannel, rxTxSerialPort);

        AtModemComponent atModemComponent = new AtModemComponent(new TypedAtModemProperties(getProperProperties()));
        when(this.serialComponentFactory.newAtModemComponent(any(AbstractAtModemProperties.class))).thenReturn(atModemComponent);

        RxTxAtModemConnectionType atModemConnectionType = spy(new RxTxAtModemConnectionType());

        try {
            atModemConnectionType.connect(comPort, getProperProperties());
        } catch (ConnectionException e) {
            if (!((ModemException) e.getCause()).getMessageId().equals("CSM-COM-206")) {
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
        ComPort comPort = getProperlyMockedComPort(comChannel, rxTxSerialPort);

        ConnectionTaskPropertyImpl delayBeforeSendProperty = new ConnectionTaskPropertyImpl(TypedAtModemProperties.DELAY_BEFORE_SEND);
        delayBeforeSendProperty.setValue(new TimeDuration(10, TimeDuration.MILLISECONDS));
        ConnectionTaskPropertyImpl atCommandTimeout = new ConnectionTaskPropertyImpl(TypedAtModemProperties.AT_COMMAND_TIMEOUT);
        atCommandTimeout.setValue(new TimeDuration(COMMAND_TIMEOUT_VALUE, TimeDuration.MILLISECONDS));
        ConnectionTaskPropertyImpl atCommandTries = new ConnectionTaskPropertyImpl(TypedAtModemProperties.AT_COMMAND_TRIES);
        atCommandTries.setValue(new BigDecimal(3));
        List<ConnectionTaskProperty> properties = Arrays.<ConnectionTaskProperty>asList(delayBeforeSendProperty, atCommandTimeout, atCommandTries);

        AtModemComponent atModemComponent = spy(new AtModemComponent(new TypedAtModemProperties(properties)));
        when(this.serialComponentFactory.newAtModemComponent(any(AbstractAtModemProperties.class))).thenReturn(atModemComponent);
        RxTxAtModemConnectionType atModemConnectionType = spy(new RxTxAtModemConnectionType());

        final int numberOfTries = 3;
        try {
            atModemConnectionType.connect(comPort, properties);
        } catch (ConnectionException e) {
            if (!((ModemException) e.getCause()).getMessageId().equals("CSM-COM-204")) {
                fail("Should have gotten exception indicating that the modem hangup failed, but was " + e.getMessage());
            }
            assertThat(((ModemException) e.getCause()).getMessageArguments()).contains(comPortName);
            verify(atModemComponent, times(numberOfTries)).readAndVerify(any(ComChannel.class), any(String.class), any(Long.class));
            throw e;
        }
    }

    @Test(timeout = TEST_TIMEOUT_MILLIS)
    public void writeSuccessfulInitStringsTest() throws Exception {
        AbstractModemTests.TestableSerialComChannel comChannel = getTestableComChannel();
        comChannel.setResponses(OK_LIST);
        RxTxSerialPort rxTxSerialPort = mock(RxTxSerialPort.class);
        ComPort comPort = getProperlyMockedComPort(comChannel, rxTxSerialPort);

        AtModemComponent atModemComponent = spy(new AtModemComponent(new TypedAtModemProperties(getProperProperties())));
        when(this.serialComponentFactory.newAtModemComponent(any(AbstractAtModemProperties.class))).thenReturn(atModemComponent);
        RxTxAtModemConnectionType atModemConnectionType = spy(new RxTxAtModemConnectionType());

        atModemConnectionType.connect(comPort, getProperProperties());

        verify(atModemComponent, times(1)).sendInitStrings(comChannel);
    }

    @Test(timeout = TEST_TIMEOUT_MILLIS, expected = ConnectionException.class)
    public void writeFailingInitStringTest() throws Exception {
        AbstractModemTests.TestableSerialComChannel comChannel = getTestableComChannel();
        comChannel.setResponses(Arrays.asList(RUBBISH_FOR_FLUSH, "OK", "OK", "Not_CorrectAnswer"));
        RxTxSerialPort rxTxSerialPort = mock(RxTxSerialPort.class);
        ComPort comPort = getProperlyMockedComPort(comChannel, rxTxSerialPort);


        AtModemComponent atModemComponent = spy(new AtModemComponent(new TypedAtModemProperties(getProperProperties())));
        when(this.serialComponentFactory.newAtModemComponent(any(AbstractAtModemProperties.class))).thenReturn(atModemComponent);
        RxTxAtModemConnectionType atModemConnectionType = spy(new RxTxAtModemConnectionType());

        try {
            atModemConnectionType.connect(comPort, getProperProperties());
        } catch (ConnectionException e) {
            if (!((ModemException) e.getCause()).getMessageId().equals("CSM-COM-207")) {
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
        ComPort comPort = getProperlyMockedComPort(comChannel, rxTxSerialPort);

        List<ConnectionTaskProperty> properProperties = new ArrayList<ConnectionTaskProperty>();
        ConnectionTaskPropertyImpl delayBeforeSendProperty = new ConnectionTaskPropertyImpl(TypedAtModemProperties.DELAY_BEFORE_SEND);
        delayBeforeSendProperty.setValue(new TimeDuration(10, TimeDuration.MILLISECONDS));
        properProperties.add(delayBeforeSendProperty);
        ConnectionTaskPropertyImpl atCommandTimeout = new ConnectionTaskPropertyImpl(TypedAtModemProperties.AT_COMMAND_TIMEOUT);
        atCommandTimeout.setValue(new TimeDuration(COMMAND_TIMEOUT_VALUE, TimeDuration.MILLISECONDS));
        properProperties.add(atCommandTimeout);
        ConnectionTaskPropertyImpl atCommandTries = new ConnectionTaskPropertyImpl(TypedAtModemProperties.AT_COMMAND_TRIES);
        atCommandTries.setValue(new BigDecimal(1));
        properProperties.add(atCommandTries);
        ConnectionTaskPropertyImpl atModemInitStrings = new ConnectionTaskPropertyImpl(TypedAtModemProperties.AT_MODEM_INIT_STRINGS);
        atModemInitStrings.setValue("ATS0=0E0V1;ATM0;ATS1=0");
        properProperties.add(atModemInitStrings);
        ConnectionTaskPropertyImpl delayAfterConnect = new ConnectionTaskPropertyImpl(TypedAtModemProperties.DELAY_AFTER_CONNECT);
        delayAfterConnect.setValue(new TimeDuration(10, TimeDuration.MILLISECONDS));
        properProperties.add(delayAfterConnect);
        ConnectionTaskPropertyImpl connectTimeOut = new ConnectionTaskPropertyImpl(TypedAtModemProperties.AT_CONNECT_TIMEOUT);
        connectTimeOut.setValue(new TimeDuration(COMMAND_TIMEOUT_VALUE, TimeDuration.MILLISECONDS));
        properProperties.add(connectTimeOut);
        ConnectionTaskPropertyImpl dialPrefix = new ConnectionTaskPropertyImpl(TypedAtModemProperties.AT_MODEM_DIAL_PREFIX);
        dialPrefix.setValue("");
        properProperties.add(dialPrefix);
        ConnectionTaskPropertyImpl addressSelector = new ConnectionTaskPropertyImpl(TypedAtModemProperties.AT_MODEM_ADDRESS_SELECTOR);
        addressSelector.setValue("");
        properProperties.add(addressSelector);
        ConnectionTaskPropertyImpl postDialCommands = new ConnectionTaskPropertyImpl(TypedAtModemProperties.AT_MODEM_POST_DIAL_COMMANDS);
        postDialCommands.setValue("");
        properProperties.add(postDialCommands);
        ConnectionTaskPropertyImpl phoneNumber = new ConnectionTaskPropertyImpl(TypedAtModemProperties.PHONE_NUMBER_PROPERTY_NAME);
        phoneNumber.setValue(PHONE_NUMBER);
        properProperties.add(phoneNumber);

        AtModemComponent atModemComponent = spy(new AtModemComponent(new TypedAtModemProperties(properProperties)));
        when(this.serialComponentFactory.newAtModemComponent(any(AbstractAtModemProperties.class))).thenReturn(atModemComponent);
        RxTxAtModemConnectionType atModemConnectionType = spy(new RxTxAtModemConnectionType());


        atModemConnectionType.connect(comPort, properProperties);

        verify(atModemComponent, times(1)).sendInitStrings(comChannel);
        verify(atModemComponent, times(4)).writeSingleInitString(any(ComChannel.class), any(String.class)); // 1 global init string and 3 user defined init strings have been send out
    }

    @Test(timeout = TEST_TIMEOUT_MILLIS, expected = ConnectionException.class)
    public void busyErrorTest() throws Exception {
        AbstractModemTests.TestableSerialComChannel comChannel = getTestableComChannel();
        comChannel.setResponses(Arrays.asList(RUBBISH_FOR_FLUSH, "OK", "BUSY"));
        RxTxSerialPort rxTxSerialPort = mock(RxTxSerialPort.class);
        ComPort comPort = getProperlyMockedComPort(comChannel, rxTxSerialPort);

        AtModemComponent atModemComponent = spy(new AtModemComponent(new TypedAtModemProperties(getProperProperties())));
        when(this.serialComponentFactory.newAtModemComponent(any(AbstractAtModemProperties.class))).thenReturn(atModemComponent);
        RxTxAtModemConnectionType atModemConnectionType = spy(new RxTxAtModemConnectionType());

        try {
            atModemConnectionType.connect(comPort, getProperProperties());
        } catch (ConnectionException e) {
            if (!((ModemException) e.getCause()).getMessageId().equals("CSM-COM-208")) {
                fail("Should have gotten exception indicating that the modem received a BUSY signal, but was " + e.getMessage());
            }
            assertThat(((ModemException) e.getCause()).getMessageArguments()).contains(comPortName);
            verify(atModemComponent, times(1)).hangUpComChannel(comChannel);
            verify(atModemComponent, times(1)).reStoreProfile(comChannel);
            verify(atModemComponent, never()).sendInitStrings(comChannel);
            throw e;
        }
    }

    @Test(timeout = TEST_TIMEOUT_MILLIS, expected = ConnectionException.class)
    public void errorAnswerTest() throws Exception {
        AbstractModemTests.TestableSerialComChannel comChannel = getTestableComChannel();
        comChannel.setResponses(Arrays.asList(RUBBISH_FOR_FLUSH, "OK", "ERROR"));
        RxTxSerialPort rxTxSerialPort = mock(RxTxSerialPort.class);
        ComPort comPort = getProperlyMockedComPort(comChannel, rxTxSerialPort);

        AtModemComponent atModemComponent = spy(new AtModemComponent(new TypedAtModemProperties(getProperProperties())));
        when(this.serialComponentFactory.newAtModemComponent(any(AbstractAtModemProperties.class))).thenReturn(atModemComponent);
        RxTxAtModemConnectionType atModemConnectionType = spy(new RxTxAtModemConnectionType());

        try {
            atModemConnectionType.connect(comPort, getProperProperties());
        } catch (ConnectionException e) {
            if (!((ModemException) e.getCause()).getMessageId().equals("CSM-COM-209")) {
                fail("Should have gotten exception indicating that the modem received a ERROR signal, but was " + e.getMessage());
            }
            assertThat(((ModemException) e.getCause()).getMessageArguments()).contains(comPortName);
            verify(atModemComponent, times(1)).hangUpComChannel(comChannel);
            verify(atModemComponent, times(1)).reStoreProfile(comChannel);
            verify(atModemComponent, never()).sendInitStrings(comChannel);
            throw e;
        }
    }

    @Test(timeout = TEST_TIMEOUT_MILLIS, expected = ConnectionException.class)
    public void noAnswerErrorTest() throws Exception {
        AbstractModemTests.TestableSerialComChannel comChannel = getTestableComChannel();
        comChannel.setResponses(Arrays.asList(RUBBISH_FOR_FLUSH, "OK", "NO ANSWER"));
        RxTxSerialPort rxTxSerialPort = mock(RxTxSerialPort.class);
        ComPort comPort = getProperlyMockedComPort(comChannel, rxTxSerialPort);

        AtModemComponent atModemComponent = spy(new AtModemComponent(new TypedAtModemProperties(getProperProperties())));
        when(this.serialComponentFactory.newAtModemComponent(any(AbstractAtModemProperties.class))).thenReturn(atModemComponent);
        RxTxAtModemConnectionType atModemConnectionType = spy(new RxTxAtModemConnectionType());

        try {
            atModemConnectionType.connect(comPort, getProperProperties());
        } catch (ConnectionException e) {
            if (!((ModemException) e.getCause()).getMessageId().equals("CSM-COM-210")) {
                fail("Should have gotten exception indicating that the modem received a NO ANSWER signal, but was " + e.getMessage());
            }
            assertThat(((ModemException) e.getCause()).getMessageArguments()).contains(comPortName);
            verify(atModemComponent, times(1)).hangUpComChannel(comChannel);
            verify(atModemComponent, times(1)).reStoreProfile(comChannel);
            verify(atModemComponent, never()).sendInitStrings(comChannel);
            throw e;
        }
    }


    @Test(timeout = TEST_TIMEOUT_MILLIS, expected = ConnectionException.class)
    public void noCarrierErrorTest() throws Exception {
        AbstractModemTests.TestableSerialComChannel comChannel = getTestableComChannel();
        comChannel.setResponses(Arrays.asList(RUBBISH_FOR_FLUSH, "OK", "NO CARRIER"));
        RxTxSerialPort rxTxSerialPort = mock(RxTxSerialPort.class);
        ComPort comPort = getProperlyMockedComPort(comChannel, rxTxSerialPort);

        AtModemComponent atModemComponent = spy(new AtModemComponent(new TypedAtModemProperties(getProperProperties())));
        when(this.serialComponentFactory.newAtModemComponent(any(AbstractAtModemProperties.class))).thenReturn(atModemComponent);
        RxTxAtModemConnectionType atModemConnectionType = spy(new RxTxAtModemConnectionType());

        try {
            atModemConnectionType.connect(comPort, getProperProperties());
        } catch (ConnectionException e) {
            if (!((ModemException) e.getCause()).getMessageId().equals("CSM-COM-211")) {
                fail("Should have gotten exception indicating that the modem received a NO CARRIER signal, but was " + e.getMessage());
            }
            assertThat(((ModemException) e.getCause()).getMessageArguments()).contains(comPortName);
            verify(atModemComponent, times(1)).hangUpComChannel(comChannel);
            verify(atModemComponent, times(1)).reStoreProfile(comChannel);
            verify(atModemComponent, never()).sendInitStrings(comChannel);
            throw e;
        }
    }

    @Test(timeout = TEST_TIMEOUT_MILLIS, expected = ConnectionException.class)
    public void noDialtoneErrorTest() throws Exception {
        AbstractModemTests.TestableSerialComChannel comChannel = getTestableComChannel();
        comChannel.setResponses(Arrays.asList(RUBBISH_FOR_FLUSH, "OK", "NO DIALTONE"));
        RxTxSerialPort rxTxSerialPort = mock(RxTxSerialPort.class);
        ComPort comPort = getProperlyMockedComPort(comChannel, rxTxSerialPort);

        AtModemComponent atModemComponent = spy(new AtModemComponent(new TypedAtModemProperties(getProperProperties())));
        when(this.serialComponentFactory.newAtModemComponent(any(AbstractAtModemProperties.class))).thenReturn(atModemComponent);
        RxTxAtModemConnectionType atModemConnectionType = spy(new RxTxAtModemConnectionType());

        try {
            atModemConnectionType.connect(comPort, getProperProperties());
        } catch (ConnectionException e) {
            if (!((ModemException) e.getCause()).getMessageId().equals("CSM-COM-212")) {
                fail("Should have gotten exception indicating that the modem received a NO DIALTONE signal, but was " + e.getMessage());
            }
            assertThat(((ModemException) e.getCause()).getMessageArguments()).contains(comPortName);
            verify(atModemComponent, times(1)).hangUpComChannel(comChannel);
            verify(atModemComponent, times(1)).reStoreProfile(comChannel);
            verify(atModemComponent, never()).sendInitStrings(comChannel);
            throw e;
        }
    }

    @Test(timeout = TEST_TIMEOUT_MILLIS)
    public void verifyConnectSuccess() throws Exception {
        AbstractModemTests.TestableSerialComChannel comChannel = getTestableComChannel();
        comChannel.setResponses(OK_LIST);
        RxTxSerialPort rxTxSerialPort = mock(RxTxSerialPort.class);
        ComPort comPort = getProperlyMockedComPort(comChannel, rxTxSerialPort);

        AtModemComponent atModemComponent = spy(new AtModemComponent(new TypedAtModemProperties(getProperProperties())));
        when(this.serialComponentFactory.newAtModemComponent(any(AbstractAtModemProperties.class))).thenReturn(atModemComponent);
        RxTxAtModemConnectionType atModemConnectionType = spy(new RxTxAtModemConnectionType());

        atModemConnectionType.connect(comPort, getProperProperties());

        verify(atModemComponent, times(1)).dialModem(comChannel);
    }

    @Test(timeout = TEST_TIMEOUT_MILLIS, expected = ConnectionException.class)
    public void verifyConnectBusy() throws Exception {
        AbstractModemTests.TestableSerialComChannel comChannel = getTestableComChannel();
        comChannel.setResponses(Arrays.asList(RUBBISH_FOR_FLUSH, "OK", "OK", "OK", "BUSY", "OK", "OK"));
        RxTxSerialPort rxTxSerialPort = mock(RxTxSerialPort.class);
        ComPort comPort = getProperlyMockedComPort(comChannel, rxTxSerialPort);

        AtModemComponent atModemComponent = spy(new AtModemComponent(new TypedAtModemProperties(getProperProperties())));
        when(this.serialComponentFactory.newAtModemComponent(any(AbstractAtModemProperties.class))).thenReturn(atModemComponent);
        RxTxAtModemConnectionType atModemConnectionType = spy(new RxTxAtModemConnectionType());

        try {
            atModemConnectionType.connect(comPort, getProperProperties());
        } catch (ConnectionException e) {
            if (!((ModemException) e.getCause()).getMessageId().equals("CSM-COM-208")) {
                fail("Should have gotten exception indicating that the connect failed with a busy command, but was " + e.getMessage());
            }
            assertThat(((ModemException) e.getCause()).getMessageArguments()).contains(comPortName);
            verify(atModemComponent, times(1)).dialModem(comChannel);
            throw e;
        }
    }

    @Test(timeout = TEST_TIMEOUT_MILLIS)
    public void testWithNoSelector() throws Exception {
        AbstractModemTests.TestableSerialComChannel comChannel = getTestableComChannel();
        comChannel.setResponses(OK_LIST);
        RxTxSerialPort rxTxSerialPort = mock(RxTxSerialPort.class);
        ComPort comPort = getProperlyMockedComPort(comChannel, rxTxSerialPort);

        AtModemComponent atModemComponent = spy(new AtModemComponent(new TypedAtModemProperties(getProperProperties())));
        when(this.serialComponentFactory.newAtModemComponent(any(AbstractAtModemProperties.class))).thenReturn(atModemComponent);
        RxTxAtModemConnectionType atModemConnectionType = spy(new RxTxAtModemConnectionType());

        atModemConnectionType.connect(comPort, getProperProperties());

        verify(atModemComponent, times(1)).dialModem(comChannel);
        verify(atModemComponent, never()).sendAddressSelector(comChannel);
    }

    @Test(timeout = TEST_TIMEOUT_MILLIS)
    public void testWithAddressSelector() throws Exception {
        AbstractModemTests.TestableSerialComChannel comChannel = getTestableComChannel();
        comChannel.setResponses(OK_LIST);
        RxTxSerialPort rxTxSerialPort = mock(RxTxSerialPort.class);
        ComPort comPort = getProperlyMockedComPort(comChannel, rxTxSerialPort);

        List<ConnectionTaskProperty> properProperties = getProperProperties();
        for (ConnectionTaskProperty properProperty : properProperties) {
            if (properProperty.getName().equals(TypedAtModemProperties.AT_MODEM_ADDRESS_SELECTOR)) {
                ((ConnectionTaskPropertyImpl) properProperty).setValue("AddressSelect_01");
            }
        }
        AtModemComponent atModemComponent = spy(new AtModemComponent(new TypedAtModemProperties(properProperties)));
        when(this.serialComponentFactory.newAtModemComponent(any(AbstractAtModemProperties.class))).thenReturn(atModemComponent);
        RxTxAtModemConnectionType atModemConnectionType = spy(new RxTxAtModemConnectionType());

        atModemConnectionType.connect(comPort, properProperties);

        verify(atModemComponent, times(1)).dialModem(comChannel);
        verify(atModemComponent, times(1)).sendAddressSelector(comChannel);
    }

}