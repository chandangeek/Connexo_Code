/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.common.services;

/**
 * SortOrder is an enumeration of the possible sort orderings.
 *
 * @see javax.swing.RowSorter
 */
public enum SortOrder {
    /**
     * Enumeration value indicating the items are sorted in increasing order.
     * For example, the set <code>1, 4, 0</code> sorted in
     * <code>ASCENDING</code> order is <code>0, 1, 4</code>.
     */
    ASCENDING("ASC"),

    /**
     * Enumeration value indicating the items are sorted in decreasing order.
     * For example, the set <code>1, 4, 0</code> sorted in
     * <code>DESCENDING</code> order is <code>4, 1, 0</code>.
     */
    DESCENDING("DESC"),

    /**
     * Enumeration value indicating the items are unordered.
     * For example, the set <code>1, 4, 0</code> in
     * <code>UNSORTED</code> order is <code>1, 4, 0</code>.
     */
    UNSORTED("");
    private final String sql;

    SortOrder(String sql) {
        this.sql=sql;
    }

    public String sql() {
        return sql;
    }
}
