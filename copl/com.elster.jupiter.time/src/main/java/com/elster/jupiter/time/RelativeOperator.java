package com.elster.jupiter.time;

/**
 * Created by borunova on 01.10.2014.
 */
public enum RelativeOperator {
    PLUS("+"),
    MINUS("-"),
    EQUAL("=");

    private String operator;

    private RelativeOperator(String operator) {
        this.operator = operator;
    }

    @Override
    public String toString() {
        return operator;
    }

    public static RelativeOperator from(String operator) {
        switch(operator) {
            case "+":
                return RelativeOperator.PLUS;
            case "-":
                return RelativeOperator.MINUS;
            case "=":
                return RelativeOperator.EQUAL;
            default:
                throw new IllegalArgumentException("Unknown operation");
        }
    }
}
