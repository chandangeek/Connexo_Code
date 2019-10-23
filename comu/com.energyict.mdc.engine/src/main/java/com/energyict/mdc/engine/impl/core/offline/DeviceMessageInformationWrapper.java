package com.energyict.mdc.engine.impl.core.offline;

import com.energyict.mdc.identifiers.DeviceMessageIdentifierByDeviceAndProtocolInfoParts;
import com.energyict.mdc.identifiers.DeviceMessageIdentifierById;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.meterdata.identifiers.MessageIdentifier;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import java.time.Instant;

/**
 * @author sva
 * @since 24/07/2014 - 13:27
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@XmlRootElement
public class DeviceMessageInformationWrapper {

    final MessageIdentifier messageIdentifier;
    final DeviceMessageStatus newDeviceMessageStatus;
    final Instant sentDate;
    final String protocolInformation;

    /**
     * Constructor only to be used by JSON (de)marshalling
     */
    public DeviceMessageInformationWrapper() {
        this.messageIdentifier = new DeviceMessageIdentifierById();
        this.newDeviceMessageStatus = DeviceMessageStatus.WAITING;
        this.sentDate = Instant.now();
        this.protocolInformation = "";
    }

    public DeviceMessageInformationWrapper(MessageIdentifier messageIdentifier, DeviceMessageStatus newDeviceMessageStatus, Instant sentDate, String protocolInformation) {
        this.messageIdentifier = messageIdentifier;
        this.newDeviceMessageStatus = newDeviceMessageStatus;
        this.sentDate = sentDate;
        this.protocolInformation = protocolInformation;
    }

    @XmlElements( {
            @XmlElement(type = DeviceMessageIdentifierById.class),
            @XmlElement(type = DeviceMessageIdentifierByDeviceAndProtocolInfoParts.class),
    })
    public MessageIdentifier getMessageIdentifier() {
        return messageIdentifier;
    }

    @XmlAttribute
    public DeviceMessageStatus getNewDeviceMessageStatus() {
        return newDeviceMessageStatus;
    }

    @XmlAttribute
    public Instant getSentDate() {
        return sentDate;
    }

    @XmlAttribute
    public String getProtocolInformation() {
        return protocolInformation;
    }

    @XmlElement(name = "type")
    public String getXmlType() {
        return this.getClass().getName();
    }

    public void setXmlType(String ignore) {
        // For xml unmarshalling purposes only
    }
}
