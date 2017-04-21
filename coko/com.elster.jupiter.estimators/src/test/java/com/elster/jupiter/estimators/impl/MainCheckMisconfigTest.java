/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimators.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.devtools.tests.assertions.JupiterAssertions;
import com.elster.jupiter.devtools.tests.fakes.LogRecorder;
import com.elster.jupiter.estimation.EstimationResult;
import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.util.logging.LoggingContext;
import com.elster.jupiter.validation.ValidationResult;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MainCheckMisconfigTest extends MainCheckEstimatorTest {

    private static final String READING_TIME_1 = "20160101000000";
    private static final String READING_TIME_2 = "20160102000000";

    private static final Double BIG_DECIMAL_100 = 100D;

    private static final Logger LOGGER = Logger.getLogger(MainCheckMisconfigTest.class.getName());
    private LogRecorder logRecorder;

    @Before
    public void setUp() {
        logRecorder = new LogRecorder(Level.ALL);
        LOGGER.addHandler(logRecorder);
        LoggingContext.getCloseableContext().with("rule", "rule");
    }

    @After
    public void tearDown() {
        LoggingContext.getCloseableContext().close();
        LOGGER.removeHandler(logRecorder);
    }

    @Test
    public void noPurposeTest() {
        EstimationConfiguration estimationConfiguration = new EstimationConfiguration()
                .withNotAvailablePurpose()
                .withLogger(LOGGER)
                .withBlock(new BlockConfiguration()
                        .withEstimatable(new EstimatableConf()
                                .of(instant(READING_TIME_1))
                                .withReferenceValue(new ReferenceValue()
                                        .withValue(bigDecimal(BIG_DECIMAL_100))
                                        .withValidationResult(ValidationResult.VALID)))
                        .withEstimatable(new EstimatableConf()
                                .of(instant(READING_TIME_2))
                                .withReferenceValue(new ReferenceValue()
                                        .withValue(bigDecimal(BIG_DECIMAL_100))
                                        .withValidationResult(ValidationResult.VALID))));

        estimationConfiguration.mockAll();

        Estimator estimator = mockEstimator(estimationConfiguration);
        EstimationResult estimationResult = estimator.estimate(estimationConfiguration.getAllBlocks(), QualityCodeSystem.MDM);

        assertEquals(0, estimationResult.estimated().size());
        assertEquals(1, estimationResult.remainingToBeEstimated().size());
        assertEquals(2, estimationResult.remainingToBeEstimated().get(0).estimatables().size());

        JupiterAssertions.assertThat(logRecorder).hasRecordWithMessage(message -> message.contains("Failed to estimate period \"Fri, 1 Jan 2016 12:00 AM until Sat, 2 Jan 2016 12:00 AM\" using method Main/Check substitution on [Daily] Secondary Delta A+ (kWh) since the specified purpose doesn't exist on the usage point name")).atLevel(Level.WARNING);
    }

    @Test
    public void noCheckChannelTest() {
        EstimationConfiguration estimationConfiguration = new EstimationConfiguration()
                .withLogger(LOGGER)
                .withBlock(new BlockConfiguration()
                        .withNoCheckChannel()
                        .withEstimatable(new EstimatableConf()
                                .of(instant(READING_TIME_1))
                                .withReferenceValue(new ReferenceValue()
                                        .withValue(bigDecimal(BIG_DECIMAL_100))
                                        .withValidationResult(ValidationResult.VALID)))
                        .withEstimatable(new EstimatableConf()
                                .of(instant(READING_TIME_2))
                                .withReferenceValue(new ReferenceValue()
                                        .withValue(bigDecimal(BIG_DECIMAL_100))
                                        .withValidationResult(ValidationResult.VALID))));

        estimationConfiguration.mockAll();

        Estimator estimator = mockEstimator(estimationConfiguration);
        EstimationResult estimationResult = estimator.estimate(estimationConfiguration.getAllBlocks(), QualityCodeSystem.MDM);

        assertEquals(0, estimationResult.estimated().size());
        assertEquals(1, estimationResult.remainingToBeEstimated().size());
        assertEquals(2, estimationResult.remainingToBeEstimated().get(0).estimatables().size());

        JupiterAssertions.assertThat(logRecorder).hasRecordWithMessage(message -> message.contains("Failed to estimate period \"Fri, 1 Jan 2016 12:00 AM until Sat, 2 Jan 2016 12:00 AM\" using method Main/Check substitution on [Daily] Secondary Delta A+ (kWh) since 'check' output with matching reading type on the specified purpose doesn't exist on usage point name")).atLevel(Level.WARNING);
    }

    @Test
    public void suspectCheckDataTest() {
        EstimationConfiguration estimationConfiguration = new EstimationConfiguration()
                .withLogger(LOGGER)
                .withBlock(new BlockConfiguration()
                        .withEstimatable(new EstimatableConf()
                                .of(instant(READING_TIME_1))
                                .withReferenceValue(new ReferenceValue()
                                        .withValue(bigDecimal(BIG_DECIMAL_100))
                                        .withValidationResult(ValidationResult.SUSPECT)))
                        .withEstimatable(new EstimatableConf()
                                .of(instant(READING_TIME_2))
                                .withReferenceValue(new ReferenceValue()
                                        .withValue(bigDecimal(BIG_DECIMAL_100))
                                        .withValidationResult(ValidationResult.VALID))));

        estimationConfiguration.mockAll();

        Estimator estimator = mockEstimator(estimationConfiguration);
        EstimationResult estimationResult = estimator.estimate(estimationConfiguration.getAllBlocks(), QualityCodeSystem.MDM);

        assertEquals(0, estimationResult.estimated().size());
        assertEquals(1, estimationResult.remainingToBeEstimated().size());
        assertEquals(2, estimationResult.remainingToBeEstimated().get(0).estimatables().size());

        JupiterAssertions.assertThat(logRecorder).hasRecordWithMessage(message -> message.contains("Failed to estimate period \"Fri, 1 Jan 2016 12:00 AM until Sat, 2 Jan 2016 12:00 AM\" using method Main/Check substitution on usage point name/Purpose/[Daily] Secondary Delta A+ (kWh) since data from check output is suspect or missing")).atLevel(Level.WARNING);
    }

    @Test
    public void missingCheckDataTest() {
        EstimationConfiguration estimationConfiguration = new EstimationConfiguration()
                .withLogger(LOGGER)
                .withBlock(new BlockConfiguration()
                        .withEstimatable(new EstimatableConf()
                                .of(instant(READING_TIME_1))
                                .withNoReferenceValue())
                        .withEstimatable(new EstimatableConf()
                                .of(instant(READING_TIME_2))
                                .withReferenceValue(new ReferenceValue()
                                        .withValue(bigDecimal(BIG_DECIMAL_100))
                                        .withValidationResult(ValidationResult.VALID))));

        estimationConfiguration.mockAll();

        Estimator estimator = mockEstimator(estimationConfiguration);
        EstimationResult estimationResult = estimator.estimate(estimationConfiguration.getAllBlocks(), QualityCodeSystem.MDM);

        assertEquals(0, estimationResult.estimated().size());
        assertEquals(1, estimationResult.remainingToBeEstimated().size());
        assertEquals(2, estimationResult.remainingToBeEstimated().get(0).estimatables().size());

        JupiterAssertions.assertThat(logRecorder).hasRecordWithMessage(message -> message.contains("Failed to estimate period \"Fri, 1 Jan 2016 12:00 AM until Sat, 2 Jan 2016 12:00 AM\" using method Main/Check substitution on usage point name/Purpose/[Daily] Secondary Delta A+ (kWh) since data from check output is suspect or missing")).atLevel(Level.WARNING);
    }

}
