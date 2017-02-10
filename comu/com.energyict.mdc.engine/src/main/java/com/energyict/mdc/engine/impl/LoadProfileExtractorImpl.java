package com.energyict.mdc.engine.impl;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.upl.Services;
import com.energyict.mdc.upl.messages.legacy.LoadProfileExtractor;
import com.energyict.obis.ObisCode;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides an implementation for the {@link LoadProfileExtractor} interface
 * that assumes that all UPL objects are in fact {@link LoadProfile}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-01-19 (09:48)
 */
@Component(name = "com.energyict.mdc.upl.messages.legacy.loadprofile.extractor", service = {LoadProfileExtractor.class}, immediate = true)
@SuppressWarnings("unused")
public class LoadProfileExtractorImpl implements LoadProfileExtractor {

    private volatile MdcReadingTypeUtilService readingTypeUtilService;

    // For OSGi purposes
    public LoadProfileExtractorImpl() {
        super();
    }

    // For testing purposes
    @Inject
    public LoadProfileExtractorImpl(MdcReadingTypeUtilService readingTypeUtilService) {
        this.readingTypeUtilService = readingTypeUtilService;
    }

    @Activate
    public void activate() {
        Services.loadProfileExtractor(this);
    }

    @Deactivate
    public void deactivate() {
        Services.loadProfileExtractor(null);
    }

    @Reference
    public void setReadingTypeUtilService(MdcReadingTypeUtilService readingTypeUtilService) {
        this.readingTypeUtilService = readingTypeUtilService;
    }

    @Override
    public String id(com.energyict.mdc.upl.meterdata.LoadProfile loadProfile) {
        return Long.toString(((LoadProfile) loadProfile).getId());
    }

    @Override
    public String specDeviceObisCode(com.energyict.mdc.upl.meterdata.LoadProfile uplLoadProfile) {
        LoadProfile loadProfile = (LoadProfile) uplLoadProfile;
        return loadProfile.getLoadProfileSpec().getDeviceObisCode().toString();
    }

    @Override
    public String deviceSerialNumber(com.energyict.mdc.upl.meterdata.LoadProfile uplLoadProfile) {
        LoadProfile loadProfile = (LoadProfile) uplLoadProfile;
        return loadProfile.getDevice().getSerialNumber();
    }

    @Override
    public List<Channel> channels(com.energyict.mdc.upl.meterdata.LoadProfile uplLoadProfile) {
        LoadProfile loadProfile = (LoadProfile) uplLoadProfile;
        Device device = loadProfile.getDevice();
        return loadProfile
                .getChannels()
                .stream()
                .map(channel -> new ExtractedChannel(device, channel))
                .collect(Collectors.toList());
    }

    @Override
    public List<Register> registers(com.energyict.mdc.upl.meterdata.LoadProfile uplLoadProfile) {
        LoadProfile loadProfile = (LoadProfile) uplLoadProfile;
        Device device = loadProfile.getDevice();
        return loadProfile
                .getChannels()
                .stream()
                .map(channel -> new ExtractedRegister(device, channel))
                .collect(Collectors.toList());
    }

    private static class ExtractedRegister implements Register {
        private final Device device;
        private final com.energyict.mdc.device.data.Channel channel;

        private ExtractedRegister(Device device, com.energyict.mdc.device.data.Channel channel) {
            this.device = device;
            this.channel = channel;
        }

        @Override
        public String deviceSerialNumber() {
            return this.device.getSerialNumber();
        }

        @Override
        public String obisCode() {
            return this.channel.getChannelSpec().getDeviceObisCode().toString();
        }

        @Override
        public int getRegisterId() {
            ObisCode deviceObisCode = this.channel.getChannelSpec().getDeviceObisCode();
            return device.getRegisterWithDeviceObisCode(deviceObisCode)
                    .map(register -> ((int) register.getRegisterSpecId()))
                    .orElse(0);
        }
    }

    private class ExtractedChannel implements Channel {
        private final Device device;
        private final com.energyict.mdc.device.data.Channel channel;

        private ExtractedChannel(Device device, com.energyict.mdc.device.data.Channel channel) {
            this.device = device;
            this.channel = channel;
        }

        @Override
        public String deviceSerialNumber() {
            return this.device.getSerialNumber();
        }

        @Override
        public String obisCode() {
            return this.channel.getChannelSpec().getDeviceObisCode().toString();
        }

        @Override
        public String unit() {
            return readingTypeUtilService.getMdcUnitFor(this.channel.getChannelSpec().getReadingType()).toString();
        }
    }
}