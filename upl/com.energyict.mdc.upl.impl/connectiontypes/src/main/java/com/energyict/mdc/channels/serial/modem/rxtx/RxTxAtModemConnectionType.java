package com.energyict.mdc.channels.serial.modem.rxtx;

import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.ManagerFactory;
import com.energyict.mdc.channels.serial.direct.rxtx.RxTxSerialConnectionType;
import com.energyict.mdc.channels.serial.modem.AtModemComponent;
import com.energyict.mdc.channels.serial.modem.TypedAtModemProperties;
import com.energyict.mdc.exceptions.ModemException;
import com.energyict.mdc.ports.ComPort;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.ConnectionException;
import com.energyict.mdc.tasks.ConnectionTaskProperty;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides an implementation for the {@link com.energyict.mdc.tasks.ConnectionType}
 * interface for Serial AT-Modem communication, using the RxTx library.
 * <p/>
 * Copyrights EnergyICT
 * Date: 20/11/12
 * Time: 16:22
 */
@XmlRootElement
public class RxTxAtModemConnectionType extends RxTxSerialConnectionType {

    private AtModemComponent atModemComponent;

    @Override
    public ComChannel connect(ComPort comPort, List<ConnectionTaskProperty> properties) throws ConnectionException {

        this.atModemComponent = ManagerFactory.getCurrent().getSerialComponentFactory().newAtModemComponent(new TypedAtModemProperties(properties));
        /*
        create the serial ComChannel and set all property values
         */
        ComChannel comChannel = super.connect(comPort, properties);
        try {
            atModemComponent.connect(comPort.getName(), comChannel);
        } catch (ModemException e) {
            comChannel.close(); // need to properly close the comChannel, otherwise the port will always be occupied
            throw new ConnectionException(e);
        }
        return comChannel;
    }

    @Override
    public void disconnect(ComChannel comChannel) throws ConnectionException {
        if (this.atModemComponent != null) {
            this.atModemComponent.disconnectModem(comChannel);
        }
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        List<PropertySpec> allOptionalProperties = new ArrayList<PropertySpec>();
        allOptionalProperties.addAll(super.getOptionalProperties());    // need to create a new list because the super returns a fixed list
        allOptionalProperties.addAll(new TypedAtModemProperties().getOptionalProperties());
        return allOptionalProperties;
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        List<PropertySpec> requiredProperties = new ArrayList<PropertySpec>();
        requiredProperties.addAll(super.getRequiredProperties());  // need to create a new list because the super returns a fixed list
        requiredProperties.addAll(new TypedAtModemProperties().getRequiredProperties());
        return requiredProperties;
    }

    @Override
    public boolean isRequiredProperty(String name) {
        return super.isRequiredProperty(name) || new TypedAtModemProperties().isRequiredProperty(name);
    }

    @Override
    public PropertySpec getPropertySpec(String name) {
        PropertySpec propertySpec = super.getPropertySpec(name);
        if (propertySpec == null) {
            return new TypedAtModemProperties().getPropertySpec(name);
        }
        return propertySpec;
    }

}
