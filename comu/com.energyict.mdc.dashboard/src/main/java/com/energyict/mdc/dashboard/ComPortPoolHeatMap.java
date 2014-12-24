package com.energyict.mdc.dashboard;

import com.energyict.mdc.engine.config.ComPortPool;

/**
 * Models the {@link ConnectionTaskHeatMap} for {@link ComPortPool}s,
 * providing the overview of which ComPortPool has
 * the most broken connections,...
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-08-04 (17:02)
 */
public interface ComPortPoolHeatMap extends ConnectionTaskHeatMap<ComPortPool> {
}