package com.elster.jupiter.issue.share.entity;

public class AssigneeTeam extends Entity implements AssigneeBaseInformation{
    protected String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
