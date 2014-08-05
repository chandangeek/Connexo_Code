package com.energyict.mdc.masterdata;

import com.energyict.mdc.common.HasId;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TimeDuration;

import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 11-jan-2011
 * Time: 15:55:54
 */
public interface LoadProfileType extends HasId {

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
     * Gets the list of ChannelTypes for this LoadProfileType
     *
     * @return a list of ChannelTypes
     */
    public List<ChannelType> getChannelTypes();

    public ChannelType createChannelTypeForRegisterType(RegisterType templateRegister);

    public void removeChannelType(ChannelType channelType);

    public void save ();

    public void delete ();

}