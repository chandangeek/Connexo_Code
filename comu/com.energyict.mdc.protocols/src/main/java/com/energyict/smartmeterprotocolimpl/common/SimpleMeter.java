package com.energyict.smartmeterprotocolimpl.common;

import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * A <CODE>SimpleMeter</CODE> provides general information about an actual meter.
 * This can either be a Master or a Slave, or a 'Smart' or a 'Dumb' meter
 * <p/>
 * <pre>
 * Copyrights EnergyICT
 * Date: 2-mrt-2011
 * Time: 10:36:40
 * <pre>
 */
public interface SimpleMeter {

    /**
     * Return the DeviceTimeZone
     *
     * @return the DeviceTimeZone
     */
    TimeZone getTimeZone();

    /**
     * Getter for the used Logger
     *
     * @return the Logger
     */
    Logger getLogger();

    /**
     * The serialNumber of the meter
     *
     * @return the serialNumber of the meter
     */
    String getSerialNumber();

    /**
     * Get the physical address of the Meter. Mostly this will be an index of the meterList 
     *
     * @return the physical Address of the Meter.
     */
    int getPhysicalAddress();
}
