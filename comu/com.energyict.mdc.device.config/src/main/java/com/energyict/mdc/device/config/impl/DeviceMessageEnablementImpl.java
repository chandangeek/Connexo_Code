package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.orm.callback.PersistenceAware;
import com.energyict.mdc.device.config.DeviceCommunicationConfiguration;
import com.energyict.mdc.device.config.DeviceMessageEnablement;
import com.energyict.mdc.device.config.DeviceMessageUserAction;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import javax.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Copyrights EnergyICT
 * Date: 9/30/14
 * Time: 1:36 PM
 */
public class DeviceMessageEnablementImpl extends PersistentIdObject<DeviceMessageEnablement> implements DeviceMessageEnablement, PersistenceAware {

    static class DeviceMessageUserActionRecord {

        private DeviceMessageUserAction userAction;
        private Reference<DeviceMessageEnablement> deviceMessageEnablement = ValueReference.absent();
        private String userName;
        private long version;
        private Instant createTime;
        private Instant modTime;

        DeviceMessageUserActionRecord() {
        }

        DeviceMessageUserActionRecord(DeviceMessageEnablement deviceMessageEnablement, DeviceMessageUserAction userAction) {
            this();
            this.deviceMessageEnablement.set(deviceMessageEnablement);
            this.userAction = userAction;
        }

    }

    private Set<DeviceMessageUserAction> deviceMessageUserActions = new HashSet<>();

    private List<DeviceMessageUserActionRecord> deviceMessageUserActionRecords = new ArrayList<>();
    private Reference<DeviceCommunicationConfiguration> deviceCommunicationConfiguration = ValueReference.absent();
    private DeviceMessageId deviceMessageId;
    private String userName;
    private long version;
    private Instant createTime;
    private Instant modTime;

    static DeviceMessageEnablement from(DataModel dataModel, DeviceCommunicationConfigurationImpl deviceCommunicationConfiguration, DeviceMessageId deviceMessageId) {
        return dataModel.getInstance(DeviceMessageEnablementImpl.class).init(deviceCommunicationConfiguration, deviceMessageId);
    }

    @Inject
    public DeviceMessageEnablementImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus) {
        super(DeviceMessageEnablement.class, dataModel, eventService, thesaurus);
    }

    @Override
    public Set<DeviceMessageUserAction> getUserActions() {
        return deviceMessageUserActions;
    }

    private DeviceMessageEnablement init(DeviceCommunicationConfiguration deviceCommunicationConfiguration, DeviceMessageId deviceMessageId) {
        setDeviceCommunicationConfiguration(deviceCommunicationConfiguration);
        this.deviceMessageId = deviceMessageId;
        return this;
    }

    @Override
    public DeviceMessageId getDeviceMessageId() {
        return deviceMessageId;
    }

    @Override
    public boolean removeDeviceMessageUserAction(DeviceMessageUserAction deviceMessageUserAction) {
        boolean removed = deviceMessageUserActions.remove(deviceMessageUserAction);
        return removed && deviceMessageUserActionRecords.removeIf(deviceMessageUserActionRecord -> deviceMessageUserActionRecord.userAction.equals(deviceMessageUserAction));
    }

    @Override
    public boolean addDeviceMessageUserAction(DeviceMessageUserAction deviceMessageUserAction) {
        boolean added = this.deviceMessageUserActions.add(deviceMessageUserAction);
        return added && this.deviceMessageUserActionRecords.add(new DeviceMessageUserActionRecord(this, deviceMessageUserAction));
    }

    @Override
    public void addDeviceMessageCategory(DeviceMessageCategory deviceMessageCategory) {
//            no implementation yet, as the requirement stated that we don't need to store full categories

    }

    @Override
    protected CreateEventType createEventType() {
        return CreateEventType.DEVICE_MESSAGE_ENABLEMENT;
    }

    @Override
    protected UpdateEventType updateEventType() {
        return UpdateEventType.DEVICE_MESSAGE_ENABLEMENT;
    }

    @Override
    protected DeleteEventType deleteEventType() {
        return DeleteEventType.DEVICE_MESSAGE_ENABLEMENT;
    }

    @Override
    protected void doDelete() {
        dataModel.mapper(DeviceMessageEnablement.class).remove(this);
    }

    @Override
    protected void validateDelete() {

    }

    @Override
    public void postLoad() {
        deviceMessageUserActions.addAll(deviceMessageUserActionRecords.stream().map(userActionRecord -> userActionRecord.userAction).collect(Collectors.toList()));
    }

    protected void setDeviceCommunicationConfiguration(DeviceCommunicationConfiguration deviceCommunicationConfiguration) {
        this.deviceCommunicationConfiguration.set(deviceCommunicationConfiguration);
    }
}
