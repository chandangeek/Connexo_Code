package com.energyict.mdc.device.lifecycle.impl.micro.actions;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.util.streams.Functions;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.lifecycle.ExecutableActionProperty;
import com.energyict.mdc.device.lifecycle.config.MicroAction;
import com.energyict.mdc.device.lifecycle.impl.ServerMicroAction;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Provides an implementation for the {@link ServerMicroAction} interface
 * that will create a meter activation for the Device on the effective
 * timestamp of the transition.
 * @see {@link com.energyict.mdc.device.lifecycle.config.MicroAction#CREATE_METER_ACTIVATION}
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-2 5(14:19)
 */
public class CreateMeterActivation extends TranslatableServerMicroAction {

    public CreateMeterActivation(Thesaurus thesaurus) {
        super(thesaurus);
    }

    @Override
    public List<PropertySpec> getPropertySpecs(PropertySpecService propertySpecService) {
        // Remember that effective timestamp is a required property enforced by the service's execute method
        return Collections.emptyList();
    }

    @Override
    public void execute(Device device, Instant effectiveTimestamp, List<ExecutableActionProperty> properties) {
        Optional<Instant> lastDataTimestamp = maxEffectiveTimestampAfterLastData(effectiveTimestamp, device);
        if (lastDataTimestamp.isPresent()) {
            List<Channel> channels = device.getCurrentMeterActivation().map(MeterActivation::getChannels).orElse(Collections.emptyList());
            device.deactivate(lastDataTimestamp.get());
            MeterActivation newMeterActivation = device.activate(lastDataTimestamp.get());
            populateMeterActivationWithChannels(newMeterActivation, channels);
            newMeterActivation.advanceStartDate(effectiveTimestamp);
        } else {
            device.activate(effectiveTimestamp);
        }
    }

    @Override
    protected MicroAction getMicroAction() {
        return MicroAction.CREATE_METER_ACTIVATION;
    }

    private Optional<Instant> maxEffectiveTimestampAfterLastData(Instant effectiveTimestamp, Device device) {
        Stream<Instant> loadProfileTimes = device.getLoadProfiles()
                .stream()
                .map(LoadProfile::getLastReading)
                .flatMap(Functions.asStream());
        Stream<Instant> registerTimes = device.getRegisters()
                .stream()
                .map(r -> (Register<?,?>) r)
                .map(Register::getLastReadingDate)
                .flatMap(Functions.asStream());
        return Stream.of(registerTimes, loadProfileTimes)
                .flatMap(Function.identity())
                .filter(max -> max.isAfter(effectiveTimestamp))
                .max(Instant::compareTo);
    }

    private void populateMeterActivationWithChannels(MeterActivation newMeterActivation, List<Channel> channels) {
        for(Channel channel : channels) {
            ReadingType mainReadingType = channel.getMainReadingType();
            ReadingType[] extraReadingTypes = channel.getReadingTypes().stream().filter(rt -> !rt.equals(mainReadingType)).toArray(ReadingType[]::new);
            newMeterActivation.createChannel(mainReadingType, extraReadingTypes);
        }
    }
}