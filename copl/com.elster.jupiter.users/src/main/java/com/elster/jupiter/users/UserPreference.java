/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users;

import java.util.Locale;

public interface UserPreference {
    
    Locale getLocale();
    
    PreferenceType getType();
    
    String getFormat();
    
    String getDisplayFormat();
    
    boolean isDefault();

}
