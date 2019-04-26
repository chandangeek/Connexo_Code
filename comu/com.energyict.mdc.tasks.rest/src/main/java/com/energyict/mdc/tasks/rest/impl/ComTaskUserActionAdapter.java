/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tasks.rest.impl;

import com.energyict.mdc.common.rest.MapBasedXmlAdapter;
import com.energyict.mdc.tasks.ComTaskUserAction;

public class ComTaskUserActionAdapter extends MapBasedXmlAdapter<ComTaskUserAction> {

    public ComTaskUserActionAdapter() {
        register("", null);
        this.register(ComTaskUserAction.EXECUTE_SCHEDULE_PLAN_COM_TASK_1);
        this.register(ComTaskUserAction.EXECUTE_SCHEDULE_PLAN_COM_TASK_2);
        this.register(ComTaskUserAction.EXECUTE_SCHEDULE_PLAN_COM_TASK_3);
        this.register(ComTaskUserAction.EXECUTE_SCHEDULE_PLAN_COM_TASK_4);
    }

    private void register(ComTaskUserAction action) {
        this.register(action.getPrivilege(), action);
    }

}