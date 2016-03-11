package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.util.streams.Predicates;

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
        return VirtualReadingType.dontCare();
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
    public VirtualReadingType visitVariable(VariableReferenceNode variable) {
        return VirtualReadingType.dontCare();
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
        Set<VirtualReadingType> preferredReadingTypes =
                children
                    .stream()
                    .map(this::getPreferredReadingType)
                    .filter(Predicates.not(VirtualReadingType::isDontCare))
                    .collect(Collectors.toSet());
        if (preferredReadingTypes.isEmpty()) {
            /* All child nodes have indicated not to care about the reading type
             * so we should be able to enforce the target onto each. */
            return this.enforceReadingType(children, this.requestedReadingType);
        } else {
            if (preferredReadingTypes.stream().anyMatch(VirtualReadingType::isUnsupported)) {
                throw unsupportedOperationExceptionSupplier.get();
            }
            if (preferredReadingTypes.size() == 1) {
                // All child nodes are fine with the same reading type, simply enforce that one
                VirtualReadingType preferredReadingType = preferredReadingTypes.iterator().next();
                return this.enforceReadingType(children, preferredReadingType);
            } else {
                // Difference of opinions, try to compromise
                return this.searchCompromise(children, preferredReadingTypes, unsupportedOperationExceptionSupplier);
            }
        }
    }

    private VirtualReadingType enforceReadingType(List<ServerExpressionNode> nodes,  VirtualReadingType readingType) {
        EnforceReadingType enforce = new EnforceReadingType(readingType);
        nodes.stream().forEach(enforce::onto);
        return readingType;
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
        List<VirtualReadingType> smallestToBiggest = new ArrayList<>(preferredReadingTypes);
        Collections.sort(smallestToBiggest, new VirtualReadingTypeRelativeComparator(this.requestedReadingType));
        Optional<VirtualReadingType> compromise =
                smallestToBiggest
                        .stream()
                        .map(CheckEnforceReadingType::new)
                        .filter(checker -> checker.forAll(nodes))
                        .map(CheckEnforceReadingType::getReadingType)
                        .findFirst();
        if (compromise.isPresent()) {
            new EnforceReadingType(compromise.get()).enforceOntoAll(nodes);
            return compromise.get();
        } else if (this.checkForCompromiseOnRequestedInterval(nodes)) {
            new EnforceInterval(this.requestedReadingType.getIntervalLength()).enforceOntoAll(nodes);
            return this.requestedReadingType;
        } else {
            throw unsupportedOperationExceptionSupplier.get();
        }
    }

    private Boolean checkForCompromiseOnRequestedInterval(List<ServerExpressionNode> nodes) {
        return new CheckEnforceReadingType(this.requestedReadingType).forAll(nodes);
    }

    private String getFunctionName(FunctionCallNode functionCall) {
        return functionCall.getFunction().name();
    }

    private VirtualReadingType getPreferredReadingType(ServerExpressionNode expression) {
        return expression.accept(this);
    }

    /**
     * Checks if enforcing a VirtualReadingType onto all expressions will work
     * and returns <code>true</code> if that is the case.
     * As an example, the VirtualReadingType 15min cannot be forced into a
     * {@link VirtualReadingTypeRequirement} if none of the backing channels
     * are capable of providing that interval.
     */
    private class CheckEnforceReadingType implements ServerExpressionNode.Visitor<Boolean> {
        private final VirtualReadingType readingType;

        private CheckEnforceReadingType(VirtualReadingType readingType) {
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
        public Boolean visitVariable(VariableReferenceNode variable) {
            return Boolean.TRUE;
        }

        @Override
        public Boolean visitVirtualRequirement(VirtualRequirementNode requirement) {
            return requirement.supports(this.getReadingType());
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
     * @see CheckEnforceReadingType
     */
    private class EnforceReadingType implements ServerExpressionNode.Visitor<Void> {
        private final VirtualReadingType readingType;

        private EnforceReadingType(VirtualReadingType readingType) {
            this.readingType = readingType;
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
        public Void visitVariable(VariableReferenceNode variable) {
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
     * @see CheckEnforceReadingType
     */
    private class EnforceInterval implements ServerExpressionNode.Visitor<Void> {
        private final IntervalLength intervalLength;

        private EnforceInterval(IntervalLength intervalLength) {
            this.intervalLength = intervalLength;
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
        public Void visitVariable(VariableReferenceNode variable) {
            return null;
        }

        @Override
        public Void visitVirtualRequirement(VirtualRequirementNode requirement) {
            requirement.setTargetInterval(this.intervalLength);
            return null;
        }

        @Override
        public Void visitVirtualDeliverable(VirtualDeliverableNode deliverable) {
            deliverable.setTargetIntervalLength(this.intervalLength);
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