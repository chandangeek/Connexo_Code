package com.elster.jupiter.metering.impl.upgraders;

import com.elster.jupiter.metering.impl.config.ReadingTypeTemplateInstaller;
import com.elster.jupiter.metering.impl.config.ServerMetrologyConfigurationService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;

import org.osgi.framework.BundleContext;

import javax.inject.Inject;

import static com.elster.jupiter.orm.Version.version;

public class UpgraderV10_3 implements Upgrader {

    private static final Version VERSION = version(10, 3);
    private final BundleContext bundleContext;
    private final DataModel dataModel;
    private final ServerMetrologyConfigurationService metrologyConfigurationService;

    @Inject
    public UpgraderV10_3(BundleContext bundleContext, DataModel dataModel, ServerMetrologyConfigurationService metrologyConfigurationService) {
        this.bundleContext = bundleContext;
        this.dataModel = dataModel;
        this.metrologyConfigurationService = metrologyConfigurationService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, VERSION);
        installTemplates();
    }

    private void installTemplates() {
        new ReadingTypeTemplateInstaller(metrologyConfigurationService).installTemplatesFor10_3();
    }
}