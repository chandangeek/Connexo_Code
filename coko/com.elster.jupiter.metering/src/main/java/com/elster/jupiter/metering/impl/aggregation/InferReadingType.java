package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.metering.config.ReadingTypeDeliverable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
public class InferReadingType implements ServerExpressionNode.Visitor<VirtualReadingType> {

    private final VirtualReadingType requestedReadingType;

    public InferReadingType(VirtualReadingType requestedReadingType) {
        super();
        this.requestedReadingType = requestedReadingType;
    }

    @Override
    public VirtualReadingType visitVirtualRequirement(VirtualRequirementNode node) {
        return node.getPreferredReadingType();
    }

    @Override
    public VirtualReadingType visitVirtualDeliverable(VirtualDeliverableNode deliverable) {
        // Just another reference to a deliverable that we can aggregate to whatever level we need anyway
        return this.requestedReadingType;
    }

    @Override
    public VirtualReadingType visitConstant(NumericalConstantNode constant) {
        return this.requestedReadingType;
    }

    @Override
    public VirtualReadingType visitConstant(StringConstantNode constant) {
        return this.requestedReadingType;
    }

    @Override
    public VirtualReadingType visitOperation(OperationNode operationNode) {
        List<ServerExpressionNode> operands = Arrays.asList(operationNode.getLeftOperand(), operationNode.getRightOperand());
        return this.visitChildren(
                operands,
                () -> new UnsupportedOperationException(
                        "The 2 operands for " + operationNode.getOperator().name() + " cannot support the same interval"));
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
                        "Not all arguments of the function " + this.getFunctionName(functionCall) + " can support the same interval"));
    }

    private VirtualReadingType visitChildren(List<ServerExpressionNode> children, Supplier<UnsupportedOperationException> unsupportedOperationExceptionSupplier) {
        Set<VirtualReadingType> preferredIntervals = children.stream().map(this::getPreferredInterval).collect(Collectors.toSet());
        if (preferredIntervals.size() == 1) {
            // All child nodes are fine with the same interval, now enforce that interval
            VirtualReadingType preferredInterval = preferredIntervals.iterator().next();
            EnforceAggregationInterval enforce = new EnforceAggregationInterval(preferredInterval);
            children.stream().forEach(enforce::onto);
            return preferredInterval;
        }
        else {
            // Difference of opinions, try to compromise
            return this.searchCompromise(children, preferredIntervals, unsupportedOperationExceptionSupplier);
        }
    }

    /**
     * Searches for a compromise when multiple {@link VirtualReadingType}s were
     * returned by the express nodes. These are the steps involved:
     * <ol>
     * <li>check if every node can agree on the actual requested target interval and use that if that is the case</li>
     * <li>check all preferred intervals, starting with the smallest interval
     *     one and enforce the first one that every node can agrees on</li>
     * </ol>
     *
     * @param nodes The expression nodes
     * @param preferredReadingTypes The preferred VirtualReadingType of each of the nodes
     * @param unsupportedOperationExceptionSupplier The supplier of the UnsupportedOperationException that will be thrown when no compromise can be found
     * @return The compromising VirtualReadingType
     */
    private VirtualReadingType searchCompromise(List<ServerExpressionNode> nodes, Set<VirtualReadingType> preferredReadingTypes, Supplier<UnsupportedOperationException> unsupportedOperationExceptionSupplier) {
        if (this.checkForCompromiseOnRequestedInterval(nodes)) {
            new EnforceAggregationInterval(this.requestedReadingType).enforceOntoAll(nodes);
            return this.requestedReadingType;
        } else {
            List<VirtualReadingType> smallestToBiggest = new ArrayList<>(preferredReadingTypes);
            Collections.sort(smallestToBiggest);
            Optional<VirtualReadingType> compromise =
                    smallestToBiggest
                            .stream()
                            .map(CheckEnforceAggregationInterval::new)
                            .filter(checker -> checker.forAll(nodes))
                            .map(CheckEnforceAggregationInterval::getReadingType)
                            .findFirst();
            if (compromise.isPresent()) {
                new EnforceAggregationInterval(compromise.get()).enforceOntoAll(nodes);
                return compromise.get();
            } else {
                throw unsupportedOperationExceptionSupplier.get();
            }
        }
    }

    private Boolean checkForCompromiseOnRequestedInterval(List<ServerExpressionNode> nodes) {
        return new CheckEnforceAggregationInterval(this.requestedReadingType).forAll(nodes);
    }

    private String getFunctionName(FunctionCallNode functionCall) {
        return functionCall.getFunction().name();
    }

    private VirtualReadingType getPreferredInterval(ServerExpressionNode expression) {
        return expression.accept(this);
    }

    /**
     * Checks if enforcing a VirtualReadingType onto all expressions will work
     * and returns <code>true</code> if that is the case.
     * As an example, the VirtualReadingType 15min cannot be forced into a
     * {@link VirtualReadingTypeRequirement} if none of the backing channels
     * are capable of providing that interval.
     */
    private class CheckEnforceAggregationInterval implements ServerExpressionNode.Visitor<Boolean> {
        private final VirtualReadingType readingType;

        private CheckEnforceAggregationInterval(VirtualReadingType readingType) {
            super();
            this.readingType = readingType;
        }

        public VirtualReadingType getReadingType() {
            return readingType;
        }

        private Boolean forAll(List<ServerExpressionNode> expressions) {
            if (this.readingType.isUnsupported()) {
                return Boolean.FALSE;
            }
            else {
                return expressions.stream().allMatch(expression -> expression.accept(this));
            }
        }

        @Override
        public Boolean visitConstant(NumericalConstantNode constant) {
            return Boolean.TRUE;
        }

        @Override
        public Boolean visitConstant(StringConstantNode constant) {
            return Boolean.TRUE;
        }

        @Override
        public Boolean visitVirtualRequirement(VirtualRequirementNode requirement) {
            return requirement.supportsInterval(this.getReadingType());
        }

        @Override
        public Boolean visitVirtualDeliverable(VirtualDeliverableNode deliverable) {
            // This is just another reference to a deliverable that we can always aggregate to a different interval
            return true;
        }

        @Override
        public Boolean visitOperation(OperationNode operationNode) {
            return operationNode.getLeftOperand().accept(this) && operationNode.getRightOperand().accept(this);
        }

        @Override
        public Boolean visitFunctionCall(FunctionCallNode functionCall) {
            return this.forAll(functionCall.getArguments());
        }
    }

    /**
     * Enforces a VirtualReadingType onto all visited expressions
     * after it has been verified that this will work.
     *
     * @see CheckEnforceAggregationInterval
     */
    private class EnforceAggregationInterval implements  ServerExpressionNode.Visitor<Void> {
        private final VirtualReadingType interval;

        private EnforceAggregationInterval(VirtualReadingType interval) {
            this.interval = interval;
        }

        private void onto(ServerExpressionNode expression) {
            expression.accept(this);
        }

        private void enforceOntoAll(List<ServerExpressionNode> expressions) {
            expressions.stream().forEach(expression -> expression.accept(this));
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
        public Void visitVirtualRequirement(VirtualRequirementNode requirement) {
            requirement.setTargetReadingType(this.interval);
            return null;
        }

        @Override
        public Void visitVirtualDeliverable(VirtualDeliverableNode deliverable) {
            deliverable.setTargetReadingType(this.interval);
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