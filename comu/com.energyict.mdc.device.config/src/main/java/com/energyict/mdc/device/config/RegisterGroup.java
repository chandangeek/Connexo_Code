package com.energyict.mdc.device.config;

/**
 * Represents a group of registers.
 *
 * @author Geert
 */
public interface RegisterGroup {

    /**
     * Returns number that uniquely identifies this LoadProfileType.
     *
     * @return the id
     */
    public long getId();

    /**
     * Returns the name that uniquely identifies this LoadProfileType.
     *
     * @return the name
     */
    public String getName();

    public void setName (String newName);

    /**
     * Flag indicating if this group will be collected during Manual Meter reading webinput (flex)
     *
     * @return true if this group will be collected during MMR webinput
     */
    public boolean isUseableInMmr();

}