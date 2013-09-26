package com.energyict.protocolimplv2.elster.ctr.MTU155.discover;


import com.energyict.cbo.NotFoundException;
import com.energyict.comserver.exceptions.DuplicateException;
import com.energyict.cpo.OfflineDeviceContext;
import com.energyict.mdc.channels.ip.CTRInboundDialHomeIdConnectionType;
import com.energyict.mdc.protocol.inbound.ServerDeviceIdentifier;
import com.energyict.mdw.core.Device;
import com.energyict.mdw.core.DeviceFactory;
import com.energyict.mdw.core.MeteringWarehouse;
import com.energyict.mdw.coreimpl.DeviceOfflineFlags;
import com.energyict.mdw.offline.OfflineDevice;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides an implementation for the {@link com.energyict.mdc.protocol.inbound.DeviceIdentifier} interface,
 * specific for the CTR protocol base (MTU155 and EK155 device types). <br></br>
 * These protocols use a Call Home ID to uniquely identify the calling device.
 *
 * @author: sva
 * @since: 26/10/12 (11:26)
 */
public class CTRDialHomeIdDeviceIdentifier implements ServerDeviceIdentifier {

    public static final String CALL_HOME_ID_PROPERTY_NAME = "callHomeId";

    private final String callHomeID;
    private Device device;
    private List<Device> allDevices;

    public CTRDialHomeIdDeviceIdentifier(String callHomeId) {
        super();
        this.callHomeID = callHomeId;
    }

    @Override
    public Device findDevice() {
        if(this.device == null){
            fetchAllDevices();
            if (this.allDevices.isEmpty()) {
                throw new NotFoundException("Device with callHomeId " + this.callHomeID + " not found");
            } else {
                if (this.allDevices.size() > 1) {
                    throw DuplicateException.duplicateFoundFor(Device.class, this.toString());
                } else {
                    this.device = this.allDevices.get(0);
                }
            }
        }
        return this.device;
    }

    private void fetchAllDevices() {
        DeviceFactory deviceFactory = MeteringWarehouse.getCurrent().getDeviceFactory();
        this.allDevices = deviceFactory.findByConnectionTypeProperty(CTRInboundDialHomeIdConnectionType.class, CALL_HOME_ID_PROPERTY_NAME, callHomeID);
    }

    @Override
    public String toString() {
        return "device with call home id " + this.callHomeID;
    }

    @Override
    public String getIdentifier() {
        return callHomeID;
    }

    @Override
    public List<OfflineDevice> getAllDevices() {
        if(this.allDevices == null){
            fetchAllDevices();
        }
        List<OfflineDevice> allOfflineDevices = new ArrayList<>();
        OfflineDeviceContext offlineDeviceContext = new DeviceOfflineFlags();
        for (Device deviceToGoOffline : this.allDevices) {
            allOfflineDevices.add(deviceToGoOffline.goOffline(offlineDeviceContext));
        }
        return allOfflineDevices;
    }
}