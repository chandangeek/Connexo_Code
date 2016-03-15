package com.energyict.mdc.channels.serial.optical.rxtx;

import com.energyict.mdc.channels.ComChannelType;
import com.energyict.mdc.channels.serial.OpticalDriver;
import com.energyict.mdc.channels.serial.direct.rxtx.RxTxSerialConnectionType;
import com.energyict.mdc.ports.ComPort;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.protocol.exceptions.ConnectionException;
import com.energyict.mdc.tasks.ConnectionTaskProperty;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

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
    public ComChannel connect(ComPort comPort, List<ConnectionTaskProperty> properties) throws ConnectionException {
        ComChannel comChannel = super.connect(comPort, properties);
        comChannel.addProperties(createTypeProperty(ComChannelType.OpticalComChannel));
        return comChannel;
    }
}
