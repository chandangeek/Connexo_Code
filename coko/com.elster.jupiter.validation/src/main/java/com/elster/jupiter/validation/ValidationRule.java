package com.elster.jupiter.validation;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.properties.PropertySpec;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

@ProviderType
public interface ValidationRule {
    long getId();

    boolean isActive();

    ValidationAction getAction();

    String getImplementation();

    String getDisplayName();

    void activate();

    void deactivate();

    ValidationRuleSet getRuleSet();

    ValidationRuleSetVersion getRuleSetVersion();

    List<ValidationRuleProperties> getProperties();

    ValidationRuleProperties addProperty(String name, Object value);

    Map<String, Object> getProps();

    Set<ReadingType> getReadingTypes();

    ReadingTypeInValidationRule addReadingType(ReadingType readingType);

    ReadingTypeInValidationRule addReadingType(String mRID);

    void deleteReadingType(ReadingType readingType);

    boolean isRequired(String propertyKey);

    String getName();

    Instant getObsoleteDate();

    long getVersion();

    List<PropertySpec> getPropertySpecs();

    boolean isObsolete();
}
