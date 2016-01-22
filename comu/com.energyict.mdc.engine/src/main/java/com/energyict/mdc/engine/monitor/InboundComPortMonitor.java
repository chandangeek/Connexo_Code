package com.energyict.mdc.engine.monitor;

import aQute.bnd.annotation.ProviderType;
import com.energyict.mdc.engine.config.ComPort;

/**
 * Exposes the information that is gathered by the process
 * that monitors a ComPortListener.
 */
@ProviderType
public interface InboundComPortMonitor {

    /**
     * Verifies if this monitors the given comport
     * @param comport to test
     * @return true is this monitor monitors the given port
     */
    boolean isMonitoring(ComPort comport);

    /**
     * The {@link InboundComPortOperationalStatistics} as a result of the monitoring of the Scheduled Comport
     * @return the operational statistics
     */
    InboundComPortOperationalStatistics getOperationalStatistics();

}