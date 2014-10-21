package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.data.tasks.ConnectionTask;

import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.util.conditions.Condition;
import com.google.common.collect.ImmutableMap;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import static com.elster.jupiter.util.conditions.Where.where;

public class ResourceHelper {

    private final DeviceService deviceService;
    private final ExceptionFactory exceptionFactory;
    private final MeteringService meteringService;

    @Inject
    public ResourceHelper(DeviceService deviceService, ExceptionFactory exceptionFactory, MeteringService meteringService) {
        super();
        this.deviceService = deviceService;
        this.exceptionFactory = exceptionFactory;
        this.meteringService = meteringService;
    }

    public Device findDeviceByMrIdOrThrowException(String mRID) {
        Device device = deviceService.findByUniqueMrid(mRID);
        if (device == null) {
            throw exceptionFactory.newException(MessageSeeds.NO_SUCH_DEVICE, mRID);
        }
        return device;
    }

    public Register findRegisterOrThrowException(Device device, long registerSpecId) {
        return device.getRegisters()
                .stream()
                .filter(r -> r.getRegisterSpec().getId() == registerSpecId)
                .findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_REGISTER, registerSpecId));
    }

    public LoadProfile findLoadProfileOrThrowException(Device device, long loadProfileId) {
        return device.getLoadProfiles()
                .stream()
                .filter(lp -> lp.getId() == loadProfileId)
                .findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_LOAD_PROFILE_ON_DEVICE, device.getmRID(), loadProfileId));
    }

    public Channel findChannelOrThrowException(LoadProfile loadProfile, long channelSpecId) {
        return loadProfile.getChannels()
                .stream()
                .filter(c -> c.getChannelSpec().getId() == channelSpecId)
                .findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_CHANNEL_ON_LOAD_PROFILE, loadProfile.getId(), channelSpecId));
    }

    public Condition getQueryConditionForDevice(StandardParametersBean params) {
        ImmutableMap<String, BiFunction<String, StandardParametersBean, Condition>> keyToConditionFunction =
            ImmutableMap.of(
                    "mRID", (String k, StandardParametersBean p) -> toSimpleCondition(p, k),
                    "serialNumber", (String k, StandardParametersBean p) -> toSimpleCondition(p, k),
                    "deviceTypeName", (String k, StandardParametersBean p) -> toMultiValuedCondition(k, "deviceConfiguration.deviceType.name"),
                    "deviceConfigurationName", (String k, StandardParametersBean p) -> toMultiValuedCondition(k, "deviceConfiguration.name"));
        return keyToConditionFunction.entrySet()
            .stream()
            .filter(e -> params.containsKey(e.getKey()))
            .map(e -> e.getValue().apply(e.getKey(), params))
            .reduce(
                Condition.TRUE,
                (c1, c2) -> c1.and(c2));
    }

    private Condition toSimpleCondition(StandardParametersBean params, String key) {
        String value = params.getFirst(key);
        if (params.wasRegExp()) {
            return where(key).likeIgnoreCase(value);
        }
        else {
            return where(key).isEqualTo(value);
        }
    }

    private Condition toMultiValuedCondition(String params, String conditionField) {
        return Stream
            .of(params.split(","))
            .map(v -> where(conditionField).isEqualTo(v.trim()))
            .reduce(
                Condition.FALSE,
                (c1, c2) -> c1.or(c2));
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
        Meter meter;
        AmrSystem amrSystem = meteringService.findAmrSystem(1).get();
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
        return this.getMeterActivationsMostCurrentFirst(meter)
                .stream()
                .map(ma -> getChannel(ma, register.getRegisterSpec().getRegisterType().getReadingType()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    public Optional<com.elster.jupiter.metering.Channel> getLoadProfileChannel(Channel channel, Meter meter) {
        return this.getMeterActivationsMostCurrentFirst(meter)
                .stream()
                .map(ma -> getChannel(ma, channel.getReadingType()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    public Optional<com.elster.jupiter.metering.Channel> getChannel(MeterActivation meterActivation, ReadingType readingType) {
        return meterActivation.getChannels()
                .stream()
                .filter(c -> c.getReadingTypes().contains(readingType))
                .findFirst();
    }

    public ConnectionTask<?, ?> findConnectionTaskOrThrowException(Device device, long connectionMethodId) {
        return device.getConnectionTasks()
                .stream()
                .filter(ct -> ct.getId() == connectionMethodId)
                .findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_CONNECTION_METHOD, device.getmRID(), connectionMethodId));
    }

}
