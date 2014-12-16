package com.energyict.mdc.protocol.api.device.offline;

import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Offline;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;

import java.math.BigDecimal;
import java.util.Collection;

/**
 * Represents an Offline version of a register.
 *
 * @author gna
 * @since 12/06/12 - 11:48
 */
public interface OfflineRegister extends Offline {

    /**
     * @return the ID of the Register
     */
    public long getRegisterId ();

    /**
     * Returns the ObisCode for this Register, known by the Device. The will be the overruled ObisCode,
     * or if no overrule was defined, this will return the same ObisCode as {@link #getAmrRegisterObisCode()}<br/>
     * (actually the ObisCode from the ChannelType)
     *
     * @return the ObisCode
     */
    public ObisCode getObisCode();


    /**
     * Tests if this register is part of the RegisterGroup
     * that is uniquely identified by the registerGroupId.
     *
     * @param registerGroupId The register group id
     * @return A flag that indicates if this register is part of the group
     */
    public boolean inGroup (long registerGroupId);

    /**
     * Tests if this register is part of at least one of the RegisterGroups
     * that are uniquely identified by the registerGroupIds.
     *
     * @param registerGroupIds The register group id
     * @return A flag that indicates if this register is part of at least one of the groups
     */
    public boolean inAtLeastOneGroup (Collection<Long> registerGroupIds);

    /**
     * The {@link Unit} corresponding with this register
     *
     * @return the unit of this register
     */
    public Unit getUnit();

    /**
     * The master resource identifier of the {@link OfflineDevice} owning this OfflineRegister.
     *
     * @return the mRID
     */
    public String getDeviceMRID();

    /**
     * The serialNumber of the {@link OfflineDevice} owning this OfflineRegister.
     *
     * @return the serialNumber of the Device owning this Register
     */
    public String getDeviceSerialNumber();

    /**
     * The ObisCode of the Register, known by the HeadEnd system.
     *
     * @return the obisCode of the Register, known by the HeadEnd system
     */
    public ObisCode getAmrRegisterObisCode();

    /**
     * The identifier that uniquely identifies the device
     *
     * @return the deviceIdentifier
     */
    public DeviceIdentifier<?> getDeviceIdentifier();

    /**
     * Returns the ReadingType of the Kore channel that will store the data
     *
     * @return the ReadingType
     */
    public ReadingType getReadingType();

    /**
     * The overflow value which is configured for this Register
     * @return the configured overFlowValue
     */
    public BigDecimal getOverFlowValue();

    /**
     * Indicates whether this is a text register
     *
     * @return true if this is a Text register, false otherwise
     */
    public boolean isText();
}