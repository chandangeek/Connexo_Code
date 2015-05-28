package com.energyict.mdc.device.data.impl.search;

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

/**
 * Serves as the root for all {@link SearchableDeviceProperty SearchableDeviceProperties}.
 * By default, all {@link Visitor} methods will throw Unsupp
 * and subclasses will override the ones they do support.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-28 (13:16)
 */
public abstract class AbstractSearchableDeviceProperty implements SearchableDeviceProperty, Visitor {

    private Condition underConstruction = Condition.TRUE;

    protected void and(Condition condition) {
        this.underConstruction = this.underConstruction.and(condition);
    }

    protected void or(Condition condition) {
        this.underConstruction = this.underConstruction.or(condition);
    }

    @Override
    public void visitOr(Or or) {
        throw unsupportedCondition("OR");
    }

    @Override
    public void visitAnd(And and) {
        throw unsupportedCondition("AND");
    }

    protected Condition constructed() {
        return underConstruction;
    }

    @Override
    public void visitComparison(Comparison comparison) {
        throw unsupportedCondition("COMPARE");
    }

    @Override
    public void visitNot(Not not) {
        throw unsupportedCondition("NOT");
    }

    @Override
    public void visitTrue(Constant trueCondition) {
        // Ignore
    }

    @Override
    public void visitFalse(Constant falseCondition) {
        // Ignore
    }

    @Override
    public void visitContains(Contains contains) {
        throw unsupportedCondition("IN (list)");
    }

    @Override
    public void visitMembership(Membership member) {
        throw unsupportedCondition("IN (subquery)");
    }

    @Override
    public void visitExists(Exists empty) {
        throw unsupportedCondition("EXISTS");
    }

    @Override
    public void visitText(Text expression) {
        throw unsupportedCondition("TEXT");
    }

    @Override
    public void visitFragmentExpression(FragmentExpression expression) {
        throw unsupportedCondition("FRAGMENT");
    }

    @Override
    public void visitEffective(Effective effective) {
        throw unsupportedCondition("EFFECTIVE");
    }

    private UnsupportedOperationException unsupportedCondition(String conditionType) {
        return new UnsupportedOperationException(conditionType + " condition not expected");
    }

}