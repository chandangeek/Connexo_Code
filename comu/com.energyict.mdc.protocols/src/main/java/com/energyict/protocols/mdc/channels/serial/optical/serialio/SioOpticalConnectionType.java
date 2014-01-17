package com.energyict.protocols.mdc.channels.serial.optical.serialio;

import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.OpticalDriver;
import com.energyict.protocols.mdc.channels.serial.direct.serialio.SioSerialConnectionType;

/**
 * Provides an implementation of a {@link ConnectionType} interface for optical
 * communication using the SerialIO libraries
 * <p/>
 * Copyrights EnergyICT
 * Date: 12/11/12
 * Time: 12:58
 * @see SioSerialConnectionType
 */
public class SioOpticalConnectionType extends SioSerialConnectionType implements OpticalDriver {
}