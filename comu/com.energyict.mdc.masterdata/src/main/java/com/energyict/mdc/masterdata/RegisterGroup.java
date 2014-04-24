package com.energyict.mdc.masterdata;

import com.energyict.mdc.common.HasId;

import java.util.HashMap;
import java.util.List;

/**
 * Represents a group of registers.
 *
 * @author Geert
 */
public interface RegisterGroup extends HasId {

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

    public List<RegisterMapping> getRegisterMappings();

    public void addRegisterMapping (RegisterMapping registerMapping);

    public void removeRegisterMapping (RegisterMapping registerMapping);

    public boolean updateRegisterMappings(HashMap<Long, RegisterMapping> registerMappings);
}