/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.imports.impl.usagepoint;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.OutOfTheBoxCategory;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.imports.impl.MessageSeeds;
import com.elster.jupiter.metering.imports.impl.exceptions.ProcessorException;
import com.elster.jupiter.metering.imports.impl.fields.CommonField;
import com.elster.jupiter.metering.imports.impl.fields.FieldSetter;
import com.elster.jupiter.metering.imports.impl.fields.FileImportField;
import com.elster.jupiter.metering.imports.impl.parsers.InstantParser;
import com.elster.jupiter.metering.imports.impl.parsers.LiteralStringParser;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Wrapper for {@link com.elster.jupiter.calendar.OutOfTheBoxCategory}
 * that adds behavior that is specific to the import process.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-02-27 (16:24)
 */
enum OutOfTheBoxCategoryForImport {
    TOU {
        @Override
        protected String calendarNameFieldName() {
            return "touCalendarName";
        }

        @Override
        protected String calendarUsageStartTimeFieldName() {
            return "touCalendarUsageStartTime";
        }

        @Override
        protected FieldSetter<String> calendarNameFieldSetter(UsagePointImportRecord record) {
            return record::setTouCalendarName;
        }

        @Override
        protected FieldSetter<Instant> calendarUsageStartTimeFieldSetter(UsagePointImportRecord record) {
            return record::setTouCalendarUsageStartTime;
        }

        @Override
        OutOfTheBoxCategory outOfTheBoxCategory() {
            return OutOfTheBoxCategory.TOU;
        }

        @Override
        Supplier<Optional<String>> calendarNameSupplier(UsagePointImportRecord data) {
            return data::getTouCalendarName;
        }

        @Override
        Supplier<Optional<Instant>> usageStartTimeSupplier(UsagePointImportRecord data) {
            return data::getTouCalendarUsageStartTime;
        }

    },

    WORKFORCE {
        @Override
        protected String calendarNameFieldName() {
            return "workForceCalendarName";
        }

        @Override
        protected String calendarUsageStartTimeFieldName() {
            return "workForceCalendarUsageStartTime";
        }

        @Override
        protected FieldSetter<String> calendarNameFieldSetter(UsagePointImportRecord record) {
            return record::setWorkForceCalendarName;
        }

        @Override
        protected FieldSetter<Instant> calendarUsageStartTimeFieldSetter(UsagePointImportRecord record) {
            return record::setWorkForceCalendarUsageStartTime;
        }

        @Override
        OutOfTheBoxCategory outOfTheBoxCategory() {
            return OutOfTheBoxCategory.WORKFORCE;
        }

        @Override
        Supplier<Optional<String>> calendarNameSupplier(UsagePointImportRecord data) {
            return data::getWorkForceCalendarName;
        }

        @Override
        Supplier<Optional<Instant>> usageStartTimeSupplier(UsagePointImportRecord data) {
            return data::getWorkForceCalendarUsageStartTime;
        }

    },

    COMMANDS {
        @Override
        protected String calendarNameFieldName() {
            return "commandsCalendarName";
        }

        @Override
        protected String calendarUsageStartTimeFieldName() {
            return "commandsCalendarUsageStartTime";
        }

        @Override
        protected FieldSetter<String> calendarNameFieldSetter(UsagePointImportRecord record) {
            return record::setCommandsCalendarName;
        }

        @Override
        protected FieldSetter<Instant> calendarUsageStartTimeFieldSetter(UsagePointImportRecord record) {
            return record::setCommandsCalendarUsageStartTime;
        }

        @Override
        OutOfTheBoxCategory outOfTheBoxCategory() {
            return OutOfTheBoxCategory.COMMANDS;
        }

        @Override
        Supplier<Optional<String>> calendarNameSupplier(UsagePointImportRecord data) {
            return data::getCommandsCalendarName;
        }

        @Override
        Supplier<Optional<Instant>> usageStartTimeSupplier(UsagePointImportRecord data) {
            return data::getCommandsCalendarUsageStartTime;
        }

    };

    void addFields(Map<String, FileImportField<?>> fields, UsagePointImportRecord record, ParserProvider parserProvider) {
        fields.put(
                this.calendarNameFieldName(),
                CommonField
                        .withParser(parserProvider.stringParser())
                        .withSetter(this.calendarNameFieldSetter(record))
                        .withName(this.calendarNameFieldName())
                        .build());
        fields.put(
                this.calendarUsageStartTimeFieldName(),
                CommonField
                        .withParser(parserProvider.instantParser())
                        .withSetter(this.calendarUsageStartTimeFieldSetter(record))
                        .withName(this.calendarUsageStartTimeFieldName())
                        .build());
    }

    protected abstract String calendarNameFieldName();

    protected abstract String calendarUsageStartTimeFieldName();

    protected abstract FieldSetter<String> calendarNameFieldSetter(UsagePointImportRecord record);

    protected abstract FieldSetter<Instant> calendarUsageStartTimeFieldSetter(UsagePointImportRecord record);

    void addCalendar(UsagePointImportRecord data, UsagePoint usagePoint, ServiceProvider serviceProvider) {
        this.addCalendar(
                this.outOfTheBoxCategory(),
                this.calendarNameSupplier(data),
                this.usageStartTimeSupplier(data),
                data,
                usagePoint,
                serviceProvider);
    }

    abstract OutOfTheBoxCategory outOfTheBoxCategory();

    abstract Supplier<Optional<String>> calendarNameSupplier(UsagePointImportRecord data);

    abstract Supplier<Optional<Instant>> usageStartTimeSupplier(UsagePointImportRecord data);

    protected void addCalendar(OutOfTheBoxCategory category, Supplier<Optional<String>> calendarNameSupplier, Supplier<Optional<Instant>> usageStartTimeSupplier, UsagePointImportRecord data, UsagePoint usagePoint, ServiceProvider serviceProvider) {
        calendarNameSupplier.get()
                .ifPresent(calendarName ->
                    this.addCalendar(
                            category,
                            calendarName,
                            usageStartTimeSupplier.get(),
                            data,
                            usagePoint,
                            serviceProvider));
    }

    protected void addCalendar(OutOfTheBoxCategory category, String calendarName, Optional<Instant> usageStartTime, UsagePointImportRecord data, UsagePoint usagePoint, ServiceProvider serviceProvider) {
        Calendar calendar = serviceProvider.calendarService()
                .findCalendarByName(calendarName)
                .orElseThrow(() -> new ProcessorException(MessageSeeds.CALENDAR_DOES_NOT_EXIST, data.getLineNumber(), category.name()));
        if (usageStartTime.isPresent()) {
            usagePoint
                    .getUsedCalendars()
                    .addCalendar(calendar, usageStartTime.get());
        } else {
            usagePoint.getUsedCalendars().addCalendar(calendar);
        }
    }

    interface ParserProvider {
        LiteralStringParser stringParser();
        InstantParser instantParser();
    }

    interface ServiceProvider {
        CalendarService calendarService();
    }

}