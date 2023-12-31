/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.config;

import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.validation.ValidationRuleSet;

import aQute.bnd.annotation.ProviderType;

import java.util.List;
import java.util.Optional;

@ProviderType
public interface UsagePointConfigurationService {

    String COMPONENTNAME = "UPC";

    void link(UsagePoint up, UsagePointMetrologyConfiguration mc);

    Boolean unlink(UsagePoint up, UsagePointMetrologyConfiguration mc);

    Optional<MetrologyConfiguration> findMetrologyConfigurationForUsagePoint(UsagePoint up);

    boolean isInUse(MetrologyConfiguration metrologyConfiguration);

    /**
     * Gets the {@link ValidationRuleSet}s that are being used by the specified {@link MetrologyContract}.
     *
     * @param metrologyContract The MetrologyContract
     * @return The List of ValidationRuleSet
     */
    List<ValidationRuleSet> getValidationRuleSets(MetrologyContract metrologyContract);

    /**
     * Gets the {@link ValidationRuleSet}s that are being used by the specified {@link MetrologyContract} and {@link State}.
     *
     * @param metrologyContract The MetrologyContract
     * @return The List of ValidationRuleSet
     */
    List<ValidationRuleSet> getValidationRuleSets(MetrologyContract metrologyContract, State state);


    List<ValidationRuleSet> getActiveValidationRuleSets(MetrologyContract metrologyContract, ChannelsContainer channelsContainer);
    /**
     * Adds the specified {@link ValidationRuleSet} to the specified {@link MetrologyContract}.
     *
     * @param metrologyContract The MetrologyContract
     * @param validationRuleSet The ValidationRuleSet
     */
    void addValidationRuleSet(MetrologyContract metrologyContract, ValidationRuleSet validationRuleSet);

    /**
     * Adds the specified {@link ValidationRuleSet} to the specified {@link MetrologyContract}.
     *
     * @param metrologyContract The MetrologyContract
     * @param validationRuleSet The ValidationRuleSet
     */
    void addValidationRuleSet(MetrologyContract metrologyContract, ValidationRuleSet validationRuleSet, List<State> states);

    /**
     * Removes the specified {@link ValidationRuleSet} from the specified {@link MetrologyConfiguration}.
     *
     * @param metrologyContract The MetrologyContract
     * @param validationRuleSet The ValidationRuleSet
     */
    void removeValidationRuleSet(MetrologyContract metrologyContract, ValidationRuleSet validationRuleSet);

    @Deprecated
    boolean isLinkableValidationRuleSet(MetrologyContract metrologyContract, ValidationRuleSet validationRuleSet, List<ValidationRuleSet> linkedValidationRuleSets);

    boolean isValidationRuleSetInUse(ValidationRuleSet ruleset);

    /**
     *
     * @param metrologyContract
     * @param validationRuleSet
     * @return list of reading matched reading type deliverables of metrology contract and validation rule set
     */
    List<ReadingTypeDeliverable> getMatchingDeliverablesOnValidationRuleSet(MetrologyContract metrologyContract, ValidationRuleSet validationRuleSet);

    /**
     *
     * @param metrologyContract
     * @param estimationRuleSet
     * @return list of reading matched reading type deliverables of metrology contract and estimation rule set
     */
    List<ReadingTypeDeliverable> getMatchingDeliverablesOnEstimationRuleSet(MetrologyContract metrologyContract, EstimationRuleSet estimationRuleSet);

    /**
     * Gets the {@link EstimationRuleSet}s that are being used by the specified {@link MetrologyContract}.
     *
     * @param metrologyContract The MetrologyContract
     * @return The List of EstimationRuleSet
     */
    List<EstimationRuleSet> getEstimationRuleSets(MetrologyContract metrologyContract);

    /**
     * Adds the specified {@link EstimationRuleSet} to the specified {@link MetrologyContract}.
     *
     * @param metrologyContract The MetrologyContract
     * @param estimationRuleSet The EstimationRuleSet
     */
    void addEstimationRuleSet(MetrologyContract metrologyContract, EstimationRuleSet estimationRuleSet);

    /**
     * Removes the specified {@link EstimationRuleSet} from the specified {@link MetrologyConfiguration}.
     *
     * @param metrologyContract The MetrologyContract
     * @param estimationRuleSet The EstimationRuleSet
     */
    void removeEstimationRuleSet(MetrologyContract metrologyContract, EstimationRuleSet estimationRuleSet);

    /**
     * Reorders the {@link EstimationRuleSet}s from the specified {@link MetrologyConfiguration}.
     *
     * @param metrologyContract The MetrologyContract
     * @param newOrder The list of EstimationRuleSets with new order
     */
    void reorderEstimationRuleSets(MetrologyContract metrologyContract, List<EstimationRuleSet> newOrder);

    boolean isLinkableEstimationRuleSet(MetrologyContract metrologyContract, EstimationRuleSet estimationRuleSet, List<EstimationRuleSet> linkedEstimationRuleSets);

    boolean isEstimationRuleSetInUse(EstimationRuleSet ruleset);

    /**
     * @return list of {@link MetrologyContract} linked to specified validation rule set
     * @param validationRuleSet Validation rule set
     */
    List<MetrologyContract> getMetrologyContractsLinkedToValidationRuleSet(ValidationRuleSet validationRuleSet);

    /**
     * @return list of {@link State}s linked to specified validation rule set and metrology contract
     * @param validationRuleSet Validation rule set
     */
    List<State> getStatesLinkedToValidationRuleSetAndMetrologyContract(ValidationRuleSet validationRuleSet, MetrologyContract metrologyContract);

    /**
     * @return list of {@link MetrologyContract} linked to specified estimation rule set
     * @param estimationRuleSet Estimation rule set
     */
    List<MetrologyContract> getMetrologyContractsLinkedToEstimationRuleSet(EstimationRuleSet estimationRuleSet);
}
