/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data;

import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.ReadingQuality;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationPropertyDefinitionLevel;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validation.ValidationRule;
import com.energyict.mdc.device.data.exceptions.InvalidLastCheckedException;

import aQute.bnd.annotation.ProviderType;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

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

    List<DataValidationStatus> getHistoryValidationStatus(Register<?, ?> register, List<? extends BaseReading> readings, List<ReadingQualityRecord> readingQualities, Range<Instant> interval);

    DataValidationStatus getValidationStatus(Channel channel, Instant instant, List<ReadingQualityRecord> readingQualities, Range<Instant> interval);

    void validateData();

    void validateLoadProfile(LoadProfile loadProfile);

    void validateChannel(Channel channel);

    void validateRegister(Register<?, ?> register);

    void setLastChecked(Channel c, Instant start);

    void setLastChecked(Register<?, ?> c, Instant start);

    /**
     * Finds all properties that are overridden on all validation rules and all device's channels.
     *
     * @return a List of {@link ChannelValidationRuleOverriddenProperties}
     */
    List<? extends ChannelValidationRuleOverriddenProperties> findAllOverriddenProperties();

    /**
     * Finds properties that are overridden on a specified {@link ValidationRule} and channel's {@link ReadingType}.
     *
     * @param validationRule target {@link ValidationRule}
     * @param readingType target {@link ReadingType} of device's channel
     * @return {@link ChannelValidationRuleOverriddenProperties} or Optional.empty() if no properties are overridden for specified validation rule and reading type
     */
    Optional<? extends ChannelValidationRuleOverriddenProperties> findOverriddenProperties(ValidationRule validationRule, ReadingType readingType);

    /**
     * Finds {@link ChannelValidationRuleOverriddenProperties} entity by id.
     *
     * @return {@link ChannelValidationRuleOverriddenProperties} or Optional.empty() if no entity with such id
     */
    Optional<? extends ChannelValidationRuleOverriddenProperties> findChannelValidationRuleOverriddenProperties(long id);

    /**
     * Finds and locks {@link ChannelValidationRuleOverriddenProperties} entity by id and version.
     *
     * @return {@link ChannelValidationRuleOverriddenProperties} or Optional.empty() if no entity with such id and version
     */
    Optional<? extends ChannelValidationRuleOverriddenProperties> findAndLockChannelValidationRuleOverriddenProperties(long id, long version);

    /**
     * Starts the process of overriding {@link ValidationRule}'s properties on {@link ValidationPropertyDefinitionLevel#TARGET_OBJECT},
     * which is a device's channel identified by {@link ReadingType} in this context.
     *
     * @param validationRule target {@link ValidationRule} for which the properties are going to be redefined
     * @param readingType target {@link ReadingType} of device's channel
     * @return {@link PropertyOverrider}
     */
    PropertyOverrider overridePropertiesFor(ValidationRule validationRule, ReadingType readingType);

    /**
     * Builder for {@link ChannelValidationRuleOverriddenProperties}
     */
    @ProviderType
    interface PropertyOverrider {

        PropertyOverrider override(String propertyName, Object propertyValue);

        ChannelValidationRuleOverriddenProperties complete();
    }
}