package com.energyict.mdc.upl.offline;

import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.RegisterIdentifier;

import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

/**
 * Represents an Offline version of a Register
 *
 * @author gna
 * @since 12/06/12 - 11:48
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
public interface OfflineRegister extends Offline {

    /**
     * @return the name (ie. Register mapping) of the Register
     */
    @XmlAttribute
    String getName();

    /**
     * @return the ID of the Register
     */
    @XmlAttribute
    long getRegisterId();

    /**
     * Returns the ObisCode for this Register.<br/>
     *
     * @return the ObisCode
     */
    @XmlAttribute
    ObisCode getObisCode();


    /**
     * Get the business Id(s) of the RegisterGroup(s) where this registers belongs to.
     *
     * @return the ID(s) of the RegisterGroup(s)
     */
    @XmlAttribute
    List<Long> getRegisterGroupIds();

    /**
     * The {@link Unit} corresponding with this register
     *
     * @return the unit of this register
     */
    @XmlAttribute
    Unit getUnit();

    /**
     * Returns the unique identifier of the
     * Device owning this OfflineRegister.
     *
     * @return The Device's id
     */
    @XmlAttribute
    long getDeviceId();

    /**
     * The serialNumber of the OfflineDevice owning this OfflineRegister.
     *
     * @return the serialNumber of the Device owning this Register
     */
    @XmlAttribute
    String getSerialNumber();

    /**
     * Tests if this register is part of the RegisterGroup
     * that is uniquely identified by the registerGroupId.
     *
     * @param registerGroupId The register group id
     * @return A flag that indicates if this register is part of the group
     */
    boolean inGroup(long registerGroupId);

    /**
     * Tests if this register is part of at least one of the RegisterGroups
     * that are uniquely identified by the registerGroupIds.
     *
     * @param registerGroupIds The register group id
     * @return A flag that indicates if this register is part of at least one of the groups
     */
    boolean inAtLeastOneGroup(Collection<Long> registerGroupIds);

    /**
     * The master resource identifier of the {@link OfflineDevice} owning this OfflineRegister.
     *
     * @return the mRID
     */
    String getDeviceMRID();

    /**
     * The ObisCode of the Register, known by the HeadEnd system.
     *
     * @return the obisCode of the Register, known by the HeadEnd system
     */
    ObisCode getAmrRegisterObisCode();

    /**
     * The identifier that uniquely identifies the device.
     *
     * @return the deviceIdentifier
     */
    DeviceIdentifier getDeviceIdentifier();

    /**
     * The identifier that uniquely identifies this {@link OfflineRegister}.
     */
    RegisterIdentifier getRegisterIdentifier();

    /**
     * Returns the MRID of the ReadingType of the Kore channel that will store the data.
     *
     * @return the MRID of the ReadingType
     */
    String getReadingTypeMRID();

    /**
     * The overflow value which is configured for this Register.
     *
     * @return the configured overFlowValue
     */
    BigDecimal getOverFlowValue();

    /**
     * Indicates whether this is a text register
     *
     * @return true if this is a Text register, false otherwise
     */
    boolean isText();

    // The element below is only used during JSON xml (un)marshalling.
    @XmlElement(name = "type")
    default String getXmlType() {
        return getClass().getName();
    }

    default void setXmlType(String ignore) {
        // For xml unmarshalling purposes only
    }

}
