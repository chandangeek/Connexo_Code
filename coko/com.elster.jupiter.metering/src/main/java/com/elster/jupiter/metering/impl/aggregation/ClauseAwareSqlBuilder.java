/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.util.sql.SqlBuilder;

import java.util.Optional;

/**
 * Wraps or assembles one or more {@link SqlBuilder}s that you will
 * use to build fragments of the complete SQL statement you need.
 * You can call each of the methods separately in whatever order you
 * need or want and when finishing the build process, this
 * builder will assemble the query, respecting the order or your calls.
 * As an example:
 * <pre><code>
 * ClauseAwareSqlBuilder builder = ...;
 * builder.select().append("w1.value - w2.value from w1 join w2 on w2.id = w1.id");
 * builder.with("w1, "id", "value").append("1, 2 from dual");
 * builder.unionAll().append("w1.value * w2.value from w1 join w2 on w2.id = w1.id");
 * builder.with("w2, "id", "value").append("1, 20 from dual");
 * builder.finish().getText() returns (excluding formatting):
 * with
 *   w1(id, value) as (select 1, 2 from dual),
 *   w2(id, value) as (select 10, 20 from dual)
 * select w1.value - w2.value
 *   from w1
 *   join w2 on w2.id = w1.id
 * union all
 * select w1.value * w2.value
 *   from w1
 *   join w2 on w2.id = w1.id
 * </code></pre>
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-11 (09:37)
 */
interface ClauseAwareSqlBuilder {

    /**
     * Tests if a with clause with the specified name already exists.
     * It will only exist after a previous call to {@link #with(String, Optional, String...)}
     * with that name.
     *
     * @param alias The name of the with clause
     * @return A flag that indicates if a with clause with that name already exists
     */
    boolean withExists(String alias);

    /**
     * Returns a SqlBuilder that will be included in the overall
     * SQL statement being built as a "WITH" clause.
     * Multiple with clauses are supported, the first call will
     * produce the "WITH" syntax element, the remaining calls
     * will simply produce a "," separating the new with clause
     * from the existing with clauses.
     *
     * @param alias The name of the with clause
     * @param columnAliasNames An optional list of aliases for the name of the columns that are selected
     * by the sql of the with clause
     * @return The SqlBuilder
     */
    SqlBuilder with(String alias, Optional<String> comment, String... columnAliasNames);

    /**
     * Returns a new SqlBuilder that already contains "SELECT" and
     * that will be included in the overall SQL statement being built.
     * Note that multiple select clauses will automatically be merged
     * together with the "UNION ALL" construct.
     *
     * @return The SqlBuilder that allows you to build the "SELECT" clause
     */
    SqlBuilder select();

    /**
     * Returns the SqlBuilder that contains the overall
     * SQL statement that was built.
     *
     * @return The SqlBuilder containing the completed SQL statement
     */
    SqlBuilder finish();

}