package com.energyict.protocols.impl.channels.serial.direct.rxtx;

import com.energyict.mdc.io.SerialComChannel;
import com.energyict.mdc.io.SerialComponentService;
import com.energyict.mdc.io.SerialPortConfiguration;
import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.dynamic.ConnectionProperty;

import com.energyict.protocols.impl.channels.serial.AbstractSerialConnectionType;

import java.util.List;

/**
 * Provides an implementation for the {@link ConnectionType} interface for Serial communication.
 * <p/>
 * Copyrights EnergyICT
 * Date: 13/08/12
 * Time: 11:14
 */
public abstract class RxTxSerialConnectionType extends AbstractSerialConnectionType {

    protected RxTxSerialConnectionType(SerialComponentService serialComponentService) {
        super(serialComponentService);
    }

    @Override
    public SerialComChannel connect (List<ConnectionProperty> properties) throws ConnectionException {
        for (ConnectionProperty property : properties) {
            this.setProperty(property.getName(), property.getValue());
        }
        SerialPortConfiguration serialPortConfiguration =
                new SerialPortConfiguration(
                        this.getComPortNameValue(),
                        getBaudRateValue(),
                        getNrOfDataBitsValue(),
                        getNrOfStopBitsValue(),
                        getParityValue(),
                        getFlowControlValue());
        if (getPortOpenTimeOutValue() != null) {
            serialPortConfiguration.setSerialPortOpenTimeOut(getPortOpenTimeOutValue());
        }
        if (getPortReadTimeOutValue() != null) {
            serialPortConfiguration.setSerialPortReadTimeOut(getPortReadTimeOutValue());
        }
        return newRxTxSerialConnection(this.getSerialComponentService(), serialPortConfiguration);
    }

}