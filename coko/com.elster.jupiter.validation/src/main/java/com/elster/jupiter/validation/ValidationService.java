package com.elster.jupiter.validation;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.util.time.Interval;
import com.google.common.base.Optional;

import java.util.Date;
import java.util.List;

public interface ValidationService {

    String COMPONENTNAME = "VAL";

    /**
     * Mangagement of ruleSets and ules *
     */

    ValidationRuleSet createValidationRuleSet(String name);

    ValidationRuleSet createValidationRuleSet(String name, String description);

    List<ValidationRuleSet> getValidationRuleSets();

    Optional<Date> getLastChecked(Channel channel);

    boolean isValidationActive(Channel channel);

    Optional<ValidationRuleSet> getValidationRuleSet(long id);

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

    Optional<Date> getLastChecked(MeterActivation meterActivation);

    void updateLastChecked(MeterActivation meterActivation, Date date);

    void updateLastChecked(Channel channel, Date date);

    void validate(MeterActivation meterActivation, Interval interval);

    void validate(MeterActivation meterActivation, String readingTypeCode, Interval interval);

    List<? extends MeterActivationValidation> getMeterActivationValidations(MeterActivation meterActivation);

    List<? extends MeterActivationValidation> getActiveMeterActivationValidations(MeterActivation meterActivation);

    ValidationEvaluator getEvaluator();
}
