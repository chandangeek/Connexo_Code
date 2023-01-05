/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.offline;

import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.identifiers.*;
import com.energyict.mdc.common.protocol.DeviceMessage;
import com.energyict.mdc.common.protocol.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageAttribute;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessageAttribute;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.MessageIdentifier;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.protocolimplv2.messages.DeviceMessageSpecImpl;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlTransient;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Straightforward implementation of an OfflineDeviceMessage
 * <p>
 *
 * Date: 11/6/14
 * Time: 8:55 AM
 */
public class OfflineDeviceMessageImpl implements OfflineDeviceMessage {

    private final DeviceMessage deviceMessage;
    private final DeviceProtocol deviceProtocol;
    private final OfflineDevice offlineDevice;
    private IdentificationService identificationService;
    private ProtocolPluggableService protocolPluggableService;
    private DeviceMessageSpecificationService deviceMessageSpecificationService;
    private DeviceMessageSpec specification;
    private DeviceMessageStatus deviceMessageStatus;
    private long deviceMessageId;
    private List<OfflineDeviceMessageAttribute> deviceMessageAttributes;
    private long deviceId;
    private String deviceSerialNumber;
    private String trackingId;
    private String protocolInfo;
    private Instant releaseDate;
    private Instant creationDate;
    private Device device;
    private String preparedContext;
    private boolean firmwareMessage;
    private DeviceIdentifier deviceIdentifier;
    private MessageIdentifier messageIdentifier;

    /**
     * Constructor only to be used by JSON (de)marshalling
     */
    public OfflineDeviceMessageImpl() {
        this.deviceMessage = null;
        this.deviceProtocol = null;
        this.offlineDevice = null;
    }

    public OfflineDeviceMessageImpl(DeviceMessage deviceMessage, DeviceProtocol deviceProtocol, IdentificationService identificationService, ProtocolPluggableService protocolPluggableService, DeviceMessageSpecificationService deviceMessageSpecificationService, OfflineDevice offlineDevice) {
        this.deviceMessage = deviceMessage;
        this.deviceProtocol = deviceProtocol;
        this.identificationService = identificationService;
        this.protocolPluggableService = protocolPluggableService;
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
        this.offlineDevice = offlineDevice;
        goOffline();
    }

    private void goOffline() {
        Device device = this.deviceMessage.getDevice();
        this.deviceMessageId = this.deviceMessage.getId();
        this.specification = this.protocolPluggableService.adapt(this.deviceMessage.getSpecification());
        this.firmwareMessage = this.deviceMessageSpecificationService.getFirmwareCategory().getId() == specification.getCategory().getId();
        this.deviceId = device.getId();
        this.deviceSerialNumber = device.getSerialNumber();
        this.releaseDate = this.deviceMessage.getReleaseDate();
        this.trackingId = this.deviceMessage.getTrackingId();
        this.protocolInfo = this.deviceMessage.getProtocolInfo();
        this.deviceMessageStatus = this.deviceMessage.getStatus();
        this.creationDate = this.deviceMessage.getCreationDate();
        this.device = device;

        List<DeviceMessageAttribute> attributes = this.deviceMessage.getAttributes().stream()
                .map(DeviceMessageAttribute.class::cast)      //Downcast to Connexo DeviceMessageAttribute
                .collect(Collectors.toList());
        getOfflineDeviceMessageAttributes(attributes);

        this.preparedContext = deviceProtocol.prepareMessageContext(device, offlineDevice, deviceMessage).orElse("");
    }

    private void getOfflineDeviceMessageAttributes(List<DeviceMessageAttribute> attributes) {
        this.deviceMessageAttributes = new ArrayList<>(attributes.size());
        this.deviceMessageAttributes.addAll(attributes.stream().map(attribute -> new OfflineDeviceMessageAttributeImpl(attribute, this, offlineDevice, deviceProtocol, this.protocolPluggableService)).collect(Collectors.toList()));
    }

    @Override
    public long getDeviceMessageId() {
        return deviceMessageId;
    }

    @Override
    @JsonIgnore
    @XmlTransient
    public DeviceProtocol getDeviceProtocol() {
        return deviceProtocol;
    }

    @Override
    @XmlElement(type = DeviceMessageSpecImpl.class)
    public DeviceMessageSpec getSpecification() {
        return specification;
    }

    @XmlElements( {
            @XmlElement(type = DeviceMessageIdentifierById.class),
            @XmlElement(type = DeviceMessageIdentifierByDeviceAndProtocolInfoParts.class),
    })
    @Override
    public MessageIdentifier getMessageIdentifier() {
        if (messageIdentifier == null && this.identificationService != null)
            messageIdentifier = this.identificationService.createMessageIdentifierForAlreadyKnownMessage(deviceMessage.getId(), deviceMessage.getDeviceIdentifier());
        return messageIdentifier;
    }

    @Override
    public long getDeviceId() {
        return this.deviceId;
    }

    @Override
    public Date getReleaseDate() {
        return releaseDate == null ? null : Date.from(releaseDate);
    }

    @Override
    public String getTrackingId() {
        return this.trackingId;
    }

    @Override
    public String getProtocolInfo() {
        return this.protocolInfo;
    }

    @Override
    public DeviceMessageStatus getDeviceMessageStatus() {
        return this.deviceMessageStatus;
    }

    @Override
    public Date getCreationDate() {
        return creationDate == null ? null : Date.from(creationDate);
    }

    @Override
    @XmlElement(type = OfflineDeviceMessageAttributeImpl.class)
    public List<OfflineDeviceMessageAttribute> getDeviceMessageAttributes() {
        return this.deviceMessageAttributes;
    }

    @Override
    public String getDeviceSerialNumber() {
        return this.deviceSerialNumber;
    }

    @Override
    @XmlElements( {
            @XmlElement(type = DeviceIdentifierById.class),
            @XmlElement(type = DeviceIdentifierBySerialNumber.class),
            @XmlElement(type = DeviceIdentifierByMRID.class),
            @XmlElement(type = DeviceIdentifierForAlreadyKnownDevice.class),
            @XmlElement(type = DeviceIdentifierByDeviceName.class),
            @XmlElement(type = DeviceIdentifierByConnectionTypeAndProperty.class),
    })
    public DeviceIdentifier getDeviceIdentifier() {
        if (identificationService != null)
            deviceIdentifier = identificationService.createDeviceIdentifierForAlreadyKnownDevice(device.getId(), device.getmRID());
        return deviceIdentifier;
    }

    @Override
    @XmlAttribute
    public boolean isFirmwareMessage() {
        return firmwareMessage;
    }

    @Override
    public String getPreparedContext() {
        return preparedContext;
    }

    @Override
    @XmlElement(name = "type")
    public String getXmlType() {
        return getClass().getName();
    }

    @Override
    public void setXmlType(String ignore) {
        // For xml unmarshalling purposes only
    }
}
