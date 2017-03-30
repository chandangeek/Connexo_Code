/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.masterdata;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;
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