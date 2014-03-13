package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.protocol.api.device.offline.OfflineRegister;

/**
 * Provides an implementation of a Register of a {@link com.energyict.mdc.device.data.Device},
 * which is actually a wrapping around a {@link com.energyict.mdc.device.config.RegisterSpec}
 * of the {@link com.energyict.mdc.device.config.DeviceConfiguration}
 * <p/>
 * Copyrights EnergyICT
 * Date: 11/03/14
 * Time: 11:19
 */
public class RegisterImpl implements Register {

    /**
     * The {@link RegisterSpec} for which this Register is serving
     */
    private final RegisterSpec registerSpec;
    /**
     * The Device which <i>owns</i> this Register
     */
    private final Device device;

    public RegisterImpl(RegisterSpec registerSpec, Device device) {
        this.registerSpec = registerSpec;
        this.device = device;
    }

    @Override
    public Device getDevice() {
        return device;
    }

    @Override
    public RegisterSpec getRegisterSpec() {
        return registerSpec;
    }

    @Override
    public ObisCode getRegisterMappingObisCode() {
        return getRegisterSpec().getRegisterMapping().getObisCode();
    }

    @Override
    public ObisCode getRegisterSpecObisCode() {
        return getRegisterSpec().getObisCode();
    }

    @Override
    public ObisCode getDeviceObisCode() {
        return getRegisterSpec().getDeviceObisCode();
    }

    @Override
    public long getRegisterSpecId() {
        return getRegisterSpec().getId();
    }

    @Override
    public OfflineRegister goOffline() {
        return null;
    }
}
