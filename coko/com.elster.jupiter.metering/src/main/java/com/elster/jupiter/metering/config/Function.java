package com.elster.jupiter.metering.config;

/**
 * Models the supported functions that can be used in {@link com.elster.jupiter.metering.config.Formula}'s
 * of {@link com.elster.jupiter.metering.config.ReadingTypeDeliverable}s.
 *
 * @author Isabelle Gheysens (igh)
 * @since 2016-02-08
 */
public enum Function {
    SUM,
    MAX,
    MIN,
    AVG,
    AGG_TIME {
        @Override
        public String toString() {
            return "agg";
        }
    };

    public String toString() {
        return this.name().toLowerCase();
    }

}