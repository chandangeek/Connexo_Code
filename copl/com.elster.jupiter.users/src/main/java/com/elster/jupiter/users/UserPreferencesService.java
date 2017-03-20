/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users;

import java.security.Principal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public interface UserPreferencesService {
    
    UserPreference createUserPreference(Locale locale, PreferenceType key, String formatBE, String formatFE, boolean isDefault);

    List<Locale> getSupportedLocales();
    
    List<UserPreference> getPreferences(User user);
    
    Optional<UserPreference> getPreferenceByKey(User user, PreferenceType format);
    
    Optional<UserPreference> getPreferenceByKey(Locale locale, PreferenceType format);

    /***
     * This method is used to receive date time formatter for user and selected preference types.
     * Note that there cannot be more than 2 preference types
     * @param principal
     * @param preferenceTypes
     * @return
     */
    DateTimeFormatter getDateTimeFormatter(Principal principal, PreferenceType... preferenceTypes);
}
