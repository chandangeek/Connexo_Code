package com.energyict.mdc.upl.messages;

import com.energyict.mdc.upl.DeviceProtocol;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.MessageIdentifier;
import com.energyict.mdc.upl.offline.Offline;
import org.codehaus.jackson.annotate.JsonTypeInfo;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.Date;
import java.util.List;

/**
 * Represents an Offline version of a {@link com.energyict.mdc.upl.messages.DeviceMessage}
 * which should contain all necessary information needed to perform the actual DeviceMessage.
 * <p>
 * Copyrights EnergyICT
 * Date: 18/02/13
 * Time: 16:27
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
public interface OfflineDeviceMessage extends Offline {

    /**
     * Gets the {@link DeviceMessageSpec} of this offline version
     * of a {@link DeviceMessage}.
     *
     * @return The DeviceMessageSpec
     */
    DeviceMessageSpec getSpecification();

    MessageIdentifier getIdentifier();

    /**
     * The value of the {@link FactoryBasedDeviceMessageSpecPrimaryKey} to uniquely identify
     * the DeviceMessageSpec of this message.
     *
     * @return the value of the DeviceMessageSpecPrimaryKey
     */
    @XmlAttribute
    String getDeviceMessageSpecPrimaryKey();

    /**
     * Returns a freeform string that contains extra context information that was prepared by the protocol implementation.
     * The message executor of the protocol implementation can access this field.
     */
    @XmlAttribute
    String getPreparedContext();

    /**
     * @return the ID of the {@link DeviceMessage}
     */
    @XmlAttribute
    long getDeviceMessageId();

    /**
     * The DeviceProtocol of the device that has this message
     */
    DeviceProtocol getDeviceProtocol();

    /**
     * The ID of the Device owning this DeviceMessage
     *
     * @return the id of the Device
     */
    @XmlAttribute
    long getDeviceId();

    /**
     * The serial number of the Device owning this DeviceMessage
     *
     * @return the serial number of the Device
     */
    @XmlAttribute
    String getDeviceSerialNumber();

    /**
     * @return the identifier of the device which owns this devicemessage
     */
    DeviceIdentifier getDeviceIdentifier();

    /**
     * The configured date of when this message <i>could</i> be executed
     *
     * @return the release date of this message
     */
    @XmlAttribute
    Date getReleaseDate();

    /**
     * An ID which can be used to keep track of this message
     *
     * @return the trackingId
     */
    @XmlAttribute
    String getTrackingId();

    /**
     * The protocol info returned from the device protocol.
     *
     * @return the protocolInfo
     */
    @XmlAttribute
    String getProtocolInfo();

    /**
     * The current {@link DeviceMessageStatus status} of this DeviceMessage
     *
     * @return the DeviceMessageStatus
     */
    @XmlAttribute
    DeviceMessageStatus getDeviceMessageStatus();

    /**
     * The date on which this DeviceMessage was created
     *
     * @return the creationDate of this message
     */
    @XmlAttribute
    Date getCreationDate();

    /**
     * The list of {@link OfflineDeviceMessageAttribute DeviceMessageAttributes} which are owned
     * by this DeviceMessage. The information contained in these attributes should be sufficient
     * to perform the DeviceMessage.
     *
     * @return the list of OfflineDeviceMessageAttribute
     */
    @XmlAttribute
    List<? extends OfflineDeviceMessageAttribute> getDeviceMessageAttributes();

    // The element below is only used during JSON xml (un)marshalling.
    @XmlElement(name = "type")
    String getXmlType();

    void setXmlType(String ignore);

}