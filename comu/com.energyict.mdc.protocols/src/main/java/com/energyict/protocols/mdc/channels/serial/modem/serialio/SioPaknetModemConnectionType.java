package com.energyict.protocols.mdc.channels.serial.modem.serialio;

import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.protocol.api.ComChannel;
import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.dynamic.ConnectionProperty;
import com.energyict.protocols.mdc.channels.serial.SerialComChannel;
import com.energyict.protocols.mdc.channels.serial.direct.serialio.SioSerialConnectionType;
import com.energyict.protocols.mdc.channels.serial.modem.PaknetModemComponent;
import com.energyict.protocols.mdc.channels.serial.modem.TypedPaknetModemProperties;

import java.util.List;

/**
 * Provides an implementation for the {@link ConnectionType}
 * interface for Serial PAKNET-Modem communication, using the Sio library.
 *
 * @author sva
 * @since 14/04/2013 - 10:50
 */
public class SioPaknetModemConnectionType extends SioSerialConnectionType {

    private PaknetModemComponent paknetModemComponent;

    @Override
    public ComChannel connect (List<ConnectionProperty> properties) throws ConnectionException {

        paknetModemComponent = new PaknetModemComponent(new TypedPaknetModemProperties(properties));
        /*
        create the serial ComChannel and set all property values
         */
        ComChannel comChannel = super.connect(properties);
        try {
            paknetModemComponent.connect(this.getComPortNameValue(), (SerialComChannel) comChannel);
        } catch (Exception e) {
            comChannel.close(); // need to properly close the comChannel, otherwise the port will always be occupied
            throw new ConnectionException(e);
        }
        return comChannel;
    }

    @Override
    public void disconnect(ComChannel comChannel) throws ConnectionException {
        if (paknetModemComponent != null) {
            paknetModemComponent.disconnectModem(comChannel);
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