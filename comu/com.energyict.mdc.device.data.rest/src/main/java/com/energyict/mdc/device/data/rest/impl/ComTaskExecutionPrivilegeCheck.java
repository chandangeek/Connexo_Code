/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.User;

import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.ComTaskUserAction;

import java.util.List;
import java.util.stream.Collectors;

public class ComTaskExecutionPrivilegeCheck {

    public boolean canExecute(ComTask comTask, User user) {
        List<String> userActionNames = comTask.getUserActions().stream().map(ComTaskUserAction::getPrivilege)
                .collect(Collectors.toList());
        return user.getPrivileges().stream().map(Privilege::getName).filter(userActionNames::contains).findAny()
                .isPresent();
    }
}
