package com.elster.jupiter.issue.rest.response;

import com.elster.jupiter.issue.share.entity.IssueAssignee;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.users.User;

import java.util.ArrayList;
import java.util.List;

import static com.elster.jupiter.issue.rest.MessageSeeds.ISSUE_ASSIGNEE_ME;
import static com.elster.jupiter.issue.rest.MessageSeeds.ISSUE_ASSIGNEE_UNASSIGNED;
import static com.elster.jupiter.issue.rest.MessageSeeds.getString;

public class AssigneeFilterListInfo {
    private List<IssueAssigneeInfo> data = new ArrayList<>();

    public AssigneeFilterListInfo() {
    }

    public AssigneeFilterListInfo(List<User> userList) {
        for (User user : userList) {
            data.add(new IssueAssigneeInfo(IssueAssignee.Types.USER, user.getId(), user.getName()));
        }
    }

    public List<IssueAssigneeInfo> getData() {
        return data;
    }

    public long getTotal() {
        return data.size();
    }


    public static AssigneeFilterListInfo defaults(User currentUser, Thesaurus thesaurus, Boolean findMe) {
        AssigneeFilterListInfo info = new AssigneeFilterListInfo();
        if (currentUser != null && findMe) {
            // Adding 'Me'
            String meText = getString(ISSUE_ASSIGNEE_ME, thesaurus);
            info.data.add(new IssueAssigneeInfo(IssueAssignee.Types.USER, currentUser.getId(), currentUser.getName()));
        }  else {
            // Adding 'Unassigned'
            String unassignedText = getString(ISSUE_ASSIGNEE_UNASSIGNED, thesaurus);
            info.data.add(new IssueAssigneeInfo("UnexistingType", -1L, unassignedText));
        }
        return info;
    }
}
