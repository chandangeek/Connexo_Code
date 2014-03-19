package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.time.UtcInstant;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.data.impl.offline.OfflineLoadProfileImpl;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.device.BaseLoadProfile;
import com.energyict.mdc.protocol.api.device.offline.OfflineLoadProfile;
import com.energyict.mdc.protocol.api.device.offline.OfflineLoadProfileChannel;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
    public BaseDevice getDevice() {
        return this.device.get();
    }

    @Override
    public List<Channel> getAllChannels() {
        List<Channel> allChannels = getChannels();
        for (BaseDevice<Channel, LoadProfile, Register> physicalConnectedDevice : this.device.get().getPhysicalConnectedDevices()) {
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

    private void save() {
        Save.action(getId()).save(dataModel, this);
    }

    @Override
    public OfflineLoadProfile goOffline() {
        return new OfflineLoadProfileImpl(this);
    }

    abstract static class LoadProfileUpdater implements LoadProfile.LoadProfileUpdater {

        final LoadProfileImpl loadProfile;

        protected LoadProfileUpdater(LoadProfileImpl loadProfile) {
            this.loadProfile = loadProfile;
        }

        @Override
        public LoadProfile.LoadProfileUpdater setLastReadingIfLater(Date lastReading) {
            Date date = this.loadProfile.lastReading.toDate();
            if (lastReading != null && (this.loadProfile.lastReading == null || lastReading.after(date))) {
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
            this.loadProfile.save();
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
        public BaseDevice getDevice() {
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
            return this.channelSpec.getRegisterMapping().getObisCode();
        }

        @Override
        public OfflineLoadProfileChannel goOffline() {
            return null;
        }
    }
}
