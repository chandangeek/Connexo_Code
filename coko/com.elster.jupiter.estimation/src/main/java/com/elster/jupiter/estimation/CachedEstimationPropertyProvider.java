/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation;

import com.elster.jupiter.metering.ReadingType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Default in-memory implementation of {@link EstimationPropertyProvider} based on map
 * that stores estimation properties per estimation rule's {@link Key}
 * which consists of: {@link ReadingType}, estimation rule name and estimator implementation
 */
public class CachedEstimationPropertyProvider implements EstimationPropertyProvider {

    private final Map<Key, Map<String, Object>> properties = new HashMap<>();

    public void setProperties(ReadingType readingType, String estimationRuleName, String estimatorImpl, Map<String, Object> properties) {
        Key key = new Key(readingType, estimationRuleName, estimatorImpl);
        this.properties.computeIfAbsent(key, props -> new HashMap<>()).putAll(properties);
    }

    @Override
    public Map<String, Object> getProperties(EstimationRule estimationRule, ReadingType readingType) {
        Key key = new Key(readingType, estimationRule.getName(), estimationRule.getImplementation());
        return this.properties.containsKey(key) ? Collections.unmodifiableMap(this.properties.get(key)) : Collections.emptyMap();
    }

    static final class Key {
        private final ReadingType readingType;
        private final String ruleName;
        private final String estimatorImpl;

        Key(ReadingType readingType, String ruleName, String estimatorImpl) {
            this.readingType = readingType;
            this.ruleName = ruleName;
            this.estimatorImpl = estimatorImpl;
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
                    Objects.equals(estimatorImpl, key.estimatorImpl);
        }

        @Override
        public int hashCode() {
            return Objects.hash(readingType, ruleName, estimatorImpl);
        }
    }
}
