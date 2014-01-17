/*
 * Lookup.java
 *
 * Created on 8 september 2004, 9:14
 */

package com.energyict.mdc.protocol.api.lookups;

import com.energyict.mdc.common.NamedBusinessObject;

import java.util.List;

/**
 * Represents a lookup table translating integers * into strings.
 *
 * @author Geert
 */
public interface Lookup extends NamedBusinessObject {

    /**
     * Returns the receiver's default value
     *
     * @return the default value
     */
    public String getDefaultValue();

    /**
     * Returns the receiver's lookup entries.
     *
     * @return a List of LookupEntry objects
     */
    public List<LookupEntry> getEntries();

    /**
     * Returns the lookup value for the argument
     *
     * @param key key to look up
     * @return the lookup value
     */
    public String getValue(int key);

    /**
     * returns the key for a given value
     *
     * @param value the value to search the corresponding key of
     * @return the key for a given value
     */
    public Integer getKey(String value);
}
