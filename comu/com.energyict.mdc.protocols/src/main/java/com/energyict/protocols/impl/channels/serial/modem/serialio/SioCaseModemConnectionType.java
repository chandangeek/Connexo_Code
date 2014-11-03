package com.energyict.protocols.impl.channels.serial.modem.serialio;

import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.io.ModemComponent;
import com.energyict.mdc.io.SerialComChannel;
import com.energyict.mdc.io.SerialComponentService;
import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.dynamic.ConnectionProperty;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.protocols.impl.channels.serial.direct.serialio.SioSerialConnectionType;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

/**
 * Provides an implementation for the {@link ConnectionType}
 * interface for Serial Case communication, using the Sio library.
 *
 * @author sva
 * @since 30/04/13 - 13:45
 */
public class SioCaseModemConnectionType extends SioSerialConnectionType {

    private ModemComponent caseModemComponent;
    private final SerialComponentService serialComponentService;

    @Inject
    public SioCaseModemConnectionType(@Named("serialio-case") SerialComponentService serialComponentService) {
        super();
        this.serialComponentService = serialComponentService;
    }

    @Override
    public SerialComChannel connect (List<ConnectionProperty> properties) throws ConnectionException {
        this.initializeCaseModemComponent(properties);
        // create the serial ComChannel and set all property values
        SerialComChannel comChannel = super.connect(properties);
        try {
            this.caseModemComponent.connect(this.getComPortNameValue(), comChannel);
        } catch (RuntimeException e) {
            comChannel.close(); // need to properly close the comChannel, otherwise the port will always be occupied
            throw new ConnectionException(e);
        }
        return comChannel;
    }

    private void initializeCaseModemComponent(List<ConnectionProperty> properties) {
        this.caseModemComponent = this.serialComponentService.newModemComponent(this.toTypedProperties(properties));
    }

    @Override
    public void disconnect(ComChannel comChannel) throws ConnectionException {
        if (this.caseModemComponent != null) {
            this.caseModemComponent.disconnect((SerialComChannel) comChannel);
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