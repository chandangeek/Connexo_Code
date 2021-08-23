/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.meterdata;

import com.energyict.mdc.engine.impl.commands.store.DeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.MeterDataStoreCommand;
import com.energyict.mdc.engine.impl.commands.store.UpdateUmiwanStructure;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.mdc.upl.meterdata.identifiers.MessageIdentifier;
import com.energyict.mdc.upl.tasks.DataCollectionConfiguration;

import java.time.Instant;
import java.util.Map;

public class UmiwanStructure extends CollectedDeviceData implements CollectedMessage {

    private MessageIdentifier messageIdentifier;
    private DeviceMessageStatus deviceMessageStatus;
    private Map<String, Object> properties;
    private String deviceProtocolInformation;
    private DataCollectionConfiguration configuration;
    private Instant sentDate;
    private String cas;

    public UmiwanStructure(MessageIdentifier messageIdentifier, Map<String, Object> properties, String structureCAS) {
        this.messageIdentifier = messageIdentifier;
        this.properties = properties;
        this.cas = structureCAS;
    }

    @Override
    public boolean isConfiguredIn(DataCollectionConfiguration configuration) {
        return false;
    }

    @Override
    public DeviceCommand toDeviceCommand(MeterDataStoreCommand meterDataStoreCommand, DeviceCommand.ServiceProvider serviceProvider) {
        return new UpdateUmiwanStructure(this, this.getComTaskExecution(), serviceProvider, cas);
    }

    @Override
    public MessageIdentifier getMessageIdentifier() {
        return messageIdentifier;
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
        this.configuration = configuration;
    }

    @Override
    public Instant getSentDate() {
        return this.sentDate;
    }

    @Override
    public void setSentDate(Instant sentDate) {
        this.sentDate = sentDate;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }
}