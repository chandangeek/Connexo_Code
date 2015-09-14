package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.util.Pair;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.ChannelDataUpdater;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.LoadProfileReading;
import com.google.common.collect.Range;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

/**
 * Provides an implementation of a LoadProfile of a {@link com.energyict.mdc.device.data.Device}
 * <p>
 * Copyrights EnergyICT
 * Date: 3/17/14
 * Time: 3:57 PM
 */
public class LoadProfileImpl implements LoadProfile {

    private final DataModel dataModel;

    @SuppressWarnings("unused")
    private long id;
    private Reference<DeviceImpl> device = ValueReference.absent();
    private Reference<LoadProfileSpec> loadProfileSpec = ValueReference.absent();
    private Instant lastReading;
    @SuppressWarnings("unused")
    private String userName;
    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;

    @Inject
    public LoadProfileImpl(DataModel dataModel) {
        super();
        this.dataModel = dataModel;
    }

    LoadProfileImpl initialize(LoadProfileSpec loadProfileSpec, DeviceImpl device) {
        this.loadProfileSpec.set(loadProfileSpec);
        this.device.set(device);
        return this;
    }

    @Override
    public Optional<Instant> getLastReading() {
        return Optional.ofNullable(this.lastReading);
    }

    @Override
    public List<Channel> getChannels() {
        return this.getLoadProfileSpec().getChannelSpecs()
                .stream()
                .map(channelSpec -> new ChannelImpl(channelSpec, this))
                .collect(toList());
    }

    @Override
    public boolean isVirtualLoadProfile() {
        boolean needsProxy = this.device.get().getDeviceType().isLogicalSlave();
        boolean noWildCardInBField = !getLoadProfileSpec().getDeviceObisCode().anyChannel();
        return needsProxy && noWildCardInBField;
    }

    @Override
    public TimeDuration getInterval() {
        return getLoadProfileSpec().getInterval();
    }

    @Override
    public LoadProfileSpec getLoadProfileSpec() {
        return this.loadProfileSpec.get();
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public ObisCode getDeviceObisCode() {
        return getLoadProfileSpec().getDeviceObisCode();
    }

    @Override
    public Device getDevice() {
        return this.device.get();
    }

    @Override
    public long getLoadProfileTypeId() {
        return getLoadProfileSpec().getLoadProfileType().getId();
    }

    @Override
    public ObisCode getLoadProfileTypeObisCode() {
        return getLoadProfileSpec().getLoadProfileType().getObisCode();
    }

    @Override
    public List<LoadProfileReading> getChannelData(Range<Instant> interval) {
        return this.device.get().getChannelData(this, interval);
    }

    private void updateLastReading() {
        this.dataModel.update(this, "lastReading");
    }

    abstract static class LoadProfileUpdater implements LoadProfile.LoadProfileUpdater {

        private final LoadProfileImpl loadProfile;
        private boolean dirty = false;

        protected LoadProfileUpdater(LoadProfileImpl loadProfile) {
            this.loadProfile = loadProfile;
        }

        @Override
        public LoadProfile.LoadProfileUpdater setLastReadingIfLater(Instant lastReading) {
            Instant loadProfileLastReading = this.loadProfile.lastReading;
            if (lastReading != null && (loadProfileLastReading == null || lastReading.isAfter(loadProfileLastReading))) {
                this.setLastReading(lastReading);
            }
            return this;
        }

        @Override
        public LoadProfile.LoadProfileUpdater setLastReading(Instant lastReading) {
            this.loadProfile.lastReading = lastReading;
            this.dirty = true;
            return this;
        }

        @Override
        public void update() {
            if (this.dirty) {
                this.loadProfile.updateLastReading();
            }
        }

    }

    /**
     * Provides an implementation of a Channel of a {@link com.energyict.mdc.device.data.Device},
     * which is actually a wrapping around a {@link com.energyict.mdc.device.config.ChannelSpec}
     * of the {@link com.energyict.mdc.device.config.DeviceConfiguration}.
     * <i>Currently a {@link Device} can only have Channel if it is owned by a LoadProfile</i>
     * <p>
     * Copyrights EnergyICT
     * Date: 3/17/14
     * Time: 2:17 PM
     */
    private class ChannelImpl implements Channel {

        /**
         * The {@link com.energyict.mdc.device.config.ChannelSpec} for which this channel is serving
         */
        private final ChannelSpec channelSpec;

        /**
         * The LoadProfile that 'owns' this Channel.
         */
        private final LoadProfile loadProfile;

        private ChannelImpl(ChannelSpec channelSpec, LoadProfile loadProfile) {
            this.channelSpec = channelSpec;
            this.loadProfile = loadProfile;
        }

        @Override
        public int getIntervalInSeconds() {
            return channelSpec.getInterval().getSeconds();
        }

        @Override
        public ChannelSpec getChannelSpec() {
            return this.channelSpec;
        }

        @Override
        public Device getDevice() {
            return this.loadProfile.getDevice();
        }

        @Override
        public Unit getUnit() {
            return this.channelSpec.getChannelType().getUnit();
        }

        @Override
        public LoadProfile getLoadProfile() {
            return this.loadProfile;
        }

        @Override
        public ObisCode getRegisterTypeObisCode() {
            return this.channelSpec.getChannelType().getObisCode();
        }

        @Override
        public String getName() {
            return getChannelSpec().getReadingType().getAliasName();
        }

        @Override
        public TimeDuration getInterval() {
            return getChannelSpec().getInterval();
        }

        @Override
        public Optional<Instant> getLastReading() {
            return getLoadProfile().getLastReading();
        }

        @Override
        public long getId() {
            return getChannelSpec().getId();
        }

        @Override
        public ObisCode getObisCode() {
            return channelSpec.getObisCode();
        }

        @Override
        public BigDecimal getOverflow() {
            return channelSpec.getOverflow();
        }

        @Override
        public ReadingType getReadingType() {
            return channelSpec.getReadingType();
        }

        @Override
        public List<LoadProfileReading> getChannelData(Range<Instant> interval) {
            return LoadProfileImpl.this.device.get().getChannelData(this, interval);
        }

        @Override
        public Optional<Instant> getLastDateTime() {
            Optional<com.elster.jupiter.metering.Channel> koreChannel = LoadProfileImpl.this.device.get().findKoreChannel(this, Instant.now());
            return koreChannel.map(com.elster.jupiter.metering.Channel::getLastDateTime);
        }

        @Override
        public boolean hasData() {
            return LoadProfileImpl.this.device.get().hasData(this);
        }

        @Override
        public ChannelDataUpdater startEditingData() {
            return new ChannelDataUpdaterImpl(this);
        }
    }

    private class ChannelDataUpdaterImpl implements ChannelDataUpdater {
        private final Channel channel;

        private final List<BaseReading> edited = new ArrayList<>();
        private final List<BaseReading> editedBulk = new ArrayList<>();
        private final List<BaseReading> confirmed = new ArrayList<>();
        private final List<Instant> removed = new ArrayList<>();

        private ChannelDataUpdaterImpl(Channel channel) {
            this.channel = channel;
        }

        @Override
        public ChannelDataUpdater editChannelData(List<BaseReading> modifiedChannelData) {
            this.edited.addAll(modifiedChannelData);
            return this;
        }

        @Override
        public ChannelDataUpdater editBulkChannelData(List<BaseReading> modifiedChannelData) {
            this.editedBulk.addAll(modifiedChannelData);
            return this;
        }

        @Override
        public ChannelDataUpdater confirmChannelData(List<BaseReading> modifiedChannelData) {
            this.confirmed.addAll(modifiedChannelData);
            return this;
        }

        @Override
        public ChannelDataUpdater removeChannelData(List<Instant> intervals) {
            this.removed.addAll(intervals);
            return this;
        }

        @Override
        public void complete() {
            groupReadingsByKoreChannel(this.edited).entrySet().forEach(entry -> entry.getKey().editReadings(entry.getValue()));
            groupReadingsByKoreChannel(this.editedBulk).entrySet().forEach(entry -> {
                com.elster.jupiter.metering.Channel koreChannel = entry.getKey();
                Optional<? extends ReadingType> readingType = koreChannel.getBulkQuantityReadingType();
                if (readingType.isPresent() && koreChannel.getCimChannel(readingType.get()).isPresent()) {
                    koreChannel.getCimChannel(readingType.get()).get().editReadings(this.editedBulk);
                }
            });
            groupReadingsByKoreChannel(this.confirmed).entrySet().forEach(entry -> entry.getKey().confirmReadings(entry.getValue()));
            this.removed.forEach(instant -> {
                LoadProfileImpl.this.device.get().findKoreChannel(channel, instant).ifPresent(koreChannel -> {
                    koreChannel.removeReadings(koreChannel.getReading(instant).map(Stream::of).orElseGet(Stream::empty).collect(toList()));
                });
            });
        }

        private com.elster.jupiter.metering.Channel findOrCreateKoreChannel(Instant when) {
            return LoadProfileImpl.this.device.get().findOrCreateKoreChannel(when, channel);
        }

        private Map<com.elster.jupiter.metering.Channel, List<BaseReading>> groupReadingsByKoreChannel(List<BaseReading> readings) {
            return readings.stream()
                    .map(reading -> Pair.of(findOrCreateKoreChannel(reading.getTimeStamp()), reading))
                    .collect(Collectors.groupingBy(Pair::getFirst, HashMap::new, mapping(Pair::getLast, toList())));
        }
    }
}