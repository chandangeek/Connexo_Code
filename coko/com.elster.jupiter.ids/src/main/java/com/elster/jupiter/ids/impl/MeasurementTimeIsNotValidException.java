/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.ids.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

public class MeasurementTimeIsNotValidException extends LocalizedException {

	private static final long serialVersionUID = 1L;

	public MeasurementTimeIsNotValidException(Thesaurus thesaurus) {
        super(thesaurus, MessageSeeds.MEASUREMENT_TIME_IS_INCORRECT);
    }
}
