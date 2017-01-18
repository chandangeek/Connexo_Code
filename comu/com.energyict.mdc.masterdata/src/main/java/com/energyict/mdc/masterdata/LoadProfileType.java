package com.energyict.mdc.masterdata;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;
import com.energyict.obis.ObisCode;

import java.util.List;
import java.util.Optional;

/**
 * Copyrights EnergyICT
 * Date: 11-jan-2011
 * Time: 15:55:54
 */
@ProviderType
public interface LoadProfileType extends HasId, HasName, com.energyict.mdc.upl.meterdata.LoadProfileType {

    void setName(String newName);

    void setDescription(String newDescription);

    void setObisCode(ObisCode obisCode);

    void setInterval(TimeDuration timeDuration);

    /**
     * Gets the list of ChannelTypes for this LoadProfileType.
     *
     * @return a list of ChannelTypes
     */
    List<ChannelType> getChannelTypes();

    /**
     * Creates a new {@link ChannelType} for the specified {@link RegisterType}.
     *
     * @param templateRegister The RegisterType
     * @return The new ChannelType or Optional.empty() when it was not possible
     * to map the RegisterType's obiscode,
     * the RegisterType's {@link com.elster.jupiter.metering.ReadingType}
     * and this LoadProfile's interval to an appropriate ReadingType
     */
    Optional<ChannelType> createChannelTypeForRegisterType(RegisterType templateRegister);

    void removeChannelType(ChannelType channelType);

    void save();

    void delete();

    Optional<ChannelType> findChannelType(RegisterType measurementTypeWithoutInterval);

    long getVersion();
}