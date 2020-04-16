/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.upgrade.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.DifferencesListener;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.StartupFinishedListener;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.util.Registration;

import com.google.inject.AbstractModule;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class UpgradeModule extends AbstractModule {

    private static final Logger LOGGER = Logger.getLogger(UpgradeModule.class.getName());

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
            installer.install(new DataModelUpgrader() {
                @Override
                public void upgrade(DataModel dataModel, Version version) {
                    dataModel.install(true, false);
                }

                @Override
                public Registration register(DifferencesListener listener) {
                    return () -> {/* Nothing to unregister */};
                }
            }, LOGGER);
            installed.add(installIdentifier);
        }

        @Override
        public boolean isInstalled(InstallIdentifier installIdentifier, Version version) {
            return installed.contains(installIdentifier);
        }

        @Override
        public void addStartupFinishedListener(StartupFinishedListener startupFinishedListener) {
        }

        @Override
        public DataModel newNonOrmDataModel() {
            return new InjectOnly();
        }
    }
}
