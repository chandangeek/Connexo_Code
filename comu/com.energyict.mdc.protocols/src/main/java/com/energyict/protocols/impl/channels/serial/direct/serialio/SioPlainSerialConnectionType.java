package com.energyict.protocols.impl.channels.serial.direct.serialio;

import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.io.SerialComChannel;
import com.energyict.mdc.io.SerialComponentService;
import com.energyict.mdc.io.SerialPortConfiguration;
import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.dynamic.ConnectionProperty;

import com.energyict.protocols.impl.ConnectionTypeServiceImpl;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

/**
 * Provides an implementation for the {@link ConnectionType} interface for Serial communication.
 * <p/>
 * Copyrights EnergyICT
 * Date: 17/08/12
 * Time: 11:39
 */
public class SioPlainSerialConnectionType extends SioSerialConnectionType {

    @Inject
    public SioPlainSerialConnectionType(@Named(ConnectionTypeServiceImpl.SERIAL_PLAIN_GUICE_INJECTION_NAME)SerialComponentService serialComponentService) {
        super(serialComponentService);
    }

    @Override
    public SerialComChannel connect (List<ConnectionProperty> properties) throws ConnectionException {
        SerialPortConfiguration serialPortConfiguration = createSerialConfiguration(this.getComPortNameValue(), properties);
        return newSioSerialConnection(this.getSerialComponentService(), serialPortConfiguration);
    }

    protected SerialPortConfiguration createSerialConfiguration(String comPortName, List<ConnectionProperty> properties) {
        for (ConnectionProperty property : properties) {
            this.setProperty(property.getName(), property.getValue());
        }
        return new SerialPortConfiguration(
                        comPortName,
                        getBaudRateValue(),
                        getNrOfDataBitsValue(),
                        getNrOfStopBitsValue(),
                        getParityValue(),
                        getFlowControlValue());
    }

    @Override
    public String getVersion() {
        return "$Date: 2014-11-04 14:09:00 +0100 $";
    }

    @Override
    public void disconnect(ComChannel comChannel) throws ConnectionException {
        // No explicit disconnect for this SioSerialConnectionType
    }

}