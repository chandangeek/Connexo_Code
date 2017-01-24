/*
 * CacheMechanism.java
 *
 * Created on 11 oktober 2004, 13:47
 */

package com.energyict.protocols.util;

/**
 * Interface to support caching in <b>ProtocolTester</b>
 */
public interface CacheMechanism {

    /**
     * Set the CacheObject
     *
     * @param cacheObject the object to store
     */
    void setCache(Object cacheObject);

    /**
     * Fetch the cacheObject. Implementors should cast it to their own CacheObject
     *
     * @return the requested CacheObject.
     */
    Object getCache();

    /**
     * The name under which the file will be save in the OperatingSystem.
     *
     * @return the expected fileName of the cacheFile.
     */
    String getFileName();

}
