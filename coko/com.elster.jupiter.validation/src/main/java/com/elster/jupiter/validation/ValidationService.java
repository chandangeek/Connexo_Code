package com.elster.jupiter.validation;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.util.time.Interval;
import com.google.common.base.Optional;

import java.util.Date;
import java.util.List;

public interface ValidationService {

    String COMPONENTNAME = "VAL";

    ValidationRuleSet createValidationRuleSet(String name);

    ValidationRuleSet createValidationRuleSet(String name, String description);

    MeterValidation createMeterValidation(MeterActivation activation);

    Optional<MeterValidation> getMeterValidation(MeterActivation meterActivation);

    void disableMeterValidation(MeterActivation meterActivation);

    void enableMeterValidation(MeterActivation meterActivation, Optional<Date> date);

    Date getLastChecked(MeterActivation meterActivation);

    Optional<ValidationRuleSet> getValidationRuleSet(long id);

    List<ValidationRuleSet> getValidationRuleSets();

    List<Validator> getAvailableValidators();

    void validate(MeterActivation meterActivation, Interval interval);

    Optional<ValidationRule> getValidationRule(long id);

    Query<ValidationRuleSet> getRuleSetQuery();

    Optional<ValidationRuleSet> getValidationRuleSet(String name);

    Validator getValidator(String implementation);

    List<MeterActivationValidation> getMeterActivationValidationsForMeterActivation(MeterActivation meterActivation);
}
