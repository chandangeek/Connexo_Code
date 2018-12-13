/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.customtask.impl;

import com.elster.jupiter.customtask.CustomTaskService;
import com.elster.jupiter.messaging.DestinationSpec;

interface ICustomTaskService extends CustomTaskService {

    DestinationSpec getDestination(String taskType);
}
