package com.energyict.mdc.scheduling;

import aQute.bnd.annotation.ProviderType;

import java.util.Calendar;
import java.util.Date;

/**
 * Calculates the timestamp of the next execution of a task
 * based on the last execution of that task.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-11 (17:47)
 */
@ProviderType
public interface NextExecutionCalculator {

    /**
     * Calculates the next execution timestamp of a task
     * based on the last execution of that task.
     *
     * @param lastExecution The last execution timestamp of the task
     * @return The next execution timestamp of the task
     */
    public Date getNextTimestamp(Calendar lastExecution);

}