package com.energyict.mdc.masterdata.exceptions;

import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.Unit;

/**
 * Models the exceptional situation that occurs when an attempt
 * is made to set a unit for a {@link com.energyict.mdc.masterdata.RegisterMapping},
 * but there is no phenomenon that has that unit defined.
 *
 */
public class UnitHasNoMatchingPhenomenonException extends LocalizedFieldValidationException {

    public UnitHasNoMatchingPhenomenonException(Thesaurus thesaurus, Unit unit) {
        super(MessageSeeds.REGISTER_MAPPING_UNIT_DOES_NOT_MATCH_PHENOMENON, "unit", unit);
    }

}