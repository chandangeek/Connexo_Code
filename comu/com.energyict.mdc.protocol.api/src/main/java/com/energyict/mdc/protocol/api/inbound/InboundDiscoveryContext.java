package com.energyict.mdc.protocol.api.inbound;

/**
 * Provides contextual information to an {@link InboundDeviceProtocol}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-12-23 (12:55)
 */
public interface InboundDiscoveryContext extends com.energyict.mdc.upl.InboundDiscoveryContext {

    void markNotAllCollectedDataWasProcessed();

}