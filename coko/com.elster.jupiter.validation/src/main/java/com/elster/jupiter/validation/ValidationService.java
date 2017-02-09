/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.tasks.TaskOccurrence;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@ProviderType
public interface ValidationService {

    String COMPONENTNAME = "VAL";

    /**
     * Management of ruleSets and rules *
     */

    ValidationRuleSet createValidationRuleSet(String name, QualityCodeSystem qualityCodeSystem);

    ValidationRuleSet createValidationRuleSet(String name, QualityCodeSystem qualityCodeSystem, String description);

    List<ValidationRuleSet> getValidationRuleSets();

    Optional<Instant> getLastChecked(Channel channel);

    boolean isValidationActive(Channel channel);

    Optional<? extends ValidationRuleSet> getValidationRuleSet(long id);

    Optional<? extends ValidationRuleSet> findAndLockValidationRuleSetByIdAndVersion(long id, long version);

    Optional<ValidationRuleSet> getValidationRuleSet(String name);

    boolean isValidationRuleSetInUse(ValidationRuleSet validationRuleSet);

    Query<ValidationRuleSet> getRuleSetQuery();

    List<Validator> getAvailableValidators();

    /**
     * Filters validators and returns ones supporting a given <code>qualityCodeSystem</code>.
     *
     * @param qualityCodeSystem a target QualityCodeSystem.
     * @return the list of validators supporting a given <code>qualityCodeSystem</code>.
     */
    List<Validator> getAvailableValidators(QualityCodeSystem qualityCodeSystem);

    Validator getValidator(String implementation);

    /**
     * Activation/Deactivation of validation on meters *
     */

    void activateValidation(Meter meter);

    void deactivateValidation(Meter meter);

    void enableValidationOnStorage(Meter meter);

    void disableValidationOnStorage(Meter meter);

    void activate(ChannelsContainer channelsContainer, ValidationRuleSet ruleSet);

    void deactivate(ChannelsContainer channelsContainer, ValidationRuleSet ruleSet);

    List<ValidationRuleSet> activeRuleSets(ChannelsContainer channelsContainer);

    boolean validationEnabled(Meter meter);

    boolean isValidationActive(ChannelsContainer channelsContainer);

    List<Meter> validationEnabledMetersIn(List<String> meterMrids);

    boolean validationOnStorageEnabled(Meter meter);

    /* last checked */

    Optional<Instant> getLastChecked(ChannelsContainer channelsContainer);

    Optional<Instant> getLastValidationRun(ChannelsContainer channelsContainer);

    void updateLastChecked(ChannelsContainer channelsContainer, Instant date);

    void updateLastChecked(Channel channel, Instant date);

    /**
     * Validates all channels in the given {@code channelsContainer}.
     *
     * @param targetQualityCodeSystems Set of desired QualityCodeSystems (only rulesets with these QualityCodeSystems will be applied).
     * It can be empty (in that case engine will use rulesets with any QualityCodeSystem).
     * @param channelsContainer Container with channels to validate.
     */
    void validate(Set<QualityCodeSystem> targetQualityCodeSystems, ChannelsContainer channelsContainer);

    /**
     * Validates channel with specific {@code readingType} in the given {@code channelsContainer}.
     *
     * @param targetQualityCodeSystems Set of desired QualityCodeSystems (only rulesets with these QualityCodeSystems will be applied).
     * It can be empty (in that case engine will use rulesets with any QualityCodeSystem).
     * @param channelsContainer Container with channels to validate.
     * @param readingType channel's reading type
     */
    void validate(Set<QualityCodeSystem> targetQualityCodeSystems, ChannelsContainer channelsContainer, ReadingType readingType);

    void validate(ValidationContext validationContext, Instant date);

    ValidationEvaluator getEvaluator();

    ValidationEvaluator getEvaluator(Meter meter);

    void addValidatorFactory(ValidatorFactory validatorfactory);

    void addValidationRuleSetResolver(ValidationRuleSetResolver resolver);

    DataValidationTaskBuilder newTaskBuilder();

    List<DataValidationTask> findValidationTasks();

    Query<DataValidationTask> findValidationTasksQuery();

    Optional<DataValidationTask> findValidationTask(long id);

    Optional<DataValidationTask> findAndLockValidationTaskByIdAndVersion(long id, long version);

    Optional<DataValidationTask> findValidationTaskByName(String name);

    DataValidationOccurrence createValidationOccurrence(TaskOccurrence taskOccurrence);

    Optional<DataValidationOccurrence> findDataValidationOccurrence(TaskOccurrence occurrence);

    Optional<? extends ValidationRuleSetVersion> findValidationRuleSetVersion(long id);

    Optional<? extends ValidationRuleSetVersion> findAndLockValidationRuleSetVersionByIdAndVersion(long id, long version);

    Optional<? extends ValidationRule> findValidationRule(long id);

    Optional<? extends ValidationRule> findAndLockValidationRuleByIdAndVersion(long id, long version);

    List<DataValidationTask> findByDeviceGroup(EndDeviceGroup endDevice, int skip, int limit);

}
