package com.energyict.mdc.channels.serial.modem.serialio;

import com.energyict.mdc.SerialComponentFactory;
import com.energyict.mdc.channels.serial.direct.serialio.SioSerialConnectionType;
import com.energyict.mdc.channels.serial.modem.PEMPModemComponent;
import com.energyict.mdc.channels.serial.modem.TypedPEMPModemProperties;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.upl.properties.PropertySpec;

import com.energyict.protocol.exceptions.ConnectionException;
import com.energyict.protocol.exceptions.ModemException;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>Provides an implementation for the {@link com.energyict.mdc.tasks.ConnectionType}
 * interface for Serial PEMP communication, using the Sio library.</p>
 *
 * @author sva
 * @since 23/04/13 - 11:53
 */
@XmlRootElement
public class SioPEMPModemConnectionType extends SioSerialConnectionType {

    private PEMPModemComponent pempModemComponent;

    @Override
    public ComChannel connect() throws ConnectionException {

        pempModemComponent = SerialComponentFactory.instance.get().newPEMPModemComponent(new TypedPEMPModemProperties(properties));
        /*
       create the serial ComChannel and set all property values
        */
        ComChannel comChannel = super.connect();
        try {
            pempModemComponent.connect(getComPortName(properties), comChannel);
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
        if (pempModemComponent != null) {
            pempModemComponent.disconnectModem(comChannel);
        }
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getPropertySpecs());
        propertySpecs.addAll(new TypedPEMPModemProperties().getPropertySpecs());
        return propertySpecs;
    }
}