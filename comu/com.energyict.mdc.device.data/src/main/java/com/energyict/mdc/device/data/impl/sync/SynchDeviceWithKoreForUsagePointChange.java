package com.energyict.mdc.device.data.impl.sync;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.UserPreferencesService;
import com.energyict.mdc.common.DateTimeFormatGenerator;
import com.energyict.mdc.device.data.exceptions.MeterActivationTimestampNotAfterLastActivationException;
import com.energyict.mdc.device.data.exceptions.UnsatisfiedReadingTypeRequirementsOfUsagePointException;
import com.energyict.mdc.device.data.exceptions.UsagePointAlreadyLinkedToAnotherDeviceException;
import com.energyict.mdc.device.data.impl.DeviceImpl;
import com.energyict.mdc.device.data.impl.ServerDevice;
import com.energyict.mdc.device.data.impl.ServerDeviceService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Additional behaviour related to 'Kore' objects when the Device's UsagePoint is changed
 */
public class SynchDeviceWithKoreForUsagePointChange extends AbstractSyncDeviceWithKoreMeter {

    private ServerDevice device;
    private UsagePoint usagePoint;
    private final Thesaurus thesaurus;
    private final UserPreferencesService userPreferencesService;
    private final ThreadPrincipalService threadPrincipalService;
    private final boolean forceNew;

    public SynchDeviceWithKoreForUsagePointChange(ServerDevice device, Instant start, UsagePoint usagePoint, ServerDeviceService deviceService, MdcReadingTypeUtilService readingTypeUtilService, Thesaurus thesaurus, UserPreferencesService userPreferencesService, ThreadPrincipalService threadPrincipalService, EventService eventService) {
        this(device, start, usagePoint, deviceService, readingTypeUtilService, thesaurus, userPreferencesService, threadPrincipalService, eventService, false);
    }

    public SynchDeviceWithKoreForUsagePointChange(ServerDevice device, Instant start, UsagePoint usagePoint, ServerDeviceService deviceService, MdcReadingTypeUtilService readingTypeUtilService, Thesaurus thesaurus, UserPreferencesService userPreferencesService, ThreadPrincipalService threadPrincipalService, EventService eventService, boolean force) {
        super(deviceService, readingTypeUtilService, eventService, (start == null ? device.getKoreHelper()
                .getCurrentMeterActivation()
                .get()
                .getStart() : start));
        this.device = device;
        this.usagePoint = usagePoint;
        this.thesaurus = thesaurus;
        this.userPreferencesService = userPreferencesService;
        this.threadPrincipalService = threadPrincipalService;
        this.forceNew = force;
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
        if (currentMeterActivation.isPresent() && generalizedStartDate.isBefore(currentMeterActivation.get()
                .getStart())) {
            throw new MeterActivationTimestampNotAfterLastActivationException(thesaurus, getLongDateFormatForCurrentUser(), generalizedStartDate, currentMeterActivation
                    .get()
                    .getStart());
        }
        //validate business constraints
        validateUsagePointIsNotLinkedAlready(usagePoint, generalizedStartDate);
        validateReadingTypeRequirements(usagePoint, generalizedStartDate);

        if (currentMeterActivation.isPresent() && currentMeterActivation.get()
                .getStart()
                .equals(generalizedStartDate)) {
            meterActivation = currentMeterActivation;
        } else {
            meterActivation = Optional.of(getDevice().getMeter().get().getMeterActivation(generalizedStartDate).get());
        }
        if ((meterActivation.isPresent() && (!meterActivation.get().getUsagePoint().isPresent() || meterActivation.get()
                .getUsagePoint()
                .get()
                .getId() != this.usagePoint.getId())) || forceNew) {
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
                throw new UsagePointAlreadyLinkedToAnotherDeviceException(thesaurus, getLongDateFormatForCurrentUser(), usagePointMeterActivation
                        .get());
            }
        }
    }

    private void validateReadingTypeRequirements(UsagePoint usagePoint, Instant from) {
        Map<MetrologyConfiguration, List<ReadingTypeRequirement>> unsatisfiedRequirements = device.getUnsatisfiedRequirements(usagePoint, from, getDevice()
                .getDeviceConfiguration());
        if (!unsatisfiedRequirements.isEmpty()) {
            throw new UnsatisfiedReadingTypeRequirementsOfUsagePointException(thesaurus, unsatisfiedRequirements);
        }
    }

    private DateTimeFormatter getLongDateFormatForCurrentUser() {
        return DateTimeFormatGenerator.getDateFormatForUser(
                DateTimeFormatGenerator.Mode.LONG,
                DateTimeFormatGenerator.Mode.LONG,
                this.userPreferencesService,
                this.threadPrincipalService.getPrincipal());
    }
}
