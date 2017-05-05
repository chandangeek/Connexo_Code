/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data;

import com.elster.jupiter.estimation.EstimationPropertyDefinitionLevel;
import com.elster.jupiter.estimation.EstimationRule;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.metering.ReadingType;

import aQute.bnd.annotation.ProviderType;

import java.util.List;
import java.util.Optional;

@ProviderType
public interface DeviceEstimation {

    boolean isEstimationActive();

    void activateEstimation();

    void deactivateEstimation();

    List<DeviceEstimationRuleSetActivation> getEstimationRuleSetActivations();

    void activateEstimationRuleSet(EstimationRuleSet estimationRuleSet);

    void deactivateEstimationRuleSet(EstimationRuleSet estimationRuleSet);

    Device getDevice();

    /**
     * Finds all properties that are overridden on all estimation rules and all device's channels.
     *
     * @return a List of {@link ChannelEstimationRuleOverriddenProperties}
     */
    List<? extends ChannelEstimationRuleOverriddenProperties> findAllOverriddenProperties();

    /**
     * Finds properties that are overridden on a specified {@link EstimationRule} and channel's {@link ReadingType}.
     *
     * @param estimationRule target {@link EstimationRule}
     * @param readingType target {@link ReadingType} of device's channel
     * @return {@link ChannelEstimationRuleOverriddenProperties} or Optional.empty() if no properties are overridden for specified estimation rule and reading type
     */
    Optional<? extends ChannelEstimationRuleOverriddenProperties> findOverriddenProperties(EstimationRule estimationRule, ReadingType readingType);

    /**
     * Finds {@link ChannelEstimationRuleOverriddenProperties} entity by id.
     *
     * @return {@link ChannelEstimationRuleOverriddenProperties} or Optional.empty() if no entity with such id
     */
    Optional<? extends ChannelEstimationRuleOverriddenProperties> findChannelEstimationRuleOverriddenProperties(long id);

    /**
     * Finds and locks {@link ChannelEstimationRuleOverriddenProperties} entity by id and version.
     *
     * @return {@link ChannelEstimationRuleOverriddenProperties} or Optional.empty() if no entity with such id and version
     */
    Optional<? extends ChannelEstimationRuleOverriddenProperties> findAndLockChannelEstimationRuleOverriddenProperties(long id, long version);

    /**
     * Starts the process of overriding {@link EstimationRule}'s properties on {@link EstimationPropertyDefinitionLevel#TARGET_OBJECT},
     * which is a device's channel identified by {@link ReadingType} in this context.
     *
     * @param estimationRule target {@link EstimationRule} for which the properties are going to be redefined
     * @param readingType target {@link ReadingType} of device's channel
     * @return {@link PropertyOverrider}
     */
    PropertyOverrider overridePropertiesFor(EstimationRule estimationRule, ReadingType readingType);

    /**
     * Builder for {@link ChannelEstimationRuleOverriddenProperties}
     */
    @ProviderType
    interface PropertyOverrider {

        PropertyOverrider override(String propertyName, Object propertyValue);

        ChannelEstimationRuleOverriddenProperties complete();
    }
}
