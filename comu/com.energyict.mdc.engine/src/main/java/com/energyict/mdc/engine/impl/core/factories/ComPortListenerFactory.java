/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core.factories;

import com.energyict.mdc.engine.config.InboundComPort;
import com.energyict.mdc.engine.impl.core.ComPortListener;
import com.energyict.mdc.engine.impl.core.RunningComServer;

/**
 * Provides factory services for {@link ComPortListener}s,
 * will know exactly when to create a single threaded or a
 * multi threaded implementation class for a certain {@link InboundComPort}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-10 (10:28)
 */
public interface ComPortListenerFactory {

    /**
     * Creates a new {@link ComPortListener} for the specified {@link InboundComPort}
     * if it is necessary to schedule that InboundComPort.
     * It would not be necessary to schedule the InboundComPort if it is not active
     * or it if has no simultaneous connections.
     *
     * @return The ComPortListener or <code>null</code> if it was not necessary to schedule
     * the InboundComPort
     */
    ComPortListener newFor(RunningComServer runningComServer, InboundComPort comPort);

}