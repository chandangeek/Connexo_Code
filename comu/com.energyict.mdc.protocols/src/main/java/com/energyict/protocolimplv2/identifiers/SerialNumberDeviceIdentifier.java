package com.energyict.protocolimplv2.identifiers;

import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.NotFoundException;
import com.energyict.mdc.protocol.api.device.BaseChannel;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.device.BaseRegister;
import com.energyict.mdc.protocol.api.device.DeviceFactory;
import com.energyict.mdc.protocol.api.device.BaseLoadProfile;
import com.energyict.mdc.protocol.api.exceptions.CommunicationException;
import com.energyict.mdc.protocol.api.exceptions.DuplicateException;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;
import com.energyict.mdc.protocol.api.inbound.FindMultipleDevices;

import java.util.Collections;
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
public class SerialNumberDeviceIdentifier implements DeviceIdentifier, FindMultipleDevices<BaseDevice<BaseChannel,BaseLoadProfile<BaseChannel>,BaseRegister>> {

    private final String serialNumber;
    private BaseDevice<?,?,?> device;
    private List<BaseDevice<BaseChannel, BaseLoadProfile<BaseChannel>, BaseRegister>> allDevices;

    public SerialNumberDeviceIdentifier (String serialNumber) {
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
                    throw DuplicateException.duplicateFoundFor(BaseDevice.class, this.toString());
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
    public List<BaseDevice<BaseChannel, BaseLoadProfile<BaseChannel>, BaseRegister>> getAllDevices() {
        if (this.allDevices == null) {
            return Collections.emptyList();
        }
        return this.allDevices;
    }
}