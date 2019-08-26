/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard;

import com.energyict.mdc.common.comserver.ComPortPool;

import aQute.bnd.annotation.ProviderType;

/**
 * Models the {@link ConnectionTaskHeatMap} for {@link ComPortPool}s,
 * providing the overview of which ComPortPool has
 * the most broken connections,...
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-08-04 (17:02)
 */
@ProviderType
public interface ComPortPoolHeatMap extends ConnectionTaskHeatMap<ComPortPool> {
}