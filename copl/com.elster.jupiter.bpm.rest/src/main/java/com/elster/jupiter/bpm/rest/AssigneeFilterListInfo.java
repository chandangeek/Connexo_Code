/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bpm.rest;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.users.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.elster.jupiter.bpm.rest.TranslationKeys.TASK_ASSIGNEE_UNASSIGNED;

public class AssigneeFilterListInfo {
    private List<TaskAssigneeInfo> data = new ArrayList<>();

    public AssigneeFilterListInfo() {
    }

    public AssigneeFilterListInfo(List<User> userList) {
        data.addAll(userList.stream().map(user -> new TaskAssigneeInfo("USER", user.getId(), user.getName())).collect(Collectors.toList()));
    }

    public AssigneeFilterListInfo addData(List<User> userList) {
        data.addAll(userList.stream().map(user -> new TaskAssigneeInfo("USER", user.getId(), user.getName())).collect(Collectors.toList()));
        return this;
    }

    public List<TaskAssigneeInfo> getData() {
        return Collections.unmodifiableList(data);
    }

    public long getTotal() {
        return data.size();
    }

    public static AssigneeFilterListInfo defaults(User currentUser, Thesaurus thesaurus, boolean findMe) {
        AssigneeFilterListInfo info = new AssigneeFilterListInfo();
        if (currentUser != null && findMe) {
            info.data.add(new TaskAssigneeInfo("USER", currentUser.getId(), currentUser.getName()));
        }  else {
            String unassignedText = thesaurus.getFormat(TASK_ASSIGNEE_UNASSIGNED).format();
            info.data.add(new TaskAssigneeInfo("UnexistingType", -1L, unassignedText));
        }
        return info;
    }

}