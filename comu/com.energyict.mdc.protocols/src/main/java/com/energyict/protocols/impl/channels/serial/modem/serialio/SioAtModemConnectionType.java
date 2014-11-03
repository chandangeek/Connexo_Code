package com.energyict.protocols.impl.channels.serial.modem.serialio;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.io.ModemComponent;
import com.energyict.mdc.io.SerialComChannel;
import com.energyict.mdc.io.SerialComponentService;
import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.dynamic.ConnectionProperty;
import com.energyict.protocols.impl.channels.serial.direct.serialio.SioSerialConnectionType;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

/**
 * Provides an implementation for the {@link ConnectionType}
 * interface for Serial AT-Modem communication, using the Sio library.
 * <p/>
 * Copyrights EnergyICT
 * Date: 12/11/12
 * Time: 13:16
 */
public class SioAtModemConnectionType extends SioSerialConnectionType {

    private ModemComponent atModemComponent;
    private final SerialComponentService serialComponentService;

    @Inject
    public SioAtModemConnectionType(@Named("serialio-at") SerialComponentService serialComponentService) {
        super();
        this.serialComponentService = serialComponentService;
    }

    @Override
    public SerialComChannel connect (List<ConnectionProperty> properties) throws ConnectionException {
        this.atModemComponent = this.serialComponentService.newModemComponent(this.toTypedProperties(properties));
        // create the SerialComChannel and set all property values
        SerialComChannel comChannel = super.connect(properties);
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
            this.atModemComponent.disconnect((SerialComChannel) comChannel);
        }
    }

    @Override
    protected void addPropertySpecs (List<PropertySpec> propertySpecs) {
        super.addPropertySpecs(propertySpecs);
        propertySpecs.addAll(this.serialComponentService.getPropertySpecs());
    }

    @Override
    public PropertySpec getPropertySpec(String name) {
        PropertySpec propertySpec = super.getPropertySpec(name);
        if (propertySpec == null) {
            return this.serialComponentService.getPropertySpec(name);
        }
        return propertySpec;
    }

}