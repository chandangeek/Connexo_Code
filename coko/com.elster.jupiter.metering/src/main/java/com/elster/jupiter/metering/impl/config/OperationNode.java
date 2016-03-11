package com.elster.jupiter.metering.impl.config;

import java.util.Arrays;
import java.util.Optional;
import java.util.OptionalInt;

import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.metering.impl.aggregation.UnitConversionSupport;

/**
 * Created by igh on 4/02/2016.
 */
public class OperationNode extends AbstractNode {

    static final String TYPE_IDENTIFIER = "OPR";

    private Operator operator;

    public OperationNode() {}

    public OperationNode(Operator operator, ExpressionNode operand1, ExpressionNode operand2) {
        super(Arrays.asList(operand1, operand2));
        this.operator = operator;
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
                   left.getReadingTypeUnit(), right.getReadingTypeUnit())) {
                throw new InvalidNodeException("Only reading type units that are compatible for automatic unit conversion can be summed or substracted");
           }
        }
        if ((operator.equals(Operator.MULTIPLY)) &&
                (!UnitConversionSupport.isAllowedMultiplication(left.getReadingTypeUnit(), right.getReadingTypeUnit()))) {
                throw new InvalidNodeException("Invalid reading type arguments for multiplication");
        }
        if ((operator.equals(Operator.DIVIDE)) &&
                (!UnitConversionSupport.isAllowedDivision(left.getReadingTypeUnit(), right.getReadingTypeUnit()))) {
                throw new InvalidNodeException("Invalid reading type arguments for division");
        }

    }

    @Override
    public ReadingTypeUnit getReadingTypeUnit() {
        if (operator.equals(Operator.MINUS) || operator.equals(Operator.PLUS)) {
            return getLeftOperand().getReadingTypeUnit();
        } else if (operator.equals(Operator.MULTIPLY)) {
            Optional<ReadingTypeUnit> readingTypeUnit =
                    UnitConversionSupport.getMultiplicationUnit(getLeftOperand().getReadingTypeUnit(), getRightOperand().getReadingTypeUnit());
            if (!readingTypeUnit.isPresent()) {
                throw new InvalidNodeException("units from multiplication arguments do not result in a valid unit");
            }
            return readingTypeUnit.get();
        } else {
            Optional<ReadingTypeUnit> readingTypeUnit =
                    UnitConversionSupport.getDivisionUnit(getLeftOperand().getReadingTypeUnit(), getRightOperand().getReadingTypeUnit());
            if (!readingTypeUnit.isPresent()) {
                throw new InvalidNodeException("units from division arguments do not result in a valid unit");
            }
            return readingTypeUnit.get();
        }
    }
}