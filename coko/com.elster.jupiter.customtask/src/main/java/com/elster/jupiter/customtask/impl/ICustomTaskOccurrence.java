/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.customtask.impl;

import com.elster.jupiter.customtask.CustomTaskOccurrence;
import com.elster.jupiter.tasks.TaskOccurrence;

interface ICustomTaskOccurrence extends CustomTaskOccurrence {

    void persist();

    ICustomTask getTask();

    TaskOccurrence getTaskOccurrence();
}
