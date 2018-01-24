package com.energyict.mdc.device.data;

import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskOccurrence;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface KeyRenewalService {
    String COMPONENT_NAME = "KRN";

    RecurrentTask getTask();

    TaskOccurrence runNow();
}
