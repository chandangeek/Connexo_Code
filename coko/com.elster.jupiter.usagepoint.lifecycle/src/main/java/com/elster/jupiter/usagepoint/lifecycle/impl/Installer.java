package com.elster.jupiter.usagepoint.lifecycle.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycle;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycleConfigurationService;

import javax.inject.Inject;
import java.util.logging.Logger;

public class Installer implements FullInstaller {
    private final DataModel dataModel;
    private final Thesaurus thesaurus;
    private final UsagePointLifeCycleConfigurationService usagePointLifeCycleConfigurationService;

    @Inject
    public Installer(DataModel dataModel,
                     Thesaurus thesaurus,
                     UsagePointLifeCycleConfigurationService usagePointLifeCycleConfigurationService) {
        this.dataModel = dataModel;
        this.thesaurus = thesaurus;
        this.usagePointLifeCycleConfigurationService = usagePointLifeCycleConfigurationService;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        dataModelUpgrader.upgrade(this.dataModel, Version.latest());
        doTry(
                "Create default usage point lifecycle",
                this::createLifeCycle,
                logger
        );
    }

    private void createLifeCycle() {
        UsagePointLifeCycle lifeCycle = this.usagePointLifeCycleConfigurationService.newUsagePointLifeCycle(this.thesaurus.getFormat(TranslationKeys.LIFE_CYCLE_NAME).format());
        lifeCycle.markAsDefault();
    }
}
