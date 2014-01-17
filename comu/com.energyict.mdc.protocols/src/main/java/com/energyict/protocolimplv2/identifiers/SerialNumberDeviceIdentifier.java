package com.energyict.protocolimplv2.identifiers;

import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.NotFoundException;
import com.energyict.mdc.protocol.api.device.Channel;
import com.energyict.mdc.protocol.api.device.Device;
import com.energyict.mdc.protocol.api.device.DeviceFactory;
import com.energyict.mdc.protocol.api.device.LoadProfile;
import com.energyict.mdc.protocol.api.device.Register;
import com.energyict.mdc.protocol.api.device.offline.DeviceOfflineFlags;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceContext;
import com.energyict.mdc.protocol.api.exceptions.CommunicationException;
import com.energyict.mdc.protocol.api.exceptions.DuplicateException;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;
import com.energyict.mdc.protocol.api.inbound.FindMultipleDevices;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides an implementation for the {@link DeviceIdentifier} interface
 * that uses an {@link Device}'s serial number to uniquely identify it.
 * <br/><br/>
 * <b>NOTE:</b> It is strongly advised to use the {@link DeviceIdentifierById} instead of this one.
 * The SerialNumber of a device doesn't have to be unique. If this identifier finds more than one
 * device with the same serialNumber, then a {@link NotFoundException} will be thrown indicating that.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-12 (11:16)
 */
public class SerialNumberDeviceIdentifier implements DeviceIdentifier, FindMultipleDevices {

    private final String serialNumber;
    private Device device;
    private List<Device<Channel, LoadProfile<Channel>, Register>> allDevices;

    public SerialNumberDeviceIdentifier (String serialNumber) {
        super();
        this.serialNumber = serialNumber;
    }

    @Override
    public Device findDevice () {
        //lazyload the device
        if (this.device == null) {
            fetchAllDevices();
            if (this.allDevices.isEmpty()) {
                throw new NotFoundException("Device with serialnumber " + this.serialNumber + " not found");
            }
            else {
                if (this.allDevices.size() > 1) {
                    throw DuplicateException.duplicateFoundFor(Device.class, this.toString());
                }
                else {
                    this.device = this.allDevices.get(0);
                }
            }
        }
        return this.device;
    }

    private void fetchAllDevices () {
        this.allDevices = this.getDeviceFactory().findDevicesBySerialNumber(this.serialNumber);
    }

    private DeviceFactory getDeviceFactory() {
        List<DeviceFactory> factories = Environment.DEFAULT.get().getApplicationContext().getModulesImplementing(DeviceFactory.class);
        if (factories.isEmpty()) {
            throw CommunicationException.missingModuleException(DeviceFactory.class);
        }
        else {
            return factories.get(0);
        }
    }

    @Override
    public boolean equals (Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SerialNumberDeviceIdentifier that = (SerialNumberDeviceIdentifier) o;
        return serialNumber.equals(that.serialNumber);
    }

    @Override
    public int hashCode () {
        return serialNumber.hashCode();
    }

    @Override
    public String toString () {
        return "serial number " + this.serialNumber;
    }

    @Override
    public String getIdentifier () {
        return serialNumber;
    }

    @Override
    public List<OfflineDevice> getAllDevices () {
        if (this.allDevices == null) {
            fetchAllDevices();
        }
        List<OfflineDevice> allOfflineDevices = new ArrayList<>();
        OfflineDeviceContext offlineDeviceContext = new DeviceOfflineFlags();
        for (Device deviceToGoOffline : this.allDevices) {
            OfflineDevice offline = (OfflineDevice) deviceToGoOffline.goOffline(offlineDeviceContext);
            allOfflineDevices.add(offline);
        }
        return allOfflineDevices;
    }
}