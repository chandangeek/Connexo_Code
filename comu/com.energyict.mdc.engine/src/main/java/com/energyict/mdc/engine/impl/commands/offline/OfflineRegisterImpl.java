/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.offline;

import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.device.config.NumericalRegisterSpec;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.masterdata.RegisterGroup;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.offline.OfflineRegister;
import com.energyict.mdc.protocol.api.services.IdentificationService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
    private final Register<?,?> register;
    private IdentificationService identificationService;

    private final Device device;

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
    private long registerId;

    /**
     * The ID of the {@link RegisterGroup} where this registers belongs to.
     */
    private List<Long> registerGroupIds;
    /**
     * The serialNumber of the Device owning this Register
     */
    private String deviceSerialNumber;
    /**
     * The mRID of the Device owning this Register
     */
    private String deviceMRID;
    /**
     * The database ID of the Device
     */
    private long deviceId;

    /**
     * The ReadingType of the Register
     */
    private ReadingType readingType;
    /**
     * The configured OverFlow value
     */
    private BigDecimal overFlow;
    /**
     * Indicates that this is a text register
     */
    private boolean isText;

    public OfflineRegisterImpl(final Register<?,?> register, IdentificationService identificationService) {
        this.register = register;
        this.identificationService = identificationService;
        this.device = register.getDevice();
        this.deviceId = register.getDevice().getId();
        this.goOffline();
    }

    /**
     * Triggers the capability to go offline and will copy all information
     * from the database into memory so that normal business operations can continue.<br>
     * Note that this may cause recursive calls to other objects that can go offline.
     */
    private void goOffline() {
        this.registerId = (int) this.register.getRegisterSpec().getId();
        this.deviceRegisterObisCode = this.register.getDeviceObisCode();
        this.amrRegisterObisCode = this.register.getRegisterSpec().getObisCode();
        this.registerUnit = this.register.getRegisterSpec().getRegisterType().getUnit();

        // We don't use the rtuRegister.getOverruledRegisterGroup as this can be overruled!
        List<RegisterGroup> registerGroups = this.register.getRegisterSpec().getRegisterType().getRegisterGroups();
        this.registerGroupIds = new ArrayList<>(registerGroups.size());
        for (RegisterGroup registerGroup : registerGroups) {
            this.registerGroupIds.add(registerGroup.getId());
        }
        this.deviceSerialNumber = this.register.getDevice().getSerialNumber();
        this.deviceMRID = this.register.getDevice().getmRID();
        this.readingType = this.register.getRegisterSpec().getRegisterType().getReadingType();
        if(this.register.getRegisterSpec().isTextual()){
            this.overFlow = new BigDecimal(Double.MAX_VALUE);
        } else if(((NumericalRegisterSpec) this.register.getRegisterSpec()).getOverflowValue().isPresent()){
            this.overFlow = ((NumericalRegisterSpec) this.register.getRegisterSpec()).getOverflowValue().get();
        }
        this.isText = this.register.getRegisterSpec().isTextual();
    }

    /**
     * @return the ID of the {@link com.energyict.mdc.protocol.api.device.BaseRegister}
     */
    @Override
    public long getRegisterId() {
        return this.registerId;
    }

    @Override
    public ObisCode getObisCode() {
        return this.deviceRegisterObisCode;
    }

    @Override
    public ObisCode getAmrRegisterObisCode() {
        return this.amrRegisterObisCode;
    }

    @Override
    public boolean inGroup(long registerGroupId) {
        return this.registerGroupIds.contains(registerGroupId);
    }

    @Override
    public boolean inAtLeastOneGroup(Collection<Long> registerGroupIds) {
        return !Collections.disjoint(this.registerGroupIds, registerGroupIds);
    }

    @Override
    public Unit getUnit() {
        return this.registerUnit;
    }

    @Override
    public String getDeviceMRID() {
        return this.deviceMRID;
    }

    @Override
    public String getDeviceSerialNumber() {
        return this.deviceSerialNumber;
    }

    @Override
    public DeviceIdentifier<?> getDeviceIdentifier() {
        return identificationService.createDeviceIdentifierForAlreadyKnownDevice(device);
    }

    @Override
    public ReadingType getReadingType() {
        return this.readingType;
    }

    @Override
    public BigDecimal getOverFlowValue() {
        return this.overFlow;
    }

    @Override
    public boolean isText() {
        return isText;
    }

}