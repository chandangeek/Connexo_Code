/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cim.webservices.inbound.soap.usagepointconfig;

import com.elster.jupiter.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.elster.jupiter.cim.webservices.inbound.soap.impl.ReplyTypeFactory;
import com.elster.jupiter.metering.ConnectionState;
import com.elster.jupiter.metering.ElectricityDetail;
import com.elster.jupiter.metering.ElectricityDetailBuilder;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePointConnectionState;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
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
import java.util.function.Supplier;
import java.util.stream.Collectors;

class UsagePointBuilder {
    private static final String CONNECTION_STATE_PROPERTY_NAME = "set.connection.state.property.name";
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

        retrieveMetrologyConfiguration(usagePointConfig).ifPresent(mc -> usagePoint.apply(mc, creationDate));

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
        // in case connection state has not been changed in scope of transition
        retrieveConnectionState(usagePointConfig).ifPresent(cs -> updateConnectionStateIfNeeded(result, cs));
        return result;
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
            default:
                throw new UnsupportedOperationException();
        }
    }

    private ConfigurationEventReason retrieveReason(ConfigurationEvent event) throws FaultMessage {
        String reason = event.getReason();
        if (reason == null) {
            throw missingUsagePointParameterSupplier("ConfigurationEvents.reason").get();
        }
        return ConfigurationEventReason.forReason(reason)
                .filter(ConfigurationEventReason.CHANGE_STATUS::equals) // only Change Status is supported now
                .orElseThrow(unsupportedValueSupplier("ConfigurationEvents.reason", reason, ConfigurationEventReason.CHANGE_STATUS.getReason()));
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

    private static boolean containsStateWithKey(UsagePointLifeCycle lifeCycle, String stateKey) {
        return lifeCycle.getStates().stream()
                .anyMatch(state -> state.getName().equals(stateKey));
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
        String nameString;
        if (names.size() > 1) {
            if (names.stream().filter(name -> name.getNameType().getName().equals("UsagePointName")).count() > 0) {
                nameString = names.stream()
                        .filter(name -> name.getNameType().getName().equals("UsagePointName"))
                        .findFirst()
                        .orElse(null)
                        .getName();
            } else {
                nameString = names.stream()
                        .filter(name -> name.getNameType().getName().equals(null))
                        .findFirst()
                        .orElse(null)
                        .getName();
            }
        } else {
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

    private Optional<UsagePointMetrologyConfiguration> retrieveMetrologyConfiguration(UsagePoint usagePointConfig)
            throws FaultMessage {
        Optional<UsagePoint.MetrologyRequirements> metrologyRequirement = usagePointConfig.getMetrologyRequirements().stream()
                .findFirst();
        if (!metrologyRequirement.isPresent()) {
            return Optional.empty();
        }
        String name = retrieveOneMandatoryElement("MetrologyRequirements[0].Names", metrologyRequirement.get()::getNames).getName();
        if (Checks.is(name).emptyOrOnlyWhiteSpace()) {
            throw emptyUsagePointParameterSupplier("MetrologyRequirements[0].Names[0].name").get();
        }
        return Optional.of(metrologyConfigurationService.findMetrologyConfiguration(name)
                .filter(UsagePointMetrologyConfiguration.class::isInstance)
                .map(UsagePointMetrologyConfiguration.class::cast)
                .orElseThrow(messageFactory.usagePointConfigFaultMessageSupplier(basicFaultMessage,
                        MessageSeeds.NO_METROLOGY_CONFIGURATION_WITH_NAME, name)));
    }

    private <T> T retrieveMandatoryParameter(String element, Supplier<T> supplier) throws FaultMessage {
        T parameter = supplier.get();
        if (parameter == null) {
            throw missingUsagePointParameterSupplier(element).get();
        }
        return parameter;
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
