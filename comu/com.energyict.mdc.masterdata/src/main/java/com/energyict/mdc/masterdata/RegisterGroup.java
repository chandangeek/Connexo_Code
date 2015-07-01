package com.energyict.mdc.masterdata;

import com.energyict.mdc.common.HasId;

import aQute.bnd.annotation.ProviderType;

import java.util.List;

/**
 * Represents a group of registers.
 *
 * @author Geert
 */
@ProviderType
public interface RegisterGroup extends HasId {

    /**
     * Returns number that uniquely identifies this RegisterGroup.
     *
     * @return the id
     */
    public long getId();

    /**
     * Returns the name that uniquely identifies this RegisterGroup.
     *
     * @return the name
     */
    public String getName();

    public void setName (String newName);

    public void save();

    public void delete();

    public List<RegisterType> getRegisterTypes();

    public void addRegisterType(RegisterType registerType);

    public void removeRegisterType(RegisterType registerType);

    public void updateRegisterTypes(List<RegisterType> registerTypes);

}