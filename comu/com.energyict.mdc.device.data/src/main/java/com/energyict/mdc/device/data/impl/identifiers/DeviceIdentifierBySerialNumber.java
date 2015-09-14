package com.energyict.mdc.device.data.impl.identifiers;

import com.energyict.mdc.common.NotFoundException;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.exceptions.CanNotFindForIdentifier;
import com.energyict.mdc.device.data.impl.MessageSeeds;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifierType;
import com.energyict.mdc.protocol.api.device.data.identifiers.FindMultipleDevices;
import com.energyict.mdc.protocol.api.exceptions.DuplicateException;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Provides an implementation for the {@link DeviceIdentifier} interface
 * that uses an {@link com.energyict.mdc.protocol.api.device.BaseDevice}'s serial number to uniquely identify it.
 * <br/><br/>
 * <b>NOTE:</b> It is strongly advised to use the {@link DeviceIdentifierById} instead of this one.
 * The SerialNumber of a device doesn't have to be unique. If this identifier finds more than one
 * device with the same serialNumber, then a {@link NotFoundException} will be thrown indicating that.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-12 (11:16)
 */
@XmlRootElement
public class DeviceIdentifierBySerialNumber implements DeviceIdentifier, FindMultipleDevices<Device> {

    private String serialNumber;
    private DeviceService deviceService;
    private Device device;
    private List<Device> allDevices;

    /**
     * Constructor only to be used by JSON (de)marshalling
     */
    public DeviceIdentifierBySerialNumber() {
    }

    public DeviceIdentifierBySerialNumber(String serialNumber, DeviceService deviceService) {
        super();
        this.serialNumber = serialNumber;
        this.deviceService = deviceService;
    }

    @Override
    public Device findDevice() {
        //lazyload the device
        if (this.device == null) {
            fetchAllDevices();
            if (this.allDevices.isEmpty()) {
                throw CanNotFindForIdentifier.device(this, MessageSeeds.CAN_NOT_FIND_FOR_DEVICE_IDENTIFIER);
            }
            else {
                if (this.allDevices.size() > 1) {
                    throw new DuplicateException(MessageSeeds.DUPLICATE_FOUND, BaseDevice.class, this.toString());
                }
                else {
                    this.device = this.allDevices.get(0);
                }
            }
        }
        return this.device;
    }

    private void fetchAllDevices () {
        this.allDevices = this.deviceService.findDevicesBySerialNumber(this.serialNumber);
    }

    @Override
    public boolean equals (Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DeviceIdentifierBySerialNumber that = (DeviceIdentifierBySerialNumber) o;
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
    public DeviceIdentifierType getDeviceIdentifierType() {
        return DeviceIdentifierType.SerialNumber;
    }

    @XmlElement(name = "type")
    public String getXmlType() {
        return this.getClass().getName();
    }

    public void setXmlType(String ignore) {
        // For xml unmarshalling purposes only
    }

    @Override
    public List<Device> getAllDevices() {
        if (this.allDevices == null) {
            fetchAllDevices();
        }
        return this.allDevices;
    }
}