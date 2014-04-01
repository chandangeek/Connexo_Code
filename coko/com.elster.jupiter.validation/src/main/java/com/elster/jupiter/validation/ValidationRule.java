package com.elster.jupiter.validation;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.util.units.Quantity;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ValidationRule {
    long getId();

    boolean isActive();

    ValidationAction getAction();

    String getImplementation();

    String getDisplayName();

    void activate();

    void deactivate();

    ValidationRuleSet getRuleSet();

    void setAction(ValidationAction action);

    void setImplementation(String implementation);

    void setPosition(int position);

    List<ValidationRuleProperties> getProperties();

    ValidationRuleProperties addProperty(String name, Quantity value);

    void deleteProperty(ValidationRuleProperties property);

    Map<String, Quantity> getProps();

    Set<ReadingType> getReadingTypes();

    ReadingTypeInValidationRule addReadingType(ReadingType readingType);

    ReadingTypeInValidationRule addReadingType(String mRID);

    void deleteReadingType(ReadingType readingType);

    boolean isRequired(String propertyKey);

    String getName();
}
