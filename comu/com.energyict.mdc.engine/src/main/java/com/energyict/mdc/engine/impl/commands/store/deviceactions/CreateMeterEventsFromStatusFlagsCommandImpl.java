package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.comserver.commands.core.SimpleComCommand;
import com.energyict.comserver.core.JobExecution;
import com.energyict.comserver.logging.LogLevel;
import com.energyict.mdc.commands.ComCommandTypes;
import com.energyict.mdc.commands.CommandRoot;
import com.energyict.mdc.commands.CreateMeterEventsFromStatusFlagsCommand;
import com.energyict.mdc.commands.LoadProfileCommand;
import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.common.comserver.logging.PropertyDescriptionBuilder;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.meterdata.DeviceLoadProfile;
import com.energyict.mdc.meterdata.DeviceLogBook;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.data.CollectedData;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.mdc.protocol.api.device.events.MeterProtocolEvent;
import com.energyict.mdc.protocol.inbound.DeviceIdentifierById;
import com.energyict.mdw.core.LogBookTypeFactory;
import com.energyict.protocolimplv2.identifiers.LogBookIdentifierByDeviceAndObisCode;
import java.util.ArrayList;
import java.util.List;

/**
 * Will create proper Events from intervalFlags.
 */
public class CreateMeterEventsFromStatusFlagsCommandImpl extends SimpleComCommand implements CreateMeterEventsFromStatusFlagsCommand {

    private LoadProfileCommand loadProfileCommand;
    private List<ExecutedDeviceLoadProfile> deviceLoadProfiles = new ArrayList<>();

    public CreateMeterEventsFromStatusFlagsCommandImpl(final LoadProfileCommand loadProfileCommand, final CommandRoot commandRoot) {
        super(commandRoot);
        this.loadProfileCommand = loadProfileCommand;
    }

    @Override
    protected void toJournalMessageDescription (DescriptionBuilder builder, LogLevel serverLogLevel) {
        super.toJournalMessageDescription(builder, serverLogLevel);
        if (this.isJournalingLevelEnabled(serverLogLevel, LogLevel.DEBUG)) {
            if (!this.loadProfileCommand.getLoadProfilesTask().getLoadProfileTypes().isEmpty()) {
                this.appendLoadProfileObisCodes(builder);
                this.appendMeterEvents(builder);
            }
            else {
                builder.addLabel("No load profile obis codes");
            }
        }
    }

    private void appendLoadProfileObisCodes (DescriptionBuilder builder) {
        PropertyDescriptionBuilder propertyBuilder = builder.addListProperty("Load profile obisCodes");
        for (LoadProfileType loadProfileType : loadProfileCommand.getLoadProfilesTask().getLoadProfileTypes()) {
            propertyBuilder = propertyBuilder.append(loadProfileType.getObisCode()).next();
        }
    }

    private void appendMeterEvents (DescriptionBuilder builder) {
        PropertyDescriptionBuilder propertyBuilder = builder.addListProperty("meterEvents");
        for (ExecutedDeviceLoadProfile executedDeviceLoadProfile : this.deviceLoadProfiles) {
            executedDeviceLoadProfile.appendTo(propertyBuilder);
        }
    }

    @Override
    public void doExecute(final DeviceProtocol deviceProtocol, JobExecution.ExecutionContext executionContext) {
        for (CollectedData collectedData : loadProfileCommand.getCollectedData()) {
            if (collectedData instanceof DeviceLoadProfile) {
                DeviceLoadProfile deviceLoadProfile = (DeviceLoadProfile) collectedData;
                ExecutedDeviceLoadProfile executedDeviceLoadProfile = new ExecutedDeviceLoadProfile(deviceLoadProfile);
                this.deviceLoadProfiles.add(executedDeviceLoadProfile);
                List<MeterProtocolEvent> collectedMeterEvents = new ArrayList<>();
                for (IntervalData intervalData : deviceLoadProfile.getCollectedIntervalData()) {
                    List<MeterEvent> meterEvents = intervalData.generateEvents();
                    if (!meterEvents.isEmpty()) {
                        executedDeviceLoadProfile.noMeterEventsCreated = false;
                    }
                    collectedMeterEvents.addAll(MeterEvent.mapMeterEventsToMeterProtocolEvents(meterEvents));
                }
                DeviceIdentifierById deviceIdentifier = new DeviceIdentifierById(loadProfileCommand.getOfflineDevice().getId());
                DeviceLogBook deviceLogBook = new DeviceLogBook(new LogBookIdentifierByDeviceAndObisCode(deviceIdentifier, LogBookTypeFactory.GENERIC_LOGBOOK_TYPE_OBISCODE));
                deviceLogBook.setMeterEvents(collectedMeterEvents);
                this.loadProfileCommand.addCollectedDataItem(deviceLogBook);
            }
        }
    }

    @Override
    public ComCommandTypes getCommandType() {
        return ComCommandTypes.CREATE_METER_EVENTS_IN_LOAD_PROFILE_FROM_STATUS_FLAGS;
    }

    private class ExecutedDeviceLoadProfile {
        private DeviceLoadProfile loadProfile;
        private boolean noMeterEventsCreated = true;

        private ExecutedDeviceLoadProfile (DeviceLoadProfile loadProfile) {
            super();
            this.loadProfile = loadProfile;
        }

        private void appendTo (PropertyDescriptionBuilder builder) {
            if (this.noMeterEventsCreated) {
                builder.append("No meter events created from load profile status flags of load profile ");
            }
            else {
                builder.append("Created meter events from load profile status flags of load profile ");
            }
            builder.append(this.loadProfile.getLoadProfileIdentifier());
        }

    }

}