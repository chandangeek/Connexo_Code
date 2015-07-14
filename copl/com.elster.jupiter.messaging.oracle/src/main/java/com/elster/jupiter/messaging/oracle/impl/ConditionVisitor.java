package com.elster.jupiter.messaging.oracle.impl;

import com.elster.jupiter.util.conditions.And;
import com.elster.jupiter.util.conditions.Comparison;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Constant;
import com.elster.jupiter.util.conditions.Contains;
import com.elster.jupiter.util.conditions.Effective;
import com.elster.jupiter.util.conditions.Exists;
import com.elster.jupiter.util.conditions.FragmentExpression;
import com.elster.jupiter.util.conditions.Membership;
import com.elster.jupiter.util.conditions.Not;
import com.elster.jupiter.util.conditions.Or;
import com.elster.jupiter.util.conditions.Text;
import com.elster.jupiter.util.conditions.Visitor;
import com.elster.jupiter.util.sql.SqlBuilder;

import java.util.List;

/**
 * Created by bbl on 13/07/2015.
 */
public class ConditionVisitor implements Visitor {

    private SqlBuilder builder = new SqlBuilder();

    @Override
    public String toString() {
        return builder.toString();
    }

    private void visitAll(List<Condition> conditions, String separator) {
        boolean firstTime = true;
        builder.openBracket();
        for (Condition each : conditions) {
            if (firstTime) {
                firstTime = false;
            } else {
                builder.append(separator);
            }
            each.visit(this);
        }
        builder.closeBracket();
    }

    @Override
    public void visitOr(Or or) {
        visitAll(or.getConditions(), " OR ");
    }

    @Override
    public void visitAnd(And and) {
        visitAll(and.getConditions(), " AND ");
    }

    @Override
    public void visitComparison(Comparison comparison) {
        if (!comparison.getFieldName().equalsIgnoreCase("corrid")) {
            throw new IllegalArgumentException("Invalid field name '" + comparison.getFieldName() + "'. Only supported is corrid");
        }
        String text = comparison.getText(comparison.getFieldName());
        for (Object o : comparison.getValues()) {
            text = text.replaceFirst("\\?", "\'" + o.toString() + "\'");
        }
        builder.append(text.trim());
    }

    @Override
    public void visitNot(Not not) {
        builder.append(" NOT ");
        builder.openBracket();
        not.getNegated().visit(this);
        builder.closeBracket();
    }

    @Override
    public void visitTrue(Constant constant) {
        builder.append(" 1 = 1 ");
    }

    @Override
    public void visitFalse(Constant constant) {
        builder.append(" 1 = 0 ");
    }

    @Override
    public void visitContains(Contains contains) {
        throw new UnsupportedOperationException("Contains condition not supported");
    }

    @Override
    public void visitMembership(Membership membership) {
        throw new UnsupportedOperationException("Membership condition not supported");
    }

    @Override
    public void visitExists(Exists exists) {
        throw new UnsupportedOperationException("Exists condition not supported");
    }

    @Override
    public void visitText(Text text) {
        builder.append(text.getText());
    }

    @Override
    public void visitFragmentExpression(FragmentExpression fragmentExpression) {
        builder.add(fragmentExpression.getFragment());
    }

    @Override
    public void visitEffective(Effective effective) {
        throw new UnsupportedOperationException("Effective condition not supported");
    }

}
