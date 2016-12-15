package com.elster.jupiter.metering.impl.upgraders;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.EventType;
import com.elster.jupiter.metering.impl.ServerMeteringService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;
import java.util.EnumSet;

import static com.elster.jupiter.orm.Version.version;

public class UpgraderV10_3 implements Upgrader {

    private static final Version VERSION = version(10, 3);
    private final DataModel dataModel;
    private final EventService eventService;
    private final ServerMeteringService meteringService;
    private final TimeService timeService;

    @Inject
    public UpgraderV10_3(DataModel dataModel,
                         ServerMeteringService meteringService,
                         TimeService timeService,
                         EventService eventService) {
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.meteringService = meteringService;
        this.timeService = timeService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, VERSION);
        installNewEventTypes();
        GasDayRelativePeriodCreator.createAll(this.meteringService, this.timeService);
    }

    private void installNewEventTypes() {
        EnumSet.of(EventType.METROLOGY_CONTRACT_DELETED)
                .forEach(eventType -> eventType.install(eventService));
    }
}
