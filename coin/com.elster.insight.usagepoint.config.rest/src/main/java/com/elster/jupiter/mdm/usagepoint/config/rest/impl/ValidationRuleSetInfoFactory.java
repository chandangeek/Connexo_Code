/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.config.rest.impl;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.usagepoint.lifecycle.rest.UsagePointLifeCycleStateInfo;
import com.elster.jupiter.util.streams.Functions;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationRuleSetVersion;
import com.elster.jupiter.validation.ValidationVersionStatus;
import com.elster.jupiter.validation.rest.ValidationRuleSetVersionInfo;

import com.google.common.collect.ImmutableMap;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ValidationRuleSetInfoFactory {
    @Inject
    public ValidationRuleSetInfoFactory() {
    }

    public ValidationRuleSetInfo from(ValidationRuleSet validationRuleSet, Set<ReadingType> readingTypesForValidation) {
        ValidationRuleSetInfo info = new ValidationRuleSetInfo();
        info.id = validationRuleSet.getId();
        info.name = validationRuleSet.getName();
        info.description = validationRuleSet.getDescription();
        List<? extends ValidationRuleSetVersion> versions = validationRuleSet.getRuleSetVersions();
        Map<ValidationVersionStatus, List<ValidationRuleSetVersion>> versionsByStatus = versions.stream()
                .collect(Collectors.groupingBy(ValidationRuleSetVersion::getStatus));
        Map<ValidationVersionStatus, Function<Stream<ValidationRuleSetVersion>, Optional<ValidationRuleSetVersion>>> desiredOnes = ImmutableMap.of(
                ValidationVersionStatus.CURRENT, stream -> stream.reduce((a, b) -> b), // findLast
                ValidationVersionStatus.FUTURE, Stream::findFirst,
                ValidationVersionStatus.PREVIOUS, stream -> stream.reduce((a, b) -> b) // findLast
        );

        Optional<ValidationRuleSetVersion> matchingVersion = desiredOnes.entrySet().stream()
                .map(partitionAndTerminalOperation -> Optional.ofNullable(versionsByStatus.get(partitionAndTerminalOperation.getKey()))
                        .flatMap(versionList -> findMatchingVersion(versionList, readingTypesForValidation, partitionAndTerminalOperation.getValue())))
                .flatMap(Functions.asStream())
                .findFirst();
        matchingVersion.ifPresent(ver -> {
            info.hasCurrent = true;
            info.currentVersionId = ver.getId();
            info.currentVersion = new ValidationRuleSetVersionInfo(ver);
            Optional.ofNullable(ver.getStartDate()).ifPresent(sd -> info.startDate = sd.toEpochMilli());
            Optional.ofNullable(ver.getEndDate()).ifPresent(ed -> info.endDate = ed.toEpochMilli());
        });
        info.numberOfVersions = versions.size();
        info.version = validationRuleSet.getVersion();
        return info;
    }

    private static Optional<ValidationRuleSetVersion> findMatchingVersion(List<ValidationRuleSetVersion> versions,
                                                                          Set<ReadingType> readingTypesForValidation,
                                                                          Function<Stream<ValidationRuleSetVersion>, Optional<ValidationRuleSetVersion>> terminal) {
        return terminal.apply(versions.stream()
                .filter(version -> version.getRules().stream()
                        .anyMatch((ValidationRule rule) -> readingTypesForValidation.stream()
                                .anyMatch(rule::appliesTo))));

    }

    public ValidationRuleSetInfo from(ValidationRuleSet validationRuleSet, Set<ReadingType> readingTypesForValidation,
                                      List<UsagePointLifeCycleStateInfo> lifeCycleStates) {
        ValidationRuleSetInfo info = from(validationRuleSet, readingTypesForValidation);
        info.lifeCycleStates = lifeCycleStates;
        return info;
    }
}
