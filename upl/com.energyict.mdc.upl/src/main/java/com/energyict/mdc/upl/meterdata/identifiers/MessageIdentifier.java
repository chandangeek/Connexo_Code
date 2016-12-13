package com.energyict.mdc.upl.meterdata.identifiers;

import com.energyict.mdc.upl.messages.DeviceMessage;

import org.codehaus.jackson.annotate.JsonTypeInfo;

import javax.xml.bind.annotation.XmlElement;
import java.util.List;

/**
 * Provides functionality to uniquely identify a {@link DeviceMessage}.
 * <p/>
 * Copyrights EnergyICT
 * Date: 22/03/13
 * Time: 8:59
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
public interface MessageIdentifier {

    /**
     * Returns the {@link DeviceMessage} uniquely identified by this identifier.
     * Note that this may throw a runtime exception when the Message could
     * either not be found or multiple messages were found in which case
     * this identifier was not as unique as you thought it was.
     *
     * @return the referenced DeviceMessage
     */
    DeviceMessage getDeviceMessage();

    /**
     * The type of this identifier.
     */
    MessageIdentifierType getMessageIdentifierType();

    /**
     * The essential part(s) of this identifier: the database ID, deviceIdentifier and ObisCode, ...
     */
    List<Object> getParts();


    // The element below is only used during JSON xml (un)marshalling.
    @XmlElement(name = "type")
    String getXmlType();

    void setXmlType(String ignore);

    /**
     * @return the DeviceIdentifier for this MessageIdentifier
     */
    public DeviceIdentifier getDeviceIdentifier();

}