package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.EventType;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.FullInstaller;

import javax.inject.Inject;
import java.util.logging.Logger;

class InstallerImpl implements FullInstaller {

    private static final Logger LOGGER = Logger.getLogger(InstallerImpl.class.getName());

    private final EventService eventService;
    private final CalendarService calendarService;
    private final DataModel dataModel;

    @Inject
    public InstallerImpl(EventService eventService, DataModel dataModel) {
        this.eventService = eventService;
        this.dataModel = dataModel;
    }

    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        dataModelUpgrader.upgrade(dataModel, Version.latest());
        doTry(
                "Create default Calendar categories.",
                this::createTOUCategory,
                logger
        );
        doTry(
                "Create event types for CAL.",
                this::createEventTypes,
                logger
        );
    }

    private void createTOUCategory() {
        CategoryImpl category = this.dataModel.getInstance(CategoryImpl.class);
        category.init(CalendarServiceImpl.TIME_OF_USE_CATEGORY_NAME);
        category.save();
    }

    private void createEventTypes() {
        for (EventType eventType : EventType.values()) {
            eventType.install(eventService);
        }
    }


}
