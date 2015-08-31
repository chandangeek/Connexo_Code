package com.energyict.mdc.engine.impl.commands.offline;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.MessageIdentifier;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageAttribute;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessageAttribute;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.protocol.api.services.IdentificationService;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Straightforward implementation of an OfflineDeviceMessage
 *
 * Copyrights EnergyICT
 * Date: 11/6/14
 * Time: 8:55 AM
 */
public class OfflineDeviceMessageImpl implements OfflineDeviceMessage {

    private final DeviceMessage deviceMessage;
    private final DeviceProtocol deviceProtocol;
    private IdentificationService identificationService;

    private DeviceMessageSpec specification;
    private DeviceMessageStatus deviceMessageStatus;
    private DeviceMessageId deviceMessageId;
    private List<OfflineDeviceMessageAttribute> deviceMessageAttributes;
    private long deviceId;
    private String deviceSerialNumber;
    private String trackingId;
    private String protocolInfo;
    private Instant releaseDate;
    private Instant creationDate;
    private BaseDevice device;

    /**
     * Constructor only to be used by JSON (de)marshalling
     */
    public OfflineDeviceMessageImpl() {
        this.deviceMessage = null;
        this.deviceProtocol = null;
    }

    public OfflineDeviceMessageImpl(DeviceMessage deviceMessage, DeviceProtocol deviceProtocol, IdentificationService identificationService) {
        this.deviceMessage = deviceMessage;
        this.deviceProtocol = deviceProtocol;
        this.identificationService = identificationService;
        goOffline();
    }

    private void goOffline() {
        this.deviceMessageId = this.deviceMessage.getDeviceMessageId();
        this.specification = this.deviceMessage.getSpecification();
        this.deviceId = this.deviceMessage.getDevice().getId();
        this.deviceSerialNumber = this.deviceMessage.getDevice().getSerialNumber();
        this.releaseDate = this.deviceMessage.getReleaseDate();
        this.trackingId = this.deviceMessage.getTrackingId();
        this.protocolInfo = this.deviceMessage.getProtocolInfo();
        this.deviceMessageStatus = this.deviceMessage.getStatus();
        this.creationDate = this.deviceMessage.getCreationDate();
        this.device = this.deviceMessage.getDevice();
        getOfflineDeviceMessageAttributes(this.deviceMessage.getAttributes());
    }

    private void getOfflineDeviceMessageAttributes(List<DeviceMessageAttribute> attributes) {
        this.deviceMessageAttributes = new ArrayList<>(attributes.size());
        for (DeviceMessageAttribute attribute : attributes) {
            this.deviceMessageAttributes.add(new OfflineDeviceMessageAttributeImpl(attribute, deviceProtocol));
        }
    }

    @Override
    public DeviceMessageId getDeviceMessageId() {
        return deviceMessageId;
    }

    @Override
    public DeviceProtocol getDeviceProtocol() {
        return deviceProtocol;
    }

    @Override
    public DeviceMessageSpec getSpecification () {
        return specification;
    }

    @Override
    public MessageIdentifier getIdentifier() {
        return this.identificationService.createMessageIdentifierForAlreadyKnownMessage(deviceMessage);
    }

    @Override
    public long getDeviceId() {
        return this.deviceId;
    }

    @Override
    public Instant getReleaseDate() {
        return this.releaseDate;
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
    public Instant getCreationDate() {
        return this.creationDate;
    }

    @Override
    public List<OfflineDeviceMessageAttribute> getDeviceMessageAttributes() {
        return this.deviceMessageAttributes;
    }

    @Override
    public String getDeviceSerialNumber() {
        return this.deviceSerialNumber;
    }

    @Override
    public DeviceIdentifier getDeviceIdentifier() {
        return this.identificationService.createDeviceIdentifierForAlreadyKnownDevice(device);
    }
}
