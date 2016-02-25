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
 * Infers the most appropriate aggregation interval for the
 * expressions in the tree that define the way a {@link ReadingTypeDeliverable}
 * should be calculated.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-08 (12:13)
 */
public class InferAggregationInterval implements  ServerExpressionNode.Visitor<IntervalLength> {

    private final IntervalLength requestedInterval;

    public InferAggregationInterval(IntervalLength requestedInterval) {
        super();
        this.requestedInterval = requestedInterval;
    }

    @Override
    public IntervalLength visitVirtualRequirement(VirtualRequirementNode node) {
        return node.getPreferredInterval();
    }

    @Override
    public IntervalLength visitVirtualDeliverable(VirtualDeliverableNode deliverable) {
        // Just another reference to a deliverable that we can aggregate to whatever level we need anyway
        return this.requestedInterval;
    }

    @Override
    public IntervalLength visitConstant(NumericalConstantNode constant) {
        return this.requestedInterval;
    }

    @Override
    public IntervalLength visitConstant(StringConstantNode constant) {
        return this.requestedInterval;
    }

    @Override
    public IntervalLength visitOperation(OperationNode operationNode) {
        List<ServerExpressionNode> operands = Arrays.asList(operationNode.getLeftOperand(), operationNode.getRightOperand());
        return this.visitChildren(
                operands,
                () -> new UnsupportedOperationException(
                        "The 2 operands for " + operationNode.getOperator().name() + " cannot support the same interval"));
    }

    @Override
    public IntervalLength visitFunctionCall(FunctionCallNode functionCall) {
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

    private IntervalLength visitChildren(List<ServerExpressionNode> children, Supplier<UnsupportedOperationException> unsupportedOperationExceptionSupplier) {
        Set<IntervalLength> preferredIntervals = children.stream().map(this::getPreferredInterval).collect(Collectors.toSet());
        if (preferredIntervals.size() == 1) {
            // All child nodes are fine with the same interval, now enforce that interval
            IntervalLength preferredInterval = preferredIntervals.iterator().next();
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
     * Searches for a compromise when multiple {@link IntervalLength}s were
     * returned by the express nodes. These are the steps involved:
     * <ol>
     * <li>check if every node can agree on the actual requested target interval and use that if that is the case</li>
     * <li>check all preferred intervals, starting with the smallest interval
     *     one and enforce the first one that every node can agrees on</li>
     * </ol>
     *
     * @param nodes The expression nodes
     * @param preferredIntervals The preferred IntervalLength of each of the nodes
     * @param unsupportedOperationExceptionSupplier The supplier of the UnsupportedOperationException that will be thrown when no compromise can be found
     * @return The compromising IntervalLength
     */
    private IntervalLength searchCompromise(List<ServerExpressionNode> nodes, Set<IntervalLength> preferredIntervals, Supplier<UnsupportedOperationException> unsupportedOperationExceptionSupplier) {
        if (this.checkForCompromiseOnRequestedInterval(nodes)) {
            new EnforceAggregationInterval(this.requestedInterval).enforceOntoAll(nodes);
            return this.requestedInterval;
        } else {
            List<IntervalLength> smallestToBiggest = new ArrayList<>(preferredIntervals);
            Collections.sort(smallestToBiggest);
            Optional<IntervalLength> compromise =
                    smallestToBiggest
                            .stream()
                            .map(CheckEnforceAggregationInterval::new)
                            .filter(checker -> checker.forAll(nodes))
                            .map(CheckEnforceAggregationInterval::getInterval)
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
        return new CheckEnforceAggregationInterval(this.requestedInterval).forAll(nodes);
    }

    private String getFunctionName(FunctionCallNode functionCall) {
        return functionCall.getFunction().name();
    }

    private IntervalLength getPreferredInterval(ServerExpressionNode expression) {
        return expression.accept(this);
    }

    /**
     * Checks if enforcing a IntervalLength onto all expressions will work
     * and returns <code>true</code> if that is the case.
     * As an example, the IntervalLength 15min cannot be forced into a
     * {@link VirtualReadingTypeRequirement} if none of the backing channels
     * are capable of providing that interval.
     */
    private class CheckEnforceAggregationInterval implements ServerExpressionNode.Visitor<Boolean> {
        private final IntervalLength interval;

        private CheckEnforceAggregationInterval(IntervalLength interval) {
            super();
            this.interval = interval;
        }

        public IntervalLength getInterval() {
            return interval;
        }

        private Boolean forAll(List<ServerExpressionNode> expressions) {
            if (IntervalLength.NOT_SUPPORTED.equals(this.interval)) {
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
            return requirement.supportsInterval(this.getInterval());
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
     * Enforces a IntervalLength onto all visited expressions
     * after it has been verified that this will work.
     *
     * @see CheckEnforceAggregationInterval
     */
    private class EnforceAggregationInterval implements  ServerExpressionNode.Visitor<Void> {
        private final IntervalLength interval;

        private EnforceAggregationInterval(IntervalLength interval) {
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
            requirement.setTargetInterval(this.interval);
            return null;
        }

        @Override
        public Void visitVirtualDeliverable(VirtualDeliverableNode deliverable) {
            deliverable.setTargetInterval(this.interval);
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