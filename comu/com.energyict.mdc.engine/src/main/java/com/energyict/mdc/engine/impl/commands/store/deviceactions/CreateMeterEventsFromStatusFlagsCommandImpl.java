/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandType;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CreateMeterEventsFromStatusFlagsCommand;
import com.energyict.mdc.engine.impl.commands.collect.LoadProfileCommand;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.impl.meterdata.DeviceLoadProfile;
import com.energyict.mdc.engine.impl.meterdata.DeviceLogBook;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.data.CollectedData;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.mdc.protocol.api.device.events.MeterProtocolEvent;
import com.energyict.mdc.protocol.api.services.IdentificationService;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Will create proper Events from the reading qualities on the interval data.
 */
public class CreateMeterEventsFromStatusFlagsCommandImpl extends SimpleComCommand implements CreateMeterEventsFromStatusFlagsCommand {

    private LoadProfileCommand loadProfileCommand;
    private List<ExecutedDeviceLoadProfile> deviceLoadProfiles = new ArrayList<>();

    public CreateMeterEventsFromStatusFlagsCommandImpl(final GroupedDeviceCommand groupedDeviceCommand, final LoadProfileCommand loadProfileCommand) {
        super(groupedDeviceCommand);
        this.loadProfileCommand = loadProfileCommand;
    }

    @Override
    public String getDescriptionTitle() {
        return "Create meter events from load profile reading qualities";
    }

    @Override
    protected void toJournalMessageDescription(DescriptionBuilder builder, LogLevel serverLogLevel) {
        super.toJournalMessageDescription(builder, serverLogLevel);
        this.appendNrOfMeterEventsCreated(builder);
    }

    private void appendNrOfMeterEventsCreated(DescriptionBuilder builder) {
        for (ExecutedDeviceLoadProfile executedDeviceLoadProfile : this.deviceLoadProfiles) {
            executedDeviceLoadProfile.appendTo(builder);
        }
    }

    @Override
    public void doExecute(final DeviceProtocol deviceProtocol, ExecutionContext executionContext) {
        for (CollectedData collectedData : loadProfileCommand.getCollectedData()) {
            if (collectedData instanceof DeviceLoadProfile) {
                DeviceLoadProfile deviceLoadProfile = (DeviceLoadProfile) collectedData;
                ExecutedDeviceLoadProfile executedDeviceLoadProfile = new ExecutedDeviceLoadProfile(deviceLoadProfile);
                this.deviceLoadProfiles.add(executedDeviceLoadProfile);
                List<MeterProtocolEvent> collectedMeterEvents = new ArrayList<>();
                for (IntervalData intervalData : deviceLoadProfile.getCollectedIntervalData()) {
                    List<MeterEvent> meterEvents = intervalData.generateEvents();
                    executedDeviceLoadProfile.setNrOfMeterEventsCreated(meterEvents.size());
                    collectedMeterEvents.addAll(MeterEvent.mapMeterEventsToMeterProtocolEvents(meterEvents, getCommandRoot().getServiceProvider().meteringService()));
                }
                DeviceIdentifier deviceIdentifier = loadProfileCommand.getOfflineDevice().getDeviceIdentifier();
                IdentificationService identificationService = getCommandRoot().getServiceProvider().identificationService();
                DeviceLogBook deviceLogBook = new DeviceLogBook(identificationService.createLogbookIdentifierByObisCodeAndDeviceIdentifier(DeviceLogBook.GENERIC_LOGBOOK_TYPE_OBISCODE, deviceIdentifier));
                deviceLogBook.setMeterEvents(collectedMeterEvents);
                this.loadProfileCommand.addCollectedDataItem(deviceLogBook);
            }
        }
    }

    @Override
    public ComCommandType getCommandType() {
        return ComCommandTypes.CREATE_METER_EVENTS_IN_LOAD_PROFILE_FROM_STATUS_FLAGS;
    }

    private class ExecutedDeviceLoadProfile {
        private DeviceLoadProfile loadProfile;
        private int nrOfMeterEventsCreated = -1;

        private ExecutedDeviceLoadProfile(DeviceLoadProfile loadProfile) {
            super();
            this.loadProfile = loadProfile;
        }

        private void appendTo(DescriptionBuilder builder) {
            if (nrOfMeterEventsCreated <= 0) {
                builder.addLabel(MessageFormat.format("No events created from profile {0}", this.loadProfile.getLoadProfileIdentifier()));
            } else {
                builder.addLabel(MessageFormat.format("Created {0} event(s) from profile {1}", this.nrOfMeterEventsCreated, this.loadProfile.getLoadProfileIdentifier()));
            }
        }

        private void setNrOfMeterEventsCreated(int nrOfMeterEventsCreated) {
            this.nrOfMeterEventsCreated = nrOfMeterEventsCreated;
        }
    }
}