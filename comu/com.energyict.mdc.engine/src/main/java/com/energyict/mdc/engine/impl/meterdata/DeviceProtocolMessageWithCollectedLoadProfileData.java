/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.meterdata;

import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.commands.store.CollectedLoadProfileDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.MeterDataStoreCommand;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.meterdata.CollectedData;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.mdc.upl.meterdata.identifiers.MessageIdentifier;
import com.energyict.mdc.upl.tasks.DataCollectionConfiguration;

import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.LoadProfileReader;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * An implementation of the {@link CollectedMessage} interface,
 * containing additional {@link CollectedLoadProfile} data.
 *
 * @author sva
 * @since 17/06/13 - 11:23
 */
public class DeviceProtocolMessageWithCollectedLoadProfileData extends CollectedDeviceData implements CollectedMessage {

    private final MessageIdentifier deviceMessageIdentifier;
    private final CollectedLoadProfile collectedLoadProfile;
    private LoadProfileReader loadProfileReader;

    private Instant sentDate;
    private DeviceMessageStatus deviceMessageStatus;
    private String deviceProtocolInformation;

    public DeviceProtocolMessageWithCollectedLoadProfileData(MessageIdentifier deviceMessageIdentifier, CollectedLoadProfile collectedLoadProfile) {
        this.deviceMessageIdentifier = deviceMessageIdentifier;
        this.collectedLoadProfile = collectedLoadProfile;
    }

    public DeviceProtocolMessageWithCollectedLoadProfileData(MessageIdentifier deviceMessageIdentifier, CollectedLoadProfile collectedLoadProfile, LoadProfileReader loadProfileReader) {
        this(deviceMessageIdentifier, collectedLoadProfile);
        this.loadProfileReader = loadProfileReader;
    }

    @Override
    public boolean isConfiguredIn(DataCollectionConfiguration configuration) {
        return configuration.isConfiguredToSendMessages();
    }

    @Override
    public MessageIdentifier getMessageIdentifier() {
        return this.deviceMessageIdentifier;
    }

    @Override
    public Instant getSentDate() {
        return sentDate;
    }

    public void setSentDate(Instant sentDate) {
        this.sentDate = sentDate;
    }

    @Override
    public DeviceMessageStatus getNewDeviceMessageStatus() {
        return this.deviceMessageStatus;
    }

    public void setNewDeviceMessageStatus(DeviceMessageStatus deviceMessageStatus) {
        this.deviceMessageStatus = deviceMessageStatus;
    }

    @Override
    public String getDeviceProtocolInformation() {
        return this.deviceProtocolInformation;
    }

    @Override
    public void setDeviceProtocolInformation(String deviceProtocolInformation) {
        this.deviceProtocolInformation = deviceProtocolInformation;
    }

    @Override
    public void setDataCollectionConfiguration(DataCollectionConfiguration configuration) {

    }

    @Override
    public void injectComTaskExecution(ComTaskExecution comTaskExecution) {
        super.injectComTaskExecution(comTaskExecution);
        if (this.collectedLoadProfile != null && this.collectedLoadProfile instanceof ServerCollectedData) {
            ((ServerCollectedData) collectedLoadProfile).injectComTaskExecution(comTaskExecution);
        }
    }

    @Override
    public DeviceCommand toDeviceCommand(MeterDataStoreCommand meterDataStoreCommand, DeviceCommand.ServiceProvider serviceProvider) {
        if (loadProfileReader != null) {
            CollectedLoadProfileHelper.addReadingTypeToChannelInfo(collectedLoadProfile, loadProfileReader);
        }
        return new CollectedLoadProfileDeviceCommand(collectedLoadProfile, this.getComTaskExecution(), meterDataStoreCommand, serviceProvider);
    }

}