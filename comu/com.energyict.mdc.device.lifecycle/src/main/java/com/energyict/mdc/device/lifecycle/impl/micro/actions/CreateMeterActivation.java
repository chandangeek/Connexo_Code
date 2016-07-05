package com.energyict.mdc.device.lifecycle.impl.micro.actions;

import com.elster.jupiter.cbo.QualityCodeCategory;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingQualityRecord;
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

import com.google.common.collect.ImmutableSet;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.streams.Predicates.not;

/**
 * Provides an implementation for the {@link ServerMicroAction} interface
 * that will create a meter activation for the Device on the effective
 * timestamp of the transition.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @see {@link com.energyict.mdc.device.lifecycle.config.MicroAction#CREATE_METER_ACTIVATION}
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
            List<Channel> channels = device.getCurrentMeterActivation()
                    .map(MeterActivation::getChannelsContainer)
                    .map(ChannelsContainer::getChannels)
                    .orElse(Collections.emptyList());
            MeterActivation newMeterActivation = device.activate(lastDataTimestamp.get());
            List<Channel> newChannels = createNewChannelsForNewMeterActivation(newMeterActivation, channels);
            newMeterActivation.advanceStartDate(effectiveTimestamp);
            removeReadingQualities(newChannels);
        } else {
            device.activate(effectiveTimestamp);
        }
    }

    @Override
    protected MicroAction getMicroAction() {
        return MicroAction.CREATE_METER_ACTIVATION;
    }

    private static Optional<Instant> maxEffectiveTimestampAfterLastData(Instant effectiveTimestamp, Device device) {
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

    private List<Channel> createNewChannelsForNewMeterActivation(MeterActivation newMeterActivation, List<Channel> channels) {
        List<Channel> defaultChannels = newMeterActivation.getChannelsContainer().getChannels();
        List<Channel> newChannels = channels.stream()
                .filter(not(channel1 -> defaultChannels.stream().anyMatch(defaultChannel -> defaultChannel.hasReadingType(channel1.getMainReadingType()))))
                .map(channel -> {
                    ReadingType mainReadingType = channel.getMainReadingType();
                    ReadingType[] extraReadingTypes = channel.getReadingTypes().stream().filter(rt -> !rt.equals(mainReadingType)).toArray(ReadingType[]::new);
                    return newMeterActivation.getChannelsContainer().createChannel(mainReadingType, extraReadingTypes);
                }).collect(Collectors.toList());
        newChannels.addAll(defaultChannels);
        return newChannels;
    }

    /** Data have been copied from old to new channels, but we should erase validation related qualities: see COMU-3231
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
}
