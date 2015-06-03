package com.elster.jupiter.issue.impl.records;

import com.elster.jupiter.issue.share.entity.AssigneeType;
import com.elster.jupiter.issue.share.entity.IssueAssignee;
import com.elster.jupiter.orm.DataModel;

public abstract class IssueAssigneeImpl extends EntityImpl implements IssueAssignee {

    private AssigneeType type;

    protected IssueAssigneeImpl(DataModel dataModel, AssigneeType type) {
        super(dataModel);
        this.type = type;
    }

    @Override
    public String getType(){
        return type.getType();
    }

    public void applyAssigneeToIssue(IssueImpl issue){
        type.applyAssigneeToIssue(issue, this);
    }
}