package com.energyict.protocolimplv2.identifiers;

import com.energyict.cbo.NotFoundException;
import com.energyict.comserver.exceptions.DuplicateException;
import com.energyict.cpo.OfflineDeviceContext;
import com.energyict.mdc.protocol.inbound.ServerDeviceIdentifier;
import com.energyict.mdw.core.Device;
import com.energyict.mdw.core.DeviceFactory;
import com.energyict.mdw.core.DeviceFactoryProvider;
import com.energyict.mdw.coreimpl.DeviceOfflineFlags;
import com.energyict.mdw.offline.OfflineDevice;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides an implementation for the {@link com.energyict.mdc.protocol.inbound.DeviceIdentifier} interface
 * that uses a PlaceHolder for a {@link com.energyict.mdw.core.Device}'s serial number to uniquely identify it.
 * <b>Be aware that the serialNumber is NOT a unique field in the database.
 * It is possible that multiple devices are found based on the provided SerialNumber.
 * In that case, a {@link com.energyict.cbo.NotFoundException} is throw</b>
 * <p/>
 * Copyrights EnergyICT
 * Date: 9/3/13
 * Time: 11:45 AM
 */
@XmlRootElement
public class DeviceIdentifierBySerialNumberPlaceHolder implements ServerDeviceIdentifier {

    private final SerialNumberPlaceHolder serialNumberPlaceHolder;
    private Device device;
    private List<Device> allDevices;

    /**
     * Constructor only to be used by JSON (de)marshalling
     */
    public DeviceIdentifierBySerialNumberPlaceHolder() {
        this.serialNumberPlaceHolder = null;
    }

    public DeviceIdentifierBySerialNumberPlaceHolder(SerialNumberPlaceHolder serialNumberPlaceHolder) {
        this.serialNumberPlaceHolder = serialNumberPlaceHolder;
    }

    @Override
    public Device findDevice() {
        // lazy load the device
        if (this.device == null) {
            fetchAllDevices();
            if (this.allDevices.isEmpty()) {
                throw new NotFoundException("Device with serialnumber " + this.serialNumberPlaceHolder.getSerialNumber() + " not found");
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
        this.allDevices = getDeviceFactory().findBySerialNumber(serialNumberPlaceHolder.getSerialNumber());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DeviceIdentifierBySerialNumberPlaceHolder that = (DeviceIdentifierBySerialNumberPlaceHolder) o;
        return serialNumberPlaceHolder.equals(that.serialNumberPlaceHolder);
    }

    @Override
    public int hashCode() {
        return serialNumberPlaceHolder.getSerialNumber().hashCode();
    }

    @Override
    public String toString() {
        return "device with serial number " + serialNumberPlaceHolder.getSerialNumber();
    }

    @XmlElement(name = "type")
    public String getXmlType() {
        return this.getClass().getName();
    }

    public void setXmlType(String ignore) {
        // For xml unmarshalling purposes only
    }

    @XmlAttribute
    public SerialNumberPlaceHolder getSerialNumberPlaceHolder() {
        return serialNumberPlaceHolder;
    }

    @Override
    public String getIdentifier() {
        return serialNumberPlaceHolder.getSerialNumber();
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

    private DeviceFactory getDeviceFactory() {
        return DeviceFactoryProvider.instance.get().getDeviceFactory();
    }
}
