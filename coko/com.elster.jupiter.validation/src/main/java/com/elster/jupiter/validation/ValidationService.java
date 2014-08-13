package com.elster.jupiter.validation;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingQuality;
import com.elster.jupiter.metering.readings.BaseReading;
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

    void setMeterValidationStatus(MeterActivation meterActivation, boolean status);

    Date getLastChecked(MeterActivation meterActivation);

    void setLastChecked(MeterActivation meterActivation, Date date);

    Optional<ValidationRuleSet> getValidationRuleSet(long id);

    List<ValidationRuleSet> getValidationRuleSets();

    List<Validator> getAvailableValidators();

    void validate(MeterActivation meterActivation, Interval interval);

    Optional<ValidationRule> getValidationRule(long id);

    Query<ValidationRuleSet> getRuleSetQuery();

    Optional<ValidationRuleSet> getValidationRuleSet(String name);

    Validator getValidator(String implementation);

    List<List<ReadingQuality>> getValidationStatus(Channel channel, List<? extends BaseReading> readings);

    List<? extends MeterActivationValidation> getMeterActivationValidations(MeterActivation meterActivation);

    List<? extends MeterActivationValidation> getActiveMeterActivationValidations(MeterActivation meterActivation);

    List<MeterActivationValidation> getOrCreateMeterActivationValidations(MeterActivation meterActivation);
}
