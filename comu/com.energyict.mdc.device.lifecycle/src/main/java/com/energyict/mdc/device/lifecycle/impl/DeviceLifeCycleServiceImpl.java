package com.energyict.mdc.device.lifecycle.impl;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.ActionDoesNotRelateToDeviceStateException;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleActionViolation;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleActionViolationException;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.ExecutableAction;
import com.energyict.mdc.device.lifecycle.ExecutableActionProperty;
import com.energyict.mdc.device.lifecycle.MultipleMicroCheckViolationsException;
import com.energyict.mdc.device.lifecycle.RequiredMicroActionPropertiesException;
import com.energyict.mdc.device.lifecycle.config.AuthorizedAction;
import com.energyict.mdc.device.lifecycle.config.AuthorizedBusinessProcessAction;
import com.energyict.mdc.device.lifecycle.config.AuthorizedStandardTransitionAction;
import com.energyict.mdc.device.lifecycle.config.AuthorizedTransitionAction;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.lifecycle.config.MicroAction;

import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.fsm.CustomStateTransitionEventType;
import com.elster.jupiter.fsm.StateTransitionEventType;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.streams.Functions;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Provides an implementation for the {@link DeviceLifeCycleService} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-20 (15:57)
 */
@Component(name = "com.energyict.device.lifecycle", service = {DeviceLifeCycleService.class, TranslationKeyProvider.class}, property = "name=" + DeviceLifeCycleService.COMPONENT_NAME)
@SuppressWarnings("unused")
public class DeviceLifeCycleServiceImpl implements DeviceLifeCycleService, TranslationKeyProvider {

    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile BpmService bpmService;
    private volatile PropertySpecService propertySpecService;
    private volatile ServerMicroCheckFactory microCheckFactory;
    private volatile ServerMicroActionFactory microActionFactory;
    private volatile DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;
    private Thesaurus thesaurus;

    // For OSGi purposes
    public DeviceLifeCycleServiceImpl() {
        super();
    }

    // For testing purposes
    @Inject
    public DeviceLifeCycleServiceImpl(NlsService nlsService, ThreadPrincipalService threadPrincipalService, BpmService bpmService, PropertySpecService propertySpecService, ServerMicroCheckFactory microCheckFactory, ServerMicroActionFactory microActionFactory, DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService) {
        this();
        this.setNlsService(nlsService);
        this.setThreadPrincipalService(threadPrincipalService);
        this.setBpmService(bpmService);
        this.setPropertySpecService(propertySpecService);
        this.setMicroCheckFactory(microCheckFactory);
        this.setMicroActionFactory(microActionFactory);
        this.setDeviceLifeCycleConfigurationService(deviceLifeCycleConfigurationService);
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(DeviceLifeCycleService.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Reference
    public void setBpmService(BpmService bpmService) {
        this.bpmService = bpmService;
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
    public void setMicroCheckFactory(ServerMicroCheckFactory microCheckFactory) {
        this.microCheckFactory = microCheckFactory;
    }

    @Reference
    public void setMicroActionFactory(ServerMicroActionFactory microActionFactory) {
        this.microActionFactory = microActionFactory;
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
        return Arrays.asList(MessageSeeds.values());
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
        }
        else {
            AuthorizedBusinessProcessAction businessProcessAction = (AuthorizedBusinessProcessAction) authorizedAction;
            return new ExecutableBusinessProcessActionImpl(device, businessProcessAction, this);
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
        }
        else {
            return false;
        }
    }

    @Override
    public List<PropertySpec> getPropertySpecsFor(MicroAction action) {
        return this.microActionFactory.from(action).getPropertySpecs(this.propertySpecService);
    }

    @Override
    public void execute(AuthorizedTransitionAction action, Device device, List<ExecutableActionProperty> properties) throws SecurityException, DeviceLifeCycleActionViolationException {
        this.validateTriggerExecution(action, device, properties);
        this.triggerExecution(action, device, properties);
    }

    @Override
    public void execute(AuthorizedBusinessProcessAction action, Device device) throws SecurityException, DeviceLifeCycleActionViolationException {
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
                throw new ActionDoesNotRelateToDeviceStateException(transitionAction, device, this.thesaurus, MessageSeeds.TRANSITION_ACTION_SOURCE_IS_NOT_CURRENT_STATE);
            }
            else {
                AuthorizedBusinessProcessAction businessProcessAction = (AuthorizedBusinessProcessAction) action;
                throw new ActionDoesNotRelateToDeviceStateException(businessProcessAction, device, this.thesaurus, MessageSeeds.BPM_ACTION_SOURCE_IS_NOT_CURRENT_STATE);
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
        MessageSeeds messageSeed = MessageSeeds.NOT_ALLOWED_2_EXECUTE;
        if (!this.userHasExecutePrivilege(action)) {
            throw newSecurityException(messageSeed);
        }
    }

    private void validateTriggerExecution(AuthorizedTransitionAction action, Device device, List<ExecutableActionProperty> properties) {
        this.validateTriggerExecution(action, device);
        this.valueAvailableForAllRequiredProperties(action, properties);
    }

    private void valueAvailableForAllRequiredProperties(AuthorizedTransitionAction action, List<ExecutableActionProperty> properties) {
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
            throw new RequiredMicroActionPropertiesException(this.thesaurus, MessageSeeds.MISSING_REQUIRED_PROPERTY_VALUES, missingRequiredPropertySpecNames);
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
        return action instanceof AuthorizedStandardTransitionAction
            || action instanceof AuthorizedBusinessProcessAction;
    }

    private boolean userHasExecutePrivilege(AuthorizedAction action) throws SecurityException {
        Principal principal = this.threadPrincipalService.getPrincipal();
        if (principal instanceof User) {
            User user = (User) principal;
            Optional<AuthorizedAction.Level> any = action.getLevels()
                    .stream()
                    .filter(level -> this.isAuthorized(level, user))
                    .findAny();
            return any.isPresent();
        }
        else {
            return false;
        }
    }

    private boolean isAuthorized(AuthorizedAction.Level level, User user) {
        Optional<Privilege> privilege = this.deviceLifeCycleConfigurationService.findInitiateActionPrivilege(level.getPrivilege());
        return privilege.isPresent() && user.hasPrivilege(privilege.get());
    }

    private SecurityException newSecurityException(MessageSeeds messageSeed) {
        return new SecurityException(this.thesaurus.getString(messageSeed.getKey(), messageSeed.getDefaultFormat()));
    }

    private void triggerExecution(AuthorizedTransitionAction action, Device device, List<ExecutableActionProperty> properties) throws DeviceLifeCycleActionViolationException {
        this.executeMicroChecks(action, device);
        this.executeMicroActions(action, device, properties);
        this.triggerEvent((CustomStateTransitionEventType) action.getStateTransition().getEventType(), device);
    }

    private void triggerExecution(AuthorizedBusinessProcessAction action, Device device) {
        Map<String, Object> processParameters = new HashMap<>();
        processParameters.put(AuthorizedBusinessProcessAction.ProcessParameterKey.DEVICE.getName(), device.getId());
        this.bpmService.startProcess(action.getDeploymentId(), action.getProcessId(), processParameters);
    }

    private void executeMicroChecks(AuthorizedTransitionAction check, Device device) throws DeviceLifeCycleActionViolationException {
        List<DeviceLifeCycleActionViolation> violations =
            check.getChecks()
                .stream()
                .map(this.microCheckFactory::from)
                .map(microCheck -> this.execute(microCheck, device))
                .flatMap(Functions.asStream())
                .collect(Collectors.toList());
        if (!violations.isEmpty()) {
            throw new MultipleMicroCheckViolationsException(this.thesaurus, MessageSeeds.MULTIPLE_MICRO_CHECKS_FAILED, violations);
        }
    }

    /**
     * Executes the {@link ServerMicroCheck} against the {@link Device}
     * and returns a {@link DeviceLifeCycleActionViolation}
     * when the ServerMicroCheck fails.
     *
     * @param check The ServerMicroCheck
     * @param device The Device
     * @return The violation or an empty Optional if the ServerMicroCheck succeeds
     */
    private Optional<DeviceLifeCycleActionViolation> execute(ServerMicroCheck check, Device device) {
        return check.evaluate(device);
    }

    private void executeMicroActions(AuthorizedTransitionAction action, Device device, List<ExecutableActionProperty> properties) {
        action.getActions()
            .stream()
            .map(this.microActionFactory::from)
            .forEach(a -> this.execute(a, device, properties));
    }

    private void execute(ServerMicroAction action, Device device, List<ExecutableActionProperty> properties) {
        action.execute(device, properties);
    }

    @Override
    public void triggerEvent(CustomStateTransitionEventType eventType, Device device) {
        eventType
            .newInstance(
                    device.getDeviceType().getDeviceLifeCycle().getFiniteStateMachine(),
                    String.valueOf(device.getId()),
                    device.getState().getName(),
                    Collections.emptyMap())
            .publish();
    }

}