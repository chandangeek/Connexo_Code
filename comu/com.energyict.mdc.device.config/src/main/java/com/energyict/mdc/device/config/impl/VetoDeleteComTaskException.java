/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.tasks.ComTask;

public class VetoDeleteComTaskException extends LocalizedException {

    public VetoDeleteComTaskException(Thesaurus thesaurus, ComTask comTask) {
        super(thesaurus, MessageSeeds.VETO_COMTASK_DELETION, comTask.getName());
    }

}