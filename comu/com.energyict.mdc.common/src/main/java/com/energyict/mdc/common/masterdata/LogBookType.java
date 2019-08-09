/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.masterdata;

import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;

import aQute.bnd.annotation.ProviderType;
import com.energyict.obis.ObisCode;

@ProviderType
public interface LogBookType extends HasId, HasName {

    void setName(String newName);

    String getDescription();

    void setDescription(String newDescription);

    ObisCode getObisCode();

    void setObisCode(ObisCode obisCode);

    void save();

    void delete();

    long getVersion();

}