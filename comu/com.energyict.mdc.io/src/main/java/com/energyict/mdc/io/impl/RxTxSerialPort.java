/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.io.impl;

import com.energyict.mdc.io.FlowControl;
import com.energyict.mdc.io.Parities;
import com.energyict.mdc.io.SerialPortConfiguration;
import com.energyict.mdc.io.SerialPortException;
import com.energyict.mdc.io.ServerSerialPort;
import com.energyict.mdc.io.SignalController;
import com.energyict.mdc.io.naming.SerialPortConfigurationPropertySpecNames;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class RxTxSerialPort implements ServerSerialPort {

    private static final int STOPBITS_1_5_UNSCALED_VALUE = 15;
    private static final int STOPBITS_2_UNSCALED_VALUE = 2;
    private static final int STOPBITS_1_UNSCALED_VALUE = 1;
    private static final Set<Integer> SUPPORTED_NUMBER_OF_DATABITS = new HashSet<>(Arrays.asList(SerialPort.DATABITS_5, SerialPort.DATABITS_6, SerialPort.DATABITS_7, SerialPort.DATABITS_8));

    private SerialPortConfiguration serialPortConfiguration;

    private SerialPort serialPort;
    private InputStream inputStream;
    private OutputStream outputStream;

    public RxTxSerialPort(SerialPortConfiguration serialPortConfiguration) {
        this.serialPortConfiguration = serialPortConfiguration;
    }

    /**
     * Opens a new SerialPort and initialize with the given configuration
     *
     * @throws SerialPortException Thrown when the port does not exist on the ComServer or
     *                             when the port is used by another process
     */
    @Override
    public void openAndInit() {
        CommPortIdentifier portIdentifier;
        try {
            portIdentifier = CommPortIdentifier.getPortIdentifier(serialPortConfiguration.getComPortName());
        } catch (NoSuchPortException e) {
            throw new SerialPortException(MessageSeeds.SERIAL_PORT_DOES_NOT_EXIST, serialPortConfiguration.getComPortName());
        }
        try {
            serialPort = (SerialPort) portIdentifier.open(this.getClass().getName(), serialPortConfiguration.getSerialPortOpenTimeOut().intValue());
        } catch (PortInUseException e) {
            throw new SerialPortException(MessageSeeds.SERIAL_PORT_IS_IN_USE, serialPortConfiguration.getComPortName(), e.currentOwner);
        }
        setSerialPortParameters();
    }

    private void setSerialPortParameters() {
        try {
            serialPort.setSerialPortParams(serialPortConfiguration.getBaudrate().value().intValue(),
                    getRxTxNrOfDataBits(serialPortConfiguration.getNrOfDataBits().value()),
                    getRxTxNrOfStopBits(serialPortConfiguration.getNrOfStopBits().value()),
                    getRxTxParity(serialPortConfiguration.getParity().value()));
        } catch (UnsupportedCommOperationException e) {
            throw new SerialPortException(MessageSeeds.SERIAL_PORT_LIBRARY_EXCEPTION, e);
        }
        try {
            setFlowControlMode(serialPortConfiguration.getFlowControl().value());
        } catch (UnsupportedCommOperationException e) {
            throw new SerialPortException(MessageSeeds.SERIAL_PORT_LIBRARY_EXCEPTION, e);
        }
        try {
            serialPort.enableReceiveTimeout(serialPortConfiguration.getSerialPortReadTimeOut().intValue());
        } catch (UnsupportedCommOperationException e) {
            throw new SerialPortException(MessageSeeds.SERIAL_PORT_LIBRARY_EXCEPTION, e);
        }
    }

    /**
     * Setting the FlowControl Mode.
     *
     * @param flowControl the configured mode
     * @throws UnsupportedCommOperationException
     *          if a mode is not supported by the underlying OS
     */
    protected void setFlowControlMode(String flowControl) throws UnsupportedCommOperationException {
        if (FlowControl.NONE.value().equals(flowControl)) {
            serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
            getSerialPortSignalController().setRTS(true);
            getSerialPortSignalController().setDTR(true);
        } else if (FlowControl.RTSCTS.value().equals(flowControl)) {
            serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN | SerialPort.FLOWCONTROL_RTSCTS_OUT);
            getSerialPortSignalController().setRTS(true);

//            //TODO need to check if the DTRDSR is supported! and how it actually works
//        } else if (FlowControl.DTRDSR.getFlowControl().equals(flowControl)) {
//            serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
//            serialPort.setRTS(true);
//            serialPort.setDTR(true);
        } else if (FlowControl.XONXOFF.value().equals(flowControl)) {
            serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_XONXOFF_IN | SerialPort.FLOWCONTROL_XONXOFF_OUT);
            getSerialPortSignalController().setRTS(true);
            getSerialPortSignalController().setDTR(true);
        } else {
            throw new SerialPortException(MessageSeeds.SERIAL_PORT_CONFIGURATION_MISMATCH, SerialPortConfigurationPropertySpecNames.FLOW_CONTROL, flowControl);
        }
    }

    protected int getRxTxParity(String configParity) {
        if (Parities.NONE.value().equals(configParity)) {
            return SerialPort.PARITY_NONE;
        } else if (Parities.EVEN.value().equals(configParity)) {
            return SerialPort.PARITY_EVEN;
        } else if (Parities.ODD.value().equals(configParity)) {
            return SerialPort.PARITY_ODD;
        } else if (Parities.MARK.value().equals(configParity)) {
            return SerialPort.PARITY_MARK;
        } else if (Parities.SPACE.value().equals(configParity)) {
            return SerialPort.PARITY_SPACE;
        }
        throw new SerialPortException(MessageSeeds.SERIAL_PORT_CONFIGURATION_MISMATCH, SerialPortConfigurationPropertySpecNames.PARITY, configParity);
    }

    protected int getRxTxNrOfStopBits(BigDecimal stopBits) {
        switch (stopBits.unscaledValue().intValue()) {   // we use the unscaled integer so we can get the 1.5 value
            case STOPBITS_1_UNSCALED_VALUE:
                return SerialPort.STOPBITS_1;
            case STOPBITS_2_UNSCALED_VALUE:
                return SerialPort.STOPBITS_2;
            case STOPBITS_1_5_UNSCALED_VALUE:
                return SerialPort.STOPBITS_1_5;
        }
        throw new SerialPortException(MessageSeeds.SERIAL_PORT_CONFIGURATION_MISMATCH, SerialPortConfigurationPropertySpecNames.NR_OF_STOP_BITS, stopBits.toString());
    }

    protected int getRxTxNrOfDataBits(BigDecimal dataBits) {
        if (SUPPORTED_NUMBER_OF_DATABITS.contains(dataBits.intValue())) {
            return dataBits.intValue();
        }
        else {
            throw new SerialPortException(MessageSeeds.SERIAL_PORT_CONFIGURATION_MISMATCH, SerialPortConfigurationPropertySpecNames.NR_OF_DATA_BITS, dataBits.toString());
        }
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
                return this.serialPort.getInputStream();
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
                return this.serialPort.getOutputStream();
            } catch (IOException e) {
                throw new SerialPortException(MessageSeeds.SERIAL_PORT_LIBRARY_EXCEPTION, e);
            }
        }
        return this.outputStream;
    }

    @Override
    public void close() {
        if (this.serialPort != null) {
            this.serialPort.close();
        }
    }

    @Override
    public void updatePortConfiguration(SerialPortConfiguration serialPortConfiguration) {
        this.serialPortConfiguration = serialPortConfiguration;
        setSerialPortParameters();
    }

    @Override
    public SerialPortConfiguration getSerialPortConfiguration() {
        return this.serialPortConfiguration;
    }

    @Override
    public SignalController getSerialPortSignalController() {
        return new RxTxSignalController(this.serialPort);
    }

    /**
     * Should generally only be used for testing purposes
     *
     * @param serialPort the serialPort to set
     */
    protected void setSerialPort(SerialPort serialPort) {
        this.serialPort = serialPort;
    }

}