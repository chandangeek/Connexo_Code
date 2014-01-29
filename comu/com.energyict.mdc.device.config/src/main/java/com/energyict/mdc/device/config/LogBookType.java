package com.energyict.mdc.device.config;

import com.energyict.mdc.common.ObisCode;

/**
 * Copyrights EnergyICT
 * Date: 24/10/12
 * Time: 9:38
 */
public interface LogBookType {

    /**
     * Returns number that uniquely identifies this LogBookType.
     *
     * @return the id
     */
    public long getId();

    /**
     * Returns the name that uniquely identifies this LogBookType.
     *
     * @return the name
     */
    public String getName();

    public void setName (String newName);

    public String getDescription();

    public void setDescription(String newDescription);

    public ObisCode getObisCode();

    public void setObisCode(ObisCode obisCode);

    public void save ();

    public void delete ();

}