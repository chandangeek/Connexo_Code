/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.tasks.TaskExecutor;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.time.StopWatch;

import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Extends the TimeSeries of one or more {@link com.elster.jupiter.calendar.Calendar}s
 * with one additional year according to the specs of the Calendar.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-02-24 (10:22)
 */
public class CalendarTimeSeriesExtenderHandler implements TaskExecutor {
    // Payload that indicates that all calendars must be recalculated
    static final String GLOBAL_START_PAYLOAD = "GLOBAL";

    private final TransactionService transactionService;
    private final ServerCalendarService calendarService;
    private final EventService eventService;

    public CalendarTimeSeriesExtenderHandler(TransactionService transactionService, ServerCalendarService calendarService, EventService eventService) {
        this.transactionService = transactionService;
        this.calendarService = calendarService;
        this.eventService = eventService;
    }

    @Override
    public void execute(TaskOccurrence occurrence) {
        /* Extenting all existing calendars is done with a single transaction
         * per calendar and because we are managing the transaction(s)
         * ourselves, we must move all logic to the postExecute method. */
    }

    @Override
    public void postExecute(TaskOccurrence occurrence) {
        new Worker(occurrence).execute();
    }

    private class Worker {
        private final Logger logger;
        private final Handler handler;
        private long expectedCount;
        private long currentCount;
        private long successCount;
        private long failureCount;
        private TaskOccurrence occurrence;

        private Worker(TaskOccurrence taskOccurrence) {
            this.logger = Logger.getAnonymousLogger();
            this.occurrence = taskOccurrence;
            this.handler = taskOccurrence.createTaskLogHandler().asHandler();
            this.logger.addHandler(handler);
        }

        void execute() {
            try {
                this.extendAllExistingCalendarTimeSeries();
            } catch (RuntimeException e) {
                this.logger.log(Level.SEVERE, "Unexpected failure: " + e.getMessage(), e);
            } finally {
                try (TransactionContext context = transactionService.getContext()) {
                    if (successCount == this.expectedCount) {
                        this.logger.info(() -> "Successfully completed the extension of all calendar(s) in the sytem");
                    } else {
                        this.logger.info(() -> this.successCount + " calendar(s) were succesfully extended, " + this.failureCount + " calendar(s) failed to extend");
                    }
                    context.commit();
                }
                this.logger.removeHandler(this.handler);
            }
        }

        private void extendAllExistingCalendarTimeSeries() {
            List<ServerCalendar> calendars = this.findAllCalendars();
            this.expectedCount = calendars.size();
            this.currentCount = 1;
            calendars.forEach(this::extendAndBumpEndYear);
        }

        private List<ServerCalendar> findAllCalendars() {
            return calendarService.findAllCalendarsForExtension();
        }

        private void extendAndBumpEndYear(ServerCalendar calendar) {
            StopWatch stopWatch = new StopWatch();
            try (TransactionContext context = transactionService.getContext()) {
                this.logger.fine(() -> "Now extending calendar " + calendar.getName() + " with id " + calendar.getId() + " (" + this.currentCount + " of " + this.expectedCount + ")");
                calendar.extendAllTimeSeries();
                calendar.bumpEndYear();
                this.logger.fine(() -> "Extending calendar " + calendar.getId() + " took " + stopWatch.getElapsed() + " nanos");
                context.commit();
                stopWatch.stop();
                this.successCount++;
            } catch (RuntimeException e) {
                String errorMsg = "Failure to extend calendar " + calendar.getId() + ": " + e.getLocalizedMessage();
                postFailEvent(eventService, occurrence, errorMsg);
                this.failureCount++;
                try (TransactionContext context = transactionService.getContext()) {
                    this.logger.log(Level.SEVERE, e, () -> errorMsg);
                    context.commit();
                }
            } finally {
                this.currentCount++;
            }
        }

    }
}