/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;

import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.Optional;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class NtaReadingTypeTest {
    private static final String[] readingTypeCodes = {
            "0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0",
            "11.0.0.9.1.1.12.0.0.0.0.1.0.0.0.3.72.0",
            "13.0.0.9.1.1.12.0.0.0.0.1.0.0.0.3.72.0",
    };
    private static MeteringInMemoryBootstrapModule inMemoryBootstrapModule = new MeteringInMemoryBootstrapModule("0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0",
            "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0",
            "11.0.0.9.1.1.12.0.0.0.0.1.0.0.0.3.72.0",
            "11.0.0.4.1.1.12.0.0.0.0.1.0.0.0.3.72.0",
            "13.0.0.9.1.1.12.0.0.0.0.1.0.0.0.3.72.0",
            "13.0.0.4.1.1.12.0.0.0.0.1.0.0.0.3.72.0");
    @Rule
    public ExpectedConstraintViolationRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();
    @Rule
    public TransactionalRule transactionalRule = new TransactionalRule(inMemoryBootstrapModule.getTransactionService());

    @BeforeClass
    public static void setUp() {
        inMemoryBootstrapModule.activate();
    }

    @AfterClass
    public static void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }


    @Test
    @Transactional
    public void test() {
        ServerMeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        AmrSystem amrSystem = meteringService.findAmrSystem(1).get();
        Meter meter = amrSystem.newMeter("myMeter", "myMeter").create();
        MeterActivation meterActivation = meter.activate(Instant.now());
        for (String code : readingTypeCodes) {
            Optional<ReadingType> readingType = meteringService.getReadingType(code);
            assertThat(readingType.isPresent()).isTrue();
            Optional<TemporalAmount> interval = readingType.get().getIntervalLength();
            assertThat(interval.isPresent()).isTrue();
        }

        for (String code : readingTypeCodes) {
            ReadingType readingType = meteringService.getReadingType(code).get();
            Channel channel = meterActivation.getChannelsContainer().createChannel(readingType);
            assertThat(((ChannelImpl) channel).getRecordSpecDefinition()).isEqualTo(RecordSpecs.BULKQUANTITYINTERVAL);
        }
    }
}
