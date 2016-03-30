package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.config.ExpressionNode;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.impl.MeteringInMemoryBootstrapModule;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.units.Dimension;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
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

    private static MeteringInMemoryBootstrapModule inMemoryBootstrapModule = new MeteringInMemoryBootstrapModule();

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

    private TransactionService getTransactionService() {
        return inMemoryBootstrapModule.getTransactionService();
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
        ExpressionNode node = nodeBuilder.create();
        try {
            node.validate();
        } catch (InvalidNodeException e) {
            fail("No InvalidNodeException expected!");
        }
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
        ExpressionNode node = nodeBuilder.create();
        try {
            node.validate();
        } catch (InvalidNodeException e) {
            fail("No InvalidNodeException expected!");
        }
    }

    @Test
    // formula = minus(readingTypeRequirement1, readingTypeRequirement2)
    public void testSubstractionOfIncompatibleDimensions() {
        when(readingTypeRequirement1.getDimension()).thenReturn(Dimension.POWER);
        when(readingTypeRequirement2.getDimension()).thenReturn(Dimension.CURRENCY);
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();

        ServerFormulaBuilder builder = service.newFormulaBuilder(Formula.Mode.EXPERT);

        ExpressionNodeBuilder nodeBuilder = builder.minus(
                builder.requirement(readingTypeRequirement1),
                builder.requirement(readingTypeRequirement2));
        ExpressionNode node = nodeBuilder.create();
        try {
            node.validate();
            fail("InvalidNodeException expected");
        } catch (InvalidNodeException e) {
            assertEquals(e.getMessage(),"Only dimensions that are compatible for automatic unit conversion can be summed or substracted.");
        }
    }

    @Test
    // formula = plus(readingTypeRequirement1, readingTypeRequirement2)
    public void testSumOfIncompatibleDimensions() {
        when(readingTypeRequirement1.getDimension()).thenReturn(Dimension.POWER);
        when(readingTypeRequirement2.getDimension()).thenReturn(Dimension.CURRENCY);
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();

        ServerFormulaBuilder builder = service.newFormulaBuilder(Formula.Mode.EXPERT);

        ExpressionNodeBuilder nodeBuilder = builder.plus(
                builder.requirement(readingTypeRequirement1),
                builder.requirement(readingTypeRequirement2));
        ExpressionNode node = nodeBuilder.create();
        try {
            node.validate();
            fail("InvalidNodeException expected");
        } catch (InvalidNodeException e) {
            assertEquals(e.getMessage(), "Only dimensions that are compatible for automatic unit conversion can be summed or substracted.");
        }
    }

    @Test
     // formula = multiply(readingTypeRequirement1, readingTypeRequirement2)
     public void testMultiplicationOfIncompatibleDimensions() {
        when(readingTypeRequirement1.getDimension()).thenReturn(Dimension.POWER);
        when(readingTypeRequirement2.getDimension()).thenReturn(Dimension.ELECTRIC_CURRENT);
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();

        ServerFormulaBuilder builder = service.newFormulaBuilder(Formula.Mode.EXPERT);

        ExpressionNodeBuilder nodeBuilder = builder.multiply(
                builder.requirement(readingTypeRequirement1),
                builder.requirement(readingTypeRequirement2));
        ExpressionNode node = nodeBuilder.create();
        try {
            node.validate();
            fail("InvalidNodeException expected");
        } catch (InvalidNodeException e) {
            assertEquals(e.getMessage(), "Dimensions from multiplication arguments do not result in a valid dimension.");
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
        ExpressionNode node = nodeBuilder.create();
        try {
            node.validate();
            assertEquals(node.getDimension(), Dimension.SURFACE);
        } catch (InvalidNodeException e) {
            fail("No InvalidNodeException expected!");
        }
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
        ExpressionNode node = nodeBuilder.create();
        try {
            node.validate();
            assertEquals(node.getDimension(), Dimension.LENGTH);
        } catch (InvalidNodeException e) {
            fail("No InvalidNodeException expected!");
        }
    }

    @Test
    // formula = divide(readingTypeRequirement1, readingTypeRequirement2)
    public void testDivisionOfIncompatibleDimensions() {
        when(readingTypeRequirement1.getDimension()).thenReturn(Dimension.SURFACE);
        when(readingTypeRequirement2.getDimension()).thenReturn(Dimension.ELECTRIC_CURRENT);
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();

        ServerFormulaBuilder builder = service.newFormulaBuilder(Formula.Mode.EXPERT);

        ExpressionNodeBuilder nodeBuilder = builder.divide(
                builder.requirement(readingTypeRequirement1),
                builder.requirement(readingTypeRequirement2));
        ExpressionNode node = nodeBuilder.create();
        try {
            node.validate();
            fail("InvalidNodeException expected");
        } catch (InvalidNodeException e) {
            assertEquals(e.getMessage(), "Dimensions from division arguments do not result in a valid dimension.");
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
        ExpressionNode node = nodeBuilder.create();
        try {
            node.validate();
            assertEquals(node.getDimension(), Dimension.VOLUME);
        } catch (InvalidNodeException e) {
            fail("No InvalidNodeException expected!");
        }
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
                builder.requirement(readingTypeRequirement1),
                builder.requirement(readingTypeRequirement2),
                builder.requirement(readingTypeRequirement3));
        ExpressionNode node = nodeBuilder.create();
        try {
            node.validate();
            assertEquals(node.getDimension(), Dimension.VOLUME);
        } catch (InvalidNodeException e) {
            fail("No InvalidNodeException expected!");
        }
    }

    @Test
    // formula = max(readingTypeRequirement1, readingTypeRequirement2, readingTypeRequirement3)
    public void testMaximumOfIncompatibleDimensions() {
        when(readingTypeRequirement1.getDimension()).thenReturn(Dimension.VOLUME);
        when(readingTypeRequirement2.getDimension()).thenReturn(Dimension.POWER);
        when(readingTypeRequirement3.getDimension()).thenReturn(Dimension.ELECTRIC_CHARGE);
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();

        ServerFormulaBuilder builder = service.newFormulaBuilder(Formula.Mode.EXPERT);

        ExpressionNodeBuilder nodeBuilder = builder.maximum(
                builder.requirement(readingTypeRequirement1),
                builder.requirement(readingTypeRequirement2),
                builder.requirement(readingTypeRequirement3));
        ExpressionNode node = nodeBuilder.create();
        try {
            node.validate();
            fail("InvalidNodeException expected");
        } catch (InvalidNodeException e) {
            assertEquals(e.getMessage(), "Only dimensions that are compatible for automatic unit conversion can be used as children of a function.");
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
                builder.divide(builder.requirement(temperature2), builder.requirement(pressure2)) );
        ExpressionNode node = nodeBuilder.create();
        try {
            node.validate();
            assertEquals(node.getDimension(), Dimension.VOLUME);
        } catch (InvalidNodeException e) {
            fail("No InvalidNodeException expected!");
        }
    }

}