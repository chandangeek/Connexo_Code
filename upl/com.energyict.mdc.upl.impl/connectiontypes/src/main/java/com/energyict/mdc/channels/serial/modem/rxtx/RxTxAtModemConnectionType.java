package com.energyict.mdc.channels.serial.modem.rxtx;

import com.energyict.mdc.channels.serial.direct.rxtx.RxTxSerialConnectionType;
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
 * interface for Serial AT-Modem communication, using the RxTx library.
 * <p>
 * Copyrights EnergyICT
 * Date: 20/11/12
 * Time: 16:22
 */
@XmlRootElement
public class RxTxAtModemConnectionType extends RxTxSerialConnectionType {

    private AtModemComponent atModemComponent;

    @Override
    public ComChannel connect() throws ConnectionException {

        this.atModemComponent = new AtModemComponent(new TypedAtModemProperties(getAllProperties()));
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
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getUPLPropertySpecs());
        propertySpecs.addAll(new TypedAtModemProperties().getUPLPropertySpecs());
        return propertySpecs;
    }

}
