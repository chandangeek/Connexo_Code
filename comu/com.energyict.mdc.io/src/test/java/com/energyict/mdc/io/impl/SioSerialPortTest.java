/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.io.impl;

import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.io.BaudrateValue;
import com.energyict.mdc.io.FlowControl;
import com.energyict.mdc.io.NrOfDataBits;
import com.energyict.mdc.io.NrOfStopBits;
import com.energyict.mdc.io.Parities;
import com.energyict.mdc.io.SerialPortConfiguration;
import com.energyict.mdc.io.SerialPortException;

import Serialio.SerialConfig;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;
import static org.mockito.Mockito.mock;

public class SioSerialPortTest {

    @Test
    public void getCorrectNrOfDataBitsTest() {
        SerialPortConfiguration configuration = mock(SerialPortConfiguration.class);
        SioSerialPort serialPort = new SioSerialPort(configuration);

        assertThat(serialPort.getSioDataBits(new BigDecimal(5))).isEqualTo(SerialConfig.LN_5BITS);
        assertThat(serialPort.getSioDataBits(new BigDecimal("6"))).isEqualTo(SerialConfig.LN_6BITS);
        assertThat(serialPort.getSioDataBits(new BigDecimal(new BigInteger("7")))).isEqualTo(SerialConfig.LN_7BITS);
        assertThat(serialPort.getSioDataBits(new BigDecimal(8))).isEqualTo(SerialConfig.LN_8BITS);
    }

    @Test
    public void getIncorrectNrOfDataBitsTest() {
        try {
            SerialPortConfiguration configuration = mock(SerialPortConfiguration.class);
            SioSerialPort serialPort = new SioSerialPort(configuration);
            serialPort.getSioDataBits(new BigDecimal("100.156"));
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
        SioSerialPort serialPort = new SioSerialPort(configuration);

        assertThat(serialPort.getSioStopBits(BigDecimal.ONE)).isEqualTo(SerialConfig.ST_1BITS);
        assertThat(serialPort.getSioStopBits(new BigDecimal("2"))).isEqualTo(SerialConfig.ST_2BITS);
    }

    @Test
    public void unsupportedOneAndHalfStopBitsTest() {
        SerialPortConfiguration configuration = mock(SerialPortConfiguration.class);
        SioSerialPort serialPort = new SioSerialPort(configuration);

        try {
            serialPort.getSioStopBits(new BigDecimal("1.5"));
        } catch (SerialPortException e) {
            // we should get a configuration mismatch exception, otherwise throw the exception so the TestFrameWork can handle this
            if (!e.getMessageSeed().equals(MessageSeeds.SERIAL_PORT_CONFIGURATION_MISMATCH)) {
                throw e;
            }
        }
    }

    @Test
    public void getIncorrectNrOfStopBitsTest() {
        SerialPortConfiguration configuration = mock(SerialPortConfiguration.class);
        SioSerialPort serialPort = new SioSerialPort(configuration);

        try {
            serialPort.getSioStopBits(new BigDecimal("96352"));
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
        SioSerialPort serialPort = new SioSerialPort(configuration);

        assertThat(serialPort.getSioParity(Parities.NONE.value())).isEqualTo(SerialConfig.PY_NONE);
        assertThat(serialPort.getSioParity(Parities.ODD.value())).isEqualTo(SerialConfig.PY_ODD);
        assertThat(serialPort.getSioParity(Parities.EVEN.value())).isEqualTo(SerialConfig.PY_EVEN);
        assertThat(serialPort.getSioParity(Parities.MARK.value())).isEqualTo(SerialConfig.PY_MARK);
        assertThat(serialPort.getSioParity(Parities.SPACE.value())).isEqualTo(SerialConfig.PY_SPACE);
    }

    @Test
    public void getIncorrectParityTest() {
        SerialPortConfiguration configuration = mock(SerialPortConfiguration.class);
        SioSerialPort serialPort = new SioSerialPort(configuration);

        try {
            serialPort.getSioParity("INCorrecTParItY");
        } catch (SerialPortException e) {
            // we should get a configuration mismatch exception, otherwise throw the exception so the TestFrameWork can handle this
            if (!e.getMessageSeed().equals(MessageSeeds.SERIAL_PORT_CONFIGURATION_MISMATCH)) {
                throw e;
            }
        }
    }

    @Test
    public void getCorrectFlowControlTest() {
        SerialPortConfiguration configuration = mock(SerialPortConfiguration.class);
        SioSerialPort serialPort = new SioSerialPort(configuration);

        assertThat(serialPort.getSioFlowControl(FlowControl.NONE.value())).isEqualTo(SerialConfig.HS_NONE);
        assertThat(serialPort.getSioFlowControl(FlowControl.DTRDSR.value())).isEqualTo(SerialConfig.HS_DSRDTR);
        assertThat(serialPort.getSioFlowControl(FlowControl.RTSCTS.value())).isEqualTo(SerialConfig.HS_CTSRTS);
        assertThat(serialPort.getSioFlowControl(FlowControl.XONXOFF.value())).isEqualTo(SerialConfig.HS_XONXOFF);
    }

    @Test
    public void getInCorrectFlowControlTest() {
        SerialPortConfiguration configuration = mock(SerialPortConfiguration.class);
        SioSerialPort serialPort = new SioSerialPort(configuration);

        try {
            serialPort.getSioFlowControl("InCORRectFloWContRoL");
        } catch (SerialPortException e) {
            // we should get a configuration mismatch exception, otherwise throw the exception so the TestFrameWork can handle this
            if (!e.getMessageSeed().equals(MessageSeeds.SERIAL_PORT_CONFIGURATION_MISMATCH)) {
                throw e;
            }
        }
    }

    @Test
    public void setBaudRateTest() {
        SerialPortConfiguration configuration = mock(SerialPortConfiguration.class);
        SioSerialPort serialPort = new SioSerialPort(configuration);

        assertThat(serialPort.getSioBaudrate(BaudrateValue.BAUDRATE_150.value())).isEqualTo(SerialConfig.BR_150);
        assertThat(serialPort.getSioBaudrate(BaudrateValue.BAUDRATE_300.value())).isEqualTo(SerialConfig.BR_300);
        assertThat(serialPort.getSioBaudrate(BaudrateValue.BAUDRATE_600.value())).isEqualTo(SerialConfig.BR_600);
        assertThat(serialPort.getSioBaudrate(BaudrateValue.BAUDRATE_1200.value())).isEqualTo(SerialConfig.BR_1200);
        assertThat(serialPort.getSioBaudrate(BaudrateValue.BAUDRATE_2400.value())).isEqualTo(SerialConfig.BR_2400);
        assertThat(serialPort.getSioBaudrate(BaudrateValue.BAUDRATE_4800.value())).isEqualTo(SerialConfig.BR_4800);
        assertThat(serialPort.getSioBaudrate(BaudrateValue.BAUDRATE_9600.value())).isEqualTo(SerialConfig.BR_9600);
        assertThat(serialPort.getSioBaudrate(BaudrateValue.BAUDRATE_19200.value())).isEqualTo(SerialConfig.BR_19200);
        assertThat(serialPort.getSioBaudrate(BaudrateValue.BAUDRATE_38400.value())).isEqualTo(SerialConfig.BR_38400);
        assertThat(serialPort.getSioBaudrate(BaudrateValue.BAUDRATE_57600.value())).isEqualTo(SerialConfig.BR_57600);
        assertThat(serialPort.getSioBaudrate(BaudrateValue.BAUDRATE_115200.value())).isEqualTo(SerialConfig.BR_115200);
        assertThat(serialPort.getSioBaudrate(BaudrateValue.BAUDRATE_230400.value())).isEqualTo(SerialConfig.BR_230400);
        assertThat(serialPort.getSioBaudrate(BaudrateValue.BAUDRATE_460800.value())).isEqualTo(SerialConfig.BR_460800);
    }

    /**
     * All the below baudrates are not supported by the SerialIO API
     */
    @Test
    public void getUnsupportedBaudRatesTest() {
        SerialPortConfiguration configuration = mock(SerialPortConfiguration.class);
        SioSerialPort serialPort = new SioSerialPort(configuration);
        int exceptionCounter = 0;

        try {
            serialPort.getSioBaudrate(BaudrateValue.BAUDRATE_1800.value());
        } catch (ApplicationException e) {
            if (!e.getMessage().equals("Baudrate " + BaudrateValue.BAUDRATE_1800.value() + " is not supported by this driver.")) {
                fail("Should have gotten an exception indicating that the given baudrate was not supported, but was " + e.getMessage());
            } else {
                exceptionCounter++;
            }
        }
        assertThat(exceptionCounter).isEqualTo(1);

        try {
            serialPort.getSioBaudrate(BaudrateValue.BAUDRATE_14400.value());
        } catch (ApplicationException e) {
            if (!e.getMessage().equals("Baudrate " + BaudrateValue.BAUDRATE_14400.value() + " is not supported by this driver.")) {
                fail("Should have gotten an exception indicating that the given baudrate was not supported, but was " + e.getMessage());
            } else {
                exceptionCounter++;
            }
        }
        assertThat(exceptionCounter).isEqualTo(2);

        try {
            serialPort.getSioBaudrate(BaudrateValue.BAUDRATE_28800.value());
        } catch (ApplicationException e) {
            if (!e.getMessage().equals("Baudrate " + BaudrateValue.BAUDRATE_28800.value() + " is not supported by this driver.")) {
                fail("Should have gotten an exception indicating that the given baudrate was not supported, but was " + e.getMessage());
            } else {
                exceptionCounter++;
            }
        }
        assertThat(exceptionCounter).isEqualTo(3);

        try {
            serialPort.getSioBaudrate(BaudrateValue.BAUDRATE_56000.value());
        } catch (ApplicationException e) {
            if (!e.getMessage().equals("Baudrate " + BaudrateValue.BAUDRATE_56000.value() + " is not supported by this driver.")) {
                fail("Should have gotten an exception indicating that the given baudrate was not supported, but was " + e.getMessage());
            } else {
                exceptionCounter++;
            }
        }
        assertThat(exceptionCounter).isEqualTo(4);

        try {
            serialPort.getSioBaudrate(BaudrateValue.BAUDRATE_7200.value());
        } catch (ApplicationException e) {
            if (!e.getMessage().equals("Baudrate " + BaudrateValue.BAUDRATE_7200.value() + " is not supported by this driver.")) {
                fail("Should have gotten an exception indicating that the given baudrate was not supported, but was " + e.getMessage());
            } else {
                exceptionCounter++;
            }
        }
        assertThat(exceptionCounter).isEqualTo(5);

        try {
            serialPort.getSioBaudrate(BaudrateValue.BAUDRATE_76800.value());
        } catch (ApplicationException e) {
            if (!e.getMessage().equals("Baudrate " + BaudrateValue.BAUDRATE_76800.value() + " is not supported by this driver.")) {
                fail("Should have gotten an exception indicating that the given baudrate was not supported, but was " + e.getMessage());
            } else {
                exceptionCounter++;
            }
        }
        assertThat(exceptionCounter).isEqualTo(6);

    }

    @Test(expected = ApplicationException.class)
    public void getInCorrectBaudRateTest() {
        SerialPortConfiguration configuration = mock(SerialPortConfiguration.class);
        SioSerialPort serialPort = new SioSerialPort(configuration);

        try {
            serialPort.getSioBaudrate(new BigDecimal("4531357.14546"));
        } catch (ApplicationException e) {
            // we should get a configuration mismatch exception, otherwise throw the exception so the TestFrameWork can handle this
            if (!e.getMessage().equals("Baudrate 4531357.14546 is not supported by this driver.")) {
                fail("Should have gotten an exception indicating that the given baudrate was not supported, but was " + e.getMessage());
            } else {
                throw e;
            }
        }
    }

//    @Test // this test can NOT run on Bamboo, only for local testing of your own ComPorts
    public void comPortTest() {
        SerialPortConfiguration configuration = new SerialPortConfiguration(
                "COM8",
                BaudrateValue.BAUDRATE_300,
                NrOfDataBits.valueFor(new BigDecimal(7)),
                NrOfStopBits.valueFor(BigDecimal.ONE),
                Parities.EVEN,
                FlowControl.NONE);

        SioSerialPort sioSerialPort = null;
        try {
            sioSerialPort = new SioSerialPort(configuration);
            sioSerialPort.openAndInit();

            sioSerialPort.updatePortConfiguration(configuration);

            sioSerialPort.getOutputStream().write("ATZ\r\n".getBytes());
            byte[] input = new byte[1024];
            sioSerialPort.getInputStream().read(input);
            System.out.println(new String(input));
            sioSerialPort.getOutputStream().write("ATS0=0E0V1\n".getBytes());
            sioSerialPort.getInputStream().read(input);
            System.out.println(new String(input));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (sioSerialPort != null) {
                sioSerialPort.close();
            }
        }
    }

}