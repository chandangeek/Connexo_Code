package com.energyict.mdc.dynamic.relation.impl;

import com.energyict.mdc.common.SqlBuilder;

/**
 * Supports appending elements of a list to a StringBuilder
 * with appropriate list separator that is currently forced to ",".
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-06-07 (11:48)
 */
enum ListAppendMode {
    FIRST {
        @Override
        protected void startOn (StringBuilder builder) {
            // Nothing to append before the first message
        }

        @Override
        protected void startOn (SqlBuilder builder) {
            // Nothing to append before the first message
        }
    },
    REMAINING {
        @Override
        protected void startOn (StringBuilder builder) {
            builder.append(", ");
        }

        @Override
        protected void startOn (SqlBuilder builder) {
            builder.append(", ");
        }
    };

    protected abstract void startOn (StringBuilder builder);

    protected abstract void startOn (SqlBuilder builder);

}