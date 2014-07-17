package com.energyict.mdc.engine.impl.monitor;

/**
 * Exposes the information that is gathered by the process
 * that monitors a ScheduledComPort.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-17 (09:43)
 */
public interface ScheduledComPortMonitor {

    public OperationalStatistics getOperationalStatistics();

}