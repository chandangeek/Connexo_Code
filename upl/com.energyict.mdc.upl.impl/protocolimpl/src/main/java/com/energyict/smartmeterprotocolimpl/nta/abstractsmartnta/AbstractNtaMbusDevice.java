package com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta;

import com.energyict.mdw.core.Pluggable;
import com.energyict.smartmeterprotocolimpl.common.SimpleMeter;

import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 15-jul-2011
 * Time: 10:31:14
 */
public abstract class AbstractNtaMbusDevice implements SimpleMeter, Pluggable {

    private final AbstractSmartNtaProtocol meterProtocol;
    private final String serialNumber;
    private final int physicalAddress;

    public AbstractNtaMbusDevice(final AbstractSmartNtaProtocol meterProtocol, final String serialNumber, final int physicalAddress) {
        this.meterProtocol = meterProtocol;
        this.serialNumber = serialNumber;
        this.physicalAddress = physicalAddress;
    }

    /**
     * Return the DeviceTimeZone
     *
     * @return the DeviceTimeZone
     */
    public TimeZone getTimeZone() {
        return this.meterProtocol.getTimeZone();
    }

    /**
     * Getter for the used Logger
     *
     * @return the Logger
     */
    public Logger getLogger() {
        return this.meterProtocol.getLogger();
    }

    /**
     * The serialNumber of the meter
     *
     * @return the serialNumber of the meter
     */
    public String getSerialNumber() {
        return this.serialNumber;
    }

    /**
     * Get the physical address of the Meter. Mostly this will be an index of the meterList
     *
     * @return the physical Address of the Meter.
     */
    public int getPhysicalAddress() {
        return this.physicalAddress;
    }
}
