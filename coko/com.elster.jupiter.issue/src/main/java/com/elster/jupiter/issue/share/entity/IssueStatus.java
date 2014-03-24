package com.elster.jupiter.issue.share.entity;


public class IssueStatus extends Entity {
    private String name;
    private boolean isFinal;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isFinal() {
        return isFinal;
    }

    public void setFinal(boolean isFinal) {
        this.isFinal = isFinal;
    }
}
