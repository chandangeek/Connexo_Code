package com.energyict.mdc.io.impl;

import com.energyict.mdc.io.BaudrateValue;
import com.energyict.mdc.io.FlowControl;
import com.energyict.mdc.io.NrOfDataBits;
import com.energyict.mdc.io.NrOfStopBits;
import com.energyict.mdc.io.Parities;
import com.energyict.mdc.io.SerialPortConfiguration;
import com.energyict.mdc.io.SerialPortException;

import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;
import org.junit.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests for the {@link RxTxSerialPort} component.
 * <p/>
 * Copyrights EnergyICT
 * Date: 17/08/12
 * Time: 9:52
 */
public class RxTxSerialPortTest {

    private static final BigDecimal NR_OF_DATA_BITS_8 = new BigDecimal(SerialPort.DATABITS_8);
    private static final BigDecimal NR_OF_STOP_BITS_1 = BigDecimal.ONE;
    private static final String PARITY_NONE = Parities.NONE.value();

    @Test
    public void getCorrectNrOfDataBitsTest() {
        SerialPortConfiguration configuration = mock(SerialPortConfiguration.class);
        RxTxSerialPort serialPortRxTx = new RxTxSerialPort(configuration);
        assertEquals(5, serialPortRxTx.getRxTxNrOfDataBits(new BigDecimal(5)));
        assertEquals(6, serialPortRxTx.getRxTxNrOfDataBits(new BigDecimal("6")));
        assertEquals(7, serialPortRxTx.getRxTxNrOfDataBits(new BigDecimal(new BigInteger("7"), 0)));
        assertEquals(8, serialPortRxTx.getRxTxNrOfDataBits(NR_OF_DATA_BITS_8));
    }

    @Test
    public void getIncorrectNrOfDataBitsTest() {
        try {
            SerialPortConfiguration configuration = mock(SerialPortConfiguration.class);
            RxTxSerialPort serialPortRxTx = new RxTxSerialPort(configuration);
            serialPortRxTx.getRxTxNrOfDataBits(new BigDecimal(2345));
        } catch (SerialPortException e) {
            // we should get a configuration mismatch exception, otherwise throw the exception so the TestFrameWork can handle this
            if (!e.getMessageSeed().equals(MessageSeeds.SERIAL_PORT_CONFIGURATION_MISMATCH)) {
                throw e;
            }
        }
    }

    @Test
    public void getCorrectNrOfStopBitsTest() {
        SerialPortConfiguration configuration = mock(SerialPortConfiguration.class);
        RxTxSerialPort serialPortRxTx = new RxTxSerialPort(configuration);
        assertEquals(SerialPort.STOPBITS_1, serialPortRxTx.getRxTxNrOfStopBits(NR_OF_STOP_BITS_1));
        assertEquals(SerialPort.STOPBITS_2, serialPortRxTx.getRxTxNrOfStopBits(new BigDecimal("2")));
        assertEquals(SerialPort.STOPBITS_1_5, serialPortRxTx.getRxTxNrOfStopBits(new BigDecimal("1.5")));
    }

    @Test
    public void getInCorrectNrOfStopBitsTest() {
        try {
            SerialPortConfiguration configuration = mock(SerialPortConfiguration.class);
            RxTxSerialPort serialPortRxTx = new RxTxSerialPort(configuration);
            serialPortRxTx.getRxTxNrOfStopBits(new BigDecimal(1000));
        } catch (SerialPortException e) {
            // we should get a configuration mismatch exception, otherwise throw the exception so the TestFrameWork can handle this
            if (!e.getMessageSeed().equals(MessageSeeds.SERIAL_PORT_CONFIGURATION_MISMATCH)) {
                throw e;
            }
        }
    }

    @Test
    public void getCorrectParityTest() {
        SerialPortConfiguration configuration = mock(SerialPortConfiguration.class);
        RxTxSerialPort serialPortRxTx = new RxTxSerialPort(configuration);
        assertEquals(SerialPort.PARITY_NONE, serialPortRxTx.getRxTxParity(PARITY_NONE));
        assertEquals(SerialPort.PARITY_EVEN, serialPortRxTx.getRxTxParity(Parities.EVEN.value()));
        assertEquals(SerialPort.PARITY_MARK, serialPortRxTx.getRxTxParity(Parities.MARK.value()));
        assertEquals(SerialPort.PARITY_ODD, serialPortRxTx.getRxTxParity(Parities.ODD.value()));
        assertEquals(SerialPort.PARITY_SPACE, serialPortRxTx.getRxTxParity(Parities.SPACE.value()));
    }

    @Test
    public void getInCorrectParityTest() {
        try {
            SerialPortConfiguration configuration = mock(SerialPortConfiguration.class);
            RxTxSerialPort serialPortRxTx = new RxTxSerialPort(configuration);
            serialPortRxTx.getRxTxParity("UnKnoWnPariTy");
        } catch (SerialPortException e) {
            // we should get a configuration mismatch exception, otherwise throw the exception so the TestFrameWork can handle this
            if (!e.getMessageSeed().equals(MessageSeeds.SERIAL_PORT_CONFIGURATION_MISMATCH)) {
                throw e;
            }
        }
    }

    @Test
    public void getCorrectFlowControlTest() throws UnsupportedCommOperationException {
        SerialPort serialPort = mock(SerialPort.class);
        SerialPortConfiguration configuration = mock(SerialPortConfiguration.class);
        RxTxSerialPort serialPortRxTx = new RxTxSerialPort(configuration);
        serialPortRxTx.setSerialPort(serialPort);

        serialPortRxTx.setFlowControlMode(FlowControl.NONE.value());
        verify(serialPort).setFlowControlMode(SerialPort.FLOWCONTROL_NONE);

        serialPortRxTx.setFlowControlMode(FlowControl.RTSCTS.value());
        verify(serialPort).setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN | SerialPort.FLOWCONTROL_RTSCTS_OUT);

        serialPortRxTx.setFlowControlMode(FlowControl.XONXOFF.value());
        verify(serialPort).setFlowControlMode(SerialPort.FLOWCONTROL_XONXOFF_IN | SerialPort.FLOWCONTROL_XONXOFF_OUT);

    }

    @Test
    public void setDtrDsrFlowControlModeTest() throws UnsupportedCommOperationException {
        SerialPort serialPort = mock(SerialPort.class);
        SerialPortConfiguration configuration = mock(SerialPortConfiguration.class);
        RxTxSerialPort serialPortRxTx = new RxTxSerialPort(configuration);
        serialPortRxTx.setSerialPort(serialPort);

        try {
            serialPortRxTx.setFlowControlMode(FlowControl.DTRDSR.value());
        } catch (SerialPortException e) {
            // we should get a configuration mismatch because currently we don't support the DTR/DSR type ...
            if (!e.getMessageSeed().equals(MessageSeeds.SERIAL_PORT_CONFIGURATION_MISMATCH)) {
                throw e;
            }
        }

    }

    @Test
    public void setInCorrectFlowControlTest() throws UnsupportedCommOperationException {
        try {
            SerialPort serialPort = mock(SerialPort.class);
            SerialPortConfiguration configuration = mock(SerialPortConfiguration.class);
            RxTxSerialPort serialPortRxTx = new RxTxSerialPort(configuration);
            serialPortRxTx.setSerialPort(serialPort);

            serialPortRxTx.setFlowControlMode("UnkNoWnflOwCONtrOlMOde");
        } catch (SerialPortException e) {
            // we should get a configuration mismatch exception, otherwise throw the exception so the TestFrameWork can handle this
            if (!e.getMessageSeed().equals(MessageSeeds.SERIAL_PORT_CONFIGURATION_MISMATCH)) {
                throw e;
            }
        }
    }

//    @Test // this test can NOT run on Bamboo, only for local testing of your own ComPorts
    public void comPortTest() {
        SerialPortConfiguration configuration = new SerialPortConfiguration(
                "COM15",
                BaudrateValue.BAUDRATE_4800,
                NrOfDataBits.SEVEN,
                NrOfStopBits.ONE,
                Parities.MARK,
                FlowControl.XONXOFF
        );

        RxTxSerialPort serialPortRxTx = null;
        try {
            serialPortRxTx = new RxTxSerialPort(configuration);
            serialPortRxTx.openAndInit();
            serialPortRxTx.getOutputStream().write("ATZ\r\n".getBytes());
            byte[] input = new byte[1024];
            serialPortRxTx.getInputStream().read(input);
            System.out.println(Arrays.toString(input));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(serialPortRxTx != null){
                serialPortRxTx.close();
            }
        }
    }

}