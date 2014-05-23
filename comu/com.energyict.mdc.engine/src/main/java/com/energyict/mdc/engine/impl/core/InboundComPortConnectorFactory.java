package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.engine.impl.core.inbound.InboundComPortConnector;
import com.energyict.mdc.engine.model.InboundComPort;

/**
 * Provides factory services to create a {@link InboundComPortConnector}
 * from an {@link InboundComPort}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-05-22 (16:46)
 */
public interface InboundComPortConnectorFactory {

    /**
     * Creates an appropriate {@link InboundComPortConnector}
     * for the specified {@link InboundComPort}.
     *
     * @param inboundComPort The InboundComPort
     * @return The InboundComPortConnector
     */
    public InboundComPortConnector connectorFor (InboundComPort inboundComPort);

}