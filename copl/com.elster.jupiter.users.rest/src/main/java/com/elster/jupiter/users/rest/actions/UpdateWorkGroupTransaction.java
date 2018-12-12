/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users.rest.actions;


import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.WorkGroup;
import com.elster.jupiter.users.rest.WorkGroupInfo;

public class UpdateWorkGroupTransaction extends UpdateMembershipToWorkGroup implements Transaction<WorkGroup>{

    private final ConcurrentModificationExceptionFactory conflictFactory;

    public UpdateWorkGroupTransaction(WorkGroupInfo info, UserService userService, ConcurrentModificationExceptionFactory conflictFactory){
        super(info, userService);
        this.conflictFactory = conflictFactory;
    }

    @Override
    public WorkGroup perform() {
        final WorkGroup workGroup = findAndLockGroupByIdAndVersion(info);
        updateMemberships(workGroup);
        if(workGroup.getDescription() == null){
            workGroup.setDescription(info.description);
            workGroup.update();
            return workGroup;
        }else if(!workGroup.getDescription().equals(info.description)){
            workGroup.setDescription(info.description);
            workGroup.update();
            return workGroup;
        }
        workGroup.update();
        return workGroup;
    }

    private WorkGroup findAndLockGroupByIdAndVersion(WorkGroupInfo info) {
        return userService.findAndLockWorkGroupByIdAndVersion(info.id, info.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(info.name)
                        .withActualVersion(() -> userService.getWorkGroup(info.id).map(WorkGroup::getVersion).orElse(null))
                        .supplier());
    }
}
