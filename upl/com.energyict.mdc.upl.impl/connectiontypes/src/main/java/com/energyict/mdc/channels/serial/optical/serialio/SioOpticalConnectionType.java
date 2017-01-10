package com.energyict.mdc.channels.serial.optical.serialio;

import com.energyict.mdc.channels.ComChannelType;
import com.energyict.mdc.channels.serial.OpticalDriver;
import com.energyict.mdc.channels.serial.direct.serialio.SioSerialConnectionType;
import com.energyict.mdc.protocol.SerialPortComChannel;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocol.exceptions.ConnectionException;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Provides an implementation of a {@link com.energyict.mdc.io.ConnectionType} interface
 * for optical communication using the SerialIO libraries.
 * <p/>
 * Copyrights EnergyICT
 * Date: 12/11/12
 * Time: 12:58
 *
 * @see SioSerialConnectionType
 */
@XmlRootElement
public class SioOpticalConnectionType extends SioSerialConnectionType implements OpticalDriver {
    public SioOpticalConnectionType(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public SerialPortComChannel connect() throws ConnectionException {
        SerialPortComChannel comChannel = super.connect();
        comChannel.addProperties(createTypeProperty(ComChannelType.OpticalComChannel));
        return comChannel;
    }
}
