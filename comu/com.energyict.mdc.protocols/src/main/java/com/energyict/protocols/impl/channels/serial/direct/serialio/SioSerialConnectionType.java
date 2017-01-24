package com.energyict.protocols.impl.channels.serial.direct.serialio;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.channels.serial.SerialPortConfiguration;
import com.energyict.mdc.io.SerialComponentService;
import com.energyict.mdc.protocol.SerialPortComChannel;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.dynamic.ConnectionProperty;
import com.energyict.protocol.exceptions.ConnectionException;
import com.energyict.protocols.impl.channels.serial.AbstractSerialConnectionType;

import java.util.List;

/**
 * Provides an implementation for the {@link ConnectionType} interface for Serial communication.
 * <p/>
 * Copyrights EnergyICT
 * Date: 17/08/12
 * Time: 11:39
 */
public abstract class SioSerialConnectionType extends AbstractSerialConnectionType {

    public SioSerialConnectionType(String customPropertySetId, SerialComponentService serialComponentService, Thesaurus thesaurus) {
        super(customPropertySetId, serialComponentService, thesaurus);
    }

    @Override
    public SerialPortComChannel connect (List<ConnectionProperty> properties) throws ConnectionException {
        SerialPortConfiguration serialPortConfiguration = createSerialConfiguration(properties);
        return newSioSerialConnection(this.getSerialComponentService(), serialPortConfiguration);
    }

    protected SerialPortConfiguration createSerialConfiguration(List<ConnectionProperty> properties) {
        for (ConnectionProperty property : properties) {
            this.setProperty(property.getName(), property.getValue());
        }
        return new SerialPortConfiguration(
                        this.getComPortNameValue(),
                        getBaudRateValue(),
                        getNrOfDataBitsValue(),
                        getNrOfStopBitsValue(),
                        getParityValue(),
                        getFlowControlValue());
    }

}