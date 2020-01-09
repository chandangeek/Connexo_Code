package com.energyict.mdc.engine.offline.core;

import java.io.Serializable;

/**
 * Utility class to bind a name to an aspect.
 * This class should not be used directly by applications.
 */
public class Association implements Comparable, Serializable {

    private String key;
    private String value;

    /**
     * Creates a new instance of Association
     *
     * @param key   the new association's key
     * @param value the new association's value
     */
    public Association(String key, String value) {
        this.key = key;
        this.value = value;
    }

    /**
     * get key field
     *
     * @return the receiver's key
     */
    public String getKey() {
        return key;
    }

    /**
     * set key field
     *
     * @param key the new key
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * get value field
     *
     * @return the receiver's value
     */
    public String getValue() {
        return value;
    }

    /**
     * set value field
     *
     * @param value the new value
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * obtain a string representation
     *
     * @return a description of the receiver
     */
    public String toString() {
        return
                key +
                        "->" +
                        value;
    }

    /**
     * check the receiver's state
     *
     * @return true if the receiver is in a valid
     *         state
     */
    public boolean isValid() {
        return
                hasValidKey() &&
                        value != null &&
                        value.length() > 0;
    }

    /**
     * Tests if the receiver has a valid key
     *
     * @return true if the receiver's key is valid
     */
    public boolean hasValidKey() {
        return
                key != null &&
                        key.length() > 0;
    }

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.<p>
     * Note: this class has a natural ordering that is
     * inconsistent with equals. Ordering is based on the key values,
     * while two associations are only equal if they are the same object.
     *
     * @param o the Object to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     *         is less than, equal to, or greater than the specified object.
     */
    public int compareTo(Object o) {
        return this.key.compareTo(((Association) o).getKey());
    }

}
