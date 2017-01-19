package com.energyict.mdc.io;

import com.energyict.mdc.protocol.ComChannel;

/**
 * A {@link ComChannel} that wraps a {@link ServerSerialPort}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-31 (15:01)
 */
public interface SerialComChannel extends ComChannel {

    ServerSerialPort getSerialPort();

}