/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.autoreschedule.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.tasks.ComTask;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-01-22 (13:42)
 */
public class VetoDeleteComTaskException extends LocalizedException {

    public VetoDeleteComTaskException(Thesaurus thesaurus, ComTask comTask) {
        super(thesaurus, MessageSeeds.VETO_COMTASK_DELETION, comTask.getName());
    }
}