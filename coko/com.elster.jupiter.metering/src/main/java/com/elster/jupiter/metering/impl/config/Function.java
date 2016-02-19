package com.elster.jupiter.metering.impl.config;

/**
 * Models the supported functions that can be used in {@link com.elster.jupiter.metering.config.Formula}'s
 * of {@link com.elster.jupiter.metering.config.ReadingTypeDeliverable}s.
 *
 * @author Isabelle Gheysens (igh)
 * @since 2016-02-08
 */
public enum Function {
    SUM(1),
    MAX(2),
    MIN(3),
    AVG(4);

    private final int id;

    Function(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

}