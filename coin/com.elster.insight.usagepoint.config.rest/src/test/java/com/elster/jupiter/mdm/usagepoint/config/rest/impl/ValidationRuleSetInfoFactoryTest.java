/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.config.rest.impl;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.usagepoint.lifecycle.rest.UsagePointLifeCycleStateInfo;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationRuleSetVersion;
import com.elster.jupiter.validation.ValidationVersionStatus;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ValidationRuleSetInfoFactoryTest {
    private static final Instant[] TIMESTAMPS = Stream.concat(
            IntStream.range(0, 6).mapToObj(Instant.EPOCH::plusSeconds),
            Stream.of((Instant) null))
            .toArray(Instant[]::new);
    @Mock
    private ValidationRuleSet vrs;
    private ValidationRuleSetVersion[] versions;
    private ValidationRule[] rules;
    @Mock
    private ReadingType readingType;
    @Mock
    private UsagePointLifeCycleStateInfo lifeCycleStateInfo;
    private ValidationRuleSetInfoFactory factory = new ValidationRuleSetInfoFactory();

    @Before
    public void setUp() {
        when(vrs.getId()).thenReturn(505L);
        when(vrs.getVersion()).thenReturn(404L);
        when(vrs.getName()).thenReturn("Imya lyubimoye moyo");
        when(vrs.getDescription()).thenReturn("Tvoyo imenno imya lyubimoye moyo");
        rules = new ValidationRule[TIMESTAMPS.length - 1];
        versions = new ValidationRuleSetVersion[rules.length];
        for (int i = 0; i < rules.length; ++i) {
            ValidationRule rule = mock(ValidationRule.class);
            when(rule.getId()).thenReturn((long) i);
            when(rule.appliesTo(readingType)).thenReturn(true);
            ValidationRuleSetVersion version = mock(ValidationRuleSetVersion.class);
            when(version.getId()).thenReturn((long) i);
            when(version.getRuleSet()).thenReturn(vrs);
            doReturn(Collections.singletonList(rule)).when(version).getRules();
            when(version.getStartDate()).thenReturn(TIMESTAMPS[i]);
            when(version.getEndDate()).thenReturn(TIMESTAMPS[i + 1]);
            rules[i] = rule;
            versions[i] = version;
        }
        doReturn(Arrays.asList(versions)).when(vrs).getRuleSetVersions();
        when(versions[0].getStatus()).thenReturn(ValidationVersionStatus.PREVIOUS);
        when(versions[1].getStatus()).thenReturn(ValidationVersionStatus.PREVIOUS);
        when(versions[2].getStatus()).thenReturn(ValidationVersionStatus.CURRENT);
        when(versions[3].getStatus()).thenReturn(ValidationVersionStatus.CURRENT);
        when(versions[4].getStatus()).thenReturn(ValidationVersionStatus.FUTURE);
        when(versions[5].getStatus()).thenReturn(ValidationVersionStatus.FUTURE);
    }

    @Test
    public void testGeneralInfo() {
        ValidationRuleSetInfo info = factory.from(vrs, Collections.singleton(readingType));

        assertThat(info.id).isEqualTo(505L);
        assertThat(info.version).isEqualTo(404L);
        assertThat(info.name).isEqualTo("Imya lyubimoye moyo");
        assertThat(info.description).isEqualTo("Tvoyo imenno imya lyubimoye moyo");
        assertThat(info.numberOfVersions).isEqualTo(6);
    }

    @Test
    public void testAllVersionsMatching() {
        ValidationRuleSetInfo info = factory.from(vrs, Collections.singleton(readingType));

        assertThat(info.hasCurrent).isTrue();
        assertThat(info.currentVersionId).isEqualTo(3);
        assertThat(info.currentVersion.id).isEqualTo(3);
        assertThat(info.currentVersion.status).isEqualTo(ValidationVersionStatus.CURRENT);
        assertThat(info.currentVersion.numberOfRules).isEqualTo(1);
        assertThat(info.startDate).isEqualTo(TIMESTAMPS[3].toEpochMilli());
        assertThat(info.endDate).isEqualTo(TIMESTAMPS[4].toEpochMilli());
    }

    @Test
    public void testOneVersionNotMatching() {
        when(rules[3].appliesTo(readingType)).thenReturn(false);

        ValidationRuleSetInfo info = factory.from(vrs, Collections.singleton(readingType));

        assertThat(info.hasCurrent).isTrue();
        assertThat(info.currentVersionId).isEqualTo(2);
        assertThat(info.currentVersion.id).isEqualTo(2);
        assertThat(info.currentVersion.status).isEqualTo(ValidationVersionStatus.CURRENT);
        assertThat(info.currentVersion.numberOfRules).isEqualTo(1);
        assertThat(info.startDate).isEqualTo(TIMESTAMPS[2].toEpochMilli());
        assertThat(info.endDate).isEqualTo(TIMESTAMPS[3].toEpochMilli());
    }

    @Test
    public void testFutureVersionMatching() {
        when(rules[3].appliesTo(readingType)).thenReturn(false);
        when(rules[2].appliesTo(readingType)).thenReturn(false);

        ValidationRuleSetInfo info = factory.from(vrs, Collections.singleton(readingType));

        assertThat(info.hasCurrent).isTrue();
        assertThat(info.currentVersionId).isEqualTo(4);
        assertThat(info.currentVersion.id).isEqualTo(4);
        assertThat(info.currentVersion.status).isEqualTo(ValidationVersionStatus.FUTURE);
        assertThat(info.currentVersion.numberOfRules).isEqualTo(1);
        assertThat(info.startDate).isEqualTo(TIMESTAMPS[4].toEpochMilli());
        assertThat(info.endDate).isEqualTo(TIMESTAMPS[5].toEpochMilli());
    }

    @Test
    public void testSecondFutureVersionMatching() {
        when(rules[3].appliesTo(readingType)).thenReturn(false);
        when(rules[2].appliesTo(readingType)).thenReturn(false);
        when(versions[4].getRules()).thenReturn(Collections.emptyList());

        ValidationRuleSetInfo info = factory.from(vrs, Collections.singleton(readingType));

        assertThat(info.hasCurrent).isTrue();
        assertThat(info.currentVersionId).isEqualTo(5);
        assertThat(info.currentVersion.id).isEqualTo(5);
        assertThat(info.currentVersion.status).isEqualTo(ValidationVersionStatus.FUTURE);
        assertThat(info.currentVersion.numberOfRules).isEqualTo(1);
        assertThat(info.startDate).isEqualTo(TIMESTAMPS[5].toEpochMilli());
        assertThat(info.endDate).isNull();
    }

    @Test
    public void testPreviousVersionMatching() {
        when(rules[3].appliesTo(readingType)).thenReturn(false);
        when(rules[2].appliesTo(readingType)).thenReturn(false);
        when(versions[4].getRules()).thenReturn(Collections.emptyList());
        when(versions[5].getRules()).thenReturn(Collections.emptyList());

        ValidationRuleSetInfo info = factory.from(vrs, Collections.singleton(readingType));

        assertThat(info.hasCurrent).isTrue();
        assertThat(info.currentVersionId).isEqualTo(1);
        assertThat(info.currentVersion.id).isEqualTo(1);
        assertThat(info.currentVersion.status).isEqualTo(ValidationVersionStatus.PREVIOUS);
        assertThat(info.currentVersion.numberOfRules).isEqualTo(1);
        assertThat(info.startDate).isEqualTo(TIMESTAMPS[1].toEpochMilli());
        assertThat(info.endDate).isEqualTo(TIMESTAMPS[2].toEpochMilli());
    }

    @Test
    public void testFirstPreviousVersionMatching() {
        when(rules[3].appliesTo(readingType)).thenReturn(false);
        when(rules[2].appliesTo(readingType)).thenReturn(false);
        when(versions[4].getRules()).thenReturn(Collections.emptyList());
        when(versions[5].getRules()).thenReturn(Collections.emptyList());
        when(rules[1].appliesTo(readingType)).thenReturn(false);

        ValidationRuleSetInfo info = factory.from(vrs, Collections.singleton(readingType));

        assertThat(info.hasCurrent).isTrue();
        assertThat(info.currentVersionId).isEqualTo(0);
        assertThat(info.currentVersion.id).isEqualTo(0);
        assertThat(info.currentVersion.status).isEqualTo(ValidationVersionStatus.PREVIOUS);
        assertThat(info.currentVersion.numberOfRules).isEqualTo(1);
        assertThat(info.startDate).isEqualTo(TIMESTAMPS[0].toEpochMilli());
        assertThat(info.endDate).isEqualTo(TIMESTAMPS[1].toEpochMilli());
    }

    @Test
    public void testOnlyOneVersion() {
        doReturn(Collections.singletonList(versions[0])).when(vrs).getRuleSetVersions();

        ValidationRuleSetInfo info = factory.from(vrs, Collections.singleton(readingType));

        assertThat(info.hasCurrent).isTrue();
        assertThat(info.currentVersionId).isEqualTo(0);
        assertThat(info.currentVersion.id).isEqualTo(0);
        assertThat(info.currentVersion.status).isEqualTo(ValidationVersionStatus.PREVIOUS);
        assertThat(info.currentVersion.numberOfRules).isEqualTo(1);
        assertThat(info.startDate).isEqualTo(TIMESTAMPS[0].toEpochMilli());
        assertThat(info.endDate).isEqualTo(TIMESTAMPS[1].toEpochMilli());
    }

    @Test
    public void testNoMatchingVersion() {
        when(rules[0].appliesTo(readingType)).thenReturn(false);
        when(rules[4].appliesTo(readingType)).thenReturn(false);
        when(rules[5].appliesTo(readingType)).thenReturn(false);
        when(versions[1].getRules()).thenReturn(Collections.emptyList());
        when(versions[2].getRules()).thenReturn(Collections.emptyList());
        when(versions[3].getRules()).thenReturn(Collections.emptyList());

        ValidationRuleSetInfo info = factory.from(vrs, Collections.singleton(readingType));

        assertThat(info.hasCurrent).isFalse();
        assertThat(info.currentVersionId).isNull();
        assertThat(info.currentVersion).isNull();
        assertThat(info.startDate).isNull();
        assertThat(info.endDate).isNull();
    }

    @Test
    public void testLifeCycleStateInfo() {
        ValidationRuleSetInfo info = factory.from(vrs, Collections.emptySet(), Collections.singletonList(lifeCycleStateInfo));

        assertThat(info.lifeCycleStates).containsExactly(lifeCycleStateInfo);
    }
}
