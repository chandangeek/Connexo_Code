/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kore.api.v2.issue;
import com.elster.jupiter.issue.share.entity.IssueAssignee;

public class IssueAssigneeInfoFactory {

    public IssueAssigneeInfo asInfo(IssueAssignee assignee){
        IssueAssigneeInfo info = new IssueAssigneeInfo();
        if(assignee.getUser() != null) {
            info.name = assignee.getUser().getName();
            info.id = assignee.getUser().getId();
        }
        return info;
    }

    public IssueAssigneeInfo asInfo(String type, IssueAssignee assignee){
        IssueAssigneeInfo info = new IssueAssigneeInfo();
        if(type.equals("WORKGROUP")){
            if(assignee.getWorkGroup() != null) {
                info.name = assignee.getWorkGroup().getName();
                info.id = assignee.getWorkGroup().getId();
            }
        }else if(type.equals("USER")){
            if(assignee.getUser() != null) {
                info.name = assignee.getUser().getName();
                info.id = assignee.getUser().getId();
            }
        }
        return info;
    }

    public IssueAssigneeInfo asInfo(String type, Long id, String name) {
        IssueAssigneeInfo info = new IssueAssigneeInfo();
        info.type = type;
        info.id = id;
        info.name = name;
        return info;
    }
}
