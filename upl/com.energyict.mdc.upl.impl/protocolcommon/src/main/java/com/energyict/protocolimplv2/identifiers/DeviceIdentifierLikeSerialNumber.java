package com.energyict.protocolimplv2.identifiers;

import com.energyict.mdc.protocol.inbound.ServerDeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.offline.OfflineDevice;

import com.energyict.cpo.OfflineDeviceContext;
import com.energyict.mdw.core.Device;
import com.energyict.mdw.core.DeviceFactory;
import com.energyict.mdw.core.DeviceFactoryProvider;
import com.energyict.mdw.core.DeviceOfflineFlags;
import com.energyict.protocol.exceptions.identifier.DuplicateException;
import com.energyict.protocol.exceptions.identifier.NotFoundException;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides an implementation for the {@link DeviceIdentifier} interface
 * that uses an {@link com.energyict.mdw.core.Device}'s serial number to uniquely identify it.
 * <p/>
 * Wild cards can be used in the serial number.
 * E.g. *012345* matches 660-012345-1245
 * <p/>
 * <b>Be aware that the serialNumber is NOT a unique field in the database.
 * It is possible that multiple devices are found based on the provided SerialNumber.
 * In that case, a {@link DuplicateException} is throw</b>
 * <p/>
 * Copyrights EnergyICT
 * Date: 13/05/13
 * Time: 13:06
 */
@XmlRootElement
public class DeviceIdentifierLikeSerialNumber implements ServerDeviceIdentifier {

    private String serialNumber;
    private Device device;
    private List<Device> allDevices;

    /**
     * Constructor only to be used by JSON (de)marshalling
     */
    private DeviceIdentifierLikeSerialNumber() {
    }

    public DeviceIdentifierLikeSerialNumber(String serialNumber) {
        super();
        this.serialNumber = serialNumber;
    }

    @Override
    public Device findDevice() {
        //lazyload the device
        if (this.device == null) {
            fetchAllDevices();
            if (this.allDevices.isEmpty()) {
                throw NotFoundException.notFound(Device.class, this.toString());
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
        this.allDevices = getDeviceFactory().findLikeSerialNumber(this.serialNumber);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DeviceIdentifierLikeSerialNumber that = (DeviceIdentifierLikeSerialNumber) o;
        return serialNumber.equals(that.serialNumber);
    }

    @Override
    public int hashCode() {
        return serialNumber.hashCode();
    }

    @Override
    public String toString() {
        return "device with serial number like " + this.serialNumber;
    }

    @XmlElement(name = "type")
    public String getXmlType() {
        return this.getClass().getName();
    }

    public void setXmlType(String ignore) {
        // For xml unmarshalling purposes only
    }

    @XmlAttribute
    public String getSerialNumber() {
        return serialNumber;
    }

    @Override
    public com.energyict.mdc.upl.meterdata.identifiers.Introspector forIntrospection() {
        return new Introspector();
    }

    @Override
    public List<OfflineDevice> getAllDevices() {
        if (this.allDevices == null) {
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

    private class Introspector implements com.energyict.mdc.upl.meterdata.identifiers.Introspector {
        @Override
        public String getTypeName() {
            return "LikeSerialNumber";
        }

        @Override
        public Object getValue(String role) {
            if ("serialNumberGrepPattern".equals(role)) {
                return serialNumber;
            } else {
                throw new IllegalArgumentException("Role '" + role + "' is not supported by identifier of type " + getTypeName());
            }
        }

    }

}
