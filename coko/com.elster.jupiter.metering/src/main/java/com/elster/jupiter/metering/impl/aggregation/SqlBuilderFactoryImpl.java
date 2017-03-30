/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

/**
 * Provides an implementation for the {@link SqlBuilderFactory} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-18 (16:09)
 */
public class SqlBuilderFactoryImpl implements SqlBuilderFactory {
    @Override
    public ClauseAwareSqlBuilder newClauseAwareSqlBuilder() {
        return new ClauseAwareSqlBuilderImpl();
    }
}