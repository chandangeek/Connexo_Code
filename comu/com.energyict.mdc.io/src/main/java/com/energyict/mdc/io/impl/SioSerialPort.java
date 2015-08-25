package com.energyict.mdc.io.impl;

import com.energyict.mdc.io.BaudrateValue;
import com.energyict.mdc.io.ConnectionCommunicationException;
import com.energyict.mdc.io.FlowControl;
import com.energyict.mdc.io.Parities;
import com.energyict.mdc.io.SerialPortConfiguration;
import com.energyict.mdc.io.SerialPortException;
import com.energyict.mdc.io.ServerSerialPort;
import com.energyict.mdc.io.SignalController;

import Serialio.SerInputStream;
import Serialio.SerOutputStream;
import Serialio.SerialConfig;
import Serialio.SerialPort;
import Serialio.SerialPortLocal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;

/**
 * A hardware SerialPort driven by the SerialIO java library.
 * <p/>
 * Copyrights EnergyICT
 * Date: 17/08/12
 * Time: 11:43
 *
 * @see <a href="http://www.serialio.com/products/serialport/serialport.php">SerialIO Homepage</a>
 */
public class SioSerialPort implements ServerSerialPort {

    private static final int SERIAL_CONFIG_1_STOP_BIT_UNSCALED_VALUE = 1;
    private static final int SERIAL_CONFIG_2_STOP_BITS_UNSCALED_VALUE = 2;
    private static final int SERIAL_CONFIG_5_DATA_BITS_MARKER = 5;
    private static final int SERIAL_CONFIG_6_DATA_BITS_MARKER = 6;
    private static final int SERIAL_CONFIG_7_DATA_BITS_MARKER = 7;
    private static final int SERIAL_CONFIG_8_DATA_BITS_MARKER = 8;
    private SerialPortConfiguration serialPortConfiguration;

    private SerialPort serialPort;
    private InputStream inputStream;
    private OutputStream outputStream;

    public SioSerialPort(SerialPortConfiguration serialPortConfiguration) {
        this.serialPortConfiguration = serialPortConfiguration;
    }

    @Override
    public void openAndInit() {
        try {
            this.serialPort = new SerialPortLocal(createSerialConfig());
        } catch (IOException e) {
            throw new SerialPortException(MessageSeeds.SERIAL_PORT_LIBRARY_EXCEPTION, e);
        }
        updateFlags();
    }

    private void updateFlags() {
        // we set the initial flags to true because most modems require this!
        getSerialPortSignalController().setRTS(true);
        getSerialPortSignalController().setDTR(true);

    }

    private SerialConfig createSerialConfig() {
        SerialConfig serialConfig;
        if (this.serialPort != null) {   // need to use the same configuration object if we update the settings
            serialConfig = this.serialPort.getConfig();
        } else {
            serialConfig = new SerialConfig(serialPortConfiguration.getComPortName());
        }
        serialConfig.setBitRate(getSioBaudrate(serialPortConfiguration.getBaudrate().value()));
        serialConfig.setDataBits(getSioDataBits(serialPortConfiguration.getNrOfDataBits().value()));
        serialConfig.setStopBits(getSioStopBits(serialPortConfiguration.getNrOfStopBits().value()));
        serialConfig.setParity(getSioParity(serialPortConfiguration.getParity().value()));
        serialConfig.setHandshake(getSioFlowControl(serialPortConfiguration.getFlowControl().value()));
        return serialConfig;
    }

    protected int getSioFlowControl(String flowControl) {
        if (FlowControl.NONE.value().equals(flowControl)) {
            return SerialConfig.HS_NONE;
        } else if (FlowControl.RTSCTS.value().equals(flowControl)) {
            return SerialConfig.HS_CTSRTS;
        } else if (FlowControl.DTRDSR.value().equals(flowControl)) {
            return SerialConfig.HS_DSRDTR;
        } else if (FlowControl.XONXOFF.value().equals(flowControl)) {
            return SerialConfig.HS_XONXOFF;
        } else {
            throw new SerialPortException(MessageSeeds.SERIAL_PORT_CONFIGURATION_MISMATCH, SerialPortConfiguration.FLOW_CONTROL_NAME, flowControl);
        }
    }

    protected int getSioParity(String parity) {
        if (Parities.NONE.value().equals(parity)) {
            return SerialConfig.PY_NONE;
        } else if (Parities.EVEN.value().equals(parity)) {
            return SerialConfig.PY_EVEN;
        } else if (Parities.ODD.value().equals(parity)) {
            return SerialConfig.PY_ODD;
        } else if (Parities.MARK.value().equals(parity)) {
            return SerialConfig.PY_MARK;
        } else if (Parities.SPACE.value().equals(parity)) {
            return SerialConfig.PY_SPACE;
        } else {
            throw new SerialPortException(MessageSeeds.SERIAL_PORT_CONFIGURATION_MISMATCH, SerialPortConfiguration.PARITY_NAME, parity);
        }
    }

    protected int getSioStopBits(BigDecimal nrOfStopBits) {
        switch (nrOfStopBits.unscaledValue().intValue()) { // the unscaled value can represent 15 as 1,5 stop bits (although it is not possible with this library)
            case SERIAL_CONFIG_1_STOP_BIT_UNSCALED_VALUE:
                return SerialConfig.ST_1BITS;
            case SERIAL_CONFIG_2_STOP_BITS_UNSCALED_VALUE:
                return SerialConfig.ST_2BITS;
            default:
                throw new SerialPortException(MessageSeeds.SERIAL_PORT_CONFIGURATION_MISMATCH, SerialPortConfiguration.NR_OF_STOP_BITS_NAME, nrOfStopBits.toString());
        }
    }

    protected int getSioDataBits(BigDecimal nrOfDataBits) {
        switch (nrOfDataBits.intValue()) {
            case SERIAL_CONFIG_5_DATA_BITS_MARKER:
                return SerialConfig.LN_5BITS;
            case SERIAL_CONFIG_6_DATA_BITS_MARKER:
                return SerialConfig.LN_6BITS;
            case SERIAL_CONFIG_7_DATA_BITS_MARKER:
                return SerialConfig.LN_7BITS;
            case SERIAL_CONFIG_8_DATA_BITS_MARKER:
                return SerialConfig.LN_8BITS;
            default:
                throw new SerialPortException(MessageSeeds.SERIAL_PORT_CONFIGURATION_MISMATCH, SerialPortConfiguration.NR_OF_DATA_BITS_NAME, nrOfDataBits.toString());
        }
    }

    protected int getSioBaudrate(BigDecimal baudrate) {
        return BaudrateValue.getSioBaudrateFor(baudrate);
    }

    @Override
    public void setSerialInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public void setSerialOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    @Override
    public InputStream getInputStream() {
        if (this.inputStream == null && this.serialPort != null) {
            try {
                return new SerInputStream(this.serialPort);
            } catch (IOException e) {
                throw new SerialPortException(MessageSeeds.SERIAL_PORT_LIBRARY_EXCEPTION, e);
            }
        }
        return this.inputStream;
    }

    @Override
    public OutputStream getOutputStream() {
        if (this.outputStream == null && this.serialPort != null) {
            try {
                return new SerOutputStream(this.serialPort);
            } catch (IOException e) {
                throw new SerialPortException(MessageSeeds.SERIAL_PORT_LIBRARY_EXCEPTION, e);
            }
        }
        return this.outputStream;
    }

    @Override
    public void close() {
        if (this.serialPort != null) {
            try {
                this.serialPort.close();
            } catch (IOException e) {
                throw new ConnectionCommunicationException(MessageSeeds.UNEXPECTED_IO_EXCEPTION, e);
            }
        }
    }

    @Override
    public void updatePortConfiguration(SerialPortConfiguration serialPortConfiguration) {
        this.serialPortConfiguration = serialPortConfiguration;
        try {
            this.serialPort.configure(createSerialConfig());
        } catch (IOException e) {
            throw new SerialPortException(MessageSeeds.SERIAL_PORT_LIBRARY_EXCEPTION, e);
        }
        updateFlags();
    }

    @Override
    public SerialPortConfiguration getSerialPortConfiguration() {
        return this.serialPortConfiguration;
    }

    @Override
    public SignalController getSerialPortSignalController() {
        return new SioSignalController(this.serialPort);
    }

}