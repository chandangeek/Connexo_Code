package com.energyict.smartmeterprotocolimpl.nta.dsmr40.xemex.topology;

import com.energyict.cbo.BusinessException;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.mdw.core.Device;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.smartmeterprotocolimpl.common.topology.DeviceMapping;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractSmartNtaProtocol;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.topology.MeterTopology;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

/**
 * @author sva
 * @since 7/06/13 - 11:19
 */
public class XemexMeterTopology extends MeterTopology {

    public XemexMeterTopology(AbstractSmartNtaProtocol protocol) {
        super(protocol);
    }

    @Override
    protected void discoverMbusDevices() throws ConnectionException {
        log(Level.FINE, "Starting discovery of MBusDevices");
        // get an MbusDeviceMap
        this.mbusMap = getMbusMapper();

        if (this.mbusMap.size() > 0) {
            try {
                // check if all the mbus devices are configured in EIServer
                checkToUpdateMbusMeters(mbusMap);
                checkForDisappearedMbusMeters(mbusMap);
            } finally {
                ProtocolTools.closeConnection();
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Found ").append(this.mbusMap.size()).append(" MBus devices: ").append("\r\n");
        for (DeviceMapping deviceMapping : this.mbusMap) {
            sb.append(deviceMapping).append("\r\n");
        }
        log(Level.INFO, sb.toString());
    }

    private void checkForDisappearedMbusMeters(List<DeviceMapping> mbusMap) {
        Device gatewayDevice = getRtuFromDatabaseBySerialNumber();
        if (gatewayDevice != null) {
            List<Device> mbusSlaves = gatewayDevice.getDownstreamDevices();
            Iterator<Device> it = mbusSlaves.iterator();
            while (it.hasNext()) {
                Device mbus = it.next();
                try {
                    if (!mbusMap.contains(new DeviceMapping(mbus.getSerialNumber()))) {
                        log(Level.INFO, "MbusDevice " + mbus.getSerialNumber() + " is not installed on the physical device - detaching from gateway.");
                        mbus.updateGateway(null);
                    }
                } catch (SQLException e) {
                    log(Level.INFO, "Failed to remove the gateway from MbusDevice " + mbus.getSerialNumber() + ".");
                } catch (BusinessException e) {
                    log(Level.INFO, "Failed to remove the gateway from MbusDevice " + mbus.getSerialNumber() + ".");
                }
            }
        }
    }
}