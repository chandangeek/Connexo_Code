/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.impl.MeteringInMemoryBootstrapModule;
import com.elster.jupiter.util.units.Dimension;

import java.math.BigDecimal;
import java.util.Arrays;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.when;

/**
 * Created by igh on 11/03/2016.
 */
@RunWith(MockitoJUnitRunner.class)
public class FormulaValidationTest {

    @Mock
    private ReadingTypeRequirement readingTypeRequirement1;
    @Mock
    private ReadingTypeRequirement readingTypeRequirement2;
    @Mock
    private ReadingTypeRequirement readingTypeRequirement3;
    @Mock
    private ReadingTypeRequirement pressure1;
    @Mock
    private ReadingTypeRequirement temperature1;
    @Mock
    private ReadingTypeRequirement volume1;
    @Mock
    private ReadingTypeRequirement pressure2;
    @Mock
    private ReadingTypeRequirement temperature2;
    @Mock
    private ReadingTypeRequirement volume2;

    private static MeteringInMemoryBootstrapModule inMemoryBootstrapModule = MeteringInMemoryBootstrapModule.withAllDefaults();

    @BeforeClass
    public static void setUp() {
        inMemoryBootstrapModule.activate();
    }

    @AfterClass
    public static void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }

    private ServerMetrologyConfigurationService getMetrologyConfigurationService() {
        return inMemoryBootstrapModule.getMetrologyConfigurationService();
    }

    @Test
    // formula = minus(readingTypeRequirement1, readingTypeRequirement2)
    public void testSubstractionOfSameDimensions() {
        when(readingTypeRequirement1.getDimension()).thenReturn(Dimension.POWER);
        when(readingTypeRequirement2.getDimension()).thenReturn(Dimension.POWER);
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();

        ServerFormulaBuilder builder = service.newFormulaBuilder(Formula.Mode.EXPERT);

        ExpressionNodeBuilder nodeBuilder = builder.minus(
                builder.requirement(readingTypeRequirement1),
                builder.requirement(readingTypeRequirement2));
        ServerExpressionNode node = nodeBuilder.create();

        node.validate();
    }

    @Test
    // formula = plus(readingTypeRequirement1, readingTypeRequirement2)
    public void testSumOfSameDimensions() {
        when(readingTypeRequirement1.getDimension()).thenReturn(Dimension.POWER);
        when(readingTypeRequirement2.getDimension()).thenReturn(Dimension.POWER);
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();

        ServerFormulaBuilder builder = service.newFormulaBuilder(Formula.Mode.EXPERT);

        ExpressionNodeBuilder nodeBuilder = builder.plus(
                builder.requirement(readingTypeRequirement1),
                builder.requirement(readingTypeRequirement2));
        ServerExpressionNode node = nodeBuilder.create();

        node.validate();
    }

    @Test(expected = InvalidNodeException.class)
    // formula = minus(readingTypeRequirement1, readingTypeRequirement2)
    public void testSubstractionOfIncompatibleDimensions() {
        when(readingTypeRequirement1.getDimension()).thenReturn(Dimension.POWER);
        when(readingTypeRequirement2.getDimension()).thenReturn(Dimension.CURRENCY);
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();

        ServerFormulaBuilder builder = service.newFormulaBuilder(Formula.Mode.EXPERT);

        ExpressionNodeBuilder nodeBuilder = builder.minus(
                builder.requirement(readingTypeRequirement1),
                builder.requirement(readingTypeRequirement2));
        ServerExpressionNode node = nodeBuilder.create();
        try {
            node.validate();
        } catch (InvalidNodeException e) {
            assertThat(e.getMessage()).isEqualTo("Only dimensions that are compatible for automatic unit conversion can be summed or substracted.");
            throw e;
        }
    }

    @Test(expected = InvalidNodeException.class)
    // formula = plus(readingTypeRequirement1, readingTypeRequirement2)
    public void testSumOfIncompatibleDimensions() {
        when(readingTypeRequirement1.getDimension()).thenReturn(Dimension.POWER);
        when(readingTypeRequirement2.getDimension()).thenReturn(Dimension.CURRENCY);
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();

        ServerFormulaBuilder builder = service.newFormulaBuilder(Formula.Mode.EXPERT);

        ExpressionNodeBuilder nodeBuilder = builder.plus(
                builder.requirement(readingTypeRequirement1),
                builder.requirement(readingTypeRequirement2));
        ServerExpressionNode node = nodeBuilder.create();
        try {
            node.validate();
        } catch (InvalidNodeException e) {
            assertThat(e.getMessage()).isEqualTo("Only dimensions that are compatible for automatic unit conversion can be summed or substracted.");
            throw e;
        }
    }

    @Test(expected = InvalidNodeException.class)
    // formula = multiply(readingTypeRequirement1, readingTypeRequirement2)
    public void testMultiplicationOfIncompatibleDimensions() {
        when(readingTypeRequirement1.getDimension()).thenReturn(Dimension.POWER);
        when(readingTypeRequirement2.getDimension()).thenReturn(Dimension.ELECTRIC_CURRENT);
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();

        ServerFormulaBuilder builder = service.newFormulaBuilder(Formula.Mode.EXPERT);

        ExpressionNodeBuilder nodeBuilder = builder.multiply(
                builder.requirement(readingTypeRequirement1),
                builder.requirement(readingTypeRequirement2));
        ServerExpressionNode node = nodeBuilder.create();
        try {
            node.validate();
        } catch (InvalidNodeException e) {
            assertThat(e.getMessage()).isEqualTo("Dimensions from multiplication arguments do not result in a valid dimension.");
            throw e;
        }
    }

    @Test
    // formula = multiply(readingTypeRequirement1, readingTypeRequirement2)
    public void testMultiplicationOfCompatibleDimensions() {
        when(readingTypeRequirement1.getDimension()).thenReturn(Dimension.LENGTH);
        when(readingTypeRequirement2.getDimension()).thenReturn(Dimension.LENGTH);
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();

        ServerFormulaBuilder builder = service.newFormulaBuilder(Formula.Mode.EXPERT);

        ExpressionNodeBuilder nodeBuilder = builder.multiply(
                builder.requirement(readingTypeRequirement1),
                builder.requirement(readingTypeRequirement2));
        ServerExpressionNode node = nodeBuilder.create();

        node.validate();

        assertThat(node.getDimension()).isEqualByComparingTo(Dimension.SURFACE);
    }

    @Test
    // formula = divide(readingTypeRequirement1, readingTypeRequirement2)
    public void testDivisionOfCompatibleDimensions() {
        when(readingTypeRequirement1.getDimension()).thenReturn(Dimension.SURFACE);
        when(readingTypeRequirement2.getDimension()).thenReturn(Dimension.LENGTH);
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();

        ServerFormulaBuilder builder = service.newFormulaBuilder(Formula.Mode.EXPERT);

        ExpressionNodeBuilder nodeBuilder = builder.divide(
                builder.requirement(readingTypeRequirement1),
                builder.requirement(readingTypeRequirement2));
        ServerExpressionNode node = nodeBuilder.create();

        node.validate();

        assertThat(node.getDimension()).isEqualTo(Dimension.LENGTH);
    }

    @Test(expected = InvalidNodeException.class)
    // formula = divide(readingTypeRequirement1, readingTypeRequirement2)
    public void testDivisionOfIncompatibleDimensions() {
        when(readingTypeRequirement1.getDimension()).thenReturn(Dimension.SURFACE);
        when(readingTypeRequirement2.getDimension()).thenReturn(Dimension.ELECTRIC_CURRENT);
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();

        ServerFormulaBuilder builder = service.newFormulaBuilder(Formula.Mode.EXPERT);

        ExpressionNodeBuilder nodeBuilder = builder.divide(
                builder.requirement(readingTypeRequirement1),
                builder.requirement(readingTypeRequirement2));
        ServerExpressionNode node = nodeBuilder.create();
        try {
            node.validate();
        } catch (InvalidNodeException e) {
            assertThat(e.getMessage()).isEqualTo("Dimensions from division arguments do not result in a valid dimension.");
            throw e;
        }
    }

    @Test
    // formula = plus(readingTypeRequirement1, readingTypeRequirement2)
    public void testPlusOfFlowAndVolumeDimensions() {
        when(readingTypeRequirement1.getDimension()).thenReturn(Dimension.VOLUME);
        when(readingTypeRequirement2.getDimension()).thenReturn(Dimension.POWER);
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();

        ServerFormulaBuilder builder = service.newFormulaBuilder(Formula.Mode.EXPERT);

        ExpressionNodeBuilder nodeBuilder = builder.plus(
                builder.requirement(readingTypeRequirement1),
                builder.requirement(readingTypeRequirement2));
        ServerExpressionNode node = nodeBuilder.create();

        node.validate();

        assertThat(node.getDimension()).isEqualTo(Dimension.VOLUME);
    }

    @Test
    // formula = max(readingTypeRequirement1, readingTypeRequirement2, readingTypeRequirement3)
    public void testMaximumOfCompatibleDimensions() {
        when(readingTypeRequirement1.getDimension()).thenReturn(Dimension.VOLUME);
        when(readingTypeRequirement2.getDimension()).thenReturn(Dimension.POWER);
        when(readingTypeRequirement3.getDimension()).thenReturn(Dimension.POWER);
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();

        ServerFormulaBuilder builder = service.newFormulaBuilder(Formula.Mode.EXPERT);

        ExpressionNodeBuilder nodeBuilder = builder.maximum(
                Arrays.asList(
                        builder.requirement(readingTypeRequirement1),
                        builder.requirement(readingTypeRequirement2),
                        builder.requirement(readingTypeRequirement3)));
        ServerExpressionNode node = nodeBuilder.create();

        node.validate();

        assertThat(node.getDimension()).isEqualTo(Dimension.VOLUME);
    }

    @Test(expected = InvalidNodeException.class)
    // formula = max(readingTypeRequirement1, readingTypeRequirement2, readingTypeRequirement3)
    public void testMaximumOfIncompatibleDimensions() {
        when(readingTypeRequirement1.getDimension()).thenReturn(Dimension.VOLUME);
        when(readingTypeRequirement2.getDimension()).thenReturn(Dimension.POWER);
        when(readingTypeRequirement3.getDimension()).thenReturn(Dimension.ELECTRIC_CHARGE);
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();

        ServerFormulaBuilder builder = service.newFormulaBuilder(Formula.Mode.EXPERT);

        ExpressionNodeBuilder nodeBuilder = builder.maximum(
                Arrays.asList(
                        builder.requirement(readingTypeRequirement1),
                        builder.requirement(readingTypeRequirement2),
                        builder.requirement(readingTypeRequirement3)));
        ServerExpressionNode node = nodeBuilder.create();
        try {
            node.validate();
        } catch (InvalidNodeException e) {
            assertThat(e.getMessage()).isEqualTo("Only dimensions that are compatible for automatic unit conversion can be used as children of a function.");
            throw e;
        }
    }

    @Test
    // formula = ((pressure1 * volume 1) / temperature1)  * (temperature2/pressure2) => should result in valid Volume Dimension
    public void testGasPressureTemperature() {
        when(pressure1.getDimension()).thenReturn(Dimension.PRESSURE);
        when(pressure2.getDimension()).thenReturn(Dimension.PRESSURE);
        when(temperature1.getDimension()).thenReturn(Dimension.TEMPERATURE);
        when(temperature2.getDimension()).thenReturn(Dimension.TEMPERATURE);
        when(volume1.getDimension()).thenReturn(Dimension.VOLUME);
        when(volume2.getDimension()).thenReturn(Dimension.VOLUME);
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();

        ServerFormulaBuilder builder = service.newFormulaBuilder(Formula.Mode.EXPERT);

        ExpressionNodeBuilder nodeBuilder = builder.multiply(builder.divide(builder.multiply(
                builder.requirement(pressure1), builder.requirement(volume1)),
                builder.requirement(temperature1)),
                builder.divide(builder.requirement(temperature2), builder.requirement(pressure2)));
        ServerExpressionNode node = nodeBuilder.create();

        node.validate();

        assertThat(node.getDimension()).isEqualTo(Dimension.VOLUME);
    }

    @Test
    // formula = safeDivide(readingTypeRequirement1, readingTypeRequirement2, constant(1))
    public void testSafeDivisionByOne() {
        when(readingTypeRequirement1.getDimension()).thenReturn(Dimension.SURFACE);
        when(readingTypeRequirement2.getDimension()).thenReturn(Dimension.LENGTH);
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();

        ServerFormulaBuilder builder = service.newFormulaBuilder(Formula.Mode.EXPERT);
        ExpressionNodeBuilder nodeBuilder =
                builder.safeDivide(
                        builder.requirement(readingTypeRequirement1),
                        builder.requirement(readingTypeRequirement2),
                        builder.constant(BigDecimal.ONE));
        ServerExpressionNode node = nodeBuilder.create();

        // Business method
        node.validate();

        // Asserts
        assertThat(node.getDimension()).isEqualTo(Dimension.LENGTH);
    }

    @Test(expected = InvalidNodeException.class)
    // formula = safeDivide(readingTypeRequirement1, readingTypeRequirement2, constant(0))
    public void testSafeDivisionByZero() {
        when(readingTypeRequirement1.getDimension()).thenReturn(Dimension.SURFACE);
        when(readingTypeRequirement2.getDimension()).thenReturn(Dimension.LENGTH);
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();

        ServerFormulaBuilder builder = service.newFormulaBuilder(Formula.Mode.EXPERT);
        ExpressionNodeBuilder nodeBuilder =
                builder.safeDivide(
                        builder.requirement(readingTypeRequirement1),
                        builder.requirement(readingTypeRequirement2),
                        builder.constant(BigDecimal.ZERO));
        ServerExpressionNode node = nodeBuilder.create();

        // Business method
        try {
            node.validate();
        } catch (InvalidNodeException e) {
            // Asserts
            assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.SAFE_DIVISION_REQUIRES_NON_ZERO_NUMERICAL_CONSTANT);
            throw e;
        }
    }

    @Test(expected = InvalidNodeException.class)
    // formula = safeDivide(readingTypeRequirement1, readingTypeRequirement2, readingTypeRequirement3))
    public void testSafeDivisionWithExpression() {
        when(readingTypeRequirement1.getDimension()).thenReturn(Dimension.SURFACE);
        when(readingTypeRequirement2.getDimension()).thenReturn(Dimension.LENGTH);
        when(readingTypeRequirement3.getDimension()).thenReturn(Dimension.LENGTH);
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();

        ServerFormulaBuilder builder = service.newFormulaBuilder(Formula.Mode.EXPERT);
        ExpressionNodeBuilder nodeBuilder =
                builder.safeDivide(
                        builder.requirement(readingTypeRequirement1),
                        builder.requirement(readingTypeRequirement2),
                        builder.requirement(readingTypeRequirement3));
        ServerExpressionNode node = nodeBuilder.create();

        try {
            // Business method
            node.validate();
        } catch (InvalidNodeException e) {
            // Asserts
            assertThat(e.getMessageSeed()).isEqualTo(MessageSeeds.SAFE_DIVISION_REQUIRES_NUMERICAL_CONSTANT);
            throw e;
        }
    }

}