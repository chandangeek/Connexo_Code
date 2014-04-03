package com.energyict.mdc.device.config;

import com.energyict.mdc.common.HasId;

/**
 * Represents a group of registers.
 *
 * @author Geert
 */
public interface RegisterGroup extends HasId{

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

    public void save();

    public void delete();

}