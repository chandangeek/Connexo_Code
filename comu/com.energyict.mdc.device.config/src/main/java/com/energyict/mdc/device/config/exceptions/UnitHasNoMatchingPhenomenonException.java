package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.Unit;

/**
 * Models the exceptional situation that occurs when an attempt
 * is made to set a unit for a register mapping, but there is no phenomenon that has that unit defined
 *
 */
public class UnitHasNoMatchingPhenomenonException extends LocalizedException {

    public UnitHasNoMatchingPhenomenonException(Thesaurus thesaurus, Unit unit) {
        super(thesaurus, MessageSeeds.UNIT_DOES_NOT_MATCH_PHENOMENON, unit);
        this.set("unit", unit);
    }

}