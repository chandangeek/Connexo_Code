package com.energyict.mdc.channels.serial.direct.serialio;

import com.energyict.mdc.channels.serial.BaudrateValue;
import com.energyict.mdc.channels.serial.FlowControl;
import com.energyict.mdc.channels.serial.NrOfDataBits;
import com.energyict.mdc.channels.serial.NrOfStopBits;
import com.energyict.mdc.channels.serial.Parities;
import com.energyict.mdc.channels.serial.SerialPortConfiguration;
import com.energyict.mdc.exceptions.SerialPortException;

import Serialio.SerialConfig;
import com.energyict.cbo.ApplicationException;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

/**
 * Tests for the {@link com.energyict.mdc.channels.serial.direct.serialio.SioSerialPort} component
 * <p/>
 * Copyrights EnergyICT
 * Date: 17/08/12
 * Time: 14:45
 */
public class SioSerialPortTest {

    @Test
    public void getCorrectNrOfDataBitsTest() {
        SerialPortConfiguration configuration = mock(SerialPortConfiguration.class);
        SioSerialPort serialPort = new SioSerialPort(configuration);

        assertEquals(SerialConfig.LN_5BITS, serialPort.getSioDataBits(new BigDecimal(5)));
        assertEquals(SerialConfig.LN_6BITS, serialPort.getSioDataBits(new BigDecimal("6")));
        assertEquals(SerialConfig.LN_7BITS, serialPort.getSioDataBits(new BigDecimal(new BigInteger("7"), 0)));
        assertEquals(SerialConfig.LN_8BITS, serialPort.getSioDataBits(new BigDecimal(8)));
    }

    @Test
    public void getIncorrectNrOfDataBitsTest() {
        try {
            SerialPortConfiguration configuration = mock(SerialPortConfiguration.class);
            SioSerialPort serialPort = new SioSerialPort(configuration);
            serialPort.getSioDataBits(new BigDecimal("100.156"));
        } catch (SerialPortException e) {
            // we should get a configuration mismatch exception, otherwise throw the exception so the TestFrameWork can handle this
            if (!e.getMessageId().equals("CSM-COM-203")) {
                throw e;
            }
        }
    }

    @Test
    public void getCorrectNrOfStopBitsTest() {
        SerialPortConfiguration configuration = mock(SerialPortConfiguration.class);
        SioSerialPort serialPort = new SioSerialPort(configuration);

        assertEquals(SerialConfig.ST_1BITS, serialPort.getSioStopBits(new BigDecimal(1)));
        assertEquals(SerialConfig.ST_2BITS, serialPort.getSioStopBits(new BigDecimal("2")));
    }

    @Test
    public void unsupportedOneAndHalfStopBitsTest() {
        SerialPortConfiguration configuration = mock(SerialPortConfiguration.class);
        SioSerialPort serialPort = new SioSerialPort(configuration);

        try {
            serialPort.getSioStopBits(new BigDecimal("1.5"));
        } catch (SerialPortException e) {
            // we should get a configuration mismatch exception, otherwise throw the exception so the TestFrameWork can handle this
            if (!e.getMessageId().equals("CSM-COM-203")) {
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
            if (!e.getMessageId().equals("CSM-COM-203")) {
                throw e;
            }
        }
    }

    @Test
    public void getCorrectParityTest() {
        SerialPortConfiguration configuration = mock(SerialPortConfiguration.class);
        SioSerialPort serialPort = new SioSerialPort(configuration);

        assertEquals(SerialConfig.PY_NONE, serialPort.getSioParity(Parities.NONE.getParity()));
        assertEquals(SerialConfig.PY_ODD, serialPort.getSioParity(Parities.ODD.getParity()));
        assertEquals(SerialConfig.PY_EVEN, serialPort.getSioParity(Parities.EVEN.getParity()));
        assertEquals(SerialConfig.PY_MARK, serialPort.getSioParity(Parities.MARK.getParity()));
        assertEquals(SerialConfig.PY_SPACE, serialPort.getSioParity(Parities.SPACE.getParity()));
    }

    @Test
    public void getIncorrectParityTest() {
        SerialPortConfiguration configuration = mock(SerialPortConfiguration.class);
        SioSerialPort serialPort = new SioSerialPort(configuration);

        try {
            serialPort.getSioParity("INCorrecTParItY");
        } catch (SerialPortException e) {
            // we should get a configuration mismatch exception, otherwise throw the exception so the TestFrameWork can handle this
            if (!e.getMessageId().equals("CSM-COM-203")) {
                throw e;
            }
        }
    }

    @Test
    public void getCorrectFlowControlTest() {
        SerialPortConfiguration configuration = mock(SerialPortConfiguration.class);
        SioSerialPort serialPort = new SioSerialPort(configuration);

        assertEquals(SerialConfig.HS_NONE, serialPort.getSioFlowControl(FlowControl.NONE.getFlowControl()));
        assertEquals(SerialConfig.HS_DSRDTR, serialPort.getSioFlowControl(FlowControl.DTRDSR.getFlowControl()));
        assertEquals(SerialConfig.HS_CTSRTS, serialPort.getSioFlowControl(FlowControl.RTSCTS.getFlowControl()));
        assertEquals(SerialConfig.HS_XONXOFF, serialPort.getSioFlowControl(FlowControl.XONXOFF.getFlowControl()));
    }

    @Test
    public void getInCorrectFlowControlTest() {
        SerialPortConfiguration configuration = mock(SerialPortConfiguration.class);
        SioSerialPort serialPort = new SioSerialPort(configuration);

        try {
            serialPort.getSioFlowControl("InCORRectFloWContRoL");
        } catch (SerialPortException e) {
            // we should get a configuration mismatch exception, otherwise throw the exception so the TestFrameWork can handle this
            if (!e.getMessageId().equals("CSM-COM-203")) {
                throw e;
            }
        }
    }

    @Test
    public void setBaudRateTest() {
        SerialPortConfiguration configuration = mock(SerialPortConfiguration.class);
        SioSerialPort serialPort = new SioSerialPort(configuration);

        assertEquals(SerialConfig.BR_150, serialPort.getSioBaudrate(BaudrateValue.BAUDRATE_150.getBaudrate()));
        assertEquals(SerialConfig.BR_300, serialPort.getSioBaudrate(BaudrateValue.BAUDRATE_300.getBaudrate()));
        assertEquals(SerialConfig.BR_600, serialPort.getSioBaudrate(BaudrateValue.BAUDRATE_600.getBaudrate()));
        assertEquals(SerialConfig.BR_1200, serialPort.getSioBaudrate(BaudrateValue.BAUDRATE_1200.getBaudrate()));
        assertEquals(SerialConfig.BR_2400, serialPort.getSioBaudrate(BaudrateValue.BAUDRATE_2400.getBaudrate()));
        assertEquals(SerialConfig.BR_4800, serialPort.getSioBaudrate(BaudrateValue.BAUDRATE_4800.getBaudrate()));
        assertEquals(SerialConfig.BR_9600, serialPort.getSioBaudrate(BaudrateValue.BAUDRATE_9600.getBaudrate()));
        assertEquals(SerialConfig.BR_19200, serialPort.getSioBaudrate(BaudrateValue.BAUDRATE_19200.getBaudrate()));
        assertEquals(SerialConfig.BR_38400, serialPort.getSioBaudrate(BaudrateValue.BAUDRATE_38400.getBaudrate()));
        assertEquals(SerialConfig.BR_57600, serialPort.getSioBaudrate(BaudrateValue.BAUDRATE_57600.getBaudrate()));
        assertEquals(SerialConfig.BR_115200, serialPort.getSioBaudrate(BaudrateValue.BAUDRATE_115200.getBaudrate()));
        assertEquals(SerialConfig.BR_230400, serialPort.getSioBaudrate(BaudrateValue.BAUDRATE_230400.getBaudrate()));
        assertEquals(SerialConfig.BR_460800, serialPort.getSioBaudrate(BaudrateValue.BAUDRATE_460800.getBaudrate()));
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
            serialPort.getSioBaudrate(BaudrateValue.BAUDRATE_1800.getBaudrate());
        } catch (ApplicationException e) {
            if (!e.getMessage().equals("Baudrate " + BaudrateValue.BAUDRATE_1800.getBaudrate() + " is not supported by this driver.")) {
                fail("Should have gotten an exception indicating that the given baudrate was not supported, but was " + e.getMessage());
            } else {
                exceptionCounter++;
            }
        }
        assertThat(exceptionCounter).isEqualTo(1);

        try {
            serialPort.getSioBaudrate(BaudrateValue.BAUDRATE_14400.getBaudrate());
        } catch (ApplicationException e) {
            if (!e.getMessage().equals("Baudrate " + BaudrateValue.BAUDRATE_14400.getBaudrate() + " is not supported by this driver.")) {
                fail("Should have gotten an exception indicating that the given baudrate was not supported, but was " + e.getMessage());
            } else {
                exceptionCounter++;
            }
        }
        assertThat(exceptionCounter).isEqualTo(2);

        try {
            serialPort.getSioBaudrate(BaudrateValue.BAUDRATE_28800.getBaudrate());
        } catch (ApplicationException e) {
            if (!e.getMessage().equals("Baudrate " + BaudrateValue.BAUDRATE_28800.getBaudrate() + " is not supported by this driver.")) {
                fail("Should have gotten an exception indicating that the given baudrate was not supported, but was " + e.getMessage());
            } else {
                exceptionCounter++;
            }
        }
        assertThat(exceptionCounter).isEqualTo(3);

        try {
            serialPort.getSioBaudrate(BaudrateValue.BAUDRATE_56000.getBaudrate());
        } catch (ApplicationException e) {
            if (!e.getMessage().equals("Baudrate " + BaudrateValue.BAUDRATE_56000.getBaudrate() + " is not supported by this driver.")) {
                fail("Should have gotten an exception indicating that the given baudrate was not supported, but was " + e.getMessage());
            } else {
                exceptionCounter++;
            }
        }
        assertThat(exceptionCounter).isEqualTo(4);

        try {
            serialPort.getSioBaudrate(BaudrateValue.BAUDRATE_7200.getBaudrate());
        } catch (ApplicationException e) {
            if (!e.getMessage().equals("Baudrate " + BaudrateValue.BAUDRATE_7200.getBaudrate() + " is not supported by this driver.")) {
                fail("Should have gotten an exception indicating that the given baudrate was not supported, but was " + e.getMessage());
            } else {
                exceptionCounter++;
            }
        }
        assertThat(exceptionCounter).isEqualTo(5);

        try {
            serialPort.getSioBaudrate(BaudrateValue.BAUDRATE_76800.getBaudrate());
        } catch (ApplicationException e) {
            if (!e.getMessage().equals("Baudrate " + BaudrateValue.BAUDRATE_76800.getBaudrate() + " is not supported by this driver.")) {
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

//    @Test // this test can NOT run on Bamboo, only for local testing of you own ComPorts
    public void comPortTest() {
        SerialPortConfiguration configuration = new SerialPortConfiguration(
                "COM8",
                BaudrateValue.BAUDRATE_300,
                NrOfDataBits.valueFor(new BigDecimal(7)),
                NrOfStopBits.valueFor(new BigDecimal(1)),
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
