package com.elster.jupiter.issue.rest.response;

import com.elster.jupiter.issue.share.entity.IssueAssignee;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.users.User;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.elster.jupiter.issue.rest.TranslationKeys.ISSUE_ASSIGNEE_UNASSIGNED;

public class AssigneeFilterListInfo {
    private List<IssueAssigneeInfo> data = new ArrayList<>();

    public AssigneeFilterListInfo() {
    }

    public AssigneeFilterListInfo(List<User> userList) {
        data.addAll(userList.stream().map(user -> new IssueAssigneeInfo(IssueAssignee.Types.USER, user.getId(), user.getName())).collect(Collectors.toList()));
    }

    public AssigneeFilterListInfo addData(List<User> userList) {
        data.addAll(userList.stream().map(user -> new IssueAssigneeInfo(IssueAssignee.Types.USER, user.getId(), user.getName())).collect(Collectors.toList()));
        return this;
    }

    public List<IssueAssigneeInfo> getData() {
        return data;
    }

    public long getTotal() {
        return data.size();
    }

    public static AssigneeFilterListInfo defaults(User currentUser, Thesaurus thesaurus, boolean findMe) {
        AssigneeFilterListInfo info = new AssigneeFilterListInfo();
        if (currentUser != null && findMe) {
            // Adding 'Me'
            info.data.add(new IssueAssigneeInfo(IssueAssignee.Types.USER, currentUser.getId(), currentUser.getName()));
        }  else {
            // Adding 'Unassigned'
            String unassignedText = thesaurus.getFormat(ISSUE_ASSIGNEE_UNASSIGNED).format();
            info.data.add(new IssueAssigneeInfo("UnexistingType", -1L, unassignedText));
        }
        return info;
    }

}