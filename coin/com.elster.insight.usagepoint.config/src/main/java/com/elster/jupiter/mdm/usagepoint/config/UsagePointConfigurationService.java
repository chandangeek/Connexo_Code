package com.elster.jupiter.mdm.usagepoint.config;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.validation.ValidationRuleSet;

import aQute.bnd.annotation.ProviderType;

import java.util.List;
import java.util.Optional;

@ProviderType
public interface UsagePointConfigurationService {

    String COMPONENTNAME = "UPC";

    void link(UsagePoint up, MetrologyConfiguration mc);

    Boolean unlink(UsagePoint up, MetrologyConfiguration mc);

    Optional<MetrologyConfiguration> findMetrologyConfigurationForUsagePoint(UsagePoint up);

    boolean isInUse(MetrologyConfiguration metrologyConfiguration);

    List<MetrologyConfiguration> findMetrologyConfigurationsForValidationRuleSet(ValidationRuleSet rs);

    /**
     * Gets the {@link ValidationRuleSet}s that are being used by the specified {@link MetrologyConfiguration}.
     *
     * @param metrologyConfiguration The MetrologyConfiguration
     * @return The List of ValidationRuleSet
     */
    List<ValidationRuleSet> getValidationRuleSets(MetrologyConfiguration metrologyConfiguration);

    /**
     * Adds the specified {@link ValidationRuleSet} to the specified {@link MetrologyConfiguration}.
     *
     * @param metrologyConfiguration The MetrologyConfiguration
     * @param validationRuleSet The ValidationRuleSet
     */
    void addValidationRuleSet(MetrologyConfiguration metrologyConfiguration, ValidationRuleSet validationRuleSet);

    /**
     * Removes the specified {@link ValidationRuleSet} from the specified {@link MetrologyConfiguration}.
     *
     * @param metrologyConfiguration The MetrologyConfiguration
     * @param validationRuleSet The ValidationRuleSet
     */
    void removeValidationRuleSet(MetrologyConfiguration metrologyConfiguration, ValidationRuleSet validationRuleSet);

}