/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimators.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.devtools.tests.assertions.JupiterAssertions;
import com.elster.jupiter.estimation.EstimationResult;
import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.validation.ValidationResult;

import java.util.logging.Level;

import org.junit.Test;

import static com.elster.jupiter.estimators.impl.Utils.*;
import static org.junit.Assert.assertEquals;

public class ReferenceBasicTest extends ReferenceEstimatorTest{

    @Test
    public void basicTest() {

        ReferenceEstimationConfiguration referenceEstimationConfiguration = new ReferenceEstimationConfiguration()
                .withBlock(new BlockConfiguration()
                        .withEstimatable(new EstimatableConf()
                                .of(INSTANT_2016_FEB_01)
                                .withReferenceValue(new ReferenceValue()
                                        .withValue(BIG_DECIMAL_100.multiply(COMPARABLE_READING_TYPE_MULTIPIER))
                                        .withValidationResult(ValidationResult.VALID)))
                        .withEstimatable(new EstimatableConf()
                                .of(INSTANT_2016_FEB_02)
                                .withReferenceValue(new ReferenceValue()
                                        .withValue(BIG_DECIMAL_200.multiply(COMPARABLE_READING_TYPE_MULTIPIER))
                                        .withValidationResult(ValidationResult.NOT_VALIDATED))));

        referenceEstimationConfiguration.mockAll();

        Estimator estimator = mockEstimator(referenceEstimationConfiguration);
        EstimationResult estimationResult = estimator.estimate(referenceEstimationConfiguration.getAllBlocks(), QualityCodeSystem.MDM);
        assertEquals(1, estimationResult.estimated().size());
        assertEquals(0, estimationResult.remainingToBeEstimated().size());
        assertEquals(2, estimationResult.estimated().get(0).estimatables().size());

        assertEquals(BIG_DECIMAL_100, findEstimatedValue(referenceEstimationConfiguration, INSTANT_2016_FEB_01));
        assertEquals(BIG_DECIMAL_200, findEstimatedValue(referenceEstimationConfiguration, INSTANT_2016_FEB_02));
    }

    @Test
    public void fullEstimationTest() {

        ReferenceEstimationConfiguration referenceEstimationConfiguration = new ReferenceEstimationConfiguration()
                .withLogger(LOGGER)
                .withBlock(new BlockConfiguration()
                        .withEstimatable(new EstimatableConf()
                                .of(INSTANT_2016_FEB_01)
                                .withReferenceValue(new ReferenceValue()
                                        .withValue(BIG_DECIMAL_100.multiply(COMPARABLE_READING_TYPE_MULTIPIER))
                                        .withValidationResult(ValidationResult.VALID)))
                        .withEstimatable(new EstimatableConf()
                                .of(INSTANT_2016_FEB_02)
                                .withReferenceValue(new ReferenceValue()
                                        .withValue(BIG_DECIMAL_200.multiply(COMPARABLE_READING_TYPE_MULTIPIER))
                                        .withValidationResult(ValidationResult.NOT_VALIDATED))))
                .withBlock(new BlockConfiguration()
                        .withEstimatable(new EstimatableConf()
                                .of(INSTANT_2016_FEB_03)
                                .withReferenceValue(new ReferenceValue()
                                        .withValue(BIG_DECIMAL_300)
                                        .withValidationResult(ValidationResult.SUSPECT)))
                        .withEstimatable(new EstimatableConf()
                                .of(INSTANT_2016_FEB_04)
                                .withReferenceValue(new ReferenceValue()
                                        .withValue(BIG_DECIMAL_400)
                                        .withValidationResult(ValidationResult.NOT_VALIDATED)))
                        .withEstimatable(new EstimatableConf()
                                .of(INSTANT_2016_FEB_05)
                                .withReferenceValue(new ReferenceValue()
                                        .withValue(BIG_DECIMAL_500)
                                        .withValidationResult(ValidationResult.VALID))));

        referenceEstimationConfiguration.mockAll();

        ReferenceSubstitutionEstimator estimator = mockEstimator(referenceEstimationConfiguration);
        EstimationResult estimationResult = estimator.estimate(referenceEstimationConfiguration.getAllBlocks(), QualityCodeSystem.MDM);
        assertEquals(1, estimationResult.estimated().size());
        assertEquals(2, estimationResult.estimated().get(0).estimatables().size());

        assertEquals(1, estimationResult.remainingToBeEstimated().size());
        assertEquals(3, estimationResult.remainingToBeEstimated().get(0).estimatables().size());


        assertEquals(BIG_DECIMAL_100, findEstimatedValue(referenceEstimationConfiguration, INSTANT_2016_FEB_01));
        assertEquals(BIG_DECIMAL_200, findEstimatedValue(referenceEstimationConfiguration, INSTANT_2016_FEB_02));

        JupiterAssertions.assertThat(logRecorder).hasRecordWithMessage(message -> message.contains("Failed to estimate period \"Wed, 3 Feb 2016 12:00 AM until Fri, 5 Feb 2016 12:00 AM\" using method Reference substitution on usage point name/Purpose/[Daily] Secondary Delta A+ (kWh) since data from check output is suspect or missing")).atLevel(Level.WARNING);

    }

}
