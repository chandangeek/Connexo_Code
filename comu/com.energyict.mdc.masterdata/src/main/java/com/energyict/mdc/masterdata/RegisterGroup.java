/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.masterdata;

import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;

import aQute.bnd.annotation.ProviderType;

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

    long getVersion();
}