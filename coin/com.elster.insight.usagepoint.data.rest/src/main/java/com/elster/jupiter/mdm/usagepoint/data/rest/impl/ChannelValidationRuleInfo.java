/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.validation.ValidationAction;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class ChannelValidationRuleInfo implements Comparable<ChannelValidationRuleInfo> {

    public Long id;
    public Long version;
    public Long ruleId;
    public String name;
    public String validator;
    public ReadingTypeInfo readingType;
    public DataQualityLevel dataQualityLevel;
    public boolean isEffective;
    public boolean isActive;
    public List<OverriddenPropertyInfo> properties;

    @Override
    public int compareTo(ChannelValidationRuleInfo another) {
        return Comparator.<ChannelValidationRuleInfo, Boolean>comparing(info -> !info.isActive)
                .thenComparing(info -> info.name.toLowerCase())
                .thenComparing(info -> info.dataQualityLevel)
                .compare(this, another);
    }

    Key getKey() {
        return new Key(name, validator, dataQualityLevel);
    }

    static ChannelValidationRuleInfo chooseEffectiveOne(ChannelValidationRuleInfo info1, ChannelValidationRuleInfo info2) {
        return effectivityComparator().compare(info1, info2) >= 0 ? info1 : info2;
    }

    static Comparator<ChannelValidationRuleInfo> effectivityComparator() {
        return Comparator.<ChannelValidationRuleInfo, Boolean>comparing(info -> info.isEffective).thenComparing(info -> info.isActive);
    }

    static class Key {

        private final String name;
        private final String validator;
        private final DataQualityLevel level;

        Key(String name, String validator, DataQualityLevel level) {
            this.name = name;
            this.validator = validator;
            this.level = level;
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
            return Objects.equals(name, key.name) &&
                    Objects.equals(validator, key.validator) &&
                    level == key.level;
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, validator, level);
        }
    }

    enum DataQualityLevel {
        SUSPECT(ValidationAction.FAIL, "Suspect"),
        INFORMATIVE(ValidationAction.WARN_ONLY, "Informative");

        private final ValidationAction validationAction;
        private final String jsonName;

        DataQualityLevel(ValidationAction validationAction, String jsonName) {
            this.validationAction = validationAction;
            this.jsonName = jsonName;
        }

        @JsonCreator
        public static DataQualityLevel forValue(String jsonValue) {
            return Stream.of(values())
                    .filter(level -> level.jsonName.equals(jsonValue))
                    .findAny()
                    .orElse(null);
        }

        public static DataQualityLevel forValue(ValidationAction validationAction) {
            return Stream.of(values())
                    .filter(level -> level.validationAction == validationAction)
                    .findAny()
                    .orElse(null);
        }

        @JsonValue
        public String toValue() {
            return jsonName;
        }
    }
}
