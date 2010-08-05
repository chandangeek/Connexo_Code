package com.energyict.genericprotocolimpl.common;

import com.energyict.mdw.amr.GenericProtocol;

import java.util.Properties;

/**
 * Copyrights EnergyICT
 * Date: 5-aug-2010
 * Time: 11:21:58
 */

/**
 * This abstract class should contain all the most used and common methods of a basic generic protocol.
 * Keep this class reusable for other generic protocols. No protocol specific code here.
 */
public abstract class AbstractGenericProtocol implements GenericProtocol {

    private Properties properties = null;
    private long timeDifference = 0;

    /**
     * Getter for the time difference of the clock in the device
     *
     * @return
     */
    public long getTimeDifference() {
        return timeDifference;
    }

    /**
     * Setter for the time difference of the clock in the device
     *
     * @param timeDifference
     */
    public void setTimeDifference(long timeDifference) {
        this.timeDifference = timeDifference;
    }

    /**
     * Setter for the generic protocol properties
     *
     * @param properties
     */
    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    /**
     * Lazy getter for the generic protocol properties
     * When properties == null, we will initialize it as a new empty Properties object and return this instead of null;
     *
     * @return
     */
    public Properties getProperties() {
        if (properties == null) {
            properties = new Properties();
        }
        return properties;
    }

    public boolean propertyExist(String propertyKey) {
        String value = getProperties().getProperty(propertyKey);
        return (value != null) && (value.length() > 0);
    }

}
