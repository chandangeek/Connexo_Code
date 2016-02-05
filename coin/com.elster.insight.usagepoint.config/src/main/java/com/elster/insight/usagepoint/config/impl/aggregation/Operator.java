package com.elster.insight.usagepoint.config.impl.aggregation;

/**
 * Created by igh on 4/02/2016.
 */
public enum Operator {
    PLUS(1),
    MINUS(2),
    MULTIPLY(3),
    DIVIDE(4);

    private final int id;

    Operator(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

}
