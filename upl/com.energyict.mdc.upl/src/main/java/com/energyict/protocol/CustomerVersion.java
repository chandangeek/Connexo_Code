/*
 * CustomerVersion.java
 *
 * Created on 13 november 2003, 10:05
 */

package com.energyict.protocol;

/**
 * represent the version of customer specific code.
 *
 * @author Karel
 */
public interface CustomerVersion {

    /**
     * Returns the version
     *
     * @return the version string
     */
    String getVersion();

    /**
     * Returns the customer's name
     *
     * @return the customer name
     */
    String getCustomer();

}
