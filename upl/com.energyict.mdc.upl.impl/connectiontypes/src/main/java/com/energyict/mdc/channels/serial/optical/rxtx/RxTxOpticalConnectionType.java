package com.energyict.mdc.channels.serial.optical.rxtx;

import com.energyict.mdc.channels.ComChannelType;
import com.energyict.mdc.channels.serial.OpticalDriver;
import com.energyict.mdc.channels.serial.direct.rxtx.RxTxSerialConnectionType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.protocol.exceptions.ConnectionException;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Provides an implementation of a {@link com.energyict.mdc.tasks.ConnectionType} interface for optical
 * communication using the open source RxTX libraries
 * <p/>
 * Copyrights EnergyICT
 * Date: 12/11/12
 * Time: 13:00
 * @see RxTxSerialConnectionType
 */
@XmlRootElement
public class RxTxOpticalConnectionType extends RxTxSerialConnectionType implements OpticalDriver {

    @Override
    public ComChannel connect(TypedProperties properties) throws ConnectionException {
        ComChannel comChannel = super.connect(properties);
        comChannel.addProperties(createTypeProperty(ComChannelType.OpticalComChannel));
        return comChannel;
    }
}
