package com.elster.jupiter.metering.impl.upgraders;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.EventType;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.impl.UsagePointImpl;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycleConfigurationService;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointState;
import com.elster.jupiter.util.conditions.Condition;

import javax.inject.Inject;
import java.util.EnumSet;

import static com.elster.jupiter.orm.Version.version;

public class UpgraderV10_3 implements Upgrader {

    private static final Version VERSION = version(10, 3);
    private final DataModel dataModel;
    private final EventService eventService;
    private final MeteringService meteringService;
    private final UsagePointLifeCycleConfigurationService lifeCycleConfigurationService;

    @Inject
    public UpgraderV10_3(DataModel dataModel, EventService eventService, MeteringService meteringService, UsagePointLifeCycleConfigurationService lifeCycleConfigurationService) {
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.meteringService = meteringService;
        this.lifeCycleConfigurationService = lifeCycleConfigurationService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, VERSION);
        installNewEventTypes();
        setInitialStateForUsagePoints();
    }

    private void installNewEventTypes() {
        EnumSet.of(EventType.METROLOGY_CONTRACT_DELETED)
                .forEach(eventType -> eventType.install(eventService));
    }

    private void setInitialStateForUsagePoints() {
        UsagePointState initialState = this.lifeCycleConfigurationService.getDefaultLifeCycle().getStates()
                .stream()
                .filter(UsagePointState::isInitial)
                .findFirst()
                .get();
        this.meteringService.getUsagePointQuery().select(Condition.TRUE)
                .stream()
                .forEach(usagePoint -> ((UsagePointImpl) usagePoint).setState(initialState, usagePoint.getInstallationTime()));
    }
}

