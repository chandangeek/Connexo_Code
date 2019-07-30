/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.cps.rest.CustomPropertySetInfo;
import com.elster.jupiter.cps.rest.CustomPropertySetInfoFactory;
import com.elster.jupiter.estimation.EstimationRule;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingQualityComment;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointManagementException;
import com.elster.jupiter.metering.UsagePointMeterActivationException;
import com.elster.jupiter.metering.UsagePointMeterActivator;
import com.elster.jupiter.metering.UsagePointPropertySet;
import com.elster.jupiter.metering.ami.EndDeviceCapabilities;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.ConcurrentModificationException;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.usagepoint.lifecycle.UsagePointLifeCycleService;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycleConfigurationService;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointTransition;
import com.elster.jupiter.users.PreferenceType;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationService;

import com.google.common.collect.Range;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ResourceHelper {

    private final MeteringService meteringService;
    private final MeteringGroupsService meteringGroupsService;
    private final ExceptionFactory exceptionFactory;
    private final ConcurrentModificationExceptionFactory conflictFactory;
    private final MetrologyConfigurationService metrologyConfigurationService;
    private final UsagePointLifeCycleService usagePointLifeCycleService;
    private final Clock clock;
    private final UsagePointLifeCycleConfigurationService usagePointLifeCycleConfigurationService;
    private final ValidationService validationService;
    private final EstimationService estimationService;
    private final Thesaurus thesaurus;
    private final UserService userService;
    private final ThreadPrincipalService threadPrincipalService;
    private final CustomPropertySetInfoFactory customPropertySetInfoFactory;

    @Inject
    public ResourceHelper(MeteringService meteringService, MeteringGroupsService meteringGroupsService, ExceptionFactory exceptionFactory,
                          ConcurrentModificationExceptionFactory conflictFactory, MetrologyConfigurationService metrologyConfigurationService,
                          UsagePointLifeCycleService usagePointLifeCycleService, Clock clock, UsagePointLifeCycleConfigurationService usagePointLifeCycleConfigurationService,
                          ValidationService validationService, EstimationService estimationService, Thesaurus thesaurus, UserService userService,
                          ThreadPrincipalService threadPrincipalService, CustomPropertySetInfoFactory customPropertySetInfoFactory) {
        super();
        this.meteringService = meteringService;
        this.meteringGroupsService = meteringGroupsService;
        this.exceptionFactory = exceptionFactory;
        this.conflictFactory = conflictFactory;
        this.metrologyConfigurationService = metrologyConfigurationService;
        this.usagePointLifeCycleService = usagePointLifeCycleService;
        this.clock = clock;
        this.usagePointLifeCycleConfigurationService = usagePointLifeCycleConfigurationService;
        this.validationService = validationService;
        this.estimationService = estimationService;
        this.thesaurus = thesaurus;
        this.userService = userService;
        this.threadPrincipalService = threadPrincipalService;
        this.customPropertySetInfoFactory = customPropertySetInfoFactory;
    }

    public MeterRole findMeterRoleOrThrowException(String key) {
        return metrologyConfigurationService.findMeterRole(key)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_METER_ROLE_FOR_KEY, key));
    }

    public Meter findMeterByNameOrThrowException(String name) {
        return meteringService.findMeterByName(name)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_DEVICE_WITH_NAME, name));
    }

    public Optional<UsagePoint> findUsagePointByName(String name) {
        return meteringService.findUsagePointByName(name);
    }

    public UsagePoint findUsagePointByNameOrThrowException(String name) {
        return meteringService.findUsagePointByName(name)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_USAGE_POINT_WITH_NAME, name));
    }

    public UsagePoint findUsagePointByMRIDOrThrowException(String mrid) {
        return meteringService.findUsagePointByMRID(mrid)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_USAGE_POINT_WITH_MRID, mrid));
    }

    public UsagePoint findUsagePointByIdOrThrowException(long id) {
        return meteringService.findUsagePointById(id)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_USAGE_POINT_FOR_ID, id));
    }

    public UsagePoint findAndLockUsagePointByNameOrThrowException(String name, long version) {
        UsagePoint up = findUsagePointByNameOrThrowException(name);
        return lockUsagePointOrThrowException(up.getId(), version, up.getName());
    }

    public EffectiveMetrologyConfigurationOnUsagePoint findEffectiveMetrologyConfigurationByUsagePointOrThrowException(UsagePoint usagePoint) {
        return usagePoint.getCurrentEffectiveMetrologyConfiguration()
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_METROLOGYCONFIG_FOR_USAGEPOINT, usagePoint.getName()));
    }

    public UsagePointMetrologyConfiguration findActiveUsagePointMetrologyConfigurationOrThrowException(long id) {
        return metrologyConfigurationService
                .findMetrologyConfiguration(id)
                .filter(metrologyConfiguration -> metrologyConfiguration instanceof UsagePointMetrologyConfiguration)
                .map(this::isActiveMetrologyConfigurationOrThrowException)
                .map(UsagePointMetrologyConfiguration.class::cast)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_METROLOGYCONFIG_FOR_ID, id));
    }

    private MetrologyConfiguration isActiveMetrologyConfigurationOrThrowException(MetrologyConfiguration metrologyConfiguration) {
        if (metrologyConfiguration.isActive()) {
            return metrologyConfiguration;
        }
        throw exceptionFactory.newException(MessageSeeds.NOT_POSSIBLE_TO_LINK_INACTIVE_METROLOGY_CONFIGURATION_TO_USAGE_POINT, metrologyConfiguration.getName());
    }

    public UsagePoint lockUsagePointOrThrowException(UsagePointInfo info) {
        return lockUsagePointOrThrowException(info.id, info.version, info.name);
    }

    public UsagePoint lockUsagePointOrThrowException(long id, long version, String name) {
        return meteringService.findAndLockUsagePointByIdAndVersion(id, version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(name)
                        .withActualVersion(() -> meteringService.findUsagePointById(id).map(UsagePoint::getVersion).orElse(null))
                        .supplier());
    }

    public ConcurrentModificationException usagePointAlreadyLinkedException(String name) {
        return conflictFactory.conflict()
                .withMessageBody(MessageSeeds.USAGE_POINT_LINKED_EXCEPTION_MSG, name)
                .withMessageTitle(MessageSeeds.USAGE_POINT_LINKED_EXCEPTION, name)
                .build();
    }

    public Optional<MetrologyPurpose> findMetrologyPurpose(long id) {
        return metrologyConfigurationService.findMetrologyPurpose(id);
    }

    public MetrologyPurpose findMetrologyPurposeOrThrowException(long id) {
        return metrologyConfigurationService.findMetrologyPurpose(id)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_METROLOGY_PURPOSE, id));
    }

    public MetrologyContract findMetrologyContractOrThrowException(EffectiveMetrologyConfigurationOnUsagePoint effectiveMC, long contractId) {
        return effectiveMC.getMetrologyConfiguration().getContracts().stream()
                .filter(contract -> contract.getId() == contractId)
                .findAny()
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.METROLOGYCONTRACT_IS_NOT_LINKED_TO_USAGEPOINT, contractId, effectiveMC.getUsagePoint().getName()));
    }

    public MetrologyContract findMetrologyContractOrThrowException(EffectiveMetrologyConfigurationOnUsagePoint effectiveMC, MetrologyPurpose purpose) {
        return effectiveMC.getMetrologyConfiguration().getContracts().stream()
                .filter(contract -> contract.getMetrologyPurpose().equals(purpose))
                .findAny()
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.METROLOGYPURPOSE_IS_NOT_FOUND_ON_USAGEPOINT, purpose.getName(), effectiveMC.getUsagePoint().getName()));
    }

    public Optional<MetrologyContract> findMetrologyContract(EffectiveMetrologyConfigurationOnUsagePoint effectiveMC, MetrologyPurpose purpose) {
        return effectiveMC.getMetrologyConfiguration().getContracts().stream()
                .filter(contract -> contract.getMetrologyPurpose().equals(purpose))
                .findAny();
    }

    public MetrologyContract findInactiveMetrologyContractOrThrowException(EffectiveMetrologyConfigurationOnUsagePoint effectiveMC, long contractId) {
        MetrologyContract metrologyContract = effectiveMC.getMetrologyConfiguration().getContracts()
                .stream()
                .filter(mc -> !mc.getDeliverables().isEmpty())
                .filter(mc -> mc.getId() == contractId)
                .filter(mc -> !mc.isMandatory())
                .findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.CANNOT_ACTIVATE_METROLOGY_PURPOSE, contractId));

        if (effectiveMC.getChannelsContainer(metrologyContract, clock.instant()).isPresent()) {
            throw conflictFactory.contextDependentConflictOn(metrologyContract.getMetrologyPurpose().getName()).build();
        }

        return metrologyContract;
    }

    public UsagePointTransition findUsagePointTransitionOrThrowException(long transitionId) {
        return usagePointLifeCycleConfigurationService.findUsagePointTransition(transitionId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_USAGEPOINT_TRANSITION_WITH_ID, transitionId));
    }

    public MetrologyConfiguration findMetrologyConfigurationOrThrowException(long metrologyConfigurationId) {
        return metrologyConfigurationService.findMetrologyConfiguration(metrologyConfigurationId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_METROLOGYCONFIG_FOR_ID, metrologyConfigurationId));
    }

    public ReadingTypeDeliverable findReadingTypeDeliverableOrThrowException(MetrologyContract metrologyContract, long outputId, String usagePointName) {
        return metrologyContract.getDeliverables().stream()
                .filter(deliverable -> deliverable.getId() == outputId)
                .findAny()
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_OUTPUT_FOR_USAGEPOINT, usagePointName, outputId));
    }

    public UsagePointGroup findUsagePointGroupOrThrowException(long id) {
        return meteringGroupsService.findUsagePointGroup(id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    public ValidationRule findValidationRuleOrThrowException(long id) {
        return validationService.findValidationRule(id)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_VALIDATION_RULE, id));
    }

    public EstimationRule findEstimationRuleOrThrowException(long id) {
        return estimationService.getEstimationRule(id)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_ESTIMATION_RULE, id));
    }

    public UsagePointGroup lockUsagePointGroupOrThrowException(UsagePointGroupInfo info) {
        return meteringGroupsService.findAndLockUsagePointGroupByIdAndVersion(info.id, info.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(info.name)
                        .withActualVersion(() -> meteringGroupsService.findUsagePointGroup(info.id)
                                .map(UsagePointGroup::getVersion)
                                .orElse(null))
                        .supplier());
    }

    public List<MetrologyConfigurationInfo> getAvailableMetrologyConfigurations(UsagePoint usagePoint, CustomPropertySetInfoFactory customPropertySetInfoFactory) {
        return metrologyConfigurationService
                .findLinkableMetrologyConfigurations(usagePoint)
                .stream()
                .filter(mc -> !mc.getCustomPropertySets().stream().anyMatch(cas -> !cas.isEditableByCurrentUser()))
                .map(mc -> new MetrologyConfigurationInfo(mc, mc.getCustomPropertySets()
                        .stream()
                        .sorted(Comparator.comparing(rcps -> rcps.getCustomPropertySet()
                                .getName(), String.CASE_INSENSITIVE_ORDER))
                        .map(customPropertySetInfoFactory::getGeneralAndPropertiesInfo)
                        .collect(Collectors.toList())))
                .collect(Collectors.toList());
    }

    public void activateMeters(UsagePointInfo info, UsagePoint usagePoint) {
        if (info.metrologyConfiguration != null && info.metrologyConfiguration.meterRoles != null && !info.metrologyConfiguration.meterRoles.isEmpty()) {
            UsagePointMeterActivator linker = usagePoint.linkMeters();

            info.metrologyConfiguration.meterRoles
                    .stream()
                    .filter(meterRoleInfo -> !Checks.is(meterRoleInfo.id).emptyOrOnlyWhiteSpace() && !Checks.is(meterRoleInfo.meter).emptyOrOnlyWhiteSpace())
                    .forEach(meterRoleInfo -> {
                        MeterRole meterRole = findMeterRoleOrThrowException(meterRoleInfo.id);
                        linker.clear(meterRole);
                        if (meterRoleInfo.meter != null && !Checks.is(meterRoleInfo.name).emptyOrOnlyWhiteSpace()) {
                            Meter meter = findMeterByNameOrThrowException(meterRoleInfo.meter);
                            validateMeterCapabilities(info.metrologyConfiguration, meter, meterRoleInfo.activationTime);
                            linker.activate(meterRoleInfo.activationTime, meter, meterRole);
                        }
                    });
            linker.complete();
        }
    }

    private void validateMeterCapabilities(MetrologyConfigurationInfo info, Meter meter, Instant start) {
        MetrologyConfiguration metrologyConfiguration = findMetrologyConfigurationOrThrowException(info.id);
        List<? extends MeterActivation> meterActivations = meter.getMeterActivations(Range.atLeast(start));
        List<MetrologyContract> metrologyContracts = metrologyConfiguration.getContracts().stream()
                .filter(MetrologyContract::isMandatory)
                .collect(Collectors.toList());
        List<ReadingType> meterActivationReadingTypes = meterActivations.stream()
                .flatMap(meterActivation -> meterActivation.getReadingTypes().stream())
                .collect(Collectors.toList());

        if (!meterActivationReadingTypes.isEmpty()) {
            List<String> metrologyPurposes = metrologyContracts.stream()
                    .filter(MetrologyContract::isMandatory)
                    .filter(contract -> contract.getRequirements()
                            .stream()
                            .noneMatch(requirement -> meterActivationReadingTypes.stream()
                                    .anyMatch(requirement::matches)))
                    .map(MetrologyContract::getMetrologyPurpose)
                    .map(MetrologyPurpose::getName)
                    .collect(Collectors.toList());

            if (!metrologyPurposes.isEmpty()) {
                throw UsagePointManagementException.incorrectMeterActivationRequirements(thesaurus, metrologyPurposes);
            }
        }
    }

    public void performMeterActivations(UsagePointInfo info, UsagePoint usagePoint) {
        UsagePointMeterActivator linker = usagePoint.linkMeters();
        if (info.meterActivations != null && !info.meterActivations.isEmpty()) {
            info.meterActivations
                    .stream()
                    .filter(meterActivation -> meterActivation.meterRole != null && !Checks.is(meterActivation.meterRole.id)
                            .emptyOrOnlyWhiteSpace())
                    .forEach(meterActivation -> {
                        Instant activationTime = meterActivation.meterRole.activationTime;
                        MeterRole meterRole = findMeterRoleOrThrowException(meterActivation.meterRole.id);
                        if (meterActivation.meter == null && !usagePoint.getMeterActivations().isEmpty()) {
                            validateUnlinkMeters(usagePoint, meterRole);
                            linker.clear(meterRole);
                        } else if (meterActivation.meter != null && !Checks.is(meterActivation.meter.name)
                                .emptyOrOnlyWhiteSpace()) {
                            replaceOrActivateMeter(linker, activationTime, meterActivation.meter.name, meterRole);
                        }
                    });
        } else {
            usagePoint.getMeterActivations().forEach(meterActivation -> meterActivation.getMeterRole()
                    .ifPresent(meterRole -> {
                        validateUnlinkMeters(usagePoint, meterRole);
                        linker.clear(meterRole);
                    }));
        }
        linker.completeRemoveOrAdd();
    }

    private void replaceOrActivateMeter(UsagePointMeterActivator linker, Instant activationTime, String meterName, MeterRole meterRole) {
        Meter meter = findMeterByNameOrThrowException(meterName);
        linker.clear(meterRole);
        linker.activate(activationTime, meter, meterRole);
    }

    private void validateUnlinkMeters(UsagePoint usagePoint, MeterRole meterRole) {
        Optional<EffectiveMetrologyConfigurationOnUsagePoint> mc = usagePoint.getCurrentEffectiveMetrologyConfiguration();
        if (mc.isPresent() && !mc.get().getMetrologyConfiguration().areGapsAllowed()) {
            EffectiveMetrologyConfigurationOnUsagePoint metrologyConfiguration = mc.get();
            List<ReadingTypeRequirement> requirementsForMeterRole = metrologyConfiguration.getMetrologyConfiguration().getRequirements(meterRole)
                    .stream()
                    .collect(Collectors.toList());
            List<ReadingTypeRequirement> allRequirements = metrologyConfiguration.getMetrologyConfiguration().getContracts()
                    .stream()
                    .filter(metrologyContract -> metrologyConfiguration.getChannelsContainer(metrologyContract).isPresent())
                    .flatMap(metrologyContract -> metrologyContract.getRequirements().stream())
                    .filter(requirementsForMeterRole::contains)
                    .distinct()
                    .collect(Collectors.toList());
            usagePoint.getMeterActivations()
                    .stream()
                    .filter(meterActivation -> allRequirements
                        .stream()
                        .anyMatch(readingTypeRequirement -> !readingTypeRequirement.getMatchesFor(meterActivation.getChannelsContainer()).isEmpty()))
                    .findAny()
                    .ifPresent(meterActivation -> {
                        DateTimeFormatter dateTimeFormatter = userService.getUserPreferencesService().getDateTimeFormatter(threadPrincipalService.getPrincipal(), PreferenceType.LONG_DATE, PreferenceType.LONG_TIME);
                            throw new UsagePointMeterActivationException.MeterCannotBeUnlinked(
                                    thesaurus,
                                    meterActivation.getMeter().get().getName(),
                                    usagePoint.getName(),
                                    dateTimeFormatter.format(LocalDateTime.ofInstant(clock.instant(), ZoneId.systemDefault())));
                    });

        }
    }

    public void persistCustomProperties(UsagePoint usagePoint, CustomPropertySetInfo customPropertySetInfo) {
        UsagePointPropertySet propertySet = usagePoint.forCustomProperties()
                .getPropertySet(customPropertySetInfo.id);
        propertySet.setValues(customPropertySetInfoFactory.getCustomPropertySetValues(customPropertySetInfo,
                propertySet.getCustomPropertySet().getPropertySpecs()));
    }

    public List<UsagePointTransition> getAvailableTransitions(UsagePoint usagePoint) {
        return usagePointLifeCycleService.getAvailableTransitions(usagePoint, "INS");
    }

    public Optional<ReadingQualityComment> getReadingQualityComment(long id) {
        return meteringService.findReadingQualityComment(id);
    }

    public void checkMeterRequirements(UsagePoint usagePoint, MetrologyContract metrologyContract) {
        List<MeterActivation> meterActivations = usagePoint.getMeterActivations(clock.instant());
        EffectiveMetrologyConfigurationOnUsagePoint metrologyConfigurationOnUsagePoint = usagePoint.getCurrentEffectiveMetrologyConfiguration()
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_METROLOGYCONFIG_FOR_USAGEPOINT, usagePoint.getName()));
        List<MeterRole> meterRolesOfMetrologyConfiguration = metrologyConfigurationOnUsagePoint.getMetrologyConfiguration().getMeterRoles();
        List<Pair<MeterRole, Meter>> metersInRoles = meterActivations.stream()
                .filter(meterActivation -> meterActivation.getMeterRole().map(meterRolesOfMetrologyConfiguration::contains).orElse(false))
                .map(meterActivation -> Pair.of(meterActivation.getMeterRole().get(), meterActivation.getMeter().get()))
                .collect(Collectors.toList());
        for (Pair<MeterRole, Meter> pair : metersInRoles) {
            MeterRole meterRole = pair.getFirst();
            Meter meter = pair.getLast();
            Set<ReadingTypeRequirement> requirements = metrologyContract.getRequirements().stream()
                    .filter(readingTypeRequirement -> meterRole.equals(metrologyConfigurationOnUsagePoint.getMetrologyConfiguration()
                            .getMeterRoleFor(readingTypeRequirement)
                            .orElse(null)))
                    .collect(Collectors.toSet());
            Set<ReadingTypeRequirement> unsatisfiedRequirements = getUnsatisfiedReadingTypeRequirementsOfMeter(requirements, meter);
            if (!unsatisfiedRequirements.isEmpty()) {
                throw exceptionFactory.newException(MessageSeeds.UNSATISFIED_READING_TYPE_REQUIREMENTS);
            }
        }
    }

    private Set<ReadingTypeRequirement> getUnsatisfiedReadingTypeRequirementsOfMeter(Set<ReadingTypeRequirement> requirements, Meter meter) {
        List<ReadingType> meterProvidedReadingTypes = meter.getHeadEndInterface()
                .map(headEndInterface -> headEndInterface.getCapabilities(meter))
                .map(EndDeviceCapabilities::getConfiguredReadingTypes)
                .orElse(Collections.emptyList());
        return requirements.stream()
                .filter(requirement -> !meterProvidedReadingTypes.stream().anyMatch(requirement::matches))
                .collect(Collectors.toSet());
    }

    public IdWithNameInfo getApplicationInfo(QualityCodeSystem system) {
        switch (system) {
            case MDC:
                return new IdWithNameInfo(system.name(), "MultiSense");
            case MDM:
                return new IdWithNameInfo(system.name(), "Insight");
            default:
                return new IdWithNameInfo(system.name(), system.name());
        }
    }

    public List<MeterRole> getMeterRoles() {
        return meteringService.getMeterRoles();
    }

    public String formatDate(Instant date) {
        DateTimeFormatter dateTimeFormatter = userService.getUserPreferencesService()
                .getDateTimeFormatter(threadPrincipalService.getPrincipal(), PreferenceType.LONG_DATE, PreferenceType.LONG_TIME);
        return dateTimeFormatter.format(LocalDateTime.ofInstant(date, ZoneId.systemDefault()));
    }

    public String formatDate(ZonedDateTime date) {
        DateTimeFormatter dateTimeFormatter = userService.getUserPreferencesService()
                .getDateTimeFormatter(threadPrincipalService.getPrincipal(), PreferenceType.LONG_DATE, PreferenceType.LONG_TIME);
        return dateTimeFormatter.format(date);
    }


    public ChannelsContainer findChannelsContainerOrThrowException(EffectiveMetrologyConfigurationOnUsagePoint effectiveMC, MetrologyContract metrologyContract) {
        return effectiveMC.getChannelsContainer(metrologyContract)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.METROLOGY_CONTRACT_NOT_LINKED_TO_CHANNELS_CONTAINER, metrologyContract.getId()));
    }

}
