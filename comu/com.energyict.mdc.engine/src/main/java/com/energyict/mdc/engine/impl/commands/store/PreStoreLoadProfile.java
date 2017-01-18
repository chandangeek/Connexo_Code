package com.energyict.mdc.engine.impl.commands.store;

import com.elster.jupiter.metering.readings.IntervalBlock;
import com.elster.jupiter.metering.readings.IntervalReading;
import com.elster.jupiter.metering.readings.beans.IntervalBlockImpl;
import com.elster.jupiter.metering.readings.beans.IntervalReadingImpl;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.collections.DualIterable;
import com.energyict.cbo.Unit;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.LoadProfileIdentifier;
import com.energyict.mdc.upl.offline.OfflineLoadProfile;
import com.energyict.mdc.upl.offline.OfflineLoadProfileChannel;
import com.energyict.protocol.ChannelInfo;
import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

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
     * @return and Array of preStored LoadProfiles: for non datalogger devices this will only contain 1 element. A data logger for which
     * at least one channel is linked to a slave channel can return an extra element, if not all channels are linked: 1 for non linked channels (device = datalogger)
     * and 1 for each slave device ;
     */
    PreStoredLoadProfile preStore(CollectedLoadProfile collectedLoadProfile) {
        if (!collectedLoadProfile.getCollectedIntervalData().isEmpty()) {
            return new CompositePreStoredLoadProfile(mdcReadingTypeUtilService, this.comServerDAO, collectedLoadProfile.getLoadProfileIdentifier()).preprocess(collectedLoadProfile, clock.instant());
        } else {
            return PreStoredLoadProfile.forLoadProfileDataNotCollected();
        }
    }

    /**
     * ValueObject representing a LoadProfile which was prepared before storing
     */
    static class PreStoredLoadProfile {

        private final MdcReadingTypeUtilService mdcReadingTypeUtilService;
        private OfflineLoadProfile offlineLoadProfile;
        private List<IntervalBlock> intervalBlocks;
        private PreStoreResult preStoreResult = PreStoreResult.NOT_PROCESSED;
        private Instant lastReading;
        private PreStoredLoadProfile(MdcReadingTypeUtilService mdcReadingTypeUtilService) {
            this.mdcReadingTypeUtilService = mdcReadingTypeUtilService;
        }

        PreStoredLoadProfile(MdcReadingTypeUtilService mdcReadingTypeUtilService, Optional<OfflineLoadProfile> offlineLoadProfile) {
            this(mdcReadingTypeUtilService);
            if (offlineLoadProfile.isPresent()) {
                this.offlineLoadProfile = offlineLoadProfile.get();
            } else {
                this.preStoreResult = PreStoreResult.LOADPROFILE_NOT_FOUND;
            }
        }

        static PreStoredLoadProfile forLoadProfileDataNotCollected() {
            PreStoredLoadProfile profile = new PreStoredLoadProfile(null);
            profile.preStoreResult = PreStoreResult.NO_INTERVALS_COLLECTED;
            return profile;
        }

        protected MdcReadingTypeUtilService getMdcReadingTypeUtilService() {
            return mdcReadingTypeUtilService;
        }

        protected OfflineLoadProfile getOfflineLoadProfile() {
            return this.offlineLoadProfile;
        }

        public LoadProfileIdentifier getLoadProfileIdentifier() {
            return this.offlineLoadProfile.getLoadProfileIdentifier();
        }

        public DeviceIdentifier getDeviceIdentifier() {
            return this.offlineLoadProfile.getDeviceIdentifier();
        }

        List<IntervalBlock> getIntervalBlocks() {
            return Collections.unmodifiableList(intervalBlocks);
        }

        protected boolean addIntervalBlock(IntervalBlock intervalBlock) {
            if (this.intervalBlocks == null) {
                this.intervalBlocks = new ArrayList<>();
            }
            intervalBlock.getIntervals()
                    .stream()
                    .map(IntervalReading::getTimeStamp)
                    .reduce(BinaryOperator.maxBy(Comparator.comparingLong(Instant::toEpochMilli)))
                    .ifPresent((maxTimeStamp) -> this.lastReading = maxTimeStamp);
            return this.intervalBlocks.add(intervalBlock);
        }

        public Instant getLastReading() {
            return lastReading;
        }

        public PreStoreResult getPreStoreResult() {
            return preStoreResult;
        }

        protected void setPreStoreResult(PreStoreResult result) {
            this.preStoreResult = result;
        }

        protected IntervalBlock processBlock(IntervalBlock intervalBlock, ChannelInfo channelInfo, Range<Instant> rangeToProcess) {
            return this.processBlock(intervalBlock, channelInfo.getReadingTypeMRID(), channelInfo.getUnit(), channelInfo.getMultiplier(), rangeToProcess);
        }

        protected IntervalBlock processBlock(IntervalBlock intervalBlock, OfflineLoadProfileChannel offlineChannel, BigDecimal multiplier, Range<Instant> rangeToProcess) {
            return this.processBlock(intervalBlock, offlineChannel.getReadingTypeMRID(), offlineChannel.getUnit(), multiplier, rangeToProcess);
        }

        private IntervalBlock processBlock(IntervalBlock intervalBlock, String readingTypeMRID, Unit unit, BigDecimal multiplier, Range<Instant> rangeToProcess) {
            Unit configuredUnit = mdcReadingTypeUtilService.getMdcUnitFor(readingTypeMRID);
            int scaler = getScaler(unit, configuredUnit);
            IntervalBlockImpl processingBlock = IntervalBlockImpl.of(readingTypeMRID);
            intervalBlock.getIntervals().stream().filter(intervalReading -> rangeToProcess.contains(intervalReading.getTimeStamp())).forEach(intervalReading -> {
                IntervalReading scaledIntervalReading = getScaledIntervalReading(scaler, intervalReading);
                IntervalReading multipliedReading = getMultipliedReading(multiplier, scaledIntervalReading);
                processingBlock.addIntervalReading(multipliedReading);
            });
            return processingBlock;
        }

        public void updateCommand(MeterDataStoreCommand meterDataStoreCommand) {
            if (!intervalBlocks.isEmpty()) {
                meterDataStoreCommand.addIntervalReadings(getDeviceIdentifier(), intervalBlocks);
                meterDataStoreCommand.addLastReadingUpdater(getLoadProfileIdentifier(), lastReading);
            }
        }

        final protected Range<Instant> getRangeForNewIntervalStorage(Instant intervalStorageEnd) {
            return Range.openClosed(offlineLoadProfile.getLastReading() == null ? Instant.EPOCH : offlineLoadProfile.getLastReading().toInstant(), intervalStorageEnd);
        }

        final protected IntervalReading getMultipliedReading(BigDecimal multiplier, IntervalReading intervalReading) {
            if (!Checks.is(multiplier).equalValue(BigDecimal.ONE)) {
                return IntervalReadingImpl.of(intervalReading.getTimeStamp(), intervalReading.getValue().multiply(multiplier), intervalReading.getReadingQualities());
            } else {
                return intervalReading;
            }
        }

        final protected IntervalReading getScaledIntervalReading(int scaler, IntervalReading intervalReading) {
            if (scaler == 0) {
                return intervalReading;
            } else {
                BigDecimal scaledValue = intervalReading.getValue().scaleByPowerOfTen(scaler);
                return IntervalReadingImpl.of(intervalReading.getTimeStamp(), scaledValue, intervalReading.getReadingQualities());
            }
        }

        final protected int getScaler(Unit fromUnit, Unit toUnit) {
            if (fromUnit.equalBaseUnit(toUnit)) {
                return fromUnit.getScale() - toUnit.getScale();
            } else {
                return 0;
            }
        }

        enum PreStoreResult {
            NOT_PROCESSED,
            OK,
            LOADPROFILE_NOT_FOUND,
            NO_INTERVALS_COLLECTED
        }

    }

    static class CompositePreStoredLoadProfile extends PreStoredLoadProfile {

        private final ComServerDAO comServerDAO;
        private List<PreStoredLoadProfile> preStoredLoadProfiles = new ArrayList<>();

        CompositePreStoredLoadProfile(MdcReadingTypeUtilService mdcReadingTypeUtilService, ComServerDAO comServerDAO, LoadProfileIdentifier loadProfileIdentifier) {
            super(mdcReadingTypeUtilService, comServerDAO.findOfflineLoadProfile(loadProfileIdentifier));
            this.comServerDAO = comServerDAO;
        }

        public PreStoredLoadProfile preprocess(CollectedLoadProfile collectedLoadProfile, Instant intervalStorageEnd) {
            for (Pair<IntervalBlock, ChannelInfo> intervalBlockChannelInfoPair : DualIterable.endWithLongest(MeterDataFactory.createIntervalBlocksFor(collectedLoadProfile), collectedLoadProfile.getChannelInfo())) {
                IntervalBlock intervalBlock = intervalBlockChannelInfoPair.getFirst();
                ChannelInfo channelInfo = intervalBlockChannelInfoPair.getLast();
                comServerDAO.getStorageLoadProfileIdentifiers(getOfflineLoadProfile(), channelInfo.getReadingTypeMRID(), getRangeForNewIntervalStorage(intervalStorageEnd)).forEach(pair -> {
                    IntervalBlock processed = null;
                    if (pair.getFirst().isDataLoggerSlaveLoadProfile()) {
                        OfflineLoadProfileChannel offlineLoadProfileChannel = pair.getFirst().getAllOfflineChannels().get(0);
                        processed = processBlock(intervalBlock, offlineLoadProfileChannel, channelInfo.getMultiplier(), pair.getLast());
                    }
                    if (processed == null) {
                        processed = processBlock(intervalBlock, channelInfo, pair.getLast());
                    }
                    if (!processed.getIntervals().isEmpty()) {
                        PreStoredLoadProfile preStoredLoadProfile = findOrCreatePreStoredLoadProfile(pair.getFirst());
                        if (preStoredLoadProfile.addIntervalBlock(processed)) {
                            preStoredLoadProfile.setPreStoreResult(PreStoreResult.OK);
                        }
                    }
                });
            }
            setPreStoreResult(PreStoredLoadProfile.PreStoreResult.OK);
            return this;
        }

        private PreStoredLoadProfile findOrCreatePreStoredLoadProfile(OfflineLoadProfile offlineLoadProfile) {
            Optional<PreStoredLoadProfile> existing = this.preStoredLoadProfiles
                    .stream()
                    .filter((preStored) -> (preStored.getOfflineLoadProfile().getLoadProfileId() == offlineLoadProfile.getLoadProfileId()))
                    .findFirst();
            if (existing.isPresent()) {
                return existing.get();
            }
            return addPrestoredLoadProfile(offlineLoadProfile);
        }

        private PreStoredLoadProfile addPrestoredLoadProfile(OfflineLoadProfile offlineLoadProfile) {
            int last = preStoredLoadProfiles.size();
            preStoredLoadProfiles.add(new PreStoredLoadProfile(this.getMdcReadingTypeUtilService(), Optional.of(offlineLoadProfile)));
            return preStoredLoadProfiles.get(last);
        }

        List<IntervalBlock> getIntervalBlocks() {
            return this.preStoredLoadProfiles.stream().flatMap(each -> each.getIntervalBlocks().stream()).collect(Collectors.toList());
        }

        public void updateCommand(MeterDataStoreCommand meterDataStoreCommand) {
            preStoredLoadProfiles.stream().forEach(each -> each.updateCommand(meterDataStoreCommand));
        }

        List<PreStoredLoadProfile> getPreStoredLoadProfiles() {
            return Collections.unmodifiableList(preStoredLoadProfiles);
        }

    }

}