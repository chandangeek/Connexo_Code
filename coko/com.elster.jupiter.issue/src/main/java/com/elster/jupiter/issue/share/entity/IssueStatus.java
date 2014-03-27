package com.elster.jupiter.issue.share.entity;


import com.elster.jupiter.orm.DataModel;

import javax.inject.Inject;

public class IssueStatus extends Entity {
    private String name;
    private boolean isFinal;

    @Inject
    public IssueStatus(DataModel dataModel) {
        super(dataModel);
    }

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
