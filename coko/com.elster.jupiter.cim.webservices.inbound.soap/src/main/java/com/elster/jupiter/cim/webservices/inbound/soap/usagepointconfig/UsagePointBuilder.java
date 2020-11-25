/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cim.webservices.inbound.soap.usagepointconfig;

import com.elster.jupiter.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.elster.jupiter.cim.webservices.inbound.soap.impl.ReplyTypeFactory;
import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.OverlapCalculatorBuilder;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.cps.ValuesRangeConflict;
import com.elster.jupiter.cps.ValuesRangeConflictType;
import com.elster.jupiter.metering.ConnectionState;
import com.elster.jupiter.metering.ElectricityDetail;
import com.elster.jupiter.metering.ElectricityDetailBuilder;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePointConnectionState;
import com.elster.jupiter.metering.ami.EndDeviceCapabilities;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.TransactionRequired;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.usagepoint.lifecycle.UsagePointLifeCycleService;
import com.elster.jupiter.usagepoint.lifecycle.UsagePointStateChangeFail;
import com.elster.jupiter.usagepoint.lifecycle.config.DefaultState;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycle;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointTransition;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.time.DefaultDateTimeFormatters;
import com.elster.jupiter.util.units.Quantity;

import ch.iec.tc57._2011.executeusagepointconfig.FaultMessage;
import ch.iec.tc57._2011.schema.message.ErrorType;
import ch.iec.tc57._2011.usagepointconfig.ConfigurationEvent;
import ch.iec.tc57._2011.usagepointconfig.Name;
import ch.iec.tc57._2011.usagepointconfig.PhaseCode;
import ch.iec.tc57._2011.usagepointconfig.ServiceCategory;
import ch.iec.tc57._2011.usagepointconfig.ServiceKind;
import ch.iec.tc57._2011.usagepointconfig.Status;
import ch.iec.tc57._2011.usagepointconfig.UsagePoint;
import ch.iec.tc57._2011.usagepointconfig.UsagePointConnectedKind;
import com.elster.connexo._2017.schema.customattributes.Attribute;
import com.elster.connexo._2017.schema.customattributes.CustomAttributeSet;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;

import javax.inject.Inject;
import javax.validation.ConstraintViolationException;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class UsagePointBuilder {
    public static final String USAGE_POINT_NAME = "UsagePointName";
    private static final String CONNECTION_STATE_PROPERTY_NAME = "set.connection.state.property.name";
    private static final String METROLOGY_CONFIGURATION = "MetrologyConfiguration";
    private static final String STATUS_CHANGE = "Change Status";
    private static final String ACTIVE_PURPOSE = "Purpose Active";
    private static final String INACTIVE_PURPOSE = "Purpose Inactive";
    private static final String CHANGE_LIFECYCLE = "Change Lifecycle";
    private final Thesaurus thesaurus;
    private final ReplyTypeFactory replyTypeFactory;
    private final UsagePointConfigFaultMessageFactory messageFactory;
    private final MeteringService meteringService;
    private final MetrologyConfigurationService metrologyConfigurationService;
    private final Clock clock;
    private final UsagePointLifeCycleService usagePointLifeCycleService;
    private final CustomPropertySetService customPropertySetService;

    private UsagePoint usagePointConfig;
    private String pathToUsagePoint;
    private Instant requestTimestamp;
    private MessageSeeds basicFaultMessage;

    @Inject
    UsagePointBuilder(Thesaurus thesaurus, ReplyTypeFactory replyTypeFactory,
                      UsagePointConfigFaultMessageFactory messageFactory, MeteringService meteringService,
                      MetrologyConfigurationService metrologyConfigurationService, Clock clock,
                      UsagePointLifeCycleService usagePointLifeCycleService, CustomPropertySetService customPropertySetService) {
        this.thesaurus = thesaurus;
        this.replyTypeFactory = replyTypeFactory;
        this.messageFactory = messageFactory;
        this.meteringService = meteringService;
        this.metrologyConfigurationService = metrologyConfigurationService;
        this.clock = clock;
        this.usagePointLifeCycleService = usagePointLifeCycleService;
        this.customPropertySetService = customPropertySetService;
    }

    private static void updateConnectionStateIfNeeded(com.elster.jupiter.metering.UsagePoint usagePoint,
                                                      ConnectionState connectionState) {
        if (!usagePoint.getCurrentConnectionState().map(UsagePointConnectionState::getConnectionState)
                .filter(connectionState::equals).isPresent()) {
            usagePoint.setConnectionState(connectionState);
        }
    }

    private static ElectricityDetailBuilder cloneDetail(ElectricityDetail detail,
                                                        com.elster.jupiter.metering.UsagePoint usagePoint, Instant timestamp) {
        return usagePoint.newElectricityDetailBuilder(timestamp).withCollar(detail.isCollarInstalled())
                .withGrounded(detail.isGrounded()).withNominalServiceVoltage(detail.getNominalServiceVoltage())
                .withPhaseCode(detail.getPhaseCode()).withRatedCurrent(detail.getRatedCurrent())
                .withRatedPower(detail.getRatedPower()).withEstimatedLoad(detail.getEstimatedLoad())
                .withLimiter(detail.isLimiter()).withLoadLimiterType(detail.getLoadLimiterType())
                .withLoadLimit(detail.getLoadLimit()).withInterruptible(detail.isInterruptible());
    }

    private static boolean containsStateWithKey(UsagePointLifeCycle lifeCycle, String stateKey) {
        return lifeCycle.getStates().stream().anyMatch(state -> state.getName().equals(stateKey));
    }

    public PreparedUsagePointBuilder from(UsagePoint usagePointConfig, int usagePointIndex) {
        this.usagePointConfig = usagePointConfig;
        pathToUsagePoint = "UsagePointConfig.UsagePoint[" + usagePointIndex + "]";
        return new PreparedUsagePointBuilder() {
            @Override
            public PreparedUsagePointBuilder at(Instant timestamp) {
                UsagePointBuilder.this.at(timestamp);
                return this;
            }

            @Override
            public com.elster.jupiter.metering.UsagePoint create() throws FaultMessage {
                basicFaultMessage = MessageSeeds.UNABLE_TO_CREATE_USAGE_POINT;
                try {
                    return UsagePointBuilder.this.create();
                } catch (ConstraintViolationException constraintViolationException) {
                    throw messageFactory.usagePointConfigFaultMessageSupplier(basicFaultMessage, MessageSeeds.DUPLICATE_USAGE_POINT_NAME).get();
                }
            }

            @Override
            public com.elster.jupiter.metering.UsagePoint update() throws FaultMessage {
                basicFaultMessage = MessageSeeds.UNABLE_TO_UPDATE_USAGE_POINT;
                return UsagePointBuilder.this.update();
            }

            @Override
            public com.elster.jupiter.metering.UsagePoint get() throws FaultMessage {
                basicFaultMessage = MessageSeeds.UNABLE_TO_GET_USAGE_POINT;
                return UsagePointBuilder.this.get();
            }
        };
    }

    UsagePointBuilder at(Instant timestamp) {
        requestTimestamp = timestamp;
        return this;
    }

    private com.elster.jupiter.metering.UsagePoint create() throws FaultMessage {
        Instant creationDate = retrieveCreationDate(usagePointConfig)
                .orElseGet(() -> requestTimestamp == null ? clock.instant() : requestTimestamp);
        com.elster.jupiter.metering.ServiceCategory serviceCategory = retrieveServiceCategory(usagePointConfig);
        com.elster.jupiter.metering.UsagePointBuilder builder = serviceCategory
                .newUsagePoint(retrieveMandatoryName(usagePointConfig), creationDate)
                .withIsSdp(retrieveMandatoryParameter("isSdp", usagePointConfig::isIsSdp))
                .withIsVirtual(retrieveMandatoryParameter("isVirtual", usagePointConfig::isIsVirtual))
                .withLifeCycle(retrieveOptionalLifeCycle(usagePointConfig));

        String mRID = usagePointConfig.getMRID();
        if (mRID != null) {
            // no API to create usage point with a given UUID
        }
        com.elster.jupiter.metering.UsagePoint usagePoint = builder.create();

        switch (serviceCategory.getKind()) {
            case ELECTRICITY:
                ElectricityDetailBuilder detailBuilder = usagePoint.newElectricityDetailBuilder(creationDate);
                retrievePhaseCode(usagePointConfig).ifPresent(detailBuilder::withPhaseCode);
                detailBuilder.create();
                break;
            case GAS:
                usagePoint.newGasDetailBuilder(creationDate).create();
                break;
            case WATER:
                usagePoint.newWaterDetailBuilder(creationDate).create();
                break;
            case HEAT:
                usagePoint.newHeatDetailBuilder(creationDate).create();
                break;
            default:
                usagePoint.newDefaultDetailBuilder(creationDate).create();
        }

        Optional<EffectiveMetrologyConfigurationOnUsagePoint> effectiveMC = usagePoint
                .getCurrentEffectiveMetrologyConfiguration();
        retrieveMetrologyConfiguration(usagePointConfig, usagePoint.getCurrentMeterActivations(), effectiveMC)
                .ifPresent(mc -> usagePoint.apply(mc, creationDate));
        retrieveConnectionState(usagePointConfig).ifPresent(cs -> usagePoint.setConnectionState(cs, creationDate));
        validateAndUpdateCustomPropertySetValues(usagePoint, usagePointConfig.getCustomAttributeSet());
        return usagePoint;
    }

    private com.elster.jupiter.metering.UsagePoint update() throws FaultMessage {
        com.elster.jupiter.metering.UsagePoint usagePoint = findUsagePoint(usagePointConfig);
        retrieveOptionalName(usagePointConfig).filter(name -> !usagePoint.getName().equals(name)).ifPresent(name -> {
            usagePoint.setName(name);
            usagePoint.update();
        });
        Instant now = clock.instant();

        if (usagePoint.getServiceCategory().getKind() == com.elster.jupiter.metering.ServiceKind.ELECTRICITY) {
            retrievePhaseCode(usagePointConfig).ifPresent(phaseCode -> usagePoint.getDetail(now)
                    .filter(ElectricityDetail.class::isInstance).map(ElectricityDetail.class::cast)
                    .filter(detail -> detail.getPhaseCode() != phaseCode)
                    .ifPresent(detail -> cloneDetail(detail, usagePoint, now).withPhaseCode(phaseCode).create()));
        }

        validateAndUpdateCustomPropertySetValues(usagePoint, usagePointConfig.getCustomAttributeSet());

        com.elster.jupiter.metering.UsagePoint result = processConfigurationEvent(usagePoint);

        retrieveMetrologyContract(result);
        retrieveConnectionState(usagePointConfig).ifPresent(cs -> updateConnectionStateIfNeeded(result, cs));
        return result;
    }

    private void validateAndUpdateCustomPropertySetValues(com.elster.jupiter.metering.UsagePoint usagePoint,
                                                          List<CustomAttributeSet> data) throws FaultMessage {
        for (CustomAttributeSet cas : data) {
            validateCustomPropertySetValues(usagePoint, cas);
            addCustomPropertySetValues(usagePoint, cas);
        }
        validateMandatoryCustomProperties(usagePoint);
        validateCustomPropertySetHasRequiredValues(usagePoint);
    }

    private com.elster.jupiter.metering.UsagePoint updateUsagePointLifeCycle(com.elster.jupiter.metering.UsagePoint usagePoint) throws FaultMessage {

        String newLifeCycleName = retrieveConfigurationEventValue(usagePointConfig);
        if (newLifeCycleName != null) {

            if (meteringService.findUsagePointLifeCycle(newLifeCycleName) == null) {
                throw messageFactory.usagePointConfigFaultMessageSupplier(basicFaultMessage,
                        MessageSeeds.LIFE_CYCLE_NOT_FOUND, newLifeCycleName).get();
            }
            UsagePointLifeCycle newLifeCycle = meteringService.findUsagePointLifeCycle(newLifeCycleName);
            String usagePointStateName = usagePoint.getState().getName();
            newLifeCycle.getStates().stream().filter(state -> state.getName().equals(usagePointStateName)).findAny()
                    .orElseThrow(messageFactory.usagePointConfigFaultMessageSupplier(basicFaultMessage,
                            MessageSeeds.NO_STATE_FOUND_IN_LIFE_CYCLE, newLifeCycle.getName(), usagePointStateName));
            usagePoint.setLifeCycle(newLifeCycleName);
            usagePoint.update();
        }
        return usagePoint;
    }

    private void retrieveMetrologyContract(com.elster.jupiter.metering.UsagePoint usagePoint) throws FaultMessage {
        Optional<UsagePoint.MetrologyRequirements> metrologyRequirement = usagePointConfig.getMetrologyRequirements()
                .stream().findFirst();
        if (metrologyRequirement.isPresent()) {
            String name = retrieveMetrologyRequirementElements(metrologyRequirement.get().getNames());

            List<UsagePoint.MetrologyRequirements.Names> metrologyPurposesList = retrieveMetrologyPurposes(
                    metrologyRequirement.get().getNames());

            UsagePointMetrologyConfiguration usagePointMetrConfig = retrieveMetrologyConfigurationDefault(name);
            if (metrologyPurposesList.size() >= 1) {
                setOptionalMetrologyContract(usagePoint, usagePointMetrConfig, metrologyPurposesList);
            }
        }
    }

    private void setOptionalMetrologyContract(com.elster.jupiter.metering.UsagePoint usagePoint,
                                              UsagePointMetrologyConfiguration usagePointMetrConfig,
                                              List<UsagePoint.MetrologyRequirements.Names> metrologyPurposesList) throws FaultMessage {
        List<MetrologyContract> metrologyMandatoryContractsList = usagePointMetrConfig.getContracts().stream()
                .filter(contract -> contract.isMandatory()).collect(Collectors.toList());
        for (MetrologyContract metrologyContract : metrologyMandatoryContractsList) {
            checkPurpose(metrologyContract, metrologyPurposesList);
        }

        List<MetrologyContract> metrologyOptionalContractsList = usagePointMetrConfig.getContracts().stream()
                .filter(contract -> !contract.isMandatory()).collect(Collectors.toList());
        for (MetrologyContract metrologyOptionalContract : metrologyOptionalContractsList) {
            if (metrologyPurposesList.stream()
                    .filter(purpose -> purpose.getName()
                            .equals(metrologyOptionalContract.getMetrologyPurpose().getName()))
                    .findFirst().get().getNameType().getDescription().equals("Active")) {
                activateOptionalMetrologyRequirment(usagePoint, metrologyOptionalContract);
            } else if (metrologyPurposesList.stream()
                    .filter(purpose -> purpose.getName()
                            .equals(metrologyOptionalContract.getMetrologyPurpose().getName()))
                    .findFirst().get().getNameType().getDescription().equals("Inactive")) {
                deactivateOptionalMetrologyRequirment(usagePoint, metrologyOptionalContract);
            }
        }
    }

    private void activateOptionalMetrologyRequirment(com.elster.jupiter.metering.UsagePoint usagePoint,
                                                     MetrologyContract mc) throws FaultMessage {
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMC = findEffectiveMetrologyConfiguration(usagePoint);
        checkMeterRequirments(usagePoint, mc);
        effectiveMC.activateOptionalMetrologyContract(mc, Instant.now(clock));
    }

    public void deactivateOptionalMetrologyRequirment(com.elster.jupiter.metering.UsagePoint usagePoint,
                                                      MetrologyContract mc) throws FaultMessage {
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMC = findEffectiveMetrologyConfiguration(usagePoint);
        Instant now = clock.instant();
        if (effectiveMC.getChannelsContainer(mc, now).isPresent()) {
            effectiveMC.deactivateOptionalMetrologyContract(mc, Instant.now(clock));
        }
    }

    private void checkMeterRequirments(com.elster.jupiter.metering.UsagePoint usagePoint,
                                       MetrologyContract metrologyContract) throws FaultMessage {
        List<MeterActivation> meterActivations = usagePoint.getMeterActivations(clock.instant());
        EffectiveMetrologyConfigurationOnUsagePoint metrologyConfigurationOnUsagePoint = usagePoint
                .getCurrentEffectiveMetrologyConfiguration()
                .orElseThrow(messageFactory.usagePointConfigFaultMessageSupplier(basicFaultMessage,
                        MessageSeeds.NO_METROLOGYCONFIG_FOR_USAGEPOINT, usagePoint.getName()));
        List<MeterRole> meterRolesOfMetrologyConfiguration = metrologyConfigurationOnUsagePoint
                .getMetrologyConfiguration().getMeterRoles();
        List<Pair<MeterRole, Meter>> metersInRoles = meterActivations.stream()
                .filter(meterActivation -> meterActivation.getMeterRole()
                        .map(meterRolesOfMetrologyConfiguration::contains).orElse(false))
                .map(meterActivation -> Pair.of(meterActivation.getMeterRole().get(), meterActivation.getMeter().get()))
                .collect(Collectors.toList());
        for (Pair<MeterRole, Meter> pair : metersInRoles) {
            MeterRole meterRole = pair.getFirst();
            Meter meter = pair.getLast();
            Set<ReadingTypeRequirement> requirements = metrologyContract.getRequirements().stream()
                    .filter(readingTypeRequirement -> meterRole.equals(metrologyConfigurationOnUsagePoint
                            .getMetrologyConfiguration().getMeterRoleFor(readingTypeRequirement).orElse(null)))
                    .collect(Collectors.toSet());
            Set<ReadingTypeRequirement> unsatisfiedRequirements = getUnsatisfiedReadingTypeRequirementsOfMeter(
                    requirements, meter);
            if (!unsatisfiedRequirements.isEmpty()) {
                throw messageFactory.usagePointConfigFaultMessageSupplier(basicFaultMessage,
                        MessageSeeds.UNSATISFIED_READING_TYPE_REQUIREMENTS).get();
            }
        }
    }

    private Set<ReadingTypeRequirement> getUnsatisfiedReadingTypeRequirementsOfMeter(
            Set<ReadingTypeRequirement> requirements, Meter meter) {
        List<ReadingType> meterProvidedReadingTypes = meter.getHeadEndInterface()
                .map(headEndInterface -> headEndInterface.getCapabilities(meter))
                .map(EndDeviceCapabilities::getConfiguredReadingTypes).orElse(Collections.emptyList());
        return requirements.stream()
                .filter(requirement -> !meterProvidedReadingTypes.stream().anyMatch(requirement::matches))
                .collect(Collectors.toSet());
    }

    private EffectiveMetrologyConfigurationOnUsagePoint findEffectiveMetrologyConfiguration(
            com.elster.jupiter.metering.UsagePoint usagePoint) throws FaultMessage {
        return usagePoint.getCurrentEffectiveMetrologyConfiguration()
                .orElseThrow(messageFactory.usagePointConfigFaultMessageSupplier(basicFaultMessage,
                        MessageSeeds.NO_METROLOGYCONFIG_FOR_USAGEPOINT, usagePoint.getName()));
    }

    private com.elster.jupiter.metering.UsagePoint get() throws FaultMessage {
        return findUsagePoint(usagePointConfig);
    }

    private com.elster.jupiter.metering.UsagePoint processConfigurationEvent(
            com.elster.jupiter.metering.UsagePoint usagePoint) throws FaultMessage {
        ConfigurationEvent configurationEvent = usagePointConfig.getConfigurationEvents();
        if (configurationEvent == null) {
            return usagePoint;
        }
        switch (retrieveReason(configurationEvent)) {
            case CHANGE_STATUS:
                return performTransition(usagePoint);
            case PURPOSE_ACTIVE:
                return activatePurpose(usagePoint);
            case PURPOSE_INACTIVE:
                return deactivatePurpose(usagePoint);
            case CHANGE_LIFECYCLE:
                return updateUsagePointLifeCycle(usagePoint);
            default:
                throw new UnsupportedOperationException();
        }
    }

    private com.elster.jupiter.metering.UsagePoint deactivatePurpose(com.elster.jupiter.metering.UsagePoint usagePoint)
            throws FaultMessage {
        String purposeName = retrieveConfigurationEventValue(usagePointConfig);
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMC = findEffectiveMetrologyConfiguration(usagePoint);
        MetrologyContract metrologyContract = findMetrologyContractOrThrowException(effectiveMC, purposeName);
        Instant now = clock.instant();

        if (!metrologyContract.isMandatory()) {
            if (effectiveMC.getChannelsContainer(metrologyContract, now).isPresent()) {
                effectiveMC.deactivateOptionalMetrologyContract(metrologyContract, now);
            } else {
                throw messageFactory
                        .usagePointConfigFaultMessageSupplier(basicFaultMessage,
                                MessageSeeds.PURPOSE_ALREADY_INACTIVE, purposeName)
                        .get();
            }
        } else {
            throw messageFactory
                    .usagePointConfigFaultMessageSupplier(basicFaultMessage,
                            MessageSeeds.CANT_DEACTIVATE_REQUIRED_PURPOSE, purposeName)
                    .get();
        }
        return usagePoint;
    }

    private com.elster.jupiter.metering.UsagePoint activatePurpose(com.elster.jupiter.metering.UsagePoint usagePoint)
            throws FaultMessage {
        String purposeName = retrieveConfigurationEventValue(usagePointConfig);
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMC = findEffectiveMetrologyConfiguration(usagePoint);
        MetrologyContract metrologyContract = findMetrologyContractOrThrowException(effectiveMC, purposeName);
        Instant now = clock.instant();

        if (!metrologyContract.isMandatory()) {
            checkMeterRequirments(usagePoint, metrologyContract);
            if (!effectiveMC.getChannelsContainer(metrologyContract, now).isPresent()) {
                effectiveMC.activateOptionalMetrologyContract(metrologyContract, now);
            } else {
                throw messageFactory
                        .usagePointConfigFaultMessageSupplier(basicFaultMessage,
                                MessageSeeds.PURPOSE_ALREADY_ACTIVE, purposeName)
                        .get();
            }
        } else {
            throw messageFactory
                    .usagePointConfigFaultMessageSupplier(basicFaultMessage,
                            MessageSeeds.CANT_ACTIVATE_REQUIRED_PUPROSE, purposeName)
                    .get();
        }
        return usagePoint;
    }


    public MetrologyContract findMetrologyContractOrThrowException(EffectiveMetrologyConfigurationOnUsagePoint effectiveMC, String purposeName)
            throws FaultMessage {
        return effectiveMC.getMetrologyConfiguration().getContracts().stream()
                .filter(mc -> mc.getMetrologyPurpose().getName().equals(purposeName))
                .findFirst()
                .orElseThrow(messageFactory.usagePointConfigFaultMessageSupplier(basicFaultMessage,
                        MessageSeeds.NO_PURPOSE_WITH_NAME, purposeName));
    }

    private ConfigurationEventReason retrieveReason(ConfigurationEvent event) throws FaultMessage {
        Status status = event.getStatus();
        if (status == null) {
            throw missingUsagePointParameterSupplier("ConfigurationEvents.status").get();
        }
        String reason = status.getReason();
        if (reason == null) {
            throw missingUsagePointParameterSupplier("ConfigurationEvents.status.reason").get();
        }

        switch (reason) {
            case STATUS_CHANGE:
                return ConfigurationEventReason.forReason(reason).filter(ConfigurationEventReason.CHANGE_STATUS::equals)
                        .get(); // only Change Status is supported now
            case ACTIVE_PURPOSE:
                return ConfigurationEventReason.forReason(reason).filter(ConfigurationEventReason.PURPOSE_ACTIVE::equals)
                        .get();
            case INACTIVE_PURPOSE:
                return ConfigurationEventReason.forReason(reason).filter(ConfigurationEventReason.PURPOSE_INACTIVE::equals)
                        .get();
            case CHANGE_LIFECYCLE:
                return ConfigurationEventReason.forReason(reason).filter(ConfigurationEventReason.CHANGE_LIFECYCLE::equals)
                        .get();
            default:
                throw unsupportedValueSupplier("ConfigurationEvents.status.reason", reason,
                        Arrays.stream(ConfigurationEventReason.values()).map(ConfigurationEventReason::getReason)
                                .toArray(String[]::new)).get();
        }
    }

    private com.elster.jupiter.metering.UsagePoint performTransition(com.elster.jupiter.metering.UsagePoint usagePoint)
            throws FaultMessage {
        String stateName = retrieveState(usagePointConfig);
        UsagePointTransition transition = findTransitionToState(stateName, usagePoint);
        Optional<ConnectionState> requestedConnectionState = retrieveConnectionState(usagePointConfig);
        Map<String, Object> properties = transition.getMicroActionsProperties().stream()
                .filter(spec -> CONNECTION_STATE_PROPERTY_NAME.equals(spec.getName())).findFirst()
                .map(spec -> (ValueFactory<?>) spec.getValueFactory())
                .flatMap(factory -> requestedConnectionState.map(ConnectionState::name).map(factory::fromStringValue)
                        .map(Object.class::cast))
                .map(value -> (Map<String, Object>) ImmutableMap.of(CONNECTION_STATE_PROPERTY_NAME, value))
                .orElseGet(Collections::emptyMap);
        List<UsagePointStateChangeFail> failures = usagePointLifeCycleService
                .performTransition(usagePoint, transition, "INS", properties).getFailReasons();
        if (!failures.isEmpty()) {
            throw faultMessage(failures);
        }
        return meteringService.findUsagePointById(usagePoint.getId()) // to re-read information about actual state
                .orElseThrow(IllegalStateException::new);
    }

    private FaultMessage faultMessage(List<UsagePointStateChangeFail> failures) {
        ErrorType[] errors = failures.stream().map(failure -> {
            switch (failure.getFailSource()) {
                case CHECK:
                    return replyTypeFactory.errorType(MessageSeeds.TRANSITION_CHECK_FAILED, failure.getName(),
                            failure.getMessage());
                case ACTION:
                    return replyTypeFactory.errorType(MessageSeeds.TRANSITION_ACTION_FAILED, failure.getName(),
                            failure.getMessage());
            }
            return null;
        }).toArray(ErrorType[]::new);
        return messageFactory.usagePointConfigFaultMessage(basicFaultMessage, errors);
    }

    private Optional<ConnectionState> retrieveConnectionState(UsagePoint usagePointConfig) throws FaultMessage {
        UsagePointConnectedKind connectionState = usagePointConfig.getConnectionState();
        if (connectionState != null) {
            String value = connectionState.value();
            return Optional.of(Arrays.stream(ConnectionState.supportedValues()).filter(cs -> cs.getId().equals(value))
                    .findFirst().orElseThrow(messageFactory.usagePointConfigFaultMessageSupplier(basicFaultMessage,
                            MessageSeeds.NO_CONNECTION_STATE_FOUND, value)));
        }
        return Optional.empty();
    }

    private UsagePointTransition findTransitionToState(String stateName,
                                                       com.elster.jupiter.metering.UsagePoint usagePoint) throws FaultMessage {
        String stateKey = DefaultUsagePointStateFinder.findForName(stateName).map(DefaultState::getKey)
                .orElse(stateName);
        if (stateKey.equals(usagePoint.getState().getName())) {
            throw messageFactory.usagePointConfigFaultMessageSupplier(basicFaultMessage,
                    MessageSeeds.USAGE_POINT_IS_ALREADY_IN_STATE, stateName).get();
        }
        return usagePointLifeCycleService.getAvailableTransitions(usagePoint, "INS").stream()
                .filter(transition -> transition.getTo().getName().equals(stateKey)).findAny().orElseThrow(() -> {
                    MessageSeeds detailedMessageSeed = containsStateWithKey(usagePoint.getLifeCycle(), stateKey)
                            ? MessageSeeds.NO_AVAILABLE_TRANSITION_TO_STATE
                            : MessageSeeds.NO_USAGE_POINT_STATE_WITH_NAME;
                    return messageFactory
                            .usagePointConfigFaultMessageSupplier(basicFaultMessage, detailedMessageSeed, stateName)
                            .get();
                });
    }

    private String retrieveConfigurationEventValue(UsagePoint usagePointConfig) throws FaultMessage {
        String configurationEventPurpose = usagePointConfig.getConfigurationEvents().getStatus().getValue();
        if (Checks.is(configurationEventPurpose).emptyOrOnlyWhiteSpace()) {
            throw emptyUsagePointParameterSupplier("status.value").get();
        }
        return configurationEventPurpose;
    }

    private String retrieveState(UsagePoint usagePointConfig) throws FaultMessage {
        Status status = retrieveMandatoryParameter("status", usagePointConfig::getStatus);
        String stateName = retrieveMandatoryParameter("status.value", status::getValue);
        if (Checks.is(stateName).emptyOrOnlyWhiteSpace()) {
            throw emptyUsagePointParameterSupplier("status.value").get();
        }
        return stateName;
    }

    private Optional<Instant> retrieveCreationDate(UsagePoint usagePointConfig) {
        return Optional.ofNullable(usagePointConfig.getConfigurationEvents())
                .map(ConfigurationEvent::getCreatedDateTime).flatMap(Optional::ofNullable);
    }

    private String retrieveMandatoryName(UsagePoint usagePointConfig) throws FaultMessage {
        return retrieveName(usagePointConfig.getNames());
    }

    private Optional<String> retrieveOptionalName(UsagePoint usagePointConfig) throws FaultMessage {
        List<Name> names = usagePointConfig.getNames();
        if (!names.isEmpty()) {
            return Optional.of(retrieveName(names));
        }
        return Optional.empty();
    }

    private String retrieveName(List<Name> names) throws FaultMessage {
        String nameString = null;
        if (names.size() > 1) {
            nameString = names.stream().filter(name -> USAGE_POINT_NAME.equals(name.getNameType().getName()))
                    .findFirst().map(Name::getName).orElse(null);
        } else if (names.size() == 1) {
            nameString = names.get(0).getName();
        }

        if (Checks.is(nameString).emptyOrOnlyWhiteSpace()) {
            throw emptyUsagePointParameterSupplier("Names[0].name").get();
        }
        return nameString;
    }

    private String retrieveOptionalLifeCycle(UsagePoint usagePointConfig) throws FaultMessage {
        return retrieveLifeCycle(usagePointConfig.getNames());
    }

    private String retrieveLifeCycle(List<Name> names) throws FaultMessage {
        String nameString = null;
        if (names.size() > 1 && names.stream().filter(name -> name.getNameType().getName().equals("UsagePointLifecycleName")).findFirst().isPresent()) {
            nameString = names.stream().filter(name -> name.getNameType().getName().equals("UsagePointLifecycleName"))
                    .findFirst().get().getName();
        }
        return nameString;
    }

    private com.elster.jupiter.metering.UsagePoint findUsagePoint(UsagePoint usagePointConfig) throws FaultMessage {
        String mRID = usagePointConfig.getMRID();
        if (mRID != null) {
            if (Checks.is(mRID).emptyOrOnlyWhiteSpace()) {
                throw emptyUsagePointParameterSupplier("mRID").get();
            }
            return meteringService.findUsagePointByMRID(mRID)
                    .orElseThrow(messageFactory.usagePointConfigFaultMessageSupplier(basicFaultMessage,
                            MessageSeeds.NO_USAGE_POINT_WITH_MRID, mRID));
        }
        String name = retrieveOptionalName(usagePointConfig)
                .orElseThrow(messageFactory.usagePointConfigFaultMessageSupplier(basicFaultMessage,
                        MessageSeeds.MISSING_MRID_OR_NAME_FOR_ELEMENT, pathToUsagePoint));
        return meteringService.findUsagePointByName(name).orElseThrow(messageFactory
                .usagePointConfigFaultMessageSupplier(basicFaultMessage, MessageSeeds.NO_USAGE_POINT_WITH_NAME, name));
    }

    private com.elster.jupiter.metering.ServiceCategory retrieveServiceCategory(UsagePoint usagePointConfig)
            throws FaultMessage {
        ServiceCategory serviceCategory = retrieveMandatoryParameter("ServiceCategory",
                usagePointConfig::getServiceCategory);
        ServiceKind serviceKind = retrieveMandatoryParameter("ServiceCategory.kind", serviceCategory::getKind);
        com.elster.jupiter.metering.ServiceKind realServiceKind = getServiceKindByValue(serviceKind.value());
        return meteringService.getServiceCategory(realServiceKind)
                .filter(com.elster.jupiter.metering.ServiceCategory::isActive)
                .orElseThrow(messageFactory.usagePointConfigFaultMessageSupplier(basicFaultMessage,
                        MessageSeeds.NO_SERVICE_CATEGORY_FOUND, realServiceKind.getDisplayName(thesaurus)));
    }

    private com.elster.jupiter.metering.ServiceKind getServiceKindByValue(String value) throws FaultMessage {
        return Arrays.stream(com.elster.jupiter.metering.ServiceKind.values())
                .filter(sk -> sk.getDisplayName().equals(value)).findFirst()
                .orElseThrow(messageFactory.usagePointConfigFaultMessageSupplier(basicFaultMessage,
                        MessageSeeds.NO_SERVICE_KIND_FOUND, value));
    }

    private Optional<com.elster.jupiter.cbo.PhaseCode> retrievePhaseCode(UsagePoint usagePointConfig) {
        return Optional.ofNullable(usagePointConfig.getPhaseCode()).map(PhaseCode::value)
                .map(com.elster.jupiter.cbo.PhaseCode::get);
    }

    private Optional<UsagePointMetrologyConfiguration> retrieveMetrologyConfiguration(UsagePoint usagePointConfig,
                                                                                      List<MeterActivation> meterActivationList,
                                                                                      Optional<EffectiveMetrologyConfigurationOnUsagePoint> effectiveMC) throws FaultMessage {
        Optional<UsagePoint.MetrologyRequirements> metrologyRequirement = usagePointConfig.getMetrologyRequirements()
                .stream().findFirst();
        if (!metrologyRequirement.isPresent()) {
            return Optional.empty();
        }
        String name = retrieveMetrologyRequirementElements(metrologyRequirement.get().getNames());
        if (Checks.is(name).emptyOrOnlyWhiteSpace()) {
            throw emptyUsagePointParameterSupplier("MetrologyRequirements[0].Names[0].name").get();
        }
        List<UsagePoint.MetrologyRequirements.Names> metrologyPurposesList = retrieveMetrologyPurposes(
                metrologyRequirement.get().getNames());
        UsagePointMetrologyConfiguration usagePointMetr = retrieveMetrologyConfigurationDefault(name);
        usagePointMetr = findPurposes(metrologyPurposesList, usagePointMetr, meterActivationList, effectiveMC);

        return Optional.of(usagePointMetr);
    }

    private UsagePointMetrologyConfiguration findPurposes(
            List<UsagePoint.MetrologyRequirements.Names> metrologyPurposesList,
            UsagePointMetrologyConfiguration usagePointMetrologyConfiguration,
            List<MeterActivation> meterActivationList,
            Optional<EffectiveMetrologyConfigurationOnUsagePoint> effectiveMC) throws FaultMessage {
        List<MetrologyContract> metrologyMandatoryContractsList = usagePointMetrologyConfiguration.getContracts()
                .stream().filter(MetrologyContract::isMandatory).collect(Collectors.toList());

        for (MetrologyContract metrologyContract : metrologyMandatoryContractsList) {
            checkPurpose(metrologyContract, metrologyPurposesList);
        }
        List<MetrologyContract> metrologyOptionalContractsList = usagePointMetrologyConfiguration.getContracts()
                .stream().filter(contract -> !contract.isMandatory()).collect(Collectors.toList());
        for (MetrologyContract metrologyOptionalContract : metrologyOptionalContractsList) {
            activateOptionalPurpose(metrologyOptionalContract, metrologyPurposesList, meterActivationList, effectiveMC);
        }

        return usagePointMetrologyConfiguration;
    }

    private void activateOptionalPurpose(MetrologyContract contract,
                                         List<UsagePoint.MetrologyRequirements.Names> metrologyPurposesList,
                                         List<MeterActivation> meterActivationList,
                                         Optional<EffectiveMetrologyConfigurationOnUsagePoint> effectiveMC) throws FaultMessage {
        if (metrologyPurposesList.stream().filter(p -> p.getNameType().getDescription().equals("Active"))
                .count() != 0) {
            if (metrologyPurposesList.stream()
                    .filter(purpose -> purpose.getName().equals(contract.getMetrologyPurpose().getName())
                            && purpose.getNameType().getDescription().equals("Active"))
                    .collect(Collectors.toList()).size() != 0 && meterActivationList.size() != 0) {

                effectiveMC.get().activateOptionalMetrologyContract(contract, clock.instant());
            }
        }
    }

    private void checkPurpose(MetrologyContract metrologyContract,
                              List<UsagePoint.MetrologyRequirements.Names> metrologyPurposesList) throws FaultMessage {
        if (metrologyPurposesList.stream()
                .filter(purpose -> purpose.getName().equals(metrologyContract.getMetrologyPurpose().getName())
                        && purpose.getNameType().getDescription().equals("Inactive"))
                .collect(Collectors.toList()).size() != 0) {
            throw messageFactory
                    .usagePointConfigFaultMessageSupplier(basicFaultMessage,
                            MessageSeeds.INVALID_METROLOGY_CONTRACT_REQUIRMENT)
                    .get();
        }
    }

    private UsagePointMetrologyConfiguration retrieveMetrologyConfigurationDefault(
            String usagePointMetrologyConfigurationName) throws FaultMessage {
        UsagePointMetrologyConfiguration metrologyConfiguration = metrologyConfigurationService
                .findMetrologyConfiguration(usagePointMetrologyConfigurationName)
                .filter(UsagePointMetrologyConfiguration.class::isInstance)
                .map(UsagePointMetrologyConfiguration.class::cast)
                .orElseThrow(messageFactory.usagePointConfigFaultMessageSupplier(basicFaultMessage,
                        MessageSeeds.NO_METROLOGY_CONFIGURATION_WITH_NAME, usagePointMetrologyConfigurationName));
        if (metrologyConfiguration.isActive()) {
            return metrologyConfiguration;
        } else {
            throw messageFactory.usagePointConfigFaultMessageSupplier(basicFaultMessage,
                    MessageSeeds.NO_ACTIVE_METROLOGY_CONFIGURATION_WITH_NAME, usagePointMetrologyConfigurationName)
                    .get();
        }
    }

    private <T> T retrieveMandatoryParameter(String element, Supplier<T> supplier) throws FaultMessage {
        T parameter = supplier.get();
        if (parameter == null) {
            throw missingUsagePointParameterSupplier(element).get();
        }
        return parameter;
    }

    private List<UsagePoint.MetrologyRequirements.Names> retrieveMetrologyPurposes(
            List<UsagePoint.MetrologyRequirements.Names> names) {
        return names.stream().filter(name -> name.getNameType().getName().equals("Purpose"))
                .collect(Collectors.toList());
    }

    private String retrieveMetrologyRequirementElements(List<UsagePoint.MetrologyRequirements.Names> names)
            throws FaultMessage {
        List<UsagePoint.MetrologyRequirements.Names> metrologyRequirementsList = names;
        if (metrologyRequirementsList.size() >= 1) {
            if (metrologyRequirementsList.stream().filter(n -> n.getNameType() == null).collect(Collectors.toList())
                    .size() == 1) {
                return setEmptyNameTypeMetrologyRequirment(metrologyRequirementsList);
            } else if (metrologyRequirementsList.stream().filter(n -> n.getNameType() == null)
                    .collect(Collectors.toList()).size() > 1) {
                throw messageFactory.usagePointConfigFaultMessageSupplier(basicFaultMessage,
                        MessageSeeds.MORE_THAN_ONE_METROLOGY_CONFIGURATION_SPECIFIED).get();
            } else if (metrologyRequirementsList.stream()
                    .filter(name -> name.getNameType().getName().equals(METROLOGY_CONFIGURATION)).count() == 1) {
                return metrologyRequirementsList.stream()
                        .filter(n -> n.getNameType().getName().equals(METROLOGY_CONFIGURATION)).findAny().get()
                        .getName();
            } else {
                throw messageFactory.usagePointConfigFaultMessageSupplier(basicFaultMessage,
                        MessageSeeds.MORE_THAN_ONE_METROLOGY_CONFIGURATION_SPECIFIED).get();
            }
        } else {
            throw emptyUsagePointParameterSupplier("MetrologyRequirements[0].Names[0].name").get();
        }
    }

    private String setEmptyNameTypeMetrologyRequirment(
            List<UsagePoint.MetrologyRequirements.Names> metrologyRequirmentsList) {
        UsagePoint.MetrologyRequirements.Names.NameType value = new UsagePoint.MetrologyRequirements.Names.NameType();
        value.setName(METROLOGY_CONFIGURATION);
        metrologyRequirmentsList.stream().filter(name -> name.getNameType() == null).findFirst().get()
                .setNameType(value);
        return metrologyRequirmentsList.stream().filter(n -> n.getNameType().getName().equals(METROLOGY_CONFIGURATION))
                .findAny().get().getName();
    }

    private <T> T retrieveOneMandatoryElement(String listElement, Supplier<List<T>> supplier) throws FaultMessage {
        return retrieveOneOptionalElement(listElement, supplier)
                .orElseThrow(emptyUsagePointParametersListSupplier(listElement));
    }

    private <T> Optional<T> retrieveOneOptionalElement(String listElement, Supplier<List<T>> supplier)
            throws FaultMessage {
        List<T> parameters = supplier.get();
        switch (parameters.size()) {
            case 0:
                return Optional.empty();
            case 1:
                return Optional.of(parameters.get(0));
            default:
                throw messageFactory.usagePointConfigFaultMessageSupplier(basicFaultMessage,
                        MessageSeeds.UNSUPPORTED_LIST_SIZE, pathToUsagePoint + '.' + listElement, 1).get();
        }
    }

    private void validateMandatoryCustomProperties(com.elster.jupiter.metering.UsagePoint usagePoint)
            throws FaultMessage {
        List<CustomPropertySet> customPropertySets = usagePoint.forCustomProperties().getAllPropertySets().stream()
                .map(RegisteredCustomPropertySet::getCustomPropertySet).collect(Collectors.toList());
        for (CustomPropertySet customPropertySet : customPropertySets) {
            List<PropertySpec> propertySpecs = customPropertySet.getPropertySpecs();
            List<CustomPropertySetValues> valuesList = new ArrayList<>();
            if (customPropertySet.isVersioned()) {
                valuesList.addAll(customPropertySetService.getAllVersionedValuesFor(customPropertySet, usagePoint));
            } else {
                valuesList.add(getValues(usagePoint, customPropertySet, null));
            }
            for (PropertySpec spec : propertySpecs) {
                if (spec.isRequired() && !valuesList.stream()
                        .filter(values -> values.getProperty(spec.getName()) != null).findFirst().isPresent()) {
                    throw messageFactory.usagePointConfigFaultMessageSupplier(basicFaultMessage,
                            MessageSeeds.MISSING_REQUIRED_CUSTOMATTRIBUTE_VALUE, spec.getName(),
                            customPropertySet.getId()).get();
                }
            }
        }
    }

    private void validateCustomPropertySetHasRequiredValues(com.elster.jupiter.metering.UsagePoint usagePoint)
            throws FaultMessage {
        List<CustomPropertySet> customPropertySets = usagePoint.forCustomProperties().getAllPropertySets().stream()
                .map(RegisteredCustomPropertySet::getCustomPropertySet).filter(cps -> cps.isRequired())
                .collect(Collectors.toList());
        for (CustomPropertySet customPropertySet : customPropertySets) {
            if (!customPropertySetService.validateCustomPropertySetHasValues(customPropertySet, usagePoint,
                    Range.greaterThan(usagePoint.getInstallationTime()))) {
                throw messageFactory.usagePointConfigFaultMessageSupplier(basicFaultMessage,
                        MessageSeeds.CUSTOMPROPERTYSET_VALUES_ON_REQUIRED_RANGE, customPropertySet.getId(),
                        Range.greaterThan(usagePoint.getInstallationTime())).get();
            }
        }
    }

    private void validatePossibleValues(CustomPropertySet<?, ?> customPropertySet, CustomPropertySetValues values,
                                        CustomAttributeSet data) throws FaultMessage {
        List<PropertySpec> propertySpecs = customPropertySet.getPropertySpecs();
        for (PropertySpec spec : propertySpecs) {
            Object value = values.getProperty(spec.getName());
            if (spec.getValueFactory().getValueType().equals(Quantity.class)
                    && !isValidQuantityValues(spec, (Quantity) value)) {
                throw messageFactory.usagePointConfigFaultMessageSupplier(basicFaultMessage,
                        MessageSeeds.WRONG_QUANTITY_FORMAT, spec.getName(),
                        spec.getPossibleValues().getAllValues().stream()
                                .map(q -> String.valueOf(((Quantity) q).getMultiplier()))
                                .collect(Collectors.joining(",")),
                        spec.getPossibleValues().getAllValues().stream()
                                .map(q -> String.valueOf(((Quantity) q).getUnit())).collect(Collectors.joining(",")))
                        .get();
            } else if (value != null && spec.getPossibleValues() != null && spec.getPossibleValues().isExhaustive()
                    && !isValidEnumValues(spec, value)) {
                throw messageFactory.usagePointConfigFaultMessageSupplier(basicFaultMessage,
                        MessageSeeds.WRONG_ENUM_FORMAT, spec.getName(), spec.getPossibleValues().getAllValues().stream()
                                .map(String::valueOf).collect(Collectors.joining(",")))
                        .get();
            }

        }
    }

    private void validateCustomPropertySetValues(com.elster.jupiter.metering.UsagePoint usagePoint,
                                                 CustomAttributeSet data) throws FaultMessage {
        Optional<CustomPropertySet> customPropertySet = usagePoint.forCustomProperties().getAllPropertySets().stream()
                .map(RegisteredCustomPropertySet::getCustomPropertySet)
                .filter(cps -> data.getId().equalsIgnoreCase(cps.getId())).findFirst();
        if (customPropertySet.isPresent()) {
            validateCustomPropertySetValues(customPropertySet.get(), usagePoint, data, null);
        } else {
            throw messageFactory.usagePointConfigFaultMessageSupplier(basicFaultMessage,
                    MessageSeeds.NO_CUSTOMATTRIBUTE, data.getId()).get();
        }
    }

    private void validateCustomPropertySetValues(CustomPropertySet customPropertySet, Object businessObject,
                                                 CustomAttributeSet data, Object additionalPrimaryKey) throws FaultMessage {
        if (!customPropertySet.isVersioned()) {
            validateCreateOrUpdateNonVersionedSet(businessObject, customPropertySet, data, additionalPrimaryKey);
        } else {
            validateCreateOrUpdateVersionedSet(businessObject, customPropertySet, data, additionalPrimaryKey);
        }
    }

    private void validateCreateOrUpdateVersionedSet(Object businessObject,
                                                    CustomPropertySet<Object, ? extends PersistentDomainExtension> customPropertySet, CustomAttributeSet data,
                                                    Object additionalObject) throws FaultMessage {
        Optional<Instant> versionId = Optional.ofNullable(data.getVersionId());
        CustomPropertySetValues values;
        if (versionId.isPresent()) {
            values = getValuesVersion(businessObject, customPropertySet, versionId.get(), additionalObject);
            if (values == null) {
                throw messageFactory.usagePointConfigFaultMessageSupplier(basicFaultMessage,
                        MessageSeeds.NO_CUSTOMATTRIBUTE_VERSION, DefaultDateTimeFormatters.shortDate().withShortTime()
                                .build().format(versionId.get().atZone(clock.getZone())))
                        .get();
            }
        } else {
            values = CustomPropertySetValues.empty();
        }
        validateCreateOrUpdateCustomAttributeValues(customPropertySet, data, values);
    }

    private void validateCreateOrUpdateNonVersionedSet(Object businessObject,
                                                       CustomPropertySet<Object, ? extends PersistentDomainExtension> customPropertySet, CustomAttributeSet data,
                                                       Object additionalObject) throws FaultMessage {
        CustomPropertySetValues values = getValues(businessObject, customPropertySet, additionalObject);
        updateValues(customPropertySet, data, values);
        validateCreateOrUpdateCustomAttributeValues(customPropertySet, data, values);
    }

    private void validateCreateOrUpdateCustomAttributeValues(
            CustomPropertySet<Object, ? extends PersistentDomainExtension> customPropertySet, CustomAttributeSet data,
            CustomPropertySetValues values) throws FaultMessage {
        if (values != null) {
            updateValues(customPropertySet, data, values);
            validatePossibleValues(customPropertySet, values, data);
            customPropertySetService.validateCustomPropertySetValues(customPropertySet, values);
        }
    }

    private CustomPropertySetValues getValues(Object businessObject,
                                              CustomPropertySet<Object, ? extends PersistentDomainExtension> customPropertySet,
                                              Object additionalPrimaryKeyObject) throws FaultMessage {
        CustomPropertySetValues values;
        if (additionalPrimaryKeyObject != null) {
            values = customPropertySetService.getUniqueValuesFor(customPropertySet, businessObject,
                    additionalPrimaryKeyObject);
        } else {
            values = customPropertySetService.getUniqueValuesFor(customPropertySet, businessObject);
        }
        return values;
    }

    private CustomPropertySetValues getValuesVersion(Object businessObject,
                                                     CustomPropertySet<Object, ? extends PersistentDomainExtension> customPropertySet, Instant versionId,
                                                     Object additionalPrimaryKeyObject) {
        if (additionalPrimaryKeyObject != null) {
            return customPropertySetService.getUniqueValuesFor(customPropertySet, businessObject, versionId,
                    additionalPrimaryKeyObject);
        } else {
            return customPropertySetService.getUniqueValuesFor(customPropertySet, businessObject, versionId);
        }
    }

    private CustomPropertySetValues updateValues(
            CustomPropertySet<Object, ? extends PersistentDomainExtension> customPropertySet, CustomAttributeSet data,
            CustomPropertySetValues values) throws FaultMessage {
        for (PropertySpec spec : customPropertySet.getPropertySpecs()) {
            Optional value = findValue(customPropertySet, spec, data);
            if (value.isPresent()) {
                values.setProperty(spec.getName(), value.get());
            }
        }
        return values;

    }

    private Optional<Object> findValue(CustomPropertySet<Object, ? extends PersistentDomainExtension> customPropertySet,
                                       PropertySpec spec, CustomAttributeSet data) throws FaultMessage {
        if (data.getId().equalsIgnoreCase(customPropertySet.getId())) {
            for (Attribute attr : data.getAttribute()) {
                if (attr.getName().equalsIgnoreCase(spec.getName())) {
                    return Optional.of(convertValues(spec, attr.getValue()));
                }
            }
        }
        return Optional.empty();
    }

    private Object convertValues(PropertySpec spec, String value) {
        return spec.getValueFactory().fromStringValue(value);
    }

    private void addCustomPropertySetValues(com.elster.jupiter.metering.UsagePoint usagePoint, CustomAttributeSet data)
            throws FaultMessage {
        for (RegisteredCustomPropertySet registeredCustomPropertySet : usagePoint.forCustomProperties()
                .getAllPropertySets().stream()
                .filter(cps -> data.getId().equalsIgnoreCase(cps.getCustomPropertySetId()))
                .collect(Collectors.toList())) {
            addCustomPropertySetValues(registeredCustomPropertySet.getCustomPropertySet(), usagePoint, data, null);
        }
    }

    private void addCustomPropertySetValues(CustomPropertySet customPropertySet, Object businessObject,
                                            CustomAttributeSet data, Object additionalPrimaryKey) throws FaultMessage {
        if (customPropertySet.isVersioned()) {
            createOrUpdateVersionedSet(businessObject, customPropertySet, data, additionalPrimaryKey);
        } else {
            createOrUpdateNonVersionedSet(businessObject, customPropertySet, data, additionalPrimaryKey);
        }
    }

    private void createOrUpdateVersionedSet(Object businessObject,
                                            CustomPropertySet<Object, ? extends PersistentDomainExtension> customPropertySet, CustomAttributeSet data,
                                            Object additionalPrimaryKeyObject) throws FaultMessage {
        Optional<Instant> startTime = Optional.ofNullable(data.getFromDateTime());
        Optional<Instant> endTime = Optional.ofNullable(data.getToDateTime());
        Optional<Instant> versionId = Optional.ofNullable(data.getVersionId());
        Optional<Boolean> updateRange = Optional.ofNullable(data.isUpdateRange());
        checkInterval(startTime, endTime, customPropertySet, data);
        if (versionId.isPresent()) {
            CustomPropertySetValues values = getValuesVersion(businessObject, customPropertySet, versionId.get(),
                    additionalPrimaryKeyObject);
            if (!values.isEmpty()) {
                values = updateValues(customPropertySet, data, values);
                Range<Instant> range;
                if (updateRange.isPresent() && updateRange.get()) {
                    if (!endTime.isPresent() && data.getId().equalsIgnoreCase(customPropertySet.getId())) {
                        endTime = Optional.of(Instant.EPOCH);
                    }
                    range = getRangeToUpdate(startTime, endTime, values.getEffectiveRange());
                    OverlapCalculatorBuilder overlapCalculatorBuilder;
                    if (additionalPrimaryKeyObject != null) {
                        overlapCalculatorBuilder = customPropertySetService.calculateOverlapsFor(customPropertySet,
                                businessObject, additionalPrimaryKeyObject);
                    } else {
                        overlapCalculatorBuilder = customPropertySetService.calculateOverlapsFor(customPropertySet,
                                businessObject);
                    }
                    for (ValuesRangeConflict conflict : overlapCalculatorBuilder.whenUpdating(versionId.get(), range)) {
                        if (conflict.getType().equals(ValuesRangeConflictType.RANGE_GAP_AFTER)) {
                            range = getRangeToUpdate(
                                    Optional.ofNullable(conflict.getConflictingRange().lowerEndpoint()), endTime,
                                    values.getEffectiveRange());
                        }
                        if (conflict.getType().equals(ValuesRangeConflictType.RANGE_GAP_BEFORE)) {
                            range = getRangeToUpdate(startTime,
                                    Optional.ofNullable(conflict.getConflictingRange().upperEndpoint()),
                                    values.getEffectiveRange());
                        }
                    }
                } else {
                    range = values.getEffectiveRange();
                }
                if (additionalPrimaryKeyObject != null) {
                    customPropertySetService.setValuesVersionFor(customPropertySet, businessObject, values, range,
                            versionId.get(), additionalPrimaryKeyObject);
                } else {
                    customPropertySetService.setValuesVersionFor(customPropertySet, businessObject, values, range,
                            versionId.get());
                }
            } else {
                throw messageFactory.usagePointConfigFaultMessageSupplier(basicFaultMessage,
                        MessageSeeds.NO_CUSTOMATTRIBUTE_VERSION, DefaultDateTimeFormatters.shortDate().withShortTime()
                                .build().format(versionId.get().atZone(clock.getZone())))
                        .get();
            }
        } else {
            com.elster.jupiter.metering.UsagePoint usagePoint = (com.elster.jupiter.metering.UsagePoint) businessObject;
            if (!startTime.isPresent() || startTime.get().isBefore(usagePoint.getInstallationTime())) {
                throw messageFactory.usagePointConfigFaultMessageSupplier(basicFaultMessage,
                        MessageSeeds.START_DATE_LOWER_CREATED_DATE, customPropertySet.getId(), usagePoint.getName())
                        .get();
            }
            Range<Instant> range = getRangeToCreate(startTime, endTime);
            OverlapCalculatorBuilder overlapCalculatorBuilder;
            if (additionalPrimaryKeyObject != null) {
                overlapCalculatorBuilder = customPropertySetService.calculateOverlapsFor(customPropertySet,
                        businessObject, additionalPrimaryKeyObject);
            } else {
                overlapCalculatorBuilder = customPropertySetService.calculateOverlapsFor(customPropertySet,
                        businessObject);
            }
            for (ValuesRangeConflict conflict : overlapCalculatorBuilder.whenCreating(range)) {
                if (conflict.getType().equals(ValuesRangeConflictType.RANGE_GAP_AFTER)) {
                    range = getRangeToCreate(Optional.ofNullable(conflict.getConflictingRange().lowerEndpoint()),
                            endTime);
                }
                if (conflict.getType().equals(ValuesRangeConflictType.RANGE_GAP_BEFORE)) {
                    range = getRangeToCreate(startTime,
                            Optional.ofNullable(conflict.getConflictingRange().upperEndpoint()));
                }
            }
            CustomPropertySetValues newValues = CustomPropertySetValues.emptyDuring(range);
            if (additionalPrimaryKeyObject != null) {
                customPropertySetService.setValuesVersionFor(customPropertySet, businessObject,
                        updateValues(customPropertySet, data, newValues), range, additionalPrimaryKeyObject);
            } else {
                customPropertySetService.setValuesVersionFor(customPropertySet, businessObject,
                        updateValues(customPropertySet, data, newValues), range);
            }
        }
    }

    private void createOrUpdateNonVersionedSet(Object businessObject,
                                               CustomPropertySet<Object, ? extends PersistentDomainExtension> customPropertySet, CustomAttributeSet data,
                                               Object additionalPrimaryKeyObject) throws FaultMessage {
        CustomPropertySetValues values = getValues(businessObject, customPropertySet, additionalPrimaryKeyObject);
        updateValues(customPropertySet, data, values);
        if (additionalPrimaryKeyObject != null) {
            customPropertySetService.setValuesFor(customPropertySet, businessObject, values,
                    additionalPrimaryKeyObject);
        } else {
            customPropertySetService.setValuesFor(customPropertySet, businessObject, values);
        }
    }

    private Range<Instant> getRangeToCreate(Optional<Instant> startTime, Optional<Instant> endTime) {
        if ((!startTime.isPresent() || startTime.get().equals(Instant.EPOCH))
                && (!endTime.isPresent() || endTime.get().equals(Instant.EPOCH))) {
            return Range.all();
        } else if (!startTime.isPresent() || startTime.get().equals(Instant.EPOCH) && endTime.isPresent()
                && !endTime.get().equals(Instant.EPOCH)) {
            return Range.lessThan(endTime.get());
        } else if (!endTime.isPresent() || endTime.get().equals(Instant.EPOCH)) {
            return Range.atLeast(startTime.get());
        } else {
            return Range.closedOpen(startTime.get(), endTime.get());
        }
    }

    private Range<Instant> getRangeToUpdate(Optional<Instant> startTime, Optional<Instant> endTime,
                                            Range<Instant> oldRange) {
        if (!startTime.isPresent() && !endTime.isPresent()) {
            return oldRange;
        } else if (!startTime.isPresent()) {
            if (oldRange.hasLowerBound()) {
                if (!endTime.get().equals(Instant.EPOCH)) {
                    return Range.closedOpen(oldRange.lowerEndpoint(), endTime.get());
                } else {
                    return Range.atLeast(oldRange.lowerEndpoint());
                }
            } else if (endTime.get().equals(Instant.EPOCH)) {
                return Range.all();
            } else {
                return Range.lessThan(endTime.get());
            }
        } else if (!endTime.isPresent()) {
            if (oldRange.hasUpperBound()) {
                if (!startTime.get().equals(Instant.EPOCH)) {
                    return Range.closedOpen(startTime.get(), oldRange.upperEndpoint());
                } else {
                    return Range.lessThan(oldRange.upperEndpoint());
                }
            } else if (startTime.get().equals(Instant.EPOCH)) {
                return Range.all();
            } else {
                return Range.atLeast(startTime.get());
            }
        } else {
            return getRangeToCreate(startTime, endTime);
        }
    }

    private void checkInterval(Optional<Instant> startTime, Optional<Instant> endTime,
                               CustomPropertySet customPropertySet, CustomAttributeSet data) throws FaultMessage {
        if (startTime.isPresent() && endTime.isPresent() && !endTime.get().equals(Instant.EPOCH)
                && endTime.get().isBefore(startTime.get())) {
            throw messageFactory.usagePointConfigFaultMessageSupplier(basicFaultMessage, MessageSeeds.INVALID_RANGE,
                    customPropertySet.getId()).get();
        }
    }

    private boolean isValidQuantityValues(PropertySpec spec, Quantity value) {
        if (value == null) {
            return true;
        }
        if (spec.getPossibleValues().getAllValues().stream()
                .noneMatch(pv -> ((Quantity) pv).getUnit().equals(value.getUnit())
                        && ((Quantity) pv).getMultiplier() == value.getMultiplier())) {
            return false;
        }
        return true;
    }

    private boolean isValidEnumValues(PropertySpec spec, Object value) {
        if (spec.getPossibleValues().getAllValues().stream().noneMatch(pv -> pv.equals(value))) {
            return false;
        }
        return true;
    }

    private Supplier<FaultMessage> missingUsagePointParameterSupplier(String element) {
        return messageFactory.usagePointConfigFaultMessageSupplier(basicFaultMessage, MessageSeeds.MISSING_ELEMENT,
                pathToUsagePoint + '.' + element);
    }

    private Supplier<FaultMessage> emptyUsagePointParametersListSupplier(String element) {
        return messageFactory.usagePointConfigFaultMessageSupplier(basicFaultMessage, MessageSeeds.EMPTY_LIST,
                pathToUsagePoint + '.' + element);
    }

    private Supplier<FaultMessage> emptyUsagePointParameterSupplier(String element) {
        return messageFactory.usagePointConfigFaultMessageSupplier(basicFaultMessage, MessageSeeds.EMPTY_ELEMENT,
                pathToUsagePoint + '.' + element);
    }

    private Supplier<FaultMessage> unsupportedValueSupplier(String element, String value, String... supportedValues) {
        return messageFactory.usagePointConfigFaultMessageSupplier(basicFaultMessage, MessageSeeds.UNSUPPORTED_VALUE,
                pathToUsagePoint + '.' + element, value,
                Arrays.stream(supportedValues).map(each -> '\'' + each + '\'').collect(Collectors.joining(", ")));
    }

    public interface PreparedUsagePointBuilder {
        PreparedUsagePointBuilder at(Instant timestamp);

        @TransactionRequired
        com.elster.jupiter.metering.UsagePoint create() throws FaultMessage;

        @TransactionRequired
        com.elster.jupiter.metering.UsagePoint update() throws FaultMessage;

        com.elster.jupiter.metering.UsagePoint get() throws FaultMessage;
    }

}
