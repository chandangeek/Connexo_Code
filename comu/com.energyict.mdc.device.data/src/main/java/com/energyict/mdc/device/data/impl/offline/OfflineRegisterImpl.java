package com.energyict.mdc.device.data.impl.offline;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.protocol.api.device.offline.OfflineRegister;

/**
 * The Offline implementation of a {@link com.energyict.mdc.protocol.api.device.BaseRegister}
 *
 * @author gna
 * @since 12/06/12 - 13:12
 */
public class OfflineRegisterImpl implements OfflineRegister {

    /**
     * The Register which will go Offline
     */
    private final Register register;

    /**
     * The ObisCode of the register which is know/used by the Device
     */
    private ObisCode deviceRegisterObisCode;

    /**
     * The ObisCode fo the register which is known/used by the AMR system
     */
    private ObisCode amrRegisterObisCode;

    /**
     * The {@link Unit} of the Register
     */
    private Unit registerUnit;

    /**
     * The Id of the rtuRegister
     */
    private int registerId;

    /**
     * The ID of the {@link com.energyict.mdc.device.config.RegisterGroup} where this registers belongs to.
     */
    private int registerGroupId;

    /**
     * The serialNumber of the Device owning this Register
     */
    private String meterSerialNumber;
    private int deviceId;

    public OfflineRegisterImpl(final Register register) {
        this.register = register;
        this.deviceId = (int) register.getDevice().getId();
        this.goOffline();
    }

    /**
     * Triggers the capability to go offline and will copy all information
     * from the database into memory so that normal business operations can continue.<br>
     * Note that this may cause recursive calls to other objects that can go offline.
     */
    private void goOffline() {
        this.registerId = (int) this.register.getRegisterSpec().getId();
        this.deviceRegisterObisCode = this.register.getRegisterSpec().getDeviceObisCode();
        this.amrRegisterObisCode = this.register.getRegisterSpec().getObisCode();
        this.registerUnit = this.register.getRegisterSpec().getUnit();

        // We don't use the rtuRegister.getOverruledRegisterGroup as this can be overruled!
        this.registerGroupId = this.register.getRegisterSpec().getRegisterMapping().getRegisterGroup() == null ? 0 : (int) this.register.getRegisterSpec().getRegisterMapping().getRegisterGroup().getId();
        this.meterSerialNumber = this.register.getDevice().getSerialNumber();
    }

    /**
     * @return the ID of the {@link com.energyict.mdc.protocol.api.device.BaseRegister}
     */
    @Override
    public int getRegisterId() {
        return this.registerId;
    }

    /**
     * Returns the ObisCode for this Register.<br/>
     * (actually the ObisCode from the {@link com.energyict.mdc.device.config.RegisterMapping})
     *
     * @return the ObisCode
     */
    @Override
    public ObisCode getObisCode() {
        return this.deviceRegisterObisCode;
    }

    @Override
    public ObisCode getAmrRegisterObisCode() {
        return this.amrRegisterObisCode;
    }

    /**
     * Get the business Id of the {@link com.energyict.mdc.device.config.RegisterGroup} where this registers belongs to
     *
     * @return the ID of the {@link com.energyict.mdc.device.config.RegisterGroup}
     */
    @Override
    public int getRegisterGroupId() {
        return this.registerGroupId;
    }

    /**
     * The {@link Unit} corresponding with this register
     *
     * @return the unit of this register
     */
    @Override
    public Unit getUnit() {
        return this.registerUnit;
    }

    /**
     * The serialNumber of the {@link com.energyict.mdc.protocol.api.device.offline.OfflineDevice} owning this {@link OfflineRegister}
     *
     * @return the serialNumber of the Device owning this Register
     */
    @Override
    public String getSerialNumber() {
        return this.meterSerialNumber;
    }

    @Override
    public int getDeviceId() {
        return this.deviceId;
    }

}
