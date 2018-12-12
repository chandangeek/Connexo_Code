/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users.rest.actions;


import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.WorkGroup;
import com.elster.jupiter.users.rest.WorkGroupInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class UpdateMembershipToWorkGroup {

    final WorkGroupInfo info;
    final UserService userService;

    public UpdateMembershipToWorkGroup(WorkGroupInfo workGroupInfo, UserService userService) {
        this.info = workGroupInfo;
        this.userService = userService;
    }

    public WorkGroup updateMemberships(WorkGroup workGroup){
        List<User> currentUsers = workGroup.getUsersInWorkGroup();
        List<User> targetUsers = getTargetUsers();
        if(targetUsers.equals(currentUsers)){
            return workGroup;
        }
        revoke(workGroup, currentUsers, targetUsers);
        grant(workGroup, currentUsers, targetUsers);
        return workGroup;
    }

    private void revoke(WorkGroup workGroup, List<User> current, List<User> target){
        List<User> toRemove = new ArrayList<>(current);
        toRemove.removeAll(target);
        toRemove.stream().forEach(workGroup::revoke);
    }

    private void grant(WorkGroup workGroup, List<User> current, List<User> target){
        List<User> toAdd = new ArrayList<>(target);
        toAdd.removeAll(current);
        toAdd.stream().forEach(workGroup::grant);
    }

    private List<User> getTargetUsers(){
        return info.users.stream()
                .map(userInfo -> userService.getUser(userInfo.id).orElse(null))
                .filter(user -> user != null)
                .collect(Collectors.toList());

    }
}
