/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.validation;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;

@ProviderType
public interface DeviceValidationKpiResults {

    boolean isAllDataValidated();

    long getAmountOfSuspects();

    long getChannelSuspects();

    long getRegisterSuspects();

    Instant getLastSuspect();

    boolean isThresholdValidator();

    boolean isMissingValuesValidator();

    boolean isReadingQualitiesValidator();

    boolean isRegisterIncreaseValidator();

}