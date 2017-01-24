package com.energyict.protocols.impl.channels.serial.modem.serialio;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.channels.serial.modem.postdialcommand.ModemComponent;
import com.energyict.mdc.io.SerialComponentService;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.SerialPortComChannel;
import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.dynamic.ConnectionProperty;
import com.energyict.protocols.impl.channels.serial.direct.serialio.SioSerialConnectionType;
import com.energyict.protocols.mdc.services.impl.ConnectionTypeServiceImpl;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

/**
 * Provides an implementation for the {@link ConnectionType}
 * interface for Serial Case communication, using the Sio library.
 *
 * @author sva
 * @since 30/04/13 - 13:45
 */
public class SioCaseModemConnectionType extends SioSerialConnectionType {

    private ModemComponent caseModemComponent;

    @Inject
    public SioCaseModemConnectionType(@Named(ConnectionTypeServiceImpl.SERIAL_CASE_GUICE_INJECTION_NAME) SerialComponentService serialComponentService, Thesaurus thesaurus) {
        super(ConnectionTypeServiceImpl.SERIAL_CASE_GUICE_INJECTION_NAME, serialComponentService, thesaurus);
    }

    @Override
    public SerialPortComChannel connect(List<ConnectionProperty> properties) throws ConnectionException {
        this.initializeCaseModemComponent(properties);
        // create the serial ComChannel and set all property values
        SerialPortComChannel comChannel = super.connect(properties);
        try {
            this.caseModemComponent.connect(this.getComPortNameValue(), comChannel);
        }
        catch (RuntimeException e) {
            comChannel.close(); // need to properly close the comChannel, otherwise the port will always be occupied
            throw new ConnectionException(e);
        }
        return comChannel;
    }

    private void initializeCaseModemComponent(List<ConnectionProperty> properties) {
        this.caseModemComponent = this.getSerialComponentService().newModemComponent(this.toTypedProperties(properties));
    }

    @Override
    public void disconnect(ComChannel comChannel) throws ConnectionException {
        if (this.caseModemComponent != null) {
            this.caseModemComponent.disconnect((SerialPortComChannel) comChannel);
        }
    }

    @Override
    public String getVersion() {
        return "$Date: 2014-11-04 14:09:00 +0100 $";
    }

}