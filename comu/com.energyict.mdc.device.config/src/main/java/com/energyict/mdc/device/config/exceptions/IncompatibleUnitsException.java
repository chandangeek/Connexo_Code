package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.common.interval.Phenomenon;

/**
 * Models the exceptional case where an attempt is made to link two different components,
 * each with their own Unit definition, which don't have compatible Unit.
 * <p/>
 * Copyrights EnergyICT
 * Date: 07/02/14
 * Time: 10:18
 */
public class IncompatibleUnitsException extends LocalizedException {

    private IncompatibleUnitsException(Thesaurus thesaurus, MessageSeed messageSeed, Object... args) {
        super(thesaurus, messageSeed, args);
    }

    /**
     * Creates a new IncompatibleUnitsException that models the
     * exceptional situation that occurs when an attempt is made to create
     * a {@link com.energyict.mdc.device.config.ChannelSpec} without with a
     * {@link Phenomenon} which is not compatible with the {@link Unit}
     * of the defined {@link com.energyict.mdc.masterdata.MeasurementType}
     *
     * @param thesaurus           The Thesaurus
     * @param phenomenon          The defined Phenomenon
     * @param measurementTypeUnit The unit of the MeasurementType
     * @return the newly created IncompatibleUnitsException
     */
    public static IncompatibleUnitsException forChannelSpecPhenomenonAndChannelTypeUnit(Thesaurus thesaurus, Phenomenon phenomenon, Unit measurementTypeUnit) {
        IncompatibleUnitsException incompatibleUnitsException = new IncompatibleUnitsException(thesaurus, MessageSeeds.CHANNEL_SPEC_UNITS_NOT_COMPATIBLE, phenomenon, measurementTypeUnit);
        incompatibleUnitsException.set("phenomenon", phenomenon);
        incompatibleUnitsException.set("unit", measurementTypeUnit);
        return incompatibleUnitsException;
    }
}
