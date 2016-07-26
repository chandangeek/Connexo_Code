package com.energyict.mdc.device.data.impl.sync;

import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.UserPreferencesService;
import com.energyict.mdc.common.DateTimeFormatGenerator;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.exceptions.MeterActivationTimestampNotAfterLastActivationException;
import com.energyict.mdc.device.data.exceptions.UnsatisfiedReadingTypeRequirementsOfUsagePointException;
import com.energyict.mdc.device.data.exceptions.UsagePointAlreadyLinkedToAnotherDeviceException;
import com.energyict.mdc.device.data.impl.DeviceImpl;
import com.energyict.mdc.device.data.impl.ServerDevice;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;

import com.google.common.collect.Range;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Additional behaviour related to 'Kore' objects when the Device's UsagePoint is changed
 */
public class SynchDeviceWithKoreForUsagePointChange extends AbstractSyncDeviceWithKoreMeter {

    private ServerDevice device;
    private UsagePoint usagePoint;
    private final DeviceConfigurationService deviceConfigurationService;
    private final Thesaurus thesaurus;
    private final UserPreferencesService userPreferencesService;
    private final ThreadPrincipalService threadPrincipalService;

    public SynchDeviceWithKoreForUsagePointChange(ServerDevice device, Instant start, UsagePoint usagePoint, MeteringService meteringService, MdcReadingTypeUtilService readingTypeUtilService, DeviceConfigurationService deviceConfigurationService, Thesaurus thesaurus, UserPreferencesService userPreferencesService, ThreadPrincipalService threadPrincipalService) {
        super(meteringService, readingTypeUtilService, (start == null ? device.getKoreHelper().getCurrentMeterActivation().get().getStart() : start));
        this.device = device;
        this.usagePoint = usagePoint;
        this.deviceConfigurationService = deviceConfigurationService;
        this.thesaurus = thesaurus;
        this.userPreferencesService = userPreferencesService;
        this.threadPrincipalService = threadPrincipalService;
    }

    @Override
    public void syncWithKore(DeviceImpl device) {
        setDevice(device);

        MeterActivation activation = activateMeter(getStart());
        // add Kore Channels for all MDC Channels and registers
        addKoreChannelsIfNecessary(activation);
    }

    @Override
    public boolean canUpdateCurrentMeterActivation() {
        return false;   // a meter activation with the newly set usagepoint needs to be created
    }

    protected MeterActivation doActivateMeter(Instant generalizedStartDate) {
        Optional<MeterActivation> meterActivation;
        // If the devices current meter activation starts at start, we just have to update this one!
        Optional<MeterActivation> currentMeterActivation = device.getKoreHelper().getCurrentMeterActivation();
        if (currentMeterActivation.isPresent() && generalizedStartDate.isBefore(currentMeterActivation.get().getStart())) {
            throw new MeterActivationTimestampNotAfterLastActivationException(thesaurus, getLongDateFormatForCurrentUser(), generalizedStartDate, currentMeterActivation.get().getStart());
        }
        //validate business constraints
        validateUsagePointIsNotLinkedAlready(usagePoint, generalizedStartDate);
        validateReadingTypeRequirements(usagePoint, generalizedStartDate);

        if (currentMeterActivation.isPresent() && currentMeterActivation.get().getStart().equals(generalizedStartDate)) {
            meterActivation = currentMeterActivation;
        } else {
            meterActivation = Optional.of(getDevice().getMeter().get().getMeterActivation(generalizedStartDate).get());
        }
        if (meterActivation.isPresent() && (!meterActivation.get().getUsagePoint().isPresent() || meterActivation.get().getUsagePoint().get().getId() != this.usagePoint.getId())) {
            meterActivation = Optional.of(endMeterActivationAndRestart(generalizedStartDate, meterActivation, Optional.of(usagePoint)));
        }
        device.getKoreHelper().setCurrentMeterActivation(meterActivation);
        return meterActivation.get();
    }

    private void validateUsagePointIsNotLinkedAlready(UsagePoint usagePoint, Instant from) {
        Optional<MeterActivation> usagePointMeterActivation = usagePoint.getMeterActivations().stream()
                .filter(meterActivation -> meterActivation.getEnd() == null || meterActivation.getEnd().isAfter(from))
                .sorted(Comparator.comparing(MeterActivation::getStart))
                .findFirst();
        if (usagePointMeterActivation.isPresent()) {
            Meter currentMeter = device.getMeter().get();
            Optional<Meter> meterLinkedToUsagePoint = usagePointMeterActivation.get().getMeter();
            if (meterLinkedToUsagePoint.isPresent() && !meterLinkedToUsagePoint.get().equals(currentMeter)) {
                throw new UsagePointAlreadyLinkedToAnotherDeviceException(thesaurus, getLongDateFormatForCurrentUser(), usagePointMeterActivation.get());
            }
        }
    }

    private void validateReadingTypeRequirements(UsagePoint usagePoint, Instant from) {
        Map<MetrologyConfiguration, List<ReadingTypeRequirement>> unsatisfiedRequirements = getUnsatisfiedRequirements(usagePoint, from);
        if (!unsatisfiedRequirements.isEmpty()) {
            throw new UnsatisfiedReadingTypeRequirementsOfUsagePointException(thesaurus, unsatisfiedRequirements);
        }
    }

    Map<MetrologyConfiguration, List<ReadingTypeRequirement>> getUnsatisfiedRequirements(UsagePoint usagePoint, Instant from) {
        List<UsagePointMetrologyConfiguration> effectiveMetrologyConfigurations = usagePoint.getMetrologyConfigurations(Range.atLeast(from));
        if (effectiveMetrologyConfigurations.isEmpty()) {
            return Collections.emptyMap();
        }
        List<ReadingType> supportedReadingTypes = getDeviceCapabilities();
        Map<MetrologyConfiguration, List<ReadingTypeRequirement>> unsatisfiedRequirements = new HashMap<>();
        for (MetrologyConfiguration metrologyConfiguration : effectiveMetrologyConfigurations) {
            List<ReadingTypeRequirement> unsatisfied = metrologyConfiguration.getMandatoryReadingTypeRequirements()
                    .stream()
                    .filter(requirement -> supportedReadingTypes.stream().noneMatch(requirement::matches))
                    .collect(Collectors.toList());
            if (!unsatisfied.isEmpty()) {
                unsatisfiedRequirements.put(metrologyConfiguration, unsatisfied);
            }
        }
        return unsatisfiedRequirements;
    }

    private List<ReadingType> getDeviceCapabilities() {
        return deviceConfigurationService.getReadingTypesRelatedToConfiguration(device.getDeviceConfiguration());
    }

    private DateTimeFormatter getLongDateFormatForCurrentUser() {
        return DateTimeFormatGenerator.getDateFormatForUser(
                DateTimeFormatGenerator.Mode.LONG,
                DateTimeFormatGenerator.Mode.LONG,
                this.userPreferencesService,
                this.threadPrincipalService.getPrincipal());
    }
}
