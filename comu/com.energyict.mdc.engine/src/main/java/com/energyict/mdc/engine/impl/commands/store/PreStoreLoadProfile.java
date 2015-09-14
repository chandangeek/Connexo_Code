package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.common.Unit;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfile;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.offline.OfflineLoadProfile;
import com.energyict.mdc.protocol.api.device.offline.OfflineLoadProfileChannel;

import com.elster.jupiter.metering.readings.IntervalBlock;
import com.elster.jupiter.metering.readings.IntervalReading;
import com.elster.jupiter.metering.readings.beans.IntervalBlockImpl;
import com.elster.jupiter.metering.readings.beans.IntervalReadingImpl;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.collections.DualIterable;
import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Performs several actions on the given LoadProfile data which are required before storing.
 *
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
     *     <li>Filter future dates</li>
     *     <li>Scale value according to unit</li>
     *     <li>OverFlow calculation</li>
     *     <li>Calculate last reading</li>
     *     <li>Remove the entry which corresponds with the lastReading (because we already have it)</li>
     * </ul>
     *
     * @param collectedLoadProfile the collected data from a LoadProfile to (pre)Store
     * @return the preStored LoadProfile
     */
    Optional<Pair<DeviceIdentifier<Device>, LocalLoadProfile>> preStore(CollectedLoadProfile collectedLoadProfile) {
        Optional<OfflineLoadProfile> offlineLoadProfile = this.comServerDAO.findOfflineLoadProfile(collectedLoadProfile.getLoadProfileIdentifier());
        if (offlineLoadProfile.isPresent()) {
            List<IntervalBlock> processedBlocks = new ArrayList<>();
            Instant lastReading = null;
            Range<Instant> range = getRangeForNewIntervalStorage(offlineLoadProfile.get());
            for (Pair<IntervalBlock, ChannelInfo> intervalBlockChannelInfoPair : DualIterable.endWithLongest(MeterDataFactory.createIntervalBlocksFor(collectedLoadProfile), collectedLoadProfile.getChannelInfo())) {
                IntervalBlock intervalBlock = intervalBlockChannelInfoPair.getFirst();
                ChannelInfo channelInfo = intervalBlockChannelInfoPair.getLast();

                Unit configuredUnit = this.mdcReadingTypeUtilService.getMdcUnitFor(channelInfo.getReadingTypeMRID());
                int scaler = getScaler(channelInfo.getUnit(), configuredUnit);
                BigDecimal channelOverFlowValue = getChannelOverFlowValue(channelInfo, offlineLoadProfile.get());
                IntervalBlockImpl processingBlock = IntervalBlockImpl.of(intervalBlock.getReadingTypeCode());
                for (IntervalReading intervalReading : intervalBlock.getIntervals()) {
                    if (range.contains(intervalReading.getTimeStamp())) {
                        IntervalReading scaledIntervalReading = getScaledIntervalReading(scaler, intervalReading);
                        IntervalReading overflowCheckedReading = getOverflowCheckedReading(channelOverFlowValue, scaledIntervalReading);
                        processingBlock.addIntervalReading(overflowCheckedReading);
                        lastReading = updateLastReadingIfLater(lastReading, intervalReading);
                    }
                }
                processedBlocks.add(processingBlock);
            }
            return Optional.of(Pair.of(collectedLoadProfile.getLoadProfileIdentifier().getDeviceIdentifier(), new LocalLoadProfile(processedBlocks, lastReading)));
        }
        else {
            return Optional.empty();
        }
    }

    private Range<Instant> getRangeForNewIntervalStorage(OfflineLoadProfile offlineLoadProfile) {
        return Range.openClosed(offlineLoadProfile.getLastReading().orElse(Instant.EPOCH), clock.instant());
    }

    private IntervalReading getOverflowCheckedReading(BigDecimal channelOverFlowValue, IntervalReading scaledIntervalReading) {
        if(scaledIntervalReading.getValue().compareTo(channelOverFlowValue) > 0){
            return IntervalReadingImpl.of(scaledIntervalReading.getTimeStamp(), scaledIntervalReading.getValue().subtract(channelOverFlowValue), scaledIntervalReading.getProfileStatus());
        }
        return scaledIntervalReading;
    }

    private BigDecimal getChannelOverFlowValue(ChannelInfo channelInfo, OfflineLoadProfile offlineLoadProfile) {
        for (OfflineLoadProfileChannel offlineLoadProfileChannel : offlineLoadProfile.getChannels()) {
            /**
             * Check the ObisCode, NOT the ReadingTye
             */
            if(offlineLoadProfileChannel.getObisCode().equals(channelInfo.getChannelObisCode())){
                return offlineLoadProfileChannel.getOverflow();
            }
        }
        return new BigDecimal(Double.MAX_VALUE);
    }

    private IntervalReading getScaledIntervalReading(int scaler, IntervalReading intervalReading) {
        if(scaler == 0){
            return intervalReading;
        } else {
            BigDecimal scaledValue = intervalReading.getValue().scaleByPowerOfTen(scaler);
            return IntervalReadingImpl.of(intervalReading.getTimeStamp(), scaledValue, intervalReading.getProfileStatus());
        }
    }

    private int getScaler(Unit fromUnit, Unit toUnit){
        if(fromUnit.equalBaseUnit(toUnit)){
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

    class LocalLoadProfile {

        private final List<IntervalBlock> intervalBlocks;
        private final Instant lastReading;

        private LocalLoadProfile(List<IntervalBlock> intervalBlocks, Instant lastReading) {
            super();
            this.intervalBlocks = intervalBlocks;
            this.lastReading = lastReading;
        }

        public List<IntervalBlock> getIntervalBlocks() {
            return intervalBlocks;
        }

        public Instant getLastReading() {
            return lastReading;
        }
    }

}