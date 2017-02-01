package com.energyict.mdc.upl.cache;

import aQute.bnd.annotation.ConsumerType;

import java.io.Serializable;

/**
 * Protocols can optionally implement this interface to maintain
 * a cache in order to optimize the execution from one session to another.
 * The protocol is not responsible for storing and/or persisting the
 * cache in any way. The protocol will create the cache if it does not
 * exist yet and it will get the cache injected back in the next session.
 * An process external to the protocol will store/persist the cache
 * and may use the java serialization mechanism to do that
 * which is why the cache is required to implement the {@link Serializable} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-10-28 (14:26)
 */
@ConsumerType
public interface CachingProtocol {

    /**
     * Returns the protocol specific cache object from the meter protocol implementation.
     *
     * @return the protocol specific cache object
     */
    Serializable getCache();

    /**
     * Inject the cache object from a previous session.
     *
     * @param cacheObject a protocol specific cache object
     */
    void setCache(Serializable cacheObject);

}