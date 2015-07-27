package com.energyict.mdc.masterdata;

import com.energyict.mdc.common.HasId;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.util.HasName;

import java.util.List;

/**
 * Represents a group of registers.
 *
 * @author Geert
 */
@ProviderType
public interface RegisterGroup extends HasId, HasName {

    public void setName (String newName);

    public void save();

    public void delete();

    public List<RegisterType> getRegisterTypes();

    public void addRegisterType(RegisterType registerType);

    public void removeRegisterType(RegisterType registerType);

    public void updateRegisterTypes(List<RegisterType> registerTypes);

}