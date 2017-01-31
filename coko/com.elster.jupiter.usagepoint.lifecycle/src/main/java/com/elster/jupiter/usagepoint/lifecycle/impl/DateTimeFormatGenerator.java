/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.impl;

import com.elster.jupiter.users.PreferenceType;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserPreference;
import com.elster.jupiter.users.UserPreferencesService;

import java.security.Principal;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

/**
 * This is a helper class to generate the date time format pattern based on the separate passed in formats
 */

public class DateTimeFormatGenerator {

    public enum Mode {LONG, SHORT}

    public static String getDateTimeFormat(String dateFormat, String timeFormat, String dateTimeOrderFormat, String separatorFormat) {
        StringBuilder dateTimeFormatBuilder = new StringBuilder();

        if (dateTimeOrderFormat.startsWith("T")) {
            dateTimeFormatBuilder.append(timeFormat);
        } else {
            dateTimeFormatBuilder.append(dateFormat);
        }
        if ("SPACE".equals(separatorFormat)) {
            dateTimeFormatBuilder.append(" ");
        } else {
            dateTimeFormatBuilder.append(" " + separatorFormat.trim() + " ");
        }
        if (dateTimeOrderFormat.startsWith("T")) {
            dateTimeFormatBuilder.append(dateFormat);
        } else {
            dateTimeFormatBuilder.append(timeFormat);
        }
        return dateTimeFormatBuilder.toString();
    }

    public static DateTimeFormatter getDateFormatForUser(DateTimeFormatGenerator.Mode dateFormatMode, DateTimeFormatGenerator.Mode timeFormatMode,
                                                         UserPreferencesService preferencesService, Principal principal) {
        // Construct the default backend date format:
        String dateTimeFormat =
                (dateFormatMode.equals(DateTimeFormatGenerator.Mode.LONG) ? "HH:mm:ss" : "HH:mm") +
                        (timeFormatMode.equals(DateTimeFormatGenerator.Mode.LONG) ? " EEE dd MMM ''yy" : " dd MMM ''yy");
        Locale locale = Locale.ENGLISH;
        if (principal instanceof User) {
            User user = (User) principal;
            Optional<UserPreference> dateFormatPref = dateFormatMode.equals(DateTimeFormatGenerator.Mode.LONG)
                    ? preferencesService.getPreferenceByKey(user, PreferenceType.LONG_DATE)
                    : preferencesService.getPreferenceByKey(user, PreferenceType.SHORT_DATE);
            Optional<UserPreference> timeFormatPref = timeFormatMode.equals(DateTimeFormatGenerator.Mode.LONG)
                    ? preferencesService.getPreferenceByKey(user, PreferenceType.LONG_TIME)
                    : preferencesService.getPreferenceByKey(user, PreferenceType.SHORT_TIME);
            Optional<UserPreference> orderFormatPref = preferencesService.getPreferenceByKey(user, PreferenceType.DATETIME_ORDER);
            Optional<UserPreference> separatorFormatPref = preferencesService.getPreferenceByKey(user, PreferenceType.DATETIME_SEPARATOR);
            if (dateFormatPref.isPresent() && timeFormatPref.isPresent() && orderFormatPref.isPresent() && separatorFormatPref.isPresent()) {
                dateTimeFormat = DateTimeFormatGenerator.getDateTimeFormat(
                        dateFormatPref.get().getFormat(),
                        timeFormatPref.get().getFormat(),
                        orderFormatPref.get().getFormat(),
                        separatorFormatPref.get().getFormat()
                );
            }
            locale = user.getLocale().orElse(locale);
        }
        return DateTimeFormatter.ofPattern(dateTimeFormat, locale);
    }
}
