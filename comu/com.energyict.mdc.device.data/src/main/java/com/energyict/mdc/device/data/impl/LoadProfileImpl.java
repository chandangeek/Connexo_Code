package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.time.UtcInstant;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.common.interval.Phenomenon;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.protocol.api.device.BaseLoadProfile;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.inject.Inject;

/**
 * Provides an implementation of a LoadProfile of a {@link com.energyict.mdc.device.data.Device}
 * <p/>
 * Copyrights EnergyICT
 * Date: 3/17/14
 * Time: 3:57 PM
 */
public class LoadProfileImpl implements LoadProfile {

    private final DeviceConfigurationService deviceConfigurationService;
    private final DataModel dataModel;

    private long id;
    private Reference<Device> device = ValueReference.absent();
    private Reference<LoadProfileSpec> loadProfileSpec = ValueReference.absent();
    private UtcInstant lastReading;

    @Inject
    public LoadProfileImpl(DataModel dataModel, DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
        this.dataModel = dataModel;
    }

    LoadProfileImpl initialize(LoadProfileSpec loadProfileSpec, Device device) {
        this.loadProfileSpec.set(loadProfileSpec);
        this.device.set(device);
        return this;
    }

    @Override
    public Date getLastReading() {
        if(lastReading != null){
            return lastReading.toDate();
        } else {
            return null;
        }
    }

    @Override
    public List<Channel> getChannels() {
        List<Channel> channels = new ArrayList<>();
        for (ChannelSpec channelSpec : this.deviceConfigurationService.findChannelSpecsForLoadProfileSpec(getLoadProfileSpec())) {
            channels.add(new ChannelImpl(channelSpec, this));
        }
        return channels;
    }

    @Override
    public boolean isVirtualLoadProfile() {
        boolean needsProxy = this.device.get().getDeviceType().isLogicalSlave();
        boolean wildCardInBfield = getLoadProfileSpec().getDeviceObisCode().anyChannel();
        return needsProxy && !wildCardInBfield;
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
    public List<Channel> getAllChannels() {
        List<Channel> allChannels = getChannels();
        for (Device physicalConnectedDevice : this.device.get().getPhysicalConnectedDevices()) {
            if (physicalConnectedDevice.isLogicalSlave()) {
                for (Channel channel : physicalConnectedDevice.getChannels()) {
                    if (channel.getLoadProfile().getLoadProfileTypeId() == this.getLoadProfileTypeId()) {
                        allChannels.add(channel);
                    }
                }
            }
        }
        return allChannels;
    }

    @Override
    public long getLoadProfileTypeId() {
        return getLoadProfileSpec().getLoadProfileType().getId();
    }

    @Override
    public ObisCode getLoadProfileTypeObisCode() {
        return getLoadProfileSpec().getLoadProfileType().getObisCode();
    }

    private void update() {
        Save.UPDATE.save(dataModel, this);
    }

    abstract static class LoadProfileUpdater implements LoadProfile.LoadProfileUpdater {

        private final LoadProfileImpl loadProfile;

        protected LoadProfileUpdater(LoadProfileImpl loadProfile) {
            this.loadProfile = loadProfile;
        }

        @Override
        public LoadProfile.LoadProfileUpdater setLastReadingIfLater(Date lastReading) {
            UtcInstant loadProfileLastReading = this.loadProfile.lastReading;
            if (lastReading != null && (loadProfileLastReading == null || lastReading.after(loadProfileLastReading.toDate()))) {
                this.loadProfile.lastReading = new UtcInstant(lastReading);
            }
            return this;
        }

        @Override
        public LoadProfile.LoadProfileUpdater setLastReading(Date lastReading) {
            this.loadProfile.lastReading = new UtcInstant(lastReading);
            return this;
        }

        @Override
        public void update() {
            this.loadProfile.update();
        }
    }

    /**
     * Provides an implementation of a Channel of a {@link com.energyict.mdc.device.data.Device},
     * which is actually a wrapping around a {@link com.energyict.mdc.device.config.ChannelSpec}
     * of the {@link com.energyict.mdc.device.config.DeviceConfiguration}.
     * <i>Currently a {@link Device} can only have Channel if it is owned by a LoadProfile</i>
     * <p/>
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
         * The LoadProfile which 'owns' this Channel
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
            return this.channelSpec.getPhenomenon().getUnit();
        }

        @Override
        public BaseLoadProfile getLoadProfile() {
            return this.loadProfile;
        }

        @Override
        public ObisCode getRegisterTypeObisCode() {
            return this.channelSpec.getChannelType().getObisCode();
        }

        @Override
        public String getName() {
            return getChannelSpec().getName();
        }

        @Override
        public TimeDuration getInterval() {
            return getChannelSpec().getInterval();
        }

        @Override
        public Phenomenon getPhenomenon() {
            return getChannelSpec().getPhenomenon();
        }

        @Override
        public Date getLastReading() {
            return ((LoadProfile)getLoadProfile()).getLastReading();
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
        public BigDecimal getMultiplier() {
            return channelSpec.getMultiplier();
        }

        @Override
        public ReadingType getReadingType() {
            return channelSpec.getReadingType();
        }
    }
}
