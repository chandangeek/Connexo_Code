package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.Register;
import com.google.common.base.Optional;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.elster.jupiter.util.conditions.Where.where;

public class ResourceHelper {

    private final DeviceDataService deviceDataService;
    private final ExceptionFactory exceptionFactory;
    private final MeteringService meteringService;

    @Inject
    public ResourceHelper(DeviceDataService deviceDataService, ExceptionFactory exceptionFactory, MeteringService meteringService) {
        super();
        this.deviceDataService = deviceDataService;
        this.exceptionFactory = exceptionFactory;
        this.meteringService = meteringService;
    }

    public Device findDeviceByMrIdOrThrowException(String mRID) {
        Device device = deviceDataService.findByUniqueMrid(mRID);
        if (device == null) {
            throw exceptionFactory.newException(MessageSeeds.NO_SUCH_DEVICE, mRID);
        }
        return device;
    }

    public Register findRegisterOrThrowException(Device device, long registerId) {
        List<Register> registers = device.getRegisters();
        for (Register register : registers) {
            Optional<RegisterSpec> registerSpecOptional = Optional.fromNullable(register.getRegisterSpec());
            if (registerSpecOptional.isPresent() && registerSpecOptional.get().getId() == registerId) {
                return register;
            }
        }

        throw exceptionFactory.newException(MessageSeeds.NO_SUCH_REGISTER, registerId);
    }

    public LoadProfile findLoadProfileOrThrowException(Device device, long loadProfileId) {
        for (LoadProfile loadProfile : device.getLoadProfiles()) {
            if (loadProfile.getId() == loadProfileId) {
                return loadProfile;
            }
        }
        throw exceptionFactory.newException(MessageSeeds.NO_SUCH_LOAD_PROFILE_ON_DEVICE, device.getmRID(), loadProfileId);
    }

    public Channel findChannelOrThrowException(LoadProfile loadProfile, long channelId) {
        for (Channel channel : loadProfile.getChannels()) {
            if (channel.getChannelSpec().getId() == channelId) {
                return channel;
            }
        }
        throw exceptionFactory.newException(MessageSeeds.NO_SUCH_CHANNEL_ON_LOAD_PROFILE, loadProfile.getId(), channelId);
    }

    public Condition getQueryConditionForDevice(StandardParametersBean params) {
        Condition condition = Condition.TRUE;
        if (params.getQueryParameters().size() > 0) {
            condition = condition.and(addDeviceQueryCondition(params));
        }
        return condition;
    }

    private Condition addDeviceQueryCondition(StandardParametersBean params) {
        Condition conditionDevice = Condition.TRUE;
        String mRID = params.getFirst("mRID");
        if (mRID != null) {
            conditionDevice = !params.isRegExp()
                    ? conditionDevice.and(where("mRID").isEqualTo(mRID))
                    : conditionDevice.and(where("mRID").likeIgnoreCase(mRID));
        }
        String serialNumber = params.getFirst("serialNumber");
        if (serialNumber != null) {
            conditionDevice = !params.isRegExp()
                    ? conditionDevice.and(where("serialNumber").isEqualTo(serialNumber))
                    : conditionDevice.and(where("serialNumber").likeIgnoreCase(serialNumber));
        }
        String deviceType = params.getFirst("deviceTypeName");
        if (deviceType != null) {
            conditionDevice = conditionDevice.and(createMultipleConditions(deviceType, "deviceConfiguration.deviceType.name"));
        }
        String deviceConfiguration = params.getFirst("deviceConfigurationName");
        if (deviceConfiguration != null) {
            conditionDevice = conditionDevice.and(createMultipleConditions(deviceConfiguration, "deviceConfiguration.name"));
        }
        return conditionDevice;
    }

    private Condition createMultipleConditions(String params, String conditionField) {
        Condition condition = Condition.FALSE;
        String[] values = params.split(",");
        for (String value : values) {
            condition = condition.or(where(conditionField).isEqualTo(value.trim()));
        }
        return condition;
    }

    public Meter getMeterFor(Device device) {
        Optional<AmrSystem> amrSystemRef = meteringService.findAmrSystem(1);
        Optional<Meter> meterRef = amrSystemRef.get().findMeter(String.valueOf(device.getId()));
        if (!meterRef.isPresent()) {
            throw new IllegalArgumentException("Validation feature on device " + device.getmRID() +
                    " wasn't configured.");
        }
        return meterRef.get();
    }

    Meter getOrCreateMeterFor(Device device) {
        Meter meter = getMeterFor(device);
        if (meter != null) {
            return meter;
        }
        return createMeter(device);
    }

    private Meter createMeter(Device device) {
        Meter meter;AmrSystem amrSystem = meteringService.findAmrSystem(1).get();
        meter = amrSystem.newMeter(String.valueOf(device.getId()), device.getmRID());
        meter.save();
        return meter;
    }

    public List<MeterActivation> getMeterActivationsMostCurrentFirst(Meter meter) {
        List<MeterActivation> activations = new ArrayList<>(meter.getMeterActivations());
        Collections.reverse(activations);
        return activations;
    }

    public Optional<com.elster.jupiter.metering.Channel> getRegisterChannel(Register register, Meter meter) {
        for (MeterActivation meterActivation : getMeterActivationsMostCurrentFirst(meter)) {
            Optional<com.elster.jupiter.metering.Channel> channelRef = getChannel(meterActivation, register.getRegisterSpec().getRegisterType().getReadingType());
            if (channelRef.isPresent()) {
                return channelRef;
            }
        }
        return Optional.absent();
    }

    public Optional<com.elster.jupiter.metering.Channel> getChannel(MeterActivation meterActivation, ReadingType readingType) {
        for (com.elster.jupiter.metering.Channel channel : meterActivation.getChannels()) {
            if (channel.getReadingTypes().contains(readingType)) {
                return Optional.of(channel);
            }
        }
        return Optional.absent();
    }
}
