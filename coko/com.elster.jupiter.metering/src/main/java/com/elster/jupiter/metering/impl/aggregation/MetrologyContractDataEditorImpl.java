/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.Category;
import com.elster.jupiter.calendar.Event;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.aggregation.DataAggregationService;
import com.elster.jupiter.metering.aggregation.IncompatibleTimeOfUseException;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.impl.DefaultTranslationKey;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.util.Ranges;

import com.google.common.collect.Range;

import java.time.Instant;
import java.time.Year;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Provides an implementation for the {@link DataAggregationService.MetrologyContractDataEditor} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-03-27 (16:13)
 */
public class MetrologyContractDataEditorImpl implements DataAggregationService.MetrologyContractDataEditor {
    private final UsagePoint usagePoint;
    private final ZoneId usagePointZoneId;
    private final MetrologyContract contract;
    private final ReadingTypeDeliverable deliverable;
    private final QualityCodeSystem qualityCodeSystem;
    private final ServerDataAggregationService dataAggregationService;
    private final Map<Range<Instant>, EffectiveEditor> editors = new HashMap<>();
    private final TimeOfUseValidator timeOfUseValidator;
    private Mode mode;

    public MetrologyContractDataEditorImpl(UsagePoint usagePoint, MetrologyContract contract, ReadingTypeDeliverable deliverable, QualityCodeSystem qualityCodeSystem, ServerDataAggregationService dataAggregationService) {
        this.usagePoint = usagePoint;
        this.contract = contract;
        this.deliverable = deliverable;
        this.qualityCodeSystem = qualityCodeSystem;
        this.dataAggregationService = dataAggregationService;
        if (deliverable.getReadingType().getTou() == 0) {
            this.timeOfUseValidator = new WithoutTimeOfUse();
        } else {
            this.timeOfUseValidator = new StrictValidator(this.deliverable.getReadingType().getTou());
        }
        this.mode = Mode.OPEN;
        this.usagePointZoneId = usagePoint.getZoneId();
    }

    @Override
    public UsagePoint getUsagePoint() {
        return usagePoint;
    }

    @Override
    public DataAggregationService.MetrologyContractDataEditor removeTimestamps(Set<Instant> readingTimestamps) {
        this.mode.validate();
        readingTimestamps
                .forEach(readingTimestamp -> {
                    this.timeOfUseValidator.validate(readingTimestamp);
                    this.findOrCreateEffectiveEditor(readingTimestamp)
                            .remove(readingTimestamp);
                });
        return this;
    }

    @Override
    public DataAggregationService.MetrologyContractDataEditor removeReadings(Set<BaseReadingRecord> readings) {
        this.mode.validate();
        readings.forEach(reading -> {
            this.timeOfUseValidator.validate(reading.getTimeStamp());
            this.findOrCreateEffectiveEditor(reading.getTimeStamp()).remove(reading);
        });
        return this;
    }

    @Override
    public DataAggregationService.MetrologyContractDataEditor estimateAll(List<BaseReading> readings) {
        this.mode.validate();
        readings.forEach(reading -> {
            this.timeOfUseValidator.validate(reading.getTimeStamp());
            this.findOrCreateEffectiveEditor(reading.getTimeStamp()).estimate(reading);
        });
        return this;
    }

    @Override
    public DataAggregationService.MetrologyContractDataEditor confirmAll(List<BaseReading> readings) {
        this.mode.validate();
        readings.forEach(reading -> {
            this.timeOfUseValidator.validate(reading.getTimeStamp());
            this.findOrCreateEffectiveEditor(reading.getTimeStamp()).confirm(reading);
        });
        return this;
    }

    @Override
    public DataAggregationService.MetrologyContractDataEditor updateAll(List<BaseReading> readings) {
        this.mode.validate();
        readings.forEach(reading -> {
            this.timeOfUseValidator.validate(reading.getTimeStamp());
            this.findOrCreateEffectiveEditor(reading.getTimeStamp()).update(reading);
        });
        return this;
    }

    private EffectiveEditor findOrCreateEffectiveEditor(Instant readingTimestamp) {
        Optional<Range<Instant>> effectiveMetrologyConfigurationRange = this.editors
                .keySet()
                .stream()
                .filter(range -> range.contains(readingTimestamp))
                .findAny();
        if (effectiveMetrologyConfigurationRange.isPresent()) {
            return this.editors.get(effectiveMetrologyConfigurationRange.get());
        } else {
            List<EffectiveMetrologyConfigurationOnUsagePoint> effectiveMetrologyConfigurations
                    = this.usagePoint.getEffectiveMetrologyConfigurations(toReadingInterval(readingTimestamp));
            if (effectiveMetrologyConfigurations.size() == 1) {
                EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration = effectiveMetrologyConfigurations.get(0);
                if (this.dataAggregationService.hasContract(effectiveMetrologyConfiguration, this.contract)) {
                    ActiveEditor editor = new ActiveEditor(effectiveMetrologyConfiguration);
                    Range<Instant> effectiveRange = effectiveMetrologyConfiguration.getRange();
                    this.editors.put(Ranges.copy(effectiveRange).asOpenClosed(), editor);
                    return editor;
                } else {
                    throw new IllegalArgumentException("The metrology configuration linked to usage point " + this.usagePoint.getMRID()
                            + " @" + readingTimestamp + " does not contain the contract " + this.contract.getId());
                }
            } else {
                throw new IllegalArgumentException("No metrology configuration linked to usage point " + this.usagePoint.getMRID() + " @" + readingTimestamp);
            }
        }
    }

    private Range<Instant> toReadingInterval(Instant instant) {
        ZonedDateTime readingTime = ZonedDateTime.ofInstant(instant, usagePointZoneId);
        return this.deliverable.getReadingType().getIntervalLength()
                .map(intervalLength -> Range.open(readingTime.minus(intervalLength).toInstant(), instant))
                .orElse(Range.closed(instant, instant));
    }

    @Override
    public void save() {
        this.editors.values().forEach(EffectiveEditor::save);
        this.mode = Mode.CLOSED;
    }

    private enum Mode {
        OPEN {
            @Override
            void validate() {
                // All edit operations are allowed as long as the editor is open.
            }
        },

        CLOSED {
            @Override
            void validate() {
                throw new IllegalStateException("Once " + DataAggregationService.MetrologyContractDataEditor.class.getSimpleName() + " is saved, it cannot be used again");
            }
        };

        abstract void validate();
    }

    private interface EffectiveEditor {
        void remove(Instant readingTimestamp);
        void remove(BaseReadingRecord reading);
        void estimate(BaseReading reading);
        void confirm(BaseReading reading);
        void update(BaseReading reading);
        void save();
    }

    private class ActiveEditor implements EffectiveEditor {
        private final Channel channel;
        private final List<BaseReadingRecord> forRemoval = new ArrayList<>();
        private final List<BaseReading> forEstimation = new ArrayList<>();
        private final List<BaseReading> forConfirmation = new ArrayList<>();
        private final List<BaseReading> forUpdating = new ArrayList<>();

        private ActiveEditor(EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration) {
            ChannelsContainer channelsContainer = effectiveMetrologyConfiguration.getChannelsContainer(contract).orElseThrow(() -> new IllegalStateException("ChannelsContainer for contract " + contract.getId() + " is missing"));
            this.channel = channelsContainer.getChannel(deliverable.getReadingType()).orElseThrow(() -> new IllegalStateException("Channel for deliverable " + deliverable.getReadingType().getName() + "(mRID=" + deliverable.getReadingType().getMRID() + ") is missing"));
        }

        @Override
        public void remove(Instant timestamp) {
            BaseReadingRecord reading = this.channel.getReading(timestamp).orElseThrow(() -> new IllegalArgumentException("No reading in channel for reading type " + deliverable.getReadingType().getMRID() + " @ " + timestamp));
            this.remove(reading);
        }

        @Override
        public void remove(BaseReadingRecord reading) {
            this.forRemoval.add(reading);
        }

        @Override
        public void estimate(BaseReading reading) {
            this.forEstimation.add(reading);
        }

        @Override
        public void confirm(BaseReading reading) {
            this.forConfirmation.add(reading);
        }

        @Override
        public void update(BaseReading reading) {
            this.forUpdating.add(reading);
        }

        @Override
        public void save() {
            this.channel.removeReadings(qualityCodeSystem, this.forRemoval);
            this.channel.estimateReadings(qualityCodeSystem, this.forEstimation);
            this.channel.confirmReadings(qualityCodeSystem, this.forConfirmation);
            this.channel.editReadings(qualityCodeSystem, this.forUpdating);
        }
    }

    private interface TimeOfUseValidator {
        void validate(Instant timestamp);
    }

    /**
     * Provides an implementation for the TimeOfUseValidator interface
     * when time of use is not active on the target ReadingTypeDeliverable.
     */
    private class WithoutTimeOfUse implements TimeOfUseValidator {
        @Override
        public void validate(Instant timestamp) {
            // All edits are fine when time of use is not active
        }
    }

    private class StrictValidator implements TimeOfUseValidator {
        private final int tou;
        private final Category timeOfUseCategory;
        private final ZoneId zoneId;
        private final UsagePoint.UsedCalendars usedCalendars;
        private final Map<Year, Calendar.ZonedView> calendarViews = new HashMap<>();

        private StrictValidator(int tou) {
            this.tou = tou;
            this.timeOfUseCategory = dataAggregationService.getTimeOfUseCategory();
            this.zoneId = usagePoint.getZoneId();
            this.usedCalendars = usagePoint.getUsedCalendars();
        }

        @Override
        public void validate(Instant timestamp) {
            this.usedCalendars
                    .getCalendar(timestamp, this.timeOfUseCategory)
                    .ifPresent(calendar -> this.validate(calendar, timestamp));
        }

        private void validate(Calendar calendar, Instant timestamp) {
            Event actualEvent = this.getEvent(calendar, timestamp);
            if (this.tou != actualEvent.getCode()) {
                Event expectedEvent =
                        calendar
                                .getEvents()
                                .stream()
                                .filter(event -> this.tou == event.getCode())
                                .findAny()
                                .orElseGet(() -> new Missing(this.tou));
                throw new IncompatibleTimeOfUseException(
                        dataAggregationService.getThesaurus(),
                        expectedEvent,
                        actualEvent);
            }
        }

        private Event getEvent(Calendar calendar, Instant timestamp) {
            return this.getView(calendar, timestamp).eventFor(timestamp);
        }

        private Calendar.ZonedView getView(Calendar calendar, Instant timestamp) {
            Year year = Year.from(timestamp.atZone(this.zoneId));
            return this.calendarViews
                    .computeIfAbsent(
                            year,
                            y -> calendar.forZone(this.zoneId, y, y));
        }
    }

    private class Missing implements Event {
        private final long code;

        private Missing(long code) {
            this.code = code;
        }

        @Override
        public long getCode() {
            return this.code;
        }

        @Override
        public Instant getCreateTime() {
            return dataAggregationService.getClock().instant();
        }

        @Override
        public long getVersion() {
            return 0;
        }

        @Override
        public Instant getModTime() {
            return dataAggregationService.getClock().instant();
        }

        @Override
        public String getUserName() {
            return "Not relevant";
        }

        @Override
        public long getId() {
            return 0;
        }

        @Override
        public String getName() {
            return dataAggregationService.getThesaurus().getFormat(DefaultTranslationKey.MISSING_EVENT_NAME).format();
        }
    }
}