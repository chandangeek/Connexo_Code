/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.masterdata.exceptions;

import com.elster.jupiter.nls.LocalizedFieldValidationException;

/**
 * Models the exceptional situation that occurs when an attempt
 * is made to update a {@link com.energyict.mdc.masterdata.RegisterGroup},
 * but there is no {@link com.energyict.mdc.masterdata.MeasurementType} selected.
 *
 */
public class RegisterTypesRequiredException extends LocalizedFieldValidationException {

    public RegisterTypesRequiredException() {
        super(MessageSeeds.REGISTER_GROUP_REQUIRES_REGISTER_TYPES, "selectedRegisterTypes");
    }

}