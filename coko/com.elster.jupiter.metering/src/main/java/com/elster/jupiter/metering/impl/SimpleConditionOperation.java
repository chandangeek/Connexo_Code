package com.elster.jupiter.metering.impl;

import com.elster.jupiter.util.conditions.Comparison;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Operator;
import com.google.common.collect.ImmutableSet;

import java.util.Arrays;
import java.util.Set;

public class SimpleConditionOperation extends ConditionOperation {

    private static final Set<Class<?>> ALLOWED_VALUE_TYPES = ImmutableSet.<Class<?>>of(Integer.class, Long.class, Byte.class, Float.class, Character.class, Double.class, Short.class, Boolean.class, String.class);
    static final String TYPE_IDENTIFIER = "SCD";

    private final String fieldName;
    private final Object[] values;

    public SimpleConditionOperation(Comparison comparison) {
        fieldName = comparison.getFieldName();
        if (Operator.EQUAL != comparison.getOperator()) {
            throw new UnsupportedOperationException("Only EQUAL operator is supported.");
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
        return Operator.EQUAL.compare(fieldName, values);
    }
}
