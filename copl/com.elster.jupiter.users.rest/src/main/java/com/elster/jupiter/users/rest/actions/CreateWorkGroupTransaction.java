/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users.rest.actions;


import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.WorkGroup;
import com.elster.jupiter.users.rest.WorkGroupInfo;

public class CreateWorkGroupTransaction extends UpdateMembershipToWorkGroup implements Transaction<WorkGroup> {

    public CreateWorkGroupTransaction(WorkGroupInfo workGroupInfo, UserService userService){
        super(workGroupInfo, userService);
    }

    @Override
    public WorkGroup perform() {
        WorkGroup workGroup = userService.createWorkGroup(info.name, info.description);
        updateMemberships(workGroup);
        return workGroup;
    }
}
