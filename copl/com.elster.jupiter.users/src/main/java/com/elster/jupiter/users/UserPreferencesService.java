package com.elster.jupiter.users;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

public interface UserPreferencesService {
    
    UserPreference createUserPreference(Locale locale, FormatKey key, String formatBE, String formatFE, boolean isDefault);
    
    List<Locale> getSupportedLocales();
    
    List<UserPreference> getPreferences(User user);
    
    Optional<UserPreference> getPreferenceByKey(User user, FormatKey format);
    
    Optional<UserPreference> getPreferenceByKey(Locale locale, FormatKey format);
}
