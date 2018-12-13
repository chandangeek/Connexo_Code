/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.config.ConstantNode;
import com.elster.jupiter.metering.config.ExpressionNode;
import com.elster.jupiter.metering.config.NullNode;
import com.elster.jupiter.metering.config.OperationNode;
import com.elster.jupiter.metering.config.Operator;
import com.elster.jupiter.metering.impl.PrivateMessageSeeds;
import com.elster.jupiter.metering.impl.aggregation.IntermediateDimension;
import com.elster.jupiter.metering.impl.aggregation.UnitConversionSupport;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.units.Dimension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Created by igh on 4/02/2016.
 */
public class OperationNodeImpl extends AbstractNode implements OperationNode {

    static final String TYPE_IDENTIFIER = "OPR";

    private Operator operator;
    private Thesaurus thesaurus;

    // For ORM layer
    @SuppressWarnings("unused")
    public OperationNodeImpl() {}

    public OperationNodeImpl(Operator operator, ServerExpressionNode operand1, ServerExpressionNode operand2, Thesaurus thesaurus) {
        this(operator, Arrays.asList(operand1, operand2), thesaurus);
    }

    public OperationNodeImpl(Operator operator, ServerExpressionNode operand1, ServerExpressionNode operand2, ServerExpressionNode zeroReplacementNode, Thesaurus thesaurus) {
        this(operator, Arrays.asList(operand1, operand2, zeroReplacementNode), thesaurus);
        this.operator = operator;
        this.thesaurus = thesaurus;
    }

    private OperationNodeImpl(Operator operator, List<ServerExpressionNode> children, Thesaurus thesaurus) {
        super(children);
        this.operator = operator;
        this.thesaurus = thesaurus;
    }

    @Override
    public Operator getOperator() {
        return operator;
    }

    @Override
    public ServerExpressionNode getLeftOperand() {
        return this.getServerSideChildren().get(0);
    }

    @Override
    public ServerExpressionNode getRightOperand() {
        return this.getServerSideChildren().get(1);
    }

    @Override
    public Optional<ExpressionNode> getZeroReplacement() {
        if (this.getOperator().equals(Operator.SAFE_DIVIDE) && this.getChildren().size() > 2) {
            return Optional.of(this.getChildren().get(2));
        } else {
            return Optional.empty();
        }
    }
    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitOperation(this);
    }

    @Override
    public String toString() {
        if (this.getChildren().size() == 3 && this.getOperator().equals(Operator.SAFE_DIVIDE)) {
            return operator.toString() + "(" + getLeftOperand().toString() + ", " + getRightOperand().toString() + ", " + this.getZeroReplacement().get().toString() + ")";
        }
        return operator.toString() + "(" + getLeftOperand().toString() + ", " + getRightOperand().toString() + ")";
    }

    public void validate() {
        if (this.getParent() == null) {
            ServerExpressionNode left = getLeftOperand();
            ServerExpressionNode right = getRightOperand();
            if (operator.equals(Operator.MINUS) || operator.equals(Operator.PLUS)) {
                if (!UnitConversionSupport.areCompatibleForAutomaticUnitConversion(
                        left.getDimension(), right.getDimension())) {
                    throw new InvalidNodeException(thesaurus, PrivateMessageSeeds.INVALID_ARGUMENTS_FOR_SUM_OR_SUBSTRACTION);
                }
            }
            if ((operator.equals(Operator.MULTIPLY)) &&
                    (!UnitConversionSupport.isAllowedMultiplication(left.getIntermediateDimension(), right.getIntermediateDimension()))) {
                throw new InvalidNodeException(thesaurus, PrivateMessageSeeds.INVALID_ARGUMENTS_FOR_MULTIPLICATION);
            }
            if ((operator.equals(Operator.DIVIDE)) &&
                    (!UnitConversionSupport.isAllowedDivision(left.getIntermediateDimension(), right.getIntermediateDimension()))) {
                throw new InvalidNodeException(thesaurus, PrivateMessageSeeds.INVALID_ARGUMENTS_FOR_DIVISION);
            }
            if (this.operator.equals(Operator.SAFE_DIVIDE)) {
                if (this.getChildren().size() != 3) {
                    throw new InvalidNodeException(thesaurus, PrivateMessageSeeds.INVALID_NUMBER_OF_ARGUMENTS_FOR_SAFE_DIVISION);
                }
                else {
                    ExpressionNode constantReplacingZero = this.getChildren().get(2);
                    if ((!this.isConstantNode(constantReplacingZero)) && (!this.isNullNode(constantReplacingZero))) {
                        throw new InvalidNodeException(thesaurus, PrivateMessageSeeds.SAFE_DIVISION_REQUIRES_NUMERICAL_CONSTANT);
                    } else if (this.isConstantNode(constantReplacingZero)) {
                        ConstantNode constant = (ConstantNode) constantReplacingZero;
                        if (constant.getValue().equals(BigDecimal.ZERO)) {
                            throw new InvalidNodeException(thesaurus, PrivateMessageSeeds.SAFE_DIVISION_REQUIRES_NON_ZERO_NUMERICAL_CONSTANT);
                        }
                    }
                }
            }
        }
    }

    private boolean isConstantNode(ExpressionNode node) {
        return node instanceof ConstantNode;
    }

    private boolean isNullNode(ExpressionNode node) {
        return node instanceof NullNode;
    }

    @Override
     public Dimension getDimension() {
        IntermediateDimension intermediateDimension = getIntermediateDimension();
        if (!intermediateDimension.exists()) {
            throw new InvalidNodeException(thesaurus, PrivateMessageSeeds.INVALID_ARGUMENTS_FOR_DIVISION);
        }
        return intermediateDimension.getDimension().get();
    }

    @Override
    public IntermediateDimension getIntermediateDimension() {
        switch (operator) {
            case MINUS: // Intentional fall-through
            case PLUS: {
                IntermediateDimension leftDimension = getLeftOperand().getIntermediateDimension();
                if (leftDimension.isDimensionless()) {
                    return this.getRightOperand().getIntermediateDimension();
                } else {
                    return leftDimension;
                }
            }
            case MULTIPLY: {
                return
                    UnitConversionSupport.multiply(
                            getLeftOperand().getIntermediateDimension(),
                            getRightOperand().getIntermediateDimension());
            }
            case SAFE_DIVIDE:   // Intentional fall-througg
            case DIVIDE : {
                return
                    UnitConversionSupport.divide(
                            getLeftOperand().getIntermediateDimension(),
                            getRightOperand().getIntermediateDimension());
            }
            default: {
                throw new IllegalArgumentException("Unknown or unsupported operation for getIntermediateDimension: " + operator);
            }
        }
    }

}