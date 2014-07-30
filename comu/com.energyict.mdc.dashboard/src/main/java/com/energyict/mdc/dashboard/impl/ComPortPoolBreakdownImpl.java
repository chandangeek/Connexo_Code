package com.energyict.mdc.dashboard.impl;

import com.energyict.mdc.dashboard.ComPortPoolBreakdown;
import com.energyict.mdc.dashboard.TaskStatusBreakdownCounter;
import com.energyict.mdc.engine.model.ComPortPool;

/**
 * Provides an implementation for the {@link ComPortPoolBreakdown} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-30 (12:00)
 */
public class ComPortPoolBreakdownImpl extends TaskStatusBreakdownCountersImpl<ComPortPool> implements ComPortPoolBreakdown {
    public ComPortPoolBreakdownImpl() {
        super();
    }
}