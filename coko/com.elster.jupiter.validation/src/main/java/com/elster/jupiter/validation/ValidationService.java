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
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
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

    boolean isValidationRuleSetActiveOnDeviceConfig(long validationRuleSetId, long deviceConfigId);

    Query<ValidationRuleSet> getRuleSetQuery();

    List<Validator> getAvailableValidators();

    /**
     * Filters validators and returns ones supporting a given {@code qualityCodeSystem}.
     *
     * @param qualityCodeSystem A target {@link QualityCodeSystem}.
     * @return The list of validators supporting a given {@code qualityCodeSystem}.
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

    void activateValidation(ChannelsContainer channelsContainer);

    void deactivateValidation(ChannelsContainer channelsContainer);

    /**
     * Method to update validation statuses of channels in provided channels container.
     * NOTE: use only if necessary. validation status update already included into some
     * operations related to getting these status, so avoid double updating
     * since it is time/resource consuming.
     *
     * @param channelsContainer Container with channels to update validation status
     */
    void forceUpdateValidationStatus(ChannelsContainer channelsContainer);

    List<Meter> validationEnabledMetersIn(List<String> meterMrids);

    boolean validationOnStorageEnabled(Meter meter);

    /* last checked */

    Optional<Instant> getLastChecked(ChannelsContainer channelsContainer);

    Optional<Instant> getLastValidationRun(ChannelsContainer channelsContainer);

    /**
     * Please consider {@link #moveLastCheckedBefore(ChannelsContainer, Instant)} instead.
     */
    @Deprecated
    void updateLastChecked(ChannelsContainer channelsContainer, Instant date);

    void moveLastCheckedBefore(ChannelsContainer channelsContainer, Instant date);

    /**
     * Please consider {@link #moveLastCheckedBefore(Channel, Instant)} instead.
     */
    @Deprecated
    void updateLastChecked(Channel channel, Instant date);

    void moveLastCheckedBefore(Channel channel, Instant date);

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

    /**
     * Validates the scope of data corresponding to a given {@link ValidationContext} at most starting from a given date.
     *
     * @param validationContext Target {@link ValidationContext}.
     * @param validateAtMostFrom A minimum timestamp that requires (re)validation, i.e.
     * (re)validation will be performed starting from this timestamp inclusively.
     * However if current last checked timestamp for a part of given {@code validationContext} is before this timestamp,
     * validation will be performed starting from last checked timestamp exclusively.
     */
    void validate(ValidationContext validationContext, Instant validateAtMostFrom);

    /**
     * Validates the scope of data corresponding to a given {@link ValidationContext} on a given interval.
     *
     * @param validationContext Target {@link ValidationContext}.
     * @param interval An interval of data that requires (re)validation, i.e.
     * (re)validation will be performed starting from the start of a given interval inclusively until the end of interval.
     * However if current last checked timestamp for a part of given {@code validationContext} is before the start of interval,
     * validation will be performed starting from last checked timestamp exclusively.
     */
    void validate(ValidationContext validationContext, Range<Instant> interval);

    /**
     * Resets last checked date on {@link Channel Channels} specified in the given map before the beginning of corresponding {@link Range Ranges},
     * and re-validates the data again if 'validation-on-storage' is active on their {@link ChannelsContainer ChannelsContainers}.
     * Does not take channel dependencies into account, i.e. does not guarantee the order of validation.
     *
     * @param rangeByChannelMap A {@link Map} of {@link Channel Channels} to re-validate and corresponding time {@link Range Ranges}.
     */
    void validate(Map<Channel, Range<Instant>> rangeByChannelMap);

    /**
     * Resets last checked date on {@link Channel Channels} specified in the given map before the beginning of corresponding {@link Range Ranges},
     * and re-validates the data again if 'validation-on-storage' is active on the given {@link ChannelsContainer}.
     * Does not take channel dependencies into account, i.e. does not guarantee the order of validation.
     *
     * @param channelsContainer A {@link ChannelsContainer} that contains all channels present in {@code rangeByChannelMap}.
     * @param rangeByChannelMap A {@link Map} of {@link Channel Channels} to re-validate and corresponding time {@link Range Ranges}.
     */
    void validate(ChannelsContainer channelsContainer, Map<Channel, Range<Instant>> rangeByChannelMap);

    ValidationEvaluator getEvaluator();

    ValidationEvaluator getEvaluator(Meter meter);

    ValidationEvaluator getEvaluator(ChannelsContainer container);


    void addValidatorFactory(ValidatorFactory validatorfactory);

    void addValidationRuleSetResolver(ValidationRuleSetResolver resolver);

    DataValidationTaskBuilder newTaskBuilder();

    List<DataValidationTask> findValidationTasks();

    Query<DataValidationTask> findValidationTasksQuery();

    Optional<DataValidationTask> findValidationTask(long id);

    Optional<DataValidationTask> findAndLockValidationTaskByIdAndVersion(long id, long version);

    Optional<DataValidationTask> findValidationTaskByName(String name);

    Optional<DataValidationTask> findValidationTaskByRecurrentTaskId(long id);

    DataValidationOccurrence createValidationOccurrence(TaskOccurrence taskOccurrence);

    Optional<DataValidationOccurrence> findDataValidationOccurrence(TaskOccurrence occurrence);

    Optional<? extends ValidationRuleSetVersion> findValidationRuleSetVersion(long id);

    Optional<? extends ValidationRuleSetVersion> findAndLockValidationRuleSetVersionByIdAndVersion(long id, long version);

    Optional<? extends ValidationRule> findValidationRule(long id);

    Optional<? extends ValidationRule> findAndLockValidationRuleByIdAndVersion(long id, long version);

    List<DataValidationTask> findByDeviceGroup(EndDeviceGroup endDevice, int skip, int limit);

    List<ValidationRule> findValidationRules(Collection<Long> ids);
}
