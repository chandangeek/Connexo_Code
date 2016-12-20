package com.energyict.mdc.engine.impl.commands.offline;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageAttribute;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessageAttribute;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.protocol.pluggable.impl.adapters.upl.ConnexoDeviceMessageSpecAdapter;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.MessageIdentifier;
import com.energyict.mdc.upl.offline.OfflineDevice;

import javax.xml.bind.annotation.XmlElement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Straightforward implementation of an OfflineDeviceMessage
 * <p>
 * Copyrights EnergyICT
 * Date: 11/6/14
 * Time: 8:55 AM
 */
public class OfflineDeviceMessageImpl implements OfflineDeviceMessage {

    private final DeviceMessage deviceMessage;
    private final DeviceProtocol deviceProtocol;
    private final OfflineDevice offlineDevice;
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
    private Device device;
    private String preparedContext;
    private String deviceMessageSpecPrimaryKey;

    /**
     * Constructor only to be used by JSON (de)marshalling
     */
    private OfflineDeviceMessageImpl() {
        this.deviceMessage = null;
        this.deviceProtocol = null;
        this.offlineDevice = null;
    }

    public OfflineDeviceMessageImpl(DeviceMessage deviceMessage, DeviceProtocol deviceProtocol, IdentificationService identificationService, OfflineDevice offlineDevice) {
        this.deviceMessage = deviceMessage;
        this.deviceProtocol = deviceProtocol;
        this.identificationService = identificationService;
        this.offlineDevice = offlineDevice;
        goOffline();
    }

    private void goOffline() {
        Device device = ((Device) this.deviceMessage.getDevice());      //Downcast to Connexo Device

        this.deviceMessageId = this.deviceMessage.getDeviceMessageId();
        this.specification = new ConnexoDeviceMessageSpecAdapter(this.deviceMessage.getSpecification());
        this.deviceMessageSpecPrimaryKey = specification.getPrimaryKey().getValue();
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

        this.preparedContext = deviceProtocol.prepareMessageContext(offlineDevice, deviceMessage);
    }

    private void getOfflineDeviceMessageAttributes(List<DeviceMessageAttribute> attributes) {
        this.deviceMessageAttributes = new ArrayList<>(attributes.size());
        this.deviceMessageAttributes.addAll(attributes.stream().map(attribute -> new OfflineDeviceMessageAttributeImpl(attribute, this, offlineDevice, deviceProtocol)).collect(Collectors.toList()));
    }

    @Override
    public long getDeviceMessageId() {
        return deviceMessageId.dbValue();
    }

    @Override
    public DeviceProtocol getDeviceProtocol() {
        return deviceProtocol;
    }

    @Override
    public DeviceMessageSpec getSpecification() {
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

    @Override
    public String getDeviceMessageSpecPrimaryKey() {
        return deviceMessageSpecPrimaryKey;
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