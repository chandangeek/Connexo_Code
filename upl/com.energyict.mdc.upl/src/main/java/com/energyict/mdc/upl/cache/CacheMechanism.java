/*
 * CacheMechanism.java
 *
 * Created on 11 oktober 2004, 13:47
 */

package com.energyict.mdc.upl.cache;

/**
 * Interface to support caching in <b>ProtocolTester</b>
 */
public interface CacheMechanism extends CachingProtocol {

    /**
     * The name under which the file will be save in the OperatingSystem.
     *
     * @return the expected fileName of the cacheFile.
     */
    String getFileName();

}