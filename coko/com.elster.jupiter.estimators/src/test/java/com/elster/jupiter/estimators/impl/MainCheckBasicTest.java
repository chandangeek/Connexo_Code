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

import java.math.BigDecimal;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MainCheckBasicTest extends MainCheckEstimatorTest {

    private static final Logger LOGGER = Logger.getLogger(MainCheckMisconfigTest.class.getName());
    private LogRecorder logRecorder;

    private static final String READING_TIME_1 = "20160101000000";
    private static final String READING_TIME_2 = "20160102000000";
    private static final String READING_TIME_3 = "20160103000000";
    private static final String READING_TIME_4 = "20160104000000";
    private static final String READING_TIME_5 = "20160105000000";

    private static final Double BIG_DECIMAL_100 = 100D;
    private static final Double BIG_DECIMAL_200 = 200D;
    private static final Double BIG_DECIMAL_300 = 300D;
    private static final Double BIG_DECIMAL_400 = 400D;
    private static final Double BIG_DECIMAL_500 = 500D;
    
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
    public void basicTest() {

        EstimationConfiguration estimationConfiguration = new EstimationConfiguration()
                .withBlock(new BlockConfiguration()
                        .withEstimatable(new EstimatableConf()
                                .of(instant(READING_TIME_1))
                                .withReferenceValue(new ReferenceValue()
                                        .withValue(bigDecimal(BIG_DECIMAL_100))
                                        .withValidationResult(ValidationResult.VALID)))
                        .withEstimatable(new EstimatableConf()
                                .of(instant(READING_TIME_2))
                                .withReferenceValue(new ReferenceValue()
                                        .withValue(bigDecimal(BIG_DECIMAL_200))
                                        .withValidationResult(ValidationResult.NOT_VALIDATED))));

        estimationConfiguration.mockAll();

        Estimator estimator = mockEstimator(estimationConfiguration);
        EstimationResult estimationResult = estimator.estimate(estimationConfiguration.getAllBlocks(), QualityCodeSystem.MDM);
        assertEquals(1, estimationResult.estimated().size());
        assertEquals(0, estimationResult.remainingToBeEstimated().size());
        assertEquals(2, estimationResult.estimated().get(0).estimatables().size());

        assertEquals(bigDecimal(BIG_DECIMAL_100), findEstimatedValue(estimationConfiguration, instant(READING_TIME_1)));
        assertEquals(bigDecimal(BIG_DECIMAL_200), findEstimatedValue(estimationConfiguration, instant(READING_TIME_2)));
    }

    @Test
    public void fullEstimationTest() {

        EstimationConfiguration estimationConfiguration = new EstimationConfiguration()
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
                                        .withValue(bigDecimal(BIG_DECIMAL_200))
                                        .withValidationResult(ValidationResult.NOT_VALIDATED))))
                .withBlock(new BlockConfiguration()
                        .withEstimatable(new EstimatableConf()
                                .of(instant(READING_TIME_3))
                                .withReferenceValue(new ReferenceValue()
                                        .withValue(bigDecimal(BIG_DECIMAL_300))
                                        .withValidationResult(ValidationResult.SUSPECT)))
                        .withEstimatable(new EstimatableConf()
                                .of(instant(READING_TIME_4))
                                .withReferenceValue(new ReferenceValue()
                                        .withValue(bigDecimal(BIG_DECIMAL_400))
                                        .withValidationResult(ValidationResult.NOT_VALIDATED)))
                        .withEstimatable(new EstimatableConf()
                                .of(instant(READING_TIME_5))
                                .withReferenceValue(new ReferenceValue()
                                        .withValue(bigDecimal(BIG_DECIMAL_500))
                                        .withValidationResult(ValidationResult.VALID))));

        estimationConfiguration.mockAll();

        Estimator estimator = mockEstimator(estimationConfiguration);
        EstimationResult estimationResult = estimator.estimate(estimationConfiguration.getAllBlocks(), QualityCodeSystem.MDM);
        assertEquals(1, estimationResult.estimated().size());
        assertEquals(2, estimationResult.estimated().get(0).estimatables().size());

        assertEquals(1, estimationResult.remainingToBeEstimated().size());
        assertEquals(3, estimationResult.remainingToBeEstimated().get(0).estimatables().size());


        assertEquals(bigDecimal(BIG_DECIMAL_100), findEstimatedValue(estimationConfiguration, instant(READING_TIME_1)));
        assertEquals(bigDecimal(BIG_DECIMAL_200), findEstimatedValue(estimationConfiguration, instant(READING_TIME_2)));

        JupiterAssertions.assertThat(logRecorder).hasRecordWithMessage(message -> message.contains("Failed to estimate period \"Sun, 3 Jan 2016 12:00 AM until Tue, 5 Jan 2016 12:00 AM\" using method Main/Check substitution on usage point name/Purpose/[Daily] Secondary Delta A+ (kWh) since data from check output is suspect or missing")).atLevel(Level.WARNING);

    }

}
