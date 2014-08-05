package com.energyict.mdc.masterdata.exceptions;

import com.energyict.mdc.common.Unit;

import com.elster.jupiter.nls.LocalizedFieldValidationException;

/**
 * Models the exceptional situation that occurs when an attempt
 * is made to set a unit for a {@link com.energyict.mdc.masterdata.MeasurementType},
 * but there is no phenomenon that has that unit defined.
 *
 */
public class UnitHasNoMatchingPhenomenonException extends LocalizedFieldValidationException {

    public UnitHasNoMatchingPhenomenonException(Unit unit) {
        super(MessageSeeds.REGISTER_MAPPING_UNIT_DOES_NOT_MATCH_PHENOMENON, "unit", unit);
    }

}