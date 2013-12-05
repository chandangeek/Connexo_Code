package com.energyict.protocols.mdc.channels.serial.optical.rxtx;

import com.energyict.mdc.channels.serial.OpticalDriver;
import com.energyict.mdc.protocol.ConnectionType;
import com.energyict.protocols.mdc.channels.serial.direct.rxtx.RxTxSerialConnectionType;

/**
 * Provides an implementation of a {@link ConnectionType} interface for optical
 * communication using the open source RxTX libraries.
 * <p/>
 * Copyrights EnergyICT
 * Date: 12/11/12
 * Time: 13:00
 * @see RxTxSerialConnectionType
 */
public class RxTxOpticalConnectionType extends RxTxSerialConnectionType implements OpticalDriver {
}
