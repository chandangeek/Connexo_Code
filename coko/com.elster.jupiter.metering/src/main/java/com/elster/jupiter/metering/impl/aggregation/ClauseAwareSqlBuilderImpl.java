/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.util.sql.SqlBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides an implementation for the {@link ClauseAwareSqlBuilder} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-11 (14:45)
 */
class ClauseAwareSqlBuilderImpl implements ClauseAwareSqlBuilder {

    private Set<String> aliasNames = new HashSet<>();
    private List<SqlBuilder> withClauses = new ArrayList<>();
    private List<SqlBuilder> selectClauses = new ArrayList<>();

    @Override
    public boolean withExists(String alias) {
        return this.aliasNames.contains(alias);
    }

    @Override
    public SqlBuilder with(String alias, Optional<String> comment, String... columnAliasNames) {
        if (this.aliasNames.add(alias)) {
            SqlBuilder sqlBuilder = new SqlBuilder(alias);
            if (columnAliasNames.length > 0) {
                sqlBuilder.append("(");
                sqlBuilder.append(Stream.of(columnAliasNames).collect(Collectors.joining(", ")));
                sqlBuilder.append(")");
            }
            sqlBuilder.append(" AS (");
            comment.ifPresent(cmt -> this.appendWithClauseComment(cmt, sqlBuilder));
            sqlBuilder.append("\n    ");
            this.withClauses.add(sqlBuilder);
            return sqlBuilder;
        } else {
            throw new IllegalArgumentException("With clause with the specified name already exists: " + alias);
        }
    }

    private void appendWithClauseComment(String comment, SqlBuilder sqlBuilder) {
        sqlBuilder.append("/* ");
        sqlBuilder.append(comment);
        sqlBuilder.append(" */");
    }

    @Override
    public SqlBuilder select() {
        SqlBuilder sqlBuilder = new SqlBuilder("SELECT ");
        this.selectClauses.add(sqlBuilder);
        return sqlBuilder;
    }

    @Override
    public SqlBuilder finish() {
        SqlBuilder complete = new SqlBuilder();
        this.appendWithClauses(complete);
        this.appendSelectClauses(complete);
        return complete;
    }

    private void appendWithClauses(SqlBuilder sqlBuilder) {
        if (!this.withClauses.isEmpty()) {
            sqlBuilder.append("WITH");
            sqlBuilder.append("\n  ");
            this.appendAll(sqlBuilder, this.withClauses, SqlSeparator.WITH_CLAUSE);
            sqlBuilder.append(")\n");
        }
    }

    private void appendSelectClauses(SqlBuilder sqlBuilder) {
        if (!this.selectClauses.isEmpty()) {
            this.appendAll(sqlBuilder, this.selectClauses, SqlSeparator.UNION);
        }
    }

    private void appendAll(SqlBuilder sqlBuilder, List<SqlBuilder> sqlBuilders, SqlSeparator separator) {
        Iterator<SqlBuilder> iterator = sqlBuilders.iterator();
        while (iterator.hasNext()) {
            SqlBuilder next = iterator.next();
            sqlBuilder.add(next);
            if (iterator.hasNext()) {
                separator.appendTo(sqlBuilder);
            }
        }
    }


    private enum SqlSeparator {
        NONE {
            @Override
            void appendTo(SqlBuilder sqlBuilder) {
                // Which part of "none" did you not understand if you think we need to append something here :-)
            }
        },
        WITH_CLAUSE {
            @Override
            void appendTo(SqlBuilder sqlBuilder) {
                sqlBuilder.append("), ");
                sqlBuilder.append("\n  ");
            }
        },
        UNION {
            @Override
            void appendTo(SqlBuilder sqlBuilder) {
                sqlBuilder.append("\n");
                sqlBuilder.append("UNION ALL ");
                sqlBuilder.append("\n");
            }
        };

        abstract void appendTo(SqlBuilder sqlBuilder);
    }
}