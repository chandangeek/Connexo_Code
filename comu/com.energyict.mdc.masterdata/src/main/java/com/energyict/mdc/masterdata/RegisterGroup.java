package com.energyict.mdc.masterdata;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;

import java.util.List;

/**
 * Represents a group of registers.
 *
 * @author Geert
 */
@ProviderType
public interface RegisterGroup extends HasId, HasName, com.energyict.mdc.upl.meterdata.RegisterGroup {

    void setName(String newName);

    void save();

    void delete();

    List<RegisterType> getRegisterTypes();

    void addRegisterType(RegisterType registerType);

    void removeRegisterType(RegisterType registerType);

    void updateRegisterTypes(List<RegisterType> registerTypes);

    long getVersion();
}