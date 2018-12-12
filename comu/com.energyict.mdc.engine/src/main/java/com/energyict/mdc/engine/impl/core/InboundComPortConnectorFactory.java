/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.engine.config.InboundComPort;
import com.energyict.mdc.engine.impl.core.inbound.InboundComPortConnector;

/**
 * Provides factory services to create a {@link InboundComPortConnector}
 * from an {@link InboundComPort}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-05-22 (16:46)
 */
interface InboundComPortConnectorFactory {

    /**
     * Creates an appropriate {@link InboundComPortConnector}
     * for the specified {@link InboundComPort}.
     *
     * @param inboundComPort The InboundComPort
     * @return The InboundComPortConnector
     */
    InboundComPortConnector connectorFor(InboundComPort inboundComPort);

}