/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.tasks;

import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.logging.LogEntry;

import java.util.List;

/**
 * Created by igh on 5/11/2015.
 */
public interface TaskFinder {

    public List<? extends RecurrentTask> find();

}
