package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.util.HasId;
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
import com.elster.jupiter.util.conditions.Operator;
import com.elster.jupiter.util.conditions.Or;
import com.elster.jupiter.util.conditions.Text;
import com.elster.jupiter.util.conditions.Visitor;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;
import com.energyict.mdc.device.data.impl.search.sqlbuilder.ValueBinder;
import com.energyict.mdc.dynamic.TimeDurationValueFactory;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;

/**
 * Serves as the root for all {@link SearchableDeviceProperty SearchableDeviceProperties}.
 * By default, all {@link Visitor} methods will throw Unsupp
 * and subclasses will override the ones they do support.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-28 (13:16)
 */
public abstract class AbstractSearchableDeviceProperty implements SearchableDeviceProperty, Visitor, ValueBinder {

    private SqlBuilder underConstruction = new SqlBuilder();
    private String columnName;
    private Instant now;

    @Override
    public final String toDisplay(Object value) {
        if (!this.valueCompatibleForDisplay(value)) {
            throw new IllegalArgumentException("Value not compatible with domain");
        }
        return this.toDisplayAfterValidation(value);
    }

    /**
     * Tests that the specified value is compatible with the possible values for this SearchableDeviceProperty.
     *
     * @param value The value
     */
    protected abstract boolean valueCompatibleForDisplay(Object value);

    protected abstract String toDisplayAfterValidation(Object value);

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
        return new InstantFragment(this, instant);
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
        if (Operator.BETWEEN == comparison.getOperator() && comparison.getValues().length > 0 && comparison.getValues()[0] instanceof TimeDuration) {
            visitBetweenComparisonWithTimeDuration((TimeDuration) comparison.getValues()[0], (TimeDuration) comparison.getValues()[1]);
        } else {
            this.underConstruction.add(new ComparisonFragment(this, this.columnName, comparison));
        }
    }

    private void visitBetweenComparisonWithTimeDuration(TimeDuration from, TimeDuration to) {
        //SUBSTR(column, instr(column, ':') + 1) = {unit} AND SUBSTR(column, 1, instr(column, ':') - 1) BETWEEN {from} AND {to}
        this.underConstruction.append("substr(");
        this.underConstruction.append(columnName);
        this.underConstruction.append(", instr(");
        this.underConstruction.append(columnName);
        this.underConstruction.append(", '");
        this.underConstruction.append(TimeDurationValueFactory.VALUE_UNIT_SEPARATOR);
        this.underConstruction.append("') + 1 ) = ");
        this.underConstruction.addInt(from.getTimeUnitCode());
        this.underConstruction.append(" AND ");
        this.underConstruction.append("substr(");
        this.underConstruction.append(columnName);
        this.underConstruction.append(", 1, instr(");
        this.underConstruction.append(columnName);
        this.underConstruction.append(", '");
        this.underConstruction.append(TimeDurationValueFactory.VALUE_UNIT_SEPARATOR);
        this.underConstruction.append("') - 1) between ");
        this.underConstruction.addInt(from.getCount());
        this.underConstruction.append(" AND ");
        this.underConstruction.addInt(to.getCount());
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
        this.underConstruction.add(new ComparisonFragment(this, this.columnName, Operator.EQUAL.compare(this.columnName, Boolean.TRUE)));
    }

    @Override
    public void visitFalse(Constant falseCondition) {
        this.underConstruction.openBracket();
        this.underConstruction.add(new ComparisonFragment(this, this.columnName, Operator.EQUAL.compare(this.columnName, Boolean.FALSE)));
        this.underConstruction.append(" OR ");
        this.underConstruction.add(new ComparisonFragment(this, this.columnName, Operator.ISNULL.compare(this.columnName)));
        this.underConstruction.closeBracket();
    }

    @Override
    public void visitContains(Contains contains) {
        this.underConstruction.add(new InFragment(this, this.columnName, contains));
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

    @Override
    public void bindSingleValue(PreparedStatement statement, int bindPosition, Object value) throws SQLException {
        if (value instanceof HasId) {
            HasId hasId = (HasId) value;
            statement.setLong(bindPosition, hasId.getId());
        } else if (value instanceof HasIdAndName) {
            HasIdAndName hasId = (HasIdAndName) value;
            statement.setObject(bindPosition, hasId.getId());
        } else {
            getSpecification().getValueFactory().bind(statement, bindPosition, value);
        }
    }

    private abstract static class ProxyAwareSqlFragment{

        private final ValueBinder valueBinder;

        protected ProxyAwareSqlFragment(ValueBinder valueBinder) {
            this.valueBinder = valueBinder;
        }

        protected void bindSingleValue(PreparedStatement statement, int bindPosition, Object value) throws SQLException {
            this.valueBinder.bindSingleValue(statement, bindPosition, value);
        }
    }

    private static class InstantFragment extends ProxyAwareSqlFragment implements SqlFragment {
        private final Instant instant;

        public InstantFragment(ValueBinder valueBinder, Instant instant) {
            super(valueBinder);
            this.instant = instant;
        }

        @Override
        public String getText() {
            return "";
        }

        @Override
        public int bind(PreparedStatement statement, int position) throws SQLException {
            statement.setLong(position, this.instant.toEpochMilli());
            return position + 1;
        }
    }

    private static class ComparisonFragment extends ProxyAwareSqlFragment implements SqlFragment {
        private final String columnName;
        private final Comparison comparison;

        private ComparisonFragment(ValueBinder valueBinder, String columnName, Comparison comparison) {
            super(valueBinder);
            this.columnName = columnName;
            this.comparison = comparison;
        }

        @Override
        public String getText() {
            return this.comparison.getText(this.columnName);
        }

        @Override
        public int bind(PreparedStatement statement, int position) throws SQLException {
            int bindPosition = position;
            for (Object value : this.comparison.getValues()) {
                this.bindSingleValue(statement, bindPosition, value);
                bindPosition++;
            }
            return bindPosition;
        }
    }

    private static class InFragment extends ProxyAwareSqlFragment implements SqlFragment {
        private final String columnName;
        private final Contains contains;

        private InFragment(ValueBinder valueBinder, String columnName, Contains contains) {
            super(valueBinder);
            this.columnName = columnName;
            this.contains = contains;
        }

        @Override
        public int bind(PreparedStatement statement, int position) throws SQLException {
            int bindPosition = position;
            for (Object value : this.contains.getCollection()) {
                this.bindSingleValue(statement, bindPosition, value);
                bindPosition++;
            }
            return bindPosition;
        }

        @Override
        public String getText() {
            StringBuilder textBuilder = new StringBuilder();
            textBuilder
                .append(this.columnName)
                .append(" ")
                .append(this.contains.getOperator().getSymbol())
                .append(" (");
            String separator = "";
            for (Object each : contains.getCollection()) {
                textBuilder.append(separator);
                textBuilder.append("?");
                separator = ",";
            }
            textBuilder.append(")");
            return textBuilder.toString();
        }
    }
}