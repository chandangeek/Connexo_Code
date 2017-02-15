/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store;

import com.elster.jupiter.metering.readings.IntervalBlock;
import com.elster.jupiter.metering.readings.IntervalReading;
import com.elster.jupiter.metering.readings.beans.IntervalBlockImpl;
import com.elster.jupiter.metering.readings.beans.IntervalReadingImpl;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.collections.DualIterable;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.masterdata.LoadProfileIntervals;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfile;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.LoadProfileIdentifier;
import com.energyict.mdc.protocol.api.device.offline.OfflineLoadProfile;
import com.energyict.mdc.protocol.api.device.offline.OfflineLoadProfileChannel;

import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.streams.Predicates.not;

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
            return new CompositePreStoredLoadProfile(mdcReadingTypeUtilService, this.comServerDAO, collectedLoadProfile.getLoadProfileIdentifier()).preprocess(collectedLoadProfile, clock
                    .instant());
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
            NO_INTERVALS_COLLECTED,
            LOAD_PROFILE_CONFIGURATION_MISMATCH
        }

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

        protected void setPreStoreResult(PreStoreResult result) {
            this.preStoreResult = result;
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

        protected IntervalBlock processBlock(IntervalBlock intervalBlock, ChannelInfo channelInfo, Range<Instant> rangeToProcess, ZoneId zone) {
            return this.processBlock(intervalBlock, channelInfo.getReadingTypeMRID(), channelInfo.getUnit(), channelInfo.getMultiplier(), rangeToProcess, zone);
        }

        protected IntervalBlock processBlock(IntervalBlock intervalBlock, OfflineLoadProfileChannel offlineChannel, BigDecimal multiplier, Range<Instant> rangeToProcess, ZoneId zone) {
            return this.processBlock(intervalBlock, offlineChannel.getReadingType().getMRID(), offlineChannel.getUnit(), multiplier, rangeToProcess, zone);
        }

        private IntervalBlock processBlock(IntervalBlock intervalBlock, String readingTypeMRID, Unit unit, BigDecimal multiplier, Range<Instant> rangeToProcess, ZoneId zone) {
            Optional<IntervalReading> invalidInterval = findInValidInterval(intervalBlock, readingTypeMRID, zone);

            if (invalidInterval.isPresent()) {
                IntervalBlockImpl processingBlock = IntervalBlockImpl.of(readingTypeMRID);
                setPreStoreResult(PreStoreResult.LOAD_PROFILE_CONFIGURATION_MISMATCH);
                return processingBlock;
            }

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

        private Optional<IntervalReading> findInValidInterval(IntervalBlock intervalBlock, String readingTypeMRID, ZoneId zone) {
            TimeDuration timeDuration = mdcReadingTypeUtilService.getReadingTypeInformationFor(readingTypeMRID).getTimeDuration();
            final boolean validInterval = Stream.of(LoadProfileIntervals.values())
                    .filter(interval -> interval.getTimeDuration().getSeconds() == timeDuration.getSeconds())
                    .findAny()
                    .isPresent();

            return intervalBlock.getIntervals().stream().filter(not(intervalReading1 -> {
                if(!validInterval) {
                    return false;
                }
                ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(intervalReading1.getTimeStamp(), zone);

                if (timeDuration.getTemporalUnit().equals(ChronoUnit.SECONDS) && timeDuration.getCount() <= 3600) {
                    return zonedDateTime.getMinute() % getMinuteIntervalLength(timeDuration.getTemporalUnit(), timeDuration) == 0
                            && zonedDateTime.getSecond() == 0 && zonedDateTime.getNano() == 0;
                }
                if (!validTimeOfDay(zonedDateTime)) {
                    return false;
                }
                return !timeDuration.getTemporalUnit().equals(ChronoUnit.MONTHS) || zonedDateTime.getDayOfMonth() == 1;
            })).findAny();
        }

        private boolean validTimeOfDay(ZonedDateTime dateTime) {
            return dateTime.getMinute() == 0 && dateTime.getSecond() == 0 && dateTime.getNano() == 0 && dateTime.getHour() == 0;
        }

        private int getMinuteIntervalLength(TemporalUnit unit, TimeDuration interval) {
            if (unit.equals(ChronoUnit.SECONDS)) {
                long seconds = interval.getSeconds();
                if ((seconds % 60) != 0) {
                    throw new IllegalArgumentException("Duration not in whole minutes");
                }
                long minutes = seconds / 60;
                return (int) minutes;
            }
            return -1;
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
                    ZoneId zone = ((Device) collectedLoadProfile.getLoadProfileIdentifier().getDeviceIdentifier().findDevice()).getZone();
                    if (pair.getFirst().isDataLoggerSlaveLoadProfile()) {
                        OfflineLoadProfileChannel offlineLoadProfileChannel = pair.getFirst().getAllChannels().get(0);
                        processed = processBlock(intervalBlock, offlineLoadProfileChannel, channelInfo.getMultiplier(), pair.getLast(), zone);
                    }
                    if (processed == null) {
                        processed = processBlock(intervalBlock, channelInfo, pair.getLast(), zone);
                    }
                    if (!processed.getIntervals().isEmpty()) {
                        PreStoredLoadProfile preStoredLoadProfile = findOrCreatePreStoredLoadProfile(pair.getFirst());
                        if (preStoredLoadProfile.addIntervalBlock(processed)) {
                            if(!this.getPreStoreResult().equals(PreStoreResult.LOAD_PROFILE_CONFIGURATION_MISMATCH)) {
                                setPreStoreResult(PreStoredLoadProfile.PreStoreResult.OK);
                            }
                        }
                    }
                });
            }
            if(!this.getPreStoreResult().equals(PreStoreResult.LOAD_PROFILE_CONFIGURATION_MISMATCH)) {
                setPreStoreResult(PreStoredLoadProfile.PreStoreResult.OK);
            }
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