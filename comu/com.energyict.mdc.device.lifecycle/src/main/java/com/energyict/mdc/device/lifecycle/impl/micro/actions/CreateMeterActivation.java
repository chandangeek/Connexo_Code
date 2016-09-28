package com.energyict.mdc.device.lifecycle.impl.micro.actions;

import com.elster.jupiter.cbo.QualityCodeCategory;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.ExecutableActionProperty;
import com.energyict.mdc.device.lifecycle.config.MicroAction;
import com.energyict.mdc.device.lifecycle.impl.ServerMicroAction;

import com.google.common.collect.ImmutableSet;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
        Optional<MeterActivation> affectedMeterActivation = device.getMeterActivationsMostRecentFirst()
                .stream()
                .filter(ma -> ma.getRange().contains(effectiveTimestamp))
                .findFirst();
        if (affectedMeterActivation.isPresent()) { // if we already have meter activation, it is possible that it has data
            MeterActivation newActivation = affectedMeterActivation.get().split(effectiveTimestamp);
            removeReadingQualities(newActivation.getChannelsContainer().getChannels());
        } else {
            device.activate(effectiveTimestamp);
        }
    }

    @Override
    protected MicroAction getMicroAction() {
        return MicroAction.CREATE_METER_ACTIVATION;
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
}
