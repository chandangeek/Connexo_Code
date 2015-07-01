package com.energyict.smartmeterprotocolimpl.nta.dsmr40.landisgyr.E35C;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.obis.ObisCode;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractSmartNtaProtocol;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.topology.MeterTopology;

/**
 * Implementation of MeterTopology made for E35C<br/>
 * Note: Current implementation does not support readout of MBus devices
 *
 * @author sva
 * @since 22/06/2015 - 15:17
 */
public class E35CMeterTopology extends MeterTopology {


    public E35CMeterTopology(AbstractSmartNtaProtocol protocol) {
        super(protocol);
    }

    @Override
    public void searchForSlaveDevices() throws ConnectionException {
        // Nothing to do - current version of E35C protocol only supports readout of E-meter
    }

    @Override
    public int getPhysicalAddress(String serialNumber) {
        return getProtocol().getPhysicalAddress();
    }

    @Override
    public String getSerialNumber(ObisCode obisCode) {
        return getProtocol().getSerialNumber();
    }
}