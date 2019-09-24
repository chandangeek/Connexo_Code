/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.offline;

import com.energyict.mdc.common.device.config.NumericalRegisterSpec;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.Register;
import com.energyict.mdc.device.data.impl.IdentificationServiceImpl;
import com.energyict.mdc.identifiers.RegisterDataIdentifierByObisCodeAndDevice;
import com.energyict.mdc.common.masterdata.RegisterGroup;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.RegisterIdentifier;
import com.energyict.mdc.upl.offline.OfflineRegister;

import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The Offline implementation of a {@link com.energyict.mdc.upl.meterdata.Register}
 *
 * @author gna
 * @since 12/06/12 - 13:12
 */
public class OfflineRegisterImpl implements OfflineRegister {

    /**
     * The Register which will go Offline
     */
    private Register<?, ?> register;
    private Device device;
    private IdentificationService identificationService;

    private DeviceIdentifier deviceIdentifier;
    private RegisterIdentifier registerIdentifier;
    /**
     * The ObisCode of the register which is know/used by the Device
     */
    private ObisCode obisCode;

    /**
     * The ObisCode fo the register which is known/used by the AMR system
     */
    private ObisCode amrRegisterObisCode;

    /**
     * The {@link Unit} of the Register
     */
    private Unit unit;

    /**
     * The Id of the rtuRegister
     */
    private long registerId;

    /**
     * The ID(s) of the {@link RegisterGroup}(s) where this registers belongs to.
     */
    private List<Long> registerGroupIds;

    /**
     * The last reading date of this register
     */
    private Optional<Instant> lastReadingDate;

    /**
     * The serialNumber of the Device owning this Register
     */
    private String serialNumber;
    /**
     * The mRID of the Device owning this Register
     */
    private String deviceMRID;
    /**
     * The database ID of the Device
     */
    private long deviceId;

    /**
     * The MRID of the ReadingType of the Register
     */
    private String readingTypeMRID;
    /**
     * The configured OverFlow value
     */
    private BigDecimal overFlow;
    /**
     * Indicates that this is a text register
     */
    private boolean text;

    private String name;

    public OfflineRegisterImpl() {
        super();
    }

    public OfflineRegisterImpl(final Register<?, ?> register, IdentificationService identificationService) {
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
        this.registerId = this.register.getRegisterSpecId();
        this.obisCode = this.register.getDeviceObisCode();
        this.amrRegisterObisCode = this.register.getRegisterSpec().getObisCode();
        this.unit = this.register.getRegisterSpec().getRegisterType().getUnit();

        // We don't use the rtuRegister.getOverruledRegisterGroup as this can be overruled!
        List<RegisterGroup> registerGroups = this.register.getRegisterSpec().getRegisterType().getRegisterGroups();
        this.registerGroupIds = new ArrayList<>(registerGroups.size());
        this.registerGroupIds.addAll(registerGroups.stream().map(RegisterGroup::getId).collect(Collectors.toList()));
        this.serialNumber = this.register.getDevice().getSerialNumber();
        this.deviceMRID = this.register.getDevice().getmRID();
        this.readingTypeMRID = this.register.getRegisterSpec().getRegisterType().getReadingType().getMRID();
        if (this.register.getRegisterSpec().isTextual()) {
            this.overFlow = new BigDecimal(Double.MAX_VALUE);
        } else if (((NumericalRegisterSpec) this.register.getRegisterSpec()).getOverflowValue().isPresent()) {
            this.overFlow = ((NumericalRegisterSpec) this.register.getRegisterSpec()).getOverflowValue().get();
        }
        this.text = this.register.getRegisterSpec().isTextual();
        this.lastReadingDate = register.getLastReadingDate();
    }

    @Override
    public Optional<Instant> getLastReadingDate() {
        return lastReadingDate;
    }

    @Override
    public long getRegisterId() {
        return this.registerId;
    }

    @Override
    public String getName() {
        name = getReadingTypeMRID();
        return name;
    }

    @Override
    public List<Long> getRegisterGroupIds() {
        return registerGroupIds;
    }

    @Override
    public long getDeviceId() {
        return deviceId;
    }

    @Override
    public ObisCode getObisCode() {
        return this.obisCode;
    }

    @Override
    @XmlTransient
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
    @XmlAttribute
    public Unit getUnit() {
        return this.unit;
    }

    @Override
    public String getDeviceMRID() {
        return this.deviceMRID;
    }

    @Override
    public String getSerialNumber() {
        return this.serialNumber;
    }

    @Override
    public DeviceIdentifier getDeviceIdentifier() {
        if (deviceIdentifier == null) {
            deviceIdentifier = identificationService.createDeviceIdentifierForAlreadyKnownDevice(device.getId(), device.getmRID());
        }
        return deviceIdentifier;
    }

    @Override
    public RegisterIdentifier getRegisterIdentifier() {
        if (registerIdentifier == null) {
            registerIdentifier = new RegisterDataIdentifierByObisCodeAndDevice(register);;
        }
        return registerIdentifier;
    }

    @Override
    public String getReadingTypeMRID() {
        return this.readingTypeMRID;
    }

    @Override
    public BigDecimal getOverFlow() {
        return this.overFlow;
    }

    @Override
    @XmlAttribute
    public boolean isText() {
        return text;
    }

    @Override
    @XmlElement(name = "type")
    public String getXmlType() {
        return getClass().getName();
    }

    @Override
    public void setXmlType(String ignore) {
        // For xml unmarshalling purposes only
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        OfflineRegisterImpl that = (OfflineRegisterImpl) o;

        if (getRegisterId() != that.getRegisterId()) {
            return false;
        }
        if (!obisCode.equals(that.obisCode)) {
            return false;
        }
        if (unit != null ? !unit.equals(that.unit) : that.unit != null) {
            return false;
        }
        if (getSerialNumber() != null ? !getSerialNumber().equals(that.getSerialNumber()) : that.getSerialNumber() != null) {
            return false;
        }
        return getReadingTypeMRID() != null ? getReadingTypeMRID().equals(that.getReadingTypeMRID()) : that.getReadingTypeMRID() == null;

    }

    @Override
    public int hashCode() {
        int result = obisCode.hashCode();
        result = 31 * result + (unit != null ? unit.hashCode() : 0);
        result = 31 * result + (int) (getRegisterId() ^ (getRegisterId() >>> 32));
        result = 31 * result + (getSerialNumber() != null ? getSerialNumber().hashCode() : 0);
        result = 31 * result + (getReadingTypeMRID() != null ? getReadingTypeMRID().hashCode() : 0);
        return result;
    }
}