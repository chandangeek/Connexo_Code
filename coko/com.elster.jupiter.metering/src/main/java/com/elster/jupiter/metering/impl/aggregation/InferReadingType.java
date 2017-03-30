/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.util.streams.Predicates;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Infers the most appropriate {@link VirtualReadingType} for the
 * expressions in the tree that define the way a {@link ReadingTypeDeliverable}
 * should be calculated.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-08 (12:13)
 */
class InferReadingType implements ServerExpressionNode.Visitor<VirtualReadingType> {

    private final VirtualReadingType requestedReadingType;

    InferReadingType(VirtualReadingType requestedReadingType) {
        super();
        this.requestedReadingType = requestedReadingType;
    }

    VirtualReadingType getRequestedReadingType() {
        return requestedReadingType;
    }

    @Override
    public VirtualReadingType visitTimeBasedAggregation(TimeBasedAggregationNode aggregationNode) {
        throw new IllegalArgumentException("Inference of reading type is not supported for expert mode functions");
    }

    @Override
    public VirtualReadingType visitVirtualRequirement(VirtualRequirementNode node) {
        return node.getPreferredReadingType();
    }

    @Override
    public VirtualReadingType visitVirtualDeliverable(VirtualDeliverableNode node) {
        return node.getPreferredReadingType();
    }

    @Override
    public VirtualReadingType visitConstant(NumericalConstantNode constant) {
        return VirtualReadingType.dontCare();
    }

    @Override
    public VirtualReadingType visitConstant(StringConstantNode constant) {
        return VirtualReadingType.dontCare();
    }

    @Override
    public VirtualReadingType visitProperty(CustomPropertyNode property) {
        return VirtualReadingType.dontCare();
    }

    @Override
    public VirtualReadingType visitSyntheticLoadProfile(SyntheticLoadProfilePropertyNode slp) {
        return slp.getSourceReadingType();
    }

    @Override
    public VirtualReadingType visitNull(NullNode nullNode) {
        return VirtualReadingType.dontCare();
    }

    @Override
    public VirtualReadingType visitSqlFragment(SqlFragmentNode variable) {
        return VirtualReadingType.dontCare();
    }

    @Override
    public VirtualReadingType visitUnitConversion(UnitConversionNode unitConversionNode) {
        return unitConversionNode.getTargetReadingType();
    }

    @Override
    public VirtualReadingType visitOperation(OperationNode operationNode) {
        List<ServerExpressionNode> operands = Arrays.asList(operationNode.getLeftOperand(), operationNode.getRightOperand());
        return this.visitChildren(
                operands,
                () -> new UnsupportedOperationException(
                        "The 2 operands for " + operationNode.getOperator().name() + " cannot support the same reading type"));
    }

    @Override
    public VirtualReadingType visitFunctionCall(FunctionCallNode functionCall) {
        /* Two cases: 1. The function call represents one of the known functions
                         and each of these operate on TimeSeries and maintain
                         the interval of the TimeSeries
                      2. The function call represents a custom function in which
                         case we have no clue what the semantics of the function call is.
         * In both cases, we should visit the arguments (if any)
         * and keep the integration interval of each argument consistent. */
        return this.visitChildren(
                functionCall.getArguments(),
                () -> new UnsupportedOperationException(
                        "Not all arguments of the function " + this.getFunctionName(functionCall) + " can support the same reading type"));
    }

    private VirtualReadingType visitChildren(List<ServerExpressionNode> children, Supplier<UnsupportedOperationException> unsupportedOperationExceptionSupplier) {
        List<VirtualReadingType> preferredReadingTypes =
                children
                        .stream()
                        .map(this::getPreferredReadingType)
                        .filter(Predicates.not(VirtualReadingType::isDontCare))
                        .distinct()
                        .collect(Collectors.toList());
        if (preferredReadingTypes.isEmpty()) {
            /* All child nodes have indicated not to care about the reading type
             * so we should be able to enforce the target onto each. */
            return this.enforceReadingType(children, this.requestedReadingType);
        } else {
            if (preferredReadingTypes.stream().anyMatch(VirtualReadingType::isUnsupported)) {
                throw new UnsupportedOperationException("At least one of the expression nodes represents an unsupported reading type");
            }
            if (preferredReadingTypes.size() == 1) {
                // All child nodes are fine with the same reading type, simply enforce that one
                VirtualReadingType preferredReadingType = preferredReadingTypes.iterator().next();
                return this.enforceReadingType(children, preferredReadingType);
            } else {
                // Difference of opinions, try to compromise
                return this.searchAndEnforceCompromise(children, preferredReadingTypes, unsupportedOperationExceptionSupplier);
            }
        }
    }

    private VirtualReadingType enforceReadingType(List<ServerExpressionNode> nodes, VirtualReadingType readingType) {
        new EnforceReadingType(readingType).enforceOntoAll(nodes);
        return readingType;
    }

    private VirtualReadingType enforceIntervalMultiplierAndCommodity(List<ServerExpressionNode> nodes, VirtualReadingType readingType) {
        new EnforceIntervalMultiplierAndCommodity(readingType).enforceOntoAll(nodes);
        return readingType;
    }

    /**
     * Searches for a compromise when multiple {@link VirtualReadingType}s were
     * returned by the express nodes. These are the steps involved:
     * <ol>
     * <li>check if every node can agree on the actual requested target interval and use that if that is the case</li>
     * <li>check all preferred intervals, starting with the smallest interval
     * one and enforce the first one that every node can agrees on</li>
     * </ol>
     *
     * @param nodes The expression nodes
     * @param preferredReadingTypes The preferred VirtualReadingType of each of the nodes
     * @param unsupportedOperationExceptionSupplier The supplier of the UnsupportedOperationException that will be thrown when no compromise can be found
     * @return The compromising VirtualReadingType
     */
    private VirtualReadingType searchAndEnforceCompromise(List<ServerExpressionNode> nodes, List<VirtualReadingType> preferredReadingTypes, Supplier<UnsupportedOperationException> unsupportedOperationExceptionSupplier) {
        Optional<VirtualReadingType> compromise = this.searchCompromise(nodes, preferredReadingTypes);
        if (compromise.isPresent()) {
            return this.enforceReadingType(nodes, compromise.get());
        } else if (this.checkForCompromiseOnRequestedIntervalAndMultiplier(nodes)) {
            return this.enforceIntervalMultiplierAndCommodity(nodes, this.requestedReadingType);
        } else {
            throw unsupportedOperationExceptionSupplier.get();
        }
    }

    protected Optional<VirtualReadingType> searchCompromise(List<ServerExpressionNode> nodes, List<VirtualReadingType> preferredReadingTypes) {
        List<VirtualReadingType> smallestToBiggest = new ArrayList<>(preferredReadingTypes);
        Collections.sort(smallestToBiggest, new VirtualReadingTypeRelativeComparator(this.requestedReadingType));
        return smallestToBiggest
                .stream()
                .map(this::newCheckInforceReadingType)
                .filter(checker -> checker.forAll(nodes))
                .map(CheckEnforceReadingType::getReadingType)
                .findFirst();
    }

    protected CheckEnforceReadingType newCheckInforceReadingType(VirtualReadingType readingType) {
        return new CheckEnforceReadingTypeImpl(readingType);
    }

    private Boolean checkForCompromiseOnRequestedIntervalAndMultiplier(List<ServerExpressionNode> nodes) {
        return new CheckEnforceReadingTypeImpl(this.requestedReadingType).forAll(nodes);
    }

    private String getFunctionName(FunctionCallNode functionCall) {
        return functionCall.getFunction().name();
    }

    private VirtualReadingType getPreferredReadingType(ServerExpressionNode expression) {
        return expression.accept(this);
    }

    /**
     * Enforces a VirtualReadingType onto all visited expressions
     * after it has been verified that this will work.
     *
     * @see CheckEnforceReadingTypeImpl
     */
    private final class EnforceReadingType implements ServerExpressionNode.Visitor<Void> {
        private final VirtualReadingType readingType;

        private EnforceReadingType(VirtualReadingType readingType) {
            this.readingType = readingType;
        }

        private void onto(ServerExpressionNode expression) {
            expression.accept(this);
        }

        private void enforceOntoAll(List<ServerExpressionNode> expressions) {
            expressions.stream().forEach(this::onto);
        }

        @Override
        public Void visitTimeBasedAggregation(TimeBasedAggregationNode aggregationNode) {
            throw new IllegalArgumentException("Inference of reading type is not supported for expert mode functions");
        }

        @Override
        public Void visitConstant(NumericalConstantNode constant) {
            return null;
        }

        @Override
        public Void visitConstant(StringConstantNode constant) {
            return null;
        }

        @Override
        public Void visitProperty(CustomPropertyNode property) {
            return null;
        }

        @Override
        public Void visitSyntheticLoadProfile(SyntheticLoadProfilePropertyNode slp) {
            return null;
        }

        @Override
        public Void visitNull(NullNode nullNode) {
            return null;
        }

        @Override
        public Void visitSqlFragment(SqlFragmentNode variable) {
            return null;
        }

        @Override
        public Void visitVirtualRequirement(VirtualRequirementNode requirement) {
            requirement.setTargetReadingType(this.readingType);
            return null;
        }

        @Override
        public Void visitVirtualDeliverable(VirtualDeliverableNode deliverable) {
            deliverable.setTargetReadingType(this.readingType);
            return null;
        }

        @Override
        public Void visitUnitConversion(UnitConversionNode unitConversionNode) {
            unitConversionNode.setTargetReadingType(this.readingType);
            return null;
        }

        @Override
        public Void visitOperation(OperationNode operationNode) {
            operationNode.getLeftOperand().accept(this);
            operationNode.getRightOperand().accept(this);
            return null;
        }

        @Override
        public Void visitFunctionCall(FunctionCallNode functionCall) {
            this.enforceOntoAll(functionCall.getArguments());
            return null;
        }
    }

    /**
     * Enforces an IntervalLength onto all visited expressions
     * after it has been verified that this will work.
     *
     * @see CheckEnforceReadingTypeImpl
     */
    private final class EnforceIntervalMultiplierAndCommodity implements ServerExpressionNode.Visitor<Void> {
        private final IntervalLength intervalLength;
        private final MetricMultiplier multiplier;
        private final Commodity commodity;

        private EnforceIntervalMultiplierAndCommodity(VirtualReadingType readingType) {
            this(readingType.getIntervalLength(), readingType.getUnitMultiplier(), readingType.getCommodity());
        }

        private EnforceIntervalMultiplierAndCommodity(IntervalLength intervalLength, MetricMultiplier multiplier, Commodity commodity) {
            this.intervalLength = intervalLength;
            this.multiplier = multiplier;
            this.commodity = commodity;
        }

        private void enforceOntoAll(List<ServerExpressionNode> expressions) {
            expressions.stream().forEach(expression -> expression.accept(this));
        }

        @Override
        public Void visitTimeBasedAggregation(TimeBasedAggregationNode aggregationNode) {
            throw new IllegalArgumentException("Inference of reading type is not supported for expert mode functions");
        }

        @Override
        public Void visitConstant(NumericalConstantNode constant) {
            return null;
        }

        @Override
        public Void visitConstant(StringConstantNode constant) {
            return null;
        }

        @Override
        public Void visitProperty(CustomPropertyNode property) {
            return null;
        }

        @Override
        public Void visitSyntheticLoadProfile(SyntheticLoadProfilePropertyNode slp) {
            return null;
        }

        @Override
        public Void visitNull(NullNode nullNode) {
            return null;
        }

        @Override
        public Void visitSqlFragment(SqlFragmentNode variable) {
            return null;
        }

        @Override
        public Void visitVirtualRequirement(VirtualRequirementNode requirement) {
            requirement.setTargetIntervalLength(this.intervalLength);
            requirement.setTargetMultiplier(this.multiplier);
            requirement.setTargetCommodity(this.commodity);
            return null;
        }

        @Override
        public Void visitVirtualDeliverable(VirtualDeliverableNode deliverable) {
            deliverable.setTargetIntervalLength(this.intervalLength);
            deliverable.setTargetMultiplier(this.multiplier);
            deliverable.setTargetCommodity(this.commodity);
            return null;
        }

        @Override
        public Void visitUnitConversion(UnitConversionNode unitConversionNode) {
            unitConversionNode.setTargetIntervalLength(this.intervalLength);
            unitConversionNode.setTargetMultiplier(this.multiplier);
            unitConversionNode.setTargetCommodity(this.commodity);
            return null;
        }

        @Override
        public Void visitOperation(OperationNode operationNode) {
            operationNode.getLeftOperand().accept(this);
            operationNode.getRightOperand().accept(this);
            return null;
        }

        @Override
        public Void visitFunctionCall(FunctionCallNode functionCall) {
            this.enforceOntoAll(functionCall.getArguments());
            return null;
        }
    }

}