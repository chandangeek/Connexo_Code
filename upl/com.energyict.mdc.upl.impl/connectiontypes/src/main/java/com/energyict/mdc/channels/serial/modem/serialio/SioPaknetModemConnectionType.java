package com.energyict.mdc.channels.serial.modem.serialio;

import com.energyict.mdc.channels.nls.MessageSeeds;
import com.energyict.mdc.channels.serial.direct.serialio.SioSerialConnectionType;
import com.energyict.mdc.channels.serial.modem.PaknetModemComponent;
import com.energyict.mdc.channels.serial.modem.TypedPaknetModemProperties;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.SerialPortComChannel;
import com.energyict.mdc.upl.io.ConnectionType;
import com.energyict.mdc.upl.io.ModemException;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocol.exceptions.ConnectionException;
import com.energyict.protocolimplv2.messages.nls.Thesaurus;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides an implementation for the {@link ConnectionType}
 * interface for Serial PAKNET-Modem communication, using the Sio library.
 *
 * @author sva
 * @since 14/04/2013 - 10:50
 */
@XmlRootElement
public class SioPaknetModemConnectionType extends SioSerialConnectionType {

    private PaknetModemComponent paknetModemComponent;

    public SioPaknetModemConnectionType(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public SerialPortComChannel connect() throws ConnectionException {
        paknetModemComponent = new PaknetModemComponent(new TypedPaknetModemProperties(getAllProperties(), this.getPropertySpecService()));
        // create the serial ComChannel and set all property values
        SerialPortComChannel comChannel = super.connect();
        try {
            paknetModemComponent.connect(getComPortName(getAllProperties()), comChannel);
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
        if (paknetModemComponent != null) {
            paknetModemComponent.disconnect((SerialPortComChannel) comChannel);
        }
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getUPLPropertySpecs());
        propertySpecs.addAll(new TypedPaknetModemProperties(this.getPropertySpecService()).getUPLPropertySpecs());
        return propertySpecs;
    }

}