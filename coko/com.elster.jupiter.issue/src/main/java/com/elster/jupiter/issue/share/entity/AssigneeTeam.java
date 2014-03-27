package com.elster.jupiter.issue.share.entity;

import com.elster.jupiter.orm.DataModel;

import javax.inject.Inject;

public class AssigneeTeam extends Entity implements AssigneeBaseInformation{
    protected String name;

    @Inject
    public AssigneeTeam(DataModel dataModel) {
        super(dataModel);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
