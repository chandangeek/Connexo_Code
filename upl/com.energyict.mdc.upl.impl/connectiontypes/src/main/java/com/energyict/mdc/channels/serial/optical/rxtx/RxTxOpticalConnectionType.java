package com.energyict.mdc.channels.serial.optical.rxtx;

import com.energyict.mdc.channels.serial.OpticalDriver;
import com.energyict.mdc.channels.serial.direct.rxtx.RxTxSerialConnectionType;

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

}
