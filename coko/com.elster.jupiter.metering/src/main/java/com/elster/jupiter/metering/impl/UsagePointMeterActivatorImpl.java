package com.elster.jupiter.metering.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.ConnectionState;
import com.elster.jupiter.metering.CustomUsagePointMeterActivationValidationException;
import com.elster.jupiter.metering.EventType;
import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePointMeterActivator;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.impl.config.SelfObjectValidator;
import com.elster.jupiter.metering.impl.config.SelfValid;
import com.elster.jupiter.metering.impl.config.ServerMetrologyConfigurationService;

import com.google.common.collect.Range;

import javax.inject.Inject;
import javax.validation.ConstraintValidatorContext;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@SelfValid
public class UsagePointMeterActivatorImpl implements UsagePointMeterActivator, SelfObjectValidator {

    private final ServerMetrologyConfigurationService metrologyConfigurationService;
    private final EventService eventService;

    private Instant activationTime;
    private Map<MeterRole, Meter> meterRoleMapping;
    private UsagePointImpl usagePoint;

    @Inject
    public UsagePointMeterActivatorImpl(ServerMetrologyConfigurationService metrologyConfigurationService, EventService eventService) {
        this.meterRoleMapping = new LinkedHashMap<>();
        this.metrologyConfigurationService = metrologyConfigurationService;
        this.eventService = eventService;
    }

    UsagePointMeterActivatorImpl init(UsagePointImpl usagePoint) {
        this.usagePoint = usagePoint;
        this.activationTime = this.usagePoint.getInstallationTime();
        return this;
    }

    @Override
    public UsagePointMeterActivator activate(Meter meter, MeterRole meterRole) {
        if (meter == null || meterRole == null) {
            throw new IllegalArgumentException("Meter and meter role can not be null");
        }
        this.meterRoleMapping.put(meterRole, meter);
        eventService.postEvent(EventType.METER_ACTIVATED.topic(), this.usagePoint);
        return this;
    }

    @Override
    public UsagePointMeterActivator clear(MeterRole meterRole) {
        this.meterRoleMapping.put(meterRole, null);
        return this;
    }

    @Override
    public void complete() {
        // 1 Check usage point state
        Optional<EffectiveMetrologyConfigurationOnUsagePoint> effectiveMetrologyConfiguration = this.usagePoint.getEffectiveMetrologyConfiguration(this.activationTime);
        if (effectiveMetrologyConfiguration.isPresent() && effectiveMetrologyConfiguration.get().isActive()
                || !effectiveMetrologyConfiguration.isPresent() && this.usagePoint.getConnectionState() != ConnectionState.UNDER_CONSTRUCTION) {
            throw UsagePointManageException.incorrectState(this.metrologyConfigurationService.getThesaurus(), this.usagePoint
                    .getMRID());
        }

        // 2 Start validation
        Save.CREATE.validate(this.metrologyConfigurationService.getDataModel(), this);

        // 3 Manage activations
        manageActivations();
    }

    @Override
    public boolean validate(ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();
        boolean result = validateNoMetersOnTheSameUsagePointTwice(context);
        result &= validateMeterOverlaps(context);
        result &= validateMetersCapabilities(context);
        result &= validateByCustomValidators(context);
        return result;
    }

    private boolean validateNoMetersOnTheSameUsagePointTwice(ConstraintValidatorContext context) {
        boolean result = true;
        Map<MeterRole, Meter> fullMeterRoleMapping = new LinkedHashMap<>();
        this.usagePoint.getMeterActivations(this.activationTime).forEach(ma -> {
            if (ma.getMeterRole().isPresent() && !this.meterRoleMapping.containsKey(ma.getMeterRole().get())) {
                fullMeterRoleMapping.put(ma.getMeterRole().get(), ma.getMeter().get());
            }
        });
        fullMeterRoleMapping.putAll(this.meterRoleMapping);
        Set<Meter> uniqueMetersCollector = new HashSet<>(fullMeterRoleMapping.size());
        for (Map.Entry<MeterRole, Meter> mappingEntry : fullMeterRoleMapping.entrySet()) {
            if (mappingEntry.getValue() == null) {
                continue;
            }
            if (!uniqueMetersCollector.add(mappingEntry.getValue())) {
                result = false;
                context.buildConstraintViolationWithTemplate("{" + MessageSeeds.Constants.THE_SAME_METER_ACTIVATED_TWICE_ON_USAGE_POINT + "}")
                        .addPropertyNode(mappingEntry.getKey().getKey()).addConstraintViolation();
            }
        }
        return result;
    }

    private boolean validateMeterOverlaps(ConstraintValidatorContext context) {
        boolean result = true;
        for (Map.Entry<MeterRole, Meter> mappingEntry : this.meterRoleMapping.entrySet()) {
            if (mappingEntry.getValue() == null) {
                continue;
            }
            List<MeterActivation> activations = mappingEntry.getValue().getMeterActivations().stream()
                    .filter(ma -> !ma.getStart().equals(ma.getEnd()))
                    .filter(ma -> ma.isEffectiveAt(this.activationTime) || ma.getRange().lowerEndpoint().isAfter(this.activationTime))
                    .filter(ma -> ma.getUsagePoint().isPresent() && !ma.getUsagePoint().get().equals(this.usagePoint))
                    .collect(Collectors.toList());
            for (MeterActivation activation : activations) {
                result = false;
                String errorMessage = this.metrologyConfigurationService.getThesaurus()
                        .getFormat(MessageSeeds.METER_ALREADY_LINKED_TO_USAGEPOINT)
                        .format(mappingEntry.getValue().getMRID(), activation.getUsagePoint().get().getMRID(), activation.getMeterRole().get().getDisplayName());
                context.buildConstraintViolationWithTemplate(errorMessage).addPropertyNode(mappingEntry.getKey().getKey()).addConstraintViolation();
            }
        }
        return result;
    }

    private boolean validateMetersCapabilities(ConstraintValidatorContext context) {
        boolean result = true;
        Optional<EffectiveMetrologyConfigurationOnUsagePoint> effectiveMetrologyConfiguration = this.usagePoint.getEffectiveMetrologyConfiguration(this.activationTime);
        if (effectiveMetrologyConfiguration.isPresent()) {
            UsagePointMetrologyConfiguration metrologyConfiguration = effectiveMetrologyConfiguration.get().getMetrologyConfiguration();
            List<ReadingTypeRequirement> mandatoryReadingTypeRequirements = metrologyConfiguration.getMandatoryReadingTypeRequirements();
            for (Map.Entry<MeterRole, Meter> mappingEntry : this.meterRoleMapping.entrySet()) {
                if (mappingEntry.getValue() == null) {
                    continue;
                }
                List<ReadingTypeRequirement> unmatchedRequirements = getUnmatchedMeterReadingTypeRequirements(metrologyConfiguration, mandatoryReadingTypeRequirements, mappingEntry);
                if (!unmatchedRequirements.isEmpty()) {
                    result = false;
                    String messageTemplate = this.metrologyConfigurationService.getThesaurus()
                            .getString(MessageSeeds.UNSATISFIED_METROLOGY_REQUIREMENT.getKey(), MessageSeeds.UNSATISFIED_METROLOGY_REQUIREMENT.getDefaultFormat());
                    String errorMessage = MessageFormat.format(messageTemplate, unmatchedRequirements
                            .stream()
                            .map(ReadingTypeRequirement::getDescription)
                            .collect(Collectors.joining(", ")));
                    context.buildConstraintViolationWithTemplate(errorMessage)
                            .addPropertyNode(mappingEntry.getKey().getKey())
                            .addConstraintViolation();
                }
            }
        }
        return result;
    }

    private List<ReadingTypeRequirement> getUnmatchedMeterReadingTypeRequirements(UsagePointMetrologyConfiguration metrologyConfiguration,
                                                                                  List<ReadingTypeRequirement> mandatoryReadingTypeRequirements,
                                                                                  Map.Entry<MeterRole, Meter> mappingEntry) {
        List<ReadingType> readingTypesOnMeter = new ArrayList<>();
        mappingEntry.getValue().getHeadEndInterface()
                .map(headEndInterface -> headEndInterface.getCapabilities(mappingEntry.getValue()))
                .ifPresent(endDeviceCapabilities -> readingTypesOnMeter.addAll(endDeviceCapabilities.getConfiguredReadingTypes()));
        return metrologyConfiguration.getRequirements(mappingEntry.getKey())
                .stream()
                .filter(mandatoryReadingTypeRequirements::contains)
                .filter(requirement -> !readingTypesOnMeter.stream().anyMatch(requirement::matches))
                .collect(Collectors.toList());
    }

    private boolean validateByCustomValidators(ConstraintValidatorContext context) {
        boolean result = true;
        for (Map.Entry<MeterRole, Meter> mappingEntry : this.meterRoleMapping.entrySet()) {
            if (mappingEntry.getValue() == null) {
                continue;
            }
            try {
                this.metrologyConfigurationService.validateUsagePointMeterActivation(mappingEntry.getKey(), mappingEntry.getValue(), this.usagePoint);
            } catch (CustomUsagePointMeterActivationValidationException ex) {
                result = false;
                context.buildConstraintViolationWithTemplate(ex.getLocalizedMessage())
                        .addPropertyNode(mappingEntry.getKey().getKey())
                        .addConstraintViolation();
            }
        }
        return result;
    }

    private void manageActivations() {
        detachUsagePointFromAllAffectedMeterRoles();
        for (Map.Entry<MeterRole, Meter> mappingEntry : this.meterRoleMapping.entrySet()) {
            MeterImpl meter = (MeterImpl) mappingEntry.getValue();
            MeterRole meterRole = mappingEntry.getKey();
            if (meter != null) {
                /* reload meter activations, but without a new one (we need to reload because it is possible that we just detached an usage point a few lines above for that meter) */
                meter.refreshMeterActivations();
                manageActivationsOnMeterForRole(meter, meterRole);
            }
        }
        this.usagePoint.touch();
        this.usagePoint.refreshMeterActivations();
    }

    private void manageActivationsOnMeterForRole(MeterImpl meter, MeterRole meterRole) {
        List<? extends MeterActivation> meterActivations = meter.getMeterActivations();
        // Main point here - we have no gaps between meter activations
        if (!meterActivations.isEmpty()) {
            for (int i = 0; i < meterActivations.size(); i++) {
                MeterActivationImpl meterActivation = (MeterActivationImpl) meterActivations.get(i);
                // if we still have meter activations after that which start before UP installation time, skip this one
                if (!meterActivation.getStart().isAfter(this.activationTime)
                        && i + 1 < meterActivations.size() && !meterActivations.get(i + 1).getStart().isAfter(this.activationTime)) {
                    continue;
                }
                if (meterActivation.getStart().isBefore(this.activationTime)) { // it is not a mistake, exclusive for corner case when start time = UP installation time
                    manageSingleMeterActivationStartedBefore(meter, meterRole, meterActivation, i, meterActivations);
                } else {
                    manageSingleMeterActivationStartedAfterOrAtTheSameTime(meterRole, meterActivation, i, meterActivations);
                }
            }
        } else {
            createNewMeterActivation(meter, meterRole, Range.atLeast(this.activationTime));
        }
    }

    private void detachUsagePointFromAllAffectedMeterRoles() {
        // Close all meter activations for that role which are active at activation time
        this.usagePoint.getMeterActivations()
                .stream()
                .filter(ma -> ma.isEffectiveAt(this.activationTime))
                .filter(ma -> ma.getMeter().isPresent() && ma.getMeterRole().isPresent() && this.meterRoleMapping.containsKey(ma.getMeterRole().get()))
                .forEach(ma -> ((MeterActivationImpl) ma).detachUsagePoint());
    }

    private void manageSingleMeterActivationStartedBefore(MeterImpl meter, MeterRole meterRole, MeterActivationImpl currentMeterActivation, int currentMeterActivationIdx, List<? extends MeterActivation> meterActivations) {
        if (currentMeterActivation.isEffectiveAt(this.activationTime)) {
            Range<Instant> range = currentMeterActivation.getRange().intersection(Range.atLeast(this.activationTime));
            if (currentMeterActivationIdx + 1 >= meterActivations.size()) { // if it is the last meter activation, then we shouldn't set end date for the new meter activation
                range = Range.atLeast(this.activationTime);
            }
            currentMeterActivation.doEndAt(this.activationTime);
            createNewMeterActivation(meter, meterRole, range).moveAllChannelsData(currentMeterActivation, range);
        } else {
            if (currentMeterActivationIdx + 1 < meterActivations.size()) {
                throw new IllegalStateException("Seems that you have gaps between meter activations: meter = " + meter.getId() + ", probably gap after meterActivation = " + currentMeterActivation
                        .getId());
            }
            currentMeterActivation.endAt(this.activationTime);
            createNewMeterActivation(meter, meterRole, Range.atLeast(this.activationTime));
        }
    }

    private MeterActivationImpl createNewMeterActivation(MeterImpl meter, MeterRole meterRole, Range<Instant> range) {
        MeterActivationImpl meterActivation = this.metrologyConfigurationService.getDataModel()
                .getInstance(MeterActivationImpl.class)
                .init(meter, meterRole, this.usagePoint, range);
        meterActivation.save();
        return meterActivation;
    }

    private void manageSingleMeterActivationStartedAfterOrAtTheSameTime(MeterRole meterRole, MeterActivationImpl currentMeterActivation, int currentMeterActivationIdx, List<? extends MeterActivation> meterActivations) {
        if (currentMeterActivationIdx == 0 && currentMeterActivation.getStart().isAfter(this.activationTime)) {
            // if it is the first meter activation and it is in future, then update the start date
            currentMeterActivation.advanceStartDate(this.activationTime);
        }
        currentMeterActivation.doSetUsagePoint(this.usagePoint);
        currentMeterActivation.doSetMeterRole(meterRole);
        if (currentMeterActivationIdx + 1 >= meterActivations.size()) { // if it is the last meter activation, then remove the end time
            currentMeterActivation.doEndAt(null);
        } else {
            currentMeterActivation.save(); // doEndAt call the save method inside
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UsagePointMeterActivatorImpl that = (UsagePointMeterActivatorImpl) o;
        return usagePoint.equals(that.usagePoint);
    }

    @Override
    public int hashCode() {
        return this.usagePoint.hashCode();
    }
}
