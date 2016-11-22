package com.elster.jupiter.metering.impl.upgraders;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.EventType;
import com.elster.jupiter.metering.impl.config.ReadingTypeTemplateInstaller;
import com.elster.jupiter.metering.impl.config.ServerMetrologyConfigurationService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;

import org.osgi.framework.BundleContext;

import javax.inject.Inject;
import java.util.EnumSet;

import static com.elster.jupiter.orm.Version.version;

public class UpgraderV10_3 implements Upgrader {

    private static final Version VERSION = version(10, 3);
    private final BundleContext bundleContext;
    private final DataModel dataModel;
    private final ServerMetrologyConfigurationService metrologyConfigurationService;
    private final EventService eventService;

    @Inject
    public UpgraderV10_3(BundleContext bundleContext, DataModel dataModel, ServerMetrologyConfigurationService metrologyConfigurationService, EventService eventService) {
        this.bundleContext = bundleContext;
        this.dataModel = dataModel;
        this.metrologyConfigurationService = metrologyConfigurationService;
        this.eventService = eventService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, VERSION);
        installTemplates();
        installNewEventTypes();
    }

    private void installTemplates() {
        new ReadingTypeTemplateInstaller(metrologyConfigurationService).installTemplatesFor10_3();
    }

    private void installNewEventTypes() {
        EnumSet.of(EventType.METROLOGY_CONTRACT_DELETED)
                .forEach(eventType -> eventType.install(eventService));
    }
}