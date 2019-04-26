/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.tasks;

import com.energyict.mdc.tasks.security.Privileges;

import java.util.Optional;

public enum ComTaskUserAction {

    EXECUTE_SCHEDULE_PLAN_COM_TASK_1(Privileges.Constants.EXECUTE_SCHEDULE_PLAN_COM_TASK_1),
    EXECUTE_SCHEDULE_PLAN_COM_TASK_2(Privileges.Constants.EXECUTE_SCHEDULE_PLAN_COM_TASK_2),
    EXECUTE_SCHEDULE_PLAN_COM_TASK_3(Privileges.Constants.EXECUTE_SCHEDULE_PLAN_COM_TASK_3),
    EXECUTE_SCHEDULE_PLAN_COM_TASK_4(Privileges.Constants.EXECUTE_SCHEDULE_PLAN_COM_TASK_4);

    private final String privilege;

    private ComTaskUserAction(String privilege) {
        this.privilege = privilege;
    }

    public String getPrivilege() {
        return privilege;
    }

    public static Optional<ComTaskUserAction> forPrivilege(String privilege) {
        for (ComTaskUserAction userAction : values()) {
            if (userAction.getPrivilege().equals(privilege)) {
                return Optional.of(userAction);
            }
        }
        return Optional.empty();
    }
}
