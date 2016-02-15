package com.elster.insight.usagepoint.config.impl.aggregation;

import com.elster.insight.usagepoint.config.ReadingTypeDeliverable;

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
public class InferAggregationInterval implements ServerExpressionNode.ServerVisitor<IntervalLength> {

    private final IntervalLength requestedInterval;

    public InferAggregationInterval(IntervalLength requestedInterval) {
        super();
        this.requestedInterval = requestedInterval;
    }

    @Override
    public IntervalLength visitRequirement(ReadingTypeRequirementNode requirement) {
        this.unexpectedRequirementNode();
        return null;    // Above will have thrown an exception so merely satisfying the compiler here
    }

    private void unexpectedRequirementNode() throws IllegalArgumentException {
        throw new IllegalArgumentException("Not expecting formulas to contain actual requirement nodes, invoke this component when those have been replaced with VirtualReadingTypeRequirement");
    }

    @Override
    public IntervalLength visitVirtualRequirement(VirtualRequirementNode node) {
        return node.getPreferredInterval();
    }

    @Override
    public IntervalLength visitDeliverable(ReadingTypeDeliverableNode deliverable) {
        this.unexpectedDeliverableNode();
        return null;    // Above will have thrown an exception so merely satisfying the compiler here
    }

    private IntervalLength unexpectedDeliverableNode() {
        throw new IllegalArgumentException("Not expecting formulas to contain actual deliverable nodes, invoke this component when those have been replaced with VirtualReadingTypeDeliverable");
    }

    @Override
    public IntervalLength visitVirtualDeliverable(VirtualDeliverableNode deliverable) {
        // Just another reference to a deliverable that we can aggregate to whatever level we need anyway
        return this.requestedInterval;
    }

    @Override
    public IntervalLength visitConstant(ConstantNode constant) {
        return this.requestedInterval;
    }

    @Override
    public IntervalLength visitOperation(OperationNode operatorNode) {
        List<AbstractNode> operands = Arrays.asList(operatorNode.getLeftOperand(), operatorNode.getRightOperand());
        return this.visitChildren(
                operands,
                () -> new UnsupportedOperationException(
                        "The 2 operands for " + operatorNode.getOperator().name() + " cannot support the same interval"));
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
                functionCall.getChildren(),
                () -> new UnsupportedOperationException(
                        "Not all arguments of the function " + this.getFunctionName(functionCall) + " can support the same interval"));
    }

    private IntervalLength visitChildren(List<AbstractNode> children, Supplier<UnsupportedOperationException> unsupportedOperationExceptionSupplier) {
        Set<IntervalLength> preferredIntervals = children.stream().map(this::getPreferredInterval).collect(Collectors.toSet());
        if (preferredIntervals.size() == 1) {
            // All child nodes are fine with the same interval, now enforce that interval
            IntervalLength preferredInterval = preferredIntervals.iterator().next();
            EnforceAggregationInterval enforce = new EnforceAggregationInterval(preferredInterval);
            children.stream().forEach(enforce::onto);
            return preferredInterval;
        }
        else {
            // Difference of opinions, try to compromise, start with the smallest interval
            List<IntervalLength> smallestToBiggest = new ArrayList<>(preferredIntervals);
            Collections.sort(smallestToBiggest);
            Optional<IntervalLength> compromise =
                    smallestToBiggest
                            .stream()
                            .map(CheckEnforceAggregationInterval::new)
                            .filter(checker -> checker.forAll(children))
                            .map(CheckEnforceAggregationInterval::getInterval)
                            .findFirst();
            if (compromise.isPresent()) {
                new EnforceAggregationInterval(compromise.get()).enforceOntoAll(children);
                return compromise.get();
            }
            else {
                throw unsupportedOperationExceptionSupplier.get();
            }
        }

    }
    private String getFunctionName(FunctionCallNode functionCall) {
        if (functionCall.getFunction() != null) {
            return functionCall.getFunction().name();
        }
        else {
            return functionCall.getName();
        }
    }

    private IntervalLength getPreferredInterval(AbstractNode expression) {
        return expression.accept(this);
    }

    /**
     * Checks if enforcing a IntervalLength onto all expressions will work
     * and returns <code>true</code> if that is the case.
     * As an example, the IntervalLength 15min cannot be forced into a
     * {@link VirtualReadingTypeRequirement} if none of the backing channels
     * are capable of providing that interval.
     */
    private class CheckEnforceAggregationInterval implements ServerExpressionNode.ServerVisitor<Boolean> {
        private final IntervalLength interval;

        private CheckEnforceAggregationInterval(IntervalLength interval) {
            super();
            this.interval = interval;
        }

        public IntervalLength getInterval() {
            return interval;
        }

        private Boolean forAll(List<AbstractNode> expressions) {
            if (IntervalLength.NOT_SUPPORTED.equals(this.interval)) {
                return Boolean.FALSE;
            }
            else {
                return expressions.stream().allMatch(expression -> expression.accept(this));
            }
        }

        @Override
        public Boolean visitConstant(ConstantNode constant) {
            return Boolean.TRUE;
        }

        @Override
        public Boolean visitRequirement(ReadingTypeRequirementNode requirement) {
            unexpectedRequirementNode();
            return Boolean.FALSE;    // Above will have thrown an exception so merely satisfying the compiler here
        }

        @Override
        public Boolean visitVirtualRequirement(VirtualRequirementNode requirement) {
            return requirement.supportsInterval(this.getInterval());
        }

        @Override
        public Boolean visitDeliverable(ReadingTypeDeliverableNode deliverable) {
            unexpectedRequirementNode();
            return Boolean.FALSE;    // Above will have thrown an exception so merely satisfying the compiler here
        }

        @Override
        public Boolean visitVirtualDeliverable(VirtualDeliverableNode deliverable) {
            // This is just another reference to a deliverable that we can always aggregate to a different interval
            return true;
        }

        @Override
        public Boolean visitOperation(OperationNode operatorNode) {
            return operatorNode.getLeftOperand().accept(this) && operatorNode.getRightOperand().accept(this);
        }

        @Override
        public Boolean visitFunctionCall(FunctionCallNode functionCall) {
            return this.forAll(functionCall.getChildren());
        }
    }

    /**
     * Enforces a IntervalLength onto all visited expressions
     * after it has been verified that this will work.
     *
     * @see CheckEnforceAggregationInterval
     */
    private class EnforceAggregationInterval implements ServerExpressionNode.ServerVisitor<Void> {
        private final IntervalLength interval;

        private EnforceAggregationInterval(IntervalLength interval) {
            this.interval = interval;
        }

        private void onto(AbstractNode expression) {
            expression.accept(this);
        }

        private void enforceOntoAll(List<AbstractNode> expressions) {
            expressions.stream().forEach(expression -> expression.accept(this));
        }

        @Override
        public Void visitConstant(ConstantNode constant) {
            return null;
        }

        @Override
        public Void visitRequirement(ReadingTypeRequirementNode requirement) {
            unexpectedRequirementNode();
            return null;    // Above will have thrown an exception so merely satisfying the compiler here
        }

        @Override
        public Void visitVirtualRequirement(VirtualRequirementNode requirement) {
            requirement.setTargetInterval(this.interval);
            return null;
        }

        @Override
        public Void visitDeliverable(ReadingTypeDeliverableNode deliverable) {
            unexpectedRequirementNode();
            return null;    // Above will have thrown an exception so merely satisfying the compiler here
        }

        @Override
        public Void visitVirtualDeliverable(VirtualDeliverableNode deliverable) {
            deliverable.setTargetInterval(this.interval);
            return null;
        }

        @Override
        public Void visitOperation(OperationNode operatorNode) {
            operatorNode.getLeftOperand().accept(this);
            operatorNode.getRightOperand().accept(this);
            return null;
        }

        @Override
        public Void visitFunctionCall(FunctionCallNode functionCall) {
            this.enforceOntoAll(functionCall.getChildren());
            return null;
        }
    }
}