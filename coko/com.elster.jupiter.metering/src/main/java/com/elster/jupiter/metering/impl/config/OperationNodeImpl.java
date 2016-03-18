package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.config.ExpressionNode;
import com.elster.jupiter.metering.config.OperationNode;
import com.elster.jupiter.metering.config.Operator;
import com.elster.jupiter.metering.impl.aggregation.TemporaryDimension;
import com.elster.jupiter.metering.impl.aggregation.UnitConversionSupport;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.units.Dimension;

import java.util.Arrays;
import java.util.Optional;

/**
 * Created by igh on 4/02/2016.
 */
public class OperationNodeImpl extends AbstractNode implements OperationNode {

    static final String TYPE_IDENTIFIER = "OPR";

    private Operator operator;
    private Thesaurus thesaurus;

    public OperationNodeImpl() {}

    public OperationNodeImpl(Operator operator, ExpressionNode operand1, ExpressionNode operand2, Thesaurus thesaurus) {
        super(Arrays.asList(operand1, operand2));
        this.operator = operator;
        this.thesaurus = thesaurus;
    }

    @Override
    public Operator getOperator() {
        return operator;
    }

    @Override
    public ExpressionNode getLeftOperand() {
        return this.getChildren().get(0);
    }

    @Override
    public ExpressionNode getRightOperand() {
        return this.getChildren().get(1);
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitOperation(this);
    }

    public String toString() {
        return operator.toString() + "(" + getLeftOperand().toString() + ", " + getRightOperand().toString() + ")";
    }

    public void validate() {
        if (this.getParent() == null) {
            AbstractNode left = (AbstractNode) getLeftOperand();
            AbstractNode right = (AbstractNode) getRightOperand();
            if (operator.equals(Operator.MINUS) || operator.equals(Operator.PLUS)) {
                if (!UnitConversionSupport.areCompatibleForAutomaticUnitConversion(
                        left.getDimension(), right.getDimension())) {
                    throw new InvalidNodeException(thesaurus, MessageSeeds.INVALID_ARGUMENTS_FOR_SUM_OR_SUBSTRACTION);
                }
            }
            if ((operator.equals(Operator.MULTIPLY)) &&
                    (!UnitConversionSupport.isAllowedMultiplication(left.getTemporaryDimension(), right.getTemporaryDimension()))) {
                throw new InvalidNodeException(thesaurus, MessageSeeds.INVALID_ARGUMENTS_FOR_MULTIPLICATION);
            }
            if ((operator.equals(Operator.DIVIDE)) &&
                    (!UnitConversionSupport.isAllowedDivision(left.getTemporaryDimension(), right.getTemporaryDimension()))) {
                throw new InvalidNodeException(thesaurus, MessageSeeds.INVALID_ARGUMENTS_FOR_DIVISION);
            }
        }

    }

    @Override
     public Dimension getDimension() {
        if (operator.equals(Operator.MINUS) || operator.equals(Operator.PLUS)) {
            return getLeftOperand().getDimension();
        } else if (operator.equals(Operator.MULTIPLY)) {
            Optional<Dimension> dimension =
                    UnitConversionSupport.getMultiplicationDimension(getLeftOperand().getDimension(), getRightOperand().getDimension());
            if (!dimension.isPresent()) {
                throw new InvalidNodeException(thesaurus, MessageSeeds.INVALID_ARGUMENTS_FOR_MULTIPLICATION);
            }
            return dimension.get();
        } else {
            Optional<Dimension> dimension =
                    UnitConversionSupport.getDivisionDimension(getLeftOperand().getDimension(), getRightOperand().getDimension());
            if (!dimension.isPresent()) {
                throw new InvalidNodeException(thesaurus, MessageSeeds.INVALID_ARGUMENTS_FOR_DIVISION);
            }
            return dimension.get();
        }
    }

    @Override
    public TemporaryDimension getTemporaryDimension() {
        if (operator.equals(Operator.MINUS) || operator.equals(Operator.PLUS)) {
            return ((AbstractNode) getLeftOperand()).getTemporaryDimension();
        } else if (operator.equals(Operator.MULTIPLY)) {
            return
                    UnitConversionSupport.multiply(
                            ((AbstractNode) getLeftOperand()).getTemporaryDimension(),
                            ((AbstractNode) getRightOperand()).getTemporaryDimension());
        } else {
            return UnitConversionSupport.divide(
                    ((AbstractNode) getLeftOperand()).getTemporaryDimension(),
                    ((AbstractNode) getRightOperand()).getTemporaryDimension());
        }
    }
}