package com.energyict.protocolimplv2.identifiers;

import com.energyict.mdc.protocol.api.device.BaseRegister;
import com.energyict.mdw.cpo.PropertySpecFactory;
import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.NotFoundException;
import com.energyict.mdc.protocol.api.device.Channel;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.device.DeviceFactory;
import com.energyict.mdc.protocol.api.device.LoadProfile;
import com.energyict.mdc.protocol.api.device.offline.DeviceOfflineFlags;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceContext;
import com.energyict.mdc.protocol.api.exceptions.DuplicateException;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;
import com.energyict.mdc.protocol.api.inbound.FindMultipleDevices;
import com.energyict.mdc.protocol.api.legacy.dynamic.PropertySpec;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides an implementation for the {@link DeviceIdentifier} interface
 * that uses an {@link com.energyict.mdc.protocol.api.device.BaseDevice}'s Call Home ID to uniquely identify it.
 *
 * @author sva
 * @since 26/10/12 (11:26)
 */
public class DialHomeIdDeviceIdentifier implements DeviceIdentifier, FindMultipleDevices {

    public static final String CALL_HOME_ID_PROPERTY_NAME = "callHomeId";
    public static final PropertySpec CALL_HOME_ID_PROPERTY_SPEC = PropertySpecFactory.stringPropertySpec(CALL_HOME_ID_PROPERTY_NAME);

    private final String callHomeID;
    private BaseDevice device;
    private List<BaseDevice<Channel, LoadProfile<Channel>, BaseRegister>> allDevices;

    public DialHomeIdDeviceIdentifier(String callHomeId) {
        super();
        this.callHomeID = callHomeId;
    }

    @Override
    public BaseDevice findDevice() {
        if (this.device == null) {
            fetchAllDevices();
            if (this.allDevices.isEmpty()) {
                throw new NotFoundException("Device with callHomeID " + this.callHomeID + " not found");
            } else {
                if (this.allDevices.size() > 1) {
                    throw DuplicateException.duplicateFoundFor(BaseDevice.class, this.toString());
                } else {
                    this.device = this.allDevices.get(0);
                }
            }
        }
        return this.device;
    }

    private void fetchAllDevices() {
        List<BaseDevice<Channel, LoadProfile<Channel>, BaseRegister>> allDevices = new ArrayList<>();
        List<DeviceFactory> deviceFactories = Environment.DEFAULT.get().getApplicationContext().getModulesImplementing(DeviceFactory.class);
        for (DeviceFactory deviceFactory : deviceFactories) {
            allDevices.addAll(deviceFactory.findDevicesByNotInheritedProtocolProperty(CALL_HOME_ID_PROPERTY_SPEC, this.getIdentifier()));
        }
        this.allDevices = allDevices;
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
        for (BaseDevice deviceToGoOffline : this.allDevices) {
            OfflineDevice offline = (OfflineDevice) deviceToGoOffline.goOffline(offlineDeviceContext);
            allOfflineDevices.add(offline);
        }
        return allOfflineDevices;
    }

}