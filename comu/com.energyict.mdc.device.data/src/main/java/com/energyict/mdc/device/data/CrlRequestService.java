/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data;

import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskOccurrence;

import aQute.bnd.annotation.ProviderType;

import java.util.Optional;

@ProviderType
public interface CrlRequestService {

    String COMPONENT_NAME = "CRR";

    Optional<RecurrentTask> getTask();

    TaskOccurrence runNow();

}
