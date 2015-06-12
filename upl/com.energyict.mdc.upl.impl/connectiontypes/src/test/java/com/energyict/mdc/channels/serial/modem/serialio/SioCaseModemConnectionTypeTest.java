package com.energyict.mdc.channels.serial.modem.serialio;

import com.energyict.cbo.TimeDuration;
import com.energyict.mdc.ManagerFactory;
import com.energyict.mdc.SerialComponentFactory;
import com.energyict.mdc.ServerManager;
import com.energyict.mdc.channels.serial.SerialPortConfiguration;
import com.energyict.mdc.channels.serial.ServerSerialPort;
import com.energyict.mdc.channels.serial.SignalController;
import com.energyict.mdc.channels.serial.direct.serialio.SioSerialPort;
import com.energyict.mdc.channels.serial.modem.AbstractCaseModemProperties;
import com.energyict.mdc.channels.serial.modem.AbstractModemTests;
import com.energyict.mdc.channels.serial.modem.CaseModemComponent;
import com.energyict.mdc.channels.serial.modem.TypedCaseModemProperties;
import com.energyict.mdc.channels.serial.modem.TypedPaknetModemProperties;
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
 * Tests for the {@link SioCaseModemConnectionType} component
 * 
 * @author sva
 * @since 30/04/13 - 14:43
 */
@RunWith(MockitoJUnitRunner.class)
public class SioCaseModemConnectionTypeTest extends AbstractModemTests{

    private static final int DTR_TOGGLE_DELAY_VALUE = 100;

    private final List<String> OK_LIST = Arrays.asList(RUBBISH_FOR_FLUSH, "ECHO OFF", "DTR NORMAL", "ERROR CORRECTING MODE", "LINK ESTABLISHED");

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
        ConnectionTaskPropertyImpl delayBeforeSendProperty = new ConnectionTaskPropertyImpl(TypedCaseModemProperties.DELAY_BEFORE_SEND);
        delayBeforeSendProperty.setValue(new TimeDuration(10, TimeDuration.MILLISECONDS));
        ConnectionTaskPropertyImpl commandTimeout = new ConnectionTaskPropertyImpl(TypedCaseModemProperties.COMMAND_TIMEOUT);
        commandTimeout.setValue(new TimeDuration(COMMAND_TIMEOUT_VALUE, TimeDuration.MILLISECONDS));
        ConnectionTaskPropertyImpl commandTries = new ConnectionTaskPropertyImpl(TypedCaseModemProperties.COMMAND_TRIES);
        commandTries.setValue(new BigDecimal(1));
        ConnectionTaskPropertyImpl modemInitStrings = new ConnectionTaskPropertyImpl(TypedCaseModemProperties.MODEM_INIT_STRINGS);
        modemInitStrings.setValue("");
        ConnectionTaskPropertyImpl delayAfterConnect = new ConnectionTaskPropertyImpl(TypedCaseModemProperties.DELAY_AFTER_CONNECT);
        delayAfterConnect.setValue(new TimeDuration(10, TimeDuration.MILLISECONDS));
        ConnectionTaskPropertyImpl connectTimeOut = new ConnectionTaskPropertyImpl(TypedCaseModemProperties.CONNECT_TIMEOUT);
        connectTimeOut.setValue(new TimeDuration(COMMAND_TIMEOUT_VALUE, TimeDuration.MILLISECONDS));
        ConnectionTaskPropertyImpl dialPrefix = new ConnectionTaskPropertyImpl(TypedCaseModemProperties.MODEM_DIAL_PREFIX);
        dialPrefix.setValue("");
        ConnectionTaskPropertyImpl addressSelector = new ConnectionTaskPropertyImpl(TypedCaseModemProperties.MODEM_ADDRESS_SELECTOR);
        addressSelector.setValue("");
        ConnectionTaskPropertyImpl dtrToggleDelay = new ConnectionTaskPropertyImpl(TypedPaknetModemProperties.DTR_TOGGLE_DELAY);
        dtrToggleDelay.setValue(new TimeDuration(DTR_TOGGLE_DELAY_VALUE, TimeDuration.MILLISECONDS));
        ConnectionTaskPropertyImpl phoneNumber = new ConnectionTaskPropertyImpl(TypedCaseModemProperties.PHONE_NUMBER_PROPERTY_NAME);
        phoneNumber.setValue(PHONE_NUMBER);

        return Arrays.<ConnectionTaskProperty>asList(delayBeforeSendProperty, commandTimeout, commandTries, modemInitStrings,
                delayAfterConnect, connectTimeOut, dialPrefix, addressSelector, dtrToggleDelay, phoneNumber);
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

        CaseModemComponent caseModemComponent = spy(new CaseModemComponent(new TypedCaseModemProperties(getProperProperties())));
        when(this.serialComponentFactory.newCaseModemComponent(any(AbstractCaseModemProperties.class))).thenReturn(caseModemComponent);
        SioCaseModemConnectionType caseModemConnectionType = new SioCaseModemConnectionType();

        try {
            caseModemConnectionType.connect(comPort, getProperProperties());
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

        CaseModemComponent atModemComponent = spy(new CaseModemComponent(new TypedCaseModemProperties(getProperProperties())));
        when(this.serialComponentFactory.newCaseModemComponent(any(AbstractCaseModemProperties.class))).thenReturn(atModemComponent);
        SioCaseModemConnectionType caseModemConnectionType = spy(new SioCaseModemConnectionType());

        caseModemConnectionType.connect(comPort, getProperProperties());

        verify(atModemComponent, times(1)).sendInitStrings(comChannel);
    }

    @Test(timeout = TEST_TIMEOUT_MILLIS, expected = ConnectionException.class)
    public void writeFailingInitStringTest() throws Exception {
        TestableSerialComChannel comChannel = getTestableComChannel();
        comChannel.setResponses(Arrays.asList(RUBBISH_FOR_FLUSH, "ECHO OFF", "DTR NORMAL", "Not_CorrectAnswer"));
        SioSerialPort sioSerialPort = mock(SioSerialPort.class);
        ComPort comPort = getProperlyMockedComPort(comChannel, sioSerialPort);


        CaseModemComponent atModemComponent = spy(new CaseModemComponent(new TypedCaseModemProperties(getProperProperties())));
        when(this.serialComponentFactory.newCaseModemComponent(any(AbstractCaseModemProperties.class))).thenReturn(atModemComponent);
        SioCaseModemConnectionType caseModemConnectionType = spy(new SioCaseModemConnectionType());

        try {
            caseModemConnectionType.connect(comPort, getProperProperties());
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

        CaseModemComponent modemComponent = spy(new CaseModemComponent(new TypedCaseModemProperties(getProperProperties())));
        when(this.serialComponentFactory.newCaseModemComponent(any(AbstractCaseModemProperties.class))).thenReturn(modemComponent);
        SioCaseModemConnectionType caseModemConnectionType = spy(new SioCaseModemConnectionType());

        try {
            caseModemConnectionType.connect(comPort, getProperProperties());
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

        CaseModemComponent modemComponent = spy(new CaseModemComponent(new TypedCaseModemProperties(getProperProperties())));
        when(this.serialComponentFactory.newCaseModemComponent(any(AbstractCaseModemProperties.class))).thenReturn(modemComponent);
        SioCaseModemConnectionType caseModemConnectionType = spy(new SioCaseModemConnectionType());

        caseModemConnectionType.connect(comPort, getProperProperties());

        verify(modemComponent, times(1)).dialModem(comChannel);
    }

    @Test(timeout = TEST_TIMEOUT_MILLIS)
    public void testWithNoSelector() throws Exception {
        TestableSerialComChannel comChannel = getTestableComChannel();
        comChannel.setResponses(OK_LIST);
        SioSerialPort sioSerialPort = mock(SioSerialPort.class);
        ComPort comPort = getProperlyMockedComPort(comChannel, sioSerialPort);

        CaseModemComponent modemComponent = spy(new CaseModemComponent(new TypedCaseModemProperties(getProperProperties())));
        when(this.serialComponentFactory.newCaseModemComponent(any(AbstractCaseModemProperties.class))).thenReturn(modemComponent);
        SioCaseModemConnectionType caseModemConnectionType = spy(new SioCaseModemConnectionType());

        caseModemConnectionType.connect(comPort, getProperProperties());

        verify(modemComponent, times(1)).dialModem(comChannel);
        verify(modemComponent, never()).sendAddressSelector(comChannel);
    }

    @Test(timeout = TEST_TIMEOUT_MILLIS)
    public void testWithAddressSelector() throws Exception {
        TestableSerialComChannel comChannel = getTestableComChannel();
        comChannel.setResponses(OK_LIST);
        SioSerialPort sioSerialPort = mock(SioSerialPort.class);
        ComPort comPort = getProperlyMockedComPort(comChannel, sioSerialPort);

        List<ConnectionTaskProperty> properProperties = getProperProperties();
        for (ConnectionTaskProperty properProperty : properProperties) {
            if (properProperty.getName().equals(TypedCaseModemProperties.MODEM_ADDRESS_SELECTOR)) {
                ((ConnectionTaskPropertyImpl) properProperty).setValue("AddressSelect_01");
            }
        }
        CaseModemComponent modemComponent = spy(new CaseModemComponent(new TypedCaseModemProperties(properProperties)));
        when(this.serialComponentFactory.newCaseModemComponent(any(AbstractCaseModemProperties.class))).thenReturn(modemComponent);
        SioCaseModemConnectionType caseModemConnectionType = spy(new SioCaseModemConnectionType());

        caseModemConnectionType.connect(comPort, properProperties);

        verify(modemComponent, times(1)).dialModem(comChannel);
        verify(modemComponent, times(1)).sendAddressSelector(comChannel);

    }
}
