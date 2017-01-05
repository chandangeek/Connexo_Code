package com.energyict.mdc.channels.serial.modem.serialio;

import com.energyict.mdc.SerialComponentFactory;
import com.energyict.mdc.channels.serial.direct.serialio.SioSerialConnectionType;
import com.energyict.mdc.channels.serial.modem.PaknetModemComponent;
import com.energyict.mdc.channels.serial.modem.TypedPaknetModemProperties;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.upl.properties.PropertySpec;

import com.energyict.protocol.exceptions.ConnectionException;
import com.energyict.protocol.exceptions.ModemException;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides an implementation for the {@link com.energyict.mdc.tasks.ConnectionType}
 * interface for Serial PAKNET-Modem communication, using the Sio library.
 *
 * @author sva
 * @since 14/04/2013 - 10:50
 */
@XmlRootElement
public class SioPaknetModemConnectionType extends SioSerialConnectionType {

    private PaknetModemComponent paknetModemComponent;

    @Override
    public ComChannel connect() throws ConnectionException {

        paknetModemComponent = SerialComponentFactory.instance.get().newPaknetModemComponent(new TypedPaknetModemProperties(getAllProperties()));
        /*
        create the serial ComChannel and set all property values
         */
        ComChannel comChannel = super.connect();
        try {
            paknetModemComponent.connect(getComPortName(getAllProperties()), comChannel);
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
        if (paknetModemComponent != null) {
            paknetModemComponent.disconnectModem(comChannel);
        }
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getUPLPropertySpecs());
        propertySpecs.addAll(new TypedPaknetModemProperties().getUPLPropertySpecs());
        return propertySpecs;
    }

}