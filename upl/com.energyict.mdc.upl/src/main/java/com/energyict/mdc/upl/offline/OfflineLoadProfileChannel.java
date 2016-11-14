package com.energyict.mdc.upl.offline;

import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import org.codehaus.jackson.annotate.JsonTypeInfo;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * Represents an Offline version of a Channel in a specific LoadProfile.
 *
 * @author gna
 * @since 30/05/12 - 9:49
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
public interface OfflineLoadProfileChannel extends Offline {

    @XmlAttribute
    String getName();

    /**
     * Returns the {@link ObisCode} for this Channel in the LoadProfile
     *
     * @return the {@link ObisCode}
     */
    @XmlAttribute
    ObisCode getObisCode();

    /**
     * Returns the ID of the Device for the LoadProfile object.
     *
     * @return the ID of the Device.
     */
    @XmlAttribute
    long getDeviceId();

    /**
     * Returns the ID of the LoadProfile
     *
     * @return the ID of the LoadProfile.
     */
    @XmlAttribute
    long getLoadProfileId();

    /**
     * Returns the receiver's configured unit.
     *
     * @return the configured unit.
     */
    @XmlAttribute
    Unit getUnit();

    /**
     * Indication whether we should store data for this channel
     *
     * @return true if we should store data for this channel, false otherwise
     */
    @XmlAttribute
    boolean isStoreData();

    /**
     * Returns the SerialNumber of the Device
     *
     * @return the SerialNumber of the Device
     */
    @XmlAttribute
    String getMasterSerialNumber();

    // The element below is only used during JSON xml (un)marshalling.
    @XmlElement(name = "type")
    String getXmlType();

    void setXmlType(String ignore);

}