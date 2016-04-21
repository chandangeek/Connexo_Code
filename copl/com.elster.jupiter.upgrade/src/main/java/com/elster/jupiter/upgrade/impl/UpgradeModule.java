package com.elster.jupiter.upgrade.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.Upgrader;

import com.google.inject.AbstractModule;

import java.util.Map;

public class UpgradeModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(UpgradeService.class).to(UpgradeServiceImpl.class);

    }

    public enum FakeUpgradeService implements UpgradeService {
        INSTANCE;

        @Override
        public void register(InstallIdentifier installIdentifier, DataModel dataModel, Class<? extends FullInstaller> installerClass, Map<Version, Class<? extends Upgrader>> upgraders) {
            FullInstaller installer = dataModel.getInstance(installerClass);
            installer.install((dataModel1, version) -> dataModel1.install(true, true));
        }
    }
}
