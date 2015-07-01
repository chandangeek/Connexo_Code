package com.energyict.mdc.dashboard;

import com.energyict.mdc.tasks.ComTask;

import aQute.bnd.annotation.ProviderType;

/**
 * Models the break down of the execution of tasks by {@link ComTask}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-08-25 (08:27)
 */
@ProviderType
public interface ComTaskBreakdown extends TaskStatusBreakdownCounters<ComTask> {
}