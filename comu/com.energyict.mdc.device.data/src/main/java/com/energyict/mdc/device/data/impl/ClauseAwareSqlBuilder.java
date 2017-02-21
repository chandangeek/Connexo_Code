/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.util.sql.Fetcher;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;
import com.elster.jupiter.util.sql.TupleParser;
import com.energyict.mdc.device.data.impl.tasks.DeviceStateSqlBuilder;
import com.energyict.mdc.device.lifecycle.config.DefaultState;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Set;

/**
 * Wraps or decorates a {@link SqlBuilder} and is aware of the
 * actual SqlClause it is generating.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-08-01 (09:13)
 */
public class ClauseAwareSqlBuilder implements PreparedStatementProvider {

    private final SqlBuilder actualBuilder;
    private final State state = new State();

    public static ClauseAwareSqlBuilder with(String withClause, String alias) {
        ClauseAwareSqlBuilder builder = new ClauseAwareSqlBuilder(new SqlBuilder());
        builder.appendWith(withClause, alias);
        return builder;
    }

    public static ClauseAwareSqlBuilder withExcludedStates(String withClauseAliasName, Set<DefaultState> excludedStates, Instant now) {
        SqlBuilder actual = new SqlBuilder("with ");
        DeviceStateSqlBuilder
                .forExcludeStates(withClauseAliasName, excludedStates)
                .appendRestrictedStatesWithClause(actual, now);
        ClauseAwareSqlBuilder builder = new ClauseAwareSqlBuilder(actual);
        builder.state.toWith();
        return builder;
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
        this.state.reset();
    }

    public void appendWith(String withClause, String aliasName) {
        this.state.with(withClause, aliasName, this.actualBuilder);
    }

    /**
     * Completes the building of the from clause and starts
     * building the state clause or continues to build the
     * state clause by generating an 'and' construct.
     */
    public void appendWhereOrAnd () {
        this.state.whereOrAnd(this.actualBuilder);
    }

    public ClauseAwareSqlBuilder append(SqlFragment sqlFragment) {
        this.actualBuilder.add(sqlFragment);
        return this;
    }

    /**
     * Completes the building of the from clause and starts
     * building the state clause or continues to build the
     * state clause by generating an 'or' construct.
     */
    public void appendWhereOrOr () {
        this.state.whereOrOr(this.actualBuilder);
    }

    public ClauseAwareSqlBuilder addObject(Object value) {
        this.actualBuilder.addObject(value);
        return this;
    }

    public ClauseAwareSqlBuilder addInt(int value) {
        this.actualBuilder.addInt(value);
        return this;
    }

    public ClauseAwareSqlBuilder addLong(long value) {
        this.actualBuilder.addLong(value);
        return this;
    }

    public ClauseAwareSqlBuilder addNull(int sqlType) {
        this.actualBuilder.addNull(sqlType);
        return this;
    }

    public ClauseAwareSqlBuilder space() {
        this.actualBuilder.space();
        return this;
    }

    public ClauseAwareSqlBuilder append(String string) {
        this.actualBuilder.append(string);
        return this;
    }

    public ClauseAwareSqlBuilder spaceOpenBracket() {
        this.actualBuilder.spaceOpenBracket();
        return this;
    }

    public ClauseAwareSqlBuilder openBracket() {
        this.actualBuilder.openBracket();
        return this;
    }

    public ClauseAwareSqlBuilder closeBracketSpace() {
        this.actualBuilder.closeBracketSpace();
        return this;
    }

    public ClauseAwareSqlBuilder closeBracket() {
        this.actualBuilder.closeBracket();
        return this;
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

    public SqlBuilder asBuilder() {
        return this.actualBuilder;
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
         * Guess what: the initial state.
         */
        INITIAL {
            @Override
            protected SqlClause appendWhereOrAnd(SqlBuilder sqlBuilder) {
                return FROM.appendWhereOrAnd(sqlBuilder);
            }

            @Override
            protected SqlClause appendWhereOrOr(SqlBuilder sqlBuilder) {
                return FROM.appendWhereOrAnd(sqlBuilder);
            }

            @Override
            protected SqlClause with(String withClause, String aliasName, SqlBuilder sqlBuilder) {
                sqlBuilder.append("with ");
                sqlBuilder.append(aliasName);
                sqlBuilder.append(" as (");
                sqlBuilder.append(withClause);
                sqlBuilder.append(") ");
                return WITH;
            }
        },
        /**
         * Generating the with clause
         */
        WITH {
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

            @Override
            protected SqlClause with(String withClause, String aliasName, SqlBuilder sqlBuilder) {
                sqlBuilder.append(", ");
                sqlBuilder.append(aliasName);
                sqlBuilder.append(" as (");
                sqlBuilder.append(withClause);
                sqlBuilder.append(") ");
                return WITH;
            }
        },
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

            @Override
            protected SqlClause with(String withClause, String aliasName, SqlBuilder sqlBuilder) {
                throw new IllegalStateException("Cannot add with clauses when already generated the from clause");
            }
        },

        /**
         * Generating the state clause
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

            @Override
            protected SqlClause with(String withClause, String aliasName, SqlBuilder sqlBuilder) {
                throw new IllegalStateException("Cannot add with clauses when already generated the where clause");
            }
        };

        protected abstract SqlClause appendWhereOrAnd(SqlBuilder sqlBuilder);

        protected abstract SqlClause appendWhereOrOr(SqlBuilder sqlBuilder);

        protected abstract SqlClause with(String withClause, String aliasName, SqlBuilder sqlBuilder);

    }

    private class State {
        private SqlClause state = SqlClause.INITIAL;

        private void toWith() {
            this.state = SqlClause.WITH;
        }

        private void whereOrAnd(SqlBuilder sqlBuilder) {
            this.state = this.state.appendWhereOrAnd(sqlBuilder);
        }

        private void whereOrOr(SqlBuilder sqlBuilder) {
            this.state = this.state.appendWhereOrOr(sqlBuilder);
        }

        private void with(String withClause, String aliasName, SqlBuilder sqlBuilder) {
            this.state = this.state.with(withClause, aliasName, sqlBuilder);
        }

        public void reset() {
            this.state = SqlClause.FROM;
        }
    }

}