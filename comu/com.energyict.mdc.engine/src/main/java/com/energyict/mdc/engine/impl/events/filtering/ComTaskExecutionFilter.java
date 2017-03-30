/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.events.filtering;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.events.ComServerEvent;
import com.energyict.mdc.engine.events.ComTaskExecutionEvent;

import java.util.List;

/**
 * Provides an implementation for the {@link EventFilterCriterion} interface
 * that will filter {@link ComServerEvent}s when they do not relate
 * to a number of {@link ComTaskExecution}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-31 (14:27)
 */
public class ComTaskExecutionFilter implements EventFilterCriterion {

    private List<ComTaskExecution> comTaskExecutions;

    public ComTaskExecutionFilter (List<ComTaskExecution> comTaskExecutions) {
        super();
        this.comTaskExecutions = comTaskExecutions;
    }

    public List<ComTaskExecution> getComTaskExecutions () {
        return comTaskExecutions;
    }

    public void setComTaskExecutions (List<ComTaskExecution> comTaskExecutions) {
        this.comTaskExecutions = comTaskExecutions;
    }

    @Override
    public boolean matches (ComServerEvent event) {
        if (event instanceof ComTaskExecutionEvent) {
            ComTaskExecutionEvent comTaskExecutionEvent = (ComTaskExecutionEvent) event;
            return !this.comTaskExecutions.stream().anyMatch(comTaskExecution -> comTaskExecution.getId() == comTaskExecutionEvent.getComTaskExecution().getId());
        }
        else {
            return false;
        }
    }

}