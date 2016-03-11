package com.elster.jupiter.metering.impl.config;

import java.util.Arrays;
import java.util.Optional;
import java.util.OptionalInt;

import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.impl.aggregation.UnitConversionSupport;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.units.Dimension;

/**
 * Created by igh on 4/02/2016.
 */
public class OperationNode extends AbstractNode {

    static final String TYPE_IDENTIFIER = "OPR";

    private Operator operator;
    private Thesaurus thesaurus;

    public OperationNode() {}

    public OperationNode(Operator operator, ExpressionNode operand1, ExpressionNode operand2, Thesaurus thesaurus) {
        super(Arrays.asList(operand1, operand2));
        this.operator = operator;
        this.thesaurus = thesaurus;
    }

    public Operator getOperator() {
        return operator;
    }

    public ExpressionNode getLeftOperand() {
        return this.getChildren().get(0);
    }

    public ExpressionNode getRightOperand() {
        return this.getChildren().get(1);
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitOperation(this);
    }

    public String toString() {
        StringBuilder result = new StringBuilder(operator.toString() + "(");
        result.append(getLeftOperand().toString()).append(", ");
        result.append(getRightOperand().toString());
        result.append(")");
        return result.toString();
    }

    public void validate() {
        ExpressionNode left = getLeftOperand();
        ExpressionNode right = getRightOperand();
        left.validate();
        right.validate();
        if (operator.equals(Operator.MINUS) || operator.equals(Operator.PLUS)) {
           if (!UnitConversionSupport.areCompatibleForAutomaticUnitConversion(
                   left.getDimension(), right.getDimension())) {
               throw new InvalidNodeException(thesaurus, MessageSeeds.INVALID_ARGUMENTS_FOR_SUM_OR_SUBSTRACTION);
           }
        }
        if ((operator.equals(Operator.MULTIPLY)) &&
                (!UnitConversionSupport.isAllowedMultiplication(left.getDimension(), right.getDimension()))) {
            throw new InvalidNodeException(thesaurus, MessageSeeds.INVALID_ARGUMENTS_FOR_MULTIPLICATION);
        }
        if ((operator.equals(Operator.DIVIDE)) &&
                (!UnitConversionSupport.isAllowedDivision(left.getDimension(), right.getDimension()))) {
            throw new InvalidNodeException(thesaurus, MessageSeeds.INVALID_ARGUMENTS_FOR_DIVISION);
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
}