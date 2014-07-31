package com.energyict.mdc.engine.impl.commands.store;

import com.elster.jupiter.metering.readings.IntervalBlock;
import com.elster.jupiter.metering.readings.IntervalReading;
import com.elster.jupiter.metering.readings.beans.IntervalBlockImpl;
import com.elster.jupiter.metering.readings.beans.IntervalReadingImpl;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.collections.DualIterable;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.common.comserver.logging.PropertyDescriptionBuilder;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.protocol.inbound.DeviceIdentifierById;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfile;
import com.energyict.mdc.protocol.api.device.data.identifiers.LoadProfileIdentifier;

import java.math.BigDecimal;
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

    private final CollectedLoadProfile collectedLoadProfile;

    public CollectedLoadProfileDeviceCommand(CollectedLoadProfile collectedLoadProfile) {
        super();
        this.collectedLoadProfile = collectedLoadProfile;
    }

    @Override
    public void doExecute (ComServerDAO comServerDAO) {
        LoadProfileIdentifier loadProfileFinder = this.collectedLoadProfile.getLoadProfileIdentifier();
        LoadProfile loadProfile = (LoadProfile) loadProfileFinder.findLoadProfile();
        LoadProfilePreStorer loadProfilePreStorer = new LoadProfilePreStorer(getClock(), getMdcReadingTypeUtilService());
        LoadProfilePreStorer.LocalLoadProfile localLoadProfile = loadProfilePreStorer.preStore(collectedLoadProfile);
        storeReadingsAndUpdateLastReading(comServerDAO, loadProfile, localLoadProfile);
    }

    private void storeReadingsAndUpdateLastReading(ComServerDAO comServerDAO, final LoadProfile loadProfile, final LoadProfilePreStorer.LocalLoadProfile localLoadProfile) {
        MeterReadingImpl meterReading = new MeterReadingImpl();
        meterReading.addAllIntervalBlocks(localLoadProfile.getIntervalBlocks());
        comServerDAO.storeMeterReadings(new DeviceIdentifierById(loadProfile.getDevice().getId(), getDeviceDataService()), meterReading);
        // Todo: use method on the comServerDAO
        LoadProfile.LoadProfileUpdater loadProfileUpdater = loadProfile.getDevice().getLoadProfileUpdaterFor(loadProfile);
        loadProfileUpdater.setLastReadingIfLater(localLoadProfile.getLastReading());
        loadProfileUpdater.update();
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

}