/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.tasks;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface NextRecurrentTask {

    RecurrentTask getRecurrentTask();

    RecurrentTask getNextRecurrentTask();

    long getId();

    void save();

    void delete();
}
