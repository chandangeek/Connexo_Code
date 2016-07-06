package com.energyict.mdc.engine.impl.commands.store;

import com.elster.jupiter.metering.readings.IntervalBlock;
import com.elster.jupiter.metering.readings.IntervalReading;
import com.elster.jupiter.metering.readings.beans.IntervalBlockImpl;
import com.elster.jupiter.metering.readings.beans.IntervalReadingImpl;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.collections.DualIterable;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfile;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.offline.OfflineLoadProfile;
import com.energyict.mdc.protocol.api.device.offline.OfflineLoadProfileChannel;

import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Performs several actions on the given LoadProfile data which are required before storing.
 * <p>
 * Copyrights EnergyICT
 * Date: 7/30/14
 * Time: 9:34 AM
 */
public class PreStoreLoadProfile {

    private final Clock clock;
    private final MdcReadingTypeUtilService mdcReadingTypeUtilService;
    private final ComServerDAO comServerDAO;

    public PreStoreLoadProfile(Clock clock, MdcReadingTypeUtilService mdcReadingTypeUtilService, ComServerDAO comServerDAO) {
        this.clock = clock;
        this.mdcReadingTypeUtilService = mdcReadingTypeUtilService;
        this.comServerDAO = comServerDAO;
    }

    /**
     * Tasks:
     * <ul>
     * <li>Filter future dates</li>
     * <li>Scale value according to unit</li>
     * <li>Apply multiplier</li>
     * <li>Calculate last reading</li>
     * <li>Remove the entry which corresponds with the lastReading (because we already have it)</li>
     * </ul>
     *
     * @param collectedLoadProfile the collected data from a LoadProfile to (pre)Store
     * @return the preStored LoadProfile
     */
    PreStoredLoadProfile preStore(CollectedLoadProfile collectedLoadProfile) {
        PreStoredLoadProfile preStoredLoadProfile = new PreStoredLoadProfile(collectedLoadProfile.getLoadProfileIdentifier().getDeviceIdentifier());
        if (!collectedLoadProfile.getCollectedIntervalData().isEmpty()) {
            Optional<OfflineLoadProfile> optionalLoadProfile = this.comServerDAO.findOfflineLoadProfile(collectedLoadProfile.getLoadProfileIdentifier());
            optionalLoadProfile.ifPresent(offlineLoadProfile -> {
                List<IntervalBlock> processedBlocks = new ArrayList<>();
                Instant lastReading = null;
                preStoredLoadProfile.setPreStoreResult(PreStoredLoadProfile.PreStoreResult.OK);
                Range<Instant> range = getRangeForNewIntervalStorage(offlineLoadProfile);
                for (Pair<IntervalBlock, ChannelInfo> intervalBlockChannelInfoPair : DualIterable.endWithLongest(MeterDataFactory.createIntervalBlocksFor(collectedLoadProfile), collectedLoadProfile.getChannelInfo())) {
                    IntervalBlock intervalBlock = intervalBlockChannelInfoPair.getFirst();
                    ChannelInfo channelInfo = intervalBlockChannelInfoPair.getLast();

                    Unit configuredUnit = this.mdcReadingTypeUtilService.getMdcUnitFor(channelInfo.getReadingTypeMRID());
                    int scaler = getScaler(channelInfo.getUnit(), configuredUnit);
                    BigDecimal multiplier = channelInfo.getMultiplier();
                    IntervalBlockImpl processingBlock = IntervalBlockImpl.of(intervalBlock.getReadingTypeCode());
                    for (IntervalReading intervalReading : intervalBlock.getIntervals()) {
                        if (range.contains(intervalReading.getTimeStamp())) {
                            IntervalReading scaledIntervalReading = getScaledIntervalReading(scaler, intervalReading);
                            IntervalReading multipliedReading = getMultipliedReading(multiplier, scaledIntervalReading);
                            processingBlock.addIntervalReading(multipliedReading);
                            lastReading = updateLastReadingIfLater(lastReading, intervalReading);
                        }
                    }
                    processedBlocks.add(processingBlock);
                }
                checkIfYouHaveAnEmptyChannel(preStoredLoadProfile, processedBlocks);
                preStoredLoadProfile.setLastReading(lastReading);
            });
        } else {
            preStoredLoadProfile.setPreStoreResult(PreStoredLoadProfile.PreStoreResult.NO_INTERVALS_COLLECTED);
        }
        return preStoredLoadProfile;
    }

    private void checkIfYouHaveAnEmptyChannel(PreStoredLoadProfile preStoredLoadProfile, List<IntervalBlock> processedBlocks) {
        final Optional<IntervalBlock> blockWithNoIntervals = processedBlocks.stream().filter(intervalBlock -> intervalBlock.getIntervals().isEmpty()).findAny();
        if (blockWithNoIntervals.isPresent()) {
            preStoredLoadProfile.setPreStoreResult(PreStoredLoadProfile.PreStoreResult.NO_INTERVALS_COLLECTED);
        } else {
            preStoredLoadProfile.setIntervalBlocks(processedBlocks);
        }
    }

    private Range<Instant> getRangeForNewIntervalStorage(OfflineLoadProfile offlineLoadProfile) {
        return Range.openClosed(offlineLoadProfile.getLastReading().orElse(Instant.EPOCH), clock.instant());
    }

    private IntervalReading getMultipliedReading(BigDecimal multiplier, IntervalReading intervalReading) {
        if (!Checks.is(multiplier).equalValue(BigDecimal.ONE)) {
            return IntervalReadingImpl.of(intervalReading.getTimeStamp(), intervalReading.getValue().multiply(multiplier), intervalReading.getReadingQualities());
        }
        else {
            return intervalReading;
        }
    }

    private IntervalReading getScaledIntervalReading(int scaler, IntervalReading intervalReading) {
        if (scaler == 0) {
            return intervalReading;
        } else {
            BigDecimal scaledValue = intervalReading.getValue().scaleByPowerOfTen(scaler);
            return IntervalReadingImpl.of(intervalReading.getTimeStamp(), scaledValue, intervalReading.getReadingQualities());
        }
    }

    private int getScaler(Unit fromUnit, Unit toUnit) {
        if (fromUnit.equalBaseUnit(toUnit)) {
            return fromUnit.getScale() - toUnit.getScale();
        } else {
            return 0;
        }
    }

    private Instant updateLastReadingIfLater(Instant lastReading, IntervalReading intervalReading) {
        if (lastReading == null || intervalReading.getTimeStamp().isAfter(lastReading)) {
            lastReading = intervalReading.getTimeStamp();
        }
        return lastReading;
    }

    /**
     * ValueObject representing a LoadProfile which was prepared before storing
     */
    static class PreStoredLoadProfile {

        enum PreStoreResult {
            OK,
            LOADPROFILE_NOT_FOUND,
            NO_INTERVALS_COLLECTED;
        }

        private final DeviceIdentifier<Device> deviceIdentifier;
        private List<IntervalBlock> intervalBlocks;
        private Instant lastReading;
        private PreStoreResult preStoreResult = PreStoreResult.LOADPROFILE_NOT_FOUND;

        PreStoredLoadProfile(DeviceIdentifier<Device> deviceIdentifier) {
            this.deviceIdentifier = deviceIdentifier;
        }

        public DeviceIdentifier<Device> getDeviceIdentifier() {
            return deviceIdentifier;
        }

        public List<IntervalBlock> getIntervalBlocks() {
            return intervalBlocks;
        }

        public void setIntervalBlocks(List<IntervalBlock> intervalBlocks) {
            this.intervalBlocks = intervalBlocks;
        }

        public Instant getLastReading() {
            return lastReading;
        }

        public void setLastReading(Instant lastReading) {
            this.lastReading = lastReading;
        }

        public PreStoreResult getPreStoreResult() {
            return preStoreResult;
        }

        public void setPreStoreResult(PreStoreResult preStoreResult) {
            this.preStoreResult = preStoreResult;
        }
    }

}