package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.exceptions.IllegalDeviceMessageIdException;
import com.energyict.mdc.device.data.exceptions.InvalidDeviceMessageStatusMove;
import com.energyict.mdc.device.data.impl.constraintvalidators.HasValidDeviceMessageAttributes;
import com.energyict.mdc.device.data.impl.constraintvalidators.IsRevokeAllowed;
import com.energyict.mdc.device.data.impl.constraintvalidators.UserHasTheMessagePrivilege;
import com.energyict.mdc.device.data.impl.constraintvalidators.ValidDeviceMessageId;
import com.energyict.mdc.device.data.impl.constraintvalidators.ValidReleaseDateUpdate;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageAttribute;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.security.thread.ThreadPrincipalService;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Straightforward implementation of a ServerDeviceMessage
 */
@ValidDeviceMessageId(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.DEVICE_MESSAGE_ID_NOT_SUPPORTED + "}")
@UserHasTheMessagePrivilege(groups = {Save.Create.class, Save.Update.class})
@HasValidDeviceMessageAttributes(groups = {Save.Create.class, Save.Update.class})
public class DeviceMessageImpl extends PersistentIdObject<ServerDeviceMessage> implements ServerDeviceMessage {

    public enum Fields {
        DEVICEMESSAGEID("deviceMessageId"),
        DEVICEMESSAGESTATUS("deviceMessageStatus"),
        TRACKINGID("trackingId"),
        PROTOCOLINFO("protocolInfo"),
        CREATIONDATE("creationDate"),
        RELEASEDATE("releaseDate"),
        SENTDATE("sentDate"),
        DEVICEMESSAGEATTRIBUTES("deviceMessageAttributes"),
        USER("user"),;

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    private ThreadPrincipalService threadPrincipalService;
    private DeviceMessageSpecificationService deviceMessageSpecificationService;
    private Clock clock;

    private String userName;
    private long version;
    private Instant createTime;
    private Instant modTime;
    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.DEVICE_IS_REQUIRED + "}")
    private Reference<Device> device = ValueReference.absent();
    private long deviceMessageId;
    private DeviceMessageStatus deviceMessageStatus;
    @IsRevokeAllowed(groups = {Save.Create.class, Save.Update.class})
    private RevokeChecker revokeChecker;
    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.DEVICE_MESSAGE_RELEASE_DATE_IS_REQUIRED + "}")
    private Instant releaseDate;
    @ValidReleaseDateUpdate(groups = {Save.Create.class, Save.Update.class})
    private ReleaseDateUpdater releaseDateUpdater;
    private Instant sentDate;
    private String trackingId;
    private String protocolInfo;
    private Optional<DeviceMessageSpec> messageSpec;
    @Valid
    private List<DeviceMessageAttribute> deviceMessageAttributes = new ArrayList<>();

    @Inject
    public DeviceMessageImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, ThreadPrincipalService threadPrincipalService, DeviceMessageSpecificationService deviceMessageSpecificationService, Clock clock) {
        super(ServerDeviceMessage.class, dataModel, eventService, thesaurus);
        this.threadPrincipalService = threadPrincipalService;
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
        this.clock = clock;
    }

    public DeviceMessageImpl initialize(Device device, DeviceMessageId deviceMessageId) {
        this.deviceMessageId = deviceMessageId.dbValue();
        this.device.set(device);
        this.deviceMessageStatus = DeviceMessageStatus.WAITING;
        this.messageSpec = this.deviceMessageSpecificationService.findMessageSpecById(this.deviceMessageId);
        return this;
    }

    @Override
    protected CreateEventType createEventType() {
        return CreateEventType.DEVICEMESSAGE;
    }

    @Override
    protected UpdateEventType updateEventType() {
        return UpdateEventType.DEVICEMESSAGE;
    }

    @Override
    protected DeleteEventType deleteEventType() {
        return DeleteEventType.DEVICEMESSAGE;
    }

    @Override
    protected void doDelete() {
        getDataMapper().remove(this);   // the deviceMessageAttributes should have a cascade delete
    }

    @Override
    protected void validateDelete() {
        // Nothing to validate for now
    }

    @Override
    public DeviceMessageSpec getSpecification() {
        return this.deviceMessageSpecificationService.findMessageSpecById(this.deviceMessageId).orElse(null);
    }

    @Override
    public DeviceMessageId getDeviceMessageId() {
        return Stream.of(DeviceMessageId.values()).filter(deviceMessage -> deviceMessage.dbValue() == this.deviceMessageId).findAny().orElseThrow(() -> new IllegalDeviceMessageIdException(this.deviceMessageId, getThesaurus(), MessageSeeds.DEVICE_MESSAGE_ID_NOT_SUPPORTED));
    }

    @Override
    public List<DeviceMessageAttribute> getAttributes() {
        return this.deviceMessageAttributes;
    }

    @Override
    public Device getDevice() {
        return this.device.get();
    }

    @Override
    public DeviceMessageStatus getStatus() {
        if (statusIsPending()) {
            return DeviceMessageStatus.PENDING;
        }
        return this.deviceMessageStatus;
    }

    private boolean statusIsPending() {
        return this.deviceMessageStatus == DeviceMessageStatus.WAITING && this.releaseDate != null && !this.releaseDate.isAfter(this.clock.instant());
    }

    @Override
    public String getProtocolInfo() {
        return this.protocolInfo;
    }

    @Override
    public Instant getReleaseDate() {
        return this.releaseDate;
    }

    @Override
    public Instant getCreationDate() {
        return this.createTime;
    }

    @Override
    public String getTrackingId() {
        return this.trackingId;
    }

    @Override
    public Optional<Instant> getSentDate() {
        return Optional.ofNullable(this.sentDate);
    }

    @Override
    public String getUser() {
        return userName;
    }

    public void setProtocolInfo(String protocolInfo) {
        this.protocolInfo = protocolInfo;
    }

    public void setTrackingId(String trackingId) {
        this.trackingId = trackingId;
    }

    public void setReleaseDate(Instant releaseDate) {
        getReleaseDateUpdater().setNewReleaseDate(releaseDate);
        this.releaseDate = releaseDate;
    }

    @Override
    public void revoke() {
        this.revokeChecker = new RevokeChecker(deviceMessageStatus);
        this.deviceMessageStatus = DeviceMessageStatus.REVOKED;
    }

    void addProperty(String key, Object value) {
        DeviceMessageAttributeImpl deviceMessageAttribute = this.getDataModel().getInstance(DeviceMessageAttributeImpl.class).initialize(this, key);
        deviceMessageAttribute.setValue(value);
        deviceMessageAttributes.add(deviceMessageAttribute);
    }

    @Override
    public void moveTo(DeviceMessageStatus status) {
        if (!getStatus().isPredecessorOf(status)) {
            throw new InvalidDeviceMessageStatusMove(this.deviceMessageStatus, status, getThesaurus(), MessageSeeds.DEVICE_MESSAGE_STATUS_INVALID_MOVE);
        }
        this.deviceMessageStatus = status;
    }

    @Override
    public void setProtocolInformation(String protocolInformation) {
        this.protocolInfo = protocolInformation;
    }

    @Override
    public void updateDeviceMessageStatus(DeviceMessageStatus newDeviceMessageStatus) {
        this.moveTo(newDeviceMessageStatus);
        this.save();
    }

    @Override
    public Instant getModTime() {
        return this.modTime;
    }

    private ReleaseDateUpdater getReleaseDateUpdater() {
        if (this.releaseDateUpdater == null) {
            this.releaseDateUpdater = new ReleaseDateUpdater(getStatus(), this.releaseDate);
        }
        return releaseDateUpdater;
    }

    public class ReleaseDateUpdater {

        private final DeviceMessageStatus status;
        private final Instant initialReleaseDate;
        private Instant newReleaseDate;

        private EnumSet<DeviceMessageStatus> allowedStatusses =
                EnumSet.of(DeviceMessageStatus.PENDING, DeviceMessageStatus.WAITING);

        private ReleaseDateUpdater(DeviceMessageStatus status, Instant initialReleaseDate) {
            this.status = status;
            this.initialReleaseDate = initialReleaseDate;
        }

        public boolean canUpdate() {
            return this.initialReleaseDate == null
                    || this.newReleaseDate != null && this.initialReleaseDate.equals(this.newReleaseDate)
                    || allowedStatusses.stream().anyMatch(deviceMessageStatus -> deviceMessageStatus.equals(this.status));
        }

        public void setNewReleaseDate(Instant releaseDate) {
            newReleaseDate = releaseDate;
        }
    }

    public class RevokeChecker {
        private final DeviceMessageStatus initialStatus;

        private RevokeChecker(DeviceMessageStatus initialStatus) {
            this.initialStatus = initialStatus;
        }

        public boolean isRevokeAllowed(){
            return initialStatus.isPredecessorOf(DeviceMessageStatus.REVOKED);
        }
    }
}
