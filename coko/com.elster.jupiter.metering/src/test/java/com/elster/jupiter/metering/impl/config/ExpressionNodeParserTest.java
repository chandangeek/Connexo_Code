package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.config.AggregationLevel;
import com.elster.jupiter.metering.config.ConstantNode;
import com.elster.jupiter.metering.config.ExpressionNode;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.Function;
import com.elster.jupiter.metering.config.FunctionCallNode;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.nls.Thesaurus;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link ExpressionNodeParser} component.
 */
@RunWith(MockitoJUnitRunner.class)
public class ExpressionNodeParserTest {

    @Mock
    private Thesaurus thesaurus;
    @Mock
    private MetrologyConfiguration config;
    @Mock
    private ServerMetrologyConfigurationService metrologyConfigurationService;
    @Mock
    private ReadingTypeRequirement readingTypeRequirement1;
    @Mock
    private ReadingTypeRequirement readingTypeRequirement2;

    @Before
    public void initializeMocks() {
        when(this.metrologyConfigurationService.findReadingTypeRequirement(10)).thenReturn(Optional.of(this.readingTypeRequirement1));
        when(this.metrologyConfigurationService.findReadingTypeRequirement(11)).thenReturn(Optional.of(this.readingTypeRequirement2));
        when(this.readingTypeRequirement1.getMetrologyConfiguration()).thenReturn(this.config);
        when(this.readingTypeRequirement2.getMetrologyConfiguration()).thenReturn(this.config);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSumWithoutAggregationLevel() {
        String formulaString = "sum(R(10))";

        // Business method
        new ExpressionNodeParser(this.thesaurus, this.metrologyConfigurationService, config, Formula.Mode.EXPERT).parse(formulaString);

        // Asserts: see expected exception rule
    }

    @Test
    public void testSumWithAggregationLevelInLowerCase() {
        String formulaString = "sum(R(10), month)";

        // Business method
        ExpressionNode node = new ExpressionNodeParser(this.thesaurus, this.metrologyConfigurationService, config, Formula.Mode.EXPERT).parse(formulaString);

        // Asserts
        assertThat(node).isInstanceOf(FunctionCallNode.class);
        FunctionCallNode functionCall = (FunctionCallNode) node;
        assertThat(functionCall.getFunction()).isEqualTo(Function.SUM);
        assertThat(functionCall.getAggregationLevel()).contains(AggregationLevel.MONTH);
    }

    @Test
    public void testSumWithAggregationLevelInUpperCase() {
        String formulaString = "sum(R(10), MONTH)";

        // Business method
        ExpressionNode node = new ExpressionNodeParser(this.thesaurus, this.metrologyConfigurationService, config, Formula.Mode.EXPERT).parse(formulaString);

        // Asserts
        assertThat(node).isInstanceOf(FunctionCallNode.class);
        FunctionCallNode functionCall = (FunctionCallNode) node;
        assertThat(functionCall.getFunction()).isEqualTo(Function.SUM);
        assertThat(functionCall.getAggregationLevel()).contains(AggregationLevel.MONTH);
    }

    @Test
    public void testSumWithAggregationLevelInMixedCase() {
        String formulaString = "sum(R(10), MontH)";

        // Business method
        ExpressionNode node = new ExpressionNodeParser(this.thesaurus, this.metrologyConfigurationService, config, Formula.Mode.EXPERT).parse(formulaString);

        // Asserts
        assertThat(node).isInstanceOf(FunctionCallNode.class);
        FunctionCallNode functionCall = (FunctionCallNode) node;
        assertThat(functionCall.getFunction()).isEqualTo(Function.SUM);
        assertThat(functionCall.getAggregationLevel()).contains(AggregationLevel.MONTH);
    }

    @Test
    public void testMaxOf() {
        String formulaString = "maxOf(R(10), week)";

        // Business method
        ExpressionNode node = new ExpressionNodeParser(this.thesaurus, this.metrologyConfigurationService, config, Formula.Mode.EXPERT).parse(formulaString);

        // Asserts
        assertThat(node).isInstanceOf(FunctionCallNode.class);
        FunctionCallNode functionCall = (FunctionCallNode) node;
        assertThat(functionCall.getFunction()).isEqualTo(Function.MAX_AGG);
        assertThat(functionCall.getAggregationLevel()).contains(AggregationLevel.WEEK);
    }

    @Test
    public void testMinOf() {
        String formulaString = "minOf(R(10), week)";

        // Business method
        ExpressionNode node = new ExpressionNodeParser(this.thesaurus, this.metrologyConfigurationService, config, Formula.Mode.EXPERT).parse(formulaString);

        // Asserts
        assertThat(node).isInstanceOf(FunctionCallNode.class);
        FunctionCallNode functionCall = (FunctionCallNode) node;
        assertThat(functionCall.getFunction()).isEqualTo(Function.MIN_AGG);
        assertThat(functionCall.getAggregationLevel()).contains(AggregationLevel.WEEK);
    }

    @Test
    public void testMaxWithMultipleArguments() {
        String formulaString = "max(constant(100), constant(10), constant(5))";

        // Business method
        ExpressionNode node = new ExpressionNodeParser(this.thesaurus, this.metrologyConfigurationService, config, Formula.Mode.EXPERT).parse(formulaString);

        // Asserts
        assertThat(node).isInstanceOf(FunctionCallNode.class);
        FunctionCallNode min = (FunctionCallNode) node;
        assertThat(min.getFunction()).isEqualTo(Function.MAX);
        assertThat(min.getAggregationLevel()).isEmpty();
        assertThat(min.getChildren()).hasSize(3);
        assertThat(min.getChildren().get(0)).isInstanceOf(ConstantNode.class);
        ConstantNode const100 = (ConstantNode) min.getChildren().get(0);
        assertThat(const100.getValue()).isEqualTo(BigDecimal.valueOf(100));
        assertThat(min.getChildren().get(1)).isInstanceOf(ConstantNode.class);
        ConstantNode const10 = (ConstantNode) min.getChildren().get(1);
        assertThat(const10.getValue()).isEqualTo(BigDecimal.TEN);
        assertThat(min.getChildren().get(2)).isInstanceOf(ConstantNode.class);
        ConstantNode const5 = (ConstantNode) min.getChildren().get(2);
        assertThat(const5.getValue()).isEqualTo(BigDecimal.valueOf(5));
    }

    @Test
    public void testMinWithMultipleArguments() {
        String formulaString = "min(constant(100), constant(10), constant(5))";

        // Business method
        ExpressionNode node = new ExpressionNodeParser(this.thesaurus, this.metrologyConfigurationService, config, Formula.Mode.EXPERT).parse(formulaString);

        // Asserts
        assertThat(node).isInstanceOf(FunctionCallNode.class);
        FunctionCallNode min = (FunctionCallNode) node;
        assertThat(min.getFunction()).isEqualTo(Function.MIN);
        assertThat(min.getAggregationLevel()).isEmpty();
        assertThat(min.getChildren()).hasSize(3);
        assertThat(min.getChildren().get(0)).isInstanceOf(ConstantNode.class);
        ConstantNode const100 = (ConstantNode) min.getChildren().get(0);
        assertThat(const100.getValue()).isEqualTo(BigDecimal.valueOf(100));
        assertThat(min.getChildren().get(1)).isInstanceOf(ConstantNode.class);
        ConstantNode const10 = (ConstantNode) min.getChildren().get(1);
        assertThat(const10.getValue()).isEqualTo(BigDecimal.TEN);
        assertThat(min.getChildren().get(2)).isInstanceOf(ConstantNode.class);
        ConstantNode const5 = (ConstantNode) min.getChildren().get(2);
        assertThat(const5.getValue()).isEqualTo(BigDecimal.valueOf(5));
    }

    @Test
    public void testSquareRoot() {
        String formulaString = "sqrt(constant(100))";

        // Business method
        ExpressionNode node = new ExpressionNodeParser(this.thesaurus, this.metrologyConfigurationService, config, Formula.Mode.EXPERT).parse(formulaString);

        // Asserts
        assertThat(node).isInstanceOf(FunctionCallNode.class);
        FunctionCallNode sqrt = (FunctionCallNode) node;
        assertThat(sqrt.getFunction()).isEqualTo(Function.SQRT);
        assertThat(sqrt.getAggregationLevel()).isEmpty();
        assertThat(sqrt.getChildren()).hasSize(1);
        assertThat(sqrt.getChildren().get(0)).isInstanceOf(ConstantNode.class);
        ConstantNode const100 = (ConstantNode) sqrt.getChildren().get(0);
        assertThat(const100.getValue()).isEqualTo(BigDecimal.valueOf(100));
    }

    @Test
    public void testPower() {
        String formulaString = "power(constant(10), constant(3))";

        // Business method
        ExpressionNode node = new ExpressionNodeParser(this.thesaurus, this.metrologyConfigurationService, config, Formula.Mode.EXPERT).parse(formulaString);

        // Asserts
        assertThat(node).isInstanceOf(FunctionCallNode.class);
        FunctionCallNode power = (FunctionCallNode) node;
        assertThat(power.getFunction()).isEqualTo(Function.POWER);
        assertThat(power.getAggregationLevel()).isEmpty();
        assertThat(power.getChildren()).hasSize(2);
        assertThat(power.getChildren().get(0)).isInstanceOf(ConstantNode.class);
        ConstantNode const10 = (ConstantNode) power.getChildren().get(0);
        assertThat(const10.getValue()).isEqualTo(BigDecimal.valueOf(10));
        assertThat(power.getChildren().get(1)).isInstanceOf(ConstantNode.class);
        ConstantNode const3 = (ConstantNode) power.getChildren().get(1);
        assertThat(const3.getValue()).isEqualTo(BigDecimal.valueOf(3));
    }

    @Test
    public void testNestedAggregationLevels() {
        String formulaString = "max(sum(R(10), day), sum(R(11), hour))";

        // Business method
        ExpressionNode node = new ExpressionNodeParser(this.thesaurus, this.metrologyConfigurationService, config, Formula.Mode.EXPERT).parse(formulaString);

        // Asserts
        assertThat(node).isInstanceOf(FunctionCallNode.class);
        FunctionCallNode max = (FunctionCallNode) node;
        assertThat(max.getFunction()).isEqualTo(Function.MAX);
        assertThat(max.getAggregationLevel()).isEmpty();
        assertThat(max.getChildren()).hasSize(2);
        assertThat(max.getChildren().get(0)).isInstanceOf(FunctionCallNode.class);
        FunctionCallNode sumR10 = (FunctionCallNode) max.getChildren().get(0);
        assertThat(sumR10.getFunction()).isEqualTo(Function.SUM);
        assertThat(sumR10.getAggregationLevel()).contains(AggregationLevel.DAY);
        assertThat(sumR10.getChildren()).hasSize(1);
        assertThat(max.getChildren().get(1)).isInstanceOf(FunctionCallNode.class);
        FunctionCallNode sumR11 = (FunctionCallNode) max.getChildren().get(1);
        assertThat(sumR11.getFunction()).isEqualTo(Function.SUM);
        assertThat(sumR11.getAggregationLevel()).contains(AggregationLevel.HOUR);
        assertThat(sumR11.getChildren()).hasSize(1);
    }

}