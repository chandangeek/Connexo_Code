package com.energyict.mdc.channels.serial.optical.rxtx;

import com.energyict.mdc.channels.ComChannelType;
import com.energyict.mdc.channels.serial.OpticalDriver;
import com.energyict.mdc.channels.serial.direct.rxtx.RxTxSerialConnectionType;
import com.energyict.mdc.protocol.SerialPortComChannel;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocol.exceptions.ConnectionException;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Provides an implementation of a {@link com.energyict.mdc.io.ConnectionType} interface for optical
 * communication using the open source RxTX libraries.
 * <p/>
 * Copyrights EnergyICT
 * Date: 12/11/12
 * Time: 13:00
 * @see RxTxSerialConnectionType
 */
@XmlRootElement
public class RxTxOpticalConnectionType extends RxTxSerialConnectionType implements OpticalDriver {
    public RxTxOpticalConnectionType(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public SerialPortComChannel connect() throws ConnectionException {
        SerialPortComChannel comChannel = super.connect();
        comChannel.addProperties(createTypeProperty(ComChannelType.OpticalComChannel));
        return comChannel;
    }
}
