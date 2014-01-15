package com.energyict.mdc.protocol.api.legacy;

import com.energyict.mdc.common.BusinessException;

import java.sql.SQLException;

/**
 * Provides functionality to maintain a cache object for a certain protocol
 * <p/>
 * Copyrights EnergyICT
 * Date: 9-feb-2011
 * Time: 13:58:09
 */
public interface CachingProtocol {

    /**
     * Set the cache object. The object itself is an implementation of a protocol
     * specific cache object representing persistent data to be used with the protocol.
     *
     * @param cacheObject a protocol specific cache object
     */
    public void setCache(Object cacheObject);

    /**
     * Returns the protocol specific cache object from the meter protocol implementation.
     *
     * @return the protocol specific cache object
     */
    public Object getCache();

    /**
     * Fetch the protocol specific cache object from the database.
     *
     * @param rtuId Database ID of the RTU
     * @return the protocol specific cache object
     * @throws SQLException Thrown in case of an SQLException
     * @throws BusinessException Thrown in case of an BusinessException
     */
    public Object fetchCache(int rtuId) throws SQLException, BusinessException;

    /**
     * Update the protocol specific cach object information in the database.
     *
     * @param rtuId       Database ID of the RTU
     * @param cacheObject the protocol specific cach object
     * @throws SQLException Thrown in case of an SQLException
     * @throws BusinessException Thrown in case of an BusinessException
     */
    public void updateCache(int rtuId, Object cacheObject) throws SQLException, BusinessException;

}