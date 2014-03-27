package com.elster.jupiter.issue.rest.response;

import com.elster.jupiter.issue.share.entity.AssigneeRole;
import com.elster.jupiter.issue.share.entity.AssigneeTeam;
import com.elster.jupiter.issue.share.entity.IssueAssigneeType;
import com.elster.jupiter.users.User;

import java.util.ArrayList;
import java.util.List;

public class AssigneeFilterListInfo {
    private static final String UNASSIGNED_TITLE = "Unassigned";
    private static final String ME_TITLE = "Me";

    private List<IssueAssigneeInfo> data;

    public AssigneeFilterListInfo() {
        data = new ArrayList<>();
    }

    public AssigneeFilterListInfo(List<AssigneeTeam> teamList, List<AssigneeRole> roleList, List<User> userList) {
        this();
        for (AssigneeTeam team : teamList) {
            data.add(new IssueAssigneeInfo(IssueAssigneeType.TEAM.getType(), team.getId(), team.getName()));
        }
        for (AssigneeRole role : roleList) {
            data.add(new IssueAssigneeInfo(IssueAssigneeType.ROLE.getType(), role.getId(), role.getName()));
        }
        for (User user : userList) {
            data.add(new IssueAssigneeInfo(IssueAssigneeType.USER.getType(), user.getId(), user.getName()));
        }
    }
    public List<IssueAssigneeInfo> getData() {
        return data;
    }

    public void setData(List<IssueAssigneeInfo> assignees) {
        this.data = assignees;
    }


    public static AssigneeFilterListInfo defaults(User currentUser) {
        AssigneeFilterListInfo info = new AssigneeFilterListInfo();
        // Adding 'Unassigned'
        info.data.add(new IssueAssigneeInfo("UnexistingType", -1L, UNASSIGNED_TITLE));
        if (currentUser != null) {
            // Adding 'Me'
            info.data.add(new IssueAssigneeInfo(IssueAssigneeType.USER.getType(), currentUser.getId(), ME_TITLE));
        }
        return info;
    }
}
