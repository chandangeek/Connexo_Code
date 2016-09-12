package com.elster.jupiter.users;

import java.util.Locale;

public interface UserPreference {
    
    Locale getLocale();
    
    PreferenceType getType();
    
    String getFormat();
    
    String getDisplayFormat();
    
    boolean isDefault();

}
