package com.energyict.mdc.dashboard;

import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;

/**
 * Models the break down of the execution of tasks by {@link ConnectionTypePluggableClass}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-30 (09:30)
 */
public interface ConnectionTypeBreakdown extends TaskStatusBreakdownCountersWithLowercaseD<ConnectionTypePluggableClass> {
}