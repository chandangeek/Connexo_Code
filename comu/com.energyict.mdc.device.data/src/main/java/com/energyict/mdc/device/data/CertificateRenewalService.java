/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data;

import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskOccurrence;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface CertificateRenewalService {
    String COMPONENT_NAME = "CRN";

    RecurrentTask getTask();

    TaskOccurrence runNow();
}
