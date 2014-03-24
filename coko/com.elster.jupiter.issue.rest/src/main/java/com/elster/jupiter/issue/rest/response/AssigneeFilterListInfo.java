package com.elster.jupiter.issue.rest.response;

import com.elster.jupiter.issue.share.entity.AssigneeRole;
import com.elster.jupiter.issue.share.entity.AssigneeTeam;
import com.elster.jupiter.issue.share.entity.IssueAssigneeType;
import com.elster.jupiter.users.User;

import java.util.ArrayList;
import java.util.List;

public class AssigneeFilterListInfo {
    private List<IssueAssigneeInfo> data;

    public AssigneeFilterListInfo() {
        data = new ArrayList<>();
    }

    public AssigneeFilterListInfo(List<AssigneeTeam> teamList, List<AssigneeRole> roleList, List<User> userList) {
        this();
        for (AssigneeTeam team : teamList) {
            data.add(new IssueAssigneeInfo(IssueAssigneeType.TEAM.toString(), team.getId(), team.getName()));
        }
        for (AssigneeRole role : roleList) {
            data.add(new IssueAssigneeInfo(IssueAssigneeType.ROLE.name(), role.getId(), role.getName()));
        }
        for (User user : userList) {
            data.add(new IssueAssigneeInfo(IssueAssigneeType.USER.name(), user.getId(), user.getName()));
        }
    }
    public List<IssueAssigneeInfo> getData() {
        return data;
    }

    public void setData(List<IssueAssigneeInfo> assignees) {
        this.data = assignees;
    }


}
