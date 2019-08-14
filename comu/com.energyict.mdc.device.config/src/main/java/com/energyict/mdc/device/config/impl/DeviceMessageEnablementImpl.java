/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.orm.callback.PersistenceAware;
import com.energyict.mdc.common.device.config.DeleteEventType;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.config.DeviceMessageEnablement;
import com.energyict.mdc.common.device.config.DeviceMessageEnablementBuilder;
import com.energyict.mdc.common.device.config.DeviceMessageUserAction;
import com.energyict.mdc.common.protocol.DeviceMessageCategory;
import com.energyict.mdc.common.protocol.DeviceMessageId;

import javax.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

class DeviceMessageEnablementImpl extends PersistentIdObject<DeviceMessageEnablement> implements ServerDeviceMessageEnablement, PersistenceAware {

    static class DeviceMessageUserActionRecord {

        private DeviceMessageUserAction userAction;
        private Reference<DeviceMessageEnablement> deviceMessageEnablement = ValueReference.absent();
        @SuppressWarnings("unused")
        private String userName;
        @SuppressWarnings("unused")
        private long version;
        @SuppressWarnings("unused")
        private Instant createTime;
        @SuppressWarnings("unused")
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
    private Reference<DeviceConfiguration> deviceConfiguration = ValueReference.absent();
    private transient DeviceMessageId deviceMessageId;
    private long deviceMessageIdDbValue;

    @SuppressWarnings("unused")
    private String userName;
    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;

    static DeviceMessageEnablementImpl from(DataModel dataModel, DeviceConfigurationImpl deviceConfiguration, DeviceMessageId deviceMessageId) {
        return dataModel.getInstance(DeviceMessageEnablementImpl.class).init(deviceConfiguration, deviceMessageId);
    }

    @Inject
    DeviceMessageEnablementImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus) {
        super(DeviceMessageEnablement.class, dataModel, eventService, thesaurus);
    }

    @Override
    public Set<DeviceMessageUserAction> getUserActions() {
        return deviceMessageUserActions;
    }

    private DeviceMessageEnablementImpl init(DeviceConfiguration deviceConfiguration, DeviceMessageId deviceMessageId) {
        setDeviceConfiguration(deviceConfiguration);
        this.deviceMessageId = deviceMessageId;
        this.deviceMessageIdDbValue = deviceMessageId.dbValue();
        return this;
    }

    public long getDeviceMessageDbValue(){
        return deviceMessageIdDbValue;
    }

    @Override
    public DeviceMessageId getDeviceMessageId() {
        if (deviceMessageId == null){
            deviceMessageId = DeviceMessageId.from(this.deviceMessageIdDbValue);
        }
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
        this.getDataModel().mapper(DeviceMessageEnablement.class).remove(this);
    }

    @Override
    protected void validateDelete() {
        // Nothing to validate
    }

    @Override
    public void postLoad() {
        deviceMessageUserActions.addAll(deviceMessageUserActionRecords.stream().map(userActionRecord -> userActionRecord.userAction).collect(Collectors.toList()));
    }

    protected void setDeviceConfiguration(DeviceConfiguration deviceConfiguration) {
        this.deviceConfiguration.set(deviceConfiguration);
    }

    @Override
    public DeviceConfiguration getDeviceConfiguration() {
        return deviceConfiguration.get();
    }

    @Override
    public DeviceMessageEnablement cloneForDeviceConfig(DeviceConfiguration deviceConfiguration) {
        DeviceMessageEnablementBuilder deviceMessageEnablement = deviceConfiguration.createDeviceMessageEnablement(getDeviceMessageId());
        getUserActions().forEach(deviceMessageEnablement::addUserAction);
        return deviceMessageEnablement.build();
    }

    void prepareDelete() {
        this.deviceMessageUserActionRecords.clear();
    }

}