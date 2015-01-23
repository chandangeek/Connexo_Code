package com.energyict.protocolimplv2.dlms;

import com.energyict.mdc.meterdata.CollectedTopology;
import com.energyict.obis.ObisCode;

/**
 * Object responsible for building/managing of the device topology;<br/>
 * During this process all attached MBus devices will be discovered
 *
 * @author sva
 * @since 23/01/2015 - 10:10
 */
public abstract class AbstractMeterTopology {

    /**
     * Search for the serialNumber of the meter which corresponds with the B-Field of the given ObisCode
     *
     * @param obisCode the ObisCode
     * @return the serialNumber of the meter which corresponds with the B-field of the ObisCode
     */
    public abstract String getSerialNumber(ObisCode obisCode);

    /**
     * Search for the physicalAddress of the meter with the given serialNumber
     *
     * @param serialNumber the serialNumber of the meter
     * @return the requested physical address or -1 when it could not be found
     */
    public abstract int getPhysicalAddress(String serialNumber);


    /**
     * Search for local slave devices so a general topology can be build up<br/>
     * Thus in other words: discover all attached slave devices
     */
    public abstract void searchForSlaveDevices();

    /**
     * Returns the actual device topology (which should be the master device and a number of attached slave devices).
     * <p/>
     * <b>Warning:</b> this method should only be called after the actual device topology is read out, or in other words
     * after method #searchForSlaveDevices() has been called!
     *
     * @return the current Topology
     */
    public abstract CollectedTopology getDeviceTopology();

    /**
     * Return a B-Field corrected ObisCode.
     *
     * @param obisCode     the ObisCode to correct
     * @param serialNumber the serialNumber of the device for which this ObisCode must be corrected
     * @return the corrected ObisCode
     */
    public abstract ObisCode getPhysicalAddressCorrectedObisCode(ObisCode obisCode, String serialNumber);
}