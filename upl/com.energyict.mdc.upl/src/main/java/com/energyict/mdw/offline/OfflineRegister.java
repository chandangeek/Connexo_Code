package com.energyict.mdw.offline;

import com.energyict.cbo.Unit;
import com.energyict.cpo.Offline;
import com.energyict.obis.ObisCode;
import org.codehaus.jackson.annotate.JsonTypeInfo;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

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
     * Get the business Id of the RegisterGroup where this registers belongs to.
     *
     * @return the ID of the RegisterGroup
     */
    @XmlAttribute
    long getRegisterGroupId();

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

    // The element below is only used during JSON xml (un)marshalling.
    @XmlElement(name = "type")
    String getXmlType();

    void setXmlType(String ignore);

}
