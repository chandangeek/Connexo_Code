package com.elster.jupiter.tasks;

import com.elster.jupiter.nls.TranslationKey;

/**
 * Copyrights EnergyICT
 * Date: 25/11/2014
 * Time: 9:22
 */
public enum TaskStatus implements TranslationKey {

    BUSY("Ongoing"),
    SUCCESS("Successful"),
    FAILED("Failed"),
    NOT_EXECUTED_YET("Created");

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

    @Override
    public String getKey() {
        return toString();
    }

    @Override
    public String getDefaultFormat() {
        return toString();
    }
}
