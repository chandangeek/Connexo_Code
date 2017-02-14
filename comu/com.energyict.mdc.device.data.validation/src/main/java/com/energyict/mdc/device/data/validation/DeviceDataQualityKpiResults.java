/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.validation;

import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.validation.Validator;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;

@ProviderType
public interface DeviceDataQualityKpiResults {

    long getAmountOfSuspects();

    Instant getLastSuspect();

    long getChannelSuspects();

    long getRegisterSuspects();

    long getAmountOfAdded();

    long getAmountOfEdited();

    long getAmountOfRemoved();

    long getAmountOfConfirmed();

    long getAmountOfEstimated();

    long getAmountOfInformatives();

    long getAmountOfValidatedBy(Validator validator);

    long getAmountOfEstimatedBy(Estimator estimator);

}
