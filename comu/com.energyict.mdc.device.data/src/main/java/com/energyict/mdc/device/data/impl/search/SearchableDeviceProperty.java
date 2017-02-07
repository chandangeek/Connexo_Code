/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.sql.SqlFragment;

import java.time.Instant;

/**
 * Adds behavior to {@link SearchableProperty} that applies
 * to Device related properties only and that will
 * assist in generating actual query {@link Condition}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-28 (13:06)
 */
public interface SearchableDeviceProperty extends SearchableProperty {

    /**
     * Appends the join clauses that are required to support
     * the condition for this SearchableDeviceProperty.
     *
     * @param builder The JoinClauseBuilder
     */
    void appendJoinClauses(JoinClauseBuilder builder);

    /**
     * Converts the {@link Condition} that is expected to be
     * expressed against this SearchableDeviceProperty
     * to a {@link SqlFragment} that can be added to a SqlBuilder.
     *
     * @param condition The Condition
     * @param now The current timestamp
     * @return The SqlFragment
     */
    SqlFragment toSqlFragment(Condition condition, Instant now);

}