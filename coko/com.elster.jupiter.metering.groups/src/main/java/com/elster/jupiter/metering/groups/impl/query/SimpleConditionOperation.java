package com.elster.jupiter.metering.groups.impl.query;

import com.elster.jupiter.util.conditions.Comparison;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Operator;
import com.google.common.collect.ImmutableSet;

import java.util.stream.Collectors;
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
        if (operator.equals(Operator.EQUAL.getSymbol())) {
            return values;
        } if (operator.equals(Operator.LIKE.getSymbol())) {
            return Arrays.asList(values).stream().map(v->fromOracleSql((String)v)).collect(Collectors.toList()).toArray();
        } else {
            throw new UnsupportedOperationException("Only EQUAL and LIKE operator are supported.");
        }
    }

    /**
     * Translate the LIKE string value into our supported wildcard regex. This method inverses Where.toOracleSql()
     */
    private String fromOracleSql(String value) {
        // escape sql our wildcards
        for (String keyword: Arrays.asList("*", "?")) {
            value=value.replace(keyword,"\\"+keyword);
        }
        // transform un-escaped LIKE operators % and _ to our wildcards
        value=value.replaceAll("([^\\\\]|^)\\%", "$1*");
        value=value.replaceAll("([^\\\\]|^)\\_", "$1?");

        // transform escaped LIKE operators % and _ to their unescaped wildcard literal
        // We need to search for double escape: it was doubled in the little loop on top of this method
        value=value.replaceAll("\\\\\\\\\\%", "%");
        value=value.replaceAll("\\\\\\\\\\_", "_");

        return value;
    }


}
