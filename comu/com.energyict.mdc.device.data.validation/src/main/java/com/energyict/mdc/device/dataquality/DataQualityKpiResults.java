/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.dataquality;

import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.validation.Validator;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;

@ProviderType
public interface DataQualityKpiResults {

    Instant getLastSuspect();

    long getAmountOfSuspects();

    long getChannelSuspects();

    long getRegisterSuspects();

    long getAmountOfAdded();

    long getAmountOfEdited();

    long getAmountOfRemoved();

    long getAmountOfConfirmed();

    long getAmountOfEstimates();

    long getAmountOfInformatives();

    long getAmountOfSuspectsBy(Validator validator);

    long getAmountOfEstimatesBy(Estimator estimator);

}
