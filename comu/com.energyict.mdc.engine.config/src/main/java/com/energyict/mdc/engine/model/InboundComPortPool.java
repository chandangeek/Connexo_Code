package com.energyict.mdc.engine.model;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.protocol.api.PluggableClass;
import java.util.List;

/**
 * Models a collection of {@link com.energyict.mdc.engine.model.InboundComPort}s that will
 * all use the same discovery protocol.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-26 (09:17)
 */
public interface InboundComPortPool extends ComPortPool {

    /**
     * Gets the list of {@link com.energyict.mdc.engine.model.InboundComPort} available through this pool.
     *
     * @return The list of InboundComPorts
     */
    public List<InboundComPort> getComPorts();

    /**
     * Gets the {@link PluggableClass discovery protocol}.
     * Note that this is a required property of an InboundComPortPool
     * and setting it to <code>null</code> will result in a
     * {@link BusinessException} when creating or updating
     * an InboundComPortPool.
     *
     * @return The discovery pluggable class
     */
    public long getDiscoveryProtocolPluggableClassId();

    void setDiscoveryProtocolPluggableClassId(long discoveryProtocolPluggableClassId);
}