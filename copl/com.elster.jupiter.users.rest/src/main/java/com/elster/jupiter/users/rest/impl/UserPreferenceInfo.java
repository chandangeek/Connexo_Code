package com.elster.jupiter.users.rest.impl;

import com.elster.jupiter.users.UserPreference;

public class UserPreferenceInfo {
    
    public String key;
    public String value;

    public UserPreferenceInfo() {
    }

    public UserPreferenceInfo(UserPreference preference) {
        this.key = preference.getKey().getKey();
        this.value = preference.getFormatFE();
    }
}