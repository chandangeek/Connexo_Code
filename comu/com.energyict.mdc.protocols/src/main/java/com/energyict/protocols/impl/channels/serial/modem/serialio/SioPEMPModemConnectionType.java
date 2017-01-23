package com.energyict.protocols.impl.channels.serial.modem.serialio;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.channels.serial.modem.postdialcommand.ModemComponent;
import com.energyict.mdc.io.SerialComponentService;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.SerialPortComChannel;
import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.dynamic.ConnectionProperty;
import com.energyict.protocols.impl.ConnectionTypeServiceImpl;
import com.energyict.protocols.impl.channels.serial.direct.serialio.SioSerialConnectionType;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

/**
 * Provides an implementation for the {@link ConnectionType}
 * interface for Serial PEMP communication, using the Sio library.
 *
 * @author sva
 * @since 23/04/13 - 11:53
 */
public class SioPEMPModemConnectionType extends SioSerialConnectionType {

    private ModemComponent pempModemComponent;

    @Inject
    public SioPEMPModemConnectionType(@Named(ConnectionTypeServiceImpl.SERIAL_PEMP_GUICE_INJECTION_NAME) SerialComponentService serialComponentService, Thesaurus thesaurus) {
        super(ConnectionTypeServiceImpl.SERIAL_PEMP_GUICE_INJECTION_NAME, serialComponentService, thesaurus);
    }

    @Override
    public SerialPortComChannel connect(List<ConnectionProperty> properties) throws ConnectionException {
        this.initializeModemComponent(properties);
        // create the serial ComChannel and set all property values
        SerialPortComChannel comChannel = super.connect(properties);
        try {
            pempModemComponent.connect(this.getComPortNameValue(), comChannel);
        }
        catch (RuntimeException e) {
            comChannel.close(); // need to properly close the comChannel, otherwise the port will always be occupied
            throw new ConnectionException(e);
        }
        return comChannel;
    }

    private void initializeModemComponent(List<ConnectionProperty> properties) {
        this.pempModemComponent = this.getSerialComponentService().newModemComponent(this.toTypedProperties(properties));
    }

    @Override
    public void disconnect(ComChannel comChannel) throws ConnectionException {
        if (pempModemComponent != null) {
            pempModemComponent.disconnect((SerialPortComChannel) comChannel);
        }
    }

    @Override
    public String getVersion() {
        return "$Date: 2014-11-04 14:09:00 +0100 $";
    }

}