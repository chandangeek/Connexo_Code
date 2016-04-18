package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.orm.DataModel;

import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by igh on 18/04/2016.
 */
public class Installer {

    private static final Logger LOGGER = Logger.getLogger(Installer.class.getName());

    private final CalendarService calendarService;
    private final DataModel dataModel;

    @Inject
    public Installer(CalendarService calendarService, DataModel dataModel) {
        this.calendarService = calendarService;
        this.dataModel = dataModel;
    }

    public void install() {
        try {
            this.dataModel.install(true, true);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }


}
