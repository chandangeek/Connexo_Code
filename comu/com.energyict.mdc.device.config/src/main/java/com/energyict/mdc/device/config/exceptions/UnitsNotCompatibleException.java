package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.common.interval.Phenomenon;

/**
 * Models the exceptional case where an attempt is made to link two different components,
 * each with their own Unit definition, which don't have compatible Unit
 * <p/>
 * Copyrights EnergyICT
 * Date: 07/02/14
 * Time: 10:18
 */
public class UnitsNotCompatibleException extends LocalizedException {

    private UnitsNotCompatibleException(Thesaurus thesaurus, MessageSeed messageSeed, Object... args) {
        super(thesaurus, messageSeed, args);
    }

    /**
     * Creates a new UnitsNotCompatibleException that models the
     * exceptional situation that occurs when an attempt is made to create
     * a {@link com.energyict.mdc.device.config.ChannelSpec} without with a
     * {@link Phenomenon} which is not compatible with the {@link Unit}
     * of the defined {@link com.energyict.mdc.device.config.RegisterMapping}
     *
     * @param thesaurus           The Thesaurus
     * @param phenomenon          The defined Phenomenon
     * @param registerMappingUnit The unit of the RegisterMapping
     * @return the newly created UnitsNotCompatibleException
     */
    public static UnitsNotCompatibleException forChannelSpecPhenomenonAndRegisterMappingUnit(Thesaurus thesaurus, Phenomenon phenomenon, Unit registerMappingUnit) {
        UnitsNotCompatibleException unitsNotCompatibleException = new UnitsNotCompatibleException(thesaurus, MessageSeeds.CHANNEL_SPEC_UNITS_NOT_COMPATIBLE, phenomenon, registerMappingUnit);
        unitsNotCompatibleException.set("phenomenon", phenomenon);
        unitsNotCompatibleException.set("unit", registerMappingUnit);
        return unitsNotCompatibleException;
    }
}
