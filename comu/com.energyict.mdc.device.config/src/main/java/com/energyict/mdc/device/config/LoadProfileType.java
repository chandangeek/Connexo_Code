package com.energyict.mdc.device.config;

import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TimeDuration;

import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 11-jan-2011
 * Time: 15:55:54
 */
public interface LoadProfileType {

    /**
     * Returns number that uniquely identifies this LoadProfileType.
     *
     * @return the id
     */
    public long getId();

    /**
     * Returns the name that uniquely identifies this LoadProfileType.
     *
     * @return the name
     */
    public String getName();

    public void setName (String newName);

    /**
     * Returns a description for the LoadProfileType.
     *
     * @return the description
     */
    public String getDescription();

    public void setDescription(String newDescription);

    /**
     * Returns the ObisCode for the LoadProfileType.
     *
     * @return the ObisCode (referring to a generic collection of channels having the same interval)
     */
    public ObisCode getObisCode();

    public void setObisCode(ObisCode obisCode);

    /**
     * Returns the LoadProfile integration period.
     *
     * @return the integration period.
     */
    public TimeDuration getInterval();

    public void setInterval(TimeDuration timeDuration);

    /**
     * Gets the list of RtuRegisterMappings for this LoadProfileType
     *
     * @return a list of RegisterMapping
     */
    public List<RegisterMapping> getRegisterMappings();

    public void addRegisterMapping (RegisterMapping registerMapping);

    public void removeRegisterMapping (RegisterMapping registerMapping);

    /**
     * Indicates if this load profile type is in use (e.g. by a load profile spec, ...) somewhere
     * @return true if this load profile type is used
     */
    public boolean isInUse();

    public void save ();

    public void delete ();

}