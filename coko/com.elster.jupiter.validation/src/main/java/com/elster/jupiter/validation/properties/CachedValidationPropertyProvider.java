/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.properties;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.validation.ValidationAction;
import com.elster.jupiter.validation.ValidationRule;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Default in-memory implementation of {@link ValidationPropertyProvider} based on map
 * that stores validation properties per validation rule's {@link Key}
 * which consists of: {@link ReadingType}, validation rule name, validator implementation and {@link ValidationAction}
 */
public class CachedValidationPropertyProvider implements ValidationPropertyProvider {

    private final Map<Key, Map<String, Object>> properties = new HashMap<>();

    public void setProperties(ReadingType readingType, String validationRuleName, String validatorImpl, ValidationAction validationAction, Map<String, Object> properties) {
        Key key = new Key(readingType, validationRuleName, validatorImpl, validationAction);
        this.properties.computeIfAbsent(key, props -> new HashMap<>()).putAll(properties);
    }

    @Override
    public Map<String, Object> getProperties(ValidationRule validationRule, ReadingType readingType) {
        Key key = new Key(readingType, validationRule.getName(), validationRule.getImplementation(), validationRule.getAction());
        return this.properties.containsKey(key) ? Collections.unmodifiableMap(this.properties.get(key)) : Collections.emptyMap();
    }

    static final class Key {
        private final ReadingType readingType;
        private final String ruleName;
        private final String validatorImpl;
        private final ValidationAction validationAction;

        Key(ReadingType readingType, String ruleName, String validatorImpl, ValidationAction validationAction) {
            this.readingType = readingType;
            this.ruleName = ruleName;
            this.validatorImpl = validatorImpl;
            this.validationAction = validationAction;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Key key = (Key) o;
            return Objects.equals(readingType, key.readingType) &&
                    Objects.equals(ruleName, key.ruleName) &&
                    Objects.equals(validatorImpl, key.validatorImpl) &&
                    validationAction == key.validationAction;
        }

        @Override
        public int hashCode() {
            return Objects.hash(readingType, ruleName, validatorImpl, validationAction);
        }
    }
}
