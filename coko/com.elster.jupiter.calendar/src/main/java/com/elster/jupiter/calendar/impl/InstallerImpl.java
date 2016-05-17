package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.CalendarService;
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

    private final CalendarService calendarService;
    private final DataModel dataModel;

    @Inject
    public InstallerImpl(CalendarService calendarService, DataModel dataModel) {
        this.calendarService = calendarService;
        this.dataModel = dataModel;
    }

    public void install() {
        ExceptionCatcher.executing(
                this::createTOUCategory
        ).andHandleExceptionsWith(Throwable::printStackTrace)
                .execute();
    }

    private void createTOUCategory() {
        CategoryImpl category = this.dataModel.getInstance(CategoryImpl.class);
        category.init(CalendarServiceImpl.TIME_OF_USE_CATEGORY_NAME);
        category.save();
    }


}
