/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data;

import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.ReadingQuality;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationResult;
import com.energyict.mdc.device.data.exceptions.InvalidLastCheckedException;

import aQute.bnd.annotation.ProviderType;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Created by tgr on 9/09/2014.
 */
@ProviderType
public interface DeviceValidation {

    Device getDevice();

    ValidationResult getValidationResult(Collection<? extends ReadingQuality> qualities);

    boolean isValidationActive();

    boolean isValidationOnStorage();

    /**
     * Activates the validation on the Device and sets the last checked
     * date to the specified instant in time.
     * Note that the last checked timestamp is only required
     * when the Device already has data.
     *
     * @param lastChecked The last checked timestamp
     * @throws InvalidLastCheckedException Thrown when lastChecked timestamp is <code>null</code>
     *                                     or after the last checked timestamp of the Device's current meter activation
     */
    void activateValidation(Instant lastChecked);

    void activateValidationOnStorage(Instant lastChecked);

    /**
     * Deactivates the validation on the Device.
     */
    void deactivateValidation();

    boolean isValidationActive(Channel channel, Instant when);

    boolean isChannelStatusActive(Channel channel);

    boolean isChannelStatusActive(Register<?, ?> register);

    boolean isValidationActive(Register<?, ?> register, Instant when);

    boolean allDataValidated(Channel channel, Instant when);

    boolean allDataValidated(Register<?, ?> register, Instant when);

    Optional<Instant> getLastChecked();

    Optional<Instant> getLastValidationRun();

    Optional<Instant> getLastChecked(Channel channel);

    Optional<Instant> getLastChecked(Register<?, ?> register);

    List<DataValidationStatus> getValidationStatus(Channel channel, List<? extends BaseReading> readings, Range<Instant> interval);

    List<DataValidationStatus> getValidationStatus(Register<?, ?> register, List<? extends BaseReading> readings, Range<Instant> interval);

    void validateData();

    void validateLoadProfile(LoadProfile loadProfile);

    void validateChannel(Channel channel);

    void validateRegister(Register<?, ?> register);

    void setLastChecked(Channel c, Instant start);

    void setLastChecked(Register<?, ?> c, Instant start);

}