package com.energyict.protocols.mdc.channels.serial.modem.serialio;

import com.energyict.mdc.ManagerFactory;
import com.energyict.mdc.channels.serial.SerialComChannel;
import com.energyict.mdc.channels.serial.modem.CaseModemComponent;
import com.energyict.mdc.channels.serial.modem.TypedCaseModemProperties;
import com.energyict.mdc.protocol.api.ComChannel;
import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.dynamic.ConnectionProperty;
import com.energyict.mdc.protocol.api.dynamic.PropertySpec;
import com.energyict.protocols.mdc.channels.serial.direct.serialio.SioSerialConnectionType;

import java.util.List;

/**
 * <p>Provides an implementation for the {@link ConnectionType}
 * interface for Serial Case communication, using the Sio library.</p>
 *
 * @author sva
 * @since 30/04/13 - 13:45
 */
public class SioCaseModemConnectionType extends SioSerialConnectionType {

    private CaseModemComponent caseModemComponent;

    @Override
    public ComChannel connect (List<ConnectionProperty> properties) throws ConnectionException {

        caseModemComponent = ManagerFactory.getCurrent().getSerialComponentFactory().newCaseModemComponent(new TypedCaseModemProperties(properties));
        /*
       create the serial ComChannel and set all property values
        */
        ComChannel comChannel = super.connect(properties);
        try {
            caseModemComponent.connect(this.getComPortNameValue(), (SerialComChannel) comChannel);
        } catch (Exception e) {
            comChannel.close(); // need to properly close the comChannel, otherwise the port will always be occupied
            throw new ConnectionException(e);
        }
        return comChannel;
    }

    @Override
    public void disconnect(ComChannel comChannel) throws ConnectionException {
        if (caseModemComponent != null) {
            caseModemComponent.disconnectModem(comChannel);
        }
    }

    @Override
    protected void addPropertySpecs (List<PropertySpec> propertySpecs) {
        super.addPropertySpecs(propertySpecs);
        propertySpecs.addAll(new TypedCaseModemProperties().getPropertySpecs());
    }
    @Override
    public PropertySpec getPropertySpec(String name) {
        PropertySpec propertySpec = super.getPropertySpec(name);
        if (propertySpec == null) {
            return new TypedCaseModemProperties().getPropertySpec(name);
        }
        return propertySpec;
    }

}