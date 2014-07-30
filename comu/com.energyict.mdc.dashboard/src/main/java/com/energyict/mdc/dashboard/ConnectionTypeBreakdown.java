package com.energyict.mdc.dashboard;

import com.energyict.mdc.protocol.api.ConnectionType;

/**
 * Models the break down of the execution of tasks by {@link ConnectionType}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-30 (09:30)
 */
public interface ConnectionTypeBreakdown extends TaskStatusBreakDownCounters<ConnectionType> {
}