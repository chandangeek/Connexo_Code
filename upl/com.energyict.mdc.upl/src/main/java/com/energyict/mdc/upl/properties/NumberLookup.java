package com.energyict.mdc.upl.properties;

/**
 * Models a Map<Integer, String>.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-12-02 (09:20)
 */
public interface NumberLookup {

    /**
     * Returns the value to which the specified key is mapped,
     * or {@code null} if this map contains no mapping for the key.
     *
     * @param key the key whose associated value is to be returned
     * @return the value to which the specified key is mapped, or
     *         {@code null} if this map contains no mapping for the key
     */
    String getValue(int key);

}