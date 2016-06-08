package com.energyict.mdc.dashboard.rest;

import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.users.UserService;

import com.google.inject.AbstractModule;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.core.Application;
import java.util.Collections;

@Component(name = "com.energyict.mdc.dashboard.init", service = {Application.class, MessageSeedProvider.class, TranslationKeyProvider.class}, immediate = true, property = {"alias=/dsr", "app=MDC", "name=" + DashboardApplication.COMPONENT_NAME})
public class DashBoardInitService {

    private volatile UserService userService;
    private volatile UpgradeService upgradeService;

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
}
