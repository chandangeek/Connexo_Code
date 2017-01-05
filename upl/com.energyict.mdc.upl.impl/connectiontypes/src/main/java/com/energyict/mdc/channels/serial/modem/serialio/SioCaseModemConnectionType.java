package com.energyict.mdc.channels.serial.modem.serialio;

import com.energyict.mdc.SerialComponentFactory;
import com.energyict.mdc.channels.serial.direct.serialio.SioSerialConnectionType;
import com.energyict.mdc.channels.serial.modem.CaseModemComponent;
import com.energyict.mdc.channels.serial.modem.TypedCaseModemProperties;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.upl.properties.PropertySpec;

import com.energyict.protocol.exceptions.ConnectionException;
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
    public ComChannel connect() throws ConnectionException {

        this.caseModemComponent = SerialComponentFactory.instance.get().newCaseModemComponent(new TypedCaseModemProperties(getAllProperties()));
        /*
       create the serial ComChannel and set all property values
        */
        ComChannel comChannel = super.connect();
        try {
            caseModemComponent.connect(getComPortName(getAllProperties()), comChannel);
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
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getUPLPropertySpecs());
        propertySpecs.addAll(new TypedCaseModemProperties().getUPLPropertySpecs());
        return propertySpecs;
    }
}
