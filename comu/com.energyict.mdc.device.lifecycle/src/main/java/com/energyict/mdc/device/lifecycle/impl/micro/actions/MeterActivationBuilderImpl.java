package com.energyict.mdc.device.lifecycle.impl.micro.actions;

import com.elster.jupiter.cbo.QualityCodeCategory;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.util.UpdatableHolder;
import com.elster.jupiter.util.streams.Functions;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleActionViolationException;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;

import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.streams.Predicates.not;

public class MeterActivationBuilderImpl implements MeterActivationBuilder {

    private final Device device;
    private UpdatableHolder<MeterActivation> previousMeterActivation;

    public MeterActivationBuilderImpl(Device device) {
        this.device = device;
    }

    private static class Combo {
        private UsagePoint usagePoint;
        private Instant start;
        private boolean copyUsagePoint;

        public Instant getStart() {
            return start;
        }

        public void setStart(Instant start) {
            this.start = start;
        }

        public UsagePoint getUsagePoint() {
            return usagePoint;
        }

        public void setUsagePoint(UsagePoint usagePoint) {
            this.usagePoint = usagePoint;
        }

        public boolean isCopyUsagePoint() {
            return copyUsagePoint;
        }

        public void setCopyUsagePoint(boolean copyUsagePoint) {
            this.copyUsagePoint = copyUsagePoint;
        }
    }

    private final RangeMap<Instant, Combo> combos = TreeRangeMap.create();
    private Combo currentCombo = new Combo();

    @Override
    public MeterActivationBuilder onUsagePoint(UsagePoint usagePoint) {
        currentCombo.setUsagePoint(usagePoint);
        return this;
    }

    @Override
    public MeterActivationBuilder startingAt(Instant start) {
        if (start == null) {
            throw new IllegalArgumentException("null is not an acceptable value for the start of a MeterActivation");
        }
        if (!start.equals(currentCombo.getStart())) {
            currentCombo = Optional.ofNullable(combos.getEntry(start))
                    .map(Map.Entry::getValue)
                    .filter(combo -> combo.getStart().equals(start))
                    .orElseGet(Combo::new);
        }
        if (!start.equals(currentCombo.getStart())) {
            currentCombo.setStart(start);
            combos.put(Range.atLeast(start), currentCombo);
        }
        return this;
    }

    @Override
    public MeterActivationBuilder keepingUsagePoint() {
        currentCombo.setCopyUsagePoint(true);
        return this;
    }

    @Override
    public List<MeterActivation> build() {
        try {
            Map<Range<Instant>, Combo> rangeMap = combos.asMapOfRanges();
            previousMeterActivation = new UpdatableHolder<>(null);
            if (!rangeMap.isEmpty()) {
                rangeMap.keySet()
                        .stream()
                        .map(Range::lowerEndpoint)
                        .min(Comparator.naturalOrder())
                        .flatMap(instant -> device.getCurrentMeterActivation()
                                .filter(meterActivation -> meterActivation.getRange().contains(instant)))
                        .ifPresent(previousMeterActivation::update);
            }
            return rangeMap
                    .entrySet()
                    .stream()
                    .map(entry -> {
                        Range<Instant> range = entry.getKey();
                        Combo combo = entry.getValue();
                        return doBuild(range, combo);
                    })
                    .collect(Collectors.toList());
        } catch (LocalizedException e) {
            throw new DeviceLifeCycleActionViolationException() {
                @Override
                public String getLocalizedMessage() {
                    return e.getLocalizedMessage();
                }
            };
        }
    }

    private MeterActivation doBuild(Range<Instant> range, Combo combo) {
        MeterActivation meterActivation = doActivate(combo);
        if (range.hasUpperBound()) {
            meterActivation.endAt(range.upperEndpoint());
        }
        return meterActivation;
    }

    private MeterActivation doActivate(Combo combo) {
        Instant effectiveTimestamp = combo.getStart();
        return maxEffectiveTimestampAfterLastData(effectiveTimestamp, device)
                .map(dataTimestamp -> {
                    List<Channel> channels = device.getCurrentMeterActivation()
                            .map(MeterActivation::getChannelsContainer)
                            .map(ChannelsContainer::getChannels)
                            .orElse(Collections.emptyList());
                    MeterActivation newMeterActivation = createNewMeterActivation(combo, dataTimestamp);
                    List<Channel> newChannels = createNewChannelsForNewMeterActivation(newMeterActivation, channels);
                    newMeterActivation.advanceStartDate(effectiveTimestamp);
                    removeReadingQualities(newChannels);
                    return newMeterActivation;
                })
                .orElseGet(() -> createNewMeterActivation(combo, effectiveTimestamp));
    }

    private MeterActivation createNewMeterActivation(Combo combo, Instant timestamp) {
        UsagePoint usagePoint = determineUsagePoint(combo);
        MeterActivation newMeterActivation = null;
        if (usagePoint == null) {
            newMeterActivation = device.activate(timestamp);
        } else {
            newMeterActivation = device.forceActivate(timestamp, usagePoint);
        }
        return newMeterActivation;
    }

    private UsagePoint determineUsagePoint(Combo combo) {
        UsagePoint usagePoint = null;
        if (combo.getUsagePoint() != null) {
            usagePoint = combo.getUsagePoint();
        } else if (combo.isCopyUsagePoint()) {
            usagePoint = Optional.ofNullable(previousMeterActivation.get())
                    .flatMap(MeterActivation::getUsagePoint)
                    .orElse(null);
        }
        return usagePoint;
    }

    /**
     * Data have been copied from old to new channels, but we should erase validation related qualities: see COMU-3231
     *
     * @param channels
     */
    private static void removeReadingQualities(List<Channel> channels) {
        channels.stream()
                .flatMap(channel -> channel.findReadingQualities()
                        // TODO: think of what systems should be taken into account when removing validation related qualities
                        .ofQualitySystems(ImmutableSet.of(QualityCodeSystem.MDC, QualityCodeSystem.MDM))
                        .actual()
                        .ofAnyQualityIndexInCategories(ImmutableSet.of(QualityCodeCategory.REASONABILITY, QualityCodeCategory.VALIDATION))
                        .stream())
                .forEach(ReadingQualityRecord::delete);
    }

    private List<Channel> createNewChannelsForNewMeterActivation(MeterActivation newMeterActivation, List<Channel> channels) {
        List<Channel> defaultChannels = newMeterActivation.getChannelsContainer().getChannels();
        List<Channel> newChannels = channels.stream()
                .filter(not(channel1 -> defaultChannels.stream()
                        .anyMatch(defaultChannel -> defaultChannel.hasReadingType(channel1.getMainReadingType()))))
                .map(channel -> {
                    ReadingType mainReadingType = channel.getMainReadingType();
                    ReadingType[] extraReadingTypes = channel.getReadingTypes()
                            .stream()
                            .filter(rt -> !rt.equals(mainReadingType))
                            .toArray(ReadingType[]::new);
                    return newMeterActivation.getChannelsContainer().createChannel(mainReadingType, extraReadingTypes);
                }).collect(Collectors.toList());
        newChannels.addAll(defaultChannels);
        return newChannels;
    }

    private Optional<Instant> maxEffectiveTimestampAfterLastData(Instant effectiveTimestamp, Device device) {
        Stream<Instant> loadProfileTimes = device.getLoadProfiles()
                .stream()
                .map(LoadProfile::getLastReading)
                .flatMap(Functions.asStream());
        Stream<Instant> registerTimes = device.getRegisters()
                .stream()
                .map(r -> (Register<?, ?>) r)
                .map(Register::getLastReadingDate)
                .flatMap(Functions.asStream());
        return Stream.of(registerTimes, loadProfileTimes)
                .flatMap(Function.identity())
                .filter(max -> max.isAfter(effectiveTimestamp))
                .max(Instant::compareTo);
    }

}
