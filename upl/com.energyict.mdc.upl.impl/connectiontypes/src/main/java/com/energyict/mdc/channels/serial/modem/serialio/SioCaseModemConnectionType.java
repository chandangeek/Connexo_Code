package com.energyict.mdc.channels.serial.modem.serialio;

import com.energyict.mdc.channels.nls.MessageSeeds;
import com.energyict.mdc.channels.nls.Thesaurus;
import com.energyict.mdc.channels.serial.direct.serialio.SioSerialConnectionType;
import com.energyict.mdc.channels.serial.modem.CaseModemComponent;
import com.energyict.mdc.channels.serial.modem.TypedCaseModemProperties;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.SerialPortComChannel;
import com.energyict.mdc.upl.io.ModemException;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocol.exceptions.ConnectionException;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides an implementation for the {@link com.energyict.mdc.io.ConnectionType} interface
 * for Serial Case communication, using the Sio library.
 * </p>
 * @author sva
 * @since 30/04/13 - 13:45
 */
@XmlRootElement
public class SioCaseModemConnectionType extends SioSerialConnectionType {

    private CaseModemComponent caseModemComponent;

    public SioCaseModemConnectionType(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public SerialPortComChannel connect() throws ConnectionException {
        this.caseModemComponent = new CaseModemComponent(new TypedCaseModemProperties(getAllProperties(), this.getPropertySpecService()));
        // create the serial ComChannel and set all property values
        SerialPortComChannel comChannel = super.connect();
        try {
            caseModemComponent.connect(getComPortName(getAllProperties()), comChannel);
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
        if (caseModemComponent != null) {
            caseModemComponent.disconnect((SerialPortComChannel) comChannel);
        }
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getUPLPropertySpecs());
        propertySpecs.addAll(new TypedCaseModemProperties(this.getPropertySpecService()).getUPLPropertySpecs());
        return propertySpecs;
    }
}
