/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.Operator;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeDeliverableNode;
import com.elster.jupiter.metering.impl.config.ConstantNodeImpl;
import com.elster.jupiter.metering.impl.config.OperationNodeImpl;
import com.elster.jupiter.metering.impl.config.ReadingTypeDeliverableNodeImpl;
import com.elster.jupiter.nls.Thesaurus;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link DependencyAnalyzer} component.
 */
@RunWith(MockitoJUnitRunner.class)
public class DependencyAnalyzerTest {

    @Mock
    private MetrologyContract contract;
    @Mock
    private Thesaurus thesaurus;

    private ConstantNodeImpl ten;
    private ConstantNodeImpl thousand;
    private OperationNodeImpl tenTimesThousand;

    @Before
    public void initializeNodes() {
        this.ten = new ConstantNodeImpl(BigDecimal.TEN);
        this.thousand = new ConstantNodeImpl(BigDecimal.valueOf(1000L));
        this.tenTimesThousand = new OperationNodeImpl(Operator.MULTIPLY, this.ten, this.thousand, this.thesaurus);
    }

    @Test
    public void noDeliverables() {
        when(this.contract.getDeliverables()).thenReturn(Collections.emptyList());
        DependencyAnalyzer analyzer = this.getInstance();

        // Business method
        List<ReadingTypeDeliverable> deliverables = analyzer.getDeliverables();

        // Asserts
        verify(this.contract).getDeliverables();
        assertThat(deliverables).isEmpty();
    }

    @Test
    public void oneDeliverableWithOnlyConstants() {
        Formula formula = mock(Formula.class);
        when(formula.getExpressionNode()).thenReturn(tenTimesThousand);
        ReadingTypeDeliverable deliverable = mockDeliverable(1, formula);
        when(this.contract.getDeliverables()).thenReturn(Collections.singletonList(deliverable));
        DependencyAnalyzer analyzer = this.getInstance();

        // Business method
        List<ReadingTypeDeliverable> deliverables = analyzer.getDeliverables();

        // Asserts
        verify(this.contract).getDeliverables();
        assertThat(deliverables).containsExactly(deliverable);
    }

    @Test
    public void yDependingOnX() {
        Formula formulaX = mock(Formula.class);
        when(formulaX.getExpressionNode()).thenReturn(tenTimesThousand);
        ReadingTypeDeliverable x = mockDeliverable(1L, formulaX);
        Formula formulaY = mock(Formula.class);
        ReadingTypeDeliverableNode xNode = new ReadingTypeDeliverableNodeImpl(x);
        when(formulaY.getExpressionNode()).thenReturn(xNode);
        ReadingTypeDeliverable y = mockDeliverable(2L, formulaY);
        when(this.contract.getDeliverables()).thenReturn(Arrays.asList(y, x));
        DependencyAnalyzer analyzer = this.getInstance();

        // Business method
        List<ReadingTypeDeliverable> deliverables = analyzer.getDeliverables();

        // Asserts
        verify(this.contract).getDeliverables();
        assertThat(deliverables).containsExactly(x, y);
    }

    @Test
    public void yAndZDependingOnX() {
        Formula formulaX = mock(Formula.class);
        when(formulaX.getExpressionNode()).thenReturn(tenTimesThousand);
        ReadingTypeDeliverable x = mockDeliverable(1L, formulaX);
        Formula formulaY = mock(Formula.class);
        ReadingTypeDeliverableNode refToXFromY = new ReadingTypeDeliverableNodeImpl(x);
        when(formulaY.getExpressionNode()).thenReturn(refToXFromY);
        ReadingTypeDeliverable y = mockDeliverable(2L, formulaY);
        Formula formulaZ = mock(Formula.class);
        ReadingTypeDeliverableNode refToXFromZ = new ReadingTypeDeliverableNodeImpl(x);
        when(formulaZ.getExpressionNode()).thenReturn(refToXFromZ);
        ReadingTypeDeliverable z = mockDeliverable(3L, formulaZ);
        when(this.contract.getDeliverables()).thenReturn(Arrays.asList(z, y, x));
        DependencyAnalyzer analyzer = this.getInstance();

        // Business method
        List<ReadingTypeDeliverable> deliverables = analyzer.getDeliverables();

        // Asserts
        verify(this.contract).getDeliverables();
        assertThat(deliverables).containsExactly(x, z, y);
    }

    @Test
    public void zDependingOnYDependingOnX() {
        Formula formulaX = mock(Formula.class);
        when(formulaX.getExpressionNode()).thenReturn(tenTimesThousand);
        ReadingTypeDeliverable x = mockDeliverable(1L, formulaX);
        Formula formulaY = mock(Formula.class);
        ReadingTypeDeliverableNode refToX = new ReadingTypeDeliverableNodeImpl(x);
        when(formulaY.getExpressionNode()).thenReturn(refToX);
        ReadingTypeDeliverable y = mockDeliverable(2L, formulaY);
        Formula formulaZ = mock(Formula.class);
        ReadingTypeDeliverableNode refToY = new ReadingTypeDeliverableNodeImpl(y);
        when(formulaZ.getExpressionNode()).thenReturn(refToY);
        ReadingTypeDeliverable z = mockDeliverable(3L, formulaZ);
        when(this.contract.getDeliverables()).thenReturn(Arrays.asList(z, y, x));
        DependencyAnalyzer analyzer = this.getInstance();

        // Business method
        List<ReadingTypeDeliverable> deliverables = analyzer.getDeliverables();

        // Asserts
        verify(this.contract).getDeliverables();
        assertThat(deliverables).containsExactly(x, y, z);
    }

    private ReadingTypeDeliverable mockDeliverable(long id, Formula formula) {
        ReadingTypeDeliverable deliverable = mock(ReadingTypeDeliverable.class);
        when(deliverable.getId()).thenReturn(id);
        when(deliverable.getFormula()).thenReturn(formula);
        return deliverable;
    }

    private DependencyAnalyzer getInstance() {
        return DependencyAnalyzer.forAnalysisOf(this.contract);
    }

}