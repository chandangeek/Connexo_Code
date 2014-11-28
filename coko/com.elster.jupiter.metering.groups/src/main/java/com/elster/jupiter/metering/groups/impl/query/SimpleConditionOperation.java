package com.elster.jupiter.metering.groups.impl.query;

import com.elster.jupiter.util.conditions.Comparison;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Operator;
import com.google.common.collect.ImmutableSet;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Set;

public class SimpleConditionOperation extends ConditionOperation {

    private static final Set<Class<?>> ALLOWED_VALUE_TYPES = ImmutableSet.<Class<?>>of(Integer.class, Long.class, Byte.class, Float.class, Character.class, Double.class, Short.class, Boolean.class, String.class);
    public static final String TYPE_IDENTIFIER = "SCD";

    private String fieldName;
    private String operator = Operator.EQUAL.getSymbol();
    private Object[] values;

    @Inject
	SimpleConditionOperation() {
        // for persistence
    }

    public SimpleConditionOperation(Comparison comparison) {
        operator = comparison.getOperator().getSymbol();
        fieldName = comparison.getFieldName();
        if ((Operator.EQUAL != comparison.getOperator()) && (Operator.LIKE != comparison.getOperator())) {
            throw new UnsupportedOperationException("Only EQUAL and LIKE operator are supported.");
        }
        values = Arrays.copyOf(comparison.getValues(), comparison.getValues().length);
        for (Object value : values) {
            if (!ALLOWED_VALUE_TYPES.contains(value.getClass())) {
                throw new UnsupportedOperationException("Only primitive types and String values are supported.");
            }
        }
    }

    @Override
    public void visit(OperationVisitor visitor) {
        visitor.visitSimpleCondition(this);
    }

    @Override
    public Condition toCondition(Condition... conditions) {
        if (operator.equals(Operator.EQUAL.getSymbol())) {
            return Operator.EQUAL.compare(fieldName, values);
        } else if (operator.equals(Operator.LIKE.getSymbol())) {
            return Operator.LIKE.compare(fieldName, values);
        } else {
            throw new UnsupportedOperationException("Only EQUAL and LIKE operator are supported.");
        }
    }

    public String getFieldName() {
        return fieldName;
    }

    public Object[] getValues() {
        return values;
    }
}
