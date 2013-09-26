package com.energyict.protocolimplv2.identifiers;

import com.energyict.cbo.NotFoundException;
import com.energyict.comserver.exceptions.DuplicateException;
import com.energyict.cpo.OfflineDeviceContext;
import com.energyict.mdc.protocol.inbound.ServerDeviceIdentifier;
import com.energyict.mdw.core.Device;
import com.energyict.mdw.coreimpl.DeviceOfflineFlags;
import com.energyict.mdw.offline.OfflineDevice;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides an implementation for the {@link com.energyict.mdc.protocol.inbound.DeviceIdentifier} interface
 * that uses an {@link com.energyict.mdw.core.Device}'s Call Home ID to uniquely identify it.
 *
 * @author: sva
 * @since: 26/10/12 (11:26)
 */
public class DialHomeIdDeviceIdentifier implements ServerDeviceIdentifier {

    private final String callHomeID;
    private Device device;
    private List<Device> allDevices;

    public DialHomeIdDeviceIdentifier(String callHomeId) {
        super();
        this.callHomeID = callHomeId;
    }

    @Override
    public Device findDevice() {
        if (this.device == null) {
            fetchAllDevices();
            if (this.allDevices.isEmpty()) {                    //TODO: just a commit to fix broken build - but still need to implement real behavior!
                throw new NotFoundException("Device with callHomeID " + this.callHomeID + " not found");
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
        //        List<Device> devicesByDialHomeId = MeteringWarehouse.getCurrent().getDeviceFactory().findByDialHomeId(this.callHomeID);
        this.allDevices = new ArrayList<>(0);  // TODO: warning - API call no longer exists (cause DialHomeId is no longer managed by device)
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