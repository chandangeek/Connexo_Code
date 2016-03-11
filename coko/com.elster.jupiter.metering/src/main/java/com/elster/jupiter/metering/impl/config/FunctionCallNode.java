package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.metering.impl.aggregation.UnitConversionSupport;

import java.util.List;

/**
 * Created by igh on 4/02/2016.
 */
public class FunctionCallNode extends AbstractNode {

    static final String TYPE_IDENTIFIER = "FCT";

    private Function function;

    public FunctionCallNode() {}

    public FunctionCallNode init(Function function) {
        this.function = function;
        return this;
    }

    public FunctionCallNode(List<? extends ExpressionNode> children, Function function) {
        super(children);
        this.function = function;
    }

    public Function getFunction() {
        return function;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitFunctionCall(this);
    }

    public String toString() {
        StringBuilder result = new StringBuilder(function.toString() + "(");
        List<ExpressionNode> children = this.getChildren();
        int size = children.size();
        for (int i = 0; i < size; i++) {
            result.append(children.get(i).toString());
            if (i != (size - 1)) {
                result.append(", ");
            }
        }
        result.append(")");
        return result.toString();
    }

    public void validate() {
        if (this.getChildren().isEmpty()) {
            throw new InvalidNodeException("At least 1 child required");
        }
        ExpressionNode first = this.getChildren().get(0);
        first.validate();
        for (int i = 1; i < this.getChildren().size(); i++) {
            ExpressionNode child = this.getChildren().get(i);
            child.validate();
            if (!UnitConversionSupport.areCompatibleForAutomaticUnitConversion(
                    first.getReadingTypeUnit(), child.getReadingTypeUnit())) {
                throw new InvalidNodeException("Only reading type units that are compatible for automatic unit conversion can be used as children of a function");
            }
        }
    }

    @Override
    public ReadingTypeUnit getReadingTypeUnit() {
        return this.getChildren().get(0).getReadingTypeUnit();
    }
}