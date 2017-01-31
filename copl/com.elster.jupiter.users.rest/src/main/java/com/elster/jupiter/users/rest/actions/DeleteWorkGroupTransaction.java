/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users.rest.actions;


import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.WorkGroup;
import com.elster.jupiter.users.rest.WorkGroupInfo;

import java.util.Optional;

public class DeleteWorkGroupTransaction extends VoidTransaction{

    private final WorkGroupInfo info;
    private final UserService userService;

    public DeleteWorkGroupTransaction(WorkGroupInfo info, UserService userService){
        this.info = info;
        this.userService = userService;
    }

    @Override
    protected void doPerform() {
        Optional<WorkGroup> workGroup = userService.findAndLockWorkGroupByIdAndVersion(info.id, info.version);
        if(workGroup.isPresent()){
            workGroup.get().delete();
        }
    }

}
