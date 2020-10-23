package com.energyict.mdc.engine.impl.meterdata;

import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.MeterDataStoreCommand;
import com.energyict.mdc.engine.impl.commands.store.UpdateDeviceSecurityProperty;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.meterdata.CollectedDeviceInfo;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.MessageIdentifier;
import com.energyict.mdc.upl.tasks.DataCollectionConfiguration;

import java.time.Instant;

/**
 * An implementation of the {@link CollectedMessage} interface,
 * containing additional {@link CollectedDeviceInfo} data.
 *
 * @author khe
 * @since 2017-01-23 (08:38)
 */
public class DeviceProtocolMessageWithCollectedSecurityProperty extends CollectedDeviceData implements CollectedMessage, CollectedDeviceInfo {
    private final MessageIdentifier deviceMessageIdentifier;
    private final DeviceIdentifier deviceIdentifier;
    private final DeviceSecurityProperty deviceSecurityProperty;

    private DeviceMessageStatus deviceMessageStatus;
    private String deviceProtocolInformation;
    private Instant sentDate;
    private DataCollectionConfiguration configuration;

    public DeviceProtocolMessageWithCollectedSecurityProperty(DeviceIdentifier deviceIdentifier, MessageIdentifier messageIdentifier, String propertyName, Object propertyValue) {
        this.deviceIdentifier = deviceIdentifier;
        this.deviceMessageIdentifier = messageIdentifier;
        this.deviceSecurityProperty = new DeviceSecurityProperty(deviceIdentifier, propertyName, propertyValue);
    }

    public DeviceProtocolMessageWithCollectedSecurityProperty(DeviceIdentifier deviceIdentifier, MessageIdentifier messageIdentifier, String propertyName) {
        this.deviceIdentifier = deviceIdentifier;
        this.deviceMessageIdentifier = messageIdentifier;
        this.deviceSecurityProperty = new DeviceSecurityProperty(deviceIdentifier, propertyName);
    }

    @Override
    public boolean isConfiguredIn(DataCollectionConfiguration comTask) {
        return comTask.isConfiguredToSendMessages();
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
    public DeviceCommand toDeviceCommand(MeterDataStoreCommand meterDataStoreCommand, DeviceCommand.ServiceProvider serviceProvider) {
        return new UpdateDeviceSecurityProperty(deviceSecurityProperty, getComTaskExecution(), serviceProvider);
    }

    @Override
    public void injectComTaskExecution(ComTaskExecution comTaskExecution) {
        super.injectComTaskExecution(comTaskExecution);
        deviceSecurityProperty.injectComTaskExecution(comTaskExecution);
    }

    @Override
    public DeviceIdentifier getDeviceIdentifier() {
        return deviceIdentifier;
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

}