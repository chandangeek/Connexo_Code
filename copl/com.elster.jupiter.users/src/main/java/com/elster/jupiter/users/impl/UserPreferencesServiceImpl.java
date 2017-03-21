/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.users.PreferenceType;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserPreference;
import com.elster.jupiter.users.UserPreferencesService;

import java.security.Principal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UserPreferencesServiceImpl implements UserPreferencesService {

    public final DataModel dataModel;

    public UserPreferencesServiceImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public UserPreference createUserPreference(Locale locale, PreferenceType key, String formatBE, String formatFE, boolean isDefault) {
        UserPreferenceImpl userPreference = new UserPreferenceImpl();
        userPreference.setLanguageTag(locale != null ? locale.toLanguageTag() : null);
        userPreference.setKey(key);
        userPreference.setFormatBE(formatBE);
        userPreference.setFormatFE(formatFE);
        userPreference.setDefault(isDefault);
        Save.CREATE.save(dataModel, userPreference);
        return userPreference;
    }

    @Override
    public List<Locale> getSupportedLocales() {
        Map<Locale, List<UserPreference>> locales = dataModel.mapper(UserPreference.class).find().stream().collect(Collectors.groupingBy(up -> up.getLocale()));
        List<Locale> supportedLocales = new ArrayList<>(locales.size());
        supportedLocales.addAll(locales.keySet());
        return supportedLocales;
    }

    @Override
    public List<UserPreference> getPreferences(User user) {
        return dataModel.mapper(UserPreference.class).find(
                new String[] { "locale", "isDefault" },
                new Object[] { user.getLocale().orElse(Locale.getDefault()).toLanguageTag(), true });
    }

    @Override
    public Optional<UserPreference> getPreferenceByKey(User user, PreferenceType key) {
        return this.getPreferenceByKey(user.getLocale().orElse(Locale.getDefault()), key);
    }

    @Override
    public Optional<UserPreference> getPreferenceByKey(Locale locale, PreferenceType key) {
        return dataModel.mapper(UserPreference.class).getUnique(
                new String[] { "locale", "key", "isDefault" },
                new Object[] { locale.toLanguageTag(), key, true });
    }

    @Override
    public DateTimeFormatter getDateTimeFormatter(Principal principal, PreferenceType... preferenceTypes) {
        // Construct the default backend date format:

        String dateTimeFormat = "HH:mm:ss EEE dd MMM ''yy";
        Locale locale = Locale.ENGLISH;
        if (principal instanceof User) {
            User user = (User) principal;
            Optional<UserPreference> dateFormatPref = Stream.of(preferenceTypes)
                    .filter(EnumSet.of(PreferenceType.SHORT_DATE, PreferenceType.LONG_DATE)::contains)
                    .distinct()
                    .reduce((p1, p2) -> {
                        throw new IllegalArgumentException("More than two preference types are defined.");
                    })
                    .flatMap(timeFormat -> getPreferenceByKey(user, timeFormat));
            Optional<UserPreference> timeFormatPref = Stream.of(preferenceTypes)
                    .filter(EnumSet.of(PreferenceType.SHORT_TIME, PreferenceType.LONG_TIME)::contains)
                    .distinct()
                    .reduce((p1, p2) -> {
                        throw new IllegalArgumentException("More than two preference types are defined.");
                    })
                    .flatMap(timeFormat -> getPreferenceByKey(user, timeFormat));
            Optional<UserPreference> orderFormatPref = getPreferenceByKey(user, PreferenceType.DATETIME_ORDER);
            Optional<UserPreference> separatorFormatPref = getPreferenceByKey(user, PreferenceType.DATETIME_SEPARATOR);
            if (dateFormatPref.isPresent() && timeFormatPref.isPresent() && orderFormatPref.isPresent() && separatorFormatPref.isPresent()) {
                dateTimeFormat = getDateTimeFormat(
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

    private String getDateTimeFormat(String dateFormat, String timeFormat, String dateTimeOrderFormat, String separatorFormat) {
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
}