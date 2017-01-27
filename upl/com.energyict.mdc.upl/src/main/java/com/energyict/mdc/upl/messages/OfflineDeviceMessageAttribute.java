package com.energyict.mdc.upl.messages;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * Represents an Offline version of a DeviceMessageAttribute.
 * <p/>
 * Copyrights EnergyICT
 * Date: 18/02/13
 * Time: 16:34
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
public interface OfflineDeviceMessageAttribute {

    /**
     * The name of this OfflineDeviceMessageAttribute.
     *
     * @return the name of the OfflineDeviceMessageAttribute
     */
    @XmlAttribute
    String getName();

    /**
     * The related object/value of the OfflineDeviceMessageAttribute
     *
     * @return this will contain the information to send or the action to perform on the Device
     */
    @XmlAttribute
    String getValue();

    /**
     * The id of the {@link DeviceMessage} which owns this OfflineDeviceMessageAttribute
     *
     * @return the id of the DeviceMessage which owns this OfflineDeviceMessageAttribute
     */
    @XmlAttribute
    long getDeviceMessageId();

    // The element below is only used during JSON xml (un)marshalling.
    @XmlElement(name = "type")
    default String getXmlType() {
        return getClass().getName();
    }

    default void setXmlType(String ignore) {}

}