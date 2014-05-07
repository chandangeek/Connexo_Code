package com.energyict.mdc.engine.impl.commands.store;

import com.elster.jupiter.metering.readings.IntervalBlock;
import com.elster.jupiter.metering.readings.IntervalReading;
import com.elster.jupiter.metering.readings.beans.IntervalBlockImpl;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.util.time.Clock;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.protocol.inbound.DeviceIdentifierById;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.device.data.identifiers.LoadProfileIdentifier;
import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.common.comserver.logging.PropertyDescriptionBuilder;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfile;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Provides functionality to store {@link com.energyict.mdc.protocol.api.device.BaseLoadProfile} data into the system
 *
 * Copyrights EnergyICT
 * Date: 29/08/12
 * Time: 14:52
 */
public class CollectedLoadProfileDeviceCommand extends DeviceCommandImpl {

    private final DeviceDataService deviceDataService;
    private final CollectedLoadProfile collectedLoadProfile;

    public CollectedLoadProfileDeviceCommand(CollectedLoadProfile collectedLoadProfile, IssueService issueService, DeviceDataService deviceDataService, Clock clock) {
        super(issueService, clock);
        this.collectedLoadProfile = collectedLoadProfile;
        this.deviceDataService = deviceDataService;
    }

    @Override
    public void doExecute (ComServerDAO comServerDAO) {
        LoadProfileIdentifier loadProfileFinder = this.collectedLoadProfile.getLoadProfileIdentifier();
        LoadProfile loadProfile = (LoadProfile) loadProfileFinder.findLoadProfile();
        LocalLoadProfile localLoadProfile = filterFutureDatesAndCalculateLastReading(loadProfile);
        storeReadingsAndUpdateLastReading(comServerDAO, loadProfile, localLoadProfile);
    }

    private void storeReadingsAndUpdateLastReading(ComServerDAO comServerDAO, LoadProfile loadProfile, LocalLoadProfile localLoadProfile) {
        MeterReadingImpl meterReading = new MeterReadingImpl();
        meterReading.addAllIntervalBlocks(localLoadProfile.intervalBlocks);
        comServerDAO.storeMeterReadings(new DeviceIdentifierById(loadProfile.getDevice().getId(), this.deviceDataService), meterReading);
        LoadProfile.LoadProfileUpdater loadProfileUpdater = ((Device) loadProfile.getDevice()).getLoadProfileUpdaterFor(loadProfile);
        loadProfileUpdater.setLastReadingIfLater(localLoadProfile.lastReading);
        loadProfileUpdater.update();
    }

    private LocalLoadProfile filterFutureDatesAndCalculateLastReading(LoadProfile loadProfile) {
        List<IntervalBlock> filteredBlocks = new ArrayList<>();
        Date lastReading = null;
        Date currentDate = getClock().now();
        for (IntervalBlock intervalBlock : MeterDataFactory.createIntervalBlocksFor(collectedLoadProfile, loadProfile.getInterval())) {
            IntervalBlockImpl filteredBlock = new IntervalBlockImpl(intervalBlock.getReadingTypeCode());
            for (IntervalReading intervalReading : intervalBlock.getIntervals()) {
                if (!intervalReading.getTimeStamp().after(currentDate)) {
                    filteredBlock.addIntervalReading(intervalReading);
                    if (lastReading == null || intervalReading.getTimeStamp().after(lastReading)) {
                        lastReading = intervalReading.getTimeStamp();
                    }
                }
            }
            filteredBlocks.add(filteredBlock);
        }
        return new LocalLoadProfile(filteredBlocks, lastReading);
    }

    @Override
    public ComServer.LogLevel getJournalingLogLevel() {
        return ComServer.LogLevel.INFO;
    }

    @Override
    protected void toJournalMessageDescription(DescriptionBuilder builder, ComServer.LogLevel serverLogLevel) {
        if (isJournalingLevelEnabled(serverLogLevel, ComServer.LogLevel.INFO)) {
            builder.addProperty("load profile").append(this.collectedLoadProfile.getLoadProfileIdentifier());
            builder.addProperty("interval data period").append(this.collectedLoadProfile.getCollectedIntervalDataRange());
            PropertyDescriptionBuilder listBuilder = builder.addListProperty("channels");
            for (ChannelInfo channel : this.collectedLoadProfile.getChannelInfo()) {
                listBuilder.append(channel.getChannelId()).next();
            }
        }
    }

    private class LocalLoadProfile {

        private final List<IntervalBlock> intervalBlocks;
        private final Date lastReading;

        private LocalLoadProfile(List<IntervalBlock> intervalBlocks, Date lastReading) {
            super();
            this.intervalBlocks = intervalBlocks;
            this.lastReading = lastReading;
        }
    }

}