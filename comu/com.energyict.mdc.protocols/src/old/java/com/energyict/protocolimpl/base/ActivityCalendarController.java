/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.base;

import java.io.IOException;
import java.util.Calendar;

/**
 * Interface contains ActivityCalendar related functionality, including SpecialDay features
 * <p/>
 * Created by IntelliJ IDEA.
 * User: gna
 * Date: 6-okt-2010
 * Time: 16:42:47
 * To change this template use File | Settings | File Templates.
 */
public interface ActivityCalendarController {

    /**
     * Parse the given content to a proper activityCalendar
     *
     * @param content the activityCalendar content
     * @throws IOException if a parsing exception occurred
     */
    void parseContent(String content) throws IOException;

    /**
     * Write a given name to the Calendar
     *
     * @param name the name of the ActivityCalendar
     */
    void writeCalendarName(String name) throws IOException;

    /**
     * Write the complete ActivityCalendar to the device
     *
     * @throws IOException if an error occurred during the writing of the calendar
     */
    void writeCalendar() throws IOException;

    /**
     * Write the SpecialDays table to the device
     *
     * @throws IOException if an error occurred during the writing of the specailDay table
     */
    void writeSpecialDaysTable() throws IOException;

    /**
     * Write a time from which the new ActivityCalendar should be active
     *
     * @param activationDate the given time
     * @throws IOException if an error occurred during the writing of the activationdate
     */
    void writeCalendarActivationTime(Calendar activationDate) throws IOException;

    /**
     * Get the name of the current <u>Active</u> Calendar
     *
     * @return the name of the current <u>Active</u> Calendar
     * @throws IOException if a reading error occurred
     */
    String getCalendarName() throws IOException;

    /**
     * Get the name of the current <u>Passive</u> Calendar
     *
     * @return the name of the current <u>Passive</u> Calendar
     * @throws IOException if a reading error occurred
     */
    String getPassiveCalendarName() throws IOException;

}