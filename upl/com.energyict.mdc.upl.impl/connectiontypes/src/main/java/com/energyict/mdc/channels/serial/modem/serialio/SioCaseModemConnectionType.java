package com.energyict.mdc.channels.serial.modem.serialio;

import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.ManagerFactory;
import com.energyict.mdc.channels.serial.SerialComChannel;
import com.energyict.mdc.channels.serial.direct.serialio.SioSerialConnectionType;
import com.energyict.mdc.channels.serial.modem.CaseModemComponent;
import com.energyict.mdc.channels.serial.modem.TypedCaseModemProperties;
import com.energyict.mdc.ports.ComPort;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.protocol.exceptions.ConnectionException;
import com.energyict.mdc.tasks.ConnectionTaskProperty;
import com.energyict.protocol.exceptions.ModemException;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>Provides an implementation for the {@link com.energyict.mdc.tasks.ConnectionType}
 * interface for Serial Case communication, using the Sio library.</p>
 *
 * @author sva
 * @since 30/04/13 - 13:45
 */
@XmlRootElement
public class SioCaseModemConnectionType extends SioSerialConnectionType {

    private CaseModemComponent caseModemComponent;

    @Override
    public ComChannel connect(ComPort comPort, List<ConnectionTaskProperty> properties) throws ConnectionException {

        caseModemComponent = ManagerFactory.getCurrent().getSerialComponentFactory().newCaseModemComponent(new TypedCaseModemProperties(properties));
        /*
       create the serial ComChannel and set all property values
        */
        ComChannel comChannel = super.connect(comPort, properties);
        try {
            caseModemComponent.connect(comPort.getName(), (SerialComChannel) comChannel);
        } catch (Throwable e) {
            comChannel.close(); // need to properly close the comChannel, otherwise the port will always be occupied
            if (e instanceof ModemException) {
                throw new ConnectionException(e);
            } else {
                throw e;
            }
        }
        return comChannel;
    }

    @Override
    public void disconnect(ComChannel comChannel) throws ConnectionException {
        super.disconnect(comChannel);
        if (caseModemComponent != null) {
            caseModemComponent.disconnectModem(comChannel);
        }
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        List<PropertySpec> allOptionalProperties = new ArrayList<>();
        allOptionalProperties.addAll(super.getOptionalProperties());    // need to create a new list because the super returns a fixed list
        allOptionalProperties.addAll(new TypedCaseModemProperties().getOptionalProperties());
        return allOptionalProperties;
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        List<PropertySpec> requiredProperties = new ArrayList<>();
        requiredProperties.addAll(super.getRequiredProperties());  // need to create a new list because the super returns a fixed list
        requiredProperties.addAll(new TypedCaseModemProperties().getRequiredProperties());
        return requiredProperties;
    }

    @Override
    public boolean isRequiredProperty(String name) {
        return super.isRequiredProperty(name) || new TypedCaseModemProperties().isRequiredProperty(name);
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
