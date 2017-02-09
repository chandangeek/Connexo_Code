/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.ReadingType;
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
 * Tests the {@link RegularDeliverableComplexityAnalyzer} component.
 */
public class RegularDeliverableComplexityAnalyzerTest {

    @Test
    public void nullOnly() {
        NullNodeImpl node = new NullNodeImpl();
        RegularDeliverableComplexityAnalyzer complexity = this.getTestInstance();

        // Business method & asserts
        assertThat(node.accept(complexity)).isTrue();
    }

    @Test
    public void constantOnly() {
        ConstantNodeImpl node = new ConstantNodeImpl(BigDecimal.TEN);
        RegularDeliverableComplexityAnalyzer complexity = this.getTestInstance();

        // Business method & asserts
        assertThat(node.accept(complexity)).isTrue();
    }

    @Test
    public void oneRequirement() {
        ReadingTypeRequirement requirement = mock(ReadingTypeRequirement.class);
        when(requirement.isRegular()).thenReturn(true);
        ReadingTypeRequirementNodeImpl node = new ReadingTypeRequirementNodeImpl(requirement);
        RegularDeliverableComplexityAnalyzer complexity = this.getTestInstance();

        // Business method & asserts
        assertThat(node.accept(complexity)).isTrue();
    }

    @Test
    public void oneIrregularRequirement() {
        ReadingTypeRequirement requirement = mock(ReadingTypeRequirement.class);
        when(requirement.isRegular()).thenReturn(false);
        ReadingTypeRequirementNodeImpl node = new ReadingTypeRequirementNodeImpl(requirement);
        RegularDeliverableComplexityAnalyzer complexity = this.getTestInstance();

        // Business method & asserts
        assertThat(node.accept(complexity)).isFalse();
    }

    @Test
    public void twoRegularRequirements() {
        Thesaurus thesaurus = mock(Thesaurus.class);
        ReadingTypeRequirement requirement1 = mock(ReadingTypeRequirement.class);
        when(requirement1.isRegular()).thenReturn(true);
        ReadingTypeRequirement requirement2 = mock(ReadingTypeRequirement.class);
        when(requirement2.isRegular()).thenReturn(true);
        ReadingTypeRequirementNodeImpl requirementNode1 = new ReadingTypeRequirementNodeImpl(requirement1);
        ReadingTypeRequirementNodeImpl requirementNode2 = new ReadingTypeRequirementNodeImpl(requirement2);
        OperationNode node =
                new OperationNodeImpl(
                        Operator.DIVIDE,
                        new OperationNodeImpl(
                                Operator.PLUS,
                                requirementNode1,
                                new ConstantNodeImpl(BigDecimal.TEN),
                                thesaurus),
                        requirementNode2,
                        thesaurus);
        RegularDeliverableComplexityAnalyzer complexity = this.getTestInstance();

        // Business method & asserts
        assertThat(node.accept(complexity)).isTrue();
    }

    @Test
    public void twoIrregularRequirements() {
        Thesaurus thesaurus = mock(Thesaurus.class);
        ReadingTypeRequirement requirement1 = mock(ReadingTypeRequirement.class);
        when(requirement1.isRegular()).thenReturn(false);
        ReadingTypeRequirement requirement2 = mock(ReadingTypeRequirement.class);
        when(requirement2.isRegular()).thenReturn(false);
        ReadingTypeRequirementNodeImpl requirementNode1 = new ReadingTypeRequirementNodeImpl(requirement1);
        ReadingTypeRequirementNodeImpl requirementNode2 = new ReadingTypeRequirementNodeImpl(requirement2);
        OperationNode node =
                new OperationNodeImpl(
                        Operator.DIVIDE,
                        new OperationNodeImpl(
                                Operator.PLUS,
                                requirementNode1,
                                new ConstantNodeImpl(BigDecimal.TEN),
                                thesaurus),
                        requirementNode2,
                        thesaurus);
        RegularDeliverableComplexityAnalyzer complexity = this.getTestInstance();

        // Business method & asserts
        assertThat(node.accept(complexity)).isFalse();
    }

    @Test
    public void oneRegularDeliverable() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.isRegular()).thenReturn(true);
        ReadingTypeDeliverable deliverable = mock(ReadingTypeDeliverable.class);
        when(deliverable.getReadingType()).thenReturn(readingType);
        ReadingTypeDeliverableNodeImpl node = new ReadingTypeDeliverableNodeImpl(deliverable);
        RegularDeliverableComplexityAnalyzer complexity = this.getTestInstance();

        // Business method & asserts
        assertThat(node.accept(complexity)).isTrue();
    }

    @Test
    public void oneIrregularDeliverable() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.isRegular()).thenReturn(false);
        ReadingTypeDeliverable deliverable = mock(ReadingTypeDeliverable.class);
        when(deliverable.getReadingType()).thenReturn(readingType);
        ReadingTypeDeliverableNodeImpl node = new ReadingTypeDeliverableNodeImpl(deliverable);
        RegularDeliverableComplexityAnalyzer complexity = this.getTestInstance();

        // Business method & asserts
        assertThat(node.accept(complexity)).isFalse();
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
        RegularDeliverableComplexityAnalyzer complexity = this.getTestInstance();

        // Business method & asserts
        assertThat(node.accept(complexity)).isTrue();
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
        RegularDeliverableComplexityAnalyzer complexity = this.getTestInstance();

        // Business method & asserts
        assertThat(node.accept(complexity)).isTrue();
    }

    private RegularDeliverableComplexityAnalyzer getTestInstance() {
        return new RegularDeliverableComplexityAnalyzer();
    }

}