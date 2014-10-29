package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.User;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.exceptions.MessageSeeds;
import com.energyict.mdc.device.data.exceptions.UnknownDeviceMessageId;
import com.energyict.mdc.device.data.impl.constraintvalidators.ValidDeviceMessageId;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageAttribute;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Copyrights EnergyICT
 * Date: 10/27/14
 * Time: 1:06 PM
 */
@ValidDeviceMessageId(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.DEVICE_MESSAGE_ID_NOT_SUPPORTED + "}")
public class DeviceMessageImpl extends PersistentIdObject<DeviceMessage> implements DeviceMessage<Device>{

    public enum Fields {
        DEVICEMESSAGEID("deviceMessageId"),
        DEVICEMESSAGESTATUS("deviceMessageStatus"),
        TRACKINGID("trackingId"),
        PROTOCOLINFO("protocolInfo"),
        CREATIONDATE("creationDate"),
        RELEASEDATE("releaseDate"),
        SENTDATE("sentDate"),;

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    private ThreadPrincipalService threadPrincipalService;
    private DeviceMessageService deviceMessageService;
    private Clock clock;

    private long id;
    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.USER_IS_REQUIRED + "}")
    private Reference<User> user = ValueReference.absent();
    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.DEVICE_IS_REQUIRED + "}")
    private Reference<Device> device = ValueReference.absent();
    private DeviceMessageId deviceMessageId;
    private DeviceMessageStatus deviceMessageStatus;
    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.CREATE_DATE_IS_REQUIRED + "}")
    private Instant creationDate;
    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.RELEASE_DATE_IS_REQUIRED + "}")
    private Instant releaseDate;
    private Instant sentDate;
    private String trackingId;
    private String protocolInfo;

    @Inject
    public DeviceMessageImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, ThreadPrincipalService threadPrincipalService, DeviceMessageService deviceMessageService, Clock clock) {
        super(DeviceMessage.class, dataModel, eventService, thesaurus);
        this.threadPrincipalService = threadPrincipalService;
        this.deviceMessageService = deviceMessageService;
        this.clock = clock;
    }

    public DeviceMessageImpl initialize(Device device, DeviceMessageId deviceMessageId){
        this.deviceMessageId = deviceMessageId;
        this.device.set(device);
        this.user.set((User) this.threadPrincipalService.getPrincipal());
        this.creationDate = Instant.now();
        this.deviceMessageStatus = DeviceMessageStatus.WAITING;
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
        return this.deviceMessageService.findMessageSpecById(this.deviceMessageId.dbValue()).orElseThrow(() -> new UnknownDeviceMessageId(getThesaurus(), this.device.get(), this.deviceMessageId));
    }

    @Override
    public DeviceMessageId getDeviceMessageId() {
        return this.deviceMessageId;
    }

    @Override
    public List<DeviceMessageAttribute> getAttributes() {
        return null;
    }

    @Override
    public Device getDevice() {
        return this.device.get();
    }

    @Override
    public DeviceMessageStatus getStatus() {
        if(statusIsPending()){
            return DeviceMessageStatus.PENDING;
        }
        return this.deviceMessageStatus;
    }

    private boolean statusIsPending() {
        return this.deviceMessageStatus == DeviceMessageStatus.WAITING && !getReleaseDate().isAfter(this.clock.instant());
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
        return this.creationDate;
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
    public User getUser() {
        return user.get();
    }

    @Override
    public OfflineDeviceMessage goOffline() {
        return null;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    protected void postNew() {
        this.creationDate = this.clock.instant();
        super.postNew();
    }

    public void setProtocolInfo(String protocolInfo) {
        this.protocolInfo = protocolInfo;
    }

    public void setTrackingId(String trackingId) {
        this.trackingId = trackingId;
    }

    public void setSentDate(Instant sentDate) {
        this.sentDate = sentDate;
    }

    public void setReleaseDate(Instant releaseDate) {
        this.releaseDate = releaseDate;
    }
}
