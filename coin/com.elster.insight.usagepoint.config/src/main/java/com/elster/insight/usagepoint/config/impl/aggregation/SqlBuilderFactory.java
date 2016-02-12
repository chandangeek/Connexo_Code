package com.elster.insight.usagepoint.config.impl.aggregation;

/**
 * Provides factory services for {@link ClauseAwareSqlBuilder}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-11 (10:01)
 */
interface SqlBuilderFactory {

    /**
     * Returns a new ClauseAwareSqlBuilder that is ready to
     * start building a SQL statement.
     *
     * @return The new ClauseAwareSqlBuilder
     */
    ClauseAwareSqlBuilder newClauseAwareSqlBuilder();

}