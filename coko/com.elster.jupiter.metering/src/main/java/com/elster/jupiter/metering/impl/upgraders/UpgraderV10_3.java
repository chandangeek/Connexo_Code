package com.elster.jupiter.metering.impl.upgraders;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.EventType;
import com.elster.jupiter.metering.impl.config.ReadingTypeTemplateInstaller;
import com.elster.jupiter.metering.impl.config.ServerMetrologyConfigurationService;
import com.elster.jupiter.metering.impl.ServerMeteringService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.upgrade.Upgrader;

import com.google.common.collect.ImmutableList;

import org.osgi.framework.BundleContext;

import javax.inject.Inject;
import java.sql.Statement;
import java.util.EnumSet;

import static com.elster.jupiter.orm.Version.version;

public class UpgraderV10_3 implements Upgrader {

    private static final Version VERSION = version(10, 3);
    private final BundleContext bundleContext;
    private final DataModel dataModel;
    private final ServerMetrologyConfigurationService metrologyConfigurationService;
    private final EventService eventService;
    private final ServerMeteringService meteringService;
    private final TimeService timeService;

    @Inject
    public UpgraderV10_3(BundleContext bundleContext, DataModel dataModel, ServerMeteringService meteringService, ServerMetrologyConfigurationService metrologyConfigurationService, TimeService timeService, EventService eventService) {
        this.bundleContext = bundleContext;
        this.dataModel = dataModel;
        this.metrologyConfigurationService = metrologyConfigurationService;
        this.eventService = eventService;
        this.meteringService = meteringService;
        this.timeService = timeService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, VERSION);
        dataModel.useConnectionRequiringTransaction(connection -> {
            try (Statement statement = connection.createStatement()) {
                ImmutableList.of(
                        "delete from MTR_USAGEPOINTSTATE where CONNECTIONSTATE = 'UNDER_CONSTRUCTION'",
                        "update MTR_EFFECTIVE_CONTRACT set CHANNELS_CONTAINER = " +
                                "(select MTR_CHANNEL_CONTAINER.ID from MTR_CHANNEL_CONTAINER where MTR_CHANNEL_CONTAINER.EFFECTIVE_CONTRACT = MTR_EFFECTIVE_CONTRACT.ID)"
                ).forEach(command -> execute(statement, command));
            }
        });
        installTemplates();
        installNewEventTypes();
        GasDayRelativePeriodCreator.createAll(this.meteringService, this.timeService);
    }

    private void installTemplates() {
        new ReadingTypeTemplateInstaller(metrologyConfigurationService).installTemplatesFor10_3();
    }

    private void installNewEventTypes() {
        EnumSet.of(EventType.METROLOGY_CONTRACT_DELETED)
                .forEach(eventType -> eventType.install(eventService));
    }
}