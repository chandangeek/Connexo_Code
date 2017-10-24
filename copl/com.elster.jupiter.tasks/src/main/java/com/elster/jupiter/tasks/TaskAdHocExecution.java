/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.tasks;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;

@ProviderType
public interface TaskAdHocExecution {

    RecurrentTask getRecurrentTask();

    long getId();

    Instant getNextExecution();

    Instant getTriggerTime();

    void save();

    void delete();
}
