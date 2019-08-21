/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.masterdata.exceptions;

import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.energyict.mdc.common.masterdata.MeasurementType;
import com.energyict.mdc.common.masterdata.RegisterGroup;

/**
 * Models the exceptional situation that occurs when an attempt
 * is made to update a {@link RegisterGroup},
 * but there is no {@link MeasurementType} selected.
 *
 */
public class RegisterTypesRequiredException extends LocalizedFieldValidationException {

    public RegisterTypesRequiredException() {
        super(MessageSeeds.REGISTER_GROUP_REQUIRES_REGISTER_TYPES, "selectedRegisterTypes");
    }

}