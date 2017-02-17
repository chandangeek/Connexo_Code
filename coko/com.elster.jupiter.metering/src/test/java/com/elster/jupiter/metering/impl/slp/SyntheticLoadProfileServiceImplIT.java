/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.slp;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.metering.impl.MeteringInMemoryBootstrapModule;
import com.elster.jupiter.metering.slp.SyntheticLoadProfile;
import com.elster.jupiter.metering.slp.SyntheticLoadProfileBuilder;

import com.google.common.collect.Range;
import com.google.inject.Injector;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unused")
public class SyntheticLoadProfileServiceImplIT {

    private final String SYNTHETIC_LOAD_PROFILE_NAME = "syntheticLoadProfile";
    private final String SYNTHETIC_LOAD_PROFILE_DESC = "syntheticLoadProfileDescription";
    private final Instant DATE = ZonedDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant();
    private final java.time.Duration INTERVAL15MIN = java.time.Duration.ofMinutes(15);
    private final java.time.Duration INTERVAL60MIN = java.time.Duration.ofMinutes(60);
    private final java.time.Period DURATION1MONTH = java.time.Period.ofMonths(1);

    public static final Clock clock = Clock.system(ZoneId.of("Europe/Athens"));

    private Injector injector;

    private static MeteringInMemoryBootstrapModule inMemoryBootstrapModule = new MeteringInMemoryBootstrapModule();

    @BeforeClass
    public static void beforeClass() {
        inMemoryBootstrapModule.activate();
    }

    @AfterClass
    public static void afterClass() {
        inMemoryBootstrapModule.deactivate();
    }

    @Rule
    public TransactionalRule transactionalRule = new TransactionalRule(inMemoryBootstrapModule.getTransactionService());

    @Test
    @Transactional
    public void testCreateSyntheticLoadProfileSpecification() {
        SyntheticLoadProfileBuilder builder = inMemoryBootstrapModule.getSyntheticLoadProfileService().newSyntheticLoadProfile(SYNTHETIC_LOAD_PROFILE_NAME);
        builder.withDescription(SYNTHETIC_LOAD_PROFILE_DESC);
        builder.withInterval(INTERVAL15MIN);
        builder.withDuration(DURATION1MONTH);
        builder.withStartTime(DATE);
        SyntheticLoadProfile syntheticLoadProfileFromBuilder = builder.build();
        assertThat(syntheticLoadProfileFromBuilder.getName()).isEqualTo(SYNTHETIC_LOAD_PROFILE_NAME);
        assertThat(syntheticLoadProfileFromBuilder.getDescription()).isEqualTo(SYNTHETIC_LOAD_PROFILE_DESC);
        assertThat(syntheticLoadProfileFromBuilder.getInterval()).isEqualTo(INTERVAL15MIN);
        assertThat(syntheticLoadProfileFromBuilder.getDuration()).isEqualTo(DURATION1MONTH);
        assertThat(syntheticLoadProfileFromBuilder.getStartTime()).isEqualTo(DATE);

        SyntheticLoadProfile syntheticLoadProfile = inMemoryBootstrapModule.getSyntheticLoadProfileService().findSyntheticLoadProfile(syntheticLoadProfileFromBuilder.getId()).get();
        assertThat(syntheticLoadProfile.getName()).isEqualTo(SYNTHETIC_LOAD_PROFILE_NAME);
        assertThat(syntheticLoadProfile.getDescription()).isEqualTo(SYNTHETIC_LOAD_PROFILE_DESC);
        assertThat(syntheticLoadProfile.getInterval()).isEqualTo(INTERVAL15MIN);
        assertThat(syntheticLoadProfile.getDuration()).isEqualTo(DURATION1MONTH);
        assertThat(syntheticLoadProfile.getStartTime()).isEqualTo(DATE);
    }

    @Test
    @Transactional
    public void testFindSyntheticLoadProfileSpecification() {
        SyntheticLoadProfileBuilder builder = inMemoryBootstrapModule.getSyntheticLoadProfileService().newSyntheticLoadProfile(SYNTHETIC_LOAD_PROFILE_NAME);
        builder.withDescription(SYNTHETIC_LOAD_PROFILE_DESC);
        builder.withInterval(INTERVAL15MIN);
        builder.withDuration(DURATION1MONTH);
        builder.withStartTime(DATE);
        builder.build();

        SyntheticLoadProfile syntheticLoadProfile = inMemoryBootstrapModule.getSyntheticLoadProfileService().findSyntheticLoadProfile(SYNTHETIC_LOAD_PROFILE_NAME).get();
        assertThat(syntheticLoadProfile.getName()).isEqualTo(SYNTHETIC_LOAD_PROFILE_NAME);
        assertThat(syntheticLoadProfile.getDescription()).isEqualTo(SYNTHETIC_LOAD_PROFILE_DESC);
        assertThat(syntheticLoadProfile.getInterval()).isEqualTo(INTERVAL15MIN);
        assertThat(syntheticLoadProfile.getDuration()).isEqualTo(DURATION1MONTH);
        assertThat(syntheticLoadProfile.getStartTime()).isEqualTo(DATE);
    }

    @Test
    @Transactional
    public void testAddValueToSyntheticLoadProfileSpecification() {
        SyntheticLoadProfileBuilder builder = inMemoryBootstrapModule.getSyntheticLoadProfileService().newSyntheticLoadProfile(SYNTHETIC_LOAD_PROFILE_NAME);
        builder.withDescription(SYNTHETIC_LOAD_PROFILE_DESC);
        builder.withInterval(INTERVAL15MIN);
        builder.withDuration(DURATION1MONTH);
        builder.withStartTime(DATE);
        long id = builder.build().getId();

        SyntheticLoadProfile syntheticLoadProfile = inMemoryBootstrapModule.getSyntheticLoadProfileService().findSyntheticLoadProfile(id).get();

        syntheticLoadProfile.addValues(Collections.singletonMap(DATE, BigDecimal.valueOf(123.456)));

        Optional<BigDecimal> returnedValue = syntheticLoadProfile.getValue(DATE);

        assertThat(returnedValue).isPresent();
        assertThat(returnedValue.get()).isEqualTo(BigDecimal.valueOf(123.456));

    }

    @Test
    @Transactional
    public void testAddMultipleValuesToSyntheticLoadProfileSpecification() {
        SyntheticLoadProfileBuilder builder = inMemoryBootstrapModule.getSyntheticLoadProfileService().newSyntheticLoadProfile(SYNTHETIC_LOAD_PROFILE_NAME);
        builder.withDescription(SYNTHETIC_LOAD_PROFILE_DESC);
        builder.withInterval(INTERVAL60MIN);
        builder.withDuration(DURATION1MONTH);
        builder.withStartTime(DATE);
        long id = builder.build().getId();

        SyntheticLoadProfile syntheticLoadProfile = inMemoryBootstrapModule.getSyntheticLoadProfileService().findSyntheticLoadProfile(id).get();

        Map<Instant, BigDecimal> values = new HashMap<>();

        IntStream.iterate(0, i -> i + 3600)
                .limit(100)
                .mapToObj(DATE::plusSeconds)
                .forEach(i -> values.put(i, BigDecimal.valueOf(i.getEpochSecond())));

        syntheticLoadProfile.addValues(values);


        Map<Instant, BigDecimal> returnedValues = syntheticLoadProfile.getValues(Range.atLeast(DATE));

        assertThat(returnedValues.size()).isEqualTo(100);

        for (Map.Entry<Instant, BigDecimal> entry : returnedValues.entrySet()) {
            assertThat(entry.getValue() != null).isTrue();
            assertThat(entry.getValue()).isEqualTo(BigDecimal.valueOf(entry.getKey().getEpochSecond()));
        }
    }

    @Test
    @Transactional
    public void testRemoveSyntheticLoadProfileSpecification() {
        SyntheticLoadProfileBuilder builder = inMemoryBootstrapModule.getSyntheticLoadProfileService().newSyntheticLoadProfile(SYNTHETIC_LOAD_PROFILE_NAME + "ToRemove");
        builder.withDescription(SYNTHETIC_LOAD_PROFILE_DESC + "ToRemove");
        builder.withInterval(INTERVAL15MIN);
        builder.withDuration(DURATION1MONTH);
        builder.withStartTime(DATE);
        long id = builder.build().getId();

        SyntheticLoadProfile syntheticLoadProfile = inMemoryBootstrapModule.getSyntheticLoadProfileService().findSyntheticLoadProfile(id).get();
        syntheticLoadProfile.delete();
        assertThat(inMemoryBootstrapModule.getSyntheticLoadProfileService().findSyntheticLoadProfile(1)).isEqualTo(Optional.empty());
    }

}
