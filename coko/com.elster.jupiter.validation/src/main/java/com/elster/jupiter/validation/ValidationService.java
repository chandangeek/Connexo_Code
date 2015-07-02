package com.elster.jupiter.validation;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@ProviderType
public interface ValidationService {

    String COMPONENTNAME = "VAL";

    /**
     * Management of ruleSets and rules *
     */

    DestinationSpec getDestination();

    ValidationRuleSet createValidationRuleSet(String name);

    ValidationRuleSet createValidationRuleSet(String name, String description);

    List<ValidationRuleSet> getValidationRuleSets();

    Optional<Instant> getLastChecked(Channel channel);

    boolean isValidationActive(Channel channel);

    Optional<? extends ValidationRuleSet> getValidationRuleSet(long id);

    Optional<ValidationRuleSet> getValidationRuleSet(String name);


    boolean isValidationRuleSetInUse(ValidationRuleSet validationRuleSet);

    Query<ValidationRuleSet> getRuleSetQuery();

    List<Validator> getAvailableValidators();

    Validator getValidator(String implementation);

    /**
     * Activation/Deactivation of validation on meters *
     */

    void activateValidation(Meter meter);
    void deactivateValidation(Meter meter);

    void enableValidationOnStorage(Meter meter);
    void disableValidationOnStorage(Meter meter);


    void activate(MeterActivation meterActivation, ValidationRuleSet ruleSet);
    void deactivate(MeterActivation meterActivation, ValidationRuleSet ruleSet);

    List<ValidationRuleSet> activeRuleSets(MeterActivation meterActivation);

    boolean validationEnabled(Meter meter);

    boolean validationOnStorageEnabled(Meter meter);

    /* last checked */

    Optional<Instant> getLastChecked(MeterActivation meterActivation);

    void updateLastChecked(MeterActivation meterActivation, Instant date);

    void updateLastChecked(Channel channel, Instant date);

    void validate(MeterActivation meterActivation);
    
    void validate(MeterActivation meterActivation, ReadingType readingType);
    
    ValidationEvaluator getEvaluator();

    ValidationEvaluator getEvaluator(Meter meter, Range<Instant> interval);

    /*
     * Following methods for adding resources in a non OSGI environment
     */
    
	void addValidatorFactory(ValidatorFactory validatorfactory);
	void addValidationRuleSetResolver(ValidationRuleSetResolver resolver);

    DataValidationTaskBuilder newTaskBuilder();

    List<DataValidationTask> findValidationTasks();

    Query<DataValidationTask> findValidationTasksQuery();

    Optional<DataValidationTask> findValidationTask(long id);

    Optional<DataValidationTask> findValidationTaskByName(String name);

    Thesaurus getThesaurus();

    DataValidationOccurrence createValidationOccurrence(TaskOccurrence taskOccurrence);

    Optional<DataValidationOccurrence> findDataValidationOccurrence(TaskOccurrence occurrence);


    List<ValidationSummary> getValidationResultsOfDeviceGroup(Long groupId, Optional<Integer> start, Optional<Integer> limit);

}
