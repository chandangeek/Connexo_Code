package com.elster.jupiter.users;

import java.util.Locale;

public interface UserPreference {
    
    Locale getLocale();
    
    FormatKey getKey();
    
    String getFormatBE();
    
    String getFormatFE();
    
    boolean isDefault();

}
