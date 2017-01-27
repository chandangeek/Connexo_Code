package com.energyict.mdc.upl.offline;

import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.LoadProfileIdentifier;

import com.energyict.obis.ObisCode;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.time.temporal.TemporalAmount;
import java.util.Date;
import java.util.List;

/**
 * Represents an Offline version of a LoadProfile.
 *
 * @author gna
 * @since 30/05/12 - 9:36
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
public interface OfflineLoadProfile extends Offline {

    /**
     * @return the name (ie. loadprofile type name) of the LoadProfile
     */
    @XmlAttribute
    String getName();

    /**
     * Returns the database ID of this LoadProfile.
     *
     * @return the ID of the LoadProfile
     */
    @XmlAttribute
    long getLoadProfileId();

    /**
     * Returns the database ID of the type of this LoadProfile.
     *
     * @return the ID of the type of the LoadProfile
     */
    @XmlAttribute
    long getLoadProfileTypeId();

    /**
     * Returns the ObisCode for the LoadProfileType.
     *
     * @return the ObisCode (referring to a generic collection of channels having the same interval)
     */
    @XmlAttribute
    ObisCode getObisCode();

    /**
     * Returns the LoadProfile integration period.
     *
     * @return the integration period.
     */
    @XmlAttribute(name = "loadProfileInterval")
    TemporalAmount interval();

    /**
     * return the end time of the last interval read from the device.
     *
     * @return end time of the last interval.
     */
    @XmlAttribute
    Date getLastReading();

    /**
     * Returns the ID of the Device for the LoadProfile object.
     *
     * @return the ID of the Device
     */
    @XmlAttribute
    int getDeviceId();

    /**
     * Returns the SerialNumber of the Master Device
     *
     * @return the SerialNumber of the Master Device
     */
    @XmlAttribute
    String getMasterSerialNumber();

    /**
     * Returns the receiver's {@link OfflineLoadProfileChannel}.<br/>
     * <b>Be aware that this will only return the channels of the MASTER rtu.</b>
     * If you require all channels of this LoadProfile, including those of the slave devices with the same LoadProfileType, then use
     * {@link #getAllOfflineChannels()} instead.
     *
     * @return a <CODE>List</CODE> of {@link OfflineLoadProfileChannel} objects
     */
    @XmlAttribute
    List<OfflineLoadProfileChannel> getOfflineChannels();

    /**
     * Returns the receiver's {@link OfflineLoadProfileChannel} AND the {@link OfflineLoadProfileChannel} of
     * all slave devices belonging to load profiles of the same type
     *
     * @return a <CODE>List</CODE> of {@link OfflineLoadProfileChannel} objects
     */
    @XmlAttribute
    List<OfflineLoadProfileChannel> getAllOfflineChannels();

    // The element below is only used during JSON xml (un)marshalling.
    @XmlElement(name = "type")
    String getXmlType();

    void setXmlType(String ignore);

    DeviceIdentifier getDeviceIdentifier();

    LoadProfileIdentifier getLoadProfileIdentifier();

    default boolean isDataLoggerSlaveLoadProfile() {
        return false;
    }
}