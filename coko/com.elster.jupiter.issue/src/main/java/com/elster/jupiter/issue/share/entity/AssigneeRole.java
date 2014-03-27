package com.elster.jupiter.issue.share.entity;

import com.elster.jupiter.orm.DataModel;

import javax.inject.Inject;

public class AssigneeRole extends Entity implements AssigneeBaseInformation {

    protected String name;
    protected String description;

    @Inject
    public AssigneeRole(DataModel dataModel) {
        super(dataModel);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
