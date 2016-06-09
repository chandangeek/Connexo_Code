package com.energyict.mdc.dashboard.rest;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.dashboard.rest.status.impl.ComSessionSuccessIndicatorTranslationKeys;
import com.energyict.mdc.dashboard.rest.status.impl.CompletionCodeTranslationKeys;
import com.energyict.mdc.dashboard.rest.status.impl.ConnectionStrategyTranslationKeys;
import com.energyict.mdc.dashboard.rest.status.impl.ConnectionTaskSuccessIndicatorTranslationKeys;
import com.energyict.mdc.dashboard.rest.status.impl.MessageSeeds;
import com.energyict.mdc.dashboard.rest.status.impl.TaskStatusTranslationKeys;
import com.energyict.mdc.dashboard.rest.status.impl.TranslationKeys;
import com.energyict.mdc.engine.config.EngineConfigurationService;

import com.google.inject.AbstractModule;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component(name = "com.energyict.mdc.dashboard.init", service = {DashBoardInitService.class, MessageSeedProvider.class, TranslationKeyProvider.class}, immediate = true, property = {"name=" + DashboardApplication.COMPONENT_NAME})
public class DashBoardInitService implements MessageSeedProvider, TranslationKeyProvider {

    private volatile UserService userService;
    private volatile UpgradeService upgradeService;
    private volatile EngineConfigurationService engineConfigurationService;

    @Activate
    public void activate() {
        DataModel dataModel = upgradeService.newNonOrmDataModel();
        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(UserService.class).toInstance(userService);
            }
        });
        upgradeService.register(InstallIdentifier.identifier("DSI"), dataModel, DashboardApplicationInstaller.class, Collections
                .emptyMap());
    }


    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    @Reference
    public void setEngineConfigurationService(EngineConfigurationService engineConfigurationService) {
        this.engineConfigurationService = engineConfigurationService;
    }

    @Override
    public Layer getLayer() {
        return Layer.REST;
    }

    @Override
    public String getComponentName() {
        return DashboardApplication.COMPONENT_NAME;
    }

    @Override
    public List<TranslationKey> getKeys() {
        List<TranslationKey> keys = new ArrayList<>();
        keys.addAll(Arrays.asList(TranslationKeys.values()));
        keys.addAll(Arrays.asList(TaskStatusTranslationKeys.values()));
        keys.addAll(Arrays.asList(ConnectionTaskSuccessIndicatorTranslationKeys.values()));
        keys.addAll(Arrays.asList(ComSessionSuccessIndicatorTranslationKeys.values()));
        keys.addAll(Arrays.asList(CompletionCodeTranslationKeys.values()));
        keys.addAll(Arrays.asList(ConnectionStrategyTranslationKeys.values()));
        return keys;
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

}
