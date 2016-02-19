package com.elster.insight.usagepoint.data.rest.impl;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.ReadingQuality;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationResult;
import com.google.common.collect.Range;

import aQute.bnd.annotation.ProviderType;

/**
 */
@ProviderType
public interface UsagePointValidation {

    UsagePoint getUsagePoint();

    public ValidationResult getValidationResult(Collection<? extends ReadingQuality> qualities);

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

    boolean allDataValidated(Channel channel, Instant when);

    Optional<Instant> getLastChecked();

    Optional<Instant> getLastChecked(Channel channel);

    List<DataValidationStatus> getValidationStatus(Channel channel, List<? extends BaseReading> readings, Range<Instant> interval);

    void validateData();

    void validateChannel(Channel channel);

    void setLastChecked(Channel c, Instant start);

}