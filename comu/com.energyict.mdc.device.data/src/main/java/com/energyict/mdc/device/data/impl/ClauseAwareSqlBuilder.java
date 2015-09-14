package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.util.sql.Fetcher;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;
import com.elster.jupiter.util.sql.TupleParser;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Wraps or decorates a {@link SqlBuilder} and is aware of the
 * actual SqlClause it is generating.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-08-01 (09:13)
 */
public class ClauseAwareSqlBuilder implements PreparedStatementProvider {

    private final SqlBuilder actualBuilder;
    private final Where where = new Where();

    public static ClauseAwareSqlBuilder with(String withClause, String alias) {
        return new ClauseAwareSqlBuilder(new SqlBuilder("with " + alias + " as (" + withClause + ") "));
    }

    public static ClauseAwareSqlBuilder select(String selectClause) {
        return new ClauseAwareSqlBuilder(new SqlBuilder("select " + selectClause));
    }

    private ClauseAwareSqlBuilder(SqlBuilder actualBuilder) {
        super();
        this.actualBuilder = actualBuilder;
    }

    public void unionAll () {
        this.actualBuilder.append(" UNION ALL ");
        this.where.reset();
    }

    /**
     * Completes the building of the from clause and starts
     * building the where clause or continues to build the
     * where clause by generating an 'and' construct.
     */
    public void appendWhereOrAnd () {
        this.where.orAnd(this.actualBuilder);
    }

    public void append(SqlFragment sqlFragment) {
        this.actualBuilder.add(sqlFragment);
    }

    /**
     * Completes the building of the from clause and starts
     * building the where clause or continues to build the
     * where clause by generating an 'or' construct.
     */
    public void appendWhereOrOr () {
        this.where.orOr(this.actualBuilder);
    }

    public void addObject(Object value) {
        this.actualBuilder.addObject(value);
    }

    public void addInt(int value) {
        this.actualBuilder.addInt(value);
    }

    public void addLong(long value) {
        this.actualBuilder.addLong(value);
    }

    public void addNull(int sqlType) {
        this.actualBuilder.addNull(sqlType);
    }

    public void space() {
        this.actualBuilder.space();
    }

    public void append(String string) {
        this.actualBuilder.append(string);
    }

    public void spaceOpenBracket() {
        this.actualBuilder.spaceOpenBracket();
    }

    public void openBracket() {
        this.actualBuilder.openBracket();
    }

    public void closeBracketSpace() {
        this.actualBuilder.closeBracketSpace();
    }

    public void closeBracket() {
        this.actualBuilder.closeBracket();
    }

    @Override
    public String toString() {
        return this.actualBuilder.toString();
    }

    public String getText() {
        return this.actualBuilder.toString();
    }

    public int bind(PreparedStatement statement, int position) throws SQLException {
        return this.actualBuilder.bind(statement, position);
    }

    public SqlBuilder asPageBuilder(int from, int to) {
        return this.actualBuilder.asPageBuilder(from, to);
    }

    @Override
    public PreparedStatement prepare(Connection connection) throws SQLException {
        return this.actualBuilder.prepare(connection);
    }

    public <T> Fetcher<T> fetcher(Connection connection, TupleParser<T> tupleParser) throws SQLException {
        return this.actualBuilder.fetcher(connection, tupleParser);
    }

    /**
     * Models the SQL clause that is currently being generated.
     */
    private enum SqlClause {
        /**
         * Generating the from clause
         */
        FROM {
            @Override
            protected SqlClause appendWhereOrAnd(SqlBuilder sqlBuilder) {
                sqlBuilder.append(" where ");
                return WHERE;
            }

            @Override
            protected SqlClause appendWhereOrOr(SqlBuilder sqlBuilder) {
                sqlBuilder.append(" where ");
                return WHERE;
            }
        },

        /**
         * Generating the where clause
         */
        WHERE {
            @Override
            protected SqlClause appendWhereOrAnd(SqlBuilder sqlBuilder) {
                sqlBuilder.append(" and ");
                return this;
            }

            @Override
            protected SqlClause appendWhereOrOr(SqlBuilder sqlBuilder) {
                sqlBuilder.append(" or ");
                return this;
            }
        };

        protected abstract SqlClause appendWhereOrAnd(SqlBuilder sqlBuilder);

        protected abstract SqlClause appendWhereOrOr(SqlBuilder sqlBuilder);

    }

    private class Where {
        private SqlClause state = SqlClause.FROM;

        private void orAnd(SqlBuilder sqlBuilder) {
            this.state = this.state.appendWhereOrAnd(sqlBuilder);
        }

        private void orOr(SqlBuilder sqlBuilder) {
            this.state = this.state.appendWhereOrOr(sqlBuilder);
        }

        public void reset() {
            this.state = SqlClause.FROM;
        }
    }

}