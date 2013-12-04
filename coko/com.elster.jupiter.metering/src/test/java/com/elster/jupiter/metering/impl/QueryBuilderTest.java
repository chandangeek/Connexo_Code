package com.elster.jupiter.metering.impl;

import com.elster.jupiter.util.conditions.And;
import com.elster.jupiter.util.conditions.Comparison;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Not;
import com.elster.jupiter.util.conditions.Operator;
import com.elster.jupiter.util.conditions.Or;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class QueryBuilderTest {

    private static final String VALUE = "value";
    private static final String FIELDNAME = "fieldname";
    private static final String VALUE1 = "value1";
    private static final String FIELDNAME1 = "fieldname1";

    @Test
    public void testFromComparison() {

        QueryBuilder parsed = QueryBuilder.parse(Operator.EQUAL.compare(FIELDNAME, VALUE));
        Condition condition = parsed.toCondition();

        assertThat(condition).isInstanceOf(Comparison.class);
        Comparison comparison = (Comparison) condition;
        assertThat(comparison.getFieldName()).isEqualTo(FIELDNAME);
        assertThat(comparison.getOperator()).isEqualTo(Operator.EQUAL);
        assertThat(comparison.getValues()).hasSize(1).contains(VALUE);
    }

    @Test
    public void testNot() {
        QueryBuilder parsed = QueryBuilder.parse(Operator.EQUAL.compare(FIELDNAME, VALUE).not());
        Condition condition = parsed.toCondition();

        assertThat(condition).isInstanceOf(Not.class);
        Not not = (Not) condition;
        assertThat(not.getNegated()).isInstanceOf(Comparison.class);
        Comparison comparison = (Comparison) not.getNegated();
        assertThat(comparison.getFieldName()).isEqualTo(FIELDNAME);
        assertThat(comparison.getOperator()).isEqualTo(Operator.EQUAL);
        assertThat(comparison.getValues()).hasSize(1).contains(VALUE);

    }

    @Test
    public void testAnd() {
        QueryBuilder parsed = QueryBuilder.parse(Operator.EQUAL.compare(FIELDNAME, VALUE).and(Operator.EQUAL.compare(FIELDNAME1, VALUE1)));
        Condition condition = parsed.toCondition();

        assertThat(condition).isInstanceOf(And.class);
        And and = (And) condition;
        assertThat(and.getConditions().get(0)).isInstanceOf(Comparison.class);
        Comparison comparison = (Comparison) and.getConditions().get(0);
        assertThat(comparison.getFieldName()).isEqualTo(FIELDNAME);
        assertThat(comparison.getOperator()).isEqualTo(Operator.EQUAL);
        assertThat(comparison.getValues()).hasSize(1).contains(VALUE);
        assertThat(and.getConditions().get(1)).isInstanceOf(Comparison.class);
        comparison = (Comparison) and.getConditions().get(1);
        assertThat(comparison.getFieldName()).isEqualTo(FIELDNAME1);
        assertThat(comparison.getOperator()).isEqualTo(Operator.EQUAL);
        assertThat(comparison.getValues()).hasSize(1).contains(VALUE1);

    }

    @Test
    public void testOr() {
        QueryBuilder parsed = QueryBuilder.parse(Operator.EQUAL.compare(FIELDNAME, VALUE).or(Operator.EQUAL.compare(FIELDNAME1, VALUE1)));
        Condition condition = parsed.toCondition();

        assertThat(condition).isInstanceOf(Or.class);
        Or or = (Or) condition;
        assertThat(or.getConditions().get(0)).isInstanceOf(Comparison.class);
        Comparison comparison = (Comparison) or.getConditions().get(0);
        assertThat(comparison.getFieldName()).isEqualTo(FIELDNAME);
        assertThat(comparison.getOperator()).isEqualTo(Operator.EQUAL);
        assertThat(comparison.getValues()).hasSize(1).contains(VALUE);
        assertThat(or.getConditions().get(1)).isInstanceOf(Comparison.class);
        comparison = (Comparison) or.getConditions().get(1);
        assertThat(comparison.getFieldName()).isEqualTo(FIELDNAME1);
        assertThat(comparison.getOperator()).isEqualTo(Operator.EQUAL);
        assertThat(comparison.getValues()).hasSize(1).contains(VALUE1);
    }

    @Test
    public void testComplexNesting() {
        Comparison comparison = Operator.EQUAL.compare(FIELDNAME, VALUE);
        QueryBuilder parsed = QueryBuilder.parse(comparison.not().or(comparison.and(comparison.not())));
        Condition condition = parsed.toCondition();

        assertThat(condition).isInstanceOf(Or.class);
        Or or = (Or) condition;
        assertThat(or.getConditions().get(0)).isInstanceOf(Not.class);
        Not not = (Not) or.getConditions().get(0);
        assertThat(not.getNegated()).isInstanceOf(Comparison.class);
        assertThat(or.getConditions().get(1)).isInstanceOf(And.class);
        And and = (And) or.getConditions().get(1);
        assertThat(and.getConditions().get(0)).isInstanceOf(Comparison.class);
        assertThat(and.getConditions().get(1)).isInstanceOf(Not.class);
        not = (Not) and.getConditions().get(1);
        assertThat(not.getNegated()).isInstanceOf(Comparison.class);
    }

}
