package com.energyict.mdc.upl.offline;

import com.energyict.obis.ObisCode;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * Represents an Offline version of a LogBookSpec.
 *
 * @author sva
 * @since 10/12/12 - 13:50
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
public interface OfflineLogBookSpec extends Offline {

    /**
     * Returns the database ID of the online LogBookSpec
     *
     * @return the ID of the LogBookSpec
     */
    @XmlAttribute
    int getLogBookSpecId();

    /**
     * Returns the database ID of the device configuration for the LogBookSpec
     *
     * @return the ID of the device configuration
     */
    @XmlAttribute
    long getDeviceConfigId();

    /**
     * Returns the database ID of the LogBookType for the LogBookSpec
     *
     * @return the database ID of the LogBookType
     */
    @XmlAttribute
    long getLogBookTypeId();

    /**
     * Returns the DeviceObisCode, this is the ObisCode as used in the device
     *
     * @return the DeviceObisCode
     */
    @XmlAttribute
    ObisCode getDeviceObisCode();

    /**
     * Returns the ObisCode, this is the ObisCode configured for this LogBookSpec
     *
     * @return the ObisCode
     */
    @XmlAttribute
    ObisCode getObisCode();

    // The element below is only used during JSON xml (un)marshalling.
    @XmlElement(name = "type")
    String getXmlType();

    void setXmlType(String ignore);

}