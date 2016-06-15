package com.energyict.mdc.engine.impl.commands.store;

import com.elster.jupiter.metering.readings.IntervalBlock;
import com.elster.jupiter.metering.readings.IntervalReading;
import com.elster.jupiter.metering.readings.beans.IntervalBlockImpl;
import com.elster.jupiter.metering.readings.beans.IntervalReadingImpl;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.collections.DualIterable;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfile;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.LoadProfileIdentifier;
import com.energyict.mdc.protocol.api.device.offline.OfflineLoadProfile;

import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

        enum PreStoreResult {
            NOT_PROCESSED,
            OK,
            LOADPROFILE_NOT_FOUND,
            NO_INTERVALS_COLLECTED;
        }

        private final MdcReadingTypeUtilService mdcReadingTypeUtilService;
        private OfflineLoadProfile offlineLoadProfile;
        private List<IntervalBlock> intervalBlocks;
        private PreStoreResult preStoreResult = PreStoreResult.NOT_PROCESSED;
        private Instant lastReading;

        private PreStoredLoadProfile(MdcReadingTypeUtilService mdcReadingTypeUtilService) {
            this.mdcReadingTypeUtilService = mdcReadingTypeUtilService;
        }

        PreStoredLoadProfile (MdcReadingTypeUtilService mdcReadingTypeUtilService, Optional<OfflineLoadProfile> offlineLoadProfile) {
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

        protected void setPreStoreResult(PreStoreResult result){
            this.preStoreResult = result;
        }

        public LoadProfileIdentifier getLoadProfileIdentifier() {
            return this.offlineLoadProfile.getLoadProfileIdentifier();
        }

        public DeviceIdentifier getDeviceIdentifier() {
            return this.offlineLoadProfile.getDeviceIdentifier();
        }

        List<IntervalBlock> getIntervalBlocks() {
            return intervalBlocks;
        }

        protected boolean addIntervalBlock(IntervalBlock intervalBlock){
            if (this.intervalBlocks == null){
                this.intervalBlocks = new ArrayList<>();
            }
            return this.intervalBlocks.add(intervalBlock);
        }

        public Instant getLastReading() {
            return lastReading;
        }

        public PreStoreResult getPreStoreResult() {
            return preStoreResult;
        }


        public PreStoredLoadProfile preprocess(CollectedLoadProfile collectedLoadProfile, Instant intervalStorageEnd) {
            if (this.preStoreResult == preStoreResult.NOT_PROCESSED) {
                List<IntervalBlock> processedBlocks = new ArrayList<>();
                Instant lastReading = null;
                preStoreResult = PreStoredLoadProfile.PreStoreResult.OK;

                Range<Instant> range = getRangeForNewIntervalStorage(intervalStorageEnd);
                for (Pair<IntervalBlock, ChannelInfo> intervalBlockChannelInfoPair : DualIterable.endWithLongest(MeterDataFactory.createIntervalBlocksFor(collectedLoadProfile), collectedLoadProfile.getChannelInfo())) {
                    IntervalBlock intervalBlock = intervalBlockChannelInfoPair.getFirst();
                    ChannelInfo channelInfo = intervalBlockChannelInfoPair.getLast();
                    processedBlocks.add(processBlock(intervalBlock, channelInfo, range));
                }
                checkIfYouHaveAnEmptyChannel(processedBlocks);
                this.lastReading = lastReading;
            }
            return this;
        }

        protected IntervalBlock processBlock(IntervalBlock intervalBlock, ChannelInfo channelInfo,  Range<Instant> rangeToProcess){
            Unit configuredUnit = mdcReadingTypeUtilService.getMdcUnitFor(channelInfo.getReadingTypeMRID());
            int scaler = getScaler(channelInfo.getUnit(), configuredUnit);
            BigDecimal multiplier = channelInfo.getMultiplier();
            IntervalBlockImpl processingBlock = IntervalBlockImpl.of(channelInfo.getReadingTypeMRID());
            for (IntervalReading intervalReading : intervalBlock.getIntervals()) {
                if (rangeToProcess.contains(intervalReading.getTimeStamp())) {
                    IntervalReading scaledIntervalReading = getScaledIntervalReading(scaler, intervalReading);
                    IntervalReading multipliedReading = getMultipliedReading(multiplier, scaledIntervalReading);
                    processingBlock.addIntervalReading(multipliedReading);
                    lastReading = updateLastReadingIfLater(lastReading, intervalReading);
                }
            }
            return processingBlock;
        }

        public void updateCommand(MeterDataStoreCommand meterDataStoreCommand) {
            if (!intervalBlocks.isEmpty()) {
                meterDataStoreCommand.addIntervalReadings(getDeviceIdentifier(), intervalBlocks);
                meterDataStoreCommand.addLastReadingUpdater(getLoadProfileIdentifier(), lastReading);
            }
        }

        final protected Range<Instant> getRangeForNewIntervalStorage(Instant intervalStorageEnd) {
            return Range.openClosed(offlineLoadProfile.getLastReading().orElse(Instant.EPOCH), intervalStorageEnd);
        }

        final protected IntervalReading getMultipliedReading(BigDecimal multiplier, IntervalReading intervalReading) {
            if (!Checks.is(multiplier).equalValue(BigDecimal.ONE)) {
                return IntervalReadingImpl.of(intervalReading.getTimeStamp(), intervalReading.getValue().multiply(multiplier), intervalReading.getProfileStatus());
            } else {
                return intervalReading;
            }
        }

        final protected IntervalReading getScaledIntervalReading(int scaler, IntervalReading intervalReading) {
            if (scaler == 0) {
                return intervalReading;
            } else {
                BigDecimal scaledValue = intervalReading.getValue().scaleByPowerOfTen(scaler);
                return IntervalReadingImpl.of(intervalReading.getTimeStamp(), scaledValue, intervalReading.getProfileStatus());
            }
        }

        final protected int getScaler(Unit fromUnit, Unit toUnit) {
            if (fromUnit.equalBaseUnit(toUnit)) {
                return fromUnit.getScale() - toUnit.getScale();
            } else {
                return 0;
            }
        }

        final protected Instant updateLastReadingIfLater(Instant lastReading, IntervalReading intervalReading) {
            if (lastReading == null || intervalReading.getTimeStamp().isAfter(lastReading)) {
                lastReading = intervalReading.getTimeStamp();
            }
            return lastReading;
        }

        final protected void checkIfYouHaveAnEmptyChannel(List<IntervalBlock> processedBlocks) {
            final Optional<IntervalBlock> blockWithNoIntervals = processedBlocks.stream().filter(intervalBlock -> intervalBlock.getIntervals().isEmpty()).findAny();
            if (blockWithNoIntervals.isPresent()) {
                this.preStoreResult = PreStoreResult.NO_INTERVALS_COLLECTED;
            } else {
                this.intervalBlocks = processedBlocks;
            }
        }
    }

    static class CompositePreStoredLoadProfile extends PreStoredLoadProfile {

        private final ComServerDAO comServerDAO;
        private List<PreStoredLoadProfile> preStoredLoadProfiles = new ArrayList<>();

        CompositePreStoredLoadProfile(MdcReadingTypeUtilService mdcReadingTypeUtilService, ComServerDAO comServerDAO , LoadProfileIdentifier loadProfileIdentifier) {
            super(mdcReadingTypeUtilService, comServerDAO.findOfflineLoadProfile(loadProfileIdentifier));
            this.comServerDAO = comServerDAO;
        }

        public PreStoredLoadProfile preprocess(CollectedLoadProfile collectedLoadProfile, Instant intervalStorageEnd) {
            for (Pair<IntervalBlock, ChannelInfo> intervalBlockChannelInfoPair : DualIterable.endWithLongest(MeterDataFactory.createIntervalBlocksFor(collectedLoadProfile), collectedLoadProfile.getChannelInfo())) {
                IntervalBlock intervalBlock = intervalBlockChannelInfoPair.getFirst();
                ChannelInfo channelInfo = intervalBlockChannelInfoPair.getLast();
                comServerDAO.getStorageLoadProfileIdentifiers(getOfflineLoadProfile(), channelInfo.getReadingTypeMRID(), getRangeForNewIntervalStorage(intervalStorageEnd)).forEach(pair -> {
                    IntervalBlock processed = processBlock(intervalBlock, channelInfo, pair.getLast());
                    if (!processed.getIntervals().isEmpty()) {
                        PreStoredLoadProfile preStoredLoadProfile = findOrCreatePreStoredLoadProfile(pair.getFirst());
                        if (preStoredLoadProfile.addIntervalBlock(processed)){
                            preStoredLoadProfile.setPreStoreResult(PreStoreResult.OK);
                        };
                    }
                });
            }
            setPreStoreResult(PreStoredLoadProfile.PreStoreResult.OK);
            return this;
        }

        private PreStoredLoadProfile findOrCreatePreStoredLoadProfile(OfflineLoadProfile offlineLoadProfile){
            Optional<PreStoredLoadProfile> existing = this.preStoredLoadProfiles
                       .stream()
                       .filter((preStored) -> (preStored.getOfflineLoadProfile().getLoadProfileId() == offlineLoadProfile.getLoadProfileId()))
                       .findFirst();
            if (existing.isPresent()){
                return existing.get();
            }
            return addPrestoredLoadProfile(offlineLoadProfile);
        }

        private PreStoredLoadProfile addPrestoredLoadProfile(OfflineLoadProfile offlineLoadProfile){
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

        List<PreStoredLoadProfile> getPreStoredLoadProfiles(){
            return preStoredLoadProfiles;
        }

    }

}