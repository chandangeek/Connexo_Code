package com.elster.jupiter.issue.impl.records;

import com.elster.jupiter.issue.impl.records.assignee.types.AssigneeTypes;
import com.elster.jupiter.issue.share.entity.IssueAssignee;
import com.elster.jupiter.orm.DataModel;

public abstract class IssueAssigneeImpl extends EntityImpl implements IssueAssignee {

    private AssigneeTypes type;

    protected IssueAssigneeImpl(DataModel dataModel, AssigneeTypes type) {
        super(dataModel);
        this.type = type;
    }

    @Override
    public String getType(){
        return type.getType();
    }

    public void applyAssigneeToIssue(BaseIssueImpl issue){
        type.applyAssigneeToIssue(issue, this);
    }
}