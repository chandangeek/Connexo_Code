/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cim.webservices.inbound.soap.usagepointconfig;

import com.elster.jupiter.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.elster.jupiter.cim.webservices.inbound.soap.impl.ReplyTypeFactory;
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
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.usagepoint.lifecycle.UsagePointLifeCycleService;
import com.elster.jupiter.usagepoint.lifecycle.UsagePointStateChangeFail;
import com.elster.jupiter.usagepoint.lifecycle.config.DefaultState;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycle;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointTransition;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.Pair;

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
import com.google.common.collect.ImmutableMap;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

class UsagePointBuilder {
    private static final String CONNECTION_STATE_PROPERTY_NAME = "set.connection.state.property.name";
    private static final String METROLOGY_CONFIGURATION = "MetrologyConfiguration";
    private static final String STATUS_CHANGE = "Change Status";
    private static final String ACTIVE_PURPOSE = "Purpose Active";
    private static final String INACTIVE_PURPOSE = "Purpose Inactive";
    private final Thesaurus thesaurus;
    private final ReplyTypeFactory replyTypeFactory;
    private final UsagePointConfigFaultMessageFactory messageFactory;
    private final MeteringService meteringService;
    private final MetrologyConfigurationService metrologyConfigurationService;
    private final Clock clock;
    private final UsagePointLifeCycleService usagePointLifeCycleService;

    private UsagePoint usagePointConfig;
    private String pathToUsagePoint;
    private Instant requestTimestamp;
    private MessageSeeds basicFaultMessage;

    @Inject
    UsagePointBuilder(Thesaurus thesaurus,
                      ReplyTypeFactory replyTypeFactory,
                      UsagePointConfigFaultMessageFactory messageFactory,
                      MeteringService meteringService,
                      MetrologyConfigurationService metrologyConfigurationService,
                      Clock clock,
                      UsagePointLifeCycleService usagePointLifeCycleService) {
        this.thesaurus = thesaurus;
        this.replyTypeFactory = replyTypeFactory;
        this.messageFactory = messageFactory;
        this.meteringService = meteringService;
        this.metrologyConfigurationService = metrologyConfigurationService;
        this.clock = clock;
        this.usagePointLifeCycleService = usagePointLifeCycleService;
    }

    private static void updateConnectionStateIfNeeded(com.elster.jupiter.metering.UsagePoint usagePoint,
                                                      ConnectionState connectionState) {
        if (!usagePoint.getCurrentConnectionState()
                .map(UsagePointConnectionState::getConnectionState)
                .filter(connectionState::equals)
                .isPresent()) {
            usagePoint.setConnectionState(connectionState);
        }
    }

    private static ElectricityDetailBuilder cloneDetail(ElectricityDetail detail,
                                                        com.elster.jupiter.metering.UsagePoint usagePoint,
                                                        Instant timestamp) {
        return usagePoint.newElectricityDetailBuilder(timestamp)
                .withCollar(detail.isCollarInstalled())
                .withGrounded(detail.isGrounded())
                .withNominalServiceVoltage(detail.getNominalServiceVoltage())
                .withPhaseCode(detail.getPhaseCode())
                .withRatedCurrent(detail.getRatedCurrent())
                .withRatedPower(detail.getRatedPower())
                .withEstimatedLoad(detail.getEstimatedLoad())
                .withLimiter(detail.isLimiter())
                .withLoadLimiterType(detail.getLoadLimiterType())
                .withLoadLimit(detail.getLoadLimit())
                .withInterruptible(detail.isInterruptible());
    }

    private static boolean containsStateWithKey(UsagePointLifeCycle lifeCycle, String stateKey) {
        return lifeCycle.getStates().stream()
                .anyMatch(state -> state.getName().equals(stateKey));
    }

    PreparedUsagePointBuilder from(UsagePoint usagePointConfig, int usagePointIndex) {
        this.usagePointConfig = usagePointConfig;
        this.pathToUsagePoint = "UsagePointConfig.UsagePoint[" + usagePointIndex + "]";
        return new PreparedUsagePointBuilder() {
            @Override
            public PreparedUsagePointBuilder at(Instant timestamp) {
                UsagePointBuilder.this.at(timestamp);
                return this;
            }

            @Override
            public com.elster.jupiter.metering.UsagePoint create() throws FaultMessage {
                basicFaultMessage = MessageSeeds.UNABLE_TO_CREATE_USAGE_POINT;
                return UsagePointBuilder.this.create();
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

        Optional<EffectiveMetrologyConfigurationOnUsagePoint> effectiveMC = usagePoint.getCurrentEffectiveMetrologyConfiguration();
        retrieveMetrologyConfiguration(usagePointConfig, usagePoint.getCurrentMeterActivations(), effectiveMC).ifPresent(mc -> usagePoint
                .apply(mc, creationDate));
        retrieveConnectionState(usagePointConfig).ifPresent(cs -> usagePoint.setConnectionState(cs, creationDate));
        return usagePoint;
    }

    private com.elster.jupiter.metering.UsagePoint update() throws FaultMessage {
        com.elster.jupiter.metering.UsagePoint usagePoint = findUsagePoint(usagePointConfig);
        retrieveOptionalName(usagePointConfig)
                .filter(name -> !usagePoint.getName().equals(name))
                .ifPresent(name -> {
                    usagePoint.setName(name);
                    usagePoint.update();
                });
        Instant now = clock.instant();

        if (usagePoint.getServiceCategory().getKind() == com.elster.jupiter.metering.ServiceKind.ELECTRICITY) {
            retrievePhaseCode(usagePointConfig)
                    .ifPresent(phaseCode -> usagePoint.getDetail(now)
                            .filter(ElectricityDetail.class::isInstance)
                            .map(ElectricityDetail.class::cast)
                            .filter(detail -> detail.getPhaseCode() != phaseCode)
                            .ifPresent(detail -> cloneDetail(detail, usagePoint, now)
                                    .withPhaseCode(phaseCode)
                                    .create()));
        }

        //update usage point life cycle if is present
        updateUsagePointLifeCycle(usagePoint, usagePointConfig);

        com.elster.jupiter.metering.UsagePoint result = processConfigurationEvent(usagePoint);

        retrieveMetrologyContract(result);
        retrieveConnectionState(usagePointConfig).ifPresent(cs -> updateConnectionStateIfNeeded(result, cs));
        return result;
    }

    public void updateUsagePointLifeCycle(com.elster.jupiter.metering.UsagePoint usagePoint, UsagePoint usagePointConfig) throws
            FaultMessage {

        String newLifeCycleName = retrieveOptionalLifeCycle(usagePointConfig);
        if (newLifeCycleName != null) {

            if (meteringService.findUsagePointLifeCycle(newLifeCycleName) == null) {
                throw messageFactory.usagePointConfigFaultMessageSupplier(basicFaultMessage,
                        MessageSeeds.LIFE_CYCLE_NOT_FOUND, newLifeCycleName).get();
            }
            UsagePointLifeCycle newLifeCycle = meteringService.findUsagePointLifeCycle(newLifeCycleName);
            String usagePointStateName = usagePoint.getState()
                    .getName();
            newLifeCycle.getStates()
                    .stream().filter(state -> state.getName().equals(usagePointStateName)).findAny()
                    .orElseThrow(messageFactory.usagePointConfigFaultMessageSupplier(basicFaultMessage,
                            MessageSeeds.NO_STATE_FOUND_IN_LIFE_CYCLE, newLifeCycle.getName(), usagePointStateName));
            usagePoint.setLifeCycle(newLifeCycleName);
            usagePoint.update();
        }

    }

    private void retrieveMetrologyContract(com.elster.jupiter.metering.UsagePoint usagePoint) throws FaultMessage {
        Optional<UsagePoint.MetrologyRequirements> metrologyRequirement = usagePointConfig.getMetrologyRequirements()
                .stream()
                .findFirst();
        if (metrologyRequirement.isPresent()) {
            String name = retrieveMetrologyRequirementElements(metrologyRequirement.get().getNames());

            List<UsagePoint.MetrologyRequirements.Names> metrologyPurposesList = retrieveMetrologyPurposes(metrologyRequirement
                    .get()
                    .getNames());

            UsagePointMetrologyConfiguration usagePointMetrConfig = retrieveMetrologyConfigurationDefault(name);
            if (metrologyPurposesList.size() >= 1) {
                setOptionalMetrologyContract(usagePoint, usagePointMetrConfig, metrologyPurposesList);
            }
        }
    }

    private void setOptionalMetrologyContract(com.elster.jupiter.metering.UsagePoint usagePoint, UsagePointMetrologyConfiguration usagePointMetrConfig, List<UsagePoint.MetrologyRequirements.Names> metrologyPurposesList) throws
            FaultMessage {
        List<MetrologyContract> metrologyMandatoryContractsList = usagePointMetrConfig.getContracts()
                .stream()
                .filter(contract -> contract.isMandatory())
                .collect(Collectors.toList());
        for (MetrologyContract metrologyContract : metrologyMandatoryContractsList) {
            checkPurpose(metrologyContract, metrologyPurposesList);
        }

        List<MetrologyContract> metrologyOptionalContractsList = usagePointMetrConfig.getContracts()
                .stream()
                .filter(contract -> !contract.isMandatory())
                .collect(Collectors.toList());
        for (MetrologyContract metrologyOptionalContract : metrologyOptionalContractsList) {
            if (metrologyPurposesList.stream()
                    .filter(purpose -> purpose.getName()
                            .equals(metrologyOptionalContract.getMetrologyPurpose().getName()))
                    .findFirst()
                    .get()
                    .getNameType()
                    .getDescription()
                    .equals("Active")) {
                activateOptionalMetrologyRequirment(usagePoint, metrologyOptionalContract);
            } else if (metrologyPurposesList.stream()
                    .filter(purpose -> purpose.getName()
                            .equals(metrologyOptionalContract.getMetrologyPurpose().getName()))
                    .findFirst()
                    .get()
                    .getNameType()
                    .getDescription()
                    .equals("Inactive")) {
                deactivateOptionalMetrologyRequirment(usagePoint, metrologyOptionalContract);
            }
        }
    }

    private void activateOptionalMetrologyRequirment(com.elster.jupiter.metering.UsagePoint usagePoint, MetrologyContract mc) throws
            FaultMessage {
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMC = findEffectiveMetrologyConfiguration(usagePoint);
        checkMeterRequirments(usagePoint, mc);
        effectiveMC.activateOptionalMetrologyContract(mc, Instant.now(clock));
    }

    public void deactivateOptionalMetrologyRequirment(com.elster.jupiter.metering.UsagePoint usagePoint, MetrologyContract mc) throws
            FaultMessage {
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMC = findEffectiveMetrologyConfiguration(usagePoint);
        Instant now = clock.instant();
        if (effectiveMC.getChannelsContainer(mc, now).isPresent()) {
            effectiveMC.deactivateOptionalMetrologyContract(mc, Instant.now(clock));
        }
    }

    private void checkMeterRequirments(com.elster.jupiter.metering.UsagePoint usagePoint, MetrologyContract metrologyContract) throws
            FaultMessage {
        List<MeterActivation> meterActivations = usagePoint.getMeterActivations(clock.instant());
        EffectiveMetrologyConfigurationOnUsagePoint metrologyConfigurationOnUsagePoint = usagePoint.getCurrentEffectiveMetrologyConfiguration()
                .orElseThrow(messageFactory.usagePointConfigFaultMessageSupplier(basicFaultMessage,
                        MessageSeeds.NO_METROLOGYCONFIG_FOR_USAGEPOINT, usagePoint.getName()));
        List<MeterRole> meterRolesOfMetrologyConfiguration = metrologyConfigurationOnUsagePoint.getMetrologyConfiguration()
                .getMeterRoles();
        List<Pair<MeterRole, Meter>> metersInRoles = meterActivations.stream()
                .filter(meterActivation -> meterActivation.getMeterRole()
                        .map(meterRolesOfMetrologyConfiguration::contains)
                        .orElse(false))
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
                throw messageFactory.usagePointConfigFaultMessageSupplier(basicFaultMessage,
                        MessageSeeds.UNSATISFIED_READING_TYPE_REQUIREMENTS).get();
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

    private EffectiveMetrologyConfigurationOnUsagePoint findEffectiveMetrologyConfiguration(com.elster.jupiter.metering.UsagePoint usagePoint) throws
            FaultMessage {
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
                return inactivatePurpose(usagePoint);
            default:
                throw new UnsupportedOperationException();
        }
    }

    private com.elster.jupiter.metering.UsagePoint inactivatePurpose(com.elster.jupiter.metering.UsagePoint usagePoint) throws
            FaultMessage {
        String purposeName = retrieveConfigurationEventValue(usagePointConfig);
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMC = findEffectiveMetrologyConfiguration(usagePoint);
        List<MetrologyContract> contractsList = effectiveMC.getMetrologyConfiguration().getContracts();

        for (MetrologyContract metrologyContract : contractsList) {
            if (!metrologyContract.isMandatory() && metrologyContract.getMetrologyPurpose()
                    .getName()
                    .equals(purposeName)) {
                effectiveMC.deactivateOptionalMetrologyContract(metrologyContract, Instant.now());
            } else if (metrologyContract.isMandatory() && metrologyContract.getMetrologyPurpose()
                    .getName()
                    .equals(purposeName)) {
                throw messageFactory.usagePointConfigFaultMessageSupplier(basicFaultMessage,
                        MessageSeeds.INVALID_METROLOGY_CONTRACT_REQUIRMENT, "Found inactive default purpose", 1).get();
            }
        }
        return usagePoint;
    }

    private com.elster.jupiter.metering.UsagePoint activatePurpose(com.elster.jupiter.metering.UsagePoint usagePoint) throws
            FaultMessage {
        String purposeName = retrieveConfigurationEventValue(usagePointConfig);
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMC = findEffectiveMetrologyConfiguration(usagePoint);
        List<MetrologyContract> contractsList = effectiveMC.getMetrologyConfiguration().getContracts();

        for (MetrologyContract metrologyContract : contractsList) {
            if (!metrologyContract.isMandatory() && metrologyContract.getMetrologyPurpose()
                    .getName()
                    .equals(purposeName)) {
                checkMeterRequirments(usagePoint, metrologyContract);
                effectiveMC.activateOptionalMetrologyContract(metrologyContract, Instant.now());
            }
        }
        return usagePoint;
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
                return ConfigurationEventReason.forReason(reason)
                        .filter(ConfigurationEventReason.CHANGE_STATUS::equals)
                        .get(); // only Change Status is supported now
            case ACTIVE_PURPOSE:
                return ConfigurationEventReason.forReason(reason)
                        .filter(ConfigurationEventReason.PURPOSE_ACTIVE::equals)
                        .get();
            case INACTIVE_PURPOSE:
                return ConfigurationEventReason.forReason(reason)
                        .filter(ConfigurationEventReason.PURPOSE_INACTIVE::equals)
                        .get();
            default:
                throw unsupportedValueSupplier("ConfigurationEvents.status.reason", reason,
                        Arrays.stream(ConfigurationEventReason.values()).map(ConfigurationEventReason::getReason).toArray(String[]::new)).get();
        }
    }

    private com.elster.jupiter.metering.UsagePoint performTransition(
            com.elster.jupiter.metering.UsagePoint usagePoint) throws FaultMessage {
        String stateName = retrieveState(usagePointConfig);
        UsagePointTransition transition = findTransitionToState(stateName, usagePoint);
        Optional<ConnectionState> requestedConnectionState = retrieveConnectionState(usagePointConfig);
        Map<String, Object> properties = transition.getMicroActionsProperties().stream()
                .filter(spec -> CONNECTION_STATE_PROPERTY_NAME.equals(spec.getName()))
                .findFirst()
                .map(spec -> (ValueFactory<?>) spec.getValueFactory())
                .flatMap(factory -> requestedConnectionState
                        .map(ConnectionState::name)
                        .map(factory::fromStringValue)
                        .map(Object.class::cast))
                .map(value -> (Map<String, Object>) ImmutableMap.of(CONNECTION_STATE_PROPERTY_NAME, value))
                .orElseGet(Collections::emptyMap);
        List<UsagePointStateChangeFail> failures = usagePointLifeCycleService
                .performTransition(usagePoint, transition, "INS", properties)
                .getFailReasons();
        if (!failures.isEmpty()) {
            throw faultMessage(failures);
        }
        return meteringService.findUsagePointById(usagePoint.getId()) // to re-read information about actual state
                .orElseThrow(IllegalStateException::new);
    }

    private FaultMessage faultMessage(List<UsagePointStateChangeFail> failures) {
        ErrorType[] errors = failures.stream()
                .map(failure -> {
                    switch (failure.getFailSource()) {
                        case CHECK:
                            return replyTypeFactory.errorType(MessageSeeds.TRANSITION_CHECK_FAILED,
                                    failure.getName(), failure.getMessage());
                        case ACTION:
                            return replyTypeFactory.errorType(MessageSeeds.TRANSITION_ACTION_FAILED,
                                    failure.getName(), failure.getMessage());
                    }
                    return null;
                })
                .toArray(ErrorType[]::new);
        return messageFactory.usagePointConfigFaultMessage(basicFaultMessage, errors);
    }

    private Optional<ConnectionState> retrieveConnectionState(UsagePoint usagePointConfig) throws FaultMessage {
        UsagePointConnectedKind connectionState = usagePointConfig.getConnectionState();
        if (connectionState != null) {
            String value = connectionState.value();
            return Optional.of(Arrays.stream(ConnectionState.supportedValues())
                    .filter(cs -> cs.getId().equals(value))
                    .findFirst()
                    .orElseThrow(messageFactory.usagePointConfigFaultMessageSupplier(basicFaultMessage,
                            MessageSeeds.NO_CONNECTION_STATE_FOUND, value)));
        }
        return Optional.empty();
    }

    private UsagePointTransition findTransitionToState(String stateName,
                                                       com.elster.jupiter.metering.UsagePoint usagePoint) throws FaultMessage {
        String stateKey = DefaultUsagePointStateFinder.findForName(stateName)
                .map(DefaultState::getKey)
                .orElse(stateName);
        if (stateKey.equals(usagePoint.getState().getName())) {
            throw messageFactory.usagePointConfigFaultMessageSupplier(basicFaultMessage,
                    MessageSeeds.USAGE_POINT_IS_ALREADY_IN_STATE, stateName).get();
        }
        return usagePointLifeCycleService.getAvailableTransitions(usagePoint, "INS").stream()
                .filter(transition -> transition.getTo().getName().equals(stateKey))
                .findAny()
                .orElseThrow(() -> {
                    MessageSeeds detailedMessageSeed = containsStateWithKey(usagePoint.getLifeCycle(), stateKey) ?
                            MessageSeeds.NO_AVAILABLE_TRANSITION_TO_STATE :
                            MessageSeeds.NO_USAGE_POINT_STATE_WITH_NAME;
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
                .map(ConfigurationEvent::getCreatedDateTime)
                .flatMap(Optional::ofNullable);
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
            nameString = names.stream()
                    .filter(name -> name.getNameType().getName().equals("UsagePointName"))
                    .findFirst()
                    .orElse(null)
                    .getName();
        } else if (names.size() == 1)
            nameString = names.get(0).getName();

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
        if (names.size() > 1) {
            nameString = names.stream()
                    .filter(name -> name.getNameType().getName().equals("UsagePointLifecycleName"))
                    .findFirst().orElse(null).getName();
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
        return meteringService.findUsagePointByName(name)
                .orElseThrow(messageFactory.usagePointConfigFaultMessageSupplier(basicFaultMessage,
                        MessageSeeds.NO_USAGE_POINT_WITH_NAME, name));
    }

    private com.elster.jupiter.metering.ServiceCategory retrieveServiceCategory(UsagePoint usagePointConfig)
            throws FaultMessage {
        ServiceCategory serviceCategory = retrieveMandatoryParameter("ServiceCategory", usagePointConfig::getServiceCategory);
        ServiceKind serviceKind = retrieveMandatoryParameter("ServiceCategory.kind", serviceCategory::getKind);
        com.elster.jupiter.metering.ServiceKind realServiceKind = getServiceKindByValue(serviceKind.value());
        return meteringService.getServiceCategory(realServiceKind)
                .filter(com.elster.jupiter.metering.ServiceCategory::isActive)
                .orElseThrow(messageFactory.usagePointConfigFaultMessageSupplier(basicFaultMessage,
                        MessageSeeds.NO_SERVICE_CATEGORY_FOUND, realServiceKind.getDisplayName(thesaurus)));
    }

    private com.elster.jupiter.metering.ServiceKind getServiceKindByValue(String value) throws FaultMessage {
        return Arrays.stream(com.elster.jupiter.metering.ServiceKind.values())
                .filter(sk -> sk.getDisplayName().equals(value))
                .findFirst()
                .orElseThrow(messageFactory.usagePointConfigFaultMessageSupplier(basicFaultMessage,
                        MessageSeeds.NO_SERVICE_KIND_FOUND, value));
    }

    private Optional<com.elster.jupiter.cbo.PhaseCode> retrievePhaseCode(UsagePoint usagePointConfig) {
        return Optional.ofNullable(usagePointConfig.getPhaseCode())
                .map(PhaseCode::value)
                .map(com.elster.jupiter.cbo.PhaseCode::get);
    }

    private Optional<UsagePointMetrologyConfiguration> retrieveMetrologyConfiguration(UsagePoint usagePointConfig, List<MeterActivation> meterActivationList, Optional<EffectiveMetrologyConfigurationOnUsagePoint> effectiveMC)
            throws FaultMessage {
        Optional<UsagePoint.MetrologyRequirements> metrologyRequirement = usagePointConfig.getMetrologyRequirements().stream()
                .findFirst();
        if (!metrologyRequirement.isPresent()) {
            return Optional.empty();
        }
        String name = retrieveMetrologyRequirementElements(metrologyRequirement.get().getNames());
        if (Checks.is(name).emptyOrOnlyWhiteSpace()) {
            throw emptyUsagePointParameterSupplier("MetrologyRequirements[0].Names[0].name").get();
        }
        List<UsagePoint.MetrologyRequirements.Names> metrologyPurposesList = retrieveMetrologyPurposes(metrologyRequirement
                .get()
                .getNames());
        UsagePointMetrologyConfiguration usagePointMetr = retrieveMetrologyConfigurationDefault(name);
        usagePointMetr = findPurposes(metrologyPurposesList, usagePointMetr, meterActivationList, effectiveMC);

        return Optional.of(usagePointMetr);
    }

    private UsagePointMetrologyConfiguration findPurposes(List<UsagePoint.MetrologyRequirements.Names> metrologyPurposesList, UsagePointMetrologyConfiguration usagePointMetrologyConfiguration, List<MeterActivation> meterActivationList, Optional<EffectiveMetrologyConfigurationOnUsagePoint> effectiveMC) throws
            FaultMessage {
        List<MetrologyContract> metrologyMandatoryContractsList = usagePointMetrologyConfiguration.getContracts()
                .stream()
                .filter(MetrologyContract::isMandatory)
                .collect(Collectors.toList());

        for (MetrologyContract metrologyContract : metrologyMandatoryContractsList) {
            checkPurpose(metrologyContract, metrologyPurposesList);
        }
        List<MetrologyContract> metrologyOptionalContractsList = usagePointMetrologyConfiguration.getContracts()
                .stream()
                .filter(contract -> !contract.isMandatory())
                .collect(Collectors.toList());
        for (MetrologyContract metrologyOptionalContract : metrologyOptionalContractsList) {
            activateOptionalPurpose(metrologyOptionalContract, metrologyPurposesList, meterActivationList, effectiveMC);
        }

        return usagePointMetrologyConfiguration;
    }

    private void activateOptionalPurpose(MetrologyContract contract, List<UsagePoint.MetrologyRequirements.Names> metrologyPurposesList, List<MeterActivation> meterActivationList, Optional<EffectiveMetrologyConfigurationOnUsagePoint> effectiveMC) throws
            FaultMessage {
        if (metrologyPurposesList.stream()
                .filter(p -> p.getNameType().getDescription().equals("Active"))
                .count() != 0) {
            if (metrologyPurposesList.stream()
                    .filter(purpose -> purpose.getName()
                            .equals(contract.getMetrologyPurpose().getName()) && purpose.getNameType()
                            .getDescription()
                            .equals("Active"))
                    .collect(Collectors.toList()).size() != 0 && meterActivationList.size() != 0) {

                effectiveMC.get().activateOptionalMetrologyContract(contract, clock.instant());
            }
        }
    }

    private void checkPurpose(MetrologyContract metrologyContract, List<UsagePoint.MetrologyRequirements.Names> metrologyPurposesList) throws
            FaultMessage {
        if (metrologyPurposesList.stream()
                .filter(purpose -> purpose.getName()
                        .equals(metrologyContract.getMetrologyPurpose().getName()) && purpose.getNameType()
                        .getDescription()
                        .equals("Inactive"))
                .collect(Collectors.toList()).size() != 0) {
            throw messageFactory.usagePointConfigFaultMessageSupplier(basicFaultMessage,
                    MessageSeeds.INVALID_METROLOGY_CONTRACT_REQUIRMENT, "Found inactive default purpose", 1).get();
        }
    }

    private UsagePointMetrologyConfiguration retrieveMetrologyConfigurationDefault(String usagePointMetrologyConfigurationName) throws
            FaultMessage {
        return metrologyConfigurationService.findMetrologyConfiguration(usagePointMetrologyConfigurationName)
                .filter(UsagePointMetrologyConfiguration.class::isInstance)
                .map(UsagePointMetrologyConfiguration.class::cast)
                .orElseThrow(messageFactory.usagePointConfigFaultMessageSupplier(basicFaultMessage,
                        MessageSeeds.NO_METROLOGY_CONFIGURATION_WITH_NAME, usagePointMetrologyConfigurationName));
    }

    private <T> T retrieveMandatoryParameter(String element, Supplier<T> supplier) throws FaultMessage {
        T parameter = supplier.get();
        if (parameter == null) {
            throw missingUsagePointParameterSupplier(element).get();
        }
        return parameter;
    }

    private List<UsagePoint.MetrologyRequirements.Names> retrieveMetrologyPurposes(List<UsagePoint.MetrologyRequirements.Names> names) {
        return names.stream()
                .filter(name -> name.getNameType().getName().equals("Purpose"))
                .collect(Collectors.toList());
    }

    private String retrieveMetrologyRequirementElements(List<UsagePoint.MetrologyRequirements.Names> names) throws
            FaultMessage {
        List<UsagePoint.MetrologyRequirements.Names> metrologyRequirementsList = names;
        if (metrologyRequirementsList.size() >= 1) {
            if (metrologyRequirementsList.stream()
                    .filter(n -> n.getNameType() == null)
                    .collect(Collectors.toList())
                    .size() == 1) {
                return setEmptyNameTypeMetrologyRequirment(metrologyRequirementsList);
            } else if (metrologyRequirementsList.stream()
                    .filter(n -> n.getNameType() == null)
                    .collect(Collectors.toList())
                    .size() > 1) {
                throw messageFactory.usagePointConfigFaultMessageSupplier(basicFaultMessage,
                        MessageSeeds.MORE_THAN_ONE_METROLOGY_CONFIGURATION_SPECIFIED).get();
            } else if (metrologyRequirementsList.stream()
                    .filter(name -> name.getNameType().getName().equals(METROLOGY_CONFIGURATION))
                    .count() == 1) {
                return metrologyRequirementsList.stream()
                        .filter(n -> n.getNameType().getName().equals(METROLOGY_CONFIGURATION))
                        .findAny()
                        .get()
                        .getName();
            } else {
                throw messageFactory.usagePointConfigFaultMessageSupplier(basicFaultMessage,
                        MessageSeeds.MORE_THAN_ONE_METROLOGY_CONFIGURATION_SPECIFIED).get();
            }
        } else {
            throw emptyUsagePointParameterSupplier("MetrologyRequirements[0].Names[0].name").get();
        }
    }

    private String setEmptyNameTypeMetrologyRequirment(List<UsagePoint.MetrologyRequirements.Names> metrologyRequirmentsList) {
        UsagePoint.MetrologyRequirements.Names.NameType value = new UsagePoint.MetrologyRequirements.Names.NameType();
        value.setName(METROLOGY_CONFIGURATION);
        metrologyRequirmentsList.stream()
                .filter(name -> name.getNameType() == null)
                .findFirst()
                .get()
                .setNameType(value);
        return metrologyRequirmentsList.stream()
                .filter(n -> n.getNameType().getName().equals(METROLOGY_CONFIGURATION))
                .findAny()
                .get()
                .getName();
    }

    private <T> T retrieveOneMandatoryElement(String listElement, Supplier<List<T>> supplier) throws FaultMessage {
        return retrieveOneOptionalElement(listElement, supplier)
                .orElseThrow(emptyUsagePointParametersListSupplier(listElement));
    }

    private <T> Optional<T> retrieveOneOptionalElement(String listElement, Supplier<List<T>> supplier) throws FaultMessage {
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

    private Supplier<FaultMessage> missingUsagePointParameterSupplier(String element) {
        return messageFactory.usagePointConfigFaultMessageSupplier(basicFaultMessage,
                MessageSeeds.MISSING_ELEMENT, pathToUsagePoint + '.' + element);
    }

    private Supplier<FaultMessage> emptyUsagePointParametersListSupplier(String element) {
        return messageFactory.usagePointConfigFaultMessageSupplier(basicFaultMessage,
                MessageSeeds.EMPTY_LIST, pathToUsagePoint + '.' + element);
    }

    private Supplier<FaultMessage> emptyUsagePointParameterSupplier(String element) {
        return messageFactory.usagePointConfigFaultMessageSupplier(basicFaultMessage,
                MessageSeeds.EMPTY_ELEMENT, pathToUsagePoint + '.' + element);
    }

    private Supplier<FaultMessage> unsupportedValueSupplier(String element, String value, String... supportedValues) {
        return messageFactory.usagePointConfigFaultMessageSupplier(basicFaultMessage,
                MessageSeeds.UNSUPPORTED_VALUE, pathToUsagePoint + '.' + element, value,
                Arrays.stream(supportedValues).map(each -> '\'' + each + '\'').collect(Collectors.joining(", ")));
    }

    interface PreparedUsagePointBuilder {
        PreparedUsagePointBuilder at(Instant timestamp);

        @TransactionRequired
        com.elster.jupiter.metering.UsagePoint create() throws FaultMessage;

        @TransactionRequired
        com.elster.jupiter.metering.UsagePoint update() throws FaultMessage;

        com.elster.jupiter.metering.UsagePoint get() throws FaultMessage;
    }
}
