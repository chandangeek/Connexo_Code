/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.legacy;

import java.sql.SQLException;

public interface CachingProtocol {

    /**
     * Set the cache object. The object itself is an implementation of a protocol
     * specific cache object representing persistent data to be used with the protocol.
     *
     * @param cacheObject a protocol specific cache object
     */
    void setCache(Object cacheObject);

    /**
     * Returns the protocol specific cache object from the meter protocol implementation.
     *
     * @return the protocol specific cache object
     */
    Object getCache();

    /**
     * Fetch the protocol specific cache object from the database.
     * @deprecated Not used anymore. The framework will fetch the cache objects
     *
     * @param rtuId Database ID of the RTU
     * @return the protocol specific cache object
     * @throws SQLException Thrown in case of an SQLException
     */
    Object fetchCache(int rtuId) throws SQLException;

    /**
     * Update the protocol specific cache object information in the database.
     * @deprecated Not used anymore. The framework will update the cache objects
     *
     * @param rtuId       Database ID of the RTU
     * @param cacheObject the protocol specific cach object
     * @throws SQLException Thrown in case of an SQLException
     */
    void updateCache(int rtuId, Object cacheObject) throws SQLException;

}