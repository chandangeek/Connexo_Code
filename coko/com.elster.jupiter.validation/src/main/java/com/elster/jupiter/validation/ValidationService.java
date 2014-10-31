package com.elster.jupiter.validation;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ValidationService {

    String COMPONENTNAME = "VAL";

    /**
     * Mangagement of ruleSets and ules *
     */

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

    Optional<ValidationRule> getValidationRule(long id);

    /**
     * Activation/Deactivation of validation on meters *
     */

    void activateValidation(Meter meter);

    void deactivateValidation(Meter meter);

    boolean validationEnabled(Meter meter);

    /* last checked */

    Optional<Instant> getLastChecked(MeterActivation meterActivation);

    void updateLastChecked(MeterActivation meterActivation, Instant date);

    void updateLastChecked(Channel channel, Instant date);

    void validate(MeterActivation meterActivation);
    
    @Deprecated
    void validate(MeterActivation meterActivation, Range<Instant> interval);

    @Deprecated
    void validate(MeterActivation meterActivation, String readingTypeCode, Range<Instant> interval);

    @Deprecated
    List<? extends MeterActivationValidation> getMeterActivationValidations(MeterActivation meterActivation);

    @Deprecated
    List<? extends MeterActivationValidation> getActiveMeterActivationValidations(MeterActivation meterActivation);

    ValidationEvaluator getEvaluator();

    ValidationEvaluator getEvaluator(Meter meter, Range<Instant> interval);

    /*
     * Following methods for adding resources in a non OSGI environment
     */
    
	void addValidatorFactory(ValidatorFactory validatorfactory);
	void addValidationRuleSetResolver(ValidationRuleSetResolver resolver);
}
