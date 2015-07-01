package com.energyict.mdc.dashboard;

import com.energyict.mdc.scheduling.model.ComSchedule;

import aQute.bnd.annotation.ProviderType;

/**
 * Models the break down of the execution of tasks by {@link ComSchedule}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-30 (10:59)
 */
@ProviderType
public interface ComScheduleBreakdown extends TaskStatusBreakdownCounters<ComSchedule> {
}