/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.lifecycle.impl;

import com.elster.jupiter.cbo.DateTimeFormatGenerator;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.fsm.CustomStateTransitionEventType;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTimeSlice;
import com.elster.jupiter.fsm.StateTransitionEventType;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.zone.MeteringZoneService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.validation.ValidationService;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.lifecycle.config.AuthorizedAction;
import com.energyict.mdc.common.device.lifecycle.config.AuthorizedBusinessProcessAction;
import com.energyict.mdc.common.device.lifecycle.config.AuthorizedTransitionAction;
import com.energyict.mdc.common.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.common.device.lifecycle.config.MicroAction;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.lifecycle.ActionDoesNotRelateToDeviceStateException;
import com.energyict.mdc.device.lifecycle.DefaultMicroCheck;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleActionViolationException;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.EffectiveTimestampNotAfterLastStateChangeException;
import com.energyict.mdc.device.lifecycle.EffectiveTimestampNotInRangeException;
import com.energyict.mdc.device.lifecycle.ExecutableAction;
import com.energyict.mdc.device.lifecycle.ExecutableActionProperty;
import com.energyict.mdc.device.lifecycle.ExecutableMicroCheck;
import com.energyict.mdc.device.lifecycle.ExecutableMicroCheckViolation;
import com.energyict.mdc.device.lifecycle.MicroCategoryTranslationKey;
import com.energyict.mdc.device.lifecycle.MultipleMicroCheckViolationsException;
import com.energyict.mdc.device.lifecycle.RequiredMicroActionPropertiesException;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.lifecycle.config.DeviceMicroCheckFactory;
import com.energyict.mdc.device.lifecycle.config.EventType;
import com.energyict.mdc.device.lifecycle.config.Privileges;
import com.energyict.mdc.device.lifecycle.impl.micro.actions.MicroActionTranslationKey;
import com.energyict.mdc.device.lifecycle.impl.micro.checks.DeviceMicroCheckFactoryImpl;
import com.energyict.mdc.device.lifecycle.impl.micro.checks.MicroCheckTranslations;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.device.topology.multielement.MultiElementDeviceService;

import com.google.common.collect.Range;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.security.Principal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.time.Clock;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component(name = "com.energyict.device.lifecycle",
        service = {DeviceLifeCycleService.class, TranslationKeyProvider.class, MessageSeedProvider.class},
        property = "name=" + DeviceLifeCycleService.COMPONENT_NAME)
@SuppressWarnings("unused")
public class DeviceLifeCycleServiceImpl implements DeviceLifeCycleService, TranslationKeyProvider, MessageSeedProvider {
    private volatile DataModel dataModel;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile PropertySpecService propertySpecService;
    private volatile ServerMicroActionFactory microActionFactory;
    private volatile DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;
    private volatile UserService userService;
    private volatile Clock clock;
    private volatile LicenseService licenseService;
    private volatile Thesaurus thesaurus;
    private volatile MeteringService meteringService;
    private volatile EventService eventService;
    private volatile TransactionService transactionService;
    private volatile UpgradeService upgradeService;
    private volatile TopologyService topologyService;
    private volatile MultiElementDeviceService multiElementDeviceService;
    private volatile ValidationService validationService;
    private volatile MeteringZoneService meteringZoneService;
    private volatile ServiceCallService serviceCallService;
    private volatile DeviceMicroCheckFactoryImpl deviceMicroCheckFactory;
    private volatile DeviceService deviceService;
    private Optional<Savepoint> savepoint = Optional.empty();

    // For OSGi purposes
    public DeviceLifeCycleServiceImpl() {
        super();
    }

    // For testing purposes
    @Inject
    public DeviceLifeCycleServiceImpl(NlsService nlsService,
                                      ThreadPrincipalService threadPrincipalService,
                                      PropertySpecService propertySpecService,
                                      ServerMicroActionFactory microActionFactory,
                                      DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService,
                                      UserService userService,
                                      Clock clock,
                                      LicenseService licenseService,
                                      MeteringService meteringService,
                                      EventService eventService,
                                      TransactionService transactionService,
                                      UpgradeService upgradeService,
                                      TopologyService topologyService,
                                      MultiElementDeviceService multiElementDeviceService,
                                      ValidationService validationService,
                                      MeteringZoneService meteringZoneService,
                                      ServiceCallService serviceCallService,
                                      OrmService ormService,
                                      DeviceService deviceService) {
        this();
        setNlsService(nlsService);
        setThreadPrincipalService(threadPrincipalService);
        setPropertySpecService(propertySpecService);
        setMicroActionFactory(microActionFactory);
        setDeviceLifeCycleConfigurationService(deviceLifeCycleConfigurationService);
        setUserService(userService);
        setClock(clock);
        setLicenseService(licenseService);
        setMeteringService(meteringService);
        setEventService(eventService);
        setTransactionService(transactionService);
        setUpgradeService(upgradeService);
        setTopologyService(topologyService);
        setMultiElementDeviceService(multiElementDeviceService);
        setValidationService(validationService);
        setMeteringZoneService(meteringZoneService);
        setServiceCallService(serviceCallService);
        setOrmService(ormService);
        this.setDeviceService(deviceService);
        activate();
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(DeviceLifeCycleService.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Reference
    public void setDeviceLifeCycleConfigurationService(DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService) {
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
    }

    @Reference
    public void setMicroActionFactory(ServerMicroActionFactory microActionFactory) {
        this.microActionFactory = microActionFactory;
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setLicenseService(LicenseService licenseService) {
        this.licenseService = licenseService;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    @Reference
    public void setTopologyService(TopologyService topologyService) {
        this.topologyService = topologyService;
    }

    @Reference
    public void setMultiElementDeviceService(MultiElementDeviceService multiElementDeviceService) {
        this.multiElementDeviceService = multiElementDeviceService;
    }

    @Reference
    public void setValidationService(ValidationService validationService) {
        this.validationService = validationService;
    }

    @Reference
    public void setMeteringZoneService(MeteringZoneService meteringZoneService) {
        this.meteringZoneService = meteringZoneService;
    }

    @Reference
    public void setServiceCallService(ServiceCallService serviceCallService) {
        this.serviceCallService = serviceCallService;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        dataModel = ormService.newDataModel(DeviceLifeCycleService.COMPONENT_NAME, "Device Life Cycle checks & actions");
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Activate
    public void activate() {
        dataModel.register(getModule());
        deviceMicroCheckFactory = dataModel.getInstance(DeviceMicroCheckFactoryImpl.class);
        deviceLifeCycleConfigurationService.addMicroCheckFactory(deviceMicroCheckFactory);
        upgradeService.register(InstallIdentifier.identifier("MultiSense", DeviceLifeCycleService.COMPONENT_NAME),
                dataModel, Installer.class,
                Collections.emptyMap());
    }

    @Deactivate
    public void deactivate() {
        deviceLifeCycleConfigurationService.removeMicroCheckFactory(deviceMicroCheckFactory);
    }

    private Module getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                bind(DataModel.class).toInstance(dataModel);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(TopologyService.class).toInstance(topologyService);
                bind(MultiElementDeviceService.class).toInstance(multiElementDeviceService);
                bind(ValidationService.class).toInstance(validationService);
                bind(Clock.class).toInstance(clock);
                bind(LicenseService.class).toInstance(licenseService);
                bind(MeteringService.class).toInstance(meteringService);
                bind(MeteringZoneService.class).toInstance(meteringZoneService);
                bind(ServiceCallService.class).toInstance(serviceCallService);
                bind(UserService.class).toInstance(userService);
                bind(DeviceLifeCycleConfigurationService.class).toInstance(deviceLifeCycleConfigurationService);
                bind(DeviceLifeCycleService.class).toInstance(DeviceLifeCycleServiceImpl.this);
                bind(DeviceLifeCycleServiceImpl.class).toInstance(DeviceLifeCycleServiceImpl.this);
                bind(DeviceMicroCheckFactory.class).to(DeviceMicroCheckFactoryImpl.class);
            }
        };
    }

    @Override
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Stream.of(
                MicroCategoryTranslationKey.values(),
                MicroActionTranslationKey.values(),
                MicroCheckTranslations.Name.values(),
                MicroCheckTranslations.Description.values(),
                Privileges.values())
                .flatMap(Arrays::stream)
                .collect(Collectors.toList());
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Stream.of(
                MessageSeeds.values(),
                MicroCheckTranslations.Message.values())
                .flatMap(Arrays::stream)
                .collect(Collectors.toList());
    }

    @Override
    public List<ExecutableAction> getExecutableActions(Device device) {
        return device
                .getDeviceType()
                .getDeviceLifeCycle()
                .getAuthorizedActions(device.getState())
                .stream()
                .filter(this::isExecutable)
                .filter(this::userHasExecutePrivilege)
                .map(a -> this.toExecutableAction(a, device))
                .collect(Collectors.toList());
    }

    private ExecutableAction toExecutableAction(AuthorizedAction authorizedAction, Device device) {
        if (authorizedAction instanceof AuthorizedTransitionAction) {
            AuthorizedTransitionAction transitionAction = (AuthorizedTransitionAction) authorizedAction;
            return new ExecutableTransitionActionImpl(device, transitionAction, this);
        } else {
            AuthorizedBusinessProcessAction businessProcessAction = (AuthorizedBusinessProcessAction) authorizedAction;
            return new ExecutableBusinessProcessActionImpl(device, businessProcessAction, this, clock);
        }
    }

    @Override
    public Optional<ExecutableAction> getExecutableActions(Device device, StateTransitionEventType eventType) {
        return this
                .getExecutableActions(device)
                .stream()
                .filter(each -> isTransitionAction(each, eventType))
                .findAny();
    }

    private boolean isTransitionAction(ExecutableAction executableAction, StateTransitionEventType eventType) {
        if (executableAction.getAction() instanceof AuthorizedTransitionAction) {
            AuthorizedTransitionAction transitionAction = (AuthorizedTransitionAction) executableAction.getAction();
            return transitionAction.getStateTransition().getEventType().getId() == eventType.getId();
        } else {
            return false;
        }
    }

    @Override
    public List<PropertySpec> getPropertySpecsFor(MicroAction action) {
        return this.microActionFactory.from(action).getPropertySpecs(this.propertySpecService);
    }

    @Override
    public ExecutableActionProperty toExecutableActionProperty(Object value, PropertySpec propertySpec) throws InvalidValueException {
        propertySpec.validateValueIgnoreRequired(value);
        return new ExecutableActionPropertyImpl(propertySpec, value);
    }

    @Override
    public void execute(AuthorizedTransitionAction action, Device device, Instant effectiveTimestamp, List<ExecutableActionProperty> properties) throws
            SecurityException, DeviceLifeCycleActionViolationException {
        this.setSavepoint();
        this.validateTriggerExecution(action, device, effectiveTimestamp, properties);
        this.triggerExecution(action, device, effectiveTimestamp, properties);
    }

    @Override
    public void execute(AuthorizedBusinessProcessAction action, Device device, Instant effectiveTimestamp) throws SecurityException, DeviceLifeCycleActionViolationException {
        this.setSavepoint();
        this.validateTriggerExecution(action, device);
        this.triggerExecution(action, device);
    }

    private void validateTriggerExecution(AuthorizedAction action, Device device) {
        this.validateActionSourceIsDeviceCurrentState(action, device);
        this.validateUserHasExecutePrivilege(action);
    }

    private void validateActionSourceIsDeviceCurrentState(AuthorizedAction action, Device device) {
        if (action.getState().getId() != device.getState().getId()) {
            if (action instanceof AuthorizedTransitionAction) {
                AuthorizedTransitionAction transitionAction = (AuthorizedTransitionAction) action;
                ActionDoesNotRelateToDeviceStateException exception = new ActionDoesNotRelateToDeviceStateException(transitionAction, device, this.thesaurus, MessageSeeds.TRANSITION_ACTION_SOURCE_IS_NOT_CURRENT_STATE);
                postEventForTransitionFailed(action, device, exception.getLocalizedMessage());
                throw exception;
            } else {
                AuthorizedBusinessProcessAction businessProcessAction = (AuthorizedBusinessProcessAction) action;
                ActionDoesNotRelateToDeviceStateException exception = new ActionDoesNotRelateToDeviceStateException(businessProcessAction, device, this.thesaurus, MessageSeeds.BPM_ACTION_SOURCE_IS_NOT_CURRENT_STATE);
                postEventForTransitionFailed(action, device, exception.getLocalizedMessage());
                throw exception;
            }
        }
    }

    /**
     * Validates that the user has the privilege to execute the {@link AuthorizedAction}
     * and throws a {@link SecurityException} if not.
     *
     * @throws SecurityException Thrown when the user is not allowed to execute the action
     */
    private void validateUserHasExecutePrivilege(AuthorizedAction action) throws SecurityException {
        if (!this.userHasExecutePrivilege(action)) {
            throw newSecurityException(MessageSeeds.NOT_ALLOWED_2_EXECUTE);
        }
    }

    private void validateTriggerExecution(AuthorizedTransitionAction action, Device device, Instant effectiveTimestamp, List<ExecutableActionProperty> properties) {
        this.validateTriggerExecution(action, device);
        this.valueAvailableForAllRequiredProperties(action, device, properties);
        this.validateExecutionTimestamp(action, device, effectiveTimestamp);
    }

    private void valueAvailableForAllRequiredProperties(AuthorizedTransitionAction action, Device device, List<ExecutableActionProperty> properties) {
        Set<String> propertySpecNames =
                properties
                        .stream()
                        .map(ExecutableActionProperty::getPropertySpec)
                        .map(PropertySpec::getName)
                        .collect(Collectors.toSet());
        Set<String> missingRequiredPropertySpecNames = action
                .getActions()
                .stream()
                .flatMap(ma -> this.getPropertySpecsFor(ma).stream())
                .filter(PropertySpec::isRequired)
                .map(PropertySpec::getName)
                .filter(each -> !propertySpecNames.contains(each))
                .collect(Collectors.toSet());
        if (!missingRequiredPropertySpecNames.isEmpty()) {
            RequiredMicroActionPropertiesException exception = new RequiredMicroActionPropertiesException(this.thesaurus, MessageSeeds.MISSING_REQUIRED_PROPERTY_VALUES, missingRequiredPropertySpecNames);
            postEventForTransitionFailed(action, device, exception.getLocalizedMessage());
            throw exception;
        }
    }

    private void validateExecutionTimestamp(AuthorizedTransitionAction action, Device device, Instant effectiveTimestamp) {
        Optional<Instant> lastStateChangeTimestamp = this.getLastStateChangeTimestamp(device);
        this.effectiveTimestampAfterLastStateChange(effectiveTimestamp, action, device, lastStateChangeTimestamp);
        this.effectiveTimestampIsInRange(effectiveTimestamp, device, action, lastStateChangeTimestamp);
    }

    private Optional<Instant> getLastStateChangeTimestamp(Device device) {
        List<StateTimeSlice> stateTimeSlices = device.getStateTimeline().getSlices();
        return this.lastSlice(stateTimeSlices).map(lastSlice -> lastSlice.getPeriod().lowerEndpoint());
    }

    private Optional<StateTimeSlice> lastSlice(List<StateTimeSlice> stateTimeSlices) {
        if (stateTimeSlices.isEmpty()) {
            // MDC device always have at least one state
            return Optional.empty();
        } else {
            return Optional.of(stateTimeSlices.get(stateTimeSlices.size() - 1));
        }
    }

    private void effectiveTimestampAfterLastStateChange(Instant effectiveTimestamp, AuthorizedTransitionAction action, Device device, Optional<Instant> lastStateChangeTimestamp) {
        if (lastStateChangeTimestamp.isPresent() && !effectiveTimestamp.isAfter(lastStateChangeTimestamp.get())) {
            EffectiveTimestampNotAfterLastStateChangeException exception = new EffectiveTimestampNotAfterLastStateChangeException(this.thesaurus, MessageSeeds.EFFECTIVE_TIMESTAMP_NOT_AFTER_LAST_STATE_CHANGE,
                    device, effectiveTimestamp, lastStateChangeTimestamp.get(), getLongDateFormatForCurrentUser());
            postEventForTransitionFailed(action, device, exception.getLocalizedMessage());
            throw exception;
        }
    }

    private void effectiveTimestampIsInRange(Instant effectiveTimestamp, Device device, AuthorizedTransitionAction action, Optional<Instant> lastStateChangeTimestamp) {
        DeviceLifeCycle deviceLifeCycle = action.getDeviceLifeCycle();
        Instant lowerBound = deviceLifeCycle.getMaximumPastEffectiveTimestamp().atZone(this.clock.getZone()).truncatedTo(ChronoUnit.DAYS).toInstant();
        if (lastStateChangeTimestamp.isPresent() && lowerBound.isBefore(lastStateChangeTimestamp.get())) {
            lowerBound = lastStateChangeTimestamp.get();
        }
        Instant upperBound = deviceLifeCycle.getMaximumFutureEffectiveTimestamp().atZone(this.clock.getZone()).truncatedTo(ChronoUnit.DAYS).plus(1, ChronoUnit.DAYS).toInstant();
        Range<Instant> range = Range.closedOpen(lowerBound, upperBound);
        if (!range.contains(effectiveTimestamp)) {
            EffectiveTimestampNotInRangeException exception = new EffectiveTimestampNotInRangeException(this.thesaurus, MessageSeeds.EFFECTIVE_TIMESTAMP_NOT_IN_RANGE,
                    lowerBound, upperBound, getLongDateFormatForCurrentUser());
            postEventForTransitionFailed(action, device, exception.getLocalizedMessage());
            throw exception;
        }
    }

    /**
     * Tests if the {@link AuthorizedAction} is executable,
     * i.e. if it can be passed to one of the triggerAction method.
     *
     * @param action The AuthorizedAction
     * @return A flag that indicates if the AuthorizedAction is compatible with one of the triggerExecution methods
     */
    private boolean isExecutable(AuthorizedAction action) {
        return action instanceof AuthorizedBusinessProcessAction
                || (action instanceof AuthorizedTransitionAction
                && ((AuthorizedTransitionAction) action).getStateTransition().getEventType() instanceof CustomStateTransitionEventType);
    }

    private boolean userHasExecutePrivilege(AuthorizedAction action) throws SecurityException {
        Principal principal = this.threadPrincipalService.getPrincipal();
        if (principal instanceof User) {
            User user = (User) principal;
            return action.getLevels()
                    .stream()
                    .anyMatch(level -> this.isAuthorized(level, user));
        } else {
            return false;
        }
    }

    private boolean isAuthorized(AuthorizedAction.Level level, User user) {
        Optional<Privilege> privilege = this.deviceLifeCycleConfigurationService.findInitiateActionPrivilege(level.getPrivilege());
        return privilege.isPresent() && user.hasPrivilege("MDC", privilege.get());
    }

    private SecurityException newSecurityException(MessageSeeds messageSeed) {
        return new SecurityException(this.thesaurus.getFormat(messageSeed).format());
    }

    private void triggerExecution(AuthorizedTransitionAction action, Device device, Instant effectiveTimestamp, List<ExecutableActionProperty> properties) throws
            DeviceLifeCycleActionViolationException {
        this.executeMicroChecks(action, device, effectiveTimestamp);
        this.executeMicroActions(action, device, effectiveTimestamp, properties);
        this.triggerEvent((CustomStateTransitionEventType) action.getStateTransition().getEventType(), device, effectiveTimestamp);
    }

    private void triggerExecution(AuthorizedBusinessProcessAction action, Device device) {
        action.getTransitionBusinessProcess().executeOn(device.getId(), action.getState());
    }

    private void executeMicroChecks(AuthorizedTransitionAction action, Device device, Instant effectiveTimestamp) throws DeviceLifeCycleActionViolationException {
        List<ExecutableMicroCheckViolation> violations = action.getChecks()
                .stream()
                .map(check -> check instanceof ExecutableMicroCheck ?
                        (ExecutableMicroCheck) check :
                        new ExecutableMicroCheck() {
                            @Override
                            public Optional<ExecutableMicroCheckViolation> execute(Device device, Instant effectiveTimestamp, State toState) {
                                return Optional.of(new ExecutableMicroCheckViolation(this, thesaurus.getSimpleFormat(MessageSeeds.MICRO_CHECK_NOT_EXECUTABLE).format()));
                            }

                            @Override
                            public String getKey() {
                                return check.getKey();
                            }

                            @Override
                            public String getName() {
                                return check.getName();
                            }

                            @Override
                            public String getDescription() {
                                return check.getDescription();
                            }

                            @Override
                            public String getCategory() {
                                return check.getCategory();
                            }

                            @Override
                            public String getCategoryName() {
                                return check.getCategoryName();
                            }
                        })
                .map(check -> check.execute(device, effectiveTimestamp, action.getStateTransition().getTo()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        if (!violations.isEmpty()) {
            MultipleMicroCheckViolationsException exception = new MultipleMicroCheckViolationsException(this.thesaurus, MessageSeeds.MULTIPLE_MICRO_CHECKS_FAILED, violations);
            postEventForTransitionFailed(action, device, exception.getLocalizedMessage());
            throw exception;
        }
    }

    private void executeMicroActions(AuthorizedTransitionAction action, Device device, Instant effectiveTimestamp, List<ExecutableActionProperty> properties) {
        action.getActions()
                .stream()
                .map(this.microActionFactory::from)
                .forEach(a ->
                        deviceService
                                .findDeviceById(device.getId())
                                .ifPresent(modDevice ->
                                        this.execute(a, modDevice, effectiveTimestamp, properties))
                );
    }

    private void execute(ServerMicroAction action, Device device, Instant effectiveTimestamp, List<ExecutableActionProperty> properties) {
        action.execute(device, effectiveTimestamp, properties);
    }

    @Override
    public void triggerEvent(CustomStateTransitionEventType eventType, Device device, Instant effectiveTimestamp) {
        this.toEndDevice(device).ifPresent(endDevice -> {
            eventType
                    .newInstance(
                            device.getDeviceType().getDeviceLifeCycle().getFiniteStateMachine(),
                            String.valueOf(endDevice.getId()),
                            Device.class.getName(),
                            device.getState().getName(),
                            effectiveTimestamp,
                            Collections.emptyMap())
                    .publish();
        });
        postEventForTransitionDone(device);

    }

    @Override
    public String getKey(DefaultMicroCheck microCheck) {
        return deviceMicroCheckFactory.from(microCheck).getKey();
    }

    @Override
    public String getName(DefaultMicroCheck microCheck) {
        return deviceMicroCheckFactory.from(microCheck).getName();
    }

    @Override
    public String getDescription(DefaultMicroCheck microCheck) {
        return deviceMicroCheckFactory.from(microCheck).getDescription();
    }

    @Override
    public String getCategoryName(DefaultMicroCheck microCheck) {
        return deviceMicroCheckFactory.from(microCheck).getCategoryName();
    }

    @Override
    public String getName(MicroAction microAction) {
        return microActionFactory.from(microAction).getName();
    }

    @Override
    public String getDescription(MicroAction microAction) {
        return microActionFactory.from(microAction).getDescription();
    }

    @Override
    public String getCategoryName(MicroAction microAction) {
        return microActionFactory.from(microAction).getCategoryName();
    }

    private DateTimeFormatter getLongDateFormatForCurrentUser() {
        Principal principal = threadPrincipalService.getPrincipal();
        return DateTimeFormatGenerator.getDateFormatForUser(
                DateTimeFormatGenerator.Mode.LONG,
                DateTimeFormatGenerator.Mode.LONG,
                this.userService.getUserPreferencesService(),
                this.threadPrincipalService.getPrincipal());
    }

    private Optional<EndDevice> toEndDevice(Device device) {
        Optional<AmrSystem> amrSystem = this.getMdcAmrSystem();
        if (amrSystem.isPresent()) {
            return this.findEndDevice(amrSystem.get(), device);
        } else {
            return Optional.empty();
        }
    }

    private Optional<AmrSystem> getMdcAmrSystem() {
        return this.meteringService.findAmrSystem(KnownAmrSystem.MDC.getId());
    }

    private Optional<EndDevice> findEndDevice(AmrSystem amrSystem, Device device) {
        return amrSystem.findMeter(String.valueOf(device.getId())).map(EndDevice.class::cast);
    }

    /**
     * Create an event for a Failed Transition.<br>
     * We don't want to do rollbacks before saving the failure message into DB, otherwise we'll get a
     * TransactionRequiredException
     *
     * @param action the authorize action
     * @param device the device
     * @param cause  the cause
     */
    private void postEventForTransitionFailed(AuthorizedAction action, Device device, String cause) {
        if (transactionService.isInTransaction()) {
            this.rollback();
        }
        eventService.postEvent(EventType.TRANSITION_FAILED.topic(),
                TransitionFailedEventInfo.forFailure(action, device, cause, Instant.now(clock)));
    }

    private void postEventForTransitionDone(Device device) {
        eventService.postEvent(EventType.TRANSITION_DONE.topic(), TransitionDoneEventInfo.forDevice(device, Instant.now(clock)));

    }

    public String getLocalizedMessage(MessageSeed seed, String message) {
        return getFormat(seed).format(message);
    }

    private NlsMessageFormat getFormat(MessageSeed seed) {
        return this.thesaurus.getSimpleFormat(seed);
    }

    private void setSavepoint() {
        savepoint = Optional.empty();
        if (transactionService.isInTransaction()) {
            try (Connection connection = dataModel.getConnection(false)) {
                savepoint = Optional.of(connection.setSavepoint());
            } catch (SQLException e) {
                throw new UnderlyingSQLFailedException(e);
            }
        }
    }

    private void rollback() {
        if (transactionService.isInTransaction() && savepoint.isPresent()) {
            try (Connection connection = dataModel.getConnection(false)) {
                connection.rollback(savepoint.get());
                savepoint = Optional.empty();
            } catch (SQLException e) {
                throw new UnderlyingSQLFailedException(e);
            }

        }
    }
}
