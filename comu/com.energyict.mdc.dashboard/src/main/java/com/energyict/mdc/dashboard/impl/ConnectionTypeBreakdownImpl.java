package com.energyict.mdc.dashboard.impl;

import com.energyict.mdc.dashboard.ConnectionTypeBreakdown;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;

/**
 * Provides an implementation for the {@link ConnectionTypeBreakdown} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-30 (13:50)
 */
public class ConnectionTypeBreakdownImpl extends TaskStatusBreakdownCountersImplWithLowercaseD<ConnectionTypePluggableClass> implements ConnectionTypeBreakdown {
    public ConnectionTypeBreakdownImpl() {
        super();
    }
}