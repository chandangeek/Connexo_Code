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
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

/**
 * Serves as the root for all {@link SearchableDeviceProperty SearchableDeviceProperties}.
 * By default, all {@link Visitor} methods will throw Unsupp
 * and subclasses will override the ones they do support.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-28 (13:16)
 */
public abstract class AbstractSearchableDeviceProperty implements SearchableDeviceProperty, Visitor {

    private SqlBuilder underConstruction = new SqlBuilder();
    private String columnName;
    private Instant now;

    protected SqlFragment toSqlFragment(String columnName, Condition condition, Instant now) {
        this.columnName = columnName;
        this.now = now;
        this.underConstruction = new SqlBuilder();
        this.underConstruction.openBracket();
        condition.visit(this);
        this.underConstruction.closeBracket();
        return this.underConstruction;
    }

    protected SqlFragment toSqlFragment(Instant instant) {
        return new InstantFragment(instant);
    }

    public void visitAnd(And and) {
        this.visitAll(and.getConditions(), " AND ");
    }

    public void visitOr(Or or) {
        this.visitAll(or.getConditions(), " OR ");
    }

    private void visitAll(List<Condition> conditions , String separator) {
        String sep = "";
        this.underConstruction.openBracket();
        for (Condition each : conditions) {
            this.underConstruction.append(sep);
            this.underConstruction.append(separator);
            each.visit(this);
        }
        this.underConstruction.closeBracket();
    }

    @Override
    public void visitComparison(Comparison comparison) {
        this.underConstruction.append(comparison.getText(this.columnName));
        Stream
            .of(comparison.getValues())
            .forEach(this.underConstruction::addObject);
    }

    @Override
    public void visitNot(Not not) {
        this.underConstruction.append(" NOT");
        this.underConstruction.openBracket();
        not.getNegated().visit(this);
        this.underConstruction.closeBracket();
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
        this.underConstruction.append(this.columnName);
        this.underConstruction.append(" ");
        this.underConstruction.append(contains.getOperator().getSymbol());
        this.underConstruction.spaceOpenBracket();
        String separator = "";
        for (Object each : contains.getCollection()) {
            this.underConstruction.append(separator);
            this.underConstruction.append("?");
            separator = ",";
        }
        this.underConstruction.closeBracket();
    }

    @Override
    public void visitMembership(Membership membership) {
        this.underConstruction.append(this.columnName);
        this.underConstruction.append(membership.getOperator().getSymbol());
        this.underConstruction.spaceOpenBracket();
        this.underConstruction.add(membership.getSubquery().toFragment());
        this.underConstruction.closeBracket();
    }

    @Override
    public void visitExists(Exists empty) {
        this.underConstruction.append(" EXISTS ");
        this.underConstruction.openBracket();
        this.underConstruction.add(empty.getSubquery().toFragment());
        this.underConstruction.closeBracket();
    }

    @Override
    public void visitText(Text expression) {
        this.underConstruction.append(expression.getText());
    }

    @Override
    public void visitFragmentExpression(FragmentExpression expression) {
        this.underConstruction.add(expression.getFragment());
    }

    @Override
    public void visitEffective(Effective effective) {
        Where.where(this.columnName).isEffective(this.now).visit(this);
    }

    private static class InstantFragment implements SqlFragment {
        private final Instant instant;

        public InstantFragment(Instant instant) {
            this.instant = instant;
        }

        @Override
        public String getText() {
            return "";
        }

        @Override
        public int bind(PreparedStatement statement, int position) throws SQLException {
            Date sqlDate = instant == null ? null : new Date(instant.toEpochMilli());
            statement.setDate(position, sqlDate);
            return position + 1;
        }
    }
}