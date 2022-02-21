package com.energyict.mdc.channels.serial.modem.serialio;

import com.energyict.mdc.channels.nls.MessageSeeds;
import com.energyict.mdc.channels.serial.direct.serialio.SioSerialConnectionType;
import com.energyict.mdc.channels.serial.modem.AtModemComponent;
import com.energyict.mdc.channels.serial.modem.TypedAtModemProperties;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.SerialPortComChannel;
import com.energyict.mdc.upl.io.ConnectionType;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocol.exceptions.ConnectionException;
import com.energyict.protocol.exceptions.ModemException;
import com.energyict.protocolimplv2.messages.nls.Thesaurus;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides an implementation for the {@link ConnectionType}
 * interface for Serial AT-Modem communication, using the Sio library.
 * <p>
 * Copyrights EnergyICT
 * Date: 12/11/12
 * Time: 13:16
 */
@XmlRootElement
public class SioAtModemConnectionType extends SioSerialConnectionType {

    private AtModemComponent atModemComponent;

    public SioAtModemConnectionType(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public SerialPortComChannel connect() throws ConnectionException {
        // create the serial ComChannel and set all property values
        SerialPortComChannel comChannel = super.connect();
        try {
            getModemComponent().connect(getComPortName(getAllProperties()), comChannel);
        } catch (Throwable e) {
            comChannel.close(); // need to properly close the comChannel, otherwise the port will always be occupied
            if (e instanceof ModemException) {
                throw new ConnectionException(Thesaurus.ID.toString(), MessageSeeds.NestedModemException, e);
            } else {
                throw e;
            }
        }
        return comChannel;
    }

    protected AtModemComponent getModemComponent() {
        if (atModemComponent == null) {
            this.atModemComponent = new AtModemComponent(new TypedAtModemProperties(getAllProperties(), getPropertySpecService()));
        }
        return atModemComponent;
    }

    @Override
    public void disconnect(ComChannel comChannel) throws ConnectionException {
        super.disconnect(comChannel);
        this.getModemComponent().disconnect((SerialPortComChannel) comChannel);
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getUPLPropertySpecs());
        propertySpecs.addAll(new TypedAtModemProperties(getPropertySpecService()).getUPLPropertySpecs());
        return propertySpecs;
    }
}