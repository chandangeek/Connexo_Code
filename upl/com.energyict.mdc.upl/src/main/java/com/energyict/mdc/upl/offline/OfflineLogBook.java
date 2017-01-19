package com.energyict.mdc.upl.offline;

import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.LogBookIdentifier;
import org.codehaus.jackson.annotate.JsonTypeInfo;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.Date;

/**
 * Represents an Offline version of a LogBook.
 *
 * @author sva
 * @since 07/12/12 - 14:30
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
public interface OfflineLogBook extends Offline {

    /**
     * Returns the database ID of this LogBook.
     *
     * @return the ID of the LogBook
     */
    @XmlAttribute
    long getLogBookId();

    /**
     * Returns the {@link OfflineLogBookSpec} for the LogBookType.
     *
     * @return the {@link OfflineLogBookSpec}
     */
    @XmlAttribute
    OfflineLogBookSpec getOfflineLogBookSpec();

    /**
     * Returns the Id of the Device which owns this LogBookType.
     *
     * @return the {@link OfflineDevice}
     */
    @XmlAttribute
    int getDeviceId();

    /**
     * Returns the SerialNumber of the Master Device
     *
     * @return the SerialNumber of the Master Device
     */
    @XmlAttribute(name = "serialNumber")
    String getMasterSerialNumber();

    /**
     * Returns the Date from where to start fetching data from the LogBook
     *
     * @return the {@link OfflineDevice}
     */
    @XmlAttribute
    Date getLastReading();

    // The element below is only used during JSON xml (un)marshalling.
    @XmlElement(name = "type")
    String getXmlType();

    void setXmlType(String ignore);

    DeviceIdentifier getDeviceIdentifier();

    LogBookIdentifier getLogBookIdentifier();
}