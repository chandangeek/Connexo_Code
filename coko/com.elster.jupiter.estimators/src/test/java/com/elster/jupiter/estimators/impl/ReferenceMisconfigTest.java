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

import static com.elster.jupiter.estimators.impl.Utils.BIG_DECIMAL_100;
import static com.elster.jupiter.estimators.impl.Utils.INSTANT_2016_FEB_01;
import static com.elster.jupiter.estimators.impl.Utils.INSTANT_2016_FEB_02;
import static org.junit.Assert.assertEquals;

public class ReferenceMisconfigTest extends ReferenceEstimatorTest {
    @Test
    public void notFullyConfiguredTest() {
        ReferenceEstimationConfiguration referenceEstimationConfiguration = new ReferenceEstimationConfiguration()
                .notFullyConfigured()
                .withLogger(LOGGER)
                .withBlock(new BlockConfiguration()
                        .withEstimatable(new EstimatableConf()
                                .of(INSTANT_2016_FEB_01)
                                .withReferenceValue(new ReferenceValue()
                                        .withValue(BIG_DECIMAL_100)
                                        .withValidationResult(ValidationResult.VALID)))
                        .withEstimatable(new EstimatableConf()
                                .of(INSTANT_2016_FEB_02)
                                .withReferenceValue(new ReferenceValue()
                                        .withValue(BIG_DECIMAL_100)
                                        .withValidationResult(ValidationResult.VALID))));

        referenceEstimationConfiguration.mockAll();

        ReferenceSubstitutionEstimator estimator = mockEstimator(referenceEstimationConfiguration);
        EstimationResult estimationResult = estimator.estimate(referenceEstimationConfiguration.getAllBlocks(), QualityCodeSystem.MDM);

        assertEquals(0, estimationResult.estimated().size());
        assertEquals(1, estimationResult.remainingToBeEstimated().size());
        assertEquals(2, estimationResult.remainingToBeEstimated().get(0).estimatables().size());

        JupiterAssertions.assertThat(logRecorder).hasRecordWithMessage(message -> message.contains("Failed to estimate usage point name/Purpose/[Daily] Secondary Delta A+ (kWh) using method Reference substitution [STD] since the check usage point, purpose and reading type are not specified")).atLevel(Level.WARNING);
    }

    @Test
    public void unlinkedPurposeTest() {
        ReferenceEstimationConfiguration referenceEstimationConfiguration = new ReferenceEstimationConfiguration()
                .withNotAvailablePurpose()
                .withLogger(LOGGER)
                .withBlock(new BlockConfiguration()
                        .withEstimatable(new EstimatableConf()
                                .of(INSTANT_2016_FEB_01)
                                .withReferenceValue(new ReferenceValue()
                                        .withValue(BIG_DECIMAL_100)
                                        .withValidationResult(ValidationResult.VALID)))
                        .withEstimatable(new EstimatableConf()
                                .of(INSTANT_2016_FEB_02)
                                .withReferenceValue(new ReferenceValue()
                                        .withValue(BIG_DECIMAL_100)
                                        .withValidationResult(ValidationResult.VALID))));

        referenceEstimationConfiguration.mockAll();

        ReferenceSubstitutionEstimator estimator = mockEstimator(referenceEstimationConfiguration);
        EstimationResult estimationResult = estimator.estimate(referenceEstimationConfiguration.getAllBlocks(), QualityCodeSystem.MDM);

        assertEquals(0, estimationResult.estimated().size());
        assertEquals(1, estimationResult.remainingToBeEstimated().size());
        assertEquals(2, estimationResult.remainingToBeEstimated().get(0).estimatables().size());

        JupiterAssertions.assertThat(logRecorder).hasRecordWithMessage(message -> message.contains("Failed to estimate usage point name/Purpose/[Daily] Secondary Delta A+ (kWh) using method Reference substitution [STD] since the specified purpose/reading type doesn't exist on the usage point name")).atLevel(Level.WARNING);
    }

    @Test
    public void withNotCompatibleReadingTypeTest() {
        ReferenceEstimationConfiguration referenceEstimationConfiguration = new ReferenceEstimationConfiguration()
                .withNotComparableReferenceReadingType()
                .withLogger(LOGGER)
                .withBlock(new BlockConfiguration()
                        .withEstimatable(new EstimatableConf()
                                .of(INSTANT_2016_FEB_01)
                                .withReferenceValue(new ReferenceValue()
                                        .withValue(BIG_DECIMAL_100)
                                        .withValidationResult(ValidationResult.VALID)))
                        .withEstimatable(new EstimatableConf()
                                .of(INSTANT_2016_FEB_02)
                                .withReferenceValue(new ReferenceValue()
                                        .withValue(BIG_DECIMAL_100)
                                        .withValidationResult(ValidationResult.VALID))));

        referenceEstimationConfiguration.mockAll();

        ReferenceSubstitutionEstimator estimator = mockEstimator(referenceEstimationConfiguration);
        EstimationResult estimationResult = estimator.estimate(referenceEstimationConfiguration.getAllBlocks(), QualityCodeSystem.MDM);

        assertEquals(0, estimationResult.estimated().size());
        assertEquals(1, estimationResult.remainingToBeEstimated().size());
        assertEquals(2, estimationResult.remainingToBeEstimated().get(0).estimatables().size());

        JupiterAssertions.assertThat(logRecorder).hasRecordWithMessage(message -> message.contains("Failed to estimate usage point name/Purpose/[Daily] Secondary Delta A+ (kWh) using method Reference substitution [STD] since specified 'check' output doesnt match the 'main' reading type")).atLevel(Level.WARNING);
    }

    @Test
    public void suspectCheckDataTest() {
        ReferenceEstimationConfiguration referenceEstimationConfiguration = new ReferenceEstimationConfiguration()
                .withLogger(LOGGER)
                .withBlock(new BlockConfiguration()
                        .withEstimatable(new EstimatableConf()
                                .of(INSTANT_2016_FEB_01)
                                .withReferenceValue(new ReferenceValue()
                                        .withValue(BIG_DECIMAL_100)
                                        .withValidationResult(ValidationResult.SUSPECT)))
                        .withEstimatable(new EstimatableConf()
                                .of(INSTANT_2016_FEB_02)
                                .withReferenceValue(new ReferenceValue()
                                        .withValue(BIG_DECIMAL_100)
                                        .withValidationResult(ValidationResult.VALID))));

        referenceEstimationConfiguration.mockAll();

        Estimator estimator = mockEstimator(referenceEstimationConfiguration);
        EstimationResult estimationResult = estimator.estimate(referenceEstimationConfiguration.getAllBlocks(), QualityCodeSystem.MDM);

        assertEquals(0, estimationResult.estimated().size());
        assertEquals(1, estimationResult.remainingToBeEstimated().size());
        assertEquals(2, estimationResult.remainingToBeEstimated().get(0).estimatables().size());

        JupiterAssertions.assertThat(logRecorder).hasRecordWithMessage(message -> message.contains("Failed to estimate period \"Mon, 1 Feb 2016 12:00 AM until Tue, 2 Feb 2016 12:00 AM\" using method Reference substitution [STD] on usage point name/Purpose/[Daily] Secondary Delta A+ (kWh) since data from check output is suspect or missing")).atLevel(Level.WARNING);
    }

    @Test
    public void missingCheckDataTest() {
        ReferenceEstimationConfiguration referenceEstimationConfiguration = new ReferenceEstimationConfiguration()
                .withLogger(LOGGER)
                .withBlock(new BlockConfiguration()
                        .withEstimatable(new EstimatableConf()
                                .of(INSTANT_2016_FEB_01)
                                .withNoReferenceValue())
                        .withEstimatable(new EstimatableConf()
                                .of(INSTANT_2016_FEB_02)
                                .withReferenceValue(new ReferenceValue()
                                        .withValue(BIG_DECIMAL_100)
                                        .withValidationResult(ValidationResult.VALID))));

        referenceEstimationConfiguration.mockAll();

        Estimator estimator = mockEstimator(referenceEstimationConfiguration);
        EstimationResult estimationResult = estimator.estimate(referenceEstimationConfiguration.getAllBlocks(), QualityCodeSystem.MDM);

        assertEquals(0, estimationResult.estimated().size());
        assertEquals(1, estimationResult.remainingToBeEstimated().size());
        assertEquals(2, estimationResult.remainingToBeEstimated().get(0).estimatables().size());

        JupiterAssertions.assertThat(logRecorder).hasRecordWithMessage(message -> message.contains("Failed to estimate period \"Mon, 1 Feb 2016 12:00 AM until Tue, 2 Feb 2016 12:00 AM\" using method Reference substitution [STD] on usage point name/Purpose/[Daily] Secondary Delta A+ (kWh) since data from check output is suspect or missing")).atLevel(Level.WARNING);
    }


}
