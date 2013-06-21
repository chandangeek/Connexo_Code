package com.energyict.mdc.channels.serial.modem.serialio;

import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.ManagerFactory;
import com.energyict.mdc.channels.serial.SerialComChannel;
import com.energyict.mdc.channels.serial.direct.serialio.SioSerialConnectionType;
import com.energyict.mdc.channels.serial.modem.PaknetModemComponent;
import com.energyict.mdc.channels.serial.modem.TypedPaknetModemProperties;
import com.energyict.mdc.ports.ComPort;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.ConnectionException;
import com.energyict.mdc.tasks.ConnectionTaskProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides an implementation for the {@link com.energyict.mdc.tasks.ConnectionType}
 * interface for Serial PAKNET-Modem communication, using the Sio library.
 *
 * @author sva
 * @since 14/04/2013 - 10:50
 */
public class SioPaknetModemConnectionType extends SioSerialConnectionType {

    private PaknetModemComponent paknetModemComponent;

    @Override
    public ComChannel connect(ComPort comPort, List<ConnectionTaskProperty> properties) throws ConnectionException {

        paknetModemComponent = ManagerFactory.getCurrent().getSerialComponentFactory().newPaknetModemComponent(new TypedPaknetModemProperties(properties));
        /*
        create the serial ComChannel and set all property values
         */
        ComChannel comChannel = super.connect(comPort, properties);
        try {
            paknetModemComponent.connect(comPort.getName(), (SerialComChannel) comChannel);
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
    public List<PropertySpec> getOptionalProperties() {
        List<PropertySpec> allOptionalProperties = new ArrayList<PropertySpec>();
        allOptionalProperties.addAll(super.getOptionalProperties());    // need to create a new list because the super returns a fixed list
        allOptionalProperties.addAll(new TypedPaknetModemProperties().getOptionalProperties());
        return allOptionalProperties;
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        List<PropertySpec> requiredProperties = new ArrayList<PropertySpec>();
        requiredProperties.addAll(super.getRequiredProperties());  // need to create a new list because the super returns a fixed list
        requiredProperties.addAll(new TypedPaknetModemProperties().getRequiredProperties());
        return requiredProperties;
    }

    @Override
    public boolean isRequiredProperty(String name) {
        return super.isRequiredProperty(name) || new TypedPaknetModemProperties().isRequiredProperty(name);
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