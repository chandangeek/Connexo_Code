package com.energyict.mdc.engine.monitor;

/**
 * Exposes the information that is gathered by the process
 * that monitors a ScheduledComPort.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-04-03 (13:03)
 */
public interface OutboundComPortMBean {

    public OutboundComPortOperationalStatistics getOperationalStatistics ();

}