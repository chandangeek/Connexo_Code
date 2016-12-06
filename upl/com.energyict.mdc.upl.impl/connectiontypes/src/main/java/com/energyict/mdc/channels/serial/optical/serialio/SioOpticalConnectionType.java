package com.energyict.mdc.channels.serial.optical.serialio;

import com.energyict.mdc.channels.ComChannelType;
import com.energyict.mdc.channels.serial.OpticalDriver;
import com.energyict.mdc.channels.serial.direct.serialio.SioSerialConnectionType;
import com.energyict.mdc.protocol.ComChannel;

import com.energyict.protocol.exceptions.ConnectionException;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Provides an implementation of a {@link com.energyict.mdc.tasks.ConnectionType} interface for optical
 * communication using the SerialIO libraries
 * <p/>
 * Copyrights EnergyICT
 * Date: 12/11/12
 * Time: 12:58
 *
 * @see SioSerialConnectionType
 */
@XmlRootElement
public class SioOpticalConnectionType extends SioSerialConnectionType implements OpticalDriver {

    @Override
    public ComChannel connect() throws ConnectionException {
        ComChannel comChannel = super.connect();
        comChannel.addProperties(createTypeProperty(ComChannelType.OpticalComChannel));
        return comChannel;
    }
}
