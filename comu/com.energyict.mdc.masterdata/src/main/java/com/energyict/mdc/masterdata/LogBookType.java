/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.masterdata;

import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;
import com.energyict.mdc.common.ObisCode;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface LogBookType extends HasId, HasName {

    public void setName (String newName);

    public String getDescription();

    public void setDescription(String newDescription);

    public ObisCode getObisCode();

    public void setObisCode(ObisCode obisCode);

    public void save ();

    public void delete ();

    long getVersion();

}