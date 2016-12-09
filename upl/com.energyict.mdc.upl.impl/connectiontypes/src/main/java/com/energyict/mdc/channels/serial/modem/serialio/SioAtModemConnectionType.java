package com.energyict.mdc.channels.serial.modem.serialio;

import com.energyict.mdc.SerialComponentFactory;
import com.energyict.mdc.channels.serial.direct.serialio.SioSerialConnectionType;
import com.energyict.mdc.channels.serial.modem.AtModemComponent;
import com.energyict.mdc.channels.serial.modem.TypedAtModemProperties;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.upl.properties.PropertySpec;

import com.energyict.protocol.exceptions.ConnectionException;
import com.energyict.protocol.exceptions.ModemException;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides an implementation for the {@link com.energyict.mdc.tasks.ConnectionType}
 * interface for Serial AT-Modem communication, using the Sio library.
 * <p>
 * Copyrights EnergyICT
 * Date: 12/11/12
 * Time: 13:16
 */
@XmlRootElement
public class SioAtModemConnectionType extends SioSerialConnectionType {

    private AtModemComponent atModemComponent;

    @Override
    public ComChannel connect() throws ConnectionException {

        this.atModemComponent = SerialComponentFactory.instance.get().newAtModemComponent(new TypedAtModemProperties(getAllProperties()));
        /*
        create the serial ComChannel and set all property values
         */
        ComChannel comChannel = super.connect();
        try {
            atModemComponent.connect(getComPortName(getAllProperties()), comChannel);
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
        if (this.atModemComponent != null) {
            this.atModemComponent.disconnectModem(comChannel);
        }
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getPropertySpecs());
        propertySpecs.addAll(new TypedAtModemProperties().getPropertySpecs());
        return propertySpecs;
    }
}