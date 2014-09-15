package com.elster.jupiter.validation;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.metering.*;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.util.time.Interval;
import com.google.common.base.Optional;

import java.util.Collection;
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

    Optional<ValidationRuleSet> getValidationRuleSet(long id);

    Optional<ValidationRuleSet> getValidationRuleSet(String name);

    boolean isValidationRuleSetInUse(ValidationRuleSet validationRuleSet);

    Query<ValidationRuleSet> getRuleSetQuery();

    List<DataValidationStatus> getValidationStatus(Channel channel, Interval interval);

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

    void validate(MeterActivation meterActivation, Interval interval);

    List<DataValidationStatus> getValidationStatus(Channel channel, List<? extends BaseReading> readings);

    List<? extends MeterActivationValidation> getMeterActivationValidations(MeterActivation meterActivation);

    List<? extends MeterActivationValidation> getActiveMeterActivationValidations(MeterActivation meterActivation);

    boolean isAllDataValidated(MeterActivation meterActivation);

    ValidationEvaluator getEvaluator();

    public boolean isValidationActive(Channel channel);
}
