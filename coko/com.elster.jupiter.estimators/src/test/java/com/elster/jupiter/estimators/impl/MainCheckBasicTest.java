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

public class MainCheckBasicTest extends MainCheckEstimatorTest {

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
    public void basicTest() {

        EstimationConfiguration estimationConfiguration = new EstimationConfiguration()
                .withCompletePeriod(false)
                .withBlock(new BlockConfiguration()
                        .withEstimatable(new EstimatableConf()
                                .of(instant("20160101000000"))
                                .withReferenceValue(new ReferenceValue()
                                        .withValue(bigDecimal(100D))
                                        .withValidationResult(ValidationResult.VALID)))
                        .withEstimatable(new EstimatableConf()
                                .of(instant("20160102000000"))
                                .withReferenceValue(new ReferenceValue()
                                        .withValue(bigDecimal(200D))
                                        .withValidationResult(ValidationResult.NOT_VALIDATED))));

        estimationConfiguration.mockAll();

        Estimator estimator = mockEstimator(estimationConfiguration);
        EstimationResult estimationResult = estimator.estimate(estimationConfiguration.getAllBlocks(), QualityCodeSystem.MDM);
        assertEquals(1, estimationResult.estimated().size());
        assertEquals(0, estimationResult.remainingToBeEstimated().size());
        assertEquals(2, estimationResult.estimated().get(0).estimatables().size());

        assertEquals(bigDecimal(100D), findEstimatedValue(estimationConfiguration, instant("20160101000000")));
        assertEquals(bigDecimal(200D), findEstimatedValue(estimationConfiguration, instant("20160102000000")));
    }

    @Test
    public void fullEstimationTest() {

        EstimationConfiguration estimationConfiguration = new EstimationConfiguration()
                .withLogger(LOGGER)
                .withCompletePeriod(false)
                .withBlock(new BlockConfiguration()
                        .withEstimatable(new EstimatableConf()
                                .of(instant("20160101000000"))
                                .withReferenceValue(new ReferenceValue()
                                        .withValue(bigDecimal(100D))
                                        .withValidationResult(ValidationResult.VALID)))
                        .withEstimatable(new EstimatableConf()
                                .of(instant("20160102000000"))
                                .withReferenceValue(new ReferenceValue()
                                        .withValue(bigDecimal(200D))
                                        .withValidationResult(ValidationResult.NOT_VALIDATED))))
                .withBlock(new BlockConfiguration()
                        .withEstimatable(new EstimatableConf()
                                .of(instant("20160103000000"))
                                .withReferenceValue(new ReferenceValue()
                                        .withValue(bigDecimal(300D))
                                        .withValidationResult(ValidationResult.SUSPECT)))
                        .withEstimatable(new EstimatableConf()
                                .of(instant("20160104000000"))
                                .withReferenceValue(new ReferenceValue()
                                        .withValue(bigDecimal(400D))
                                        .withValidationResult(ValidationResult.NOT_VALIDATED)))
                        .withEstimatable(new EstimatableConf()
                                .of(instant("20160105000000"))
                                .withReferenceValue(new ReferenceValue()
                                        .withValue(bigDecimal(500D))
                                        .withValidationResult(ValidationResult.VALID))));

        estimationConfiguration.mockAll();

        Estimator estimator = mockEstimator(estimationConfiguration);
        EstimationResult estimationResult = estimator.estimate(estimationConfiguration.getAllBlocks(), QualityCodeSystem.MDM);
        assertEquals(1, estimationResult.estimated().size());
        assertEquals(2, estimationResult.estimated().get(0).estimatables().size());

        assertEquals(1, estimationResult.remainingToBeEstimated().size());
        assertEquals(3, estimationResult.remainingToBeEstimated().get(0).estimatables().size());


        assertEquals(bigDecimal(100D), findEstimatedValue(estimationConfiguration, instant("20160101000000")));
        assertEquals(bigDecimal(200D), findEstimatedValue(estimationConfiguration, instant("20160102000000")));

        JupiterAssertions.assertThat(logRecorder).hasRecordWithMessage(message -> message.contains("Failed to estimate period \"Sun, 03 Jan 2016 12:00 until Tue, 05 Jan 2016 12:00\" using method Main/Check substitution on usage point name/Purpose/[Daily] Secondary Delta A+ (kWh) since data from check output is suspect or missing")).atLevel(Level.WARNING);

    }

    @Test
    public void fullEstimationOnCompletePeriodTest() {

        EstimationConfiguration estimationConfiguration = new EstimationConfiguration()
                .withLogger(LOGGER)
                .withCompletePeriod(true)
                .withBlock(new BlockConfiguration()
                        .withEstimatable(new EstimatableConf()
                                .of(instant("20160101000000"))
                                .withReferenceValue(new ReferenceValue()
                                        .withValue(bigDecimal(100D))
                                        .withValidationResult(ValidationResult.VALID)))
                        .withEstimatable(new EstimatableConf()
                                .of(instant("20160102000000"))
                                .withReferenceValue(new ReferenceValue()
                                        .withValue(bigDecimal(200D))
                                        .withValidationResult(ValidationResult.NOT_VALIDATED))))
                .withBlock(new BlockConfiguration()
                        .withEstimatable(new EstimatableConf()
                                .of(instant("20160210000000"))
                                .withReferenceValue(new ReferenceValue()
                                        .withValue(bigDecimal(300D))
                                        .withValidationResult(ValidationResult.SUSPECT)))
                        .withEstimatable(new EstimatableConf()
                                .of(instant("20160211000000"))
                                .withReferenceValue(new ReferenceValue()
                                        .withValue(bigDecimal(400D))
                                        .withValidationResult(ValidationResult.NOT_VALIDATED)))
                        .withEstimatable(new EstimatableConf()
                                .of(instant("20160212000000"))
                                .withReferenceValue(new ReferenceValue()
                                        .withValue(bigDecimal(500D))
                                        .withValidationResult(ValidationResult.VALID))));

        estimationConfiguration.mockAll();

        Estimator estimator = mockEstimator(estimationConfiguration);
        EstimationResult estimationResult = estimator.estimate(estimationConfiguration.getAllBlocks(), QualityCodeSystem.MDM);
        assertEquals(0, estimationResult.estimated().size());

        assertEquals(2, estimationResult.remainingToBeEstimated().size());
        assertEquals(2, estimationResult.remainingToBeEstimated().get(0).estimatables().size());
        assertEquals(3, estimationResult.remainingToBeEstimated().get(1).estimatables().size());


        assertEquals(bigDecimal(100D), findEstimatedValue(estimationConfiguration, instant("20160101000000")));
        assertEquals(bigDecimal(200D), findEstimatedValue(estimationConfiguration, instant("20160102000000")));

        JupiterAssertions.assertThat(logRecorder).hasRecordWithMessage(message -> message.contains("Failed to estimate period \"Wed, 10 Feb 2016 12:00 until Fri, 12 Feb 2016 12:00\" using method Main/Check substitution on usage point name/Purpose/[Daily] Secondary Delta A+ (kWh) since data from check output is suspect or missing")).atLevel(Level.WARNING);

    }

}
