package com.energyict.mdc.engine.impl.commands.offline;

import com.elster.jupiter.pki.KeyAccessorType;
import com.elster.jupiter.pki.SecurityValueWrapper;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.KeyAccessor;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.offline.OfflineKeyAccessor;
import com.energyict.mdc.protocol.api.services.IdentificationService;

import java.util.Optional;

public class OfflineKeyAccessorImpl<T extends SecurityValueWrapper> implements OfflineKeyAccessor {

    /**
     * The {@link com.energyict.mdc.protocol.api.device.BaseLogBook} which is going offline
     */
    private final KeyAccessor<T> keyAccessor;
    private IdentificationService identificationService;

    private final Device device;
    private KeyAccessorType keyAccessorType;
    private Optional<T> actualValue;
    private Optional<T> tempValue;
    private int deviceId;

    private String deviceMRID;

    public OfflineKeyAccessorImpl(KeyAccessor keyAccessor, IdentificationService identificationService) {
        this.keyAccessor = keyAccessor;
        this.identificationService = identificationService;
        this.device = keyAccessor.getDevice();
        goOffline();
    }

    /**
     * Triggers the capability to go offline and will copy all information
     * from the database into memory so that normal business operations can continue.<br>
     * Note that this may cause recursive calls to other objects that can go offline.
     */
    protected void goOffline() {
        setDeviceId((int) this.keyAccessor.getDevice().getId());
        setDeviceMRID(this.keyAccessor.getDevice().getmRID());
        setKeyAccessorType(this.keyAccessor.getKeyAccessorType());
        setActualValue(this.keyAccessor.getActualValue());
        setTempValue(this.keyAccessor.getTempValue());
    }


    @Override
    public DeviceIdentifier<?> getDeviceIdentifier() {
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


    public KeyAccessor<T> getKeyAccessor() {
        return keyAccessor;
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
    public KeyAccessorType getKeyAccessorType() {
        return keyAccessorType;
    }

    @Override
    public Optional<T> getActualValue() {
        return actualValue;
    }

    @Override
    public Optional<T> getTempValue() {
        return tempValue;
    }

    public void setKeyAccessorType(KeyAccessorType keyAccessorType) {
        this.keyAccessorType = keyAccessorType;
    }

    public void setActualValue(Optional<T> actualValue) {
        this.actualValue = actualValue;
    }

    public void setTempValue(Optional<T> tempValue) {
        this.tempValue = tempValue;
    }
}