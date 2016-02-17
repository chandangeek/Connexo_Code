package com.elster.jupiter.metering.impl.config;

/**
 * Created by igh on 8/02/2016.
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
