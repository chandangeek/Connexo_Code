package com.energyict.mdc.engine.impl.commands.offline;

import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.SecurityValueWrapper;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.SecurityAccessor;
import com.energyict.mdc.protocol.api.device.offline.OfflineKeyAccessor;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;

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
        setSecurityAccessorType(this.securityAccessor.getKeyAccessorType());
        setActualValue(this.securityAccessor.getActualValue());
        setTempValue(this.securityAccessor.getTempValue());
    }


    @Override
    public DeviceIdentifier getDeviceIdentifier() {
        return this.identificationService.createDeviceIdentifierForAlreadyKnownDevice(device);
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

    public void setSecurityAccessorType(SecurityAccessorType securityAccessorType) {
        this.securityAccessorType = securityAccessorType;
    }

    public void setActualValue(Optional<T> actualValue) {
        this.actualValue = actualValue;
    }

    public void setTempValue(Optional<T> tempValue) {
        this.tempValue = tempValue;
    }
}