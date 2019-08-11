/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.masterdata;

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