package com.energyict.mdc.channels.serial.modem.serialio;

import com.energyict.mdc.channels.nls.MessageSeeds;
import com.energyict.mdc.channels.nls.Thesaurus;
import com.energyict.mdc.channels.serial.direct.serialio.SioSerialConnectionType;
import com.energyict.mdc.channels.serial.modem.PEMPModemComponent;
import com.energyict.mdc.channels.serial.modem.TypedPEMPModemProperties;
import com.energyict.mdc.io.ModemException;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.SerialPortComChannel;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocol.exceptions.ConnectionException;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>Provides an implementation for the {@link com.energyict.mdc.io.ConnectionType}
 * interface for Serial PEMP communication, using the Sio library.</p>
 *
 * @author sva
 * @since 23/04/13 - 11:53
 */
@XmlRootElement
public class SioPEMPModemConnectionType extends SioSerialConnectionType {

    private PEMPModemComponent pempModemComponent;

    public SioPEMPModemConnectionType(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public SerialPortComChannel connect() throws ConnectionException {
        pempModemComponent = new PEMPModemComponent(new TypedPEMPModemProperties(getAllProperties(), this.getPropertySpecService()));
        // create the serial ComChannel and set all property values
        SerialPortComChannel comChannel = super.connect();
        try {
            pempModemComponent.connect(getComPortName(getAllProperties()), comChannel);
        } catch (ModemException e) {
            throw new ConnectionException(Thesaurus.ID.toString(), MessageSeeds.NestedModemException, e);
        } finally {
            comChannel.close(); // need to properly close the comChannel, otherwise the port will always be busy
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
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getUPLPropertySpecs());
        propertySpecs.addAll(new TypedPEMPModemProperties(this.getPropertySpecService()).getUPLPropertySpecs());
        return propertySpecs;
    }
}