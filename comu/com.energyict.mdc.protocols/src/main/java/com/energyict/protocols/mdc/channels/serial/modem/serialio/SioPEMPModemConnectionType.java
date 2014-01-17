package com.energyict.protocols.mdc.channels.serial.modem.serialio;

import com.energyict.protocols.mdc.channels.serial.SerialComChannel;
import com.energyict.mdc.protocol.api.ComChannel;
import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.dynamic.ConnectionProperty;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.protocols.mdc.channels.serial.direct.serialio.SioSerialConnectionType;
import com.energyict.protocols.mdc.channels.serial.modem.PEMPModemComponent;
import com.energyict.protocols.mdc.channels.serial.modem.TypedPEMPModemProperties;
import com.energyict.protocols.mdc.channels.serial.modem.TypedPaknetModemProperties;

import java.util.List;

/**
 * <p>Provides an implementation for the {@link ConnectionType}
 * interface for Serial PEMP communication, using the Sio library.</p>
 *
 * @author sva
 * @since 23/04/13 - 11:53
 */
public class SioPEMPModemConnectionType extends SioSerialConnectionType {

    private PEMPModemComponent pempModemComponent;

    @Override
    public ComChannel connect (List<ConnectionProperty> properties) throws ConnectionException {

        pempModemComponent = new PEMPModemComponent(new TypedPEMPModemProperties(properties));
        /*
       create the serial ComChannel and set all property values
        */
        ComChannel comChannel = super.connect(properties);
        try {
            pempModemComponent.connect(this.getComPortNameValue(), (SerialComChannel) comChannel);
        } catch (Exception e) {
            comChannel.close(); // need to properly close the comChannel, otherwise the port will always be occupied
            throw new ConnectionException(e);
        }
        return comChannel;
    }

    @Override
    public void disconnect(ComChannel comChannel) throws ConnectionException {
        if (pempModemComponent != null) {
            pempModemComponent.disconnectModem(comChannel);
        }
    }

    @Override
    protected void addPropertySpecs (List<PropertySpec> propertySpecs) {
        super.addPropertySpecs(propertySpecs);
        propertySpecs.addAll(new TypedPaknetModemProperties().getPropertySpecs());
    }

    @Override
    public PropertySpec getPropertySpec(String name) {
        PropertySpec propertySpec = super.getPropertySpec(name);
        if (propertySpec == null) {
            return new TypedPaknetModemProperties().getPropertySpec(name);
        }
        return propertySpec;
    }

}