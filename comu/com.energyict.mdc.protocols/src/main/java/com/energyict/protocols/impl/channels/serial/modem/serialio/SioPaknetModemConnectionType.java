package com.energyict.protocols.impl.channels.serial.modem.serialio;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.channels.serial.modem.postdialcommand.ModemComponent;
import com.energyict.mdc.io.SerialComponentService;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.SerialPortComChannel;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.dynamic.ConnectionProperty;
import com.energyict.protocol.exceptions.ConnectionException;
import com.energyict.protocols.impl.ConnectionTypeServiceImpl;
import com.energyict.protocols.impl.channels.serial.direct.serialio.SioSerialConnectionType;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

/**
 * Provides an implementation for the {@link ConnectionType}
 * interface for Serial PAKNET-Modem communication, using the Sio library.
 *
 * @author sva
 * @since 14/04/2013 - 10:50
 */
public class SioPaknetModemConnectionType extends SioSerialConnectionType {

    private ModemComponent paknetModemComponent;

    @Inject
    public SioPaknetModemConnectionType(@Named(ConnectionTypeServiceImpl.SERIAL_PAKNET_GUICE_INJECTION_NAME) SerialComponentService serialComponentService, Thesaurus thesaurus) {
        super(ConnectionTypeServiceImpl.SERIAL_PAKNET_GUICE_INJECTION_NAME, serialComponentService, thesaurus);
    }

    @Override
    public SerialPortComChannel connect(List<ConnectionProperty> properties) throws ConnectionException {
        this.paknetModemComponent = this.getSerialComponentService().newModemComponent(this.toTypedProperties(properties));
        // Create the SerialComChannel and set all property values
        SerialPortComChannel comChannel = super.connect(properties);
        try {
            this.paknetModemComponent.connect(this.getComPortNameValue(), comChannel);
        }
        catch (RuntimeException e) {
            comChannel.close(); // need to properly close the comChannel, otherwise the port will always be occupied
            throw new ConnectionException(e);
        }
        return comChannel;
    }

    @Override
    public void disconnect(ComChannel comChannel) throws ConnectionException {
        if (paknetModemComponent != null) {
            paknetModemComponent.disconnect((SerialPortComChannel) comChannel);
        }
    }

    @Override
    public String getVersion() {
        return "$Date: 2014-11-04 14:09:00 +0100 $";
    }

}