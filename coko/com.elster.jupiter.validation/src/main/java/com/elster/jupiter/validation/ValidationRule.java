package com.elster.jupiter.validation;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.properties.PropertySpec;

import java.util.Date;
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

    void rename(String name);

    void setAction(ValidationAction action);

    void setImplementation(String implementation);

    void setPosition(int position);

    List<ValidationRuleProperties> getProperties();

    ValidationRuleProperties addProperty(String name, Object value);

    void setProperties(Map<String, Object> map);

    void deleteProperty(ValidationRuleProperties property);

    Map<String, Object> getProps();

    Set<ReadingType> getReadingTypes();

    ReadingTypeInValidationRule addReadingType(ReadingType readingType);

    ReadingTypeInValidationRule addReadingType(String mRID);

    void deleteReadingType(ReadingType readingType);

    boolean isRequired(String propertyKey);

    String getName();

    Date getObsoleteDate();

    long getVersion();

    List<PropertySpec> getPropertySpecs();
}
