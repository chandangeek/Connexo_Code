/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

/**
 * Provides sql building services for timelines that need truncation.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-07-18 (10:30)
 */
interface TruncatedTimelineSqlBuilder {
    void append(String sqlName);
}