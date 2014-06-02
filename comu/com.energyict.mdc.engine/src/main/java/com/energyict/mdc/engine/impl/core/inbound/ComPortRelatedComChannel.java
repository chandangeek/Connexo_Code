package com.energyict.mdc.engine.impl.core.inbound;

import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.protocol.api.ComChannel;

/**
 * Wraps {@link ComChannel} to add that the ComChannel
 * is actually connected from a {@link ComPort}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-06-19 (14:07)
 */
public interface ComPortRelatedComChannel extends ComChannel {

    public ComPort getComPort ();

    public void setComPort (ComPort comPort);

    public ComChannel getActualComChannel();

}