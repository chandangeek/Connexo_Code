/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.channels.serial.modem.serialio;

import com.energyict.mdc.channel.serial.SerialPortConfiguration;
import com.energyict.mdc.channel.serial.ServerSerialPort;
import com.energyict.mdc.channel.serial.SignalController;
import com.energyict.mdc.channel.serial.direct.serialio.SioSerialPort;
import com.energyict.mdc.channel.serial.modemproperties.PEMPModemConfiguration;
import com.energyict.mdc.channel.serial.modemproperties.TypedAtModemProperties;
import com.energyict.mdc.channels.serial.modem.AbstractModemTests;
import com.energyict.mdc.channels.serial.modem.TypedPEMPModemProperties;
import com.energyict.mdc.protocol.ComChannelType;
import com.energyict.mdc.upl.io.ModemException;
import com.energyict.mdc.upl.io.SerialComponentService;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.protocol.exceptions.ConnectionException;
import org.junit.Before;
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
public class SioPEMPModemConnectionTypeTest extends AbstractModemTests {

    private static final int DTR_TOGGLE_DELAY_VALUE = 100;
    private static final String MODEM_CONFIGURATION_KEY = PEMPModemConfiguration.WWS.getKey();

    protected final List<String> OK_LIST = Arrays.asList(
            RUBBISH_FOR_FLUSH,
            "\r\n*\r\n",
            "\r\nFENS\r\n", "\r\nFENS\r\n", "\r\nFENS\r\n", "\r\nFENS\r\n",
            "\r\nXX COM\r\nYY\r\n"
    );

    private PropertySpecService propertySpecService;
    private SerialComponentService serialComponentService;

    @Before
    public void initializeMocksAndFactories() {
        propertySpecService = mock(PropertySpecService.class);
        serialComponentService = mock(SerialComponentService.class);
/*
        //TODO
        PropertySpecBuilderWizard.NlsOptions propertySpecBuilder = new PropertySpecBuilderImpl();
        when(propertySpecService.encryptedStringSpec()).thenReturn(propertySpecBuilder);
*/

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

    private TypedProperties getProperProperties() {
        com.energyict.protocolimpl.properties.TypedProperties result = com.energyict.protocolimpl.properties.TypedProperties.empty();

        result.setProperty(TypedAtModemProperties.DELAY_BEFORE_SEND, Duration.ofMillis(10));
        result.setProperty(TypedAtModemProperties.COMMAND_TIMEOUT, Duration.ofMillis(COMMAND_TIMEOUT_VALUE));
        result.setProperty(TypedAtModemProperties.COMMAND_TRIES, new BigDecimal(1));
        result.setProperty(TypedAtModemProperties.MODEM_INIT_STRINGS, "");
        result.setProperty(TypedAtModemProperties.DELAY_AFTER_CONNECT, Duration.ofMillis(10));
        result.setProperty(TypedAtModemProperties.CONNECT_TIMEOUT, Duration.ofMillis(COMMAND_TIMEOUT_VALUE));
        result.setProperty(TypedAtModemProperties.DTR_TOGGLE_DELAY, Duration.ofMillis(DTR_TOGGLE_DELAY_VALUE));
        result.setProperty(TypedAtModemProperties.PHONE_NUMBER_PROPERTY_NAME, PHONE_NUMBER);
        result.setProperty(TypedPEMPModemProperties.MODEM_CONFIGURATION_KEY, MODEM_CONFIGURATION_KEY);

        return result;
    }

    private void getProperlyMockedComPort(AbstractModemTests.TestableSerialComChannel serialComChannel, SioSerialPort sioSerialPort) throws Exception {
        when(this.serialComponentService.newSerialPort(any(SerialPortConfiguration.class))).thenReturn(sioSerialPort);
        when(this.serialComponentService.newSerialComChannel(any(ServerSerialPort.class), any(ComChannelType.class))).thenReturn(serialComChannel);
    }

    @Test(timeout = TEST_LONG_TIMEOUT_MILLIS, expected = ConnectionException.class)
    public void testInitializePEMPCommandStateFails() throws Exception {
        AbstractModemTests.TestableSerialComChannel serialComChannel = getTestableComChannel();
        serialComChannel.setResponses(Arrays.asList("   ", "\r\n*\r\n",
                "NotValidResponse", "NotValidResponse", "NotValidResponse", "NotValidResponse", "NotValidResponse",
                "NotValidResponse", "NotValidResponse", "NotValidResponse", "NotValidResponse", "NotValidResponse",
                "NotValidResponse", "NotValidResponse", "NotValidResponse", "NotValidResponse", "NotValidResponse",
                "NotValidResponse", "NotValidResponse", "NotValidResponse", "NotValidResponse", "NotValidResponse"));
        SioSerialPort sioSerialPort = mock(SioSerialPort.class);
        getProperlyMockedComPort(serialComChannel, sioSerialPort);

        SioPEMPModemConnectionType modemConnectionType = new SioPEMPModemConnectionType(propertySpecService);
        modemConnectionType.setUPLProperties(getProperProperties());

        try {
            modemConnectionType.connect();
        } catch (ConnectionException e) {
            if (!((ModemException) e.getCause()).getType().equals(ModemException.Type.MODEM_COULD_NOT_INITIALIZE_COMMAND_STATE)) {
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
        getProperlyMockedComPort(serialComChannel, sioSerialPort);

        SioPEMPModemConnectionType modemConnectionType = new SioPEMPModemConnectionType(propertySpecService);
        modemConnectionType.setUPLProperties(getProperProperties());

        try {
            modemConnectionType.connect();
        } catch (ModemException e) {
            if (!((ModemException) e.getCause()).getType().equals(ModemException.Type.MODEM_READ_TIMEOUT)) {
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
        getProperlyMockedComPort(comChannel, sioSerialPort);

        SioPEMPModemConnectionType modemConnectionType = spy(new SioPEMPModemConnectionType(propertySpecService));

        modemConnectionType.setUPLProperties(getProperProperties());
        modemConnectionType.connect();

        verify(modemConnectionType.pempModemComponent, times(1)).initializeAfterConnect(comChannel);
    }

    @Test(timeout = TEST_TIMEOUT_MILLIS)
    public void verifyConnectSuccess() throws Exception {
        AbstractModemTests.TestableSerialComChannel comChannel = getTestableComChannel();
        comChannel.setResponses(OK_LIST);
        SioSerialPort sioSerialPort = mock(SioSerialPort.class);
        getProperlyMockedComPort(comChannel, sioSerialPort);

        SioPEMPModemConnectionType modemConnectionType = spy(new SioPEMPModemConnectionType(propertySpecService));

        modemConnectionType.setUPLProperties(getProperProperties());
        modemConnectionType.connect();

        verify(modemConnectionType.pempModemComponent, times(1)).dialModem(comChannel);
        verify(modemConnectionType.pempModemComponent, times(1)).initializeAfterConnect(comChannel);
    }

    @Test(timeout = TEST_TIMEOUT_MILLIS)
    public void verifyConnectSuccessAfterRetry() throws Exception {
        AbstractModemTests.TestableSerialComChannel comChannel = getTestableComChannel();
        comChannel.setResponses(Arrays.asList(RUBBISH_FOR_FLUSH, "\r\n*\r\n", "\r\nFENS\r\n", "\r\nFENS\r\n", "\r\nFENS\r\n", "\r\nFENS\r\n", "RUBBISH", "\r\nXX COM\r\nYY\r\n"));
        SioSerialPort sioSerialPort = mock(SioSerialPort.class);
        getProperlyMockedComPort(comChannel, sioSerialPort);

        SioPEMPModemConnectionType modemConnectionType = spy(new SioPEMPModemConnectionType(propertySpecService));

        modemConnectionType.setUPLProperties(getProperProperties());
        modemConnectionType.connect();

        verify(modemConnectionType.pempModemComponent, times(1)).dialModem(comChannel);
        verify(modemConnectionType.pempModemComponent, times(1)).initializeAfterConnect(comChannel);
    }
}