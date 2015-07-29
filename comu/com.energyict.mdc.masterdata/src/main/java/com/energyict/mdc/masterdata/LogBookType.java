package com.energyict.mdc.masterdata;

import com.elster.jupiter.util.HasId;
import com.energyict.mdc.common.ObisCode;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.util.HasName;

/**
 * Copyrights EnergyICT
 * Date: 24/10/12
 * Time: 9:38
 */
@ProviderType
public interface LogBookType extends HasId, HasName {

    public void setName (String newName);

    public String getDescription();

    public void setDescription(String newDescription);

    public ObisCode getObisCode();

    public void setObisCode(ObisCode obisCode);

    public void save ();

    public void delete ();

}