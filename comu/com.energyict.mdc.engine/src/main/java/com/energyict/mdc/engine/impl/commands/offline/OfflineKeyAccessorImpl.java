package com.energyict.mdc.engine.impl.commands.offline;

import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.SecurityValueWrapper;
import com.energyict.mdc.common.device.config.SecurityAccessorTypeOnDeviceType;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.SecurityAccessor;
import com.energyict.mdc.identifiers.*;
import com.energyict.mdc.protocol.api.device.offline.OfflineKeyAccessor;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import java.util.Optional;

public class OfflineKeyAccessorImpl<T extends SecurityValueWrapper> implements OfflineKeyAccessor {

    /**
     * The {@link SecurityAccessor} which is going offline
     */
    private final SecurityAccessor<T> securityAccessor;
    private IdentificationService identificationService;

    private final Device device;
    private SecurityAccessorType securityAccessorType;
    private Optional<T> actualValue;
    private Optional<T> tempValue;
    private int deviceId;
    private String deviceMRID;
    private DeviceIdentifier deviceIdentifier;
    private Optional<T> wrappingKeyActualValue;

    public OfflineKeyAccessorImpl(SecurityAccessor securityAccessor, IdentificationService identificationService) {
        this.securityAccessor = securityAccessor;
        this.identificationService = identificationService;
        this.device = securityAccessor.getDevice();
        goOffline();
    }

    /**
     * Triggers the capability to go offline and will copy all information
     * from the database into memory so that normal business operations can continue.<br>
     * Note that this may cause recursive calls to other objects that can go offline.
     */
    protected void goOffline() {
        setDeviceId((int) this.securityAccessor.getDevice().getId());
        setDeviceMRID(this.securityAccessor.getDevice().getmRID());
        setSecurityAccessorType(this.securityAccessor.getSecurityAccessorType());
        setActualValue(this.securityAccessor.getActualValue());
        setTempValue(this.securityAccessor.getTempValue());
        //set wrapping key info
        Optional<SecurityAccessorType> wrappingKeyAssignedToAccessor = getWrappingKeyAssignedToAccessor();
        setWrappingKeyActualValue(getWrappingKeyActualValue(wrappingKeyAssignedToAccessor));
    }


    @Override
    @XmlElements( {
            @XmlElement(type = DeviceIdentifierById.class),
            @XmlElement(type = DeviceIdentifierBySerialNumber.class),
            @XmlElement(type = DeviceIdentifierByMRID.class),
            @XmlElement(type = DeviceIdentifierForAlreadyKnownDevice.class),
            @XmlElement(type = DeviceIdentifierByDeviceName.class),
            @XmlElement(type = DeviceIdentifierByConnectionTypeAndProperty.class),
    })
    public DeviceIdentifier getDeviceIdentifier() {
        if (identificationService != null)
            deviceIdentifier = identificationService.createDeviceIdentifierForAlreadyKnownDevice(device.getId(), device.getmRID());
        return deviceIdentifier;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceMRID() {
        return deviceMRID;
    }

    private void setDeviceMRID(String deviceMRID) {
        this.deviceMRID = deviceMRID;
    }


    public SecurityAccessor<T> getSecurityAccessor() {
        return securityAccessor;
    }

    public IdentificationService getIdentificationService() {
        return identificationService;
    }

    public void setIdentificationService(IdentificationService identificationService) {
        this.identificationService = identificationService;
    }

    public Device getDevice() {
        return device;
    }

    @Override
    public int getDeviceId() {
        return this.deviceId;
    }

    @Override
    public SecurityAccessorType getSecurityAccessorType() {
        return securityAccessorType;
    }

    @Override
    public Optional<T> getActualValue() {
        return actualValue;
    }

    @Override
    public Optional<T> getTempValue() {
        return tempValue;
    }

    @Override
    public Optional<T> getWrappingKeyActualValue() {
        return wrappingKeyActualValue;
    }

    private void setWrappingKeyActualValue(Optional<T> wrappingKeyActualValue) {
        this.wrappingKeyActualValue = wrappingKeyActualValue;
    }

    private Optional<SecurityAccessorType> getWrappingKeyAssignedToAccessor() {
        Optional<SecurityAccessorTypeOnDeviceType> accessor = getDevice()
                .getDeviceType()
                .getSecurityAccessors()
                .stream()
                .filter(securityAccessorTypeOnDeviceType -> securityAccessorTypeOnDeviceType.getSecurityAccessorType().getName().equals(getSecurityAccessor().getName()))
                .findFirst();

        if (accessor.isPresent()) {
            return accessor.get().getDeviceSecurityAccessorType().getWrappingSecurityAccessor();
        }
        return Optional.empty();
    }

    private Optional<T> getWrappingKeyActualValue(Optional<SecurityAccessorType> wrappingKey) {
        if (wrappingKey.isPresent()) {
            Optional<SecurityAccessor> securityAccessorByName = device.getSecurityAccessorByName(wrappingKey.get().getName());
            return securityAccessorByName.isPresent() ? securityAccessorByName.get().getActualValue() : Optional.empty();
        }
        return Optional.empty();
    }

    private void setSecurityAccessorType(SecurityAccessorType securityAccessorType) {
        this.securityAccessorType = securityAccessorType;
    }

    private void setActualValue(Optional<T> actualValue) {
        this.actualValue = actualValue;
    }

    private void setTempValue(Optional<T> tempValue) {
        this.tempValue = tempValue;
    }
}
