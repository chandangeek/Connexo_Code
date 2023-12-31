/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.channels.serial.direct.rxtx;

import com.energyict.mdc.channel.serial.BaudrateValue;
import com.energyict.mdc.channel.serial.FlowControl;
import com.energyict.mdc.channel.serial.NrOfDataBits;
import com.energyict.mdc.channel.serial.NrOfStopBits;
import com.energyict.mdc.channel.serial.Parities;
import com.energyict.mdc.channel.serial.SerialPortConfiguration;
import com.energyict.mdc.channel.serial.direct.rxtx.RxTxSerialPort;
import com.energyict.mdc.upl.io.SerialPortException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class RxTxSerialPortTest {

    private static final BigDecimal NR_OF_DATA_BITS_8 = new BigDecimal(SerialPort.DATABITS_8);
    private static final BigDecimal NR_OF_STOP_BITS_1 = BigDecimal.ONE;
    private static final String PARITY_NONE = Parities.NONE.getParity();

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
            if (!e.getType().equals(SerialPortException.Type.SERIAL_PORT_CONFIGURATION_MISMATCH)) {
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
            if (!e.getType().equals(SerialPortException.Type.SERIAL_PORT_CONFIGURATION_MISMATCH)) {
                throw e;
            }
        }
    }

    @Test
    public void getCorrectParityTest() {
        SerialPortConfiguration configuration = mock(SerialPortConfiguration.class);
        RxTxSerialPort serialPortRxTx = new RxTxSerialPort(configuration);
        assertEquals(SerialPort.PARITY_NONE, serialPortRxTx.getRxTxParity(PARITY_NONE));
        assertEquals(SerialPort.PARITY_EVEN, serialPortRxTx.getRxTxParity(Parities.EVEN.getParity()));
        assertEquals(SerialPort.PARITY_MARK, serialPortRxTx.getRxTxParity(Parities.MARK.getParity()));
        assertEquals(SerialPort.PARITY_ODD, serialPortRxTx.getRxTxParity(Parities.ODD.getParity()));
        assertEquals(SerialPort.PARITY_SPACE, serialPortRxTx.getRxTxParity(Parities.SPACE.getParity()));
    }

    @Test
    public void getInCorrectParityTest() {
        try {
            SerialPortConfiguration configuration = mock(SerialPortConfiguration.class);
            RxTxSerialPort serialPortRxTx = new RxTxSerialPort(configuration);
            serialPortRxTx.getRxTxParity("UnKnoWnPariTy");
        } catch (SerialPortException e) {
            // we should get a configuration mismatch exception, otherwise throw the exception so the TestFrameWork can handle this
            if (!e.getType().equals(SerialPortException.Type.SERIAL_PORT_CONFIGURATION_MISMATCH)) {
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

        serialPortRxTx.setFlowControlMode(FlowControl.NONE.getFlowControl());
        verify(serialPort).setFlowControlMode(SerialPort.FLOWCONTROL_NONE);

        serialPortRxTx.setFlowControlMode(FlowControl.RTSCTS.getFlowControl());
        verify(serialPort).setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN | SerialPort.FLOWCONTROL_RTSCTS_OUT);

        serialPortRxTx.setFlowControlMode(FlowControl.XONXOFF.getFlowControl());
        verify(serialPort).setFlowControlMode(SerialPort.FLOWCONTROL_XONXOFF_IN | SerialPort.FLOWCONTROL_XONXOFF_OUT);

    }

    @Test
    public void setDtrDsrFlowControlModeTest() throws UnsupportedCommOperationException {
        SerialPort serialPort = mock(SerialPort.class);
        SerialPortConfiguration configuration = mock(SerialPortConfiguration.class);
        RxTxSerialPort serialPortRxTx = new RxTxSerialPort(configuration);
        serialPortRxTx.setSerialPort(serialPort);

        try {
            serialPortRxTx.setFlowControlMode(FlowControl.DTRDSR.getFlowControl());
        } catch (SerialPortException e) {
            // we should get a configuration mismatch because currently we don't support the DTR/DSR type ...
            if (!e.getType().equals(SerialPortException.Type.SERIAL_PORT_CONFIGURATION_MISMATCH)) {
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
            if (!e.getType().equals(SerialPortException.Type.SERIAL_PORT_CONFIGURATION_MISMATCH)) {
                throw e;
            }
        }
    }

    //    @Test // this test can NOT run on Bamboo, only for local testing of you own ComPorts
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
            if (serialPortRxTx != null) {
                serialPortRxTx.close();
            }
        }
    }
}
