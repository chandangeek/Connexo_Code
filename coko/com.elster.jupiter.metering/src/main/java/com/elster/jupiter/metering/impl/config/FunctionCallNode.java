package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.impl.aggregation.UnitConversionSupport;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.units.Dimension;

import java.util.List;

/**
 * Created by igh on 4/02/2016.
 */
public class FunctionCallNode extends AbstractNode {

    static final String TYPE_IDENTIFIER = "FCT";

    private Function function;
    private Thesaurus thesaurus;

    public FunctionCallNode() {}

    public FunctionCallNode init(Function function) {
        this.function = function;
        return this;
    }

    public FunctionCallNode(List<? extends ExpressionNode> children, Function function, Thesaurus thesaurus) {
        super(children);
        this.function = function;
        this.thesaurus = thesaurus;
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
            throw new InvalidNodeException(thesaurus, MessageSeeds.INVALID_ARGUMENTS_AT_LEAST_ONE_CHILD_REQUIRED);
        }
        ExpressionNode first = this.getChildren().get(0);
        first.validate();
        for (int i = 1; i < this.getChildren().size(); i++) {
            ExpressionNode child = this.getChildren().get(i);
            child.validate();
            if (!UnitConversionSupport.areCompatibleForAutomaticUnitConversion(
                    first.getDimension(), child.getDimension())) {
                throw new InvalidNodeException(thesaurus, MessageSeeds.INVALID_ARGUMENTS_FOR_FUNCTION_CALL);
            }
        }
    }

    @Override
    public Dimension getDimension() {
        return this.getChildren().get(0).getDimension();
    }
}