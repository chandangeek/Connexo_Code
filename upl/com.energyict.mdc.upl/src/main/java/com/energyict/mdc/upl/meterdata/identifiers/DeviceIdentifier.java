package com.energyict.mdc.upl.meterdata.identifiers;

import com.energyict.mdc.upl.meterdata.Device;

import org.codehaus.jackson.annotate.JsonTypeInfo;

import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;

/**
 * Identifies a device that started inbound communication
 * and is also capable of finding that device.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-12 (10:56)
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
public interface DeviceIdentifier extends Serializable {

    /**
     * Finds the device that is uniquely identified by this DeviceIdentifier.
     * Note that this may throw a runtime exception when the Device could
     * either not be found or multiple devices were found in which case
     * this identifier was not as unique as you thought it was.
     *
     * @return The device
     */
    Device findDevice();

    /**
     * The essential part of this identifier: the serial number, the database ID, the call home Id or something else.
     */
    String getIdentifier();

    /**
     * The type of this identifier. E.g. SerialNumber, DataBaseId, ...
     */
    DeviceIdentifierType getDeviceIdentifierType();

    // The element below is only used during JSON xml (un)marshalling.
    @XmlElement(name = "type")
    String getXmlType();

    void setXmlType(String ignore);

}