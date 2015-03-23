package com.energyict.mdc.device.lifecycle.impl;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.ActionNotPartOfDeviceLifeCycleException;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleActionViolationException;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.config.AuthorizedTransitionAction;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

/**
 * Provides an implementation for the {@link DeviceLifeCycleService} interace.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-20 (15:57)
 */
@Component(name = "com.energyict.device.lifecycle", service = {DeviceLifeCycleService.class, TranslationKeyProvider.class}, property = "name=" + DeviceLifeCycleService.COMPONENT_NAME)
@SuppressWarnings("unused")
public class DeviceLifeCycleServiceImpl implements DeviceLifeCycleService, TranslationKeyProvider {

    private volatile NlsService nlsService;
    private volatile ServerMicroCheckFactory microCheckFactory;
    private volatile ServerMicroActionFactory microActionFactory;
    private Thesaurus thesaurus;

    // For OSGi purposes
    public DeviceLifeCycleServiceImpl() {
        super();
    }

    // For testing purposes
    @Inject
    public DeviceLifeCycleServiceImpl(NlsService nlsService, ServerMicroCheckFactory microCheckFactory, ServerMicroActionFactory microActionFactory) {
        this();
        this.setNlsService(nlsService);
        this.setMicroCheckFactory(microCheckFactory);
        this.setMicroActionFactory(microActionFactory);
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        this.thesaurus = nlsService.getThesaurus(DeviceLifeCycleService.COMPONENT_NAME, Layer.DOMAIN);
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
    public void execute(AuthorizedTransitionAction action, Device device) throws DeviceLifeCycleActionViolationException {
        if (this.actionIsPartOfDeviceLifeCycle(action, device)) {
            this.executeMicroChecks(action, device);
            this.executeMicroActions(action, device);
        }
        else {
            throw new ActionNotPartOfDeviceLifeCycleException(action, device, this.thesaurus, MessageSeeds.ACTION_NOT_PART_OF_DLC);
        }
    }

    private boolean actionIsPartOfDeviceLifeCycle(AuthorizedTransitionAction action, Device device) {
        return device.getDeviceType().getDeviceLifeCycle().getId() == action.getDeviceLifeCycle().getId();
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