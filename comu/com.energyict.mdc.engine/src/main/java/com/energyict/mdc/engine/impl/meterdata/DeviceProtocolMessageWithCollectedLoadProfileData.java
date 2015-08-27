package com.energyict.mdc.engine.impl.meterdata;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.commands.store.CollectedLoadProfileDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.MeterDataStoreCommand;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfile;
import com.energyict.mdc.protocol.api.device.data.CollectedMessage;
import com.energyict.mdc.protocol.api.device.data.DataCollectionConfiguration;
import com.energyict.mdc.protocol.api.device.data.identifiers.MessageIdentifier;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;

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

    private DeviceMessageStatus deviceMessageStatus;
    private String deviceProtocolInformation;

    public DeviceProtocolMessageWithCollectedLoadProfileData(MessageIdentifier deviceMessageIdentifier, CollectedLoadProfile collectedLoadProfile) {
        this.deviceMessageIdentifier = deviceMessageIdentifier;
        this.collectedLoadProfile = collectedLoadProfile;
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
        return new CollectedLoadProfileDeviceCommand(collectedLoadProfile, this.getComTaskExecution(), meterDataStoreCommand, serviceProvider);
    }

}