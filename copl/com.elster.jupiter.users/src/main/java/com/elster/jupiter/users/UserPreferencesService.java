/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users;

import aQute.bnd.annotation.ProviderType;

import java.security.Principal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@ProviderType
public interface UserPreferencesService {
    
    UserPreference createUserPreference(Locale locale, PreferenceType key, String formatBE, String formatFE, boolean isDefault);

    List<Locale> getSupportedLocales();
    
    List<UserPreference> getPreferences(User user);
    
    Optional<UserPreference> getPreferenceByKey(User user, PreferenceType format);
    
    Optional<UserPreference> getPreferenceByKey(Locale locale, PreferenceType format);

    /***
     * @param principal
     * @param preferenceTypes
     * @return dateTimeFormatter which is configured based on user preferences: date and time formats.
     * If date and time preferences are not configured for provided user, then default format is using: <>
     * Note that maximum two preference types can be passed in arguments: one for date and another one for time
     */
    DateTimeFormatter getDateTimeFormatter(Principal principal, PreferenceType... preferenceTypes);
}
