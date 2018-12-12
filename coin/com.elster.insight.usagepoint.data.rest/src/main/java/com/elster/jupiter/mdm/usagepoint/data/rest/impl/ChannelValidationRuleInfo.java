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

public class ChannelValidationRuleInfo {

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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChannelValidationRuleInfo info = (ChannelValidationRuleInfo) o;
        return Objects.equals(name, info.name) &&
                Objects.equals(validator, info.validator) &&
                dataQualityLevel == info.dataQualityLevel;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, validator, dataQualityLevel);
    }

    static ChannelValidationRuleInfo chooseEffectiveOne(ChannelValidationRuleInfo info1, ChannelValidationRuleInfo info2) {
        return effectivityComparator().compare(info1, info2) >= 0 ? info1 : info2;
    }

    static Comparator<ChannelValidationRuleInfo> effectivityComparator() {
        return Comparator.<ChannelValidationRuleInfo, Boolean>comparing(info -> info.isEffective)
                .thenComparing(info -> info.isActive);
    }

    static Comparator<ChannelValidationRuleInfo> defaultComparator() {
        return Comparator.<ChannelValidationRuleInfo, Boolean>comparing(info -> !info.isActive)
                .thenComparing(info -> info.name.toLowerCase())
                .thenComparing(info -> info.dataQualityLevel);
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
