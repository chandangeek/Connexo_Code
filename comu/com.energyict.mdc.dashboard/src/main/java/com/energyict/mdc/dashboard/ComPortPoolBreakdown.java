package com.energyict.mdc.dashboard;

import com.energyict.mdc.engine.config.ComPortPool;

import aQute.bnd.annotation.ProviderType;

/**
 * Models the break down of the execution of tasks by {@link ComPortPool}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-30 (09:30)
 */
@ProviderType
public interface ComPortPoolBreakdown extends TaskStatusBreakdownCounters<ComPortPool> {
}