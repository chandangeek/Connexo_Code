package com.energyict.mdc.protocol.api.lookups;

import com.energyict.mdc.common.IdBusinessObject;

/**
 * represents an entry in a lookup table
 *
 * @author Geert
 */
public interface LookupEntry extends IdBusinessObject {

    /**
     * Returns the value
     *
     * @return the value
     */
    public String getValue();

    /**
     * Returns the key
     *
     * @return the key
     */
    public int getKey();

    /**
     * Returns the id of the Lookup object
     * this entry belongs to
     *
     * @return the lookup id.
     */
    public int getLookupId();

    /**
     * Returns the receiver's custom translated value (or simply the value itself if no translation is available)
     *
     * @return the receiver's custom translated value
     */
    public String getCustomValue();

}