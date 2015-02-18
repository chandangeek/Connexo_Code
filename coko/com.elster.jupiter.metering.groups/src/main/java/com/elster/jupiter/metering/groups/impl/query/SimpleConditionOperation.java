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
    private static final Set<Operator> ALLOWED_OPERATIONS = ImmutableSet.of(Operator.EQUAL, Operator.LIKE, Operator.LIKEIGNORECASE);
    public static final String TYPE_IDENTIFIER = "SCD";

    private String fieldName;
    private Operator operator = Operator.EQUAL;
    private Object[] values;

    @Inject
	SimpleConditionOperation() {
        // for persistence
    }

    public SimpleConditionOperation(Comparison comparison) {
        operator = comparison.getOperator();
        fieldName = comparison.getFieldName();
        if (!ALLOWED_OPERATIONS.contains(operator)) {
            throw new UnsupportedOperationException("Unsupported operator, use one of the following: "+Arrays.toString(ALLOWED_OPERATIONS.toArray()));
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
        if (!ALLOWED_OPERATIONS.contains(operator)) {
            throw new UnsupportedOperationException("Unsupported operator, use one of the following: "+Arrays.toString(ALLOWED_OPERATIONS.toArray()));
        }
        return operator.compare(fieldName, values);
    }

    public String getFieldName() {
        return fieldName;
    }

    public Object[] getValues() {
        if (Operator.EQUAL.equals(operator)) {
            return values;
        } if (Operator.LIKE.equals(operator) || Operator.LIKEIGNORECASE.equals(operator)) {
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
