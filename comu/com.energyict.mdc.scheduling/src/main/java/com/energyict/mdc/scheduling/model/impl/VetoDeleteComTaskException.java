/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.scheduling.model.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.tasks.ComTask;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-01-22 (13:42)
 */
public class VetoDeleteComTaskException extends LocalizedException {

    public VetoDeleteComTaskException(Thesaurus thesaurus, ComTask comTask, List<ComSchedule> comSchedules) {
        super(thesaurus, MessageSeeds.VETO_COMTASK_DELETION, comTask.getName(), asString(comSchedules));
    }

    private static String asString(List<ComSchedule> clients) {
        return clients.stream().map(ComSchedule::getName).collect(Collectors.joining(", "));
    }

}