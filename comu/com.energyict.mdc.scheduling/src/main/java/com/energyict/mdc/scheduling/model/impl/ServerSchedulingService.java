/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.scheduling.model.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.tasks.ComTask;

import java.util.List;

/**
 * Adds behavior to the {@link SchedulingService}
 * that is specific to server side components.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-01-22 (08:44)
 */
public interface ServerSchedulingService extends SchedulingService {

    public Thesaurus getThesaurus();

    /**
     * Finds the {@link ComSchedule}s that are using
     * the specified {@link ComTask}.
     *
     * @param comTask The ComTask
     * @return The List of ComSchedule
     */
    public List<ComSchedule> findComSchedulesUsing(ComTask comTask);
}