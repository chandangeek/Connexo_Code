/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

/**
 * Provides factory services for {@link ClauseAwareSqlBuilder}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-11 (10:01)
 */
public interface SqlBuilderFactory {

    /**
     * Returns a new ClauseAwareSqlBuilder that is ready to
     * start building a SQL statement.
     *
     * @return The new ClauseAwareSqlBuilder
     */
    ClauseAwareSqlBuilder newClauseAwareSqlBuilder();

}