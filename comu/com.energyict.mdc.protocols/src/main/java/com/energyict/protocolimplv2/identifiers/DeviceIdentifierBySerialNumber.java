package com.energyict.protocolimplv2.identifiers;

import com.energyict.mdc.common.NotFoundException;
import com.energyict.mdc.protocol.api.device.BaseChannel;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.device.BaseRegister;
import com.energyict.mdc.protocol.api.device.BaseLoadProfile;
import com.energyict.mdc.protocol.api.exceptions.DuplicateException;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifierType;
import com.energyict.mdc.protocol.api.device.data.identifiers.FindMultipleDevices;

import com.energyict.protocols.mdc.services.impl.MessageSeeds;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Provides an implementation for the {@link DeviceIdentifier} interface
 * that uses an {@link com.energyict.mdc.protocol.api.device.BaseDevice}'s serial number to uniquely identify it.
 * <b>Be aware that the serialNumber is NOT a unique field in the database.
 * It is possible that multiple devices are found based on the provided SerialNumber.
 * In that case, a {@link DuplicateException} is throw</b>
 *
 * Copyrights EnergyICT
 * Date: 13/05/13
 * Time: 13:06
 */
@XmlRootElement
public class DeviceIdentifierBySerialNumber implements DeviceIdentifier, FindMultipleDevices<BaseDevice<BaseChannel,BaseLoadProfile<BaseChannel>,BaseRegister>> {

    private String serialNumber;
    private BaseDevice<?,?,?> device;
    private List<BaseDevice<BaseChannel, BaseLoadProfile<BaseChannel>, BaseRegister>> allDevices;

    public DeviceIdentifierBySerialNumber(String serialNumber) {
        super();
        this.serialNumber = serialNumber;
    }

    @Override
    public BaseDevice<? extends BaseChannel, ? extends BaseLoadProfile<? extends BaseChannel>, ? extends BaseRegister> findDevice() {
        //lazyload the device
        if (this.device == null) {
            fetchAllDevices();
            if (this.allDevices.isEmpty()) {
                throw new NotFoundException("Device with serialnumber " + this.serialNumber + " not found");
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

    private void fetchAllDevices() {
        List<BaseDevice<BaseChannel, BaseLoadProfile<BaseChannel>, BaseRegister>> allDevices = new ArrayList<>();
        // TODO can't fetch them for now ...
//        List<DeviceFactory> deviceFactories = Environment.DEFAULT.get().getApplicationContext().getModulesImplementing(DeviceFactory.class);
//        for (DeviceFactory deviceFactory : deviceFactories) {
//            allDevices.addAll(deviceFactory.findDevicesBySerialNumber(this.getDescription()));
//        }
        this.allDevices = allDevices;
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
        return this.getIdentifier().equals(that.getIdentifier());
    }

    @Override
    public int hashCode () {
        return this.getIdentifier().hashCode();
    }

    @Override
    public String toString () {
        return "device with serial number " + this.getIdentifier();
    }

    @Override
    public String getIdentifier() {
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
    public List<BaseDevice<BaseChannel, BaseLoadProfile<BaseChannel>, BaseRegister>> getAllDevices() {
        if (this.allDevices == null) {
            return Collections.emptyList();
        }
        return this.allDevices;
    }

}
