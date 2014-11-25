package com.elster.jupiter.tasks;

/**
 * Copyrights EnergyICT
 * Date: 25/11/2014
 * Time: 9:22
 */
public enum TaskStatus {

    BUSY("Busy"),
    SUCCESS("Success"),
    FAILED("Failed"),
    NOT_EXECUTED_YET("Not executed yet");

    private String name;

    TaskStatus(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }

    public TaskStatus success() {
        return (FAILED.equals(this) ? FAILED : SUCCESS);
    }

    public TaskStatus fail() {
        return (SUCCESS.equals(this) ? SUCCESS : FAILED);
    }

    public boolean isFinal() {
        return SUCCESS.equals(this) || FAILED.equals(this);
    }
}
