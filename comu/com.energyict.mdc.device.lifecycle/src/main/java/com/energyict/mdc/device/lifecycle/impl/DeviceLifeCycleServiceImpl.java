package com.energyict.mdc.device.lifecycle.impl;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.ActionNotPartOfDeviceLifeCycleException;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleActionViolationException;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.config.AuthorizedAction;
import com.energyict.mdc.device.lifecycle.config.AuthorizedBusinessProcessAction;
import com.energyict.mdc.device.lifecycle.config.AuthorizedTransitionAction;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;

import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.User;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.security.Principal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Provides an implementation for the {@link DeviceLifeCycleService} interace.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-20 (15:57)
 */
@Component(name = "com.energyict.device.lifecycle", service = {DeviceLifeCycleService.class, TranslationKeyProvider.class}, property = "name=" + DeviceLifeCycleService.COMPONENT_NAME)
@SuppressWarnings("unused")
public class DeviceLifeCycleServiceImpl implements DeviceLifeCycleService, TranslationKeyProvider {

    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile BpmService bpmService;
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
    public DeviceLifeCycleServiceImpl(NlsService nlsService, ThreadPrincipalService threadPrincipalService, BpmService bpmService, ServerMicroCheckFactory microCheckFactory, ServerMicroActionFactory microActionFactory, DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService) {
        this();
        this.setNlsService(nlsService);
        this.setThreadPrincipalService(threadPrincipalService);
        this.setBpmService(bpmService);
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
    public void triggerExecution(AuthorizedAction action, Device device) throws SecurityException, DeviceLifeCycleActionViolationException {
        this.validateTriggerExecution(action, device);
        if (action instanceof AuthorizedTransitionAction) {
            AuthorizedTransitionAction transitionAction = (AuthorizedTransitionAction) action;
            this.triggerExecution(transitionAction, device);
        }
        else {
            AuthorizedBusinessProcessAction businessProcessAction = (AuthorizedBusinessProcessAction) action;
            this.triggerExecution(businessProcessAction, device);
        }
    }

    private void validateTriggerExecution(AuthorizedAction action, Device device) {
        this.validateActionIsPartOfDeviceLifeCycle(action, device);
        this.validateUserHasExecutePrivilege(action);
    }

    private void validateActionIsPartOfDeviceLifeCycle(AuthorizedAction action, Device device) {
        if (!this.actionIsPartOfDeviceLifeCycle(action, device)) {
            if (action instanceof AuthorizedTransitionAction) {
                AuthorizedTransitionAction transitionAction = (AuthorizedTransitionAction) action;
                throw new ActionNotPartOfDeviceLifeCycleException(transitionAction, device, this.thesaurus, MessageSeeds.TRANSITION_ACTION_NOT_PART_OF_DLC);
            }
            else {
                AuthorizedBusinessProcessAction businessProcessAction = (AuthorizedBusinessProcessAction) action;
                throw new ActionNotPartOfDeviceLifeCycleException(businessProcessAction, device, this.thesaurus, MessageSeeds.BPM_ACTION_NOT_PART_OF_DLC);
            }
        }
    }

    private boolean actionIsPartOfDeviceLifeCycle(AuthorizedAction action, Device device) {
        return device.getDeviceType().getDeviceLifeCycle().getId() == action.getDeviceLifeCycle().getId();
    }

    /**
     * Validates that the user has the privilege to execute the {@link AuthorizedAction}
     * and throws a {@link SecurityException} if not.
     *
     * @throws SecurityException Thrown when the user is not allowed to execute the action
     */
    private void validateUserHasExecutePrivilege(AuthorizedAction action) throws SecurityException {
        MessageSeeds messageSeed = MessageSeeds.NOT_ALLOWED_2_EXECUTE;
        Principal principal = this.threadPrincipalService.getPrincipal();
        if (principal instanceof User) {
            User user = (User) principal;
            action.getLevels()
                    .stream()
                    .filter(level -> this.isAuthorized(level, user))
                    .findFirst()
                    .orElseThrow(() -> newSecurityException(messageSeed));
        }
        else {
            throw newSecurityException(messageSeed);
        }
    }

    private boolean isAuthorized(AuthorizedAction.Level level, User user) {
        Optional<Privilege> privilege = this.deviceLifeCycleConfigurationService.findPrivilege(level.getPrivilege());
        return privilege.isPresent() && user.hasPrivilege(privilege.get());
    }

    private SecurityException newSecurityException(MessageSeeds messageSeed) {
        return new SecurityException(this.thesaurus.getString(messageSeed.getKey(), messageSeed.getDefaultFormat()));
    }

    private void triggerExecution(AuthorizedTransitionAction action, Device device) throws DeviceLifeCycleActionViolationException {
        this.executeMicroChecks(action, device);
        this.executeMicroActions(action, device);
    }

    private void triggerExecution(AuthorizedBusinessProcessAction action, Device device) {
        Map<String, Object> processParameters = new HashMap<>();
        processParameters.put(AuthorizedBusinessProcessAction.ProcessParameterKey.DEVICE.getName(), device.getId());
        this.bpmService.startProcess(action.getDeploymentId(), action.getProcessId(), processParameters);
    }

    private void executeMicroChecks(AuthorizedTransitionAction check, Device device) throws DeviceLifeCycleActionViolationException {
        check.getChecks()
            .stream()
            .map(this.microCheckFactory::from)
            .forEach(a -> this.execute(a, device));
    }

    private void execute(ServerMicroCheck check, Device device) throws DeviceLifeCycleActionViolationException {

    }

    private void executeMicroActions(AuthorizedTransitionAction action, Device device) {
        action.getActions()
            .stream()
            .map(this.microActionFactory::from)
            .forEach(a -> this.execute(a, device));
    }

    private void execute(ServerMicroAction action, Device device) {

    }

}