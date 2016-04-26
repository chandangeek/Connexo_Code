package com.elster.jupiter.upgrade.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.Upgrader;

import com.google.inject.AbstractModule;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class UpgradeModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(UpgradeService.class).to(UpgradeServiceImpl.class);

    }

    public static class FakeUpgradeService implements UpgradeService {

        private Set<InstallIdentifier> installed = new HashSet<>();

        public static FakeUpgradeService getInstance() {
            return new FakeUpgradeService();
        }

        @Override
        public void register(InstallIdentifier installIdentifier, DataModel dataModel, Class<? extends FullInstaller> installerClass, Map<Version, Class<? extends Upgrader>> upgraders) {
            FullInstaller installer = dataModel.getInstance(installerClass);
            installer.install((dataModel1, version) -> dataModel1.install(true, false));
            installed.add(installIdentifier);
        }

        @Override
        public boolean isInstalled(InstallIdentifier installIdentifier, Version version) {
            return installed.contains(installIdentifier);
        }
    }
}
