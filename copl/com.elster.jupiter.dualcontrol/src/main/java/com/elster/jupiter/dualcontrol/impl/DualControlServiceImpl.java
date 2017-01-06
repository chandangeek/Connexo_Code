package com.elster.jupiter.dualcontrol.impl;

import com.elster.jupiter.dualcontrol.DualControlService;
import com.elster.jupiter.dualcontrol.Monitor;
import com.elster.jupiter.dualcontrol.Privileges;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.exception.MessageSeed;

import com.google.inject.AbstractModule;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component(name = "com.elster.jupiter.dualcontrol", service = {DualControlService.class, MessageSeedProvider.class, TranslationKeyProvider.class}, property = {"name=" + DualControlService.COMPONENT_NAME}, immediate = true)
public class DualControlServiceImpl implements DualControlService, MessageSeedProvider, TranslationKeyProvider {

    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile DataModel dataModel;
    private volatile UpgradeService upgradeService;
    private volatile UserService userService;

    public DualControlServiceImpl() {
    }

    @Inject
    public DualControlServiceImpl(ThreadPrincipalService threadPrincipalService, OrmService ormService, UserService userService, UpgradeService upgradeService) {
        setThreadPrincipalService(threadPrincipalService);
        setOrmService(ormService);
        setUserService(userService);
        setUpgradeService(upgradeService);
        activate();
    }

    @Activate
    public void activate() {
        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(ThreadPrincipalService.class).toInstance(threadPrincipalService);
                bind(DataModel.class).toInstance(dataModel);
                bind(UserService.class).toInstance(userService);
            }
        });
        upgradeService.register(InstallIdentifier.identifier("Pulse", COMPONENT_NAME), dataModel, Installer.class, Collections.emptyMap());
    }

    @Override
    public Monitor createMonitor() {
        MonitorImpl monitor = new MonitorImpl(dataModel, threadPrincipalService);
        dataModel.persist(monitor);
        return monitor;
    }

    @Override
    public Optional<Monitor> getMonitor(long id) {
        return dataModel.mapper(Monitor.class).getOptional(id);
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
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

    @Override
    public List<TranslationKey> getKeys() {
        List<TranslationKey> keys = new ArrayList<>();
        keys.addAll(Arrays.asList(Privileges.values()));
        return keys;
    }

    @Reference(name = "ZUserService")
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        dataModel = ormService.newDataModel(COMPONENT_NAME, "Dual control");
        for (TableSpecs each : TableSpecs.values()) {
            each.addTo(dataModel);
        }
    }

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }
}
