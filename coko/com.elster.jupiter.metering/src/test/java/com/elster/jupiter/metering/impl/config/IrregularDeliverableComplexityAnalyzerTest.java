/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.config.Function;
import com.elster.jupiter.metering.config.FunctionCallNode;
import com.elster.jupiter.metering.config.OperationNode;
import com.elster.jupiter.metering.config.Operator;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.nls.Thesaurus;

import java.math.BigDecimal;
import java.util.Arrays;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link IrregularDeliverableComplexityAnalyzer} component.
 */
public class IrregularDeliverableComplexityAnalyzerTest {

    @Test
    public void nullOnly() {
        NullNodeImpl node = new NullNodeImpl();
        IrregularDeliverableComplexityAnalyzer complexity = this.getTestInstance();

        // Business method
        node.accept(complexity);

        // Asserts
        assertThat(complexity.isSimple()).isTrue();
    }

    @Test
    public void constantOnly() {
        ConstantNodeImpl node = new ConstantNodeImpl(BigDecimal.TEN);
        IrregularDeliverableComplexityAnalyzer complexity = this.getTestInstance();

        // Business method
        node.accept(complexity);

        // Asserts
        assertThat(complexity.isSimple()).isTrue();
    }

    @Test
    public void oneRequirement() {
        ReadingTypeRequirement requirement = mock(ReadingTypeRequirement.class);
        when(requirement.isRegular()).thenReturn(false);
        ReadingTypeRequirementNodeImpl node = new ReadingTypeRequirementNodeImpl(requirement);
        IrregularDeliverableComplexityAnalyzer complexity = this.getTestInstance();

        // Business method
        node.accept(complexity);

        // Asserts
        assertThat(complexity.isSimple()).isTrue();
    }

    @Test
    public void oneRegularRequirement() {
        ReadingTypeRequirement requirement = mock(ReadingTypeRequirement.class);
        when(requirement.isRegular()).thenReturn(true);
        ReadingTypeRequirementNodeImpl node = new ReadingTypeRequirementNodeImpl(requirement);
        IrregularDeliverableComplexityAnalyzer complexity = this.getTestInstance();

        // Business method
        node.accept(complexity);

        // Asserts
        assertThat(complexity.isSimple()).isFalse();
    }

    @Test
    public void twoRequirements() {
        Thesaurus thesaurus = mock(Thesaurus.class);
        ReadingTypeRequirementNodeImpl requirement1 = new ReadingTypeRequirementNodeImpl(mock(ReadingTypeRequirement.class));
        ReadingTypeRequirementNodeImpl requirement2 = new ReadingTypeRequirementNodeImpl(mock(ReadingTypeRequirement.class));
        OperationNode node =
                new OperationNodeImpl(
                        Operator.DIVIDE,
                        new OperationNodeImpl(
                                Operator.PLUS,
                                requirement1,
                                new ConstantNodeImpl(BigDecimal.TEN),
                                thesaurus),
                        requirement2,
                        thesaurus);
        IrregularDeliverableComplexityAnalyzer complexity = this.getTestInstance();

        // Business method
        node.accept(complexity);

        // Asserts
        assertThat(complexity.isSimple()).isFalse();
    }

    @Test
    public void oneDeliverable() {
        ReadingTypeDeliverableNodeImpl node = new ReadingTypeDeliverableNodeImpl(mock(ReadingTypeDeliverable.class));
        IrregularDeliverableComplexityAnalyzer complexity = this.getTestInstance();

        // Business method
        node.accept(complexity);

        // Asserts
        assertThat(complexity.isSimple()).isFalse();
    }

    @Test
    public void operationsOnConstantsOnly() {
        Thesaurus thesaurus = mock(Thesaurus.class);
        ConstantNodeImpl ten = new ConstantNodeImpl(BigDecimal.TEN);
        ConstantNodeImpl twenty = new ConstantNodeImpl(BigDecimal.valueOf(20L));
        ConstantNodeImpl three = new ConstantNodeImpl(BigDecimal.valueOf(3L));
        OperationNode node =
                new OperationNodeImpl(
                        Operator.DIVIDE,
                        new OperationNodeImpl(
                                Operator.PLUS,
                                ten,
                                twenty,
                                thesaurus),
                        three,
                        thesaurus);
        IrregularDeliverableComplexityAnalyzer complexity = this.getTestInstance();

        // Business method
        node.accept(complexity);

        // Asserts
        assertThat(complexity.isSimple()).isTrue();
    }

    @Test
    public void functionCallWithConstantsOnly() {
        Thesaurus thesaurus = mock(Thesaurus.class);
        ConstantNodeImpl ten = new ConstantNodeImpl(BigDecimal.TEN);
        ConstantNodeImpl twenty = new ConstantNodeImpl(BigDecimal.valueOf(20L));
        ConstantNodeImpl three = new ConstantNodeImpl(BigDecimal.valueOf(3L));
        FunctionCallNode node =
                new FunctionCallNodeImpl(
                        Arrays.asList(ten, twenty, three),
                        Function.FIRST_NOT_NULL,
                        null,
                        thesaurus);
        IrregularDeliverableComplexityAnalyzer complexity = this.getTestInstance();

        // Business method
        node.accept(complexity);

        // Asserts
        assertThat(complexity.isSimple()).isTrue();
    }

    @Test
    public void minAggregationFunction() {
        Thesaurus thesaurus = mock(Thesaurus.class);
        ConstantNodeImpl ten = new ConstantNodeImpl(BigDecimal.TEN);
        ConstantNodeImpl twenty = new ConstantNodeImpl(BigDecimal.valueOf(20L));
        ConstantNodeImpl three = new ConstantNodeImpl(BigDecimal.valueOf(3L));
        FunctionCallNode node =
                new FunctionCallNodeImpl(
                        Arrays.asList(ten, twenty, three),
                        Function.MIN_AGG,
                        null,
                        thesaurus);
        IrregularDeliverableComplexityAnalyzer complexity = this.getTestInstance();

        // Business method
        node.accept(complexity);

        // Asserts
        assertThat(complexity.isSimple()).isFalse();
    }

    @Test
    public void maxAggregationFunction() {
        Thesaurus thesaurus = mock(Thesaurus.class);
        ConstantNodeImpl ten = new ConstantNodeImpl(BigDecimal.TEN);
        ConstantNodeImpl twenty = new ConstantNodeImpl(BigDecimal.valueOf(20L));
        ConstantNodeImpl three = new ConstantNodeImpl(BigDecimal.valueOf(3L));
        FunctionCallNode node =
                new FunctionCallNodeImpl(
                        Arrays.asList(ten, twenty, three),
                        Function.MAX_AGG,
                        null,
                        thesaurus);
        IrregularDeliverableComplexityAnalyzer complexity = this.getTestInstance();

        // Business method
        node.accept(complexity);

        // Asserts
        assertThat(complexity.isSimple()).isFalse();
    }

    @Test
    public void sumFunction() {
        Thesaurus thesaurus = mock(Thesaurus.class);
        ConstantNodeImpl ten = new ConstantNodeImpl(BigDecimal.TEN);
        ConstantNodeImpl twenty = new ConstantNodeImpl(BigDecimal.valueOf(20L));
        ConstantNodeImpl three = new ConstantNodeImpl(BigDecimal.valueOf(3L));
        FunctionCallNode node =
                new FunctionCallNodeImpl(
                        Arrays.asList(ten, twenty, three),
                        Function.SUM,
                        null,
                        thesaurus);
        IrregularDeliverableComplexityAnalyzer complexity = this.getTestInstance();

        // Business method
        node.accept(complexity);

        // Asserts
        assertThat(complexity.isSimple()).isFalse();
    }

    @Test
    public void avgFunction() {
        Thesaurus thesaurus = mock(Thesaurus.class);
        ConstantNodeImpl ten = new ConstantNodeImpl(BigDecimal.TEN);
        ConstantNodeImpl twenty = new ConstantNodeImpl(BigDecimal.valueOf(20L));
        ConstantNodeImpl three = new ConstantNodeImpl(BigDecimal.valueOf(3L));
        FunctionCallNode node =
                new FunctionCallNodeImpl(
                        Arrays.asList(ten, twenty, three),
                        Function.AVG,
                        null,
                        thesaurus);
        IrregularDeliverableComplexityAnalyzer complexity = this.getTestInstance();

        // Business method
        node.accept(complexity);

        // Asserts
        assertThat(complexity.isSimple()).isFalse();
    }

    private IrregularDeliverableComplexityAnalyzer getTestInstance() {
        return new IrregularDeliverableComplexityAnalyzer();
    }

}