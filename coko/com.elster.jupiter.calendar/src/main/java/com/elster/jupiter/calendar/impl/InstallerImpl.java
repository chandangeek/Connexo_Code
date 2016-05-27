package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.EventType;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.exception.ExceptionCatcher;

import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by igh on 18/04/2016.
 */
public class InstallerImpl {

    private static final Logger LOGGER = Logger.getLogger(InstallerImpl.class.getName());

    private final EventService eventService;
    private final DataModel dataModel;

    @Inject
    public InstallerImpl(EventService eventService, DataModel dataModel) {
        this.eventService = eventService;
        this.dataModel = dataModel;
    }

    public void install() {
        ExceptionCatcher.executing(
                this::createTOUCategory,
                this::createEventTypes
        ).andHandleExceptionsWith(Throwable::printStackTrace)
                .execute();
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
