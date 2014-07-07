package com.energyict.protocols.mdc.channels.serial.modem.rxtx;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.protocol.api.ComChannel;
import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.dynamic.ConnectionProperty;
import com.energyict.protocols.mdc.channels.serial.direct.rxtx.RxTxSerialConnectionType;
import com.energyict.protocols.mdc.channels.serial.modem.AtModemComponent;
import com.energyict.protocols.mdc.channels.serial.modem.TypedAtModemProperties;

import java.util.List;

/**
 * Provides an implementation for the {@link ConnectionType}
 * interface for Serial AT-Modem communication, using the RxTx library.
 * <p/>
 * Copyrights EnergyICT
 * Date: 20/11/12
 * Time: 16:22
 */
public class RxTxAtModemConnectionType extends RxTxSerialConnectionType {

    private AtModemComponent atModemComponent;

    @Override
    public ComChannel connect (List<ConnectionProperty> properties) throws ConnectionException {
        this.atModemComponent = new AtModemComponent(new TypedAtModemProperties(properties));
        /*
        create the serial ComChannel and set all property values
         */
        ComChannel comChannel = super.connect(properties);
        try {
            atModemComponent.connect(this.getComPortNameValue(), comChannel);
        } catch (Exception e) {
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
    protected void addPropertySpecs (List<PropertySpec> propertySpecs) {
        super.addPropertySpecs(propertySpecs);
        propertySpecs.addAll(new TypedAtModemProperties().getPropertySpecs());
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
