/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.scheduling.model.impl;

import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.tasks.ComTask;

/**
 * Created with IntelliJ IDEA.
 * User: bvn
 * Date: 4/18/14
 * Time: 9:39 AM
 * To change this template use File | Settings | File Templates.
 */
public interface ComTaskInComSchedule {

    ComSchedule getComSchedule();

    void setComSchedule(ComSchedule comSchedule);

    ComTask getComTask();

    void setComTask(ComTask comTask);
}
