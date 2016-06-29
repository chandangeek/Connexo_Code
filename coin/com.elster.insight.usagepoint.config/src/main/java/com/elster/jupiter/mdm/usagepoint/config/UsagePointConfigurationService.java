package com.elster.jupiter.mdm.usagepoint.config;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.validation.ValidationRuleSet;

import aQute.bnd.annotation.ProviderType;

import java.util.List;
import java.util.Optional;

@ProviderType
public interface UsagePointConfigurationService {

    String COMPONENTNAME = "UPC";

    void link(UsagePoint up, UsagePointMetrologyConfiguration mc);

    Optional<UsagePointMetrologyConfiguration> findMetrologyConfigurationForUsagePoint(UsagePoint up);

    boolean isInUse(MetrologyConfiguration metrologyConfiguration);

    /**
     * Gets the {@link ValidationRuleSet}s that are being used by the specified {@link MetrologyContract}.
     *
     * @param metrologyContract The MetrologyContract
     * @return The List of ValidationRuleSet
     */
    List<ValidationRuleSet> getValidationRuleSets(MetrologyContract metrologyContract);

    /**
     * Adds the specified {@link ValidationRuleSet} to the specified {@link MetrologyContract}.
     *
     * @param metrologyContract The MetrologyContract
     * @param validationRuleSet The ValidationRuleSet
     */
    void addValidationRuleSet(MetrologyContract metrologyContract, ValidationRuleSet validationRuleSet);

    /**
     * Removes the specified {@link ValidationRuleSet} from the specified {@link MetrologyConfiguration}.
     *
     * @param metrologyContract The MetrologyContract
     * @param validationRuleSet The ValidationRuleSet
     */
    void removeValidationRuleSet(MetrologyContract metrologyContract, ValidationRuleSet validationRuleSet);

    boolean isLinkableValidationRuleSet(MetrologyContract metrologyContract, ValidationRuleSet validationRuleSet, List<ValidationRuleSet> linkedValidationRuleSets);

    boolean isValidationRuleSetInUse(ValidationRuleSet ruleset);
}