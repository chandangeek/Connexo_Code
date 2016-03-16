package com.energyict.mdc.engine.impl.monitor;

import com.energyict.mdc.engine.monitor.InboundComPortOperationalStatistics;

/**
 * Exposes the information that is gathered by the process
 * that monitors a ComPortListener.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-04-03 (13:03)
 */
public interface InboundComPortMBean {

    InboundComPortOperationalStatistics getOperationalStatistics ();

}