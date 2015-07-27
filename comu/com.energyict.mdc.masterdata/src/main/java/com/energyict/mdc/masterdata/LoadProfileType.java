package com.energyict.mdc.masterdata;

import com.energyict.mdc.common.HasId;
import com.energyict.mdc.common.ObisCode;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.util.HasName;

import java.util.List;
import java.util.Optional;

/**
 * Copyrights EnergyICT
 * Date: 11-jan-2011
 * Time: 15:55:54
 */
@ProviderType
public interface LoadProfileType extends HasId, HasName {

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
     * Gets the list of ChannelTypes for this LoadProfileType.
     *
     * @return a list of ChannelTypes
     */
    public List<ChannelType> getChannelTypes();

    /**
     * Creates a new {@link ChannelType} for the specified {@link RegisterType}.
     *
     * @param templateRegister The RegisterType
     * @return The new ChannelType or Optional.empty() when it was not possible
     *         to map the RegisterType's obiscode,
     *         the RegisterType's {@link com.elster.jupiter.metering.ReadingType}
     *         and this LoadProfile's interval to an appropriate ReadingType
     */
    public Optional<ChannelType> createChannelTypeForRegisterType(RegisterType templateRegister);

    public void removeChannelType(ChannelType channelType);

    public void save ();

    public void delete ();

    Optional<ChannelType> findChannelType(RegisterType measurementTypeWithoutInterval);

}